package com.example.ecodrive.saferide;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecodrive.saferide.Services.AccelerationDetectionService;
import com.example.ecodrive.saferide.Services.ActivityRecognitionService;
import com.example.ecodrive.saferide.Services.DetectedActivityService;
import com.example.ecodrive.saferide.Services.MapService;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.google.firebase.messaging.RemoteMessage;


import android.os.Vibrator;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    final private int REQUEST_COURSE_ACCESS = 123;
    boolean permissionGranted = false;
    private float live_speed;
    private float live_acceleration;
    private double live_distance;
    private double Co2emission = 0;
    private String fuelType = "petrol";
    BroadcastReceiver broadcastReceiverForRecognition;
    BroadcastReceiver broadcastReceiverForAcceleration;
    private int[] activitiesConfidence;
    boolean firstTime;
    private Marker previousMarker = null;
    private static final String TAG = MapsActivity.class.getName();
    private LatLng currentLatLng;
    private float accuracy;
    private static int latLngCount=0;
    TextView activtyUpdates;
    TextView accelUpdates;
    private List<LatLng> onWayPoints;
    TextView speedDisplay;
    private DecimalFormat decimalFormatForSpeed;
    private DecimalFormat decimalFormatForDistance;
    private DecimalFormat decimalFormatForAccel;
    private FusedLocationProviderClient mfusedlocationclient;
    private LatLng previousLatLng;
    private double x,y,z,mAccelCurrent,mAccelLast = 0,mAccel;
    private Vibrator vibrateObj;
    private boolean startstop = true;
    private FirebaseFirestore firestore;
    private DocumentReference documentReference;
    private FirebaseUser user;
    FirebaseAuth firebaseAuth;
    private GeoPoint geoPoint;
    private HashMap<String,GeoPoint> hashMap;
    static ArrayList<Responder> tempList;
    static float[] results;
    private Context context;
    final private int REQUEST_SEND_SMS = 123;
    private boolean accidentDetected;
    static ArrayList<Responder> EmergencyResponders;
    private ProgressDialog mProgressDialog;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = getApplicationContext();


        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        activtyUpdates = findViewById(R.id.activtyInfo);
        accelUpdates = findViewById(R.id.accel);
        activitiesConfidence = new int[5];
        onWayPoints=new ArrayList<>();
        speedDisplay = findViewById(R.id.speed);
        decimalFormatForSpeed = new DecimalFormat("0.00##");
        decimalFormatForDistance = new DecimalFormat("0.##");
        decimalFormatForAccel = new DecimalFormat("0.##");
        mfusedlocationclient = LocationServices.getFusedLocationProviderClient(this);
        vibrateObj = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        hashMap = new HashMap<>();
        accidentDetected = false;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Constants.Token = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.SEND_SMS}, 123);
        }
        else
        {
            startLocationTracking();

            //Location Update Receiver
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    live_speed = intent.getFloatExtra("SPEED", 0);
                    live_acceleration = intent.getFloatExtra("ACCELERATION", 0);
                    live_distance = intent.getDoubleExtra("DISTANCE", 0);
                    accuracy = intent.getFloatExtra("ACCURACY",0);
                    currentLatLng=intent.getExtras().getParcelable("CURRENT_LATLNG");
                    previousLatLng=intent.getExtras().getParcelable("PREVIOUS_LATLNG");

                    Constants.CurrentLocation = currentLatLng;

                    if(latLngCount%2==0){
                        onWayPoints.add(currentLatLng);
                    }
                    latLngCount+=1;
                    if(previousMarker!=null)
                        previousMarker.remove();

                    MarkerOptions markerOptions = new MarkerOptions().position(currentLatLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    previousMarker = mMap.addMarker(markerOptions);

                    Polyline livePolyline = mMap.addPolyline(new PolylineOptions().addAll(onWayPoints));
                    livePolyline.setColor(ContextCompat.getColor(getBaseContext(), R.color.Blue));
                    livePolyline.setEndCap(new RoundCap());livePolyline.setEndCap(new RoundCap());
                    livePolyline.setWidth(10);
                    livePolyline.setClickable(true);

                    updateLocationTextView(live_acceleration, live_speed, live_distance, accuracy);


                    Log.d(TAG, "ONRECEIVE MAP ACTIVITY");
                    Log.d(TAG, "ONWAYPOINTS SIZE: "+onWayPoints.size());
                }
            };
            LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                    new IntentFilter(MapService.MAP_SERVICE_BROADCAST)
            );
        }

        Log.d("USER ID",""+FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseFirestore.getInstance().collection("USER LIST").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Log.d("SUCCESS",task.getResult().getData().toString());
                            Map<String,Object> tempMap = task.getResult().getData();
                            Constants.Name = (String) tempMap.get("Name");
                            Constants.Email = (String) tempMap.get("Email");
//                            GeoPoint geoPoint = (GeoPoint) tempMap.get("MyLocation");
//                            Constants.CurrentLocation = new LatLng (geoPoint.getLatitude(),geoPoint.getLongitude());
                        }
                        else {
                            Log.d("Error", ""+task.getException().getMessage());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error",e.getMessage());
            }
        });



        startTrackingActivities();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SEND_SMS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MapsActivity.this,
                            "Permission Granted", Toast.LENGTH_SHORT).show();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);

                } else {
                    Toast.makeText(MapsActivity.this,
                            "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode,
                        permissions, grantResults);
        }
    }

    //Call Responder
    static void callResponder()
    {
        tempList = new ArrayList<>();
        EmergencyResponders = new ArrayList<>();
        results = new float[10];
        FirebaseFirestore.getInstance().collection("RESPONDER LIST")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                GeoPoint tempGeopoint = (GeoPoint) document.getData().get("MyLocation");
//                                Log.d(TAG,tempMap.get("Ambulence Number") + " :" +tempGeopoint.getLatitude() + ", "+tempGeopoint.getLongitude() );
                                Location.distanceBetween(Constants.CurrentLocation.latitude,Constants.CurrentLocation.longitude,tempGeopoint.getLatitude(),tempGeopoint.getLongitude(),results);
//                                Toast.makeText(getBaseContext(),"Responder Name"+document.getData().get(" Name")+" ",Toast.LENGTH_SHORT).show();
                                tempList.add(new Responder(""+document.getData().get("Responder Number"),results[0],""+document.getData().get("Token")));
                            }
                            Responder closest = new Responder("No Responder",-1.0f,"");
                            float min = 99999;
                            int count = 0;
                            float minDistance = 500;
                            //finding closest Emergency Responders

                            while(count==0) {
                                for (Responder a : tempList) {
                                    if(a.distance<minDistance)
                                    {
                                        EmergencyResponders.add(a);
                                        count++;
                                    }
                                }
                                minDistance += 500;
                            }
//                            for (Responder a: tempList)
//                            {
//                                if(a.distance < min)
//                                {
//                                    min = a.distance;
//                                    closest = new Responder(a.ResponderNo,min,a.token);
//                                }
//                            }
//                          Constants.Token = closest.token;
                            for(Responder a:EmergencyResponders)
                            {
                                Constants.Token.add(a.token);
                                Log.d("Constant token updated","");
                                String message="There has been an accident around you.\nReach them : ";
                                message = message.concat("http://maps.google.com/maps?q=loc:" + String.format("%f,%f", Constants.CurrentLocation.latitude , Constants.CurrentLocation.longitude));
                                Log.d("Emergency message",a.ResponderNo+"  "+message);
//                                sendMessage(a.ResponderNo,message);
//                                Toast.makeText(getBaseContext(),"Responder "+a.ResponderNo+" ",Toast.LENGTH_SHORT).show();
                            }

//                            Toast.makeText(getBaseContext(),"Closest Responder is :  "+closest.ResponderNo+" at "+closest.distance+"m",Toast.LENGTH_LONG).show();
//                            Log.d("Closest Ambnulance","Closest Responder is : "+closest.ResponderNo+" at "+closest.distance+"m Token: "+closest.token);




                            new Notify().execute();



                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
//                            Toast.makeText(getBaseContext(),"Error getting documents: " + task.getException(),Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error getting documents: "+ e.getMessage());
//                Toast.makeText(getBaseContext(),"Error getting documents: " + e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startTrackingActivities() {
        Intent intent = new Intent(MapsActivity.this, ActivityRecognitionService.class);
        startService(intent);

        //ActivityRecognitionReceiver
        broadcastReceiverForRecognition = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(DetectedActivityService.RECOGNITION_SERVICE_BROADCAST)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverForRecognition),
                new IntentFilter(DetectedActivityService.RECOGNITION_SERVICE_BROADCAST)
        );
    }

    public void startAccelerationService(final View view) {
        if(startstop)
        {
            Intent intent = new Intent(MapsActivity.this, AccelerationDetectionService.class);
            startService(intent);
            Button btn = (Button) view;
            btn.setText("Stop Monitoring");
            startstop = false;
            HashMap<String,String> dataValue = new HashMap<>();
            dataValue.put("Name","Atharva");

            //AccelerationReceiver
            broadcastReceiverForAcceleration = new BroadcastReceiver() {
                @Override
                public synchronized void onReceive(Context context, Intent intent) {

                    x = intent.getDoubleExtra("AXIS-X",0);
                    y = intent.getDoubleExtra("AXIS-Y",0);
                    z = intent.getDoubleExtra("AXIS-Z",0);
                    //Log.d(TAG,"INSIDE ACCELERATION BROADCAST RECEIVER");

                    mAccelCurrent = Math.sqrt(x * x + y * y + z * z);
                    double delta = mAccelCurrent - mAccelLast;
                    mAccel = mAccel * 0.9f + delta;
                    double g = (mAccelCurrent / 9.81);

                    //if(mAccel > 50){
                    if(g > 4 && !accidentDetected /*&& Constants.CurrentActivity ==4*/){
                        //stopping further updates;
                        accidentDetected = true;
                        Intent in = new Intent(MapsActivity.this, AccelerationDetectionService.class);
                        stopService(in);
                        Button btn = (Button) view;
                        btn.setText("Start Monitoring");
                        startstop = true;
                        //Log.d("Change in acceleration", String.valueOf(mAccel));
                        Log. d("Change in acceleration", String.valueOf(g));

                        timer = new Timer(getBaseContext(),mProgressDialog);
                        timer.execute();
                        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel Alert", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                timer.cancel(true);
                            }
                        });

                        mProgressDialog.show();

                        vibrateObj.vibrate(1000);
//                        callResponder();



                        Toast.makeText(getBaseContext(), "Accident Detected", Toast.LENGTH_SHORT).show();

                        if (Constants.CurrentLocation != null)
                        {
                            user = firebaseAuth.getCurrentUser();

                            geoPoint = new GeoPoint(Constants.CurrentLocation.latitude, Constants.CurrentLocation.longitude);
                            Log.d("FirebaseUser","user ID: "+user.getUid()+" GeoPoint: "+geoPoint.getLatitude()+" "+geoPoint.getLongitude());
                            hashMap.put("MyLocation",geoPoint);
                            documentReference = firestore.collection("USER LIST").document(user.getUid());
                            documentReference.update("MyLocation",geoPoint).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("MapsActivity", "Location updated to firestore");
                                    //Toast.makeText(getBaseContext(), "Location updated to firestore", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                    //accelUpdates.setText("X: "+decimalFormatForAccel.format(x)+"\nY: "+decimalFormatForAccel.format(y)+"\nZ: "+decimalFormatForAccel.format(z)+"\nG: "+decimalFormatForAccel.format(g));
                    accelUpdates.setText("G: "+decimalFormatForAccel.format(g));
                }
            };
            LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverForAcceleration),
                    new IntentFilter(AccelerationDetectionService.ACCELERATION_SERVICE_BROADCAST)
            );
        }
        else{
            Intent intent = new Intent(this, AccelerationDetectionService.class);
            stopService(intent);
            Button btn = (Button) view;
            btn.setText("Start Monitoring");
            startstop = true;
            accidentDetected = false;
        }
    }

    static void sendMessage(String phoneNo ,String message)
    {
        if(phoneNo!=null) {
//            Toast.makeText(getBaseContext(),"Emergency contacts informed\n"+message,Toast.LENGTH_SHORT).show();
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNo, null, message, null, null);
        }
    }

    static void informResponder()
    {
        FirebaseFirestore.getInstance().collection("USER LIST").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("CONTACT LIST")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Contact retrieve", document.getId() + " => " + document.getData());
                                Map<String,Object> contact = document.getData();
                                String message="Your friend has been in an accident.\nReach them : ";
                                message = message.concat("http://maps.google.com/maps?q=loc:" + String.format("%f,%f", Constants.CurrentLocation.latitude , Constants.CurrentLocation.longitude));
                                String phoneNo = (String)contact.get("PhoneNo");
                                Log.d("EmgContact",phoneNo);
                                sendMessage(phoneNo,message);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void handleUserActivity(int type, int confidence) {

        String activityName = "";
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                activityName = "IN_VEHICLE";
                Log.d(TAG, "In Vehicle");
                //activtyUpdates.setText("Driving  Confidence: " + confidence);
                activitiesConfidence[0] = confidence;
                break;

            case DetectedActivity.ON_BICYCLE:
                activityName = "ON_BICYCLE";
                Log.d(TAG, "On Bicycle");
                activitiesConfidence[1] = confidence;
                break;

            case DetectedActivity.WALKING:
                activityName = "WALKING";
                Log.d(TAG, "Walking");
                activitiesConfidence[2] = confidence;
                break;

            case DetectedActivity.RUNNING:
                activityName = "RUNNING";
                Log.d(TAG, "Running");
                activitiesConfidence[3] = confidence;
                break;

            case DetectedActivity.STILL:
                activityName = "STILL";
                Log.d(TAG, "Still");
                activitiesConfidence[4] = confidence;
                break;
        }
        int activity[] = getMax(activitiesConfidence);
        Constants.CurrentActivity = activity[1];
        /*if (confidence > 75) {
            Toast.makeText(getBaseContext(), "Are you " + activityName + " ?\nConfidence: " + confidence, Toast.LENGTH_SHORT).show();
        }*/
        updateTextView(Constants.ACTIVITY_NAMES[activity[1]]);

    }

    int[] getMax(int[] activitiesConfidence)
    {
        int activity[]={0,0};
        for(int i=0;i<activitiesConfidence.length;i++)
            if(activity[0]<activitiesConfidence[i]) {
                activity[0] = activitiesConfidence[i];
                activity[1]=i;
            }
        return activity;
    }

    void updateTextView(String activityName) {
        StringBuilder sb = new StringBuilder();
//        sb.append("IN_VEHICLE: " + activitiesConfidence[0] + "\t ");
//        sb.append("ON_BICYCLE: " + activitiesConfidence[1] + "\n");
//        sb.append("WALKING: " + activitiesConfidence[2] + "\t ");
//        sb.append("RUNNING: " + activitiesConfidence[3] + "\n");
//        sb.append("STILL: " + activitiesConfidence[4] + "\n");
        sb.append("You Are " + activityName);
        activtyUpdates.setText(sb);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "OnMapREADY Detected");
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COURSE_ACCESS);
            return;
        } else {
            permissionGranted = true;
        }
        if (permissionGranted) {
            moveToUserLocation();
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(false);
            startLocationTracking();
        }

    }

    void startLocationTracking()
    {
        Intent serviceIntent = new Intent(this, MapService.class);
        serviceIntent.putExtra("PERMISSION_STATUS", permissionGranted);
        startService(serviceIntent);
    }

    void moveToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mfusedlocationclient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location!=null) {
                    Log.d(TAG,"INSIDE FUSED: "+ "" + location.getLatitude() + " " + location.getLongitude());
                    Log.d(TAG,"BEARING : "+ "" + location.getBearing());
                    Log.d(TAG,"ACCURACY : "+ "" + location.getAccuracy());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),16));
                    //mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("Me"));
                    //Toast.makeText(getBaseContext(), "Your Location: \nlat:" + location.getLatitude() + " Lng:" + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    Constants.CurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                }
            }
        });
    }
    void updateLocationTextView(float live_acceleration, float live_speed, double live_distance, float accuracy){

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Acceleration: " + decimalFormatForSpeed.format(live_acceleration) +
                "\nSpeed: " + decimalFormatForSpeed.format(live_speed) + " km/hr" + "" +
                "\nDistance: " + decimalFormatForDistance.format(live_distance) + " m");
        //+"\nAccuracy : "+accuracy);

        speedDisplay.setText(stringBuilder);

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }


    public void optionClick(View view) {

        PopupMenu popup = new PopupMenu(MapsActivity.this, view);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.options_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getTitle().equals("Logout")) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    Runtime.getRuntime().exit(0);
                }
                else
                {
                    Intent i = new Intent(MapsActivity.this,Contacts.class);
                    startActivity(i);
                }
                return true;
            }
        });

        popup.show();
    }
}
class Responder
{
    String ResponderNo;
    Float distance;
    String token;

    public Responder(String ResponderNo, Float distance, String token) {
        ResponderNo = ResponderNo;
        this.distance = distance;
        this.token = token;
    }
}

class Notify extends AsyncTask<String,Void,Void>
{
    private String token;
    @Override
    protected Void doInBackground(String... param) {

        for(String token:Constants.Token) {
//            token = Constants.Token;

            try {

                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=AIzaSyDhfU0G8_vLMIfjX5VXEC4K2-rbn-vydfs");
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();

                json.put("to", token);

                Log.d("SendNotif", Constants.Name + " " + Constants.Email + " " + Constants.CurrentLocation.toString());
                JSONObject myData = new JSONObject();
                myData.put("Name", "" + Constants.Name);
                myData.put("Email", "" + Constants.Email);
                myData.put("Location", Constants.CurrentLocation.latitude + "," + Constants.CurrentLocation.longitude);


                JSONObject info = new JSONObject();
                info.put("title", "Emergency New Request");   // Notification title
                info.put("body", "There's been accident around you"); // Notification body

                json.put("notification", info);
                json.put("data", myData);
                Log.d("JSON", json.toString());
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(json.toString());
                wr.flush();
                conn.getInputStream();

            } catch (Exception e) {
                Log.d("Error", "" + e);
            }
        }

        return null;
    }
}

