package com.example.joinriding.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.joinriding.MainActivity;
import com.example.joinriding.NotificationActivity;
import com.example.joinriding.R;
import com.example.joinriding.SettingsActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.actionitembadge.library.utils.BadgeStyle;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.tapadoo.alerter.Alerter;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.maps.Style.MAPBOX_STREETS;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, PermissionsListener,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,  NavigationEventListener, NavigationListener, ProgressChangeListener {

    Handler mHandler ;
    // Declare Info Get Available Driver User
    private LinearLayout dlayoutDriverInfo, ddriverInfo, dLayoutButtonRequest;
    private ImageView ddriverProfileImage;
    private TextView ddriverName, ddriverPhone, ddriverAddress;
    private Button btnRequestCycle;

    // Declare Info Get Help User Info
    private LinearLayout ulayoutUserInfo, uuserInfo, uLayoutButtonHelp;
    private ImageView uuserProfileImage;
    private TextView uuserName, uuserPhone, uuserAddress;
    private Button btnHelpCycle, btnStartNavigation;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsLocationManager;
    private Location myLocation;
    private static final String TAG = "MapsFragment";
    private FirebaseUser driveruser = FirebaseAuth.getInstance().getCurrentUser();

    // Firebase Auth
    private FirebaseAuth mAuth;

    // Button Switch
    private Switch mHelpingSwitch, mPositionSwitch;

    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private Marker marker;
    private int radius = 1;
    private Boolean driverFound = false;

    private String driverFoundID;
    private String driverId = "";

    private String customerID = "";

    private NavigationMapRoute navigationMapRoute;
    private DirectionsRoute currentRoute;
    private boolean state = true;

    private LatLng origincord;

    private GeoQuery geoQuery;

    // badge
    BadgeStyle style;
    private int badgeCount = 10;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Mapbox.getInstance(getContext(), "pk.eyJ1IjoibWFoZnVkcHJhc2V0eW8iLCJhIjoiY2tiYXoyMzByMG1wcTJycGdtcms2dGN2NiJ9.KKCrYckZ-y-Uo2FQx6L6vg");

        View view = view = inflater.inflate(R.layout.fragment_maps, container, false);

        customerID = driveruser.getUid();

        // init driver info
        dlayoutDriverInfo = view.findViewById(R.id.layoutDriverInfo);
        ddriverInfo = view.findViewById(R.id.driverInfo);
        ddriverProfileImage = view.findViewById(R.id.driverProfileImage);
        ddriverName = view.findViewById(R.id.driverName);
        ddriverPhone = view.findViewById(R.id.driverPhone);
        ddriverAddress = view.findViewById(R.id.driverAddress);
        dLayoutButtonRequest = view.findViewById(R.id.layoutButtonRequest);
        btnRequestCycle = view.findViewById(R.id.requestCycle);

        // init user info
        ulayoutUserInfo = view.findViewById(R.id.layoutUserInfo);
        uuserInfo = view.findViewById(R.id.userInfo);
        uuserProfileImage = view.findViewById(R.id.userProfileImage);
        uuserName = view.findViewById(R.id.userName);
        uuserPhone = view.findViewById(R.id.userPhone);
        uuserAddress = view.findViewById(R.id.userAddress);
        uLayoutButtonHelp= view.findViewById(R.id.layoutButtonHelp);
        btnHelpCycle = view.findViewById(R.id.helpCycle);

        // init badge
        style = ActionItemBadge.BadgeStyles.RED.getStyle();

        mapView = view.findViewById(R.id.mapViewUser);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        MapsFragment.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(MAPBOX_STREETS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });

        btnHelpCycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAssignedCustomer();
            }
        });

        btnRequestCycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapsFragment.this.mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                        .title("Pickup Here").setIcon(IconFactory.getInstance(getContext())
                                .fromResource(R.drawable.icon_marker_location)));
                getClosestDriver();
            }
        });

        mPositionSwitch = getView().findViewById(R.id.positionSwitch);
        mPositionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    connectPositionUser();
                    mHelpingSwitch.setVisibility(View.GONE);
                }else{
                    disconnectPositionUser();
                    mHelpingSwitch.setVisibility(View.VISIBLE);
                }
            }
        });

        mHelpingSwitch = getView().findViewById(R.id.helpingSwitch);
        mHelpingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    connectHelpingUser();
                    mPositionSwitch.setVisibility(View.GONE);
                }else{
                    disconnectHelpingUser();
                    mPositionSwitch.setVisibility(View.VISIBLE);
                }
            }
        });

        btnStartNavigation = getView().findViewById(R.id.navCycle);
        btnStartNavigation.setVisibility(View.GONE);
        btnStartNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(true)
                        .build();
                NavigationLauncher.startNavigation(getActivity(), options);
            }
        });
    }

    private void connectHelpingUser() {
        Toast.makeText(getApplicationContext(),"Connect Help", Toast.LENGTH_LONG).show();

        dlayoutDriverInfo.setVisibility(View.VISIBLE);
        ddriverInfo.setVisibility(View.GONE);
        dLayoutButtonRequest.setVisibility(View.VISIBLE);
        buildGoogleApiClient();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location1) {
                        if (location1 != null) {
                            myLocation = location1;
                            origincord = new LatLng(location1.getLatitude(), location1.getLongitude());
                            setCameraPosition(location1);
                            try {
                                String userID = driveruser.getUid();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("positionUser");
                                GeoFire geoFire = new GeoFire(ref);
                                geoFire.setLocation(userID, new GeoLocation(location1.getLatitude(), location1.getLongitude()));
                            } catch (Exception te) {
                                getActivity().finish();
                            }
                        }
                    }
                });
    }

    private void disconnectHelpingUser() {
        Toast.makeText(getApplicationContext(),"Disconnect Help", Toast.LENGTH_LONG).show();

        dlayoutDriverInfo.setVisibility(View.GONE);
        ddriverInfo.setVisibility(View.GONE);
        dLayoutButtonRequest.setVisibility(View.GONE);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("positionUser");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerID);
        customerID = "";
    }

    private void connectPositionUser() {
        Toast.makeText(getApplicationContext(),"Connect Position", Toast.LENGTH_LONG).show();

        ulayoutUserInfo.setVisibility(View.VISIBLE);
        uuserInfo.setVisibility(View.GONE);
        uLayoutButtonHelp.setVisibility(View.VISIBLE);

        buildGoogleApiClient();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location1) {
                        if (location1 != null) {
                            // Logic to handle location object
                            myLocation = location1;
                            setCameraPosition(location1);
                            String userId;
                            try {
                                userId = driveruser.getUid();
                                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("availableUser");
                                GeoFire geoFireAvailable = new GeoFire(refAvailable);

                                switch (customerID) {
                                    default:
                                        geoFireAvailable.setLocation(userId, new GeoLocation(location1.getLatitude(), location1.getLongitude()));
                                        break;
                                }
                                driverId = userId;
                            } catch (Exception t) {
                                getActivity().finish();
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), "Location Not Found.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void disconnectPositionUser() {
        Toast.makeText(getApplicationContext(),"Disconnect Position", Toast.LENGTH_LONG).show();

        ulayoutUserInfo.setVisibility(View.GONE);
        uuserInfo.setVisibility(View.GONE);
        uLayoutButtonHelp.setVisibility(View.GONE);
        btnStartNavigation.setVisibility(View.GONE);

        DatabaseReference refA = FirebaseDatabase.getInstance().getReference("availableUser");

        GeoFire geoFireA = new GeoFire(refA);
        geoFireA.removeLocation(driverId);

        customerID = "";
        driverId = "";
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); //high Accuracy decrease if needed
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(state) {
            if (driverFoundID != null) {
                getDriverLocation();
            }
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getContext(), "My Location", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(getContext(), "Not Allow Permission", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getClosestDriver(){

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("availableUser");
        GeoFire geoFire = new GeoFire(driverLocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), radius);
        geoQuery.removeAllListeners();  //Removes all data stored

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(driverFound = true){
//                    driverFound = true;
                    driverFoundID = key;
                    Log.d("Key : ", driverFoundID);

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                            .child("TBL_USERS").child(driverFoundID).child("helpingRequest");

                    HashMap<String, Object> dataMap = new HashMap<>();
                    dataMap.put("CustomerRideID", customerID);
                    dataMap.put("destinationLat", myLocation.getLatitude());
                    dataMap.put("destinationLng", myLocation.getLongitude());
                    driverRef.updateChildren(dataMap);
                    Toast.makeText(getApplicationContext(),"Driver Found, Wait.", Toast.LENGTH_LONG).show();

                    getDriverInfo();
                    getDriverLocation();
                }
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound && radius < 100){
                    radius++;
                    getClosestDriver();
                    Log.d("Radius : ", String.valueOf(radius));
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    private void getDriverInfo(){
        ddriverInfo.setVisibility(View.VISIBLE);

        DatabaseReference mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("TBL_USERS").child(driverFoundID);
        mDriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // checked until required data get
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    String photo = ""+snapshot.child("photoUser").getValue();
                    String name = ""+snapshot.child("nameUser").getValue();
                    String phone = ""+snapshot.child("phoneUser").getValue();
                    String address= ""+snapshot.child("addressUser").getValue();

                    ddriverName.setText(name);
                    ddriverPhone.setText(phone);
                    ddriverAddress.setText(address);

                    // if image equal no image
                    if (!photo.equals("noImage")){
                        try {
                            // if image success set
                            Glide.with(getContext())
                                    .load(photo)
                                    .error(R.drawable.icon_user_grey)
                                    .into(ddriverProfileImage);
                        }
                        catch (Exception e){}
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getDriverLocation(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("availableUser");
        GeoFire geoFire = new GeoFire(ref);

        geoFire.getLocation(driverFoundID, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    double lat = location.latitude;
                    double lng = location.longitude;
                    LatLng driverLatLng = new LatLng(lat, lng);
                    if (marker != null) {
                        mapboxMap.removeMarker(marker);
                    }
                    marker = mapboxMap.addMarker(new MarkerOptions().position(driverLatLng)
                            .title("Your Driver").setIcon(IconFactory.getInstance(getContext())
                                    .fromResource(R.drawable.icon_marker_cycle)));

                    getRouteDriver(Point.fromLngLat(myLocation.getLongitude(), myLocation.getLatitude()),
                            Point.fromLngLat(driverLatLng.getLongitude(), driverLatLng.getLatitude()));
//                    animate(driverLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(),"There was an error getting the GeoFire location: "
                        + databaseError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getAssignedCustomer(){
//        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference()
                .child("TBL_USERS").child(driverId).child("helpingRequest").getRef();

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Toast.makeText(getContext(),"User Helping Found.", Toast.LENGTH_LONG).show();
                    customerID = dataSnapshot.getKey();
                    Map<String, Object> dataMap = (Map<String, Object>) dataSnapshot.getValue();
                    double locationLat = 25.625818;
                    double locationLng = 85.106596;

                    assert dataMap != null;
                    if (dataMap.get("destinationLat") != null && dataMap.get("destinationLng") != null) {
                        customerID = Objects.requireNonNull(dataMap.get("CustomerRideID")).toString();
                        locationLat = Double.parseDouble(Objects.requireNonNull(dataMap.get("destinationLat")).toString());
                        locationLng = Double.parseDouble(Objects.requireNonNull(dataMap.get("destinationLng")).toString());
//                        Toast.makeText(getApplicationContext(),"Destination to Customer Found, Directing you there.", Toast.LENGTH_LONG).show();
                        Alerter.create(getActivity())
                                .setTitle("Customer Found ")
                                .setText("Directing You there.")
                                .enableProgress(true)
                                .enableSwipeToDismiss()
                                .setBackgroundColorInt(R.color.mapbox_blue)
                                .setIcon(R.drawable.alert_progress_drawable)
                                .setIconColorFilter(0)
                                .show();

                        shareDriverLocation(customerID);
                        getAssignedCustomerInfo();
                    }else{
//                        Toast.makeText(getApplicationContext(),"Destination to Customer Not Found.", Toast.LENGTH_LONG).show();
                        Alerter.create(getActivity())
                                .setTitle("Customer Not Found")
                                .setText("No Customer are looking for driver.")
                                .enableProgress(false)
                                .enableSwipeToDismiss()
                                .setBackgroundColorInt(R.color.mapbox_blue)
                                .setIcon(R.drawable.alert_progress_drawable)
                                .setIconColorFilter(0)
                                .show();
                    }

                    LatLng pickUpLocation = new LatLng(locationLat, locationLng);
                    mapboxMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pickup Location")
                            .setIcon(IconFactory.getInstance(getContext()).fromResource(R.drawable.icon_marker_location)));

                    getRouteCustomer(Point.fromLngLat(myLocation.getLongitude(), myLocation.getLatitude()),
                            Point.fromLngLat(pickUpLocation.getLongitude(), pickUpLocation.getLatitude()));
                    Toast.makeText(getApplicationContext(), "Direction Found, Starting Navigation", Toast.LENGTH_LONG).show();

                    btnStartNavigation.setEnabled(true);
                    btnStartNavigation.setVisibility(View.VISIBLE);
                    btnHelpCycle.setText("PROGRESS...");
                }else{
                    Toast.makeText(getContext(), "Customer Not Found, Try Again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"Database Error.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getAssignedCustomerInfo() {
        ulayoutUserInfo.setVisibility(View.VISIBLE);
        uuserInfo.setVisibility(View.VISIBLE);
        uLayoutButtonHelp.setVisibility(View.VISIBLE);

//        customerID = "" ;
        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("TBL_USERS").child(customerID);
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // checked until required data get
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    String photo = ""+ snapshot.child("photoUser").getValue();
                    String name = ""+ snapshot.child("nameUser").getValue();
                    String phone = ""+ snapshot.child("phoneUser").getValue();
                    String address= ""+ snapshot.child("addressUser").getValue();


                    uuserName.setText(name);
                    uuserPhone.setText(phone);
                    uuserAddress.setText(address);

                    // if image equal no image
                    if (!photo.equals("noImage")){
                        try {
                            // if image success set
                            Glide.with(getContext())
                                    .load(photo)
                                    .error(R.drawable.icon_user_grey)
                                    .into(uuserProfileImage);
                        }
                        catch (Exception e){}
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void shareDriverLocation(String customerID){
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("TBL_USERS").child(customerID).child("drivingRequest");
        String driverID = driveruser.getUid();
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("driverRideID", driverID);
        dataMap.put("destinationLat", myLocation.getLatitude());
        dataMap.put("destinationLng", myLocation.getLongitude());
        driverRef.updateChildren(dataMap);
    }

    private void getRouteDriver(Point origin, Point destination) {
        assert Mapbox.getAccessToken() != null;
        NavigationRoute.builder(getContext())
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
//                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        //noinspection ConstantConditions
                        String text = "Distance: " + Math.round(((currentRoute.distance())/1000.0) * 100.0)/100.0
                                + " Kilometre Duration: " + Math.round(((currentRoute.duration())/60.0)*100.0)/100.0 + " Minutes";

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable t) {
                        Log.e(TAG, "Error: " + t.getMessage());
                    }
                });
    }

    private void getRouteCustomer(Point origin, Point destination){
        assert Mapbox.getAccessToken() != null;
        NavigationRoute.builder(getContext())
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull retrofit2.Response<DirectionsResponse> response) {
                        if(response.body() == null) {
                            Log.e(TAG, "No Routes found, chek right user and access token");
                            return;
                        }else if(response.body().routes().size() == 0){
                            Log.e(TAG, "No Routes Found");
                            return;
                        }
                        currentRoute = response.body().routes().get(0);
                        if(navigationMapRoute != null){
                            navigationMapRoute.removeRoute();
                        }else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable t) {
                        Log.e(TAG, "Error:" + t.getMessage());
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            // Set the LocationComponent activation options

            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(getContext(), loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            myLocation = locationComponent.getLastKnownLocation();
        } else {
            permissionsLocationManager = new PermissionsManager(this);
            permissionsLocationManager.requestLocationPermissions(getActivity());
        }
    }

    private void setCameraPosition(Location location) {
        if(mapboxMap.getMarkers().size() > 1) {
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),
                    location.getLongitude())));
            cameraZoom();
        }
        else {
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                    location.getLongitude()), 13.0));
        }
    }

    private void cameraZoom(){
        List<Marker> markers = mapboxMap.getMarkers();
        final int width = 256;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker m : markers) {
            builder.include(m.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = ((width * 50) / 100); // offset from edges of the map
        // in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                padding);
        mapboxMap.animateCamera(cu);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void clearDatabaseCustomer() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("positionUser");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerID);
        customerID = "";
    }

    private void clearDatabaseDriver() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("availableUser");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(driverId);
        customerID = "";
        driverId = "";
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearDatabaseDriver();
        clearDatabaseCustomer();

        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){

        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu action in fragment
        super.onCreate(savedInstanceState);
    }

    // Inflate Option Menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflated Menu
        inflater.inflate(R.menu.top_nav_menu, menu);

        // Hide Some Menu
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_user).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        if (badgeCount > 0) {
            ActionItemBadge.update(getActivity(), menu.findItem(R.id.action_notification), FontAwesome.Icon.faw_android, ActionItemBadge.BadgeStyles.RED, badgeCount);
        } else {
            ActionItemBadge.hide(menu.findItem(R.id.action_notification));
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    // handle menu item click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }else if(id == R.id.action_settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }else if(id == R.id.action_notification){
            ActionItemBadge.update(item, badgeCount);
            startActivity(new Intent(getActivity(), NotificationActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    //********************************************************************************************//
    @Override
    public void onCancelNavigation() {

    }

    @Override
    public void onNavigationFinished() {
        Toast.makeText(getApplicationContext(),"Navigasi Telah Selesai Dilakukan", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNavigationRunning() {

    }

    @Override
    public void onRunning(boolean running) {

    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {

    }
    //********************************************************************************************//
}
