# Optimization Strategy

## Device Budget

Samsung Galaxy S21 FE target budget for 30 FPS:

- Camera acquisition: 2 to 4 ms
- Face landmarker: 5 to 10 ms
- Pose tracking: 4 to 8 ms
- Segmentation: 4 to 10 ms with GPU delegate
- GLES composition: 2 to 5 ms

Total budget:

- Aim for 24 to 28 ms steady-state
- Leave thermal and GC margin

## Rendering Rules

- Use `SurfaceTexture` / OES external texture for camera preview.
- Keep mask and overlay textures on GPU memory.
- Do not round-trip segmentation masks through JS.
- Reuse FBOs and texture objects.
- Smooth body-part masks natively before rendering to reduce flicker.

## Inference Rules

- Face landmarks every frame if possible.
- Pose every 2nd frame if overlays are stable.
- Segmentation every 2nd or 3rd frame under heat.
- Body-part parsing every 3rd or 4th frame, never every frame on S21 FE unless profiling proves it.
- If body swap is enabled, prefer torso-only or upper-body-only swap on thermal pressure.
- Switch from GPU delegate to NNAPI only after profiling on target device.

## Thermal Controls

- Drop segmentation cadence before lowering landmark cadence.
- Reduce internal resolution from 720p to 540p when device temperature rises.
- Stop recording and heavy effects in background state.

## Save Photo/Video

- Photos: render composed frame to offscreen framebuffer, then encode JPEG.
- Video: mirror compositor output to `MediaCodec` input surface.
- Keep audio off unless required; this reduces sync complexity and CPU use.
