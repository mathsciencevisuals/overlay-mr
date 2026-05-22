import {Router} from 'express';
import {getSessionConfig, createSignedUpload, listOverlayPresets} from '../controllers/sessionController.js';
import {createCapture, listCaptures} from '../controllers/captureController.js';
import {getEffect, listEffects} from '../controllers/effectController.js';
import {getMobileConfig} from '../controllers/mobileConfigController.js';
import {createSession, updateRuntimeSession} from '../controllers/runtimeSessionController.js';

export const router = Router();

router.get('/session/config', getSessionConfig);
router.get('/config/mobile', getMobileConfig);
router.get('/overlays', listOverlayPresets);
router.get('/effects', listEffects);
router.get('/effects/:id', getEffect);
router.get('/captures', listCaptures);
router.post('/captures', createCapture);
router.post('/sessions', createSession);
router.patch('/sessions/:id', updateRuntimeSession);
router.post('/media/upload-url', createSignedUpload);
