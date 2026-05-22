import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import type {SessionConfig} from '../types/mr';

type Props = {
  config: SessionConfig;
  onToggleSegmentation: () => void;
  onToggleBodyTracking: () => void;
  onToggleBodyParts: () => void;
  onCapturePhoto: () => void;
  onToggleRecord: () => void;
  isRecording: boolean;
};

export function ControlBar({
  config,
  onToggleSegmentation,
  onToggleBodyTracking,
  onToggleBodyParts,
  onCapturePhoto,
  onToggleRecord,
  isRecording,
}: Props) {
  return (
    <View style={styles.container}>
      <Action label={config.segmentationEnabled ? 'Mask On' : 'Mask Off'} onPress={onToggleSegmentation} />
      <Action label={config.bodyTrackingEnabled ? 'Body On' : 'Body Off'} onPress={onToggleBodyTracking} />
      <Action label={config.bodyPartSegmentationEnabled ? 'Parts On' : 'Parts Off'} onPress={onToggleBodyParts} />
      <Action label="Photo" onPress={onCapturePhoto} />
      <Action label={isRecording ? 'Stop' : 'Rec'} onPress={onToggleRecord} active={isRecording} />
    </View>
  );
}

function Action({label, onPress, active}: {label: string; onPress: () => void; active?: boolean}) {
  return (
    <Pressable onPress={onPress} style={[styles.button, active && styles.buttonActive]}>
      <Text style={styles.label}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    bottom: 24,
    left: 16,
    right: 16,
    flexDirection: 'row',
    gap: 8,
    justifyContent: 'space-between',
  },
  button: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 18,
    backgroundColor: 'rgba(15, 23, 42, 0.78)',
    borderWidth: 1,
    borderColor: 'rgba(148, 163, 184, 0.3)',
  },
  buttonActive: {
    backgroundColor: 'rgba(220, 38, 38, 0.85)',
  },
  label: {
    color: '#F8FAFC',
    textAlign: 'center',
    fontWeight: '700',
  },
});
