package com.overlay.mr.frameprocessor

import com.mrousavy.camera.frameprocessors.Frame
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin
import com.mrousavy.camera.frameprocessors.VisionCameraProxy

class MRFrameProcessorPlugin : FrameProcessorPlugin() {
    private val nativeImageExtractor = NativeImageFrameExtractor()

    override fun callback(frame: Frame, params: Map<String, Any>?): Any? {
        FramePipelineRegistry.get()?.submitFrame(
            FrameEnvelope(
                timestamp = frame.timestamp.toLong(),
                width = frame.width,
                height = frame.height,
                rotationDegrees = frame.orientation.ordinal * 90,
                pixelFormat = nativeImageExtractor.extractPixelFormat(frame),
                isMirrored = readBooleanProperty(frame, "isMirrored") ?: false,
                planes = nativeImageExtractor.extractPlanes(frame),
                segmentationEnabled = params?.get("enableSegmentation") as? Boolean ?: true,
                bodyTrackingEnabled = params?.get("enableBodyTracking") as? Boolean ?: true,
                bodyPartSegmentationEnabled = params?.get("enableBodyPartSegmentation") as? Boolean ?: false,
                bodySwapEnabled = params?.get("enableBodySwap") as? Boolean ?: false
            )
        )
        return null
    }

    companion object {
        fun register() {
            VisionCameraProxy.addFrameProcessorPlugin("processMRFrame") {
                MRFrameProcessorPlugin()
            }
        }
    }

    private fun readBooleanProperty(target: Any, property: String): Boolean? {
        return invokeMethod(target, property) as? Boolean
    }

    private fun invokeMethod(target: Any, property: String): Any? {
        val capitalized = property.replaceFirstChar { it.uppercase() }
        val candidates = listOf(property, "get$capitalized", "is$capitalized")
        val method = target.javaClass.methods.firstOrNull { method ->
            method.parameterCount == 0 && candidates.any { candidate -> method.name == candidate }
        } ?: return null
        return runCatching { method.invoke(target) }.getOrNull()
    }
}
