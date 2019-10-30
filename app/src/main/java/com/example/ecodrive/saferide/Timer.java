package com.example.ecodrive.saferide;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.TintContextWrapper;

public class Timer extends AsyncTask<String, String, Integer> {
    Context context;
    ProgressDialog dialog;
    int counter;

    Timer(Context ctx, ProgressDialog dialog){
        this.context=ctx;
        this.dialog=dialog;
        this.counter=15;

    }
    @Override
    protected Integer doInBackground(String... strings) {
        while(this.counter>0){
            try {
                if(isCancelled()){
                    this.counter = 999;
                    break;
                }
                Thread.sleep(1000);
                publishProgress(""+this.counter);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.counter--;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        dialog.setMessage(" Sending Alert in ... "+values[0]+" secs");
        dialog.setProgress(Integer.parseInt(values[0]));

    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        dialog.dismiss();
        if(this.counter<=0) {
            MapsActivity.callResponder();
            MapsActivity.informResponder();
            this.counter=15;
            Toast.makeText(context,"Alert Sent",Toast.LENGTH_SHORT).show();
        }
    }
}
