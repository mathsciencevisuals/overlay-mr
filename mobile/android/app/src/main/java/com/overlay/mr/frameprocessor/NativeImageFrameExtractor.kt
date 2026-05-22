package com.overlay.mr.frameprocessor

import android.media.Image
import java.nio.ByteBuffer

class NativeImageFrameExtractor {
    fun extractPlanes(frame: Any): List<FramePlaneBuffer> {
        val imageSource = imageSource(frame) ?: return emptyList()
        return extractPlanesFromImageSource(imageSource)
    }

    fun extractPixelFormat(frame: Any): FramePixelFormat {
        val imageSource = imageSource(frame)
        if (imageSource is Image && imageSource.format == android.graphics.ImageFormat.YUV_420_888) {
            return FramePixelFormat.YUV_420_8_BIT_FULL
        }

        val raw = readPropertyString(frame, "pixelFormat") ?: return FramePixelFormat.UNKNOWN
        return when (raw.lowercase()) {
            "yuv-420-8-bit-full" -> FramePixelFormat.YUV_420_8_BIT_FULL
            "rgb-bgra-8-bit", "rgb-rgba-8-bit" -> FramePixelFormat.RGB_BGRA_8_BIT
            else -> FramePixelFormat.UNKNOWN
        }
    }

    private fun imageSource(frame: Any): Any? {
        return invokeMethod(frame, "image")
            ?: invokeMethod(frame, "imageProxy")
            ?: invokeMethod(frame, "getImage")
            ?: invokeMethod(frame, "getImageProxy")
    }

    private fun extractPlanesFromImageSource(imageSource: Any): List<FramePlaneBuffer> {
        if (imageSource is Image) {
            return imageSource.planes.map { plane ->
                FramePlaneBuffer(
                    width = imageSource.width,
                    height = imageSource.height,
                    bytesPerRow = plane.rowStride,
                    bytesPerPixel = plane.pixelStride,
                    data = plane.buffer.toByteArray()
                )
            }
        }

        val mediaImage = invokeMethod(imageSource, "image") ?: invokeMethod(imageSource, "getImage")
        if (mediaImage is Image) {
            return mediaImage.planes.map { plane ->
                FramePlaneBuffer(
                    width = mediaImage.width,
                    height = mediaImage.height,
                    bytesPerRow = plane.rowStride,
                    bytesPerPixel = plane.pixelStride,
                    data = plane.buffer.toByteArray()
                )
            }
        }

        val planeObjects = invokeMethod(imageSource, "planes")
            ?: invokeMethod(imageSource, "getPlanes")
            ?: return emptyList()

        return (planeObjects as? Array<*>)?.mapNotNull { plane ->
            plane ?: return@mapNotNull null
            val buffer = invokeMethod(plane, "buffer") ?: invokeMethod(plane, "getBuffer")
            val byteBuffer = buffer as? ByteBuffer ?: return@mapNotNull null
            FramePlaneBuffer(
                width = readIntProperty(imageSource, "width") ?: 0,
                height = readIntProperty(imageSource, "height") ?: 0,
                bytesPerRow = readIntProperty(plane, "rowStride") ?: readIntProperty(plane, "bytesPerRow") ?: 1,
                bytesPerPixel = readIntProperty(plane, "pixelStride") ?: readIntProperty(plane, "bytesPerPixel") ?: 1,
                data = byteBuffer.toByteArray()
            )
        } ?: emptyList()
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        val duplicate = duplicate()
        val bytes = ByteArray(duplicate.remaining())
        duplicate.get(bytes)
        return bytes
    }

    private fun readPropertyString(target: Any, property: String): String? {
        return invokeMethod(target, property)?.toString()
    }

    private fun readIntProperty(target: Any, property: String): Int? {
        return (invokeMethod(target, property) as? Number)?.toInt()
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
