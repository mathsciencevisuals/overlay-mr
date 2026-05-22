package com.overlay.mr.render

import com.overlay.mr.frameprocessor.BodyPart

data class SwapRegion(
    val part: BodyPart,
    val confidence: Float,
    val textureId: Int? = null,
    val normalizedBounds: FloatArray? = null
)
