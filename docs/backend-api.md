# Backend APIs

## `GET /health`

Service liveness check.

## `GET /api/session/config?deviceModel=Samsung%20Galaxy%20S21%20FE`

Returns feature flags and recommended realtime configuration.

Example response:

```json
{
  "targetFps": 30,
  "internalResolution": {
    "width": 1280,
    "height": 720
  },
  "segmentationStride": 2,
  "faceTrackingModel": "face_landmarker.task",
  "poseTrackingModel": "pose_landmarker.task",
  "features": {
    "faceSwap": true,
    "bodyOverlay": true,
    "segmentation": true,
    "videoCapture": true
  }
}
```

## `GET /api/overlays`

Returns overlay preset metadata.

## `GET /api/effects`

Returns effect metadata for active effects.

## `GET /api/effects/:id`

Returns one effect definition.

## `POST /api/media/upload-url`

Request body:

```json
{
  "filename": "capture.mp4",
  "contentType": "video/mp4"
}
```

Returns a signed upload URL for Cloud Storage or a local mock URL if GCP is not configured yet.

## `POST /api/captures`

Registers capture metadata after a photo/video export or upload.

## `GET /api/captures`

Lists known capture metadata, optionally filtered by `userId`.

## `POST /api/sessions`

Creates a runtime session for device/performance tracking.

## `PATCH /api/sessions/:id`

Updates session telemetry such as FPS, thermal state, and dropped frames.

## `GET /api/config/mobile`

Returns runtime configuration, feature flags, and model asset names for the mobile app.
