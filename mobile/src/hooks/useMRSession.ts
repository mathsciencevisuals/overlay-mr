import {useEffect, useRef, useState} from 'react';
import {mrEvents, mrNativeModule} from '../services/nativeBridge';
import {createRuntimeSession, fetchSessionConfig, updateRuntimeSession} from '../services/api';
import type {
  FaceTrackingResult,
  PoseTrackingResult,
  RenderStats,
  SegmentationResult,
  SessionConfig,
} from '../types/mr';

const defaultConfig: SessionConfig = {
  overlayPreset: 'minimal',
  segmentationEnabled: true,
  bodyPartSegmentationEnabled: false,
  bodySwapEnabled: false,
  bodyTrackingEnabled: true,
  saveVideoEnabled: false,
};

export function useMRSession() {
  const [config, setConfig] = useState<SessionConfig>(defaultConfig);
  const [face, setFace] = useState<FaceTrackingResult | null>(null);
  const [pose, setPose] = useState<PoseTrackingResult | null>(null);
  const [segmentation, setSegmentation] = useState<SegmentationResult | null>(null);
  const [stats, setStats] = useState<RenderStats | null>(null);
  const sessionIdRef = useRef<string | null>(null);

  useEffect(() => {
    fetchSessionConfig('Samsung Galaxy S21 FE')
      .then(async remoteConfig => {
        setConfig(remoteConfig);
        await mrNativeModule.initialize(remoteConfig);
        const session = await createRuntimeSession({
          deviceModel: 'Samsung Galaxy S21 FE',
          effectsUsed: [remoteConfig.overlayPreset],
        });
        sessionIdRef.current = session.id;
      })
      .catch(async error => {
        console.error(error);
        await mrNativeModule.initialize(config);
      });

    const subs = [
      mrEvents.addListener('onFaceTracking', setFace),
      mrEvents.addListener('onPoseTracking', setPose),
      mrEvents.addListener('onSegmentation', setSegmentation),
      mrEvents.addListener('onRenderStats', (nextStats: RenderStats) => {
        setStats(nextStats);
        if (sessionIdRef.current) {
          updateRuntimeSession(sessionIdRef.current, {
            avgFps: nextStats.fps,
            droppedFrames: nextStats.droppedFrames,
          }).catch(console.error);
        }
      }),
    ];
    return () => {
      subs.forEach(sub => sub.remove());
      mrNativeModule.stopProcessing().catch(console.error);
    };
  }, []);

  const updateConfig = async (patch: Partial<SessionConfig>) => {
    const next = {...config, ...patch};
    setConfig(next);
    await mrNativeModule.updateConfig(patch);
  };

  return {
    config,
    face,
    pose,
    segmentation,
    stats,
    start: () => mrNativeModule.startProcessing(),
    stop: () => mrNativeModule.stopProcessing(),
    capturePhoto: () => mrNativeModule.capturePhoto(),
    startRecording: () => mrNativeModule.startRecording(),
    stopRecording: () => mrNativeModule.stopRecording(),
    updateConfig,
  };
}
