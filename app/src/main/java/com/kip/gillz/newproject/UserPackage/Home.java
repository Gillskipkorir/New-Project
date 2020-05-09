package com.kip.gillz.newproject.UserPackage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kip.gillz.newproject.GPSTracker;
import com.kip.gillz.newproject.R;
import com.kip.gillz.newproject.WelcomeActivity;
import com.kip.gillz.newproject.utils.Tools;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Home extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private GoogleMap mMap;
    LatLng latLng;
    ImageButton button;
    private BottomSheetBehavior bottomSheetBehavior;
    String state,country,subLocality,county,locality;
    Double xxx,yyy;
    // GPSTracker class
    GPSTracker gps;
    private  TextView Logout;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        // create class object
        gps = new GPSTracker(Home.this);
        if(gps.canGetLocation()){
             xxx = gps.getLatitude();
             yyy = gps.getLongitude();
            Toast.makeText(getApplicationContext(), "Lat: "
                    + xxx + "\nLong: " + yyy, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(), "Please Turn on you Gps", Toast.LENGTH_LONG).show();
            gps.showSettingsAlert();
        }

        initComponent();

        button= findViewById(R.id.map_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Home.this,button);
                popupMenu.getMenuInflater().inflate(R.menu.main,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        if (id == R.id.Normal)
                        {
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            return true;
                        }
                        else if (id==R.id.Satelite)
                        {
                            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            return true;
                        }

                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        Logout= (TextView) findViewById(R.id.logout);


        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mAuth.signOut();

                LogOutUser();
            }
        });
    }

    public void LogOutUser()
    {
        Intent startPageIntent = new Intent(Home.this, WelcomeActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Showing Current Location Marker on Map
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location locations = locationManager.getLastKnownLocation(provider);
        List<String> providerList = locationManager.getAllProviders();
        if (null != locations && null != providerList && providerList.size() > 0) {
            double longitude = locations.getLongitude();
            double latitude = locations.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(),
                    Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude,
                        longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    state = listAddresses.get(0).getAdminArea();
                    country = listAddresses.get(0).getCountryName();
                    subLocality = listAddresses.get(0).getSubLocality();
                    // markerOptions.title("My Location");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        //mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        // new marker
        mMap = Tools.configActivityMaps(googleMap);
        final MarkerOptions markerOptions;
        markerOptions = new MarkerOptions().position(new LatLng(xxx, yyy));
        markerOptions.title("My Location");

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                try
                {
                    mMap.animateCamera(zoomingLocation());
                }
                catch (Exception e) {
                }
                return true;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    private CameraUpdate zoomingLocation() {
        return CameraUpdateFactory.newLatLngZoom(new LatLng(xxx, yyy), 15);
    }

    //Buttons on the bottom page
    public void clickAction(View view) {
        int id = view.getId();
        switch (id) {

            case R.id.list_button:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                try {
                    mMap.animateCamera(zoomingLocation());
                } catch (Exception e) {
                }

                //Toast.makeText(getApplicationContext(), "List Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.add_button:
                Toast.makeText(getApplicationContext(), "This is my Location", Toast.LENGTH_SHORT).show();
                //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                // mCurrLocationMarker = mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

                break;
        }
    }
    private void initComponent() {
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location locations = locationManager.getLastKnownLocation(provider);
        List<String> providerList = locationManager.getAllProviders();
        if (null != locations && null != providerList && providerList.size() > 0) {
            double latitude = xxx;
            double longitude = yyy;

            Geocoder geocoder = new Geocoder(getApplicationContext(),
                    Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude,
                        longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    state = listAddresses.get(0).getAdminArea();
                    country = listAddresses.get(0).getCountryName();
                    subLocality = listAddresses.get(0).getSubLocality();
                    county = listAddresses.get(0).getSubAdminArea();
                    locality = listAddresses.get(0).getLocality();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // get the bottom sheet view
        LinearLayout llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        // change the state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        //initializing
        TextView textViewmycar,textView,textView1,textView2,textView3,textView4,textView5;
        //casting TextViews
        textViewmycar= findViewById(R.id.mycar);
        textView= findViewById(R.id.countryyy);
        textView1= findViewById(R.id.countyy);
        textView2= findViewById(R.id.latt);
        textView3= findViewById(R.id.longg);
        textView4= findViewById(R.id.datetimee);

        //Setting Values on the Bottom sheet textviews
        //textViewmycar.setText(carreg);
        textView.setText(country+".");
        textView1.setText(state+", "+ locality+".");
        textView2.setText(String.valueOf(xxx));
        textView3.setText(String.valueOf(yyy));

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


    }
    @Override
    public void onBackPressed() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        doExitApp();
    }
    private long exitTime = 0;
    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "Press again to exit app", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }



}

