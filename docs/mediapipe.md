# MediaPipe Integration

This project is wired to the current Android MediaPipe Tasks contract using:

- `FaceLandmarker.detectForVideo(MPImage, ImageProcessingOptions, timestampMs)`
- `PoseLandmarker.detectForVideo(MPImage, ImageProcessingOptions, timestampMs)`
- `RunningMode.VIDEO`
- `BaseOptions.setModelAssetPath(...)`

Primary references:

- https://ai.google.dev/edge/api/mediapipe/java/com/google/mediapipe/tasks/vision/facelandmarker/FaceLandmarker
- https://ai.google.dev/edge/api/mediapipe/java/com/google/mediapipe/tasks/vision/poselandmarker/PoseLandmarker
- https://ai.google.dev/edge/api/mediapipe/java/com/google/mediapipe/tasks/core/BaseOptions.Builder
- https://ai.google.dev/edge/api/mediapipe/java/com/google/mediapipe/tasks/vision/core/ImageProcessingOptions.Builder

## Asset Contract

Required app assets:

- `mobile/android/app/src/main/assets/mediapipe/face_landmarker.task`
- `mobile/android/app/src/main/assets/mediapipe/pose_landmarker.task`

Optional segmentation assets:

- `mobile/android/app/src/main/assets/models/selfie_segmenter.tflite`
- `mobile/android/app/src/main/assets/models/body_part_segmenter.tflite`

## Current State

What is implemented:

- official MediaPipe Tasks API surface for face and pose loading
- `VIDEO` mode task construction
- rotation-aware `ImageProcessingOptions`
- result mapping into the app's `FaceTrackingPacket` and `PoseTrackingPacket`
- blendshape and facial transform matrix propagation to JS events

What is still staged:

- true camera frame to `MPImage` conversion from Vision Camera buffers
- zero-copy GPU path instead of placeholder bitmap conversion
- verification against real `.task` files on a local Android build

## Vision Camera Interop Direction

The current Android interop direction follows Vision Camera's documented native model:

- prefer native `ImageProxy`/`Image` access in Kotlin native frame processing
- keep YUV as the preferred input format when possible
- treat `NativeBuffer` / `AHardwareBuffer*` as the longer-term zero-copy path for GPU-heavy processing

References:

- https://visioncamera.margelo.com/docs/guides/frame-processor-plugins-community
- https://visioncamera.margelo.com/docs/a-frame
- https://visioncamera.margelo.com/docs/a-frames-nativebuffer
- https://visioncamera.margelo.com/docs/guides/pixel-formats

## Important Limitation

`MediaPipeFrameConverter` currently creates an `ARGB_8888` bitmap placeholder to keep the integration seam stable. That means the app is now wired to the correct MediaPipe task API, but not yet consuming real camera pixels. The next native step is replacing that converter with an actual frame-plane or texture-backed `MPImage` path.
