package com.overlay.mr.render

import android.os.SystemClock
import kotlin.math.max

class RenderStatsTracker {
    private var lastFrameAt = 0L
    private var droppedFrames = 0

    fun recordFrame(inferenceMs: Double, renderMs: Double): RenderSnapshot {
        val now = SystemClock.elapsedRealtime()
        val fps = if (lastFrameAt == 0L) {
            30.0
        } else {
            1000.0 / max(1L, now - lastFrameAt).toDouble()
        }
        lastFrameAt = now
        return RenderSnapshot(
            fps = fps.coerceAtMost(60.0),
            inferenceMs = inferenceMs,
            renderMs = renderMs,
            droppedFrames = droppedFrames
        )
    }

    fun incrementDroppedFrame() {
        droppedFrames += 1
    }
}

data class RenderSnapshot(
    val fps: Double,
    val inferenceMs: Double,
    val renderMs: Double,
    val droppedFrames: Int
)
