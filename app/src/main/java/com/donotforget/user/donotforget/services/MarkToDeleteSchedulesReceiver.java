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
import com.donotforget.user.donotforget.R;
import com.donotforget.user.donotforget.objects.ContactSchedule;
import com.donotforget.user.donotforget.objects.JSONParser;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 30.08.2016.
 */
public class MarkToDeleteSchedulesReceiver extends BroadcastReceiver {
    // Domain Name is:www.allreminders.co.nr
    public final static String BROADCAST_MARK_DELETE_SCHEDULES = "com.donotforget.user.donotforget.services.MARK_DELETE_SCHEDULES";
    private static String url_mark_delete_contact_schedule = "http://donotforget.info/deleteFromDB/markDeleteContactSchedule.php";
    private static String url_notif_delete_schedule = "http://donotforget.info/deleteFromDB/delete_schedule_notif.php";

//http://www.donotforget.info/

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
    private String errMessage = "";
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    private boolean status = false;
    private static final String TAG = "Alex_" + MarkToDeleteSchedulesReceiver.class.getSimpleName();

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

        if(intent.hasExtra(MyUsefulFuncs.DELETE_SCHEDULES)){
            delSchedulesID = intent.getIntegerArrayListExtra(MyUsefulFuncs.DELETE_SCHEDULES);
            if(delSchedulesID.size() > 0) {
                if(intent.hasExtra(MyUsefulFuncs.USER_PHONE)) {
                    phone = intent.getStringExtra(MyUsefulFuncs.USER_PHONE);
                    markToDeleteWebSchedules();
                }
            }
        }

        if(intent.hasExtra(MyUsefulFuncs.DELETE_SCHEDULE)){
            deleteScheduleID = intent.getIntExtra(MyUsefulFuncs.DELETE_SCHEDULE,-1);
            if(deleteScheduleID > 0){
                strDelScheduleID = String.format("%d_%s",deleteScheduleID,MyUsefulFuncs.myReg_ID);
                if(intent.hasExtra(MyUsefulFuncs.USER_PHONE)) {
                    phone = intent.getStringExtra(MyUsefulFuncs.USER_PHONE);
                    markToDeleteWebSchedule();
                }
            }
        }
    }

    private void notifyContactsToDeleteSchedule() {
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", phone));

         if(sendJson(url_notif_delete_schedule, params) == true){
             Log.d(TAG,"Notification to delete schedule sent successfully");
             showToast(context.getResources().getText(R.string.reminder_delete_successfully).toString());
         }
         else{
             Log.d(TAG,"Failed to send Notification to delete schedule with error: " + errMessage);
             showToast(context.getResources().getText(R.string.reminder_delete_error).toString());
         }
    }

    private void markToDeleteWebSchedules() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                for (int i = 0; i <delSchedulesID.size(); i++) {
                    params = new ArrayList<NameValuePair>();
                    strDelScheduleID = String.format("%d_%s",delSchedulesID.get(i),MyUsefulFuncs.myReg_ID);
                    params.add(new BasicNameValuePair("schedule_id", strDelScheduleID));
                    params.add(new BasicNameValuePair("phone", phone));

                    Log.d(TAG, "Params to markToDeleteWebSchedules:\n");
                    Log.d(TAG,"phone = " + phone);
                    Log.d(TAG,", Schedule_ID to markToDeleteWebSchedules = " + strDelScheduleID);

                    if(sendJson(url_mark_delete_contact_schedule, params) == true){
                        Log.d(TAG,"ContactSchedule on Web was marked successfully");
//                        showToast("ContactSchedule on Web was marked successfully");
                        notifyContactsToDeleteSchedule();
                    }
                    else{
                        Log.d(TAG,"Failed to mark the ContactSchedule on Web with error: " + errMessage);
                        showToast(context.getResources().getText(R.string.reminder_delete_error).toString());
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
            }

        }.execute();
    }

    private void markToDeleteWebSchedule() {
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("schedule_id", strDelScheduleID));
        params.add(new BasicNameValuePair("phone", phone));

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if(sendJson(url_mark_delete_contact_schedule, params) == true){
                    Log.d(TAG,"ContactSchedule on Web was marked successfully");
//                    showToast("ContactSchedule on Web was marked successfully");
                    notifyContactsToDeleteSchedule();
                }
                else{
                    Log.d(TAG,"Failed to mark the ContactSchedule on Web with error: " + errMessage);
                    showToast(context.getResources().getText(R.string.reminder_delete_error).toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
            }

        }.execute();

    }

    private boolean sendJson(String url, List<NameValuePair> params){
        JSONObject json = jsonParser.makeHttpRequest(url,"POST", params);
        if(json != null) {
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
            Log.d(TAG,"Failed to connect to DataBase");
            errMessage = "Failed to connect";
            showToast(context.getResources().getText(R.string.reminder_delete_error).toString());
            return false;
        }
        return false;
    }
}
