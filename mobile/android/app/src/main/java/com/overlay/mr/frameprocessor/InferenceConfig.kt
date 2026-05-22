package com.overlay.mr.frameprocessor

data class InferenceConfig(
    val targetFps: Int = 30,
    val segmentationStride: Int = 2,
    val bodyPartSegmentationStride: Int = 3,
    val poseStride: Int = 2,
    val internalWidth: Int = 1280,
    val internalHeight: Int = 720,
    val segmentationEnabled: Boolean = true,
    val bodyTrackingEnabled: Boolean = true,
    val bodyPartSegmentationEnabled: Boolean = false,
    val bodySwapEnabled: Boolean = false
)
