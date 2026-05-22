import {useMemo} from 'react';
import {VisionCameraProxy, type Frame} from 'react-native-vision-camera';

type MRFrameProcessorOptions = {
  enableSegmentation: boolean;
  enableBodyTracking: boolean;
  enableBodyPartSegmentation: boolean;
  enableBodySwap: boolean;
};
type MRFrameProcessorPlugin = (frame: Frame, options: MRFrameProcessorOptions) => void;

const plugin = VisionCameraProxy.initFrameProcessorPlugin('processMRFrame') as
  | MRFrameProcessorPlugin
  | undefined;

export function useMRFrameProcessor(options: MRFrameProcessorOptions) {
  return useMemo(() => {
    return (frame: Frame) => {
      'worklet';
      plugin?.(frame, options);
    };
  }, [
    options.enableBodyPartSegmentation,
    options.enableBodySwap,
    options.enableBodyTracking,
    options.enableSegmentation,
  ]);
}
