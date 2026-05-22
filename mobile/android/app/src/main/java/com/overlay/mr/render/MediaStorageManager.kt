package com.overlay.mr.render

import android.content.Context
import android.os.Environment
import java.io.File

class MediaStorageManager(private val context: Context) {
    fun nextPhotoFile(): File {
        val dir = photoDir()
        dir.mkdirs()
        return File(dir, "mr_capture_${System.currentTimeMillis()}.jpg")
    }

    fun nextVideoFile(): File {
        val dir = videoDir()
        dir.mkdirs()
        return File(dir, "mr_recording_${System.currentTimeMillis()}.mp4")
    }

    private fun photoDir(): File {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?.resolve("overlay-mr")
            ?: context.filesDir.resolve("pictures/overlay-mr")
    }

    private fun videoDir(): File {
        return context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            ?.resolve("overlay-mr")
            ?: context.filesDir.resolve("movies/overlay-mr")
    }
}
