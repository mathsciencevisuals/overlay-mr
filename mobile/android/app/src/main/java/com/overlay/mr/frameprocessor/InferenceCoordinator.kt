package com.overlay.mr.frameprocessor

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InferenceCoordinator(private val context: Context) {
    private val mediaPipeGraphManager = MediaPipeGraphManager(context)
    private val segmentationEngine = SegmentationEngine(context)
    @Volatile
    private var config: InferenceConfig = InferenceConfig()
    private var frameCounter = 0L
    private var latestPose: PoseTrackingPacket? = null
    private var latestSegmentation: SegmentationPacket? = null
    private var latestBodyPartSegmentation: SegmentationPacket? = null

    suspend fun initialize(config: InferenceConfig) = withContext(Dispatchers.Default) {
        this@InferenceCoordinator.config = config
        mediaPipeGraphManager.initialize()
        segmentationEngine.initialize()
    }

    fun updateConfig(config: InferenceConfig) {
        this.config = config
    }

    suspend fun processFrame(frame: FrameEnvelope): InferenceResult {
        return withContext(Dispatchers.Default) {
            val startMs = SystemClock.elapsedRealtime()
            frameCounter += 1
            val mediaPipeInference = mediaPipeGraphManager.run(frame, config)

            val faceOutput = mediaPipeInference.face
                ?: error("Face landmarker output is required for MR session")
            val face = FaceTrackingPacket(
                timestamp = frame.timestamp,
                landmarks = faceOutput.landmarks,
                boundingBox = faceOutput.boundingBox,
                blendshapeScores = faceOutput.blendshapeScores,
                facialTransformationMatrix = faceOutput.facialTransformationMatrix
            )

            if (frame.bodyTrackingEnabled && shouldRun(frameCounter, config.poseStride)) {
                val poseOutput = mediaPipeInference.pose
                if (poseOutput != null) {
                    latestPose = PoseTrackingPacket(
                        timestamp = frame.timestamp,
                        landmarks = poseOutput.landmarks
                    )
                }
            }

            if (frame.segmentationEnabled || frame.bodyPartSegmentationEnabled) {
                val segmentationInference = segmentationEngine.run(frame, config)
                if (frame.segmentationEnabled &&
                    shouldRun(frameCounter, config.segmentationStride) &&
                    segmentationInference.personMask != null
                ) {
                    latestSegmentation = SegmentationPacket(
                        timestamp = frame.timestamp,
                        width = segmentationInference.personMask.width,
                        height = segmentationInference.personMask.height,
                        textureId = segmentationInference.personMask.textureId,
                        kind = SegmentationKind.PERSON
                    )
                }

                if (frame.bodyPartSegmentationEnabled &&
                    shouldRun(frameCounter, config.bodyPartSegmentationStride) &&
                    segmentationInference.bodyPartMasks.isNotEmpty()
                ) {
                    latestBodyPartSegmentation = SegmentationPacket(
                        timestamp = frame.timestamp,
                        width = segmentationInference.bodyPartMasks.first().width,
                        height = segmentationInference.bodyPartMasks.first().height,
                        textureId = null,
                        kind = SegmentationKind.BODY_PARTS,
                        partMasks = segmentationInference.bodyPartMasks.map {
                            BodyPartMask(
                                part = it.part,
                                textureId = it.textureId,
                                confidence = it.averageConfidence
                            )
                        }
                    )
                }
            }

            InferenceResult(
                face = face,
                pose = if (frame.bodyTrackingEnabled) latestPose else null,
                segmentation = when {
                    frame.bodyPartSegmentationEnabled -> latestBodyPartSegmentation
                    frame.segmentationEnabled -> latestSegmentation
                    else -> null
                },
                inferenceMs = currentInferenceMs(startMs)
            )
        }
    }

    fun currentInferenceMs(startMs: Long): Double {
        return (SystemClock.elapsedRealtime() - startMs).toDouble()
    }

    fun close() {
        mediaPipeGraphManager.close()
        segmentationEngine.close()
    }

    private fun shouldRun(frameCounter: Long, stride: Int): Boolean {
        return stride <= 1 || frameCounter % stride.toLong() == 0L
    }
}
