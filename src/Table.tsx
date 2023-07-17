import React from 'react';
import {View, Text, StyleSheet, TouchableOpacity} from 'react-native';

const Table = ({events, deleteEvent, isIOS}) => {
  console.log( events );
  const key = isIOS ? "identifier" : "eventId"
  console.log(`This is IOS-${isIOS} so the key is ${key}`)
  return (
    <View style={styles.container}>
      <Text style={styles.header}>Calendar Events</Text>
      <View style={styles.table}>
        {events.map((event, index) => (
          <View style={styles.row} key={index}>
            <Text style={styles.cell}>{event.title}</Text>
            <Text style={styles.cell}>{event.location}</Text>
            <Text style={styles.cell}>{event.startDate}</Text>
            <Text style={styles.cell}>{event.endDate}</Text>
            <TouchableOpacity
              style={styles.deleteButton}
              onPress={() => deleteEvent(event[key])}>
              <Text style={styles.deleteButtonText}>Delete</Text>
            </TouchableOpacity>
          </View>
        ))}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginVertical: 20,
  },
  header: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
    textAlign: 'center',
  },
  table: {
    borderRadius: 8,
    padding: 10,
  },
  row: {
    flexDirection: 'row',
    marginBottom: 5,
  },
  cell: {
    flex: 1,
  },
  deleteButton: {
    backgroundColor: 'red',
    paddingVertical: 4,
    paddingHorizontal: 8,
    borderRadius: 4,
  },
  deleteButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
});

export default Table;
