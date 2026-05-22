import React from 'react';
import {SafeAreaProvider} from 'react-native-safe-area-context';
import {MRScreen} from './src/screens/MRScreen';

export default function App(): React.JSX.Element {
  return (
    <SafeAreaProvider>
      <MRScreen />
    </SafeAreaProvider>
  );
}
