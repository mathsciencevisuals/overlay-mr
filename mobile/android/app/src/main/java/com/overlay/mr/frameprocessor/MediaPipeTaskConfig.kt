package com.overlay.mr.frameprocessor

data class MediaPipeTaskConfig(
    val faceAssetPath: String = "mediapipe/face_landmarker.task",
    val poseAssetPath: String = "mediapipe/pose_landmarker.task",
    val runningMode: MediaPipeRunningMode = MediaPipeRunningMode.VIDEO,
    val maxFaces: Int = 1,
    val maxPoses: Int = 1,
    val minFaceDetectionConfidence: Float = 0.5f,
    val minFacePresenceConfidence: Float = 0.5f,
    val minFaceTrackingConfidence: Float = 0.5f,
    val minPoseDetectionConfidence: Float = 0.5f,
    val minPosePresenceConfidence: Float = 0.5f,
    val minPoseTrackingConfidence: Float = 0.5f,
    val outputFaceBlendshapes: Boolean = true,
    val outputFacialTransformationMatrixes: Boolean = true
)

enum class MediaPipeRunningMode {
    IMAGE,
    VIDEO,
    LIVE_STREAM
}
