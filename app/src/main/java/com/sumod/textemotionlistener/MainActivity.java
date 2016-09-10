package com.sumod.textemotionlistener;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.sumod.textemotionlistener.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 100;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_SMS},
                    MY_PERMISSIONS_REQUEST_READ_SMS);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ToneAnalyzer service = new ToneAnalyzer(ToneAnalyzer.VERSION_DATE_2016_05_19);
        service.setUsernameAndPassword(Constants.watson_username, Constants.watson_password);

        String text =
                "I know the times are difficult! Our sales have been "
                        + "disappointing for the past three quarters for our data analytics "
                        + "product suite. We have a competitive data analytics product "
                        + "suite in the industry. But we need to do our job selling it! "
                        + "We need to acknowledge and fix our sales challenges. "
                        + "We can’t blame the economy for our lack of execution! "
                        + "We are missing critical sales opportunities. "
                        + "Our product is in no way inferior to the competitor products. "
                        + "Our clients are hungry for analytical tools to improve their "
                        + "business outcomes. Economy has nothing to do with it.";

// Call the service and get the tone
        ToneAnalysis tone = service.getTone(text, null).execute();
        Log.d(TAG, String.valueOf(tone));

        String dominantTone = getDominantTone(tone);
        Log.d(TAG, dominantTone);
    }

    private class RunToneAnalysis extends AsyncTask<String, Void, ToneAnalysis>{



        @Override
        protected ToneAnalysis doInBackground(String... params) {
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_READ_SMS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    Toast.makeText(this, "Please grant SMS permissions for this app to work", Toast.LENGTH_SHORT).show();
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_SMS)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_SMS},
                                MY_PERMISSIONS_REQUEST_READ_SMS);
                    }
                }
                return;

            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    Toast.makeText(this, "Please grant SMS permissions for this app to work", Toast.LENGTH_SHORT).show();
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_CONTACTS)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                    }
                }
                return;
        }
    }

    private String getDominantTone(ToneAnalysis toneAnalysis){
        String dominantToneName = "";

        try {
            JSONObject mainDocument = new JSONObject(toneAnalysis.toString());
            JSONObject documentArray = mainDocument.getJSONObject("document_tone");
            JSONArray toneCategories = documentArray.getJSONArray("tone_categories");
            JSONObject categoryEmotion = toneCategories.getJSONObject(0);
            JSONArray tonesArray = categoryEmotion.getJSONArray("tones");

            JSONObject anger = tonesArray.getJSONObject(0);
            JSONObject disgust = tonesArray.getJSONObject(1);
            JSONObject fear = tonesArray.getJSONObject(2);
            JSONObject joy = tonesArray.getJSONObject(3);
            JSONObject sadness = tonesArray.getJSONObject(4);

            List<JSONObject> tonesList = new ArrayList<>();
            tonesList.add(anger);
            tonesList.add(disgust);
            tonesList.add(fear);
            tonesList.add(joy);
            tonesList.add(sadness);

            JSONObject dominantTone = tonesList.get(0);
            for (int i=1; i<tonesList.size(); i++){
                if (tonesList.get(i).getInt("score") > dominantTone.getDouble("score"))
                    dominantTone = tonesList.get(i);
            }
            dominantToneName = dominantTone.getString("tone_name");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dominantToneName;
    }

}
