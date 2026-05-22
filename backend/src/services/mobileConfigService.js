export async function buildMobileConfig({deviceModel}) {
  const s21Class = /S21 FE/i.test(deviceModel);
  return {
    runtime: {
      targetFps: 30,
      cameraPreset: '1280x720',
      internalResolution: s21Class ? {width: 1280, height: 720} : {width: 960, height: 540},
      segmentationStride: s21Class ? 2 : 3,
      bodyPartSegmentationStride: s21Class ? 3 : 4,
    },
    assets: {
      faceTrackingModel: 'face_landmarker.task',
      poseTrackingModel: 'pose_landmarker.task',
      segmentationModel: 'selfie_segmenter.tflite',
    },
    features: {
      faceSwap: true,
      bodyOverlay: true,
      segmentation: true,
      bodyPartSegmentation: s21Class,
      bodySwap: false,
      videoCapture: true,
    },
  };
}
