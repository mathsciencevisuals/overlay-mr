package com.overlay.mr.frameprocessor

import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage

class MediaPipeFrameConverter {
    private val yuvConverter = Yuv420ToBitmapConverter()

    fun toMpImage(frame: FrameEnvelope): MPImage {
        val bitmap = toBitmap(frame)
        return BitmapImageBuilder(bitmap).build()
    }

    private fun toBitmap(frame: FrameEnvelope): Bitmap {
        return if (frame.pixelFormat == FramePixelFormat.YUV_420_8_BIT_FULL && frame.planes.isNotEmpty()) {
            yuvConverter.convert(frame)
        } else {
            Bitmap.createBitmap(
                frame.width.coerceAtLeast(1),
                frame.height.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
        }
    }
}
