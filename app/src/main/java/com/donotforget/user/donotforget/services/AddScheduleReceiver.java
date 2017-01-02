package com.donotforget.user.donotforget.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContactSchedule;
import com.donotforget.user.donotforget.DBObjects.DBSchedule;
import com.donotforget.user.donotforget.objects.ContactSchedule;
import com.donotforget.user.donotforget.objects.JSONParser;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 28.08.2016.
 */
public class AddScheduleReceiver extends BroadcastReceiver {
    public final static String BROADCAST_ADD_SCHEDULE = "com.donotforget.user.donotforget.services.ADD_SCHEDULE";
    private static String url_add_schedule = "http://donotforget.info/getFromDB/schedules.php";
    private static String url_update_schedule_status = "http://donotforget.info/updateDB/updateSchedulesStatus.php";
    private Context context;
    private DBSchedule dbSchedule;
    private DBContactSchedule dbContactSchedule;
    private ArrayList<ContactSchedule> contactSchedulesList = new ArrayList<>();
    private ArrayList<Schedule> schedules = new ArrayList<>();
    private String strNewPhone;
    private JSONParser jsonParser = new JSONParser();
    private String errMessage = "", message = "";
    private ArrayList<Long> rowIdList;
    private long rowID = -1;
    private JSONObject jsonObject = new JSONObject();
    private static final String TAG = "Alex_" + AddScheduleReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
/*** Phone does not needed, My Phone should be used instead *************/
        if(intent.hasExtra(MyUsefulFuncs.USER_PHONE)){
            strNewPhone = intent.getStringExtra(MyUsefulFuncs.USER_PHONE);
        }
        if(strNewPhone == null || strNewPhone.isEmpty())
            return;
/**************************************************************************************/
        readSchedules();
    }

    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean sendJson(String url, List<NameValuePair> params){
        JSONObject json = jsonParser.makeHttpRequest(url,"POST", params);
        if(json != null) {
            jsonObject = json;
            Log.d(TAG, json.toString());
            try {
                int success = json.getInt("success");
                if (success == 1) {
                    return true;

                } else {
                    errMessage = json.getString("message");
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG,"Failed to send json with exception: " + e.toString());
            }
        }
        else{
            Log.d(TAG,"Failed to connect to schedules.php");
            errMessage = "Failed to connect";

//            showToast("Failed to connect to schedules.php");

            return false;
        }
        return false;
    }

    private void readSchedules() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG,"In AddScheduleReceiver, strNewPhone = " + strNewPhone);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("phone", strNewPhone));

                if(sendJson(url_add_schedule, params) == true){
                    Log.d(TAG, "Schedule added successfully");
                    JSONArray jsonArr = null;
                    try {
                        jsonArr = jsonObject.getJSONArray("schedules");
                        int numOfRaws = jsonArr.length();
                        String strDate;
                        if (numOfRaws > 0)  {
                            for (int i = 0; i < jsonArr.length(); i++) {
                                JSONObject jsonObj = jsonArr.getJSONObject(i);
                                Schedule schedule = new Schedule();

                                schedule.setRecurring(jsonObj.getInt("recurring"));

                                strDate = jsonObj.getString("onceDate");
                                if(strDate != null && !strDate.isEmpty() && !strDate.equalsIgnoreCase("null")) {
                                    strDate = strDate.replace("\\", "");
                                    schedule.setOnceDate(strDate, 0);
                                }

                                strDate = jsonObj.getString("fromDate");
                                if(strDate != null && !strDate.isEmpty()) {
                                    strDate = strDate.replace("\\", "");
                                    schedule.setFromDate(strDate, 0);
                                }

                                strDate = jsonObj.getString("toDate");
                                if(strDate != null && !strDate.isEmpty()) {
                                    strDate = strDate.replace("\\", "");
                                    schedule.setToDate(strDate, 0);
                                }
                                strDate = jsonObj.getString("onceTime");
                                Log.d(TAG,"strDate = " + strDate);
                                if(strDate != null && !strDate.isEmpty() && !strDate.equalsIgnoreCase("null"))
                                    schedule.setOnceTime(strDate);

                                schedule.setAtTime(jsonObj.getString("atTime"));

                                schedule.setPlayRingtone(jsonObj.getInt("playRing"));
                                schedule.setVibrate(jsonObj.getInt("vibrate"));
                                if(jsonObj.getString("weekDays") != null && !jsonObj.getString("weekDays").isEmpty() && !jsonObj.getString("weekDays").equalsIgnoreCase("null"))
                                    schedule.setWeekDays(jsonObj.getString("weekDays"));
                                if(jsonObj.getString("msgText") != null && !jsonObj.getString("msgText").isEmpty())
                                    schedule.setText(jsonObj.getString("msgText"));

                                if(jsonObj.getString("scheduleFrom") != null && !jsonObj.getString("scheduleFrom").isEmpty())
                                    schedule.setScheduleFrom(jsonObj.getString("scheduleFrom"));

                                schedule.setStatus("active");

                                schedules.add(schedule);

                            }
                            if(addToDataBase(schedules) == true){
                                Log.d(TAG,"The Schedules from the Web added successfully");
/******************************************************************************************************************/
                /*** Send JSON to update the schedules's status on WEB, for schedule, that were read **************************************/
/******************************************************************************************************************/
                                params = new ArrayList<NameValuePair>();
                                params.add(new BasicNameValuePair("status", "old"));
                                params.add(new BasicNameValuePair("phone", strNewPhone));
                                if(sendJson(url_update_schedule_status, params) == true){
                                    Log.d(TAG,"Schedules status on Web were updated successfully");
                                }
                                else{
                                    Log.d(TAG,"Failed to update schedules status on Web with error: " + errMessage);

//                                    showToast("Failed to update the schedules status on Web");
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.d(TAG, "Failed to add contactSchedule: " + errMessage);

//                    showToast("Failed to add contactSchedule");

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

//                showToast("Send message to AlarmService");
/*** There were new schedules added, check for today's notifications and if needed - set Alarms ******/
                Intent serviceIntent = new Intent(context,AlarmService.class);
                serviceIntent.setAction(AlarmService.ACTION_CREATE);
                context.startService(serviceIntent);
/*************************************************************************************************/
            }
        }.execute();
    }

    private boolean addToDataBase(final ArrayList<Schedule> schedules) {
        dbSchedule = new DBSchedule(context);
        dbContactSchedule = new DBContactSchedule(context);
        ContactSchedule contactSchedule = new ContactSchedule();
        final ArrayList<ContactSchedule> NewContactSchedulesList = new ArrayList<>();

        contactSchedule = new ContactSchedule();
        contactSchedule.setSchedule_owner(MyUsefulFuncs.REMIND_TO_MYSELF);
        contactSchedule.setIsSent(1);
        contactSchedulesList.add(contactSchedule);

        rowIdList = dbSchedule.addToDataBase(schedules);
        if(rowIdList.size() > 0){
            for (int i = 0; i <contactSchedulesList.size(); i++) {
                for (int j = 0; j <rowIdList.size(); j++) {
                    ContactSchedule NewContactSchedule = new ContactSchedule();
                    NewContactSchedule.Copy(contactSchedulesList.get(i));
                    NewContactSchedule.setSchedule_id(rowIdList.get(j));
                    NewContactSchedulesList.add(NewContactSchedule);
                }
            }
            rowIdList = dbContactSchedule.addToDataBase(NewContactSchedulesList);
            if(rowIdList.size() > 0) {
                return true;
            }
        }
        return false;
    }
}
