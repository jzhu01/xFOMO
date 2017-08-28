package hu.ait.nofomo;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainApplication  extends Application {

    private Realm realmEvents;

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
    }

    public void openRealm() {
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realmEvents = Realm.getInstance(config);
    }

    public void closeRealm() {
        realmEvents.close();
    }

    public Realm getRealmEvents() {
        return realmEvents;
    }
}
