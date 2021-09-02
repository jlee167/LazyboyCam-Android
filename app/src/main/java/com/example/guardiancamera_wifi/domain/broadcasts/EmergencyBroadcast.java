package com.example.guardiancamera_wifi.domain.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.guardiancamera_wifi.MyApplication;
import org.json.JSONException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;



public class EmergencyBroadcast extends BroadcastReceiver {

    public EmergencyBroadcast() {}


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] pdus;
        SmsMessage[] smsMessage;

        try {
            assert bundle != null;
            pdus = (Objects[]) bundle.get("pdus");
            assert pdus != null;
            smsMessage = new SmsMessage[pdus.length];
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < pdus.length; i++) {
            smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));
            Log.d("SMS MESSAGE: ", smsMessage[0].getMessageBody());

            /* Check if message is from Lazyboy Server */
            /* @Todo: Improve input check */
            try {
                if (!smsMessage[i].getOriginatingAddress().contains("00644"))
                    continue;
                if (!smsMessage[i].getMessageBody().contains("Lazyboy"))
                    continue;
            } catch(NullPointerException e) {
                e.printStackTrace();
                return;
            }
            /* If message is emergency broadcast from Lazyboy Server */
            this.handleEmergency(context);
        }
    }


    private void handleEmergency(Context context) {
        try {
            /* Update peer states */
            MyApplication.mainServerConn.getPeers();

            /* Alert user with push notification and alarm */
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            RingtoneManager.getRingtone(context, notification).play();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "LazyBoyChannel")
                    .setContentTitle("Emergency Notification")
                    .setContentText("Emergency Text")
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(0, builder.build());

        } catch (JSONException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
