package hu.ait.nofomo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.nofomo.data.Event;
import hu.ait.nofomo.data.EventLocation;
import hu.ait.nofomo.data.Place;
import io.realm.Realm;
import io.realm.RealmList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private CallbackManager callbackManager;
    private GoogleMap mMap;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private String cityName;
    private AccessToken accessToken;

    @BindView(R.id.btnToggleListView)
    FloatingActionButton btnToggleListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ButterKnife.bind(this);

        callbackManager = CallbackManager.Factory.create();

        accessToken = AccessToken.getCurrentAccessToken();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setTrafficEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria mCriteria = new Criteria();
        String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));

        Location mLocation = manager.getLastKnownLocation(bestProvider);

        checkLocationPermission();
        mMap.setMyLocationEnabled(true);

        final double currentLatitude = mLocation.getLatitude();
        final double currentLongitude = mLocation.getLongitude();
        LatLng loc1 = new LatLng(currentLatitude, currentLongitude);
        mMap.addMarker(new MarkerOptions().position(loc1).title("Your Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 15));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cityName = addresses.get(0).getAddressLine(0);

        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/search",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject eventsInCityObject = response.getJSONObject();
                        RealmList<Event> cityEvents = new RealmList<Event>();


                        try {
                            JSONArray eventsInCityArray =  eventsInCityObject.getJSONArray("data");
                            for (int i = 0; i < eventsInCityArray.length(); i++) {
                                JSONObject eventJSON = eventsInCityArray.getJSONObject(i);
                                JSONObject placeJSON = (JSONObject) eventJSON.get("place");

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
                                    Place place = getRealm().createObject(Place.class, UUID.randomUUID().toString());
                                    place.setName(placeName);
                                    place.setId(placeID);
                                    place.setLoc(loc);

                                    Event event = getRealm().createObject(Event.class, UUID.randomUUID().toString());
                                    event.setDesc(eventJSON.getString(getString(R.string.getDescription)));
                                    event.setEndTime(eventJSON.getString(getString(R.string.getEndTime)));
                                    event.setName(eventJSON.getString(getString(R.string.getEventName)));
                                    event.setPlace(place);
                                    event.setStartTime(eventJSON.getString(getString(R.string.getStartTime)));
                                    event.setEventID(eventJSON.getString(getString(R.string.getEventID)));

                                    cityEvents.add(event);

                                    mMap.addMarker(new MarkerOptions().position(new LatLng(event.getPlace().getLoc().getLat(),
                                            event.getPlace().getLoc().getLng())).title(event.getName()));
                                } else {
                                    String placeName = placeJSON.getString(getString(R.string.getPlaceName));
                                    Place place = getRealm().createObject(Place.class, UUID.randomUUID().toString());
                                    place.setName(placeName);


                                    Event event = getRealm().createObject(Event.class, UUID.randomUUID().toString());
                                    event.setDesc(eventJSON.getString(getString(R.string.getDescription)));
                                    event.setEndTime(eventJSON.getString(getString(R.string.getEndTime)));
                                    event.setName(eventJSON.getString(getString(R.string.getEventName)));
                                    event.setPlace(place);
                                    event.setStartTime(eventJSON.getString(getString(R.string.getStartTime)));
                                    event.setEventID(eventJSON.getString(getString(R.string.getEventID)));
                                }
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString(getString(R.string.query), cityName);
        parameters.putString(getString(R.string.type), getString(R.string.event));
        request.setParameters(parameters);
        request.executeAsync();


    }


    @OnClick(R.id.btnToggleListView)
    public void generalListener(){
        Intent goToListView = new Intent(MapsActivity.this, EventsListActivity.class);
        startActivity(goToListView);
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                            mMap.setMyLocationEnabled(true);
                        //Request location updates:
                        //locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, R.string.PermissionRejectedMessage, Toast.LENGTH_SHORT).show();

                }
                return;
            }

        }
    }

    public Realm getRealm() {
        return ((MainApplication)getApplication()).getRealmEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
