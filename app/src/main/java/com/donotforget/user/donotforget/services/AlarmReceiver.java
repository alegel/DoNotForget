package com.donotforget.user.donotforget.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.donotforget.user.donotforget.NotificationActivity;
import com.donotforget.user.donotforget.R;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;

/**
 * Created by user on 02.08.2016.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public final static String BROADCAST_ACTION = "com.donotforget.user.donotforget.services.ALARM";
    private static final String TAG = "Alex_" + AlarmReceiver.class.getSimpleName();

    private Intent intent;
    private PendingIntent pendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(TAG,"In AlarmReceiver");
        Schedule schedule = new Schedule();
        if(intent.hasExtra(MyUsefulFuncs.SCHEDULE)) {
            schedule = (Schedule) intent.getSerializableExtra(MyUsefulFuncs.SCHEDULE);
        }
        if(schedule != null && schedule.getId() != 0){
            Log.d(TAG,"In AlarmReceiver, Schedule:");
            Log.d(TAG,"atTime: " + schedule.getAtTime() + ", Status: " + schedule.getStatus() + ", Text: " + schedule.getText());

            intent = new Intent(context, NotificationActivity.class);
            intent.setAction(AlarmReceiver.BROADCAST_ACTION + schedule.getId());
            intent.putExtra(MyUsefulFuncs.SCHEDULE,schedule);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_access_time_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(schedule.getText())
                    .setContentText(context.getResources().getString(R.string.dismiss_notification));

            Notification notification = builder.build();

            notification.defaults = Notification.DEFAULT_ALL;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.flags |= Notification.FLAG_NO_CLEAR;

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(schedule.getId(),notification);

        }
    }
}
