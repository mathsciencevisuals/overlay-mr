import {z} from 'zod';
import {buildSessionConfig, buildUploadUrl, getOverlayPresets} from '../services/sessionService.js';

export async function getSessionConfig(req, res) {
  const config = await buildSessionConfig({
    deviceModel: String(req.query.deviceModel || 'unknown'),
  });
  res.json(config);
}

export async function listOverlayPresets(_req, res) {
  res.json({items: getOverlayPresets()});
}

export async function createSignedUpload(req, res) {
  const payload = z.object({
    filename: z.string().min(1),
    contentType: z.string().min(1),
  }).parse(req.body);

  const signed = await buildUploadUrl(payload);
  res.json(signed);
}
