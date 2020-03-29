package com.ivo.dev.boox.rnd.screen;

import com.onyx.android.sdk.utils.BitmapUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class RandomizeWorker extends Worker {

    private static final String STORAGE_BASE_PATH = "/storage/emulated/0";
    private static String[] standbyFileNames = new String[] {"standby-1.png", "standby-2.png", "standby-3.png"};

    public RandomizeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String path = sharedPref.getString(MainActivity.DIR_PREF_KEY, null);

        if (path == null) {
            backgroundToast(getApplicationContext(), "Fail to randomize screensaver. No image dir selected.");
            return Result.failure();
        }

        File dir = new File(STORAGE_BASE_PATH + path);
        if (dir.exists()) {
            try {
                File[] allImgs = dir.listFiles();
                int randomNumber = new Random().nextInt(allImgs.length);
                File img = allImgs[randomNumber];

                String targetDir = "/data/local/assets/images" + File.separator;

                //set file 1
                String targetPathString = targetDir + standbyFileNames[0];
                Bitmap temp = BitmapFactory.decodeFile(img.getAbsolutePath());
                if (temp.getHeight() > temp.getWidth()) {
                    temp = BitmapUtils.rotateBmp(temp, 270);
                }
                BitmapUtils.savePngToFile(temp, targetDir, targetPathString, true);

                //copy to the other files
                File first = new File(targetPathString);
                copy(first, new File(targetDir + standbyFileNames[1]));
                copy(first, new File(targetDir + standbyFileNames[2]));

                Intent intent = new Intent("update_standby_pic");
                getApplicationContext().sendBroadcast(intent);

                backgroundToast(getApplicationContext(), "Screensaver randomized");
            } catch (Exception e) {
                backgroundToast(getApplicationContext(),
                        "No files found. Make sure you enabled storage access permission for the app!");
            }
        } else {
            backgroundToast(getApplicationContext(), "The image directory does not exist");
        }

        return Result.success();
    }

    public static void backgroundToast(final Context context, final String msg) {
        if (context != null && msg != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
}
