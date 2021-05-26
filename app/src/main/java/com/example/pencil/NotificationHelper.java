package com.example.pencil;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

import androidx.core.app.NotificationCompat;

public class NotificationHelper  extends ContextWrapper {

    public static final String channel1ID = "channel1ID";
    public static final String channel1Name = "channel 1";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        createChannel();
    }

    private void createChannel() {
        NotificationChannel channel1 = new NotificationChannel(channel1ID,channel1Name, NotificationManager.IMPORTANCE_DEFAULT);
        channel1.enableLights(true);
        channel1.enableVibration(true);
        channel1.setLightColor(R.color.design_default_color_primary);
        channel1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel1);
    }
    public NotificationManager getManager(){

        if (mManager == null){
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;

    }
    public NotificationCompat.Builder getChannel1Notification(){
        return new NotificationCompat.Builder(getApplicationContext(),channel1ID)
                .setContentTitle("Alarm !")
                .setContentText("Important...")
                .setSmallIcon(R.drawable.ic_baseline_looks_one_24);
    }

}
