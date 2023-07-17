import React, {useState, useEffect} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  useColorScheme,
  View,
} from 'react-native';
import {Colors} from 'react-native/Libraries/NewAppScreen';
import ButtonModule from './Button';
import {Calendar} from './NativeModules';
import {Platform} from 'react-native';
import Table from './Table';

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const isIOS = Platform.OS === 'ios';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const [calendarEvents, setCalendarEvents] = useState([]);

  useEffect(() => {
    fetchCalendarEvents();
  }, []);

  const fetchCalendarEvents = () => {
    Calendar.getCalendarEventsForWeek()
      .then(events => {
        setCalendarEvents(events);
      })
      .catch(error => {
        console.error(error);
      });
  };
  const deleteEvent = (eventIdentifier: string): Promise<boolean> => {
    //if (isIOS) {
    console.log('this is ios');
    console.log('eventId is', eventIdentifier);
    return new Promise((resolve, reject) => {
      Calendar.deleteCalendarEvent(eventIdentifier)
        .then((success: boolean) => {
          resolve(success);
        })
        .catch((error: Error) => {
          reject(error);
        })
        .finally(() => {
          fetchCalendarEvents();
        });
    });
    //} else {
    //  console.log('it is not ios, cannot delete event');
    //  return Promise.reject(
    //    new Error('Cannot delete event on non-iOS platforms'),
    //  );
    //}
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}>
          <ButtonModule fetchCalendarEvents={fetchCalendarEvents} />
          <Table
            events={calendarEvents}
            deleteEvent={deleteEvent}
            isIOS={isIOS}
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

export default App;
