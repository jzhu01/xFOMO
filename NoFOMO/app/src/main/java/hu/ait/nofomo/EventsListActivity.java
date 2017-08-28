package hu.ait.nofomo;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hu.ait.nofomo.adapter.EventsAdapter;
import hu.ait.nofomo.data.Event;
import hu.ait.nofomo.data.Place;
import hu.ait.nofomo.touch.EventListTouchHelperCallback;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class EventsListActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private AccessToken accessToken;
    private EventsAdapter eventAdapter;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);

        final String userID = getIntent().getStringExtra("UserID");

        callbackManager = CallbackManager.Factory.create();

        accessToken = AccessToken.getCurrentAccessToken();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {

            }
        };

        accessTokenTracker.startTracking();

        RealmResults<Event> allEvents = getRealm().where(Event.class).findAll();
        Event eventsArray[] = new Event[allEvents.size()];
        List<Event> eventsResult = new ArrayList<Event>(Arrays.asList(allEvents.toArray(eventsArray)));

        eventAdapter = new EventsAdapter(eventsResult, this);
        RecyclerView recyclerViewPlaces = (RecyclerView) findViewById(
                R.id.recyclerViewEvents);
        recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPlaces.setAdapter(eventAdapter);

        EventListTouchHelperCallback touchHelperCallback = new EventListTouchHelperCallback(
                eventAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(
                touchHelperCallback);
        touchHelper.attachToRecyclerView(recyclerViewPlaces);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.accountNavigationView);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.action_about:
                                drawerLayout.closeDrawer(GravityCompat.START);
                                Toast.makeText(EventsListActivity.this, R.string.AboutMessage, Toast.LENGTH_SHORT).show();
                                menuItem.setChecked(false);
                                break;
                            case R.id.action_profile:
                                drawerLayout.closeDrawer(GravityCompat.START);
                                Intent goToUserDash = new Intent(EventsListActivity.this, UserDashActivity.class);
                                startActivity(goToUserDash);
                                menuItem.setChecked(false);
                                break;
                            case R.id.action_logout:
                                LoginManager.getInstance().logOut();
//                                logoutUser();
                                Toast.makeText(EventsListActivity.this, R.string.SuccessfulLoggingOut, Toast.LENGTH_SHORT).show();
                                menuItem.setChecked(false);
                                onBackPressed();
                                break;
                        }
                        return false;
                    }
                });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.com_facebook_button_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void deleteEvent(Event eventToDelete){
        getRealm().beginTransaction();
        eventToDelete.deleteFromRealm();
        getRealm().commitTransaction();
    }

//    public void logoutUser(){
//        if (accessToken.getCurrentAccessToken() == null){
//            return;
//        }
//        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
//                .Callback() {
//            @Override
//            public void onCompleted(GraphResponse graphResponse) {
//                LoginManager.getInstance().logOut();
//            }
//        }).executeAsync();
//    }

    public Realm getRealm() {
        return ((MainApplication)getApplication()).getRealmEvents();
    }

    @Override
    public void onDestroy() {
        ((MainApplication)getApplication()).closeRealm();
        accessTokenTracker.stopTracking();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
