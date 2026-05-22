import {createCapture, listCaptures} from './dataStore.js';

export async function registerCapture(payload) {
  return createCapture(payload);
}

export async function fetchCaptures({userId}) {
  return listCaptures({userId});
}
