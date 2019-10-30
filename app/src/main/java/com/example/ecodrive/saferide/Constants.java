package com.example.ecodrive.saferide;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Constants {
    public static final String[] ACTIVITY_NAMES = {"IN_VEHICLE","ON_BICYCLE","WALKING","RUNNING","STILL"};
    public static int CurrentActivity = -1;
    public static LatLng CurrentLocation = null;
    public static ArrayList<String> Token = null;
    public static String Name = null;
    public static String Email = null;
}
