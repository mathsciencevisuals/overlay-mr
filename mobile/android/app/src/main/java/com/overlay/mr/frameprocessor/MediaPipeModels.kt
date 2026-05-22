package com.overlay.mr.frameprocessor

data class FaceLandmarkerOutput(
    val landmarks: List<Point3D>,
    val boundingBox: FloatArray? = null,
    val blendshapeScores: Map<String, Float> = emptyMap(),
    val facialTransformationMatrix: FloatArray? = null
)

data class PoseLandmarkerOutput(
    val landmarks: List<Point3D>
)

data class MediaPipeInference(
    val face: FaceLandmarkerOutput? = null,
    val pose: PoseLandmarkerOutput? = null
)
