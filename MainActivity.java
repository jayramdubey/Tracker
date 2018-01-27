package com.jayram.routetracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1001;
    public static final int LOCATION_UPDATE_INTERVAL_IN_MS = 10_000;
    public static final int LOCATION_UPDATE_FASTEST_INTERVAL_IN_MS = 5_000;
    public static final String LAT_LONG_TEXT_FORMAT = "Count: %s , Lat: %s ,Long: %s, Distance: %s";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private TextView latlngTextView ;
    private LocationCallback locationCallback;

    private boolean isVisible ;
    private int count;
    private Location lastLocation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        latlngTextView = (TextView) findViewById(R.id.latlng_tv);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        setupLocationClient();
        checkAndRequestPermission(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
        stopLocationUpdates();
    }

    void checkAndRequestPermission(Activity activity){
        int permissionCheck = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            showToast(activity,"Permission Granted.. Request Location now.", Toast.LENGTH_SHORT);
            //Take him to next screen and show location data or refresh same screen with location data
            // setupLocationClient();
            requestLastLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showToast(activity,"Permission Denied with Don't Ask Again- Show rationale", Toast.LENGTH_SHORT);
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    private void setupLocationClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = createLocationRequest();
        locationCallback  = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(isVisible){
                    Location location = locationResult.getLastLocation();
                    count++;
                    String latLngText = latlngTextView.getText() + "\t "+
                            String.format(LAT_LONG_TEXT_FORMAT,count,
                                    Double.toString(location.getLatitude()),
                                    Double.toString(location.getLongitude()),
                                    HaversineDistanceUtil.distance(lastLocation, location));
                    latlngTextView.setText(latLngText);
                    lastLocation = location;
                }

            }
        };
    }


    void showToast(Context context, String text, int duration){
        Toast.makeText(context,text, duration).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       if(requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {

           if (grantResults.length > 0
                   && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

               // permission was granted, yay! Do the
               // contacts-related task you need to do.
               showToast(this,"Permission Granted.. Request Locatiom now.", Toast.LENGTH_SHORT);
               //setupLocationClient();
               requestLastLocation();
           } else {

               // permission denied, boo! Disable the
               // functionality that depends on this permission.
               showToast(this,"Permission Denied.", Toast.LENGTH_SHORT);
           }
       }

    }

    @SuppressLint("MissingPermission")
    private void requestLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            count++;
                            String latLngText = latlngTextView.getText() + "\t "+
                                    String.format(LAT_LONG_TEXT_FORMAT,count,
                                    Double.toString(location.getLatitude()),
                                    Double.toString(location.getLongitude()),
                                            HaversineDistanceUtil.distance(lastLocation, location));
                            latlngTextView.setText(latLngText);
                            lastLocation = location;
                        }
                    }
                });

    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL_IN_MS);
        mLocationRequest.setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL_IN_MS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,null);
    }

    void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

}
