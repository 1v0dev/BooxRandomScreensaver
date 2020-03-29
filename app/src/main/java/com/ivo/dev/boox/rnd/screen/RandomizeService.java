package com.ivo.dev.boox.rnd.screen;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class RandomizeService extends Service {

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
        Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT).show();
        }
    }
}
