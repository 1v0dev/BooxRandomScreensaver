package com.ivo.dev.boox.rnd.screen;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.dirPathEdit)
    EditText dirPathEdit;

    SharedPreferences sharedPref;
    BroadcastReceiver broadcastReceiver;

    private static final int SEL_DIR_REQUEST_CODE = 2121;
    public static final String DIR_PREF_KEY = "sel_img_dir";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String path = sharedPref.getString(DIR_PREF_KEY, null);
        if (path != null) {
            dirPathEdit.setText(path);
        }
    }

    public void pickImgDir(View view) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(i, "Choose directory"), SEL_DIR_REQUEST_CODE);
    }

    public void manualRandomizeScreensaver(View view) {
        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(RandomizeWorker.class).build();
        WorkManager.getInstance(getApplicationContext()).enqueue(uploadWorkRequest);
    }

    public void startRandomizeSchedule(View view) {
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(RandomizeWorker.class).build();
                    WorkManager.getInstance(getApplicationContext()).enqueue(uploadWorkRequest);
                }
            };
            registerReceiver(broadcastReceiver, new IntentFilter("android.intent.action.USER_PRESENT"));
            Toast.makeText(getApplicationContext(), "Schedule started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Schedule already running", Toast.LENGTH_SHORT).show();
        }

    }

    public void stopRandomizeSchedule(View view) {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
            Toast.makeText(getApplicationContext(), "Schedule stopped", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "No running schedule", Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEL_DIR_REQUEST_CODE) {
            Uri uri = data.getData();
            if (uri != null && uri.getPath() != null) {
                File file = new File(uri.getPath());
                final String[] split = file.getPath().split(":");
                String path = "/" + split[1];
                dirPathEdit.setText(path);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(DIR_PREF_KEY, path);
                editor.apply();
            }
        }
    }
}
