/**
 * This exposes the native CalendarModule and BirthdayModule modules as JS modules.
 * CalendarModule has functions for creating calendar events and retrieving calendar events for the week.
 * BirthdayModule has a function for retrieving the number of days until the next birthday event.
 */
import {NativeModules} from 'react-native';

const {CalendarModule} = NativeModules;

interface CalendarInterface {
  createCalendarEvent(name: string, location: string, date: string): void;
  getCalendarEventsForWeek(): Promise<CalendarEvent[]>;
  deleteCalendarEvent(eventIdentifier: string): Promise<boolean>;
}

interface CalendarEvent {
  title: string;
  location: string;
  startDate: string;
  endDate: string;
}

export const Calendar: CalendarInterface = CalendarModule;
