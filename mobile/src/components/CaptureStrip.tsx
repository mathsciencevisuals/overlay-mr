import React from 'react';
import {ScrollView, StyleSheet, Text, View} from 'react-native';
import type {LocalCapture} from '../types/capture';

export function CaptureStrip({captures}: {captures: LocalCapture[]}) {
  if (captures.length === 0) {
    return null;
  }

  return (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={styles.content}>
      {captures.map(capture => (
        <View key={capture.id} style={styles.card}>
          <Text style={styles.kind}>{capture.kind.toUpperCase()}</Text>
          <Text style={styles.path} numberOfLines={1}>
            {capture.path}
          </Text>
          <Text style={styles.meta}>{capture.overlayPreset}</Text>
        </View>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: {
    gap: 10,
    paddingHorizontal: 16,
    paddingBottom: 96,
  },
  card: {
    width: 220,
    padding: 12,
    borderRadius: 16,
    backgroundColor: 'rgba(15, 23, 42, 0.74)',
    borderWidth: 1,
    borderColor: 'rgba(148, 163, 184, 0.25)',
  },
  kind: {
    color: '#F8FAFC',
    fontWeight: '800',
    fontSize: 11,
    marginBottom: 6,
  },
  path: {
    color: '#E2E8F0',
    fontSize: 11,
  },
  meta: {
    color: '#94A3B8',
    fontSize: 10,
    marginTop: 6,
  },
});
