export type Landmark = {
  x: number;
  y: number;
  z: number;
  visibility?: number;
};

export type FaceTrackingResult = {
  timestamp: number;
  landmarks: Landmark[];
  blendshapeScores?: Record<string, number>;
  boundingBox?: {x: number; y: number; width: number; height: number};
  facialTransformationMatrix?: number[];
};

export type PoseTrackingResult = {
  timestamp: number;
  landmarks: Landmark[];
};

export type SegmentationResult = {
  timestamp: number;
  width: number;
  height: number;
  maskTextureId?: number;
  kind?: 'person' | 'body-parts';
  partMasks?: BodyPartMask[];
};

export type BodyPartMask = {
  part:
    | 'hair'
    | 'faceSkin'
    | 'torso'
    | 'leftArm'
    | 'rightArm'
    | 'leftLeg'
    | 'rightLeg';
  textureId?: number;
  confidence: number;
};

export type RenderStats = {
  fps: number;
  inferenceMs: number;
  renderMs: number;
  droppedFrames: number;
};

export type SessionConfig = {
  swapAssetId?: string;
  overlayPreset: 'minimal' | 'sport' | 'cyber' | 'custom';
  segmentationEnabled: boolean;
  bodyPartSegmentationEnabled: boolean;
  bodySwapEnabled: boolean;
  bodyTrackingEnabled: boolean;
  saveVideoEnabled: boolean;
};
