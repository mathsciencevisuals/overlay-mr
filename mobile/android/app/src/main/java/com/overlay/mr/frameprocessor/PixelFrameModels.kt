package com.overlay.mr.frameprocessor

data class FramePlaneBuffer(
    val width: Int,
    val height: Int,
    val bytesPerRow: Int,
    val bytesPerPixel: Int,
    val data: ByteArray
)

enum class FramePixelFormat {
    YUV_420_8_BIT_FULL,
    RGB_BGRA_8_BIT,
    UNKNOWN
}
