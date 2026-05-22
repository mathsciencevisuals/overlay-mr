package com.overlay.mr.render

import android.content.Context
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.overlay.mr.frameprocessor.FramePipelineRegistry
import com.overlay.mr.frameprocessor.BodyPart
import com.overlay.mr.frameprocessor.InferenceConfig
import com.overlay.mr.frameprocessor.InferenceResult
import com.overlay.mr.frameprocessor.Point3D
import java.util.concurrent.atomic.AtomicBoolean

class MRRendererCoordinator(
    private val context: Context,
    private val emitter: (String, WritableMap) -> Unit
) {
    private val started = AtomicBoolean(false)
    private var config: Map<String, Any?> = emptyMap()
    private val renderer = OpenGLCompositor()
    private val statsTracker = RenderStatsTracker()
    private val mediaStorageManager = MediaStorageManager(context)

    fun initialize(config: Map<String, Any?>) {
        this.config = config
        val pipeline = FramePipelineRegistry.initialize(context, this, inferenceConfig())
        kotlinx.coroutines.runBlocking {
            pipeline.initialize(inferenceConfig())
        }
    }

    fun updateConfig(config: Map<String, Any?>) {
        this.config = this.config + config
        FramePipelineRegistry.get()?.updateConfig(inferenceConfig())
    }

    fun start() {
        if (!started.compareAndSet(false, true)) {
            return
        }
        FramePipelineRegistry.get()?.start()
    }

    fun stop() {
        started.set(false)
        FramePipelineRegistry.get()?.stop()
    }

    fun onInferenceResult(result: InferenceResult) {
        val state = RenderState(
            face = result.face,
            pose = result.pose,
            segmentation = result.segmentation,
            swapAssetId = config["swapAssetId"] as? String,
            overlayPreset = config["overlayPreset"] as? String ?: "minimal",
            bodySwapEnabled = config["bodySwapEnabled"] as? Boolean ?: false,
            swapRegions = swapRegions(result)
        )
        renderer.updateState(state)
        val renderMs = renderer.renderFrame(result)
        val snapshot = statsTracker.recordFrame(result.inferenceMs, renderMs)
        emitter("onFaceTracking", faceMap(result.face))
        result.pose?.let { emitter("onPoseTracking", poseMap(it)) }
        result.segmentation?.let { emitter("onSegmentation", segmentationMap(it)) }
        emitter("onRenderStats", statsMap(snapshot))
    }

    fun capturePhoto(): String {
        val output = mediaStorageManager.nextPhotoFile()
        output.writeBytes(ByteArray(0))
        return output.absolutePath
    }

    fun startRecording() {
        // Bind encoder surface here for composed output.
    }

    fun stopRecording(): String {
        val output = mediaStorageManager.nextVideoFile()
        output.writeBytes(ByteArray(0))
        return output.absolutePath
    }

    private fun faceMap(packet: com.overlay.mr.frameprocessor.FaceTrackingPacket): WritableMap {
        return Arguments.createMap().apply {
            putDouble("timestamp", packet.timestamp.toDouble())
            putArray("landmarks", landmarksArray(packet.landmarks))
            putMap("blendshapeScores", Arguments.createMap().apply {
                packet.blendshapeScores.forEach { (name, score) ->
                    putDouble(name, score.toDouble())
                }
            })
            packet.boundingBox?.let {
                putMap("boundingBox", Arguments.createMap().apply {
                    putDouble("x", it[0].toDouble())
                    putDouble("y", it[1].toDouble())
                    putDouble("width", it[2].toDouble())
                    putDouble("height", it[3].toDouble())
                })
            }
            packet.facialTransformationMatrix?.let { matrix ->
                putArray("facialTransformationMatrix", Arguments.createArray().apply {
                    matrix.forEach { pushDouble(it.toDouble()) }
                })
            }
        }
    }

    private fun poseMap(packet: com.overlay.mr.frameprocessor.PoseTrackingPacket): WritableMap {
        return Arguments.createMap().apply {
            putDouble("timestamp", packet.timestamp.toDouble())
            putArray("landmarks", landmarksArray(packet.landmarks))
        }
    }

    private fun segmentationMap(packet: com.overlay.mr.frameprocessor.SegmentationPacket): WritableMap {
        return Arguments.createMap().apply {
            putDouble("timestamp", packet.timestamp.toDouble())
            putInt("width", packet.width)
            putInt("height", packet.height)
            putString("kind", if (packet.kind.name == "BODY_PARTS") "body-parts" else "person")
            putArray("partMasks", Arguments.createArray().apply {
                packet.partMasks.forEach { mask ->
                    pushMap(Arguments.createMap().apply {
                        putString("part", bodyPartName(mask.part))
                        putDouble("confidence", mask.confidence.toDouble())
                        mask.textureId?.let { putInt("textureId", it) }
                    })
                }
            })
        }
    }

    private fun statsMap(snapshot: RenderSnapshot): WritableMap {
        return Arguments.createMap().apply {
            putDouble("fps", snapshot.fps)
            putDouble("inferenceMs", snapshot.inferenceMs)
            putDouble("renderMs", snapshot.renderMs)
            putInt("droppedFrames", snapshot.droppedFrames)
        }
    }

    private fun landmarksArray(points: List<Point3D>): WritableArray {
        return Arguments.createArray().apply {
            points.forEach { point ->
                pushMap(Arguments.createMap().apply {
                    putDouble("x", point.x.toDouble())
                    putDouble("y", point.y.toDouble())
                    putDouble("z", point.z.toDouble())
                    point.visibility?.let { putDouble("visibility", it.toDouble()) }
                })
            }
        }
    }

    private fun inferenceConfig(): InferenceConfig {
        return InferenceConfig(
            targetFps = 30,
            segmentationStride = if ((config["segmentationEnabled"] as? Boolean) == false) 99 else 2,
            bodyPartSegmentationStride = if ((config["bodyPartSegmentationEnabled"] as? Boolean) == false) 99 else 3,
            poseStride = if ((config["bodyTrackingEnabled"] as? Boolean) == false) 99 else 2,
            internalWidth = 1280,
            internalHeight = 720,
            segmentationEnabled = config["segmentationEnabled"] as? Boolean ?: true,
            bodyTrackingEnabled = config["bodyTrackingEnabled"] as? Boolean ?: true,
            bodyPartSegmentationEnabled = config["bodyPartSegmentationEnabled"] as? Boolean ?: false,
            bodySwapEnabled = config["bodySwapEnabled"] as? Boolean ?: false
        )
    }

    private fun swapRegions(result: InferenceResult): List<SwapRegion> {
        val segmentation = result.segmentation ?: return emptyList()
        if (segmentation.partMasks.isEmpty()) {
            return emptyList()
        }

        return segmentation.partMasks.map { mask ->
            SwapRegion(
                part = mask.part,
                confidence = mask.confidence,
                textureId = mask.textureId,
                normalizedBounds = defaultBounds(mask.part)
            )
        }
    }

    private fun defaultBounds(part: BodyPart): FloatArray {
        return when (part) {
            BodyPart.HAIR -> floatArrayOf(0.28f, 0.06f, 0.72f, 0.24f)
            BodyPart.FACE_SKIN -> floatArrayOf(0.32f, 0.14f, 0.68f, 0.40f)
            BodyPart.TORSO -> floatArrayOf(0.30f, 0.28f, 0.70f, 0.78f)
            BodyPart.LEFT_ARM -> floatArrayOf(0.12f, 0.26f, 0.34f, 0.82f)
            BodyPart.RIGHT_ARM -> floatArrayOf(0.66f, 0.26f, 0.88f, 0.82f)
            BodyPart.LEFT_LEG -> floatArrayOf(0.30f, 0.72f, 0.48f, 1.0f)
            BodyPart.RIGHT_LEG -> floatArrayOf(0.52f, 0.72f, 0.70f, 1.0f)
        }
    }

    private fun bodyPartName(part: BodyPart): String {
        return when (part) {
            BodyPart.HAIR -> "hair"
            BodyPart.FACE_SKIN -> "faceSkin"
            BodyPart.TORSO -> "torso"
            BodyPart.LEFT_ARM -> "leftArm"
            BodyPart.RIGHT_ARM -> "rightArm"
            BodyPart.LEFT_LEG -> "leftLeg"
            BodyPart.RIGHT_LEG -> "rightLeg"
        }
    }
}
