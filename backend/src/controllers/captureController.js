import {z} from 'zod';
import {fetchCaptures, registerCapture} from '../services/captureService.js';

const createCaptureSchema = z.object({
  userId: z.string().min(1).optional(),
  type: z.enum(['photo', 'video']),
  storagePath: z.string().min(1),
  effectId: z.string().min(1).optional(),
  deviceModel: z.string().min(1).optional(),
});

export async function createCapture(req, res) {
  const payload = createCaptureSchema.parse(req.body);
  const capture = await registerCapture(payload);
  res.status(201).json(capture);
}

export async function listCaptures(req, res) {
  const items = await fetchCaptures({
    userId: typeof req.query.userId === 'string' ? req.query.userId : undefined,
  });
  res.json({items});
}
