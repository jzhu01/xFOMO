package hu.ait.nofomo.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by MacOwner on 5/21/17.
 */

public class Place extends RealmObject{

    @PrimaryKey
    private String placeID;

    private String name;

    private EventLocation loc;

    private String id;

    public Place(String name, EventLocation location, String id){
        this.name = name;
        this.loc = location;
        this.id = id;

    }

    public Place(String name){
        this.name = name;
    }

    public Place(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EventLocation getLoc() {
        return loc;
    }

    public void setLoc(EventLocation loc) {
        this.loc = loc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaceID() {
        return placeID;
    }



}
