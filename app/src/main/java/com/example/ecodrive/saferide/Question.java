package com.example.ecodrive.saferide;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class Question extends Activity {

    RadioGroup rg1,rg2,rg3,rg4,rg5,rg6;
    RadioButton rb11,rb12,rb13,rb14,rb16,rb17;
    RadioButton rb21,rb22,rb23,rb24,rb26,rb27;
    RadioButton rb31,rb32,rb33,rb34,rb36,rb37;
    RadioButton rb41,rb42,rb43,rb44,rb46,rb47;
    RadioButton rb51,rb52,rb53,rb54,rb56,rb57;
    RadioButton rb61,rb62,rb63,rb64,rb66,rb67;

    float value;
    Button save;
    Boolean riskBehavior=Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        save = (Button)findViewById(R.id.savedata);
        findIds();
        rg1 = (RadioGroup)findViewById(R.id.rg1);
        rg2 = (RadioGroup)findViewById(R.id.rg2);
        rg3 = (RadioGroup)findViewById(R.id.rg3);
        rg4 = (RadioGroup)findViewById(R.id.rg4);
        rg5 = (RadioGroup)findViewById(R.id.rg5);
        rg6 = (RadioGroup)findViewById(R.id.rg6);



        rg1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(rb11.isChecked())
                    value +=0.0;
                else if(rb12.isChecked())
                    value +=0.2;
                else if(rb13.isChecked())
                    value +=0.4;
                else if(rb14.isChecked())
                    value +=0.6;
                else if(rb16.isChecked())
                    value +=0.8;
                else if(rb17.isChecked())
                    value +=1.0;
            }
        });

        rg2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(rb21.isChecked())
                    value +=0.0;
                else if(rb22.isChecked())
                    value +=0.2;
                else if(rb23.isChecked())
                    value +=0.4;
                else if(rb24.isChecked())
                    value +=0.6;
                else if(rb26.isChecked())
                    value +=0.8;
                else if(rb27.isChecked())
                    value +=1.0;
            }
        });

        rg3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(rb31.isChecked())
                    value +=0.0;
                else if(rb32.isChecked())
                    value +=0.2;
                else if(rb33.isChecked())
                    value +=0.4;
                else if(rb34.isChecked())
                    value +=0.6;
                else if(rb36.isChecked())
                    value +=0.8;
                else if(rb37.isChecked())
                    value +=1.0;
            }
        });

        rg4.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(rb41.isChecked())
                    value +=0.0;
                else if(rb42.isChecked())
                    value +=0.2;
                else if(rb43.isChecked())
                    value +=0.4;
                else if(rb44.isChecked())
                    value +=0.6;
                else if(rb46.isChecked())
                    value +=0.8;
                else if(rb47.isChecked())
                    value +=1.0;
            }
        });

        rg5.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(rb51.isChecked())
                    value +=0.0;
                else if(rb52.isChecked())
                    value +=0.2;
                else if(rb53.isChecked())
                    value +=0.4;
                else if(rb54.isChecked())
                    value +=0.6;
                else if(rb56.isChecked())
                    value +=0.8;
                else if(rb57.isChecked())
                    value +=1.0;
            }
        });

        rg6.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(rb61.isChecked())
                    value +=0.0;
                else if(rb62.isChecked())
                    value +=0.2;
                else if(rb63.isChecked())
                    value +=0.4;
                else if(rb64.isChecked())
                    value +=0.6;
                else if(rb66.isChecked())
                    value +=0.8;
                else if(rb67.isChecked())
                    value +=1.0;
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(value>3)
                    riskBehavior = Boolean.TRUE;
                else
                    riskBehavior = Boolean.FALSE;
                Toast.makeText(getApplicationContext(),"Risk Taking behavior is "+riskBehavior,Toast.LENGTH_SHORT).show();

                Intent startAct = new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(startAct);
            }
        });

    }

    public void findIds()
    {

        rb11 =findViewById(R.id.rb11);
        rb12 =findViewById(R.id.rb12);
        rb13 =findViewById(R.id.rb13);
        rb14 =findViewById(R.id.rb14);
        rb16 =findViewById(R.id.rb16);
        rb17 =findViewById(R.id.rb17);

        rb21 =findViewById(R.id.rb21);
        rb22 =findViewById(R.id.rb22);
        rb23 =findViewById(R.id.rb23);
        rb24 =findViewById(R.id.rb24);
        rb26 =findViewById(R.id.rb26);
        rb27 =findViewById(R.id.rb27);

        rb31 =findViewById(R.id.rb31);
        rb32 =findViewById(R.id.rb32);
        rb33 =findViewById(R.id.rb33);
        rb34 =findViewById(R.id.rb34);
        rb36 =findViewById(R.id.rb36);
        rb37 =findViewById(R.id.rb37);

        rb41 =findViewById(R.id.rb41);
        rb42 =findViewById(R.id.rb42);
        rb43 =findViewById(R.id.rb43);
        rb44 =findViewById(R.id.rb44);
        rb46 =findViewById(R.id.rb46);
        rb47 =findViewById(R.id.rb47);

        rb51 =findViewById(R.id.rb51);
        rb52 =findViewById(R.id.rb52);
        rb53 =findViewById(R.id.rb53);
        rb54 =findViewById(R.id.rb54);
        rb56 =findViewById(R.id.rb56);
        rb57 =findViewById(R.id.rb57);

        rb61 =findViewById(R.id.rb61);
        rb62 =findViewById(R.id.rb62);
        rb63 =findViewById(R.id.rb63);
        rb64 =findViewById(R.id.rb64);
        rb66 =findViewById(R.id.rb66);
        rb67 =findViewById(R.id.rb67);

    }
}
