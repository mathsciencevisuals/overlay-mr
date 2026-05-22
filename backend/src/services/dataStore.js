const store = {
  captures: [],
  sessions: [],
  effects: [
    {
      id: 'minimal',
      name: 'Minimal Overlay',
      type: 'faceSticker',
      assetUrls: ['/assets/minimal.json'],
      renderOrder: 10,
      active: true,
      version: 1,
    },
    {
      id: 'sport',
      name: 'Sport Overlay',
      type: 'torsoOverlay',
      assetUrls: ['/assets/sport.json'],
      renderOrder: 20,
      active: true,
      version: 1,
    },
    {
      id: 'cyber',
      name: 'Cyber Overlay',
      type: 'faceTexture',
      assetUrls: ['/assets/cyber.json'],
      renderOrder: 30,
      active: true,
      version: 1,
    },
  ],
};

function nowIso() {
  return new Date().toISOString();
}

function nextId(prefix) {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
}

export async function listEffects() {
  return store.effects.filter(effect => effect.active);
}

export async function getEffectById(id) {
  return store.effects.find(effect => effect.id === id) || null;
}

export async function createCapture(input) {
  const capture = {
    id: nextId('capture'),
    createdAt: nowIso(),
    ...input,
  };
  store.captures.unshift(capture);
  return capture;
}

export async function listCaptures({userId}) {
  return store.captures.filter(capture => !userId || capture.userId === userId);
}

export async function createSession(input) {
  const session = {
    id: nextId('session'),
    createdAt: nowIso(),
    updatedAt: nowIso(),
    ...input,
  };
  store.sessions.unshift(session);
  return session;
}

export async function updateSession(id, patch) {
  const session = store.sessions.find(item => item.id === id);
  if (!session) {
    return null;
  }

  Object.assign(session, patch, {updatedAt: nowIso()});
  return session;
}
