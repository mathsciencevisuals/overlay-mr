# Architecture

## Pipeline

1. Vision Camera captures front-camera frames.
2. Frame processor plugin forwards a GPU-friendly image buffer to Kotlin.
3. Kotlin inference coordinator runs:
   - face landmarks
   - pose landmarks
   - segmentation
   - optional body-part segmentation
   - optional swap embedding/blend inference
4. Results are written into a ring buffer shared with the renderer.
5. OpenGL ES compositor draws:
   - camera texture
   - segmentation mask
   - body-part masks for torso/arms/legs when enabled
   - swap regions generated from part-mask confidence and normalized bounds
   - face swap mesh texture
   - body overlay sprites or meshes
6. Capture manager saves video/photo with synchronized overlay output.

## Threading

- Camera thread: acquisition only
- Inference thread pool: MediaPipe/TFLite
- GL thread: composition
- JS thread: UI controls and session state

## 30 FPS Strategy

- Camera preview at 30 or 60 FPS
- Inference at adaptive 15 to 30 FPS
- Reproject last-known landmarks between inference frames
- Segmentation every N frames under load
- Body-part segmentation every 3rd or 4th frame under load
- 720p input, render upscale for display/export
