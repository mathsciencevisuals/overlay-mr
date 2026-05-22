# Product Requirements

## 1. Product Goal

Build an Android mixed-reality app that processes the front camera in realtime, tracks face and body landmarks, renders overlays or swap effects, segments the user from the background, and saves the rendered result as photo or video.

Primary target device:

- Samsung Galaxy S21 FE

Primary product target:

- social-media-grade MR effects at 30 FPS minimum

Non-goal for the first production-quality implementation:

- photorealistic full-neural face or body synthesis on-device

## 2. Repo Alignment

This document is aligned to the current repo layout:

```text
mobile/
backend/
docs/
scripts/
```

It does not assume a future `apps/` or `packages/` monorepo restructure.

## 3. Core Functional Requirements

### Mobile

- Front camera realtime preview using React Native Vision Camera
- Native frame processing on Android through Kotlin
- Face tracking with landmarks and face transform data
- Face overlays such as masks, glasses, stickers, and texture-driven face swap
- Person segmentation for background-aware rendering
- Body overlays attached to pose landmarks
- Optional part-aware body segmentation and staged body swap pipeline
- GPU-based native rendering using OpenGL ES 3.0
- Save rendered photo output
- Save rendered video output
- Target 30 FPS minimum on Samsung Galaxy S21 FE at 720p internal processing

### Backend

- Mobile runtime config API
- Effect and preset metadata APIs
- Signed upload URL API
- Capture registration/listing APIs
- Session and performance metadata APIs
- Backend deployment on GCP Cloud Run
- Asset storage in Cloud Storage
- Lightweight metadata persistence in Firestore

## 4. Core MVP Scope

The first shippable version should include:

1. Android React Native app with front camera preview
2. Kotlin native frame processor pipeline
3. MediaPipe face landmark tracking
4. MediaPipe pose tracking
5. Person segmentation
6. OpenGL ES overlay composition
7. Face masks, stickers, glasses, and simple face texture overlay
8. Body overlays anchored to pose landmarks
9. Photo capture of the composited frame
10. Basic video capture of the composited frame
11. Node.js Express backend on Cloud Run
12. Cloud Storage for captures and effect assets
13. Firestore for users, effects, captures, and sessions
14. Diagnostics through Cloud Logging and mobile crash reporting

## 5. Expanded Scope

The architecture should remain ready for these extensions:

- Background replacement
- Hand tracking
- Multi-face support
- Part-aware body segmentation
- Lightweight body-part swap
- Analytics dashboards
- Admin APIs
- Asset moderation workflows
- Cloud enhancement jobs
- Additional GCP services if scale or product complexity requires them

## 6. Acceptance Criteria

### Realtime

- Front camera preview starts successfully on the S21 FE
- Face overlay remains visually attached during moderate head rotation
- Pose overlays remain stable during moderate upper-body movement
- Median runtime is at least 30 FPS in MVP mode on S21 FE
- Internal inference/render path operates at 720p by default

### Capture

- Saved photo contains the rendered overlay output, not raw camera only
- Saved video contains the rendered overlay output, not raw camera only
- Capture files are returned to the app with usable local paths

### Lifecycle

- Inference pauses when the app goes to background
- Inference resumes safely when the app returns to foreground
- Camera interruption or permission loss fails gracefully
- Under thermal pressure, the app reduces quality before failing preview

### Backend

- Mobile app can fetch runtime config from backend
- Mobile app can request a signed upload URL
- Backend stores or returns effect metadata consistently

## 7. Performance Requirements

- Minimum FPS: 30
- Preferred FPS for simple overlays: 45 to 60
- Internal processing resolution: 1280x720 by default
- Heavy segmentation tasks may run every N frames
- Adaptive downgrade path may reduce resolution toward 960x540 or 854x480 under sustained load
- Body-part segmentation cadence must be lower than base face tracking cadence on S21 FE unless profiling proves otherwise

### Optimization Rules

- Keep inference off the JS thread
- Keep composition in OpenGL ES, not React Native view overlays
- Use GPU delegate or device acceleration where stable
- Smooth landmarks and masks natively
- Preload active effect assets
- Warm models before user capture flow
- Pause nonessential work in background

## 8. Technical Architecture

```text
React Native UI
  -> Vision Camera preview and controls
  -> Native bridge events and commands

Kotlin Native Layer
  -> Frame processor
  -> MediaPipe task manager
  -> TFLite segmentation engine
  -> OpenGL ES compositor
  -> Photo/video capture coordinator
  -> Performance and thermal monitor

Node.js Backend
  -> config APIs
  -> effects APIs
  -> uploads APIs
  -> capture/session APIs

GCP
  -> Cloud Run
  -> Cloud Storage
  -> Firestore
  -> Cloud Build
  -> Artifact Registry
  -> Secret Manager
  -> Cloud Logging
  -> Cloud Monitoring
```

## 9. Rendering Pipeline

```text
Front camera frame
  -> native frame processor
  -> resize / normalize for inference
  -> face / pose / segmentation inference
  -> landmark and mask smoothing
  -> OpenGL ES composition
  -> preview surface
  -> optional photo/video encoder surface
```

### Rendering Rules

- Face swap is mesh or texture based, not full neural synthesis
- Segmentation masks are handled natively, not sent through JS pixel-by-pixel
- Video export must encode the composited output surface
- React Native is control/UI only, not the realtime effect renderer

## 10. MediaPipe and ML Requirements

- Face tracking: MediaPipe `FaceLandmarker`
- Pose tracking: MediaPipe `PoseLandmarker`
- Person segmentation: TFLite or MediaPipe-compatible mobile segmentation model
- Optional body-part segmentation: staged after person segmentation is stable
- Optional hand tracking: later phase

Required asset paths:

- `mobile/android/app/src/main/assets/mediapipe/face_landmarker.task`
- `mobile/android/app/src/main/assets/mediapipe/pose_landmarker.task`
- `mobile/android/app/src/main/assets/models/selfie_segmenter.tflite`

Optional staged assets:

- `mobile/android/app/src/main/assets/models/body_part_segmenter.tflite`
- `mobile/android/app/src/main/assets/models/face_swap_encoder.tflite`

## 11. Backend API Requirements

### Required APIs

- `GET /api/session/config`
- `GET /api/overlays`
- `POST /api/media/upload-url`

### Recommended Next APIs

- `POST /api/captures`
- `GET /api/captures`
- `POST /api/sessions`
- `PATCH /api/sessions/:id`
- `GET /api/config/mobile`
- `GET /api/effects`
- `GET /api/effects/:id`

### Extended APIs

- auth endpoints
- admin effect management
- enhancement job endpoints
- moderation endpoints
- metrics endpoints

## 12. Data Model Requirements

### Firestore Collections

```text
users/
effects/
userAssets/
captures/
sessions/
enhancementJobs/
```

### Minimum MVP Fields

- `users`: profile, device info, settings
- `effects`: name, type, asset URLs, version, active flag
- `captures`: owner, type, storage path, effect ID, created time
- `sessions`: device model, avg FPS, thermal state, active effects, created time

## 13. Effect Schema Requirements

Each effect definition should support:

- effect ID
- effect type
- referenced assets
- anchor landmark mapping
- render order
- blend mode
- occlusion rules
- expected performance cost tier
- version
- active/inactive state

Example effect types:

- `faceSticker`
- `glasses`
- `faceTexture`
- `torsoOverlay`
- `background`
- `bodyPartSwap`

## 14. Security And Privacy Requirements

- Signed upload URLs must be time-limited
- Sensitive backend configuration belongs in Secret Manager or runtime env vars
- User captures should have explicit ownership metadata
- Face/video capture retention policy must be defined before production release
- If auth is enabled, mobile token handling must be documented clearly

## 15. Local Build Requirements

- Node.js 20+
- npm 10+
- JDK 17
- Android SDK 34
- Android NDK 26
- Gradle
- adb / Android platform-tools

Scripts available in this repo:

- `./scripts/check-env.sh`
- `./scripts/setup-android-env.sh`
- `./scripts/build-apk.sh`
- `./scripts/install-apk.sh`

## 16. GCP Deployment Requirements

### Required GCP Services

- Cloud Run
- Cloud Storage
- Firestore
- Cloud Build
- Artifact Registry
- Secret Manager
- Cloud Logging
- Cloud Monitoring

### Deployment Flow

```text
GitHub push
  -> Cloud Build
  -> container build
  -> Artifact Registry push
  -> Cloud Run deploy
  -> backend serves mobile APIs
```

## 17. Recommended GitHub Repo Name

Recommended:

- `overlay-mr`

Good alternatives:

- `overlay-mr-android`
- `mixed-reality-overlay`
- `mr-face-body-effects`

## 18. Current Implementation Status

Ready in repo now:

- React Native scaffold
- Kotlin native module scaffold
- MediaPipe task integration surface
- segmentation pipeline scaffold
- backend scaffold
- GCP deployment scaffold
- local build helper scripts

Still incomplete:

- real Vision Camera frame-to-`MPImage` conversion
- real segmentation model inference
- real GLES face/body compositing
- rendered video export path
- local verified APK build on a machine with Android/JDK tools installed
