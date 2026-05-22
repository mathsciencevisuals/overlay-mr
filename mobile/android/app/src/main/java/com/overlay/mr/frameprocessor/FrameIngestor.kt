package com.overlay.mr.frameprocessor

import java.util.concurrent.atomic.AtomicReference

class FrameIngestor {
    private val latestFrame = AtomicReference<FrameEnvelope?>()

    fun offer(frame: FrameEnvelope) {
        latestFrame.set(frame)
    }

    fun drainLatest(): FrameEnvelope? {
        return latestFrame.getAndSet(null)
    }
}
