package com.ivo.dev.boox.rnd.screen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class RandomizeService extends Service {

    public static final String CHANNEL_ID = "RandomizeServiceChannel";
    public static final int ONGOING_NOTIFICATION_ID = 123;

    BroadcastReceiver broadcastReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(RandomizeWorker.class).build();
                WorkManager.getInstance(getApplicationContext()).enqueue(uploadWorkRequest);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("android.intent.action.USER_PRESENT"));

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle("Boox Random Screensaver")
                        .setContentText("Service running")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent)
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
        Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Randomize Service Channel",
                    NotificationManager.IMPORTANCE_MIN
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
