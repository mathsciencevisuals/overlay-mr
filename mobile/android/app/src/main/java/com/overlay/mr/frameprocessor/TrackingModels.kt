package com.overlay.mr.frameprocessor

data class Point3D(val x: Float, val y: Float, val z: Float, val visibility: Float? = null)

data class FaceTrackingPacket(
    val timestamp: Long,
    val landmarks: List<Point3D>,
    val boundingBox: FloatArray? = null,
    val blendshapeScores: Map<String, Float> = emptyMap(),
    val facialTransformationMatrix: FloatArray? = null
)

data class PoseTrackingPacket(
    val timestamp: Long,
    val landmarks: List<Point3D>
)

data class SegmentationPacket(
    val timestamp: Long,
    val width: Int,
    val height: Int,
    val textureId: Int? = null,
    val kind: SegmentationKind = SegmentationKind.PERSON,
    val partMasks: List<BodyPartMask> = emptyList()
)

data class BodyPartMask(
    val part: BodyPart,
    val textureId: Int? = null,
    val confidence: Float
)

enum class SegmentationKind {
    PERSON,
    BODY_PARTS
}

enum class BodyPart {
    HAIR,
    FACE_SKIN,
    TORSO,
    LEFT_ARM,
    RIGHT_ARM,
    LEFT_LEG,
    RIGHT_LEG
}
