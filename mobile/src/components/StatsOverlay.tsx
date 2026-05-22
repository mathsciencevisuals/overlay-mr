import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import type {RenderStats} from '../types/mr';

export function StatsOverlay({stats}: {stats: RenderStats | null}) {
  if (!stats) {
    return null;
  }

  return (
    <View style={styles.container}>
      <Text style={styles.text}>{`FPS ${stats.fps.toFixed(1)}`}</Text>
      <Text style={styles.text}>{`INF ${stats.inferenceMs.toFixed(1)}ms`}</Text>
      <Text style={styles.text}>{`GL ${stats.renderMs.toFixed(1)}ms`}</Text>
      <Text style={styles.text}>{`DROP ${stats.droppedFrames}`}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    top: 24,
    left: 16,
    padding: 12,
    borderRadius: 16,
    backgroundColor: 'rgba(2, 6, 23, 0.7)',
  },
  text: {
    color: '#E2E8F0',
    fontSize: 12,
    fontWeight: '600',
  },
});
