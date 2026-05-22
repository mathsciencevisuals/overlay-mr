import {NativeEventEmitter, NativeModules} from 'react-native';
import type {
  FaceTrackingResult,
  PoseTrackingResult,
  RenderStats,
  SegmentationResult,
  SessionConfig,
} from '../types/mr';

type NativeMRModule = {
  initialize(config: SessionConfig): Promise<void>;
  updateConfig(config: Partial<SessionConfig>): Promise<void>;
  startProcessing(): Promise<void>;
  stopProcessing(): Promise<void>;
  capturePhoto(): Promise<string>;
  startRecording(): Promise<void>;
  stopRecording(): Promise<string>;
};

const {MRNativeModule} = NativeModules as {MRNativeModule: NativeMRModule};

export const mrNativeModule = MRNativeModule;
export const mrEvents = new NativeEventEmitter(NativeModules.MRNativeModule);

export type NativeEvents = {
  onFaceTracking: FaceTrackingResult;
  onPoseTracking: PoseTrackingResult;
  onSegmentation: SegmentationResult;
  onRenderStats: RenderStats;
};
