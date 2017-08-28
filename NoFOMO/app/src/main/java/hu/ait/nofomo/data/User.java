package hu.ait.nofomo.data;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by MacOwner on 5/21/17.
 */

public class User extends RealmObject {

    @PrimaryKey
    private String userID;

    private String name;
    private String email;
    private String propicURL;
    private RealmList<Event> previous_events;

    public User(String name, String email, String propicURL, RealmList<Event> events){
        this.name = name;
        this.email = email;
        this.propicURL = propicURL;
        this.previous_events = events;

    }

    public User(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserID() {
        return userID;
    }

    public String getPropicURL() {
        return propicURL;
    }

    public void setPropicURL(String propicURL) {
        this.propicURL = propicURL;
    }

    public RealmList<Event> getPrevious_events() {
        return previous_events;
    }

    public void setPrevious_events(RealmList<Event> previous_events) {
        this.previous_events = previous_events;
    }
}
