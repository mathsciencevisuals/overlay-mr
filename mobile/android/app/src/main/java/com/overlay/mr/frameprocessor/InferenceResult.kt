package com.overlay.mr.frameprocessor

data class InferenceResult(
    val face: FaceTrackingPacket,
    val pose: PoseTrackingPacket?,
    val segmentation: SegmentationPacket?,
    val inferenceMs: Double,
    val renderEligible: Boolean = true
)
