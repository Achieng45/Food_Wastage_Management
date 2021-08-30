package com.steph.foodwastagemanagement;

public class Event {
    private String Location;
    private String  Plates;
    private String Date;
    private String uid;
    private String Event;
    private String eventID;
    public Event() {
    }

    public Event(String location, String plates, String date,String uid,String Event,String eventID) {
       this.Location = location;
      this.Plates = plates;
       this.Date = date;
       this.uid=uid;
       this.Event=Event;
       this.eventID=eventID;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getPlates() {
        return Plates;
    }
    public String getEvent() {
        return this.Event;
    }
    public void setEvent(String Event) {
        this.Event = Event;
    }
    public void setPlates(String plates) {
        this.Plates = plates;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
    public void setuid(String uid) {
        this.uid=uid;
    }
    public String getuid() {
        return uid;
    }
    public String getEventID() {
        return this.eventID;
    }
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
}
