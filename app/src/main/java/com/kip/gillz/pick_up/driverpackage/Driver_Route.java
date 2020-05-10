package com.kip.gillz.pick_up.driverpackage;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.kip.gillz.pick_up.GPSTracker;
import com.kip.gillz.pick_up.R;
import com.kip.gillz.pick_up.WelcomeActivity;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Driver_Route extends AppCompatActivity implements OnMapReadyCallback,
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
    ImageButton button ,btnroute;
    private BottomSheetBehavior bottomSheetBehavior;
    String state,country,subLocality,county,locality;
    Double xxx,yyy;

   private  TextView Logout;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference CustomerDatabaseRef;
    private String driverID;
    // GPSTracker class
    GPSTracker gps;
    private LatLng mOrigin;
    private LatLng mDestination;
    private LatLng mInterpoint;
    private Polyline mPolyline;
    ArrayList<LatLng> mMarkerPoints;
    int n;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver__route);
       // FirebaseApp.initializeApp(this);
//        mAuth = FirebaseAuth.getInstance();
//        currentUser = mAuth.getCurrentUser();
//        driverID = mAuth.getCurrentUser().getUid();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMarkerPoints = new ArrayList<>();
        // create class object
        gps = new GPSTracker(Driver_Route.this);
        if(gps.canGetLocation()){
            xxx = gps.getLatitude();
            yyy = gps.getLongitude();
        }else{
            Toast.makeText(getApplicationContext(), "Please Turn on you Gps", Toast.LENGTH_LONG).show();
            gps.showSettingsAlert();
        }
        initComponent();
        button= findViewById(R.id.map_button);
        btnroute = findViewById(R.id.list_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Driver_Route.this,button);
                popupMenu.getMenuInflater().inflate(R.menu.main,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.on_off)
                        {
                            return true;
                        }
                        else if (id==R.id.navstart)
                        {
                            return true;
                        }
                        else if (id==R.id.navstop)
                        {
                            return true;
                        }
                        return true;


                    }
                });
                popupMenu.show();
            }
        });

        btnroute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Driver_Route.this,btnroute);
                popupMenu.getMenuInflater().inflate(R.menu.route,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.addnew)
                        {
                            mMarkerPoints.clear();
                            mMap.clear();
                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng point) {
                                    // Already two locations
                                    if(mMarkerPoints.size()==3){

                                        Toast.makeText(getApplicationContext(), "Only One Way Point Needed ", Toast.LENGTH_LONG).show();

                                    }

                                    else {
                                        // Adding new item to the ArrayList
                                        mMarkerPoints.add(point);
                                        // Creating MarkerOptions
                                        MarkerOptions options = new MarkerOptions();

                                        // Setting the position of the marker
                                        options.position(point);

                                        if (mMarkerPoints.size() == 1) {
                                            options.title("Start Point");
                                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                        } else if (mMarkerPoints.size() == 2) {
                                            Toast.makeText(getApplicationContext(), "Add One Way Point", Toast.LENGTH_LONG).show();
                                            options.title("End Point");
                                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                        } else
                                            {
                                            options.title("Way Point");
                                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                        }

                                        // Add new marker to the Google Map Android API V2
                                        mMap.addMarker(options);

                    if (mMarkerPoints.size() == 3) {
                        mOrigin = mMarkerPoints.get(0);
                        mDestination = mMarkerPoints.get(1);
                        drawRoute();
                        new LovelyStandardDialog(Driver_Route.this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                                .setTopColorRes(R.color.colorPrimary)
                                .setButtonsColorRes(R.color.cyan_500)
                                .setIcon(R.drawable.ic_airport_shuttle_black_24dp)
                                .setTitle("Save this Route").setTitleGravity(Gravity.CENTER)
                                .setMessage("Start Point:\n"+mOrigin+
                                        "\n\n Way Point:\n"+mMarkerPoints.get(1)+
                                        "\n\nEnd Point:\n"+mDestination+"\n")
                                .setMessageGravity(Gravity.CENTER)
                                .setCancelable(false)
                                .setPositiveButton("Save", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        //TODO: SAVE DATA TO DB
                                        Toast.makeText(getApplicationContext(), " Saved", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .setNegativeButton("Cancel", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                    Toast.makeText(getApplicationContext(), "Not Saved", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .show();
                    }

                                    }
                                }
                            });

                            return true;
                        }
                        else if (id==R.id.viewroute)
                        {
                            return true;
                        }

                        return true;


                    }
                });
                popupMenu.show();
            }
        });
//        Logout=(TextView) findViewById(R.id.logout);
//
//        Logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAuth.signOut();
//
//                LogOutUser();
//            }
//        });
    }
//    public void LogOutUser()
//    {
//        Intent startPageIntent = new Intent(Driver_Route.this, WelcomeActivity.class);
//        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(startPageIntent);
//        finish();
//    }
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

    private void drawRoute(){

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(mOrigin,mDestination);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }


    private String getDirectionsUrl(LatLng origin , LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;


        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Waypoints
        LatLng point  = mMarkerPoints.get(2);
        String  waypoint = "waypoints="+point.latitude + "," + point.longitude;

/*
        // Waypoints
        String waypoints = "";
        for(int i=n ;i<mMarkerPoints.size();i++){
            LatLng point  =  mMarkerPoints.get(i);

                waypoints += point.latitude + "," + point.longitude + "|";
                waypoints = "waypoints="+waypoints;

        }*/
        // Key
        String key = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+key+"&"+waypoint;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception on download", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /** A class to download data from Google Directions URL */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask","DownloadTask : " + data);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Directions in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size();i++)
            {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                if(mPolyline != null){
                    mPolyline.remove();
                }
                mPolyline = mMap.addPolyline(lineOptions);

            }else
                Toast.makeText(getApplicationContext(),"No route is found", Toast.LENGTH_LONG).show();
        }
    }
    public void DroppointsDialog() {
        final Dialog dialog = new Dialog(Driver_Route.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.addroute);
        dialog.setCancelable(true);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
       final EditText editTextroute=dialog.findViewById(R.id.rrrr);
        (dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 n= Integer.parseInt(editTextroute.getText().toString().trim());
                String s= String.valueOf(n);
                if (TextUtils.isEmpty(s)) {
                    editTextroute.setError("Required");
                    return;
                }
                else

                {
                    Toast.makeText(getApplicationContext(), s+"Drop Points", Toast.LENGTH_SHORT).show();

                    editTextroute.setError(null);
                    dialog.dismiss();

                }                        }
                    });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

}

