# Overlay MR

Android mixed-reality scaffold for realtime face/body effects on Samsung Galaxy S21 FE class devices.

Stack:
- React Native + TypeScript
- `react-native-vision-camera`
- Kotlin native modules
- MediaPipe + TensorFlow Lite
- OpenGL ES 3.0 renderer
- Node.js + Express backend
- GCP Cloud Run for low-cost hosting

## Project Structure

```text
mobile/
  App.tsx
  index.js
  src/
    components/
    hooks/
    screens/
    services/
    types/
  android/
    app/src/main/java/com/overlay/mr/
      bridge/
      frameprocessor/
      render/
backend/
  src/
    controllers/
    routes/
    services/
docs/
  setup.md
  architecture.md
  optimization.md
  backend-api.md
  mediapipe.md
  product-requirements.md
```

## MVP Targets

- Front camera realtime processing
- Face tracking
- Face swap texture mapping
- Body-part overlays
- Segmentation-aware composition
- Save photo/video
- 30 FPS minimum at 720p internal processing

## Fast Start

1. Install Node 20, JDK 17, Android Studio, Android SDK 34, and NDK 26.
2. Follow [docs/setup.md](/home/pc/projects/overlay/docs/setup.md).
3. Add MediaPipe graphs and TFLite models under:
   - `mobile/android/app/src/main/assets/mediapipe/`
   - `mobile/android/app/src/main/assets/models/`
4. Start backend:
   - `cd backend && npm install && npm run dev`
5. Start app:
   - `cd mobile && npm install && npm run android`
6. For APK/release/GitHub/GCP workflow, use [docs/release.md](/home/pc/projects/overlay/docs/release.md).
7. For consolidated product scope and architecture decisions, use [docs/product-requirements.md](/home/pc/projects/overlay/docs/product-requirements.md).

## Notes

- Internal inference target is 1280x720.
- Rendering path is camera texture -> inference metadata -> OpenGL compositor.
- Backend is optional for on-device effects, but used for asset sync, upload, templates, and post-processing.
- The Kotlin side is a scaffold with clear integration points; you still need to wire real `ImageProxy`/Vision Camera frames into MediaPipe and the GLES renderer.
- GitHub/GCP bootstrap files are included; see [docs/release.md](/home/pc/projects/overlay/docs/release.md) for the exact init/push/deploy sequence.
- MediaPipe task wiring details are in [docs/mediapipe.md](/home/pc/projects/overlay/docs/mediapipe.md).
