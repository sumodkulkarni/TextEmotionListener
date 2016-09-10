package com.sumod.textemotionlistener.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.Contacts;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;

import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneCategory;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;
import com.sumod.textemotionlistener.MainActivity;
import com.sumod.textemotionlistener.R;
import com.sumod.textemotionlistener.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sumodkulkarni on 10/9/16.
 */
public class SMSListener extends BroadcastReceiver {

    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String messageBody = smsMessage.getMessageBody();

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                ToneAnalyzer service = new ToneAnalyzer(ToneAnalyzer.VERSION_DATE_2016_05_19);
                service.setUsernameAndPassword(Constants.watson_username, Constants.watson_password);

                ToneAnalysis tone = service.getTone(messageBody, null).execute();

                Uri uri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(smsMessage.getOriginatingAddress()));

                String senderName = null;
                Cursor cursor = context.getContentResolver().query(uri,
                        new String[] { Contacts.People.Phones.DISPLAY_NAME }, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    senderName = cursor.getString(cursor.getColumnIndex(Contacts.People.Phones.DISPLAY_NAME));
                    cursor.close();
                }

                if (senderName != null){
                    showNotification(context, senderName, getDominantTone(tone));
                }
                else {
                    showNotification(context, smsMessage.getDisplayOriginatingAddress(), getDominantTone(tone));
                }

            }
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

    private void showNotification(Context context, String senderName, String emotion){
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        Resources r = context.getResources();
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle("Message from " + senderName)
                .setContentText(emotion)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

}
