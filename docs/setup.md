# Setup

## 1. Environment

- Node.js 20.x
- npm 10+
- JDK 17
- Android Studio Iguana or newer
- Android SDK 34
- Android NDK 26
- React Native CLI environment

## 2. Android Device

Samsung Galaxy S21 FE recommended settings:

- Enable developer options
- Enable USB debugging
- Set display to 60 Hz while profiling thermals
- Test with front camera at 720p and 1080p

## 3. Mobile Dependencies

From `mobile/`:

```bash
npm install
```

Native Android dependencies are declared in Gradle:

- Vision Camera
- MediaPipe Tasks / framework integration hooks
- TensorFlow Lite GPU delegate
- Kotlin coroutines

## 4. Models

Place optimized assets here:

- `mobile/android/app/src/main/assets/models/face_swap_encoder.tflite`
- `mobile/android/app/src/main/assets/models/selfie_segmenter.tflite`
- `mobile/android/app/src/main/assets/mediapipe/face_landmarker.task`
- `mobile/android/app/src/main/assets/mediapipe/pose_landmarker.task`

Recommended first-pass models:

- MediaPipe Face Landmarker
- MediaPipe Pose Landmarker Lite
- MobileNet-class selfie segmentation
- Lightweight blend / color matching TFLite model only if needed

## 5. Backend

From `backend/`:

```bash
npm install
npm run dev
```

Required env:

```bash
PORT=8080
GCP_PROJECT_ID=your-project-id
ASSET_BUCKET=overlay-mr-assets
SIGNED_URL_TTL_SECONDS=900
```

## 6. Deploy to GCP

Low-cost default:

- Cloud Run: backend API
- Cloud Storage: media/assets
- Firestore or no database initially

Deploy:

```bash
gcloud builds submit --tag gcr.io/$GCP_PROJECT_ID/overlay-backend backend
gcloud run deploy overlay-backend \
  --image gcr.io/$GCP_PROJECT_ID/overlay-backend \
  --region us-central1 \
  --platform managed \
  --allow-unauthenticated \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 3
```

## 7. Performance Rules

- Keep inference on a worker thread.
- Avoid CPU copies of camera frames where possible.
- Use GPU delegate for segmentation and blend models.
- Downscale frames before heavy inference.
- Render overlays with GLES, not React Native views.
