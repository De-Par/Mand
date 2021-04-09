package com.messenger.mand.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

public class OreoAndAboveNotification extends ContextWrapper {

    private static final String ID = "some_id";
    private static final String NAME = "Mand";
    private NotificationManager notificationManager;

    private final long[] pattern = {0, 1000, 500, 1000};

    @SuppressLint("ObsoleteSdkInt")
    public OreoAndAboveNotification(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(ID, NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(pattern);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    }

    public final NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @SuppressWarnings("deprecation")
    public final Notification.Builder getNotifications(String title,
                                                       String body,
                                                       PendingIntent intent,
                                                       Uri soundUri,
                                                       int icon) {
        return new Notification.Builder(getApplicationContext(), ID)
                .setContentIntent(intent)
                .setSmallIcon(icon)
                .setSound(soundUri)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);

    }
}
