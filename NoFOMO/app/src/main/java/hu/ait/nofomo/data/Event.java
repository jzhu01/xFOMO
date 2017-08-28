package hu.ait.nofomo.data;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by MacOwner on 5/21/17.
 */

public class Event extends RealmObject{

    @PrimaryKey
    private String eventKeyID;

    private String desc;
    private String endTime;
    private String name;
    private Place place;
    private String startTime;
    private String eventID;

    public Event(String description, String endTime, String name, Place place, String startTime, String eventID){
        this.desc = description;
        this.endTime = endTime;
        this.name = name;
        this.place = place;
        this.startTime = startTime;
        this.eventID = eventID;

    }

    public Event(){}


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public String getEventKeyID() {
        return eventKeyID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
}
