package hu.ait.nofomo;

import android.support.design.widget.NavigationView;
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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.ait.nofomo.adapter.EventsAdapter;
import hu.ait.nofomo.data.Event;
import hu.ait.nofomo.data.User;
import hu.ait.nofomo.touch.EventListTouchHelperCallback;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class UserDashActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private DrawerLayout drawerLayout;

    @BindView(R.id.tvUserName)
    TextView tvUserName;

    @BindView(R.id.tvEmail)
    TextView tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dash);

        callbackManager = CallbackManager.Factory.create();

        accessToken = AccessToken.getCurrentAccessToken();

        ButterKnife.bind(this);

        String userID = getIntent().getStringExtra("UserID");

        final User[] currentUser = new User[1];

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            String email = object.getString("email");
                            currentUser[0] = getRealm().where(User.class).equalTo("email", email).findFirst();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        //me?fields=events
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();


        tvUserName.setText(currentUser[0].getName());
        tvEmail.setText(currentUser[0].getEmail());

        RealmList<Event> allEvents = currentUser[0].getPrevious_events();
        Event eventsArray[] = new Event[allEvents.size()];
        List<Event> eventsResult = new ArrayList<Event>(Arrays.asList(allEvents.toArray(eventsArray)));

        EventsAdapter eventAdapter = new EventsAdapter(eventsResult, this);
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
                                Toast.makeText(UserDashActivity.this, R.string.AboutMessage, Toast.LENGTH_SHORT).show();
                                menuItem.setChecked(false);
                                break;
                            case R.id.action_profile:
                                drawerLayout.closeDrawer(GravityCompat.START);
                                Toast.makeText(UserDashActivity.this, R.string.DashboardIntent, Toast.LENGTH_SHORT).show();
                                menuItem.setChecked(false);
                                break;
                            case R.id.action_logout:
                                LoginManager.getInstance().logOut();
                                Toast.makeText(UserDashActivity.this, R.string.SuccessfulLoggingOut, Toast.LENGTH_SHORT).show();
                                menuItem.setChecked(false);
                                break;
                        }

                        return false;
                    }
                });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.com_facebook_profile_picture_blank_portrait);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

    public Realm getRealm() {
        return ((MainApplication)getApplication()).getRealmEvents();
    }

    @Override
    public void onDestroy() {
        ((MainApplication)getApplication()).closeRealm();
        super.onDestroy();
    }
}
