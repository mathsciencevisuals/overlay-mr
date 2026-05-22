import {z} from 'zod';
import {patchSession, startSession} from '../services/sessionRuntimeService.js';

const createSessionSchema = z.object({
  userId: z.string().min(1).optional(),
  deviceModel: z.string().min(1),
  effectsUsed: z.array(z.string()).default([]),
  avgFps: z.number().nonnegative().optional(),
  thermalState: z.string().min(1).optional(),
});

const patchSessionSchema = z.object({
  effectsUsed: z.array(z.string()).optional(),
  avgFps: z.number().nonnegative().optional(),
  thermalState: z.string().min(1).optional(),
  droppedFrames: z.number().int().nonnegative().optional(),
});

export async function createSession(req, res) {
  const payload = createSessionSchema.parse(req.body);
  const session = await startSession(payload);
  res.status(201).json(session);
}

export async function updateRuntimeSession(req, res) {
  const payload = patchSessionSchema.parse(req.body);
  const session = await patchSession(req.params.id, payload);
  if (!session) {
    res.status(404).json({error: 'Session not found'});
    return;
  }

  res.json(session);
}
