package com.overlay.mr.frameprocessor

enum class SegmentationModelType {
    PERSON,
    BODY_PARTS
}

data class SegmentationModelSpec(
    val type: SegmentationModelType,
    val assetPath: String,
    val inputWidth: Int,
    val inputHeight: Int,
    val useGpuDelegate: Boolean = true
)

data class RawSegmentationMask(
    val width: Int,
    val height: Int,
    val confidenceMap: FloatArray,
    val textureId: Int? = null
)

data class BodyPartMaskData(
    val part: BodyPart,
    val width: Int,
    val height: Int,
    val confidenceMap: FloatArray,
    val averageConfidence: Float,
    val textureId: Int? = null
)

data class SegmentationInference(
    val personMask: RawSegmentationMask? = null,
    val bodyPartMasks: List<BodyPartMaskData> = emptyList()
)
