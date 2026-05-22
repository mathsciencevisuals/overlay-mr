package com.overlay.mr.frameprocessor

import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class MediaPipeResultMapper {
    fun mapFace(result: FaceLandmarkerResult): FaceLandmarkerOutput? {
        val faceLandmarks = result.faceLandmarks()
        if (faceLandmarks.isEmpty()) {
            return null
        }

        val firstFace = faceLandmarks.first()
        val mapped = firstFace.map { landmark ->
            Point3D(
                x = landmark.x(),
                y = landmark.y(),
                z = landmark.z()
            )
        }

        val minX = mapped.minOfOrNull { it.x } ?: 0f
        val minY = mapped.minOfOrNull { it.y } ?: 0f
        val maxX = mapped.maxOfOrNull { it.x } ?: 0f
        val maxY = mapped.maxOfOrNull { it.y } ?: 0f

        val blendshapeScores = result.faceBlendshapes()
            .map { classifications -> classifications.firstOrNull()?.associateByCategoryName() ?: emptyMap() }
            .orElse(emptyMap())

        val matrix = result.facialTransformationMatrixes()
            .map { matrices -> matrices.firstOrNull() }
            .orElse(null)

        return FaceLandmarkerOutput(
            landmarks = mapped,
            boundingBox = floatArrayOf(minX, minY, maxX - minX, maxY - minY),
            blendshapeScores = blendshapeScores,
            facialTransformationMatrix = matrix
        )
    }

    fun mapPose(result: PoseLandmarkerResult): PoseLandmarkerOutput? {
        val poses = result.landmarks()
        if (poses.isEmpty()) {
            return null
        }

        return PoseLandmarkerOutput(
            landmarks = poses.first().map { landmark ->
                Point3D(
                    x = landmark.x(),
                    y = landmark.y(),
                    z = landmark.z(),
                    visibility = landmark.visibility().orElse(null)
                )
            }
        )
    }

    private fun List<Category>.associateByCategoryName(): Map<String, Float> {
        return associate { category ->
            val key = category.categoryName().ifBlank { category.displayName() }
            key to category.score()
        }
    }
}
