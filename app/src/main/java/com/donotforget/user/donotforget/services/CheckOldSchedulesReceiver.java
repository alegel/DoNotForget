package com.donotforget.user.donotforget.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContactSchedule;
import com.donotforget.user.donotforget.DBObjects.DBSchedule;
import com.donotforget.user.donotforget.R;
import com.donotforget.user.donotforget.ShowFailedSchedulesActivity;
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

public class CheckOldSchedulesReceiver extends BroadcastReceiver {
    private static final String TAG = "Alex_" + CheckOldSchedulesReceiver.class.getSimpleName();
    public final static String BROADCAST_CHECK_OLD_SCHEDULES = "com.donotforget.user.donotforget.services.CHECK_OLD_SCHEDULES";
    private static String url_getUpdatedContactSchedule = "http://donotforget.info/getFromDB/getNewSchedules.php";

    private JSONParser jsonParser = new JSONParser();
    private String errMessage = "";
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private DBSchedule dbSchedule;
    private DBContactSchedule dbContactSchedule;
    private ArrayList<ContactSchedule> contactSchedulesList = new ArrayList<>();
    private ArrayList<ContactSchedule> goodContactSchedulesList = new ArrayList<>();
    private ArrayList<ContactSchedule> badContactSchedulesList = new ArrayList<>();
    private ArrayList<ContactSchedule> ContactSchedulesToCheckLater = new ArrayList<>();
    private ArrayList<ContactSchedule> ContactSchedulesListToUpdate = new ArrayList<>();
    private ArrayList<Schedule> schedulesList = new ArrayList<>();
    private List<NameValuePair> params;


    public CheckOldSchedulesReceiver() {
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

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        if(intent.getAction().equals(BROADCAST_CHECK_OLD_SCHEDULES)) {
            if (intent.hasExtra(MyUsefulFuncs.CONTACT_SCHEDULES)) {
                contactSchedulesList = (ArrayList<ContactSchedule>) intent.getSerializableExtra(MyUsefulFuncs.CONTACT_SCHEDULES);

                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        handleActionCHECK_OLD_SCHEDULES();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        }
    }

    private void handleActionCHECK_OLD_SCHEDULES() {
        Log.d(TAG,"In handleActionCHECK_OLD_SCHEDULES");

        Log.d(TAG,"contactSchedulesList.size() = " + contactSchedulesList.size());

        getNumOfNotUpdatedContactSchedules();

        if (ContactSchedulesListToUpdate.size() > 0) { // For saved in WEB contactSchedules isSent should be updated to '1'
            dbContactSchedule = new DBContactSchedule(context);
            if (dbContactSchedule.updateContactSchedules(ContactSchedulesListToUpdate) <= 0) {
                Log.d(TAG, "Some of contactSchedules were NOT Updated: ");
            }
        }

        if(contactSchedulesList != null && contactSchedulesList.size() > 0){
            for (int i = 0; i < contactSchedulesList.size(); i++) {
                badContactSchedulesList.add(contactSchedulesList.get(i));
            }

            Log.d(TAG," Call to handleFailedSchedules");
            handleFailedSchedules();
        }
    }

    private void handleFailedSchedules(){
        if(badContactSchedulesList != null && badContactSchedulesList.size() > 0){
/***************************** CREATE NOTIFICATION *******************************************************/
            Intent intent = new Intent(context, ShowFailedSchedulesActivity.class);
            intent.setAction(SendToWebService.ACTION_SEND_SCHEDULE + badContactSchedulesList.get(0).getId());

            intent.putExtra(MyUsefulFuncs.CONTACT_SCHEDULES, badContactSchedulesList);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_access_time_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(context.getString(R.string.send_schedules_err))
                    .setContentText(context.getResources().getString(R.string.dismiss_notification));

            Notification notification = builder.build();

            notification.defaults = Notification.DEFAULT_ALL;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.flags |= Notification.FLAG_NO_CLEAR;

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify((int) badContactSchedulesList.get(0).getId(),notification);
/***************************** CREATE NOTIFICATION *******************************************************/

            for (int i = 0; i < badContactSchedulesList.size(); i++) {    // Schedules, which were no saved in Web
                dbContactSchedule = new DBContactSchedule(context);
                if(dbContactSchedule.deleteFromDataBase(badContactSchedulesList.get(i).getSchedule_id(), badContactSchedulesList.get(i).getContactName()) == false){
                    Log.d(TAG, "Failed to delete contactSchedule : " + badContactSchedulesList.get(i).toString());
                }
            }

//            showToast(context.getResources().getText(R.string.add_notification_err).toString());

        }
    }

    private boolean getNumOfNotUpdatedContactSchedules() {
        ArrayList<ContactSchedule> tempCSList = new ArrayList<>();
        tempCSList.addAll(contactSchedulesList);

        boolean isUpdated = true;
        for (int i = 0; i < tempCSList.size(); i++) {
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("phone", tempCSList.get(i).getPhone()));

            Log.d(TAG, "Params to Verify updated ContactSchedules:\n");
            Log.d(TAG, "phone = " + tempCSList.get(i).getPhone());

            if (sendJson(url_getUpdatedContactSchedule, params) == false) {
                Log.d(TAG, "The " + tempCSList.get(i).getPhone() + " Was NOT UPDATED");
                isUpdated = false;
            }
            else {
                Log.d(TAG, "The user " + tempCSList.get(i).getPhone() + " was updated successfully");
                ContactSchedulesListToUpdate.add(tempCSList.get(i));
                int index = contactSchedulesList.indexOf(tempCSList.get(i));
                contactSchedulesList.remove(index);
            }
        }
        return isUpdated;
    }

    private boolean sendJson(String url, List<NameValuePair> params){
        JSONObject json = jsonParser.makeHttpRequest(url,"POST", params);
        if(json != null) {
            Log.d(TAG, "In sendJson: " + json.toString());
            try {
                int success = json.getInt("success");
                if (success == 1) {
                    return true;

                } else {
                    errMessage = json.getString("message");
                    Log.d(TAG,"sendJson error message: " + errMessage);
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG,"Failed to send json with exception: " + e.toString());

            }
        }
        else{
            Log.d(TAG,"Failed to connect to server");
            showToast("Failed to connect to the Server");
            errMessage = "Failed to connect";
            return false;
        }
        return false;
    }
}
