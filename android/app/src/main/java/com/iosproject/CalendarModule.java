package com.iosproject;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.TimeZone;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.net.Uri;
import android.database.Cursor;
import android.content.ContentUris;
import android.annotation.SuppressLint;
import android.util.Log;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;


public class CalendarModule extends ReactContextBaseJavaModule {
    public CalendarModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "CalendarModule";
    }

    @ReactMethod
    public void createCalendarEvent(String name, String location, String date) {
        Log.d("CalendarModule", "Create event called with name: " + name
                + ", location: " + location
                + ", and date: " + date);

        // Parse the date string to a specific format if needed
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date eventDate;
        try {
            eventDate = dateFormat.parse(date);
            Log.d("eventDate", "Create event Date: " +  eventDate);
        } catch (ParseException e) {
            Log.e("CalendarModule", "Error parsing date: " + date, e);
            return;
        }

        // Get the content resolver and calendar ID
        ContentResolver contentResolver = getReactApplicationContext().getContentResolver();
        long calendarId = getCalendarId();
        Log.d("contentResolver", contentResolver.toString());

        // Create the event
        ContentValues eventValues;
        eventValues = new ContentValues();
        eventValues.put(Events.CALENDAR_ID, calendarId);
        eventValues.put(Events.TITLE, name);
        eventValues.put(Events.EVENT_LOCATION, location);
        eventValues.put(Events.DTSTART, eventDate.getTime());
        eventValues.put(Events.DTEND, eventDate.getTime());
        eventValues.put(Events.ALL_DAY, 1);
        eventValues.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        // Insert the event
        Uri eventUri = contentResolver.insert(Events.CONTENT_URI, eventValues);
        Log.d("eventUri", eventUri.toString());

        if (eventUri != null) {
            Log.d("CalendarModule", "Event added successfully");
        } else {
            Log.e("CalendarModule", "Failed to add event");
        }
    }

    @ReactMethod
    public void getCalendarEventsForWeek(Promise promise) {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        // Set the start date to the beginning of the week
        long startMillis = calendar.getTimeInMillis();

        // Set the end date to the end of the week
        calendar.add(Calendar.DAY_OF_WEEK, 20);
        long endMillis = calendar.getTimeInMillis();

        // Define the range of the query
        String[] projection = {
                CalendarContract.Events.TITLE,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events._ID,
                CalendarContract.Events._ID + " AS identifier" // Alias for the identifier field
        };
        String selection = CalendarContract.Events.DTSTART + " >= ? AND " +
                CalendarContract.Events.DTEND + " <= ?";
        String[] selectionArgs = {
                String.valueOf(startMillis),
                String.valueOf(endMillis)
        };
        String sortOrder = CalendarContract.Events.DTSTART + " ASC";

        // Query the calendar events
        Cursor cursor = getReactApplicationContext().getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );

        // Create an array to hold the event data
        WritableArray eventArray = Arguments.createArray();

        if (cursor != null) {
            int titleIndex = cursor.getColumnIndex(CalendarContract.Events.TITLE);
            int locationIndex = cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
            int startDateIndex = cursor.getColumnIndex(CalendarContract.Events.DTSTART);
            int endDateIndex = cursor.getColumnIndex(CalendarContract.Events.DTEND);
            int eventIdIndex = cursor.getColumnIndex(CalendarContract.Events._ID);


            while (cursor.moveToNext()) {
                // Extract event details from the cursor
                String title = cursor.getString(titleIndex);
                String location = cursor.getString(locationIndex);
                long startDate = cursor.getLong(startDateIndex);
                long endDate = cursor.getLong(endDateIndex);
                String eventId = cursor.getString(eventIdIndex);

                // Create a map to hold the event details
                WritableMap eventMap = Arguments.createMap();
                eventMap.putString("title", title);
                eventMap.putString("location", location);
                eventMap.putString("startDate", formatDate(startDate));
                eventMap.putString("endDate", formatDate(endDate));
                eventMap.putString("eventId", cursor.getString(eventIdIndex));

                // Add the event map to the event array
                eventArray.pushMap(eventMap);
            }
            cursor.close();
        }

        // Resolve the promise with the event data
        promise.resolve(eventArray);
    }



    // Helper method to format the date as a string
    private String formatDate(long millis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(new Date(millis));
    }
    // Helper method to get the calendar ID
    @SuppressLint("LongLogTag")
    private long getCalendarId() {
        String[] projection = { CalendarContract.Calendars._ID };
        Log.d("projection", projection.toString());
        String selection = CalendarContract.Calendars.VISIBLE + " = 1";
        Log.d("selection", selection);
        Cursor cursor = getReactApplicationContext().getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int calendarIdIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID);
            long calendarId = cursor.getLong(calendarIdIndex);
            cursor.close();
            return calendarId;
        } else {
            Log.e("CalendarModule", "No calendar found");
            return -1;
        }
    }

    @ReactMethod
    public void deleteCalendarEvent(String eventIdentifier, Promise promise) {
        // Get the default calendar content resolver
        ContentResolver contentResolver = getReactApplicationContext().getContentResolver();

        // Create the URI for the event to be deleted
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(eventIdentifier));

        // Delete the event
        int rowsDeleted = contentResolver.delete(deleteUri, null, null);

        // Check if the event was successfully deleted
        if (rowsDeleted > 0) {
            promise.resolve(true);
        } else {
            promise.reject("event_deletion_failed", "Failed to delete event");
        }
    }



}
