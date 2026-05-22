import AsyncStorage from '@react-native-async-storage/async-storage';
import type {LocalCapture} from '../types/capture';

const STORAGE_KEY = 'overlay-mr/local-captures';

export async function listLocalCaptures(): Promise<LocalCapture[]> {
  const raw = await AsyncStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return [];
  }

  try {
    const parsed = JSON.parse(raw) as LocalCapture[];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export async function saveLocalCapture(
  input: Omit<LocalCapture, 'id' | 'createdAt'>,
): Promise<LocalCapture> {
  const item: LocalCapture = {
    id: `capture_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    createdAt: new Date().toISOString(),
    ...input,
  };

  const current = await listLocalCaptures();
  const next = [item, ...current].slice(0, 200);
  await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  return item;
}
