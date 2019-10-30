package com.example.ecodrive.saferide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Contacts extends AppCompatActivity {

    private static final int RESULT_PICK_CONTACT =1;
    private TextView phone;
    private Button select;
    ArrayList<String> alist = new ArrayList<>();
    String[] contactsArray={};
    ListView listView;
    Map<String,String> emgcontact;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_contacts);

        try
        {
            if(getSupportActionBar()!=null)
                this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        firestore = FirebaseFirestore.getInstance();
//        phone = findViewById (R.id.phone);
        select = findViewById (R.id.select);
        listView= findViewById(R.id.phoneList);
        emgcontact = new HashMap<>();
        user = FirebaseAuth.getInstance().getCurrentUser();


        firestore.collection("USER LIST").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("CONTACT LIST")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Contact retrieve", document.getId() + " => " + document.getData());
                                Map<String,Object> contact = document.getData();
                                String phoneNo = (String)contact.get("PhoneNo");
                                String name = (String)contact.get("Name");
                                Log.d("EmgContact",phoneNo);
                                if(phoneNo!=null) {
                                    alist.add("Name : "+name+"\nNumber : "+phoneNo);
                                }
                            }
                            if(alist!=null) {
                                Log.d("EmgContact", alist.toString());
                                contactsArray = alist.toArray(new String[alist.size()]);
                                Log.d("EmgContact", "" + contactsArray.length);
                                ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_listview, contactsArray);
                                listView.setAdapter(adapter);
                            }
                        } else {
                            Log.d("ELSE ", "Error getting documents: ", task.getException());
                        }
                    }
                });


        select.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent in = new Intent (Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult (in, RESULT_PICK_CONTACT);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK)
        {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked (data);
                    break;
            }
        }
        else
        {
            Toast.makeText (this, "Failed To pick contact", Toast.LENGTH_SHORT).show ();
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;

        try {
            String phoneNo = null;
            String name= null;

            Uri uri = data.getData ();
            cursor = getContentResolver ().query (uri, null, null,null,null);
            cursor.moveToFirst ();

            int phoneIndex = cursor.getColumnIndex (ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nameIndex = cursor.getColumnIndex (ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            phoneNo = cursor.getString (phoneIndex);
            name = cursor.getString(nameIndex);
            String text = phoneNo+"\n"+name;

            phoneNo = phoneNo.replaceAll("-","");
            //get the picked contact from above ^^ lines


            documentReference = firestore.collection("USER LIST").document(user.getUid()).collection("CONTACT LIST").document(phoneNo);
            emgcontact.clear();
            emgcontact.put("Name", name);
            emgcontact.put("PhoneNo", phoneNo);
            documentReference.set(emgcontact).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d("Contact details", "Data added to databse");
                    Toast.makeText(getApplicationContext(),"Contact added",Toast.LENGTH_SHORT).show();
                }
            });
            alist.clear();
            firestore.collection("USER LIST").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("CONTACT LIST")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("Contact retrieve", document.getId() + " => " + document.getData());
                                    Map<String,Object> contact = document.getData();
                                    String phoneNo = (String)contact.get("PhoneNo");
                                    String name = (String)contact.get("Name");
                                    Log.d("EmgContact",phoneNo);
                                    if(phoneNo!=null) {
                                        alist.add("Name : "+name+"\nNumber : "+phoneNo);
                                    }
                                }
                                Log.d("EmgContact",alist.toString());
                                contactsArray = alist.toArray(new String[alist.size()]);
                                Log.d("EmgContact",""+contactsArray.length);
                                ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.activity_listview,contactsArray);
                                listView.setAdapter(adapter);
                            } else {
                                Log.d("ELSE ", "Error getting documents: ", task.getException());
                            }
                        }
                    });


        } catch (Exception e) {
            e.printStackTrace ();
        }
    }
}
