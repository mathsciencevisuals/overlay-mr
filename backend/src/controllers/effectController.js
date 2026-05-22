import {fetchEffect, fetchEffects} from '../services/effectService.js';

export async function listEffects(_req, res) {
  const items = await fetchEffects();
  res.json({items});
}

export async function getEffect(req, res) {
  const effect = await fetchEffect(req.params.id);
  if (!effect) {
    res.status(404).json({error: 'Effect not found'});
    return;
  }

  res.json(effect);
}
