//
//  RCTCalendarModule.m
//  IosProject
//
//  Created by Natalia on 7/15/23.
//

#import <Foundation/Foundation.h>
// RCTCalendarModule.m
#import "RCTCalendarModule.h"
#import <React/RCTLog.h>
#import <EventKit/EventKit.h>
#import <React/RCTConvert.h>

@implementation RCTCalendarModule

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(createCalendarEvent:(NSString *)name location:(NSString *)location date:(NSString *)date)
{
  RCTLogInfo(@"Starting to create an event %@ at %@ on %@", name, location, date);

  // Parse the date string to a specific format
  NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
  dateFormat.dateFormat = @"yyyy-MM-dd";
  NSDate *eventDate = [dateFormat dateFromString:date];
  RCTLogInfo(@"! EventDate %@",  eventDate);
  
  if (!eventDate) {
    RCTLogError(@"Error parsing date: %@", date);
    return;
  }
  
  EKEventStore *eventStore = [[EKEventStore alloc] init];
  RCTLogInfo(@"! EventSTORE %@",  eventStore);
  if ([eventStore respondsToSelector:@selector(requestAccessToEntityType:completion:)]) {
    [eventStore requestAccessToEntityType:EKEntityTypeEvent completion:^(BOOL granted, NSError *error) {
      if (!granted) {
        RCTLogError(@"Access to calendar not granted");
        return;
      }
      
      EKEvent *event = [EKEvent eventWithEventStore:eventStore];
      RCTLogInfo(@"! Event %@",  event);
      event.title = name;
      event.location = location;
      event.startDate = eventDate;
      event.endDate = eventDate;
      event.allDay = YES;
      event.calendar = [eventStore defaultCalendarForNewEvents];
      RCTLogInfo(@"! Event %@",  event);
      
      NSError *saveError = nil;
      [eventStore saveEvent:event span:EKSpanThisEvent commit:YES error:&saveError];
      
      if (saveError) {
        RCTLogError(@"Failed to add event: %@", saveError);
      } else {
        RCTLogInfo(@"Event added successfully");
      }
    }];
  } else {
    RCTLogError(@"EventStore does not respond to requestAccessToEntityType:completion:");
  }
}

RCT_EXPORT_METHOD(getCalendarEventsForWeek:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  EKEventStore *eventStore = [[EKEventStore alloc] init];
  if ([eventStore respondsToSelector:@selector(requestAccessToEntityType:completion:)]) {
    [eventStore requestAccessToEntityType:EKEntityTypeEvent completion:^(BOOL granted, NSError *error) {
      if (!granted) {
        reject(@"calendar_access_denied", @"Access to calendar not granted", error);
        return;
      }

      NSDate *startDate = [NSDate date];
      NSDate *endDate = [NSDate dateWithTimeIntervalSinceNow:7 * 24 * 60 * 60]; // Fetch events for the next 7 days

      NSPredicate *predicate = [eventStore predicateForEventsWithStartDate:startDate endDate:endDate calendars:nil];
      RCTLogInfo(@"Predicate: %@", predicate);

      NSArray<EKEvent *> *events = [eventStore eventsMatchingPredicate:predicate];
      RCTLogInfo(@"Fetched events: %@", events);
      
      NSMutableArray<NSDictionary *> *eventList = [NSMutableArray array];
      NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
      dateFormatter.dateFormat = @"M/d/yy, h:mm a";

      for (EKEvent *event in events) {
        NSString *startDateString = [dateFormatter stringFromDate:event.startDate];
        RCTLogInfo(@"Start date converted: %@", startDateString);
        NSString *endDateString = [dateFormatter stringFromDate:event.endDate];
        NSDictionary *eventData = @{
          @"identifier": event.eventIdentifier,
          @"title": event.title,
          @"location": event.location ?: [NSNull null],
          @"startDate": startDateString ?: [NSNull null],
          @"endDate": endDateString ?: [NSNull null]
        };
        [eventList addObject:eventData];
      }
      RCTLogInfo(@"Event list: %@", eventList);

      resolve(eventList);
    }];
  }
  else {
    reject(@"calendar_unavailable", @"Calendar access unavailable", nil);
  }
}

RCT_EXPORT_METHOD(deleteCalendarEvent:(NSString *)eventIdentifier resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  RCTLogInfo(@"This identifier is detected , event it to be deleted: %@", eventIdentifier);
  EKEventStore *eventStore = [[EKEventStore alloc] init];
  
  // Request calendar access permission
  if ([eventStore respondsToSelector:@selector(requestAccessToEntityType:completion:)]) {
    [eventStore requestAccessToEntityType:EKEntityTypeEvent completion:^(BOOL granted, NSError *error) {
      if (!granted) {
        reject(@"calendar_access_denied", @"Access to calendar not granted", error);
        return;
      }
      
      // Fetch the event with the given event identifier
      EKEvent *event = [eventStore eventWithIdentifier:eventIdentifier];
      if (!event) {
        NSError *fetchError = [NSError errorWithDomain:@"CalendarModule" code:0 userInfo:@{ NSLocalizedDescriptionKey: @"Event not found" }];
        reject(@"event_not_found", @"Event not found", fetchError);
        return;
      }
      
      // Delete the event
      NSError *deleteError;
      [eventStore removeEvent:event span:EKSpanThisEvent error:&deleteError];
      if (deleteError) {
        reject(@"event_deletion_failed", @"Failed to delete event", deleteError);
        return;
      }
      
      resolve(@(YES));
    }];
  }
  else {
    reject(@"calendar_unavailable", @"Calendar access unavailable", nil);
  }
}


@end
