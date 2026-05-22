package com.overlay.mr.frameprocessor

import android.content.Context
import com.overlay.mr.render.MRRendererCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class NativeFramePipeline(
    context: Context,
    private val rendererCoordinator: MRRendererCoordinator
) {
    private val ingestor = FrameIngestor()
    private val inferenceCoordinator = InferenceCoordinator(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val running = AtomicBoolean(false)
    private var loopJob: Job? = null

    suspend fun initialize(config: InferenceConfig) {
        inferenceCoordinator.initialize(config)
    }

    fun start() {
        if (!running.compareAndSet(false, true)) {
            return
        }
        loopJob = scope.launch {
            while (isActive && running.get()) {
                val frame = ingestor.drainLatest()
                if (frame == null) {
                    delay(4)
                    continue
                }

                val result = inferenceCoordinator.processFrame(frame)
                rendererCoordinator.onInferenceResult(result)
            }
        }
    }

    fun stop() {
        running.set(false)
        loopJob?.cancel()
        loopJob = null
    }

    fun submitFrame(frame: FrameEnvelope) {
        ingestor.offer(frame)
    }

    fun updateConfig(config: InferenceConfig) {
        inferenceCoordinator.updateConfig(config)
    }

    fun dispose() {
        stop()
        inferenceCoordinator.close()
        scope.cancel()
    }
}
