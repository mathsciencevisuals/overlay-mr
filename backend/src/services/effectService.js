import {getEffectById, listEffects} from './dataStore.js';

export async function fetchEffects() {
  return listEffects();
}

export async function fetchEffect(id) {
  return getEffectById(id);
}
