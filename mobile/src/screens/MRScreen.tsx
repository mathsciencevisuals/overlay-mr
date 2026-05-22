import React, {useEffect, useState} from 'react';
import {AppState, PermissionsAndroid, Platform, StyleSheet, View} from 'react-native';
import {Camera, useCameraDevice} from 'react-native-vision-camera';
import {CaptureStrip} from '../components/CaptureStrip';
import {ControlBar} from '../components/ControlBar';
import {StatsOverlay} from '../components/StatsOverlay';
import {useLocalCaptures} from '../hooks/useLocalCaptures';
import {useMRSession} from '../hooks/useMRSession';
import {useMRFrameProcessor} from '../services/frameProcessor';

export function MRScreen() {
  const device = useCameraDevice('front');
  const [recording, setRecording] = useState(false);
  const mr = useMRSession();
  const {captures, addCapture} = useLocalCaptures();
  const frameProcessor = useMRFrameProcessor({
    enableSegmentation: mr.config.segmentationEnabled,
    enableBodyTracking: mr.config.bodyTrackingEnabled,
    enableBodyPartSegmentation: mr.config.bodyPartSegmentationEnabled,
    enableBodySwap: mr.config.bodySwapEnabled,
  });

  useEffect(() => {
    requestPermissions().then(mr.start).catch(console.error);
    return () => {
      mr.stop().catch(console.error);
    };
  }, []);

  useEffect(() => {
    const sub = AppState.addEventListener('change', nextState => {
      if (nextState === 'active') {
        mr.start().catch(console.error);
        return;
      }
      mr.stop().catch(console.error);
    });

    return () => {
      sub.remove();
    };
  }, [mr]);

  if (!device) {
    return <View style={styles.root} />;
  }

  return (
    <View style={styles.root}>
      <Camera
        style={StyleSheet.absoluteFill}
        device={device}
        isActive
        photo
        video
        audio={false}
        preset="1280x720"
        pixelFormat="yuv"
        frameProcessor={frameProcessor}
      />
      <StatsOverlay stats={mr.stats} />
      <View style={styles.captureStrip}>
        <CaptureStrip captures={captures} />
      </View>
      <ControlBar
        config={mr.config}
        isRecording={recording}
        onToggleSegmentation={() =>
          mr.updateConfig({segmentationEnabled: !mr.config.segmentationEnabled}).catch(console.error)
        }
        onToggleBodyTracking={() =>
          mr.updateConfig({bodyTrackingEnabled: !mr.config.bodyTrackingEnabled}).catch(console.error)
        }
        onToggleBodyParts={() =>
          mr
            .updateConfig({
              bodyPartSegmentationEnabled: !mr.config.bodyPartSegmentationEnabled,
              bodySwapEnabled: !mr.config.bodyPartSegmentationEnabled,
            })
            .catch(console.error)
        }
        onCapturePhoto={async () => {
          const path = await mr.capturePhoto();
          await addCapture({
            kind: 'photo',
            path,
            overlayPreset: mr.config.overlayPreset,
          });
        }}
        onToggleRecord={async () => {
          if (recording) {
            const path = await mr.stopRecording();
            await addCapture({
              kind: 'video',
              path,
              overlayPreset: mr.config.overlayPreset,
            });
            setRecording(false);
            return;
          }
          await mr.startRecording();
          setRecording(true);
        }}
      />
    </View>
  );
}

async function requestPermissions() {
  if (Platform.OS !== 'android') {
    return;
  }

  const granted = await PermissionsAndroid.requestMultiple([
    PermissionsAndroid.PERMISSIONS.CAMERA,
    PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
    PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
  ]);

  const denied = Object.values(granted).some(value => value !== PermissionsAndroid.RESULTS.GRANTED);
  if (denied) {
    throw new Error('Camera permissions are required.');
  }
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#020617',
  },
  captureStrip: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 88,
  },
});
