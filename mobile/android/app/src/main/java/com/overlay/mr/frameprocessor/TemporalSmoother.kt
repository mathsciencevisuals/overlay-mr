package com.overlay.mr.frameprocessor

class TemporalSmoother(private val alpha: Float = 0.65f) {
    private val previousPartMasks = mutableMapOf<BodyPart, FloatArray>()
    private var previousPersonMask: FloatArray? = null

    fun smoothPersonMask(mask: RawSegmentationMask): RawSegmentationMask {
        val previous = previousPersonMask
        val smoothed = if (previous == null || previous.size != mask.confidenceMap.size) {
            mask.confidenceMap.copyOf()
        } else {
            blend(previous, mask.confidenceMap)
        }
        previousPersonMask = smoothed
        return mask.copy(confidenceMap = smoothed)
    }

    fun smoothBodyPartMasks(masks: List<BodyPartMaskData>): List<BodyPartMaskData> {
        return masks.map { mask ->
            val previous = previousPartMasks[mask.part]
            val smoothed = if (previous == null || previous.size != mask.confidenceMap.size) {
                mask.confidenceMap.copyOf()
            } else {
                blend(previous, mask.confidenceMap)
            }
            previousPartMasks[mask.part] = smoothed
            mask.copy(confidenceMap = smoothed)
        }
    }

    fun reset() {
        previousPersonMask = null
        previousPartMasks.clear()
    }

    private fun blend(previous: FloatArray, current: FloatArray): FloatArray {
        val result = FloatArray(current.size)
        for (index in current.indices) {
            result[index] = (alpha * current[index]) + ((1f - alpha) * previous[index])
        }
        return result
    }
}
