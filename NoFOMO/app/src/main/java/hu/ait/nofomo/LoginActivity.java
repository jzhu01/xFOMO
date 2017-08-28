package hu.ait.nofomo;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.nofomo.data.Event;
import hu.ait.nofomo.data.EventLocation;
import hu.ait.nofomo.data.Place;
import hu.ait.nofomo.data.User;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.fb_login_button)
    Button fb_loginbtn;

    @BindView(R.id.mapViewBtn)
    Button mapViewBtn;

    @BindView(R.id.arrow)
    ImageView arrow;


    private CallbackManager callbackManager;
    private String name;
    private String email;
    private AccessToken accessToken;
    private AccessTokenTracker accessTokenTracker;
    private JSONObject user_events;
    private JSONArray userEventsArray;
    private ProfileTracker profileTracker;

    private User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        ((MainApplication)getApplication()).openRealm();

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(LoginActivity.this, "Login OK", Toast.LENGTH_SHORT).show();

                        accessToken = loginResult.getAccessToken();
                        accessTokenTracker = new AccessTokenTracker() {
                            @Override
                            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {

                            }
                        };
                        accessTokenTracker.startTracking();

                        profileTracker = new ProfileTracker() {
                            @Override
                            protected void onCurrentProfileChanged(Profile profile, Profile profile1) {

                            }
                        };
                        profileTracker.startTracking();


                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        Log.d("Response: ", response.toString());

                                        try {
                                            name = object.getString(getString(R.string.getUserName));

                                            email = object.getString(getString(R.string.getUserEmail));

                                            user_events = (JSONObject) object.get(getString(R.string.getUserPreviousEvents));

                                            getRealm().beginTransaction();
                                            currentUser = getRealm().createObject(User.class, UUID.randomUUID().toString());
                                            currentUser.setEmail(email);
                                            currentUser.setName(name);

                                            RealmList<Event> prvEvents = new RealmList<Event>();

                                            userEventsArray = user_events.getJSONArray("data");

                                            for (int i = 0; i < userEventsArray.length(); i++) {
                                                JSONObject eventJSON = userEventsArray.getJSONObject(i);
                                                JSONObject placeJSON = (JSONObject) eventJSON.get("place");
                                                Place place;
                                                if (placeJSON.has(getString(R.string.getEventLocation))){
                                                    JSONObject locJSON = (JSONObject) placeJSON.get(getString(R.string.getEventLocation));
                                                    String city = locJSON.getString(getString(R.string.getEventCity));
                                                    String country = locJSON.getString(getString(R.string.getEventCountry));
                                                    Double lat = locJSON.getDouble(getString(R.string.getEventLat));
                                                    Double lng = locJSON.getDouble(getString(R.string.getEventLng));

                                                    EventLocation loc = getRealm().createObject(EventLocation.class, UUID.randomUUID().toString());
                                                    loc.setCity(city);
                                                    loc.setCountry(country);
                                                    loc.setLat(lat);
                                                    loc.setLng(lng);

                                                    String placeName = placeJSON.getString(getString(R.string.getPlaceName));
                                                    String placeID = placeJSON.getString(getString(R.string.getPlaceID));
                                                    place = getRealm().createObject(Place.class, UUID.randomUUID().toString());
                                                    place.setName(placeName);
                                                    place.setId(placeID);
                                                    place.setLoc(loc);
                                                } else {
                                                    String placeName = placeJSON.getString(getString(R.string.getPlaceName));
                                                    String placeID = placeJSON.getString(getString(R.string.getPlaceID));
                                                    place = getRealm().createObject(Place.class, UUID.randomUUID().toString());
                                                    place.setName(placeName);
                                                    place.setId(placeID);
                                                }

                                                Event event = getRealm().createObject(Event.class, UUID.randomUUID().toString());
                                                event.setDesc(eventJSON.getString(getString(R.string.getDescription)));
                                                if (eventJSON.has(getString(R.string.getEndTime))) {
                                                    event.setEndTime(eventJSON.getString(getString(R.string.getEndTime)));
                                                }
                                                event.setName(eventJSON.getString(getString(R.string.getEventName)));
                                                event.setPlace(place);
                                                event.setStartTime(eventJSON.getString(getString(R.string.getStartTime)));
                                                event.setEventID(eventJSON.getString(getString(R.string.getEventID)));

                                                prvEvents.add(event);
                                            }

                                            currentUser.setPrevious_events(prvEvents);
                                            getRealm().commitTransaction();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        //me?fields=events
                        parameters.putString("fields", "id,name,email,events");
                        request.setParameters(parameters);
                        request.executeAsync();

                        Intent startMapsActivity = new Intent(LoginActivity.this, MapsActivity.class);
                        startActivity(startMapsActivity);
                    }

                    @Override
                    public void onCancel() {
                        LoginManager.getInstance().logOut();
                        Toast.makeText(LoginActivity.this, R.string.CancelLogin, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(LoginActivity.this, getString(R.string.FailedLogin)+exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        if (AccessToken.getCurrentAccessToken() == null){
            arrow.setVisibility(View.INVISIBLE);
            mapViewBtn.setVisibility(View.VISIBLE);
        } else {
            arrow.setVisibility(View.VISIBLE);
            mapViewBtn.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.fb_login_button)
    public void fbLoginClick(){
        if(AccessToken.getCurrentAccessToken() == null){
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends", "user_events"));
            arrow.setVisibility(View.INVISIBLE);
            mapViewBtn.setVisibility(View.VISIBLE);
            return;
        }
        // this code will register a response in the button, but raises a realm error
//        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.GET, new GraphRequest
//                .Callback() {
//
//            @Override
//            public void onCompleted(GraphResponse graphResponse) {
//
//                LoginManager.getInstance().logOut();
//                arrow.setVisibility(View.VISIBLE);
//                mapViewBtn.setVisibility(View.INVISIBLE);
//            }
//        }).executeAsync();

        LoginManager.getInstance().logOut();
        arrow.setVisibility(View.VISIBLE);
        mapViewBtn.setVisibility(View.INVISIBLE);


    }

    @OnClick(R.id.mapViewBtn)
    public void mapViewBtnClick(){
        Intent startMapsActivity = new Intent(LoginActivity.this, MapsActivity.class);
        startActivity(startMapsActivity);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public Realm getRealm() {
        return ((MainApplication)getApplication()).getRealmEvents();
    }

    @Override
    protected void onDestroy() {
        ((MainApplication)getApplication()).closeRealm();
        accessTokenTracker.stopTracking();
        super.onDestroy();
    }

}

