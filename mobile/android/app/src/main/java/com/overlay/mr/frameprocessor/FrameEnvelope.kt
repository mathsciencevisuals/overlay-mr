package com.overlay.mr.frameprocessor

data class FrameEnvelope(
    val timestamp: Long,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val pixelFormat: FramePixelFormat = FramePixelFormat.UNKNOWN,
    val isMirrored: Boolean = false,
    val planes: List<FramePlaneBuffer> = emptyList(),
    val segmentationEnabled: Boolean,
    val bodyTrackingEnabled: Boolean,
    val bodyPartSegmentationEnabled: Boolean,
    val bodySwapEnabled: Boolean
)
