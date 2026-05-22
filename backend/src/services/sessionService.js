import {Storage} from '@google-cloud/storage';
import {buildMobileConfig} from './mobileConfigService.js';

const storage = new Storage({
  projectId: process.env.GCP_PROJECT_ID,
});

export function getOverlayPresets() {
  return [
    {id: 'minimal', bodyOverlayUrl: '/assets/minimal.json'},
    {id: 'sport', bodyOverlayUrl: '/assets/sport.json'},
    {id: 'cyber', bodyOverlayUrl: '/assets/cyber.json'},
  ];
}

export async function buildSessionConfig({deviceModel}) {
  const config = await buildMobileConfig({deviceModel});
  return {
    targetFps: config.runtime.targetFps,
    internalResolution: config.runtime.internalResolution,
    segmentationStride: config.runtime.segmentationStride,
    bodyPartSegmentationStride: config.runtime.bodyPartSegmentationStride,
    faceTrackingModel: config.assets.faceTrackingModel,
    poseTrackingModel: config.assets.poseTrackingModel,
    features: config.features,
  };
}

export async function buildUploadUrl({filename, contentType}) {
  const bucketName = process.env.ASSET_BUCKET;
  if (!bucketName) {
    return {
      method: 'PUT',
      uploadUrl: `https://example.invalid/mock/${filename}`,
      fileUrl: `https://example.invalid/mock/${filename}`,
      contentType,
    };
  }

  const bucket = storage.bucket(bucketName);
  const file = bucket.file(`uploads/${Date.now()}-${filename}`);
  const expires = Date.now() + Number(process.env.SIGNED_URL_TTL_SECONDS || 900) * 1000;
  const [uploadUrl] = await file.getSignedUrl({
    version: 'v4',
    action: 'write',
    expires,
    contentType,
  });

  return {
    method: 'PUT',
    uploadUrl,
    fileUrl: `gs://${bucketName}/${file.name}`,
    contentType,
  };
}
