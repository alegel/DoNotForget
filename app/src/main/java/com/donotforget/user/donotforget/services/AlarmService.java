package com.donotforget.user.donotforget.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.donotforget.user.donotforget.DBObjects.DBContactSchedule;
import com.donotforget.user.donotforget.DBObjects.DBSchedule;
import com.donotforget.user.donotforget.objects.JSONParser;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class AlarmService extends IntentService {
    private AlarmManager alarmManager;
    private Intent intent;
    private DBSchedule dbSchedule;
    private DBContactSchedule dbContactSchedule;
    private Context context;
    private ArrayList<Integer> delSchedulesID = new ArrayList<>();
    private PendingIntent pendingIntent;
    public static final String ACTION_CREATE = "com.donotforget.user.donotforget.services.action.CREATE";
    public static final String ACTION_CANCEL = "com.donotforget.user.donotforget.services.action.CANCEL";

    private static final String TAG = "Alex_" + AlarmService.class.getSimpleName();

    // TODO: Rename parameters
    public static final String SCHEDULE_ID = "com.donotforget.user.donotforget.services.extra.SCHEDULE_ID";
    public static final String EXTRA_PARAM2 = "com.donotforget.user.donotforget.services.extra.PARAM2";

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE.equals(action)) {
                final String param1 = intent.getParcelableExtra(SCHEDULE_ID);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionCreate();
            } else if (ACTION_CANCEL.equals(action)) {
                final int schedule_id = intent.getIntExtra(SCHEDULE_ID,0);
                final Schedule schedule = (Schedule) intent.getSerializableExtra(MyUsefulFuncs.SCHEDULE);
                handleActionCancel(schedule_id, schedule);
            }

            try {
                CheckSchedulesForToday();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void DeleteOldSchedules(final Calendar cal){
        context = getApplicationContext();
        dbContactSchedule = new DBContactSchedule(context);
        dbSchedule = new DBSchedule(context);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                delSchedulesID = dbSchedule.DelOldSchedules(cal);
                if(delSchedulesID.size() > 0){
                    dbContactSchedule.DelOldSchedules(delSchedulesID);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(delSchedulesID.size() > 0) {
                    intent = new Intent(context, DeleteSchedulesReceiver.class);
                    intent.setAction(DeleteSchedulesReceiver.BROADCAST_DELETE_SCHEDULES);
                    intent.putExtra(MyUsefulFuncs.DELETE_SCHEDULES,delSchedulesID);

                    pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + 5000,pendingIntent);
                }
            }
        }.execute();

    }

    private void CheckSchedulesForToday() throws ParseException {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent notifyIntent = new Intent(this,AlarmService.class);
        notifyIntent.setAction(AlarmService.ACTION_CREATE);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
/*** Add One Day to the Current Day in order to wake up the Service tomorrow at 00"00:10 ***/
        Calendar cal = Calendar.getInstance();
        try {
            calendar = MyUsefulFuncs.addDate(cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.MONTH),cal.get(Calendar.YEAR), Calendar.DAY_OF_MONTH,1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
 /*************************************************************************************/
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,10);
        calendar.set(Calendar.MILLISECOND,0);

        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

        if(calendar.get((Calendar.DAY_OF_MONTH)) == 1){
            DeleteOldSchedules(cal);
        }



    }

    /**
     * Handle action CHECK_DB in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCreate() {
        //Log.d(TAG,"In AlarmService, before reading Schedules from DataBase");
        final DBSchedule dbSchedule = new DBSchedule(this);
        ArrayList<Schedule> schedules = new ArrayList<>();
        Schedule schedule;
        schedules = dbSchedule.ReadMyNextSchedule();
        for (int i = 0; i <schedules.size(); i++) {
            schedule = new Schedule();
            schedule = schedules.get(i);
            if (schedule != null && schedule.getId() != 0) {
//                intent = new Intent(AlarmReceiver.BROADCAST_ACTION);
                intent = new Intent(this,AlarmReceiver.class);                          // new
                intent.setAction(AlarmReceiver.BROADCAST_ACTION + schedule.getId());    // new

                intent.putExtra(MyUsefulFuncs.SCHEDULE, schedule);
                intent.putExtra(dbSchedule.COL_ID, schedule.getId());
                Calendar cal = Calendar.getInstance();
                String[] time = MyUsefulFuncs.SplitString(schedule.getAtTime(), ":");
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                cal.set(Calendar.SECOND, Integer.parseInt(time[2]));
                cal.set(Calendar.MILLISECOND, 0);

                Log.d(TAG, "AlarmService: handleActionCreate(): The date of Alarm: " + MyUsefulFuncs.DateToString(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));
                Log.d(TAG, "AlarmService: handleActionCreate(): The Time of Alarm: " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
                Log.d(TAG, "AlarmService: handleActionCreate(): Schedule_ID: " + schedule.getId());

                pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
//        alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + 5000,pendingIntent);
            }
        }
/******************************************************************************************************/
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCancel(int schedule_id, Schedule schedule) {
        // In order to Cancel the Alarm - its intent must be the same as
        // it was when this Alarm was set (The same "Action" and "Data", but not Extra Data)
        intent = new Intent(this,AlarmReceiver.class);                          // new
        intent.setAction(AlarmReceiver.BROADCAST_ACTION + schedule.getId());    // new
        intent.putExtra(MyUsefulFuncs.SCHEDULE,schedule);
        intent.putExtra(DBSchedule.COL_ID,schedule_id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(TAG,"Cancel Alarm - Schedule_id: " + schedule_id + ", Schedule: " + schedule.toString());
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

 //       handleActionCreate();
    }
}
