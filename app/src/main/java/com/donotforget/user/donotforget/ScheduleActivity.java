package com.donotforget.user.donotforget;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.donotforget.user.donotforget.DBObjects.DBContactSchedule;
import com.donotforget.user.donotforget.DBObjects.DBSchedule;
import com.donotforget.user.donotforget.interfaces.IDatePickerFragmentListener;
import com.donotforget.user.donotforget.objects.ContactSchedule;
import com.donotforget.user.donotforget.objects.MyContacts;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;
import com.donotforget.user.donotforget.services.AlarmService;
import com.donotforget.user.donotforget.services.SendToWebService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.widget.Toast.LENGTH_LONG;

public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener, IDatePickerFragmentListener {
    private static final int DATES_EQUALS = 0;
    private static final int CUR_DATE_BEFORE = 1;
    private static final int CUR_DATE_AFTER = 2;
    private ViewSwitcher vs;
    private String strTimeAt = "",dayOfWeek = "", strMessageText = "";
    private RadioButton radioDay, radioWeek, radioMonth, radioYear;
    private Button btnSave;
    private CheckBox cbPlayRingtone, cbSun, cbMon, cbTue, cbWed, cbThu, cbFri, cbSat;
    private int year, month, day, hear, min, sec, dayMonth;
    private TextView tvDateOnce, tvTimeOnce, tvDateFrom, tvDateTo, tvTimeAt;
    private ImageView imgViewTime, imgViewDate, imgViewDateFrom, imgViewDateTo, imgViewTimeAt;
    private View currView;
    private TextView txtSun, txtMon, txtTue, txtWed, txtThu, txtFri, txtSat;
    private static ScheduleActivity CurrContext;
    private Schedule schedule;
    private ArrayList<Schedule> schedules = new ArrayList<>();
    private ArrayList<ContactSchedule> contactSchedulesList = new ArrayList<>();
    private DBSchedule dbSchedule;
    private DBContactSchedule dbContactSchedule;
    private ArrayList<MyContacts> myContactsList = null;
    private MyContacts contact = null;
    private boolean bRemindToMyself = false;
    private int scheduleOwner = 0;
    boolean state = true;
    private ArrayList<Long> rowIdList;
    private long rowID = -1;
    private Button btnOnce, btnRepeat;
    private Intent serviceIntent;
    private boolean addToDBRes = true;
    private static final String TAG = "Alex_" + ScheduleActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        initFields();
        SetDayButtonsAlpha(false);
 //       fillSchedules();

        Intent intent = getIntent();
        strMessageText = intent.getStringExtra(MyUsefulFuncs.MESSAGE_TEXT);
        scheduleOwner = intent.getIntExtra(MyUsefulFuncs.SCHEDULE_OWNER,0); // 0 = Remind to myself; 1 = to Contact; 2 = to Group
        if(intent.hasExtra(MyUsefulFuncs.CONTACTS)){    // In case of "Remind to Contact" or "Remind to Group"
            myContactsList = (ArrayList<MyContacts>) intent.getSerializableExtra(MyUsefulFuncs.CONTACTS);
        }
    }

    @Override
    public boolean validateDates(int year, int month, int day) {
        String strDate = MyUsefulFuncs.DateToString(day, month + 1,year);
        String [] strDays = getResources().getStringArray(R.array.daysOfWeek);
        dayOfWeek = strDays[MyUsefulFuncs.getDayOfWeek(day,month,year)];
        dayMonth = day;

        // Once Date
        if(currView.getId() == R.id.tvDateOne || currView.getId() == R.id.imageViewDate || currView.getId() == R.id.Date) {
            if(validateDate(strDate) == CUR_DATE_AFTER){
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.date_err), LENGTH_LONG).show();
                return false;
            }
            tvDateOnce.setText(strDate);
            imgViewDate.setVisibility(View.GONE);
            if(!tvTimeOnce.getText().toString().isEmpty() && validateTime(tvTimeOnce.getText().toString(),true) == false){
                return false;
            }
        }
        // From Date
        else if(currView.getId() == R.id.tvPhone || currView.getId() == R.id.imageViewDateFrom || currView.getId() == R.id.FromDate){
            if(validateDate(strDate) == CUR_DATE_AFTER){
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.date_from_err), LENGTH_LONG).show();
                return false;
            }
            if(!tvDateTo.getText().toString().isEmpty() && validateRepeatDates(strDate,0) == CUR_DATE_AFTER){
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.fromDateAfterToDate), LENGTH_LONG).show();
                tvDateFrom.setText("");
                imgViewDateFrom.setVisibility(View.VISIBLE);
                return false;
            }
            tvDateFrom.setText(strDate);
            imgViewDateFrom.setVisibility(View.GONE);
            if(!tvTimeAt.getText().toString().isEmpty() && validateTime(tvTimeAt.getText().toString(),false) == false){
                tvDateFrom.setText("");
                imgViewDateFrom.setVisibility(View.VISIBLE);
                return false;
            }

        }
        // To Date
        else if(currView.getId() == R.id.tvNotifDateTo || currView.getId() == R.id.imageViewDateTo || currView.getId() == R.id.ToDate){
            if(validateDate(strDate) == CUR_DATE_AFTER){
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.date_to_err), LENGTH_LONG).show();
                return false;
            }

            if(!tvDateFrom.getText().toString().isEmpty() && validateRepeatDates(strDate,1) == CUR_DATE_AFTER){
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.fromDateAfterToDate), LENGTH_LONG).show();
                tvDateTo.setText("");
                imgViewDateTo.setVisibility(View.VISIBLE);
                return false;
            }
            tvDateTo.setText(strDate);
            imgViewDateTo.setVisibility(View.GONE);
            if(!tvTimeAt.getText().toString().isEmpty() && validateTime(tvTimeAt.getText().toString(),false) == false){
                tvDateTo.setText("");
                imgViewDateTo.setVisibility(View.VISIBLE);
                return false;
            }

        }

        return true;
    }

    public void fillSchedules() {
        dbSchedule = new DBSchedule(this);
        new AsyncTask<Void, Void, ArrayList<Schedule>>() {

            @Override
            protected ArrayList<Schedule> doInBackground(Void... params) {
                schedules = dbSchedule.ReadFromDataBaseDaily();
                return schedules;
            }

            @Override
            protected void onPostExecute(ArrayList<Schedule> schedules) {
/*
                if(schedules.size() > 0){
                    for (int i = 0; i <schedules.size(); i++) {
                        Log.d(TAG,"row[" + i + "] =" + schedules.get(i).toString() );
                    }
                }
                else{
                    Log.d(TAG,"The Schedules Table is Empty");
                }
*/
//                if (schedules.size() > 0) {
//                    intent.putExtra("schedules", schedules);
//                    startActivity(intent);
//                } else
//                    Toast.makeText(getApplicationContext(), "There are no MyContacts in DataBase", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    /****************************************************************************/
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
    private IDatePickerFragmentListener iDatePickerFragmentListener;

        public DatePickerFragment() {

            if(CurrContext == null)
                return;
            try {
                iDatePickerFragmentListener = (IDatePickerFragmentListener) CurrContext;
            }
            catch (ClassCastException e){
                throw new ClassCastException((CurrContext.toString() + " must implement IDatePickerFragmentListener"));
            }

        }

    @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            if(iDatePickerFragmentListener == null){
                return;
            }
            if(iDatePickerFragmentListener.validateDates(year, month, day) == false){
//                Log.d(TAG, "ValidateDates returned: FALSE");
                return;
            }
            else {
//                Log.d(TAG, "ValidateDates returned: TRUE");
            }

        }
}

    public void showDatePickerDialog(View v) {
  //      Log.d(TAG,"showDatePickerDialog");
        FragmentManager fragmentManager = getFragmentManager();
        currView = v;
        CurrContext = this;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(fragmentManager, "datePicker");
    }
/********************************************************************************/
    private void initFields() {
//        txtSun = (TextView) findViewById(R.id.txtSun);
//        txtMon = (TextView) findViewById(R.id.txtMon);
//        txtTue = (TextView) findViewById(R.id.txtTue);
//        txtWed = (TextView) findViewById(R.id.txtWed);
//        txtThu = (TextView) findViewById(R.id.txtThu);
//        txtFri = (TextView) findViewById(R.id.txtFri);
//        txtSat = (TextView) findViewById(R.id.txtSat);

        btnOnce = (Button) findViewById(R.id.btnOnce);
        btnOnce.setOnClickListener(this);
        btnRepeat = (Button) findViewById(R.id.btnRepeat);
        btnRepeat.setOnClickListener(this);

        radioYear = (RadioButton) findViewById(R.id.radioYear);
        radioDay = (RadioButton) findViewById(R.id.radioDay);
       // radioDay.setChecked(true);
        radioWeek = (RadioButton) findViewById(R.id.radioWeek);
        radioMonth = (RadioButton) findViewById(R.id.radioMonth);


        radioDay.setOnClickListener(this);
        radioWeek.setOnClickListener(this);
        radioMonth.setOnClickListener(this);
        radioYear.setOnClickListener(this);


        tvDateOnce = (TextView) findViewById(R.id.tvDateOne);
        tvDateFrom = (TextView) findViewById(R.id.tvPhone);
        tvDateTo = (TextView) findViewById(R.id.tvNotifDateTo);

        tvTimeOnce = (TextView) findViewById(R.id.tvTimeOne);
        tvTimeAt = (TextView) findViewById(R.id.tvTimeAt);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    SaveParams();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        cbPlayRingtone = (CheckBox) findViewById(R.id.cbPlayRingtone);
        cbSun = (CheckBox) findViewById(R.id.cbSun);
        cbMon = (CheckBox) findViewById(R.id.cbMon);
        cbTue = (CheckBox) findViewById(R.id.cbTue);
        cbWed = (CheckBox) findViewById(R.id.cbWed);
        cbThu = (CheckBox) findViewById(R.id.cbThu);
        cbFri = (CheckBox) findViewById(R.id.cbFri);
        cbSat = (CheckBox) findViewById(R.id.cbSat);

        vs = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        imgViewTime = (ImageView) findViewById(R.id.imageViewTime);
        imgViewDate = (ImageView) findViewById(R.id.imageViewDate);

        imgViewDateFrom = (ImageView) findViewById(R.id.imageViewDateFrom);
        imgViewDateTo = (ImageView) findViewById(R.id.imageViewDateTo);
        imgViewTimeAt = (ImageView) findViewById(R.id.imageViewTimeAt);

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.radioDay:
                radioMonth.setChecked(!radioDay.isChecked());
                radioYear.setChecked(!radioDay.isChecked());
                radioWeek.setChecked(!radioDay.isChecked());
                SetDayButtonsAlpha(radioWeek.isChecked());
                break;
            case R.id.radioWeek:
                radioMonth.setChecked(!radioWeek.isChecked());
                radioYear.setChecked(!radioWeek.isChecked());
                radioDay.setChecked(!radioWeek.isChecked());
                SetDayButtonsAlpha(radioWeek.isChecked());
                break;
            case R.id.radioMonth:
                radioDay.setChecked(!radioMonth.isChecked());
                radioYear.setChecked(!radioMonth.isChecked());
                radioWeek.setChecked(!radioMonth.isChecked());
                SetDayButtonsAlpha(radioWeek.isChecked());
                break;
            case R.id.radioYear:
                radioDay.setChecked(!radioYear.isChecked());
                radioMonth.setChecked(!radioYear.isChecked());
                radioWeek.setChecked(!radioYear.isChecked());
                SetDayButtonsAlpha(radioWeek.isChecked());
                break;
            case R.id.btnOnce:
                changeScheduleState();
                break;
            case R.id.btnRepeat:
                changeScheduleState();
                break;
        }
    }

    private void changeScheduleState() {
        state = !state;
        if(state == true){
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                btnOnce.setBackgroundDrawable( getResources().getDrawable(R.drawable.btn_once_active) );
                btnRepeat.setBackgroundDrawable( getResources().getDrawable(R.drawable.btn_once_inactive) );

            }
            else {
                btnOnce.setBackground( ResourcesCompat.getDrawable(getResources(), R.drawable.btn_once_active, null));
                btnRepeat.setBackground( ResourcesCompat.getDrawable(getResources(), R.drawable.btn_once_inactive, null));
            }
        }
        else {
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                btnOnce.setBackgroundDrawable( getResources().getDrawable(R.drawable.btn_once_inactive) );
                btnRepeat.setBackgroundDrawable( getResources().getDrawable(R.drawable.btn_once_active) );

            }
            else {
                btnOnce.setBackground( ResourcesCompat.getDrawable(getResources(), R.drawable.btn_once_inactive, null));
                btnRepeat.setBackground( ResourcesCompat.getDrawable(getResources(), R.drawable.btn_once_active, null));
            }
        }
        vs.showNext();
    }

    private void SaveParams() throws ParseException {
        addToDBRes = true;
        if (!validateParams())
            return;
/********************************************************/
        schedule = new Schedule();
        if(!MyUsefulFuncs.myPhoneNumber.equals("")){
            schedule.setScheduleFrom(MyUsefulFuncs.myPhoneNumber);
        }
        if(cbPlayRingtone.isChecked())
            schedule.setPlayRingtone(1);
        else
            schedule.setPlayRingtone(0);
        schedule.setText(strMessageText);
        if(vs.getDisplayedChild() == 0) {         // ONE TIME
            schedule.setRecurring(MyUsefulFuncs.ONE_TIME);
            schedule.setOnceDate(tvDateOnce.getText().toString(),0);
            schedule.setFromDate(tvDateOnce.getText().toString(),0);
            schedule.setToDate(tvDateOnce.getText().toString(),0);
            schedule.setOnceTime(tvTimeOnce.getText().toString());
            schedule.setAtTime(tvTimeOnce.getText().toString());

            if(addToDataBase(schedule) == false){
//                Toast.makeText(this,getResources().getText(R.string.add_db_err), LENGTH_LONG).show();
                return;
            }
        }
        else {                                      // REPEAT
            if(radioDay.isChecked()){               // Once a Day
                schedule = new Schedule();
                if(!MyUsefulFuncs.myPhoneNumber.equals("")){
                    schedule.setScheduleFrom(MyUsefulFuncs.myPhoneNumber);
                }
                if(cbPlayRingtone.isChecked())
                    schedule.setPlayRingtone(1);
                else
                    schedule.setPlayRingtone(0);
                schedule.setText(strMessageText);
                schedule.setRecurring(MyUsefulFuncs.DAILY);
                schedule.setFromDate(tvDateFrom.getText().toString(),0);
                schedule.setToDate(tvDateTo.getText().toString(),0);
                schedule.setAtTime(tvTimeAt.getText().toString());

                if(addToDataBase(schedule) == false){
//                    Toast.makeText(this,getResources().getText(R.string.add_db_err), LENGTH_LONG).show();
                    return;
                }
            }
            else if(radioWeek.isChecked()){          // Weekly
                schedule = new Schedule();
                if(!MyUsefulFuncs.myPhoneNumber.equals("")){
                    schedule.setScheduleFrom(MyUsefulFuncs.myPhoneNumber);
                }
                if(cbPlayRingtone.isChecked())
                    schedule.setPlayRingtone(1);
                else
                    schedule.setPlayRingtone(0);
                schedule.setText(strMessageText);
                schedule.setRecurring(MyUsefulFuncs.WEEKLY);
                schedule.setFromDate(tvDateFrom.getText().toString(),0);
                schedule.setToDate(tvDateTo.getText().toString(),0);
                schedule.setAtTime(tvTimeAt.getText().toString());

                schedule.setDaysOfWeek(cbSun.isChecked(),cbMon.isChecked(), cbTue.isChecked(),
                        cbWed.isChecked(),cbThu.isChecked(),cbFri.isChecked(),cbSat.isChecked());

                if(addToDataBase(schedule) == false){
//                    Toast.makeText(this,getResources().getText(R.string.add_db_err), LENGTH_LONG).show();
                    return;
                }
            }
            else if(radioMonth.isChecked()){         // Once a month
                String [] parts = MyUsefulFuncs.SplitString(tvDateFrom.getText().toString(),"/");
                if(parts.length < 3) {
                    Log.d(TAG, "Missing Date parameter");
                    return;
                }

                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                Calendar cFrom = Calendar.getInstance();
                cFrom.set(year, month - 1,day);
                String strDateFrom = MyUsefulFuncs.DateToString(day,month,year);
//                Log.d(TAG,"The From Date is: " + strDateFrom);

                parts = MyUsefulFuncs.SplitString(tvDateTo.getText().toString(),"/");
                if(parts.length < 3) {
                    Log.d(TAG, "Missing Date parameter");
                    return;
                }

                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                year = Integer.parseInt(parts[2]);
                Calendar cTo = Calendar.getInstance();
                cTo.set(year, month - 1,day);
                String strDateTo = MyUsefulFuncs.DateToString(day,month,year);
//                Log.d(TAG,"The To Date is: " + strDateTo);
                schedules = new ArrayList<Schedule>();
                while(MyUsefulFuncs.dateFormat.parse(strDateFrom).before(MyUsefulFuncs.dateFormat.parse(strDateTo))
                        || MyUsefulFuncs.dateFormat.parse(strDateFrom).equals(MyUsefulFuncs.dateFormat.parse(strDateTo))){
                    schedule = new Schedule();
                    if(!MyUsefulFuncs.myPhoneNumber.equals("")){
                        schedule.setScheduleFrom(MyUsefulFuncs.myPhoneNumber);
                    }
                    if(cbPlayRingtone.isChecked())
                        schedule.setPlayRingtone(1);
                    else
                        schedule.setPlayRingtone(0);
                    schedule.setText(strMessageText);
                    schedule.setRecurring(MyUsefulFuncs.MONTHLY);
                    schedule.setFromDate(tvDateFrom.getText().toString(),0);
                    schedule.setToDate(tvDateTo.getText().toString(),0);
                    schedule.setAtTime(tvTimeAt.getText().toString());
                    schedule.setOnceDate(strDateFrom,0);
                    schedule.setStatus(getResources().getString(R.string.status_active));

                    schedules.add(schedule);

                    cFrom = MyUsefulFuncs.addDate(cFrom.get(Calendar.DAY_OF_MONTH),cFrom.get(Calendar.MONTH),cFrom.get(Calendar.YEAR), Calendar.MONTH,1);
                    strDateFrom = MyUsefulFuncs.DateToString(cFrom.get(Calendar.DAY_OF_MONTH),cFrom.get(Calendar.MONTH)+1,cFrom.get(Calendar.YEAR));
                }
                if(addToDataBase(schedules) == false){
//                    Toast.makeText(this,getResources().getText(R.string.add_db_err), LENGTH_LONG).show();
                    return;
                }
            }
            else if(radioYear.isChecked()){          // Once a year
                String [] parts = MyUsefulFuncs.SplitString(tvDateFrom.getText().toString(),"/");
                if(parts.length < 3) {
                    Log.d(TAG, "Missing Date parameter");
                    return;
                }

                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                Calendar cFrom = Calendar.getInstance();
                cFrom.set(year, month - 1,day);
                String strDateFrom = MyUsefulFuncs.DateToString(day,month,year);
//                Log.d(TAG,"The From Date is: " + strDateFrom);

                parts = MyUsefulFuncs.SplitString(tvDateTo.getText().toString(),"/");
                if(parts.length < 3) {
                    Log.d(TAG, "Missing Date parameter");
                    return;
                }

                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                year = Integer.parseInt(parts[2]);
                Calendar cTo = Calendar.getInstance();
                cTo.set(year, month - 1,day);
                String strDateTo = MyUsefulFuncs.DateToString(day,month,year);
//                Log.d(TAG,"The To Date is: " + strDateTo);
                schedules = new ArrayList<Schedule>();
                while(MyUsefulFuncs.dateFormat.parse(strDateFrom).before(MyUsefulFuncs.dateFormat.parse(strDateTo))
                        || MyUsefulFuncs.dateFormat.parse(strDateFrom).equals(MyUsefulFuncs.dateFormat.parse(strDateTo))){
                    schedule = new Schedule();
                    if(!MyUsefulFuncs.myPhoneNumber.equals("")){
                        schedule.setScheduleFrom(MyUsefulFuncs.myPhoneNumber);
                    }
                    if(cbPlayRingtone.isChecked())
                        schedule.setPlayRingtone(1);
                    else
                        schedule.setPlayRingtone(0);
                    schedule.setText(strMessageText);
                    schedule.setRecurring(MyUsefulFuncs.YEARLY);
                    schedule.setFromDate(tvDateFrom.getText().toString(),0);
                    schedule.setToDate(tvDateTo.getText().toString(),0);
                    schedule.setAtTime(tvTimeAt.getText().toString());

                    schedule.setOnceDate(strDateFrom,0);
                    schedule.setStatus(getResources().getString(R.string.status_active));

                    schedules.add(schedule);

                    cFrom = MyUsefulFuncs.addDate(cFrom.get(Calendar.DAY_OF_MONTH),cFrom.get(Calendar.MONTH),cFrom.get(Calendar.YEAR), Calendar.YEAR,1);
                    strDateFrom = MyUsefulFuncs.DateToString(cFrom.get(Calendar.DAY_OF_MONTH),cFrom.get(Calendar.MONTH)+1,cFrom.get(Calendar.YEAR));
                }
                if(addToDataBase(schedules) == false){
//                    Toast.makeText(this,getResources().getText(R.string.add_db_err), LENGTH_LONG).show();
                    return;
                }
            }
        }
/********************************************************/
        if(scheduleOwner == MyUsefulFuncs.REMIND_TO_MYSELF) {   // Reminders to myself only
//            Toast.makeText(getApplicationContext(), getResources().getText(R.string.add_db_successfully), LENGTH_LONG).show();

            serviceIntent = new Intent(this, AlarmService.class);
            serviceIntent.setAction(AlarmService.ACTION_CREATE);
            this.startService(serviceIntent);
        }
        else{                                                   // Reminders to myself and other Contacts
            serviceIntent = new Intent(this,SendToWebService.class);
            serviceIntent.setAction(SendToWebService.ACTION_SEND_SCHEDULE);
            this.startService(serviceIntent);

            if(bRemindToMyself == true){
                bRemindToMyself = false;
 //               Toast.makeText(getApplicationContext(), getResources().getText(R.string.add_db_successfully), LENGTH_LONG).show();

                serviceIntent = new Intent(this, AlarmService.class);
                serviceIntent.setAction(AlarmService.ACTION_CREATE);
                this.startService(serviceIntent);
            }
        }
        finish();
        //Log.d(TAG,"End of SaveParams");
    }

    private boolean addToDataBase(final Schedule schedule) {
        //Log.d(TAG,"schedule " + schedule.toString());
        ContactSchedule contactSchedule;
        if(scheduleOwner != MyUsefulFuncs.REMIND_TO_MYSELF && myContactsList != null){  // Add to Contact
            for (int i = 0; i <myContactsList.size(); i++) {
                contactSchedule = new ContactSchedule();
                if(MyUsefulFuncs.myPhoneNumber.equals(myContactsList.get(i).getPhone())){
                    contactSchedule.setSchedule_owner(MyUsefulFuncs.REMIND_TO_MYSELF);
                    contactSchedule.setIsSent(1);
                    bRemindToMyself = true;
                }
                else {
                    contactSchedule.setSchedule_owner(scheduleOwner);
                    contactSchedule.setContactName(myContactsList.get(i).getName());
                    contactSchedule.setPhone(myContactsList.get(i).getPhone());
                    contactSchedule.setIsSent(0);
                }

                contactSchedulesList.add(contactSchedule);
            }
        }
        else{                                                                           // Add to Myself
            contactSchedule = new ContactSchedule();

            contactSchedule.setSchedule_owner(scheduleOwner);
            contactSchedule.setIsSent(1);
            contactSchedulesList.add(contactSchedule);
        }
        schedule.setStatus(getResources().getString(R.string.status_active));
        dbSchedule = new DBSchedule(this);
        dbContactSchedule = new DBContactSchedule(this);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
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
                    ClearFields();
                    if(scheduleOwner == MyUsefulFuncs.REMIND_TO_MYSELF) {   // Reminders to myself only
                        Toast.makeText(getApplicationContext(), getResources().getText(R.string.add_db_successfully), LENGTH_LONG).show();
                    }
                }
                else{
                    addToDBRes = false;
                }

            }
        }.execute();
        return addToDBRes;
    }

    private boolean addToDataBase(final ArrayList<Schedule> schedules) {
        dbSchedule = new DBSchedule(this);
        dbContactSchedule = new DBContactSchedule(this);
        ContactSchedule contactSchedule = new ContactSchedule();
        final ArrayList<ContactSchedule> NewContactSchedulesList = new ArrayList<>();

        if(scheduleOwner == MyUsefulFuncs.REMIND_TO_MYSELF){
            contactSchedule = new ContactSchedule();

            contactSchedule.setSchedule_owner(scheduleOwner);
            contactSchedule.setIsSent(1);
            contactSchedulesList.add(contactSchedule);
        }
        else if(scheduleOwner == MyUsefulFuncs.REMIND_TO_CONTACT || scheduleOwner == MyUsefulFuncs.REMIND_TO_GROUP){
            if(myContactsList != null){
                for (int i = 0; i <myContactsList.size(); i++) {
                    contactSchedule = new ContactSchedule();
                    if(MyUsefulFuncs.myPhoneNumber.equals(myContactsList.get(i).getPhone())){
                        contactSchedule.setSchedule_owner(MyUsefulFuncs.REMIND_TO_MYSELF);
                        contactSchedule.setIsSent(1);
                        bRemindToMyself = true;
                    }
                    else {
                        contactSchedule.setSchedule_owner(scheduleOwner);
                        contactSchedule.setContactName(myContactsList.get(i).getName());
                        contactSchedule.setPhone(myContactsList.get(i).getPhone());
                        contactSchedule.setIsSent(0);
                    }
                    contactSchedulesList.add(contactSchedule);

                }
            }
        }
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                rowIdList = dbSchedule.addToDataBase(schedules);
                if(rowIdList.size() > 0){
                    for (int i = 0; i <contactSchedulesList.size(); i++) {
                        for (int j = 0; j <rowIdList.size(); j++) {
                            ContactSchedule NewContactSchedule = new ContactSchedule();
                            NewContactSchedule.Copy(contactSchedulesList.get(i));
                            NewContactSchedule.setSchedule_id(rowIdList.get(j));
                            NewContactSchedulesList.add(NewContactSchedule);
                            //Log.d(TAG,"rowIdList-->rowID[" + i + "][" + j + "] = " + NewContactSchedule.getSchedule_id());
                        }
                    }
                   //Log.d(TAG,"NewContactSchedulesList size = " + NewContactSchedulesList.size());
                    rowIdList = dbContactSchedule.addToDataBase(NewContactSchedulesList);
                    if(rowIdList.size() > 0) {
                        return 1;
                    }
                }

                return 0;
            }
            @Override
            protected void onPostExecute(Integer result) {
                if(result == 1) {
                    ClearFields();
                    if(scheduleOwner == MyUsefulFuncs.REMIND_TO_MYSELF) {   // Reminders to myself only
                        Toast.makeText(getApplicationContext(), getResources().getText(R.string.add_db_successfully), LENGTH_LONG).show();
                    }
                }
                else{
                    addToDBRes = false;
                }
            }
        }.execute();

        return addToDBRes;
    }

    private void ClearFields() {
        tvTimeAt.setText("");
        tvDateTo.setText("");
        tvDateFrom.setText("");
        tvTimeOnce.setText("");
        tvDateOnce.setText("");
        cbPlayRingtone.setChecked(false);
        radioDay.setChecked(true);
        radioWeek.setChecked(false);
        radioMonth.setChecked(false);
        radioYear.setChecked(false);

        imgViewTime.setVisibility(View.VISIBLE);
        imgViewTimeAt.setVisibility(View.VISIBLE);
        imgViewDateTo.setVisibility(View.VISIBLE);
        imgViewDate.setVisibility(View.VISIBLE);
        imgViewDateFrom.setVisibility(View.VISIBLE);

        cbSun.setChecked(false);
        cbMon.setChecked(false);
        cbTue.setChecked(false);
        cbWed.setChecked(false);
        cbThu.setChecked(false);
        cbFri.setChecked(false);
        cbSat.setChecked(false);

        SetDayButtonsAlpha(false);
    }

    private boolean validateParams() {
        if(vs.getDisplayedChild() == 0)         // Once
        {
            if(tvDateOnce.getText().toString().isEmpty()){
                Toast.makeText(this,getResources().getText(R.string.date_empty), LENGTH_LONG).show();
                return false;
            }
            if(tvTimeOnce.getText().toString().isEmpty()){
                Toast.makeText(this,getResources().getText(R.string.time_empty), LENGTH_LONG).show();
                return false;
            }
            if(validateTime(tvTimeOnce.getText().toString(),true) == false){
                return false;
            }
        }
        else if(vs.getDisplayedChild() == 1)    // Repeat
        {
            if(tvDateFrom.getText().toString().isEmpty()){
                Toast.makeText(this,getResources().getText(R.string.dateFrom_empty), LENGTH_LONG).show();
                return false;
            }

            if(tvDateTo.getText().toString().isEmpty()){
                Toast.makeText(this,getResources().getText(R.string.dateTo_empty), LENGTH_LONG).show();
                return false;
            }

            try {
                if (MyUsefulFuncs.dateFormat.parse(tvDateFrom.getText().toString()).after(MyUsefulFuncs.dateFormat.parse(tvDateTo.getText().toString()))) {
                    Toast.makeText(this, getResources().getText(R.string.fromDateAfterToDate), LENGTH_LONG).show();
                    return false;
                }
            } catch (ParseException e) {}

            if(tvTimeAt.getText().toString().isEmpty()){
                Toast.makeText(this,getResources().getText(R.string.time_empty), LENGTH_LONG).show();
                return false;
            }
            if(radioMonth.isChecked())
            {
                try {
                    long period = MyUsefulFuncs.getdateDiff(MyUsefulFuncs.dateFormat.parse(tvDateFrom.getText().toString()),
                            MyUsefulFuncs.dateFormat.parse(tvDateTo.getText().toString()),TimeUnit.DAYS);
                   // Log.d(TAG, "(MONTH) The diff in days is: " + period);
                    if(period < 30){
                        Toast.makeText(this,getResources().getText(R.string.date_period_month_err), LENGTH_LONG).show();
                        return false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if(radioYear.isChecked())
            {
                try {
                    long period = MyUsefulFuncs.getdateDiff(MyUsefulFuncs.dateFormat.parse(tvDateFrom.getText().toString()),
                            MyUsefulFuncs.dateFormat.parse(tvDateTo.getText().toString()),TimeUnit.DAYS);
                    //Log.d(TAG, "(YEAR) The diff in days is: " + period);
                    if(period < 365){
                        Toast.makeText(this,getResources().getText(R.string.date_period_year_err), LENGTH_LONG).show();
                        return false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if(radioWeek.isChecked()) {
                if (!cbSun.isChecked() && !cbMon.isChecked() && !cbTue.isChecked()
                        && !cbWed.isChecked() && !cbThu.isChecked()
                        && !cbFri.isChecked() && !cbSat.isChecked()){
                    Toast.makeText(this,getResources().getText(R.string.days_err), LENGTH_LONG).show();
                    return false;
                }
            }
        }


        return true;
    }

    public void onClickTime(final View view) {
        Calendar calendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int min = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minOfDay) {
                        String strTime = MyUsefulFuncs.TimeToString(hourOfDay,minOfDay);

                        if(view.getId() == R.id.imageViewTime || view.getId() == R.id.Time || view.getId() == R.id.tvTimeOne) {
                            if(validateTime(strTime,true) == false){
                                return;
                            }
                            tvTimeOnce.setText(strTime);
                            imgViewTime.setVisibility(View.GONE);
                        }
                        else if(view.getId() == R.id.imageViewTimeAt || view.getId() == R.id.atTime || view.getId() == R.id.tvTimeAt){
                            if(validateTime(strTime,false) == false){
                                return;
                            }
                            tvTimeAt.setText(strTime);
                            strTimeAt = strTime;
                            imgViewTimeAt.setVisibility(View.GONE);
                        }

                    }
                },hour,min,true);

        timePickerDialog.show();

    }

    private boolean validateTime(String strTime, boolean once) {    /*Once --> once = true; Repeat --> once = false*/
        boolean flag = true;
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String strCurTime = MyUsefulFuncs.TimeToString(hour,min + 1);
        String strCurDate = MyUsefulFuncs.DateToString(day, month + 1,year);
        if(once) {
            try {
                if(!tvDateOnce.getText().toString().isEmpty() && validateDate(tvDateOnce.getText().toString()) == DATES_EQUALS)    // Today
                 {
                     if (MyUsefulFuncs.timeFormat.parse(strCurTime).after(MyUsefulFuncs.timeFormat.parse(strTime))) {
                         tvTimeOnce.setText("");
                         imgViewTime.setVisibility(View.VISIBLE);
                         Toast.makeText(this, getResources().getText(R.string.time_once_err), Toast.LENGTH_LONG).show();

                         flag = false;
                    }
                }
            } catch (ParseException e) {
                Log.d(TAG,e.toString());
            }
        }
        else{
            if(!tvDateFrom.getText().toString().isEmpty() && !tvDateTo.getText().toString().isEmpty()){ // If Dates are Not empty
                try {
                    if (MyUsefulFuncs.dateFormat.parse(tvDateFrom.getText().toString()).equals(MyUsefulFuncs.dateFormat.parse(tvDateTo.getText().toString()))) {
                        /* If dates are equal and set for today --> should be Cur_Time < Selected Time*/
                        if (MyUsefulFuncs.dateFormat.parse(strCurDate).equals(MyUsefulFuncs.dateFormat.parse(tvDateFrom.getText().toString()))) {
                            if (MyUsefulFuncs.timeFormat.parse(strCurTime).after(MyUsefulFuncs.timeFormat.parse(strTime))) {
                                tvTimeAt.setText("");
                                imgViewTimeAt.setVisibility(View.VISIBLE);
                                Toast.makeText(this, getResources().getText(R.string.time_once_err), Toast.LENGTH_LONG).show();

                                flag = false;
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }

        }

       // Log.d(TAG,"validateTime returned " + flag);
        return flag;
    }

    private int validateRepeatDates(String strDate, int from) { /*From Date = 0; To Date = 1*/
        int status = DATES_EQUALS;
        if(from == 0){
            try {
                if (MyUsefulFuncs.dateFormat.parse(strDate).after(MyUsefulFuncs.dateFormat.parse(tvDateTo.getText().toString()))) {
                    status = CUR_DATE_AFTER;
                }
                else if (MyUsefulFuncs.dateFormat.parse(strDate).equals(MyUsefulFuncs.dateFormat.parse(tvDateTo.getText().toString()))) {
                    status = DATES_EQUALS;
                }
                else{
                    status = CUR_DATE_BEFORE;
                }

            } catch (ParseException e) {
                Log.d(TAG, "Exception: " + e.toString());
                return status;
            }
        }
        else {
            try {
                if (MyUsefulFuncs.dateFormat.parse(tvDateFrom.getText().toString()).after(MyUsefulFuncs.dateFormat.parse(strDate))) {
                    status = CUR_DATE_AFTER;
                }
                else if (MyUsefulFuncs.dateFormat.parse(tvDateFrom.getText().toString()).equals(MyUsefulFuncs.dateFormat.parse(strDate))) {
                    status = DATES_EQUALS;
                }
                else{
                    status = CUR_DATE_BEFORE;
                }

            } catch (ParseException e) {
                Log.d(TAG, "Exception: " + e.toString());
                return status;
            }
        }
        return status;
    }


    private int validateDate(String strDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH) + 1;
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        String strCurDate = MyUsefulFuncs.DateToString(day, month,year);
        try {
            if (MyUsefulFuncs.dateFormat.parse(strCurDate).after(MyUsefulFuncs.dateFormat.parse(strDate))) {
                return CUR_DATE_AFTER;
            }
            else if (MyUsefulFuncs.dateFormat.parse(strCurDate).equals(MyUsefulFuncs.dateFormat.parse(strDate))){
                return DATES_EQUALS;
            }
            else {
                return CUR_DATE_BEFORE;
            }
        } catch (ParseException e) {
            Log.d(TAG, "Exception: " + e.toString());
            return 0;
        }
    }

    private void SetDayButtonsAlpha(boolean b) {
        float fAlpha;
        int color = Color.BLACK;
        if(b == true) {
            fAlpha = 1.0f;
            color = Color.WHITE;
        }
        else {
            fAlpha = 0.2f;
            color = Color.BLACK;
        }
        cbSun.setAlpha(fAlpha); cbMon.setAlpha(fAlpha); cbTue.setAlpha(fAlpha);
        cbWed.setAlpha(fAlpha); cbThu.setAlpha(fAlpha); cbFri.setAlpha(fAlpha);
        cbSat.setAlpha(fAlpha);

        cbSun.setTextColor(color); cbMon.setTextColor(color); cbTue.setTextColor(color);
        cbWed.setTextColor(color); cbThu.setTextColor(color); cbFri.setTextColor(color);
        cbSat.setTextColor(color);

    }
}
