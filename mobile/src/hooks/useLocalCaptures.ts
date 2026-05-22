import {useEffect, useState} from 'react';
import {listLocalCaptures, saveLocalCapture} from '../services/localCaptures';
import type {LocalCapture} from '../types/capture';

export function useLocalCaptures() {
  const [captures, setCaptures] = useState<LocalCapture[]>([]);

  useEffect(() => {
    listLocalCaptures().then(setCaptures).catch(console.error);
  }, []);

  const addCapture = async (input: Omit<LocalCapture, 'id' | 'createdAt'>) => {
    const item = await saveLocalCapture(input);
    setCaptures(current => [item, ...current].slice(0, 200));
    return item;
  };

  return {
    captures,
    addCapture,
  };
}
