package com.example.ecodrive.saferide.Services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class AccelerationDetectionService extends Service implements SensorEventListener {

    private SensorManager sensorMan;
    private Sensor accelerometer;
    private float[] mGravity;
    private double mAccel;
    private double mAccelCurrent;
    private double mAccelLast;
    private Intent updateintent;
    final static public String ACCELERATION_SERVICE_BROADCAST = MapService.class.getName() + "AccelerationBroadcast";
    private static String TAG = AccelerationDetectionService.class.getName();
    LocalBroadcastManager broadcaster;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"INSIDE ONCREATE");
        sensorMan = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    /*@Override
    public void onResume() {
        super.onResume();
        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMan.unregisterListener(this);
    }*/


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

       // Log.d("INSIDE","SENSORCHANGED");
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = sensorEvent.values.clone();
            // Shake detection
            double x = mGravity[0];
            double y = mGravity[1];
            double z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(x * x + y * y + z * z);
            double delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            sendBroadcast(x,y,z,mAccel);


//            if(mAccel > 1.5){
//                Log.d("Change in acceleration", String.valueOf(mAccel));
//                 sendBroadcast(x,y,z,mAccel);
//            }
        }

    }

    void sendBroadcast(double x,double y,double z,double mAccel)
    {
        updateintent = new Intent(ACCELERATION_SERVICE_BROADCAST);
        updateintent.putExtra("AXIS-X",x);
        updateintent.putExtra("AXIS-Y",y);
        updateintent.putExtra("AXIS-Z",z);
        updateintent.putExtra("ACCEL",mAccel);
        broadcaster.sendBroadcast(updateintent);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorMan.unregisterListener(this);
//        Toast.makeText(this, "AccelerationDetectionService stopped", Toast.LENGTH_LONG).show();
    }
}
