package com.donotforget.user.donotforget.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by user on 02.08.2016.
 */
public class RestartService extends BroadcastReceiver {
    private static final String TAG = "Alex_" + RestartService.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"In RestartService");
        Intent serviceIntent = new Intent(context,AlarmService.class);

        serviceIntent.setAction(AlarmService.ACTION_CREATE);
        context.startService(serviceIntent);
    }
}
