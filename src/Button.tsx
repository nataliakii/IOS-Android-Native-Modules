import React from 'react';
import {Button} from 'react-native';
import {Calendar} from './NativeModules';

const ButtonModule = ({fetchCalendarEvents}) => {
  const onPress = async () => {
    try {
      await Calendar.createCalendarEvent('bd', 'kyiv', '2023-07-24');
      fetchCalendarEvents();
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Button
      title="Click to invoke your native module!"
      color="#841584"
      onPress={onPress}
    />
  );
};

export default ButtonModule;
