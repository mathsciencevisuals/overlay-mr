import type {SessionConfig} from '../types/mr';

const API_BASE = 'http://10.0.2.2:8080/api';

export type MobileRuntimeConfig = {
  runtime: {
    targetFps: number;
    cameraPreset: string;
    internalResolution: {width: number; height: number};
    segmentationStride: number;
    bodyPartSegmentationStride: number;
  };
  assets: {
    faceTrackingModel: string;
    poseTrackingModel: string;
    segmentationModel: string;
  };
  features: {
    faceSwap: boolean;
    bodyOverlay: boolean;
    segmentation: boolean;
    bodyPartSegmentation: boolean;
    bodySwap: boolean;
    videoCapture: boolean;
  };
};

export async function fetchSessionConfig(deviceModel: string): Promise<SessionConfig> {
  const response = await fetch(`${API_BASE}/session/config?deviceModel=${encodeURIComponent(deviceModel)}`);
  if (!response.ok) {
    throw new Error(`Config request failed: ${response.status}`);
  }

  const payload = await response.json();
  return {
    overlayPreset: 'minimal',
    segmentationEnabled: payload.features?.segmentation ?? true,
    bodyPartSegmentationEnabled: payload.features?.bodyPartSegmentation ?? false,
    bodySwapEnabled: payload.features?.bodySwap ?? false,
    bodyTrackingEnabled: payload.features?.bodyOverlay ?? true,
    saveVideoEnabled: payload.features?.videoCapture ?? true,
  };
}

export async function fetchMobileConfig(deviceModel: string): Promise<MobileRuntimeConfig> {
  const response = await fetch(`${API_BASE}/config/mobile?deviceModel=${encodeURIComponent(deviceModel)}`);
  if (!response.ok) {
    throw new Error(`Mobile config request failed: ${response.status}`);
  }

  return response.json();
}

export async function createUploadUrl(filename: string, contentType: string) {
  const response = await fetch(`${API_BASE}/media/upload-url`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({filename, contentType}),
  });

  if (!response.ok) {
    throw new Error(`Upload URL request failed: ${response.status}`);
  }

  return response.json();
}

export async function createRuntimeSession(payload: {
  userId?: string;
  deviceModel: string;
  effectsUsed?: string[];
  avgFps?: number;
  thermalState?: string;
}) {
  const response = await fetch(`${API_BASE}/sessions`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error(`Session create failed: ${response.status}`);
  }

  return response.json();
}

export async function updateRuntimeSession(
  sessionId: string,
  payload: {effectsUsed?: string[]; avgFps?: number; thermalState?: string; droppedFrames?: number},
) {
  const response = await fetch(`${API_BASE}/sessions/${sessionId}`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error(`Session patch failed: ${response.status}`);
  }

  return response.json();
}
