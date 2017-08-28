package hu.ait.nofomo.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by MacOwner on 5/21/17.
 */

public class EventLocation extends RealmObject {
    @PrimaryKey
    private String locationID;

    private String city;
    private String country;
    private double lat;
    private double lng;
    private String str;
    private int zip;

    public EventLocation(String city, String country, double lat, double lng, String str, int zip ){
        this.city = city;
        this.country = country;
        this.lat = lat;
        this.lng = lng;
        this.str = str;
        this.zip = zip;
    }

    public EventLocation(String city, String country, double lat, double lng){
        this.city = city;
        this.country = country;
        this.lat = lat;
        this.lng = lng;
    }

    public EventLocation(){}

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public int getZip() {
        return zip;
    }

    public void setZip(int zip) {
        this.zip = zip;
    }

    public String getLocationID() {
        return locationID;
    }

}
