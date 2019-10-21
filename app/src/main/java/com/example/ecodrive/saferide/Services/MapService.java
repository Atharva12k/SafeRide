package com.example.ecodrive.saferide.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.example.ecodrive.saferide.SpeedCalculator;
import com.google.android.gms.maps.model.LatLng;

public class MapService extends Service {
    LocationManager locationManager;
    LocationListener locationListener;
    private double live_distance;
    private float live_acceleration;
    private float live_speed;
    private LatLng previous_latlong;
    private int LOCATION_COUNT;
    SpeedCalculator speedCalculator;
    private boolean permissionGranted;
    final private int REQUEST_COURSE_ACCESS = 123;
    final static public String MAP_SERVICE_BROADCAST = MapService.class.getName() + "LocationBroadcast";
    private Context context;
    private Intent updateintent;
    LocalBroadcastManager broadcaster;
    private float liveAccuracy;
    private static final String TAG = MapService.class.getName();
    //@androidx.annotation.Nullable
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        LOCATION_COUNT = 0;
        live_distance = 0;
        speedCalculator = new SpeedCalculator();
        Log.d(TAG,"oncreate");
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        permissionGranted = intent.getBooleanExtra("PERMISSION_STATUS", false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Service.START_STICKY;
        }
        Log.d(TAG,"onstartcommand");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        return Service.START_STICKY;
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }
        @Override
        public void onProviderEnabled(String s) { }
        @Override
        public void onProviderDisabled(String s) {}
        public void onLocationChanged(Location loc) {
            if(loc!=null) {
                float[] results = new float[10];
                if (loc != null) {
                    if (LOCATION_COUNT == 0) {
                        previous_latlong = new LatLng(loc.getLatitude(), loc.getLongitude());
                        LOCATION_COUNT = 1;
                    }  else {
                        LatLng curr_latlng = new LatLng(loc.getLatitude(),loc.getLongitude());
                        Location.distanceBetween(curr_latlng.latitude, curr_latlng.longitude, previous_latlong.latitude, previous_latlong.longitude, results);
                        live_distance += (results[0]);
                        liveAccuracy=loc.getAccuracy();
                        live_acceleration = speedCalculator.updateAcceleration(loc);
                        live_speed = (float) (3.6 * (loc.getSpeed()));
                        sendBroadcastMessage(live_speed, live_acceleration, live_distance, liveAccuracy, curr_latlng, previous_latlong);
                        //Toast.makeText(getBaseContext(),""+live_distance,Toast.LENGTH_SHORT).show();
                        Log.d(TAG, String.valueOf(live_distance));
                        previous_latlong = curr_latlng;

                        //Adding  polyLine

                        /**/
                    }
                }
            }
        }
        public void sendBroadcastMessage(float speed,float acceleration,double distance, float accuracy, LatLng currLatLng,LatLng previousLatLang)
        {
            updateintent=new Intent(MAP_SERVICE_BROADCAST);
            updateintent.putExtra("SPEED",speed);
            updateintent.putExtra("ACCELERATION",acceleration);
            updateintent.putExtra("DISTANCE",distance);
            updateintent.putExtra("ACCURACY",accuracy);
            updateintent.putExtra("CURRENT_LATLNG",currLatLng);
            updateintent.putExtra("PREVIOUS_LATLNG",previousLatLang);
            broadcaster.sendBroadcast(updateintent);
        }
    }
}
