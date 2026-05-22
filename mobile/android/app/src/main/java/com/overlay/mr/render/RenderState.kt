package com.overlay.mr.render

import com.overlay.mr.frameprocessor.FaceTrackingPacket
import com.overlay.mr.frameprocessor.PoseTrackingPacket
import com.overlay.mr.frameprocessor.SegmentationPacket

data class RenderState(
    val face: FaceTrackingPacket? = null,
    val pose: PoseTrackingPacket? = null,
    val segmentation: SegmentationPacket? = null,
    val swapAssetId: String? = null,
    val overlayPreset: String = "minimal",
    val bodySwapEnabled: Boolean = false,
    val swapRegions: List<SwapRegion> = emptyList()
)
