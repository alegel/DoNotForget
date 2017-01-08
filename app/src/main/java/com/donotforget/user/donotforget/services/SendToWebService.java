package com.donotforget.user.donotforget.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContactSchedule;
import com.donotforget.user.donotforget.DBObjects.DBContacts;
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


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class SendToWebService extends IntentService {
    public static final String ACTION_ADD_USER = "com.donotforget.user.donotforget.services.action.ADD_USER";
    public static final String ACTION_SEND_SCHEDULE = "com.donotforget.user.donotforget.services.action.SEND_SCHEDULE";
    private static String url_add_user = "http://donotforget.info/addToDB/users.php";
    private static String url_add_contactSchedule = "http://donotforget.info/addToDB/contactSchedule.php";
    private static String url_getUpdatedContactSchedule = "http://donotforget.info/getFromDB/getNewSchedules.php";
    private static String url_add_schedule = "http://donotforget.info/addToDB/schedule.php";
    private static String url_notif_add_schedule = "http://donotforget.info/addToDB/new_schedule_notif.php";
    private static final String TAG = "Alex_" + SendToWebService.class.getSimpleName();
    public static final String EXTRA_PARAM1 = "com.donotforget.user.donotforget.services.extra.PARAM1";
    public static final String EXTRA_PARAM2 = "com.donotforget.user.donotforget.services.extra.PARAM2";

    private DBSchedule dbSchedule;
    private DBContactSchedule dbContactSchedule;
    private ArrayList<ContactSchedule> contactSchedulesList = new ArrayList<>();
    private ArrayList<ContactSchedule> goodContactSchedulesList = new ArrayList<>();
    private ArrayList<ContactSchedule> badContactSchedulesList = new ArrayList<>();
    private ArrayList<ContactSchedule> ContactSchedulesListToUpdate = new ArrayList<>();
    private ArrayList<Schedule> schedulesList = new ArrayList<>();
    private List<NameValuePair> params;

    private JSONParser jsonParser = new JSONParser();
    private String errMessage = "";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public SendToWebService() {
        super("SendToWebService");
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


    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ADD_USER.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionAddUser(param1, param2);
            } else if (ACTION_SEND_SCHEDULE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionSEND_SCHEDULE(param1, param2);
            }
        }
    }

    /**
     * Handle action ADD_USER in the provided background thread with the provided
     * parameters.
     */
    private void handleActionAddUser(String param1, String param2) {
        Log.d(TAG,"In SendToWebService->handleActionAddUser");
        if(MyUsefulFuncs.myName.isEmpty() || MyUsefulFuncs.myPhoneNumber.isEmpty() || MyUsefulFuncs.myReg_ID.isEmpty()) {
            Log.d(TAG, "Failed to add user.The required parameters are empty");
            showToast(getResources().getString(R.string.database_err));
            DeleteDetailsFromSharedPreferences();
            return;
        }
        MyUsefulFuncs.registered = 1;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", MyUsefulFuncs.myName));
        params.add(new BasicNameValuePair("phone", MyUsefulFuncs.myPhoneNumber));
        params.add(new BasicNameValuePair("server_id", MyUsefulFuncs.myReg_ID));

        if(sendJson(url_add_user, params) == true){
            Log.d(TAG, "User added successfully to DataBase ");
            showToast(getResources().getString(R.string.phone_registered));

        }
        else{
            Log.d(TAG, "Failed to add user. errMessage = " + errMessage);
            showToast(getResources().getString(R.string.database_err));
            DeleteDetailsFromSharedPreferences();
        }
    }

    private void DeleteDetailsFromSharedPreferences() {
        final DBContacts dbContacts = new DBContacts(this);
        dbContacts.deleteAllContacts();

        try {
            preferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Impossible reading from the UserDetails file",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            editor = preferences.edit()
                    .putString(MyUsefulFuncs.USER_NAME, "error")
                    .putString(MyUsefulFuncs.USER_PHONE, "error");
            editor.commit();

            MyUsefulFuncs.myName = "";
            MyUsefulFuncs.myPhoneNumber = "";
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Impossible writing to the UserDetails file",Toast.LENGTH_LONG).show();
            return;
        }
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

    private void sendContactSchedules(){
        for (int j = 0; j <contactSchedulesList.size(); j++) {
            params = new ArrayList<NameValuePair>();
            String strSchedule_ID = String.format("%d_%s",contactSchedulesList.get(j).getSchedule_id(),MyUsefulFuncs.myReg_ID);
            params.add(new BasicNameValuePair("schedule_id", strSchedule_ID));
            params.add(new BasicNameValuePair("fromPhone", MyUsefulFuncs.myPhoneNumber));
            params.add(new BasicNameValuePair("phone", contactSchedulesList.get(j).getPhone()));

            Log.d(TAG, "Params to ContactSchedules:\n");
            Log.d(TAG,"fromPhone = " + MyUsefulFuncs.myPhoneNumber + ", To phone = " + contactSchedulesList.get(j).getPhone());
//            Log.d(TAG,"Schedule_ID to ContactSchedule = " + strSchedule_ID);

            if(sendJson(url_add_contactSchedule, params) == true){
                Log.d(TAG, "contactSchedule added successfully");
//                showToast("contactSchedule added successfully");
                goodContactSchedulesList.add(contactSchedulesList.get(j));
            }
            else{
                Log.d(TAG, "Failed to add contactSchedule: " + errMessage);
//                showToast(getResources().getText(R.string.add_db_err).toString());
                badContactSchedulesList.add(contactSchedulesList.get(j));
            }
        }
    }
    private boolean sendSchedules(){
        Schedule schedule;
        for ( int i = 0; i < schedulesList.size(); i++) {
            schedule = new Schedule();
            schedule = schedulesList.get(i);
            if (schedule != null && schedule.getId() != 0 && goodContactSchedulesList.size() > 0) {
                params = new ArrayList<NameValuePair>();
                String strSchedule_ID = String.format("%d_%s",schedule.getId(),MyUsefulFuncs.myReg_ID);
                params.add(new BasicNameValuePair("schedule_id", strSchedule_ID));
                params.add(new BasicNameValuePair("fromDate", schedule.getFromDate()));
                params.add(new BasicNameValuePair("toDate", schedule.getToDate()));
                params.add(new BasicNameValuePair("atTime", schedule.getAtTime()));
                params.add(new BasicNameValuePair("msgText", schedule.getText()));
                params.add(new BasicNameValuePair("scheduleFrom", schedule.getScheduleFrom()));
                params.add(new BasicNameValuePair("status", schedule.getStatus()));
                params.add(new BasicNameValuePair("onceDate", schedule.getOnceDate()));
                params.add(new BasicNameValuePair("onceTime", schedule.getOnceTime()));
                params.add(new BasicNameValuePair("weekDays", schedule.getDaysOfWeek()));
                params.add(new BasicNameValuePair("playRing", String.valueOf(schedule.getPlayRingtone())));
                params.add(new BasicNameValuePair("vibrate", String.valueOf(schedule.getVibrate())));
                params.add(new BasicNameValuePair("recurring", String.valueOf(schedule.getRecurring())));

//                Log.d(TAG,"Schedule_ID to Schedule = " + strSchedule_ID);

                if(sendJson(url_add_schedule, params) == true){
//                    showToast("Schedule added successfully");
                    Log.d(TAG, "Schedule added successfully");
                }
                else{
                    Log.d(TAG, "Failed to add Schedule");
                    showToast(getResources().getText(R.string.add_db_err).toString());
                    return false;
                }
            }
        }
        return true;
    }

    private void handleFailedSchedules(){
        for (int i = 0; i < badContactSchedulesList.size(); i++) {    // Schedules, which were no saved in Web
            dbContactSchedule = new DBContactSchedule(this);
            if(dbContactSchedule.deleteFromDataBase(badContactSchedulesList.get(i).getSchedule_id(), badContactSchedulesList.get(i).getContactName()) == false){
                Log.d(TAG, "Failed to delete contactSchedule : " + badContactSchedulesList.get(i).toString());
            }
        }
        if(badContactSchedulesList != null && badContactSchedulesList.size() > 0){
/***************************** CREATE NOTIFICATION *******************************************************/
            Intent intent = new Intent(getApplicationContext(), ShowFailedSchedulesActivity.class);
            intent.setAction(SendToWebService.ACTION_SEND_SCHEDULE + badContactSchedulesList.get(0).getId());

            intent.putExtra(MyUsefulFuncs.CONTACT_SCHEDULES, badContactSchedulesList);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Builder builder = new Notification.Builder(getApplicationContext());
            builder.setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_access_time_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.mipmap.ic_launcher))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(getResources().getString(R.string.send_schedules_err))
                    .setContentText(getApplicationContext().getResources().getString(R.string.dismiss_notification));

            Notification notification = builder.build();

            notification.defaults = Notification.DEFAULT_ALL;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.flags |= Notification.FLAG_NO_CLEAR;

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify((int) badContactSchedulesList.get(0).getId(),notification);
/***************************** CREATE NOTIFICATION *******************************************************/
        }
    }

    private void handleActionSEND_SCHEDULE(String param1, String param2) {
        dbSchedule = new DBSchedule(this);
        dbContactSchedule = new DBContactSchedule(this);
        contactSchedulesList = new ArrayList<>();
        goodContactSchedulesList = new ArrayList<>();
        badContactSchedulesList = new ArrayList<>();
        schedulesList = new ArrayList<>();
        Schedule schedule;
        contactSchedulesList = dbContactSchedule.ReadContactsWeb();
        schedulesList = dbSchedule.ReadContactsScheduleWeb();
        boolean flag = false;

//        showToast("In handleActionSEND_SCHEDULE");

        sendContactSchedules(); // Failed SContactSchedules will be written to the badContactSchedulesList
        if(errMessage.equals("Failed to connect")){
            showToast(getResources().getString(R.string.web_connect_err));
            return;
        }

        if(sendSchedules() == false){
            showToast(getResources().getText(R.string.add_db_err).toString());
            return;
        }

        Log.d(TAG,"The number of badContactSchedules is: " + badContactSchedulesList.size());
        Log.d(TAG,"The number of goodContactSchedules is: " + goodContactSchedulesList.size());

        for (int i = 0; i <5; i++) {
            if (sendNotifications() == true) {
                // Verify what there are no ContactSchedules with Status = "New"
                if(getNumOfNotUpdatedContactSchedules() == true) {
                    flag = true;
                    break;
                }
            }
        }
//************* Update isSent field in Contact Schedules ****************************************/
        if (ContactSchedulesListToUpdate.size() > 0) { // For saved in WEB contactSchedules isSent should be updated to '1'
            dbContactSchedule = new DBContactSchedule(this);
            if (dbContactSchedule.updateContactSchedules(ContactSchedulesListToUpdate) <= 0) {
                Log.d(TAG, "Some of contactSchedules were NOT Updated: ");
            }
        }

        if(flag == false || goodContactSchedulesList.size() > 0){
            if(goodContactSchedulesList != null) {
                for (int i = 0; i < goodContactSchedulesList.size(); i++) {
                    badContactSchedulesList.add(goodContactSchedulesList.get(i));
                }
            }
            showToast(getResources().getText(R.string.add_notification_err).toString());
            handleFailedSchedules();
        }
        else{
            showToast(getResources().getText(R.string.add_db_successfully).toString());
        }
    }

    private boolean getNumOfNotUpdatedContactSchedules() {
        ArrayList<ContactSchedule> tempCSList = new ArrayList<>();
        tempCSList.addAll(goodContactSchedulesList);

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
                int index = goodContactSchedulesList.indexOf(tempCSList.get(i));
                goodContactSchedulesList.remove(index);
            }
        }
        return isUpdated;
    }

    private boolean sendNotifications(){
        boolean isSent = true;
        if(goodContactSchedulesList != null) {
            for (int j = 0; j < goodContactSchedulesList.size(); j++) {
                if(j > 0){
                    try {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("phone", goodContactSchedulesList.get(j).getPhone()));

                Log.d(TAG, "Send Notification to = " + goodContactSchedulesList.get(j).getPhone());

                if (sendJson(url_notif_add_schedule, params) == true) {
                    Log.d(TAG, "notification added successfully");

                } else {
                    Log.d(TAG, "Failed to add notification: " + errMessage);
                    badContactSchedulesList.add(goodContactSchedulesList.get(j));
                    isSent = false;
                }
            }
            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return isSent;
    }
}
