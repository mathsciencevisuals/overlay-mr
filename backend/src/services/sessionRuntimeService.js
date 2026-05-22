import {createSession, updateSession} from './dataStore.js';

export async function startSession(payload) {
  return createSession(payload);
}

export async function patchSession(id, payload) {
  return updateSession(id, payload);
}
