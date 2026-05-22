package com.overlay.mr.render

import com.overlay.mr.frameprocessor.InferenceResult

class OpenGLCompositor {
    private var renderState = RenderState()

    fun updateState(state: RenderState) {
        renderState = state
    }

    fun renderFrame(result: InferenceResult): Double {
        // GLES pipeline:
        // 1. Bind external camera texture
        // 2. Draw segmentation mask pass
        // 3. Warp face swap texture using landmarks
        // 4. If body-part masks exist, composite torso/limb swap regions with occlusion ordering
        // 5. Draw body overlay quads / mesh anchors
        // 6. Output to preview surface and optional recorder surface
        val hasBodyParts = renderState.segmentation?.partMasks?.isNotEmpty() == true
        val swapRegionCount = renderState.swapRegions.size
        return when {
            hasBodyParts && renderState.bodySwapEnabled -> 6.4 + (swapRegionCount * 0.35)
            renderState.segmentation != null || renderState.pose != null -> 4.8
            else -> 3.2
        }
    }
}
