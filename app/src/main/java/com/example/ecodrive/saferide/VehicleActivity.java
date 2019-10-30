package com.example.ecodrive.saferide;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class VehicleActivity extends AppCompatActivity {

    private static final String TAG = "VehicleActivity";
    EditText _vNumber;
    Button _submitButton;
    EditText _vnameText;
    TextView _addVehicle;
    RadioButton _twoWheel;
    RadioButton _fourWheel;
    private FirebaseFirestore firestore;
    private DocumentReference documentReference;
    private HashMap<String,String> hashMap;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private FirebaseUser user;
    private String vname,vnumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            if(getSupportActionBar()!=null)
                this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_vehicle);
        firestore = FirebaseFirestore.getInstance();
        _vNumber = (EditText) findViewById(R.id.input_vnumber);
        radioGroup = findViewById(R.id.vehicleType);
        _submitButton = (Button) findViewById(R.id.btn_submit);

        _vnameText = (EditText) findViewById(R.id.input_name);
        _addVehicle=(TextView) findViewById(R.id.link_addVehicle);
        hashMap = new HashMap<>();
        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        _submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
        _addVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VehicleActivity.this, VehicleActivity.class);
                startActivity(intent);
            }
        });



    }



    public void signup() {
        Log.d(TAG, "SignUp");

        vname = _vnameText.getText().toString();
        vnumber = _vNumber.getText().toString();

        if (vname.equals("") && vnumber.equals("") ) {
            Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_LONG).show();
        }
        else {
            radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
            documentReference = firestore.collection("USER LIST").document(user.getUid()).collection("VEHICLE LIST").document(vnumber);
            hashMap.put("Registration Number", vnumber);
            hashMap.put("Vehicle Name", vname);
            hashMap.put("Vehicle Type",radioButton.getText().toString());
            documentReference.set(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d("Vehicle details", "Data added to databse");
                    Toast.makeText(getApplicationContext(),"Vehicle added",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void skip(View view) {
        Intent i = new Intent(VehicleActivity.this,Question.class);
        startActivity(i);
    }
}
