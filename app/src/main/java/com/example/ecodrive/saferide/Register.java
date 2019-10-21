package com.example.ecodrive.saferide;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    EditText _emailText;
    EditText _passwordText;
    EditText _retypePasswordText;
    Button _signupButton;
    TextView _loginLink;
    EditText _fnameText;
    static String uid = "";
    public ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private CollectionReference collectionReference;
    private DocumentReference documentReference;
    private HashMap<String,Object> hashMap;
    private String fname,email,password,retypePassword;
    private FirebaseUser user;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            if(getSupportActionBar()!=null)
                this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_register);
        _emailText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _retypePasswordText = (EditText) findViewById(R.id.input_retypePassword);
        _signupButton = (Button) findViewById(R.id.btn_signup);
        _loginLink = (TextView) findViewById(R.id.link_login);
        _fnameText = (EditText) findViewById(R.id.input_fname);
        firebaseAuth=FirebaseAuth.getInstance();
        hashMap = new HashMap<>();
        firestore = FirebaseFirestore.getInstance();

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signup();
                //Intent intent = new Intent(getApplicationContext(), VehicleActivity.class);
                //startActivity(intent);

            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the com.example.rift.jiofinal.Login activity
                Intent intent = new Intent(Register.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public void signup() {
        Log.d(TAG, "SignUp");
        fname = _fnameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        retypePassword = _retypePasswordText.getText().toString();

        if (!email.equals("") && password.equals("") && fname.equals("") && retypePassword.equals("")) {
            Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_LONG).show();

        }
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Toast.makeText(getBaseContext(),"Account created successfully",Toast.LENGTH_SHORT).show();
                user = firebaseAuth.getCurrentUser();
                documentReference = firestore.collection("USER LIST").document(user.getUid());
                hashMap.put("Name",fname);hashMap.put("Email",email);
                documentReference.set(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Register","Data added to databse");
                    }
                });
                Intent intent = new Intent(getApplicationContext(), VehicleActivity.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(),"User creation failed",Toast.LENGTH_SHORT).show();
            }
        });

        _signupButton.setEnabled(false);


    }
}
