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
 * Created by user on 30.08.2016.
 */
public class DeleteSchedulesReceiver extends BroadcastReceiver {
    public final static String BROADCAST_DELETE_SCHEDULES = "com.donotforget.user.donotforget.services.DELETE_SCHEDULES";
    private static String url_delete_contact_schedule = "http://donotforget.info/deleteFromDB/deleteContactSchedule.php";
    private static String url_delete_schedule = "http://donotforget.info/deleteFromDB/deleteSchedule.php";
    private static String url_get_marked_schedule = "http://donotforget.info/getFromDB/getMarkedSchedules.php";
    private static String url_get_num_schedule = "http://donotforget.info/getFromDB/getNumSchedulesToDelete.php";
    private static final String TAG = "Alex_" + DeleteSchedulesReceiver.class.getSimpleName();

    private Context context;
    private DBSchedule dbSchedule;
    private DBContactSchedule dbContactSchedule;
    private ArrayList<ContactSchedule> contactSchedulesList = new ArrayList<>();
    private ArrayList<Schedule> schedules = new ArrayList<>();
    private ArrayList<Integer> delSchedulesID = new ArrayList<>();
    private int deleteScheduleID = -1;
    private String strDelScheduleID;
    private String phone;
    private JSONParser jsonParser = new JSONParser();
    private String errMessage = "", message = "";
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
  //  private String msg;
    private ArrayList<String> strScheduleIDs = new ArrayList<>();
    private JSONObject jsonObject = new JSONObject();

    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(intent.hasExtra(MyUsefulFuncs.USER_PHONE)) {
            phone = intent.getStringExtra(MyUsefulFuncs.USER_PHONE);
            if(phone != null && !phone.isEmpty()){
                deleteMarkedSchedules();
            }
        }
        else if(intent.hasExtra(MyUsefulFuncs.DELETE_SCHEDULES)){
            DeleteOldSchedules(intent.getIntegerArrayListExtra(MyUsefulFuncs.DELETE_SCHEDULES));
        }
    }

    private void deleteMarkedSchedules() {
//        showToast("Going to delete marked schedules");
        dbSchedule = new DBSchedule(context);
        dbContactSchedule = new DBContactSchedule(context);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG, "DeleteSchedulesReceiver");
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("phone", phone));

                if (sendJson(url_get_marked_schedule, params) == true) {
//                    showToast("contactSchedule received successfully");
                    Log.d(TAG, "contactSchedule received successfully");
                    JSONArray jsonArr = null;
                    try {
                        jsonArr = jsonObject.getJSONArray("schedules");
                        int numOfRaws = jsonArr.length();
                        String strDate, strScheduleID;
                        if (numOfRaws > 0) {
                            for (int i = 0; i < jsonArr.length(); i++) {
                                JSONObject jsonObj = jsonArr.getJSONObject(i);
                                Schedule schedule = new Schedule();

                                strScheduleID = jsonObj.getString("schedule_id");
                                strScheduleIDs.add(strScheduleID);

                                schedule.setRecurring(jsonObj.getInt("recurring"));

                                strDate = jsonObj.getString("onceDate");
                                if (strDate != null && !strDate.isEmpty() && !strDate.equalsIgnoreCase("null")) {
                                    strDate = strDate.replace("\\", "");
                                    schedule.setOnceDate(strDate, 0);
                                }

                                strDate = jsonObj.getString("fromDate");
                                if (strDate != null && !strDate.isEmpty()) {
                                    strDate = strDate.replace("\\", "");
                                    schedule.setFromDate(strDate, 0);
                                }

                                strDate = jsonObj.getString("toDate");
                                if (strDate != null && !strDate.isEmpty()) {
                                    strDate = strDate.replace("\\", "");
                                    schedule.setToDate(strDate, 0);
                                }
                                strDate = jsonObj.getString("onceTime");
                                Log.d(TAG, "strDate = " + strDate);
                                if (strDate != null && !strDate.isEmpty() && !strDate.equalsIgnoreCase("null"))
                                    schedule.setOnceTime(strDate);

                                schedule.setAtTime(jsonObj.getString("atTime"));

                                schedule.setPlayRingtone(jsonObj.getInt("playRing"));
                                schedule.setVibrate(jsonObj.getInt("vibrate"));
                                if (jsonObj.getString("weekDays") != null && !jsonObj.getString("weekDays").isEmpty() && !jsonObj.getString("weekDays").equalsIgnoreCase("null"))
                                    schedule.setWeekDays(jsonObj.getString("weekDays"));
                                if (jsonObj.getString("msgText") != null && !jsonObj.getString("msgText").isEmpty())
                                    schedule.setText(jsonObj.getString("msgText"));
                                if(jsonObj.getString("scheduleFrom") != null && !jsonObj.getString("scheduleFrom").isEmpty())
                                    schedule.setScheduleFrom(jsonObj.getString("scheduleFrom"));
                                schedule.setStatus("active");

                                schedules.add(schedule);

                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
/**************************************************************************************/
                    delSchedulesID = dbSchedule.ReadSelectedSchedulesForWeb(schedules.get(0));
                    if (delSchedulesID.size() > 0) {
                        dbContactSchedule.deleteSelectedSchedules(delSchedulesID, 0, "");
                        dbSchedule = new DBSchedule(context);
                        for (int i = 0; i < delSchedulesID.size(); i++) {
                            dbSchedule.deleteFromDataBase(delSchedulesID.get(i));
                        }

                        deleteWebSchedules();
                    }
/**************************************************************************************/
                } else {
//                    showToast(context.getResources().getText(R.string.reminder_delete_error).toString());
                    Log.d(TAG, "Failed to receive marked Schedules: " + errMessage);

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void DeleteOldSchedules(final ArrayList<Integer> integerArrayListExtra) {
        phone = MyUsefulFuncs.myPhoneNumber;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                for (int i = 0; i <integerArrayListExtra.size(); i++) {
                    params = new ArrayList<NameValuePair>();
                    strDelScheduleID = String.valueOf(integerArrayListExtra.get(i) + MyUsefulFuncs.myReg_ID);
                    params.add(new BasicNameValuePair("schedule_id", strDelScheduleID));
                    params.add(new BasicNameValuePair("phone", phone));

                    if(sendJson(url_delete_contact_schedule, params) == true){
                        Log.d(TAG,"ContactSchedule on Web was deleted successfully");
                        UpdateSchedulestable(strDelScheduleID);
                    }
                    else{
                        Log.d(TAG,"Failed to delete the ContactSchedule on Web with error: " + errMessage);
                    }
                }
                return null;
            }
        }.execute();
    }

    private void deleteWebSchedules() {
        for (int i = 0; i <strScheduleIDs.size(); i++) {
            params = new ArrayList<NameValuePair>();
            strDelScheduleID = strScheduleIDs.get(i);
            params.add(new BasicNameValuePair("schedule_id", strDelScheduleID));
            params.add(new BasicNameValuePair("phone", phone));

            Log.d(TAG,"Delete from ContactSchedules WHERE phone = " + phone + " AND schedule_id = \n" + strDelScheduleID);

            if(sendJson(url_delete_contact_schedule, params) == true){
              //  showToast("ContactSchedule on Web was deleted successfully");
                Log.d(TAG,"ContactSchedule on Web was deleted successfully");
                UpdateSchedulestable(strDelScheduleID);
            }
            else{
//                showToast(context.getResources().getText(R.string.reminder_delete_error).toString());
                Log.d(TAG,"Failed to delete the ContactSchedule on Web with error: " + errMessage);
            }
        }
    }

    private void UpdateSchedulestable(String deleteScheduleID) {
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("schedule_id", strDelScheduleID));
        params.add(new BasicNameValuePair("phone", phone));

        if(sendJson(url_get_num_schedule, params) == true){
            Log.d(TAG,"Delete from Schedule Table WHERE schedule_ID = " + strDelScheduleID);
            if(sendJson(url_delete_schedule, params) == true){
//                showToast("Schedule on Web was deleted successfully");
                Log.d(TAG,"Schedule on Web was deleted successfully");
            }
            else{
//                showToast(context.getResources().getText(R.string.reminder_delete_error).toString());
                Log.d(TAG,"Failed to delete the schedule on Web with error: " + errMessage);
            }
        }
        else{
//            showToast(context.getResources().getText(R.string.reminder_delete_error).toString());
            Log.d(TAG,"Failed to find the schedule on Web with error: " + errMessage);
        }
    }

    private boolean sendJson(String url, List<NameValuePair> params){
        JSONObject json = jsonParser.makeHttpRequest(url,"POST", params);
        if(json != null) {
            Log.d(TAG, json.toString());
            jsonObject = json;
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
//                showToast("Failed to send data to the Web");
            }
        }
        else{
            Log.d(TAG,"Failed to connect to getMarkedSchedules.php");
//            showToast("Failed to connect to the server");
            errMessage = "Failed to connect";
            return false;
        }
        return false;
    }
}
