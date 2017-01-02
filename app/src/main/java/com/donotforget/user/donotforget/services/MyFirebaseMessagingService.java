package com.donotforget.user.donotforget.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

/**
 * Created by user on 14.11.2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "Alex_" + MyFirebaseMessagingService.class.getSimpleName();
    String title, message;
    private AlarmManager alarmManager;
    private Intent intent;
    private PendingIntent pendingIntent;
    private SharedPreferences preferences;
    private String myPhoneNumber;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

//        showToast("MyFirebaseMessagingService:onMessageReceived()");
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG,"In MyFirebaseMessagingService.onMessageReceived, " + "From " + remoteMessage.getFrom());

        if(remoteMessage == null)
            return;
        try {
            preferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Impossible reading from the UserDetails file", Toast.LENGTH_LONG).show();
            return;
        }
        myPhoneNumber = preferences.getString(MyUsefulFuncs.USER_PHONE,"error");
        Log.d(TAG, "myPhoneNumber: " + myPhoneNumber);

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage);
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                Map<String,String> data = remoteMessage.getData();
                title = data.get("title");
                message = data.get("body");
                handleDataMessage(title, message);
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }

    }

    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleDataMessage(String title, String message) {

            Log.d(TAG, "title: " + title);
            Log.d(TAG, "message: " + message);

            if(title.isEmpty() || message.isEmpty())
                return;
/************* New User subscribed to DoNotForget ***********************************/
            if(title.equals("New user"))
            {
                    intent = new Intent(this,AddContactReceiver.class);
                    intent.setAction(AddContactReceiver.BROADCAST_ADD_CONTACT);
                    intent.putExtra(MyUsefulFuncs.USER_PHONE,message);

                    pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),pendingIntent);
                    Log.d(TAG,"Call to AddContactReciever from handleDataMessage");
            }
            else if(title.equals("New schedule")){
                if(message.equals(myPhoneNumber)){    // There is a schedule for me
                    intent = new Intent(this,AddScheduleReceiver.class);
                    intent.setAction(AddScheduleReceiver.BROADCAST_ADD_SCHEDULE);
                    intent.putExtra(MyUsefulFuncs.USER_PHONE,message);

                    pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),pendingIntent);
                    Log.d(TAG,"Call to AddScheduleReceiver from handleDataMessage");
                }
            }
            else if(title.equals("Delete schedule")){
                if(message.equals(myPhoneNumber)){    // There is a schedule to delete
                    intent = new Intent(this,DeleteSchedulesReceiver.class);
                    intent.setAction(DeleteSchedulesReceiver.BROADCAST_DELETE_SCHEDULES);
                    intent.putExtra(MyUsefulFuncs.USER_PHONE,message);

                    pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),pendingIntent);
                    Log.d(TAG,"Call to DeleteScheduleReceiver from handleDataMessage");
                }
            }
/******************************************************************************************/
    }

    private void handleNotification(RemoteMessage remoteMessage) {
        title =     remoteMessage.getNotification().getTitle();
        message =   remoteMessage.getNotification().getBody();
        Log.d(TAG,"Title = " + title + ", message = " + message);
        if(title.isEmpty() || message.isEmpty())
            return;
/************* New User subscribed to DoNotForget ***********************************/
        if(title.equals("New user"))
        {
            if(message.equals(MyUsefulFuncs.myPhoneNumber)){
                // At this stage I have gotten my token registered id in DataBase and
                // now subscribing to TOPIC_NEW_USER
                FirebaseMessaging.getInstance().subscribeToTopic(MyUsefulFuncs.TOPIC_NEW_USER);
            }
            else{
                // New contact subscribed to DoNotForget, check if his phone
                // exists in my contacts. If Yes - add him to SQLite DB
                intent = new Intent(this,AddContactReceiver.class);
                intent.setAction(AddContactReceiver.BROADCAST_ADD_CONTACT);
                intent.putExtra(MyUsefulFuncs.USER_PHONE,message);

                pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),pendingIntent);
                Log.d(TAG,"Call to AddContactReciever");
            }
        }
/******************************************************************************************/
/****************************************************************************
        if (!isAppIsInBackground()) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(MyUsefulFuncs.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", body);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
        }else{
            // If the app is in background, firebase itself handles the notification
        }
/****************************************************************************/
    }

    public boolean isAppIsInBackground() {
        Context context = getApplicationContext();
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}
