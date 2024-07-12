package com.vpn.supervpnfree;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.multidex.MultiDexApplication;


import java.util.ArrayList;

public class MainApp extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @SuppressLint("StaticFieldLeak")
    private static MainApp mAppInstance;

    public static Context getContext() {
        return context;
    }

    public static synchronized MainApp getAppInstance() {
        return mAppInstance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mAppInstance = this;
        context = this;
        initHydraSdk();
    }

    public void initHydraSdk() {
        createNotificationChannel();
    }




    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getResources().getString(R.string.app_name)+"";
            String description = getResources().getString(R.string.app_name)+""+getString(R.string.notify);
            int importance = NotificationManager.IMPORTANCE_MAX;
            @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel(getPackageName(), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
