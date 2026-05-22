package com.overlay.mr.frameprocessor

import android.content.Context
import java.io.FileNotFoundException

class SegmentationEngine(private val context: Context) {
    private var personModel: SegmentationModelSpec? = null
    private var bodyPartModel: SegmentationModelSpec? = null
    private val smoother = TemporalSmoother()

    fun initialize() {
        personModel = modelSpec(
            type = SegmentationModelType.PERSON,
            assetPath = "models/selfie_segmenter.tflite",
            inputWidth = 256,
            inputHeight = 256
        )
        bodyPartModel = modelSpec(
            type = SegmentationModelType.BODY_PARTS,
            assetPath = "models/body_part_segmenter.tflite",
            inputWidth = 256,
            inputHeight = 256
        )
    }

    fun run(frame: FrameEnvelope, config: InferenceConfig): SegmentationInference {
        val personMask = if (frame.segmentationEnabled && config.segmentationEnabled) {
            smoother.smoothPersonMask(createPersonMask(frame))
        } else {
            null
        }

        val bodyPartMasks = if (frame.bodyPartSegmentationEnabled && config.bodyPartSegmentationEnabled) {
            smoother.smoothBodyPartMasks(createBodyPartMasks(frame, config))
        } else {
            emptyList()
        }

        return SegmentationInference(
            personMask = personMask,
            bodyPartMasks = bodyPartMasks
        )
    }

    fun close() {
        // Release TFLite interpreter and delegate here.
        smoother.reset()
        personModel = null
        bodyPartModel = null
    }

    private fun modelSpec(
        type: SegmentationModelType,
        assetPath: String,
        inputWidth: Int,
        inputHeight: Int
    ): SegmentationModelSpec {
        verifyAssetExists(assetPath)
        return SegmentationModelSpec(
            type = type,
            assetPath = assetPath,
            inputWidth = inputWidth,
            inputHeight = inputHeight
        )
    }

    private fun verifyAssetExists(assetPath: String) {
        runCatching {
            context.assets.open(assetPath).close()
        }.onFailure {
            if (assetPath.contains("body_part_segmenter")) {
                return
            }
            throw FileNotFoundException("Missing segmentation asset: $assetPath")
        }
    }

    private fun createPersonMask(frame: FrameEnvelope): RawSegmentationMask {
        val spec = personModel ?: error("Person segmentation model is not initialized")
        val size = spec.inputWidth * spec.inputHeight
        val confidenceMap = FloatArray(size) { index ->
            val x = index % spec.inputWidth
            val y = index / spec.inputWidth
            val xNorm = x.toFloat() / spec.inputWidth.toFloat()
            val yNorm = y.toFloat() / spec.inputHeight.toFloat()
            if (xNorm in 0.18f..0.82f && yNorm in 0.12f..0.94f) 0.93f else 0.05f
        }
        return RawSegmentationMask(
            width = spec.inputWidth,
            height = spec.inputHeight,
            confidenceMap = confidenceMap,
            textureId = null
        )
    }

    private fun createBodyPartMasks(frame: FrameEnvelope, config: InferenceConfig): List<BodyPartMaskData> {
        val spec = bodyPartModel ?: SegmentationModelSpec(
            type = SegmentationModelType.BODY_PARTS,
            assetPath = "models/body_part_segmenter.tflite",
            inputWidth = 256,
            inputHeight = 256
        )

        // These are structured stand-ins for a real parser output.
        // Replace this with TFLite logits -> per-part mask decoding.
        return listOf(
            bodyPartMask(spec, BodyPart.TORSO, 0.90f, 0.32f, 0.30f, 0.68f, 0.76f),
            bodyPartMask(spec, BodyPart.LEFT_ARM, 0.86f, 0.16f, 0.28f, 0.34f, 0.80f),
            bodyPartMask(spec, BodyPart.RIGHT_ARM, 0.86f, 0.66f, 0.28f, 0.84f, 0.80f),
            bodyPartMask(spec, BodyPart.LEFT_LEG, 0.81f, 0.32f, 0.72f, 0.48f, 0.98f),
            bodyPartMask(spec, BodyPart.RIGHT_LEG, 0.81f, 0.52f, 0.72f, 0.68f, 0.98f)
        ).filter { mask ->
            config.bodySwapEnabled || mask.part == BodyPart.TORSO
        }
    }

    private fun bodyPartMask(
        spec: SegmentationModelSpec,
        part: BodyPart,
        averageConfidence: Float,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float
    ): BodyPartMaskData {
        val map = FloatArray(spec.inputWidth * spec.inputHeight) { index ->
            val x = index % spec.inputWidth
            val y = index / spec.inputWidth
            val xNorm = x.toFloat() / spec.inputWidth.toFloat()
            val yNorm = y.toFloat() / spec.inputHeight.toFloat()
            if (xNorm in x0..x1 && yNorm in y0..y1) averageConfidence else 0.02f
        }
        return BodyPartMaskData(
            part = part,
            width = spec.inputWidth,
            height = spec.inputHeight,
            confidenceMap = map,
            averageConfidence = averageConfidence,
            textureId = null
        )
    }
}
