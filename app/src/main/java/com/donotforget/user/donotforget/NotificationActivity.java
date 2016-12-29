package com.donotforget.user.donotforget;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContactSchedule;
import com.donotforget.user.donotforget.DBObjects.DBContacts;
import com.donotforget.user.donotforget.DBObjects.DBSchedule;
import com.donotforget.user.donotforget.objects.ContactSchedule;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;
import com.donotforget.user.donotforget.services.AlarmService;

import java.util.ArrayList;
import java.util.Calendar;

public class NotificationActivity extends AppCompatActivity implements View.OnClickListener {
    private static int SEC = 1000;
    private Button btnSnooze, btnClose, btnPlusHour, btnPlusMin, btnMinusHour, btnMinusMin;
    private TextView tvMsg, tvDate, tvTime, tvMsgFrom;
    private EditText tvHours, tvMins;
    private int hour = 0, min = 5;
    private String sHour, sMin;
    private boolean bSnooze = false;
    private Schedule schedule = new Schedule();
    private DBSchedule dbSchedule;
    private DBContactSchedule dbContactSchedule;
    private ArrayList<Long> rowIdList;
    private ContactSchedule contactSchedule = new ContactSchedule();
    private ArrayList<ContactSchedule> contactSchedulesList = new ArrayList<>();
    private long rowID = -1;
    private int myCount;
    private static final String TAG = "Alex_" + NotificationActivity.class.getSimpleName();

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
    protected void onCreate(Bundle savedInstanceState) {
        DBContacts dbContacts = new DBContacts(getApplicationContext());
        String strContactName = "";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        initFields();

        Intent intent = getIntent();
        if(intent.hasExtra(MyUsefulFuncs.SCHEDULE)){
            schedule = (Schedule) intent.getSerializableExtra(MyUsefulFuncs.SCHEDULE);
            Log.d(TAG,"In NotificationActivity: Schedule = " + schedule.toString());
            strContactName = dbContacts.getContactName(schedule.getScheduleFrom());
            if(!strContactName.equals("")) {
                tvMsgFrom.setText(getResources().getString(R.string.scheduleFrom) + " " + strContactName);
            }
            tvMsg.setText(schedule.getText());
            tvTime.setText(schedule.getAtTime());
            Calendar cal = Calendar.getInstance();
            tvDate.setText(MyUsefulFuncs.DateToString(cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.MONTH)+1,cal.get(Calendar.YEAR)));
        }
    }

    private void updateScheduleStatus(){
        final Context context = this;
        dbSchedule = new DBSchedule(this);
        new AsyncTask<Void,Integer,Integer>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected Integer doInBackground(Void... voids) {

                Log.d(TAG,"In Notification:updateScheduleStatus(). Schedule_ID = " + schedule.getId());
                Integer counter = dbSchedule.UpdateStatus(getResources().getString(R.string.status_expired),schedule.getId());

                Log.d(TAG,"Number of updated schedules is " + counter);
                return counter;
            }
            @Override
            protected void onPostExecute(Integer counter) {
                myCount = counter;
                if(myCount > 0){
                    Log.d(TAG, "Status was updated successfully");
                }
                else {
                    Log.d(TAG, "Failed to update the status");
                }
                if(bSnooze == true){
                    Intent serviceIntent = new Intent(context,AlarmService.class);
                    serviceIntent.setAction(AlarmService.ACTION_CREATE);
                    context.startService(serviceIntent);
                }
            }
        }.execute();
    }

    private void initFields() {
        tvMsgFrom = (TextView) findViewById(R.id.tvMessageFrom);
        tvMsg = (TextView) findViewById(R.id.tvNotifMessage);
        tvDate = (TextView) findViewById(R.id.tvPhone);
        tvTime = (TextView) findViewById(R.id.tvNotifTime);
        tvHours = (EditText) findViewById(R.id.txtHours);
        tvMins = (EditText) findViewById(R.id.txtMins);

        btnSnooze = (Button) findViewById(R.id.btnSnooze);
        btnClose = (Button) findViewById(R.id.btnClose);
        btnPlusHour = (Button) findViewById(R.id.btnPlusHour);
        btnPlusMin = (Button) findViewById(R.id.btnPlusMin);
        btnMinusHour = (Button) findViewById(R.id.btnMinusHour);
        btnMinusMin = (Button) findViewById(R.id.btnMinusMin);

        btnSnooze.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnPlusHour.setOnClickListener(this);
        btnPlusMin.setOnClickListener(this);
        btnMinusHour.setOnClickListener(this);
        btnMinusMin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSnooze:
                snoozePressed();
                break;
            case R.id.btnClose:
                closePressed();
                break;
            case R.id.btnPlusHour:
                plusHour();
                break;
            case R.id.btnPlusMin:
                plusMin();
                break;
            case R.id.btnMinusHour:
                minusHour();
                break;
            case R.id.btnMinusMin:
                minusMin();
                break;
        }
    }

    private void minusMin() {
        if(min == 0){
            min = 59;
        }
        else {
            min --;
        }
        sMin = String.format("%02d",min);
        tvMins.setText(sMin);
    }

    private void minusHour() {
        if(hour == 0)
            hour = 23;
        else
            hour --;
        sHour = String.format("%02d",hour);
        tvHours.setText(sHour);
    }

    private void plusMin() {
        if(min == 59)
            min = 0;
        else
            min ++;
        sMin = String.format("%02d",min);
        tvMins.setText(sMin);
    }

    private void plusHour() {
        if(hour == 23)
            hour = 0;
        else
            hour ++;
        sHour = String.format("%02d",hour);
        tvHours.setText(sHour);
    }

    private void closePressed() {
        finish();
    }

    private void snoozePressed() {
        contactSchedule.setSchedule_owner(MyUsefulFuncs.REMIND_TO_MYSELF);
        contactSchedulesList.add(contactSchedule);
        /************ ADD TIME TO SCHEDULE *****************/
        Calendar now = Calendar.getInstance();
        Calendar newCal = (Calendar)now.clone();
        newCal.add(Calendar.HOUR_OF_DAY,Integer.parseInt(tvHours.getText().toString()));
        newCal.add(Calendar.MINUTE,Integer.parseInt(tvMins.getText().toString()));
        int day = newCal.get(Calendar.DAY_OF_MONTH);
        int month = newCal.get(Calendar.MONTH);
        int year = newCal.get(Calendar.YEAR);
        String strTime = String.format("%02d:%02d:00",newCal.get(Calendar.HOUR_OF_DAY),newCal.get(Calendar.MINUTE));
        String strDate = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(day, month + 1,year));
        strDate = MyUsefulFuncs.DateFromSqlFormat(strDate);
        Log.d(TAG,"The new Date after SNOOZE is:" + strDate);
        Log.d(TAG,"The new Time after SNOOZE is:" + strTime);

        schedule.setRecurring(MyUsefulFuncs.ONE_TIME);

        schedule.setOnceDate(strDate,0);
        schedule.setFromDate(strDate,0);
        schedule.setToDate(strDate,0);
        schedule.setOnceTime(strTime);
        schedule.setAtTime(strTime);
        /************ ADD TIME TO SCHEDULE *****************/

        dbSchedule = new DBSchedule(this);
        dbContactSchedule = new DBContactSchedule(this);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Log.d(TAG,"In Notification, shedule_ID = " + schedule.getId());
                rowID = dbSchedule.addToDataBase(schedule);
                if(rowID == 0) {
                    Log.d(TAG,"Failed to save Schedules");
                    return 0;
                }
                if(contactSchedulesList != null){
                    for (int i = 0; i <contactSchedulesList.size(); i++) {
                        contactSchedulesList.get(i).setSchedule_id(rowID);
                        //Log.d(TAG,"rowID = " + rowID);
                    }
                }
                rowIdList = dbContactSchedule.addToDataBase(contactSchedulesList);
                if(rowIdList == null || rowIdList.size() == 0) {
                    Log.d(TAG,"Failed to save Contact_Schedules");
                    return 0;
                }
                return 1;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if(result == 1) {
                    showToast(getResources().getString(R.string.add_db_successfully));
                }
                else{
                    showToast(getResources().getString(R.string.save_schedule_err));
                }
            }
        }.execute();
        bSnooze = true;

        finish();
    }

    @Override
    protected void onDestroy() {
        updateScheduleStatus();
        Log.d(TAG,"onDestroy");

        super.onDestroy();
    }
}
