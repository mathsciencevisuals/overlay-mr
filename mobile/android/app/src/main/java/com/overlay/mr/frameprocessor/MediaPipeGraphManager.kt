package com.overlay.mr.frameprocessor

import android.content.Context
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import java.io.FileNotFoundException

class MediaPipeGraphManager(private val context: Context) {
    private val frameConverter = MediaPipeFrameConverter()
    private val resultMapper = MediaPipeResultMapper()
    private var taskConfig: MediaPipeTaskConfig = MediaPipeTaskConfig()
    private var faceLandmarker: FaceLandmarker? = null
    private var poseLandmarker: PoseLandmarker? = null

    fun initialize(config: MediaPipeTaskConfig = MediaPipeTaskConfig()) {
        taskConfig = config
        verifyAsset(taskConfig.faceAssetPath)
        verifyAsset(taskConfig.poseAssetPath)
        faceLandmarker = createFaceLandmarker(taskConfig)
        poseLandmarker = createPoseLandmarker(taskConfig)
    }

    fun run(frame: FrameEnvelope, config: InferenceConfig): MediaPipeInference {
        val mpImage = frameConverter.toMpImage(frame)
        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setRotationDegrees(frame.rotationDegrees)
            .build()

        return try {
            val timestampMs = frame.timestamp
            val faceResult = faceLandmarker?.detectForVideo(mpImage, imageProcessingOptions, timestampMs)
            val poseResult = if (frame.bodyTrackingEnabled && config.bodyTrackingEnabled) {
                poseLandmarker?.detectForVideo(mpImage, imageProcessingOptions, timestampMs)
            } else {
                null
            }

            MediaPipeInference(
                face = faceResult?.let(resultMapper::mapFace) ?: fallbackFace(frame.timestamp),
                pose = poseResult?.let(resultMapper::mapPose) ?: fallbackPose(frame, config)
            )
        } finally {
            mpImage.close()
        }
    }

    fun close() {
        faceLandmarker?.close()
        poseLandmarker?.close()
        faceLandmarker = null
        poseLandmarker = null
    }

    private fun createFaceLandmarker(config: MediaPipeTaskConfig): FaceLandmarker {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(config.faceAssetPath)
            .build()

        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(config.runningMode.toNativeRunningMode())
            .setNumFaces(config.maxFaces)
            .setMinFaceDetectionConfidence(config.minFaceDetectionConfidence)
            .setMinFacePresenceConfidence(config.minFacePresenceConfidence)
            .setMinTrackingConfidence(config.minFaceTrackingConfidence)
            .setOutputFaceBlendshapes(config.outputFaceBlendshapes)
            .setOutputFacialTransformationMatrixes(config.outputFacialTransformationMatrixes)
            .build()

        return FaceLandmarker.createFromOptions(context, options)
    }

    private fun createPoseLandmarker(config: MediaPipeTaskConfig): PoseLandmarker {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(config.poseAssetPath)
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(config.runningMode.toNativeRunningMode())
            .setNumPoses(config.maxPoses)
            .setMinPoseDetectionConfidence(config.minPoseDetectionConfidence)
            .setMinPosePresenceConfidence(config.minPosePresenceConfidence)
            .setMinTrackingConfidence(config.minPoseTrackingConfidence)
            .setOutputSegmentationMasks(false)
            .build()

        return PoseLandmarker.createFromOptions(context, options)
    }

    private fun fallbackFace(timestamp: Long): FaceLandmarkerOutput {
        return FaceLandmarkerOutput(
            landmarks = listOf(
                Point3D(0.5f, 0.42f, -0.01f),
                Point3D(0.46f, 0.40f, -0.02f),
                Point3D(0.54f, 0.40f, -0.02f),
                Point3D(0.5f, 0.49f, 0f)
            ),
            boundingBox = floatArrayOf(0.28f, 0.18f, 0.44f, 0.44f),
            blendshapeScores = mapOf("neutral" to 1.0f),
            facialTransformationMatrix = floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
            )
        )
    }

    private fun fallbackPose(frame: FrameEnvelope, config: InferenceConfig): PoseLandmarkerOutput? {
        if (!frame.bodyTrackingEnabled || !config.bodyTrackingEnabled) {
            return null
        } else {
            return PoseLandmarkerOutput(
                landmarks = listOf(
                    Point3D(0.42f, 0.64f, 0f, 0.98f),
                    Point3D(0.58f, 0.64f, 0f, 0.98f),
                    Point3D(0.46f, 0.82f, 0f, 0.92f),
                    Point3D(0.54f, 0.82f, 0f, 0.92f)
                )
            )
        }
    }

    private fun verifyAsset(assetPath: String) {
        runCatching {
            context.assets.open(assetPath).close()
        }.onFailure {
            throw FileNotFoundException("Missing MediaPipe asset: $assetPath")
        }
    }

    private fun MediaPipeRunningMode.toNativeRunningMode(): RunningMode {
        return when (this) {
            MediaPipeRunningMode.IMAGE -> RunningMode.IMAGE
            MediaPipeRunningMode.VIDEO -> RunningMode.VIDEO
            MediaPipeRunningMode.LIVE_STREAM -> RunningMode.LIVE_STREAM
        }
    }
}
