package com.overlay.mr.frameprocessor

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min

class Yuv420ToBitmapConverter {
    fun convert(frame: FrameEnvelope): Bitmap {
        val width = frame.width.coerceAtLeast(1)
        val height = frame.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val yPlane = frame.planes.firstOrNull()
        if (frame.pixelFormat == FramePixelFormat.YUV_420_8_BIT_FULL && yPlane != null) {
            val pixels = IntArray(width * height)
            for (row in 0 until height) {
                for (col in 0 until width) {
                    val yIndex = min(
                        row * max(1, yPlane.bytesPerRow) + col * max(1, yPlane.bytesPerPixel),
                        yPlane.data.lastIndex
                    )
                    val y = yPlane.data[yIndex].toInt() and 0xFF
                    pixels[row * width + col] = (0xFF shl 24) or (y shl 16) or (y shl 8) or y
                }
            }
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        }

        return bitmap
    }
}
