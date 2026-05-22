import {buildMobileConfig} from '../services/mobileConfigService.js';

export async function getMobileConfig(req, res) {
  const deviceModel = typeof req.query.deviceModel === 'string' ? req.query.deviceModel : 'unknown';
  const config = await buildMobileConfig({deviceModel});
  res.json(config);
}
