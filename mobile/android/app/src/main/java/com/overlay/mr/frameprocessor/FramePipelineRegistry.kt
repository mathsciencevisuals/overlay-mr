package com.overlay.mr.frameprocessor

import android.content.Context
import com.overlay.mr.render.MRRendererCoordinator

object FramePipelineRegistry {
    @Volatile
    private var pipeline: NativeFramePipeline? = null

    fun initialize(context: Context, rendererCoordinator: MRRendererCoordinator, config: InferenceConfig): NativeFramePipeline {
        val existing = pipeline
        if (existing != null) {
            existing.updateConfig(config)
            return existing
        }

        return NativeFramePipeline(context, rendererCoordinator).also {
            pipeline = it
        }
    }

    fun get(): NativeFramePipeline? = pipeline
}
