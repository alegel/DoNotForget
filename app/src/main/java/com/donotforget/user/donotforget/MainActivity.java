package com.donotforget.user.donotforget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContactSchedule;
import com.donotforget.user.donotforget.DBObjects.DBContacts;
import com.donotforget.user.donotforget.DBObjects.DBSchedule;
import com.donotforget.user.donotforget.objects.Group;
import com.donotforget.user.donotforget.objects.MyContacts;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;
import com.donotforget.user.donotforget.objects.ScheduleListAdapter;
import com.donotforget.user.donotforget.services.AlarmService;
import com.donotforget.user.donotforget.services.ReadContactsService;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final int DAY = 0;
    public static final int WEEK = 1;
    public static final int MONTH = 2;
    private ArrayList<Schedule> schedules = new ArrayList<>();
    private ScheduleListAdapter adapter;
    private ListView lvSchedules;
    private DBSchedule dbSchedule;
    private Spinner spinToday;
    private Button btnAddReminder, btnMessages, btnAddGroup;
    private static ArrayList<MyContacts> contactsList = new ArrayList<>();
    ArrayList<Group> groups;
    public ArrayList<String> children = new ArrayList<>();
    public AlertDialog.Builder alertDlg;
    private ProgressDialog pDialog, pDialog2;
    private int deleteScheduleID = -1, deletePosition = -1;
    private int delMenu = -1;
    private ArrayList<Integer> delSchedulesID = new ArrayList<>();
    private boolean reading = false;
    private boolean firstReadSchedules = false;
    private SharedPreferences preferences;
    private static Context context;
    private boolean readingWebContacts = false;
    private static final String TAG = "Alex_" + MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFields();
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        context = this;
        try {
            preferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Impossible reading from the UserDetails file", Toast.LENGTH_LONG).show();
            return;
        }
        String name, phone, reg_id;

/****************************************************************/
        name = preferences.getString(MyUsefulFuncs.USER_NAME,"error");
        phone = preferences.getString(MyUsefulFuncs.USER_PHONE,"error");
        reg_id = preferences.getString(MyUsefulFuncs.USER_REG_ID,"error");

        if(name.equals("error") || phone.equals("error") || reg_id.equals("error")) // First access
        {
            Intent serviceIntent = new Intent(this,ReadContactsService.class);
            serviceIntent.setAction(ReadContactsService.ACTION_READ_CONTACTS);
            startService(serviceIntent);

            Intent intent = new Intent(this,SignInActivity.class);
            startActivity(intent);
        }
        else{
            MyUsefulFuncs.myName = name;
            MyUsefulFuncs.myPhoneNumber = phone;
            MyUsefulFuncs.myReg_ID = reg_id;

//            Toast.makeText(this,MyUsefulFuncs.myName + ": " + MyUsefulFuncs.myPhoneNumber,LENGTH_LONG).show();
        }
   //     Toast.makeText(this,MyUsefulFuncs.myPhoneNumber,LENGTH_LONG).show();
/****************************************************************
        Intent intent = new Intent(this,SignInActivity.class);
        startActivity(intent);
/****************************************************************/
    }



    @Override
    protected void onStart() {
        super.onStart();
        if(reading == false) {
            spinToday.setSelection(0);
            ReadFromSchedulesTable(DAY);
        }
        if(firstReadSchedules == false){
            fillContactList();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

/************* MENU LISTENER ************************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.menu_all_reminders:
                ReadAllFromSchedulesTable();
                break;
            default:
                ReadAllFromSchedulesTable();
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
/*******************************************************/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        int recurring = schedules.get(deletePosition).getRecurring();
        switch(recurring){
            case 0:
                menu.add(Menu.NONE, MyUsefulFuncs.MENU_DELETE, Menu.NONE, getResources().getString(R.string.delete));
                break;
            case 1:
            case 2:
                menu.add(Menu.NONE, MyUsefulFuncs.MENU_DELETE_REPEATING, Menu.NONE, getResources().getString(R.string.delete_repeating));
                break;
            case 3:
            case 4:
                menu.add(Menu.NONE, MyUsefulFuncs.MENU_DELETE, Menu.NONE, getResources().getString(R.string.delete));
                menu.add(Menu.NONE, MyUsefulFuncs.MENU_DELETE_REPEATING, Menu.NONE, getResources().getString(R.string.delete_repeating));
                break;
        }
/*
        getMenuInflater().inflate(R.menu.context_menu, menu);
        if(schedules != null && schedules.size() >= deletePosition) {
            menu.setHeaderTitle(schedules.get(deletePosition).getText());
        }
*/
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case MyUsefulFuncs.MENU_DELETE:
                delMenu = 0;
                break;
            case MyUsefulFuncs.MENU_DELETE_REPEATING:
                delMenu = 1;
                break;
        }
        initAlertDialog(schedules.get(deletePosition));
        alertDlg.show();

        return super.onContextItemSelected(item);
    }

    private void initAlertDialog(Schedule schedule) {
        alertDlg = new AlertDialog.Builder(this);
        alertDlg.setTitle(getResources().getString(R.string.warning_title));
        alertDlg.setIcon(android.R.drawable.ic_dialog_alert);
        switch (schedule.getRecurring()){
            case 0:
                alertDlg.setMessage(getResources().getString(R.string.warning_msg));
                break;
            case 1:
                alertDlg.setMessage(String.format(getResources().getString(R.string.warning_msg_repeat),
                        getResources().getString(R.string.onceDay),schedule.getFromDate(),schedule.getToDate()));
                break;
            case 2:
                alertDlg.setMessage(String.format(getResources().getString(R.string.warning_msg_repeat),
                        getResources().getString(R.string.weekly),schedule.getFromDate(),schedule.getToDate()));
                break;
            case 3:
                if(delMenu == 0)
                    alertDlg.setMessage(getResources().getString(R.string.warning_msg));
                else if(delMenu == 1)
                    alertDlg.setMessage(String.format(getResources().getString(R.string.warning_msg_repeat),
                            getResources().getString(R.string.onceMonth),schedule.getFromDate(),schedule.getToDate()));
                break;
            case 4:
                if(delMenu == 0)
                    alertDlg.setMessage(getResources().getString(R.string.warning_msg));
                else if(delMenu == 1)
                    alertDlg.setMessage(String.format(getResources().getString(R.string.warning_msg_repeat),
                            getResources().getString(R.string.onceYear),schedule.getFromDate(),schedule.getToDate()));
                break;
        }
        alertDlg.setNegativeButton(getResources().getString(R.string.warning_no),warningClickListener);
        alertDlg.setPositiveButton(getResources().getString(R.string.warning_yes),warningClickListener);
    }

    DialogInterface.OnClickListener warningClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int btn) {
            switch (btn){
                case Dialog.BUTTON_NEGATIVE:
                    break;
                case Dialog.BUTTON_POSITIVE:
                    if(delMenu == 0)
                        deleteSchedule();
                    else if(delMenu == 1)
                        deleteRepeatingSchedule();
                    break;
                default:
                    break;
            }
        }
    };


    private void deleteRepeatingSchedule() {
        final Context context = this;
        final Schedule schedule = schedules.get(deletePosition);

        dbSchedule = new DBSchedule(this);
        final DBContactSchedule dbContactSchedule = new DBContactSchedule(this);
        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected void onPreExecute() {
                pDialog2 = new ProgressDialog(MainActivity.this);
                pDialog2 = ProgressDialog.show(context, "", "Deleting Schedules...", true);
            }

            @Override
            protected Integer doInBackground(Void... voids) {
                delSchedulesID = dbSchedule.ReadSelectedSchedules(schedule);
                if(delSchedulesID.size() <= 0 )
                    return 0;
                boolean flag = dbContactSchedule.deleteSelectedSchedules(delSchedulesID,0,"");
                if(flag == true)
                    return 1;

                return 0;
            }

            @Override
            protected void onPostExecute(Integer status) {
                Schedule delSchedule = new Schedule();
                if(status == 1){
                    for (int i = 0; i <delSchedulesID.size(); i++) {
                        UpdateSchedulestable(delSchedulesID.get(i));
                        for (int j = 0; j <schedules.size(); j++) {
                            if(schedules.get(j).getId() == delSchedulesID.get(i)) {
                                delSchedule = schedules.get(deletePosition);
                                schedules.remove(j);
                                break;
                            }
                        }
                        /*** CANCEL ALARM ***/
                        Intent intent = new Intent(context,AlarmService.class);
                        intent.setAction(AlarmService.ACTION_CANCEL);
                        intent.putExtra(MyUsefulFuncs.SCHEDULE,delSchedule);
                        intent.putExtra(AlarmService.SCHEDULE_ID,deleteScheduleID);
                        startService(intent);
                    }

                    adapter = new ScheduleListAdapter(schedules,getApplicationContext());
                    adapter.setPeriod(0);
                    adapter.notifyDataSetChanged();
                    lvSchedules.setAdapter(adapter);

                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getApplicationContext(),R.anim.list_layout_controller);
                    lvSchedules.setLayoutAnimation(controller);
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.schedule_delete_successfully), LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.schedule_delete_error), LENGTH_LONG).show();
                }
//                pDialog.dismiss();
                try{
                    if(pDialog2.isShowing()){
                        pDialog2.dismiss();
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally
                {
                    pDialog2.dismiss();
                }
            }
        }.execute();

    }

    private void deleteSchedule() {
        final Context context = this;
        dbSchedule = new DBSchedule(this);
        final DBContactSchedule dbContactSchedule = new DBContactSchedule(this);
        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Integer doInBackground(Void... voids) {

                boolean flag = dbContactSchedule.deleteFromDataBase(deleteScheduleID);
                if(flag == true)
                    return 1;
                return 0;
            }

            @Override
            protected void onPostExecute(Integer status) {
                if(status == 1){
                    Schedule delSchedule = schedules.get(deletePosition);
                    schedules.remove(deletePosition);

                    adapter = new ScheduleListAdapter(schedules,getApplicationContext());
                    adapter.setPeriod(0);
                    adapter.notifyDataSetChanged();
                    lvSchedules.setAdapter(adapter);

                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getApplicationContext(),R.anim.list_layout_controller);
                    lvSchedules.setLayoutAnimation(controller);
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.schedule_delete_successfully), LENGTH_LONG).show();
                    UpdateSchedulestable(deleteScheduleID);

                    /*** CANCEL ALARM ***/
                    Intent intent = new Intent(context,AlarmService.class);
                    intent.setAction(AlarmService.ACTION_CANCEL);
                    intent.putExtra(MyUsefulFuncs.SCHEDULE,delSchedule);
                    intent.putExtra(AlarmService.SCHEDULE_ID,deleteScheduleID);

                    startService(intent);
                    /*** CANCEL ALARM ***/
                }
                else{
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.schedule_delete_error), LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void UpdateSchedulestable(final int deleteScheduleID) {
        dbSchedule = new DBSchedule(this);
        final DBContactSchedule dbContactSchedule = new DBContactSchedule(this);
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                int count = dbContactSchedule.GetNumOfSchedules(deleteScheduleID);
                boolean flag = false;
                if(count == 0){
                    flag = dbSchedule.deleteFromDataBase(deleteScheduleID);
                }
                //Log.d(TAG,"Number of remained schedules is " + count);
                if(flag == true){
                    Log.d(TAG, "The schedule_id = " + deleteScheduleID + " was removed from Schedules Table");
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
            }
        }.execute();
    }

    public void fillContactList() {
        // Read MyContacts and fill the contactsList
        final DBContacts dbContacts = new DBContacts(this);
        new AsyncTask<Void,Void,ArrayList<MyContacts>>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected ArrayList<MyContacts> doInBackground(Void... voids) {
                contactsList = dbContacts.ReadContacts();
                return contactsList;
            }

            @Override
            protected void onPostExecute(ArrayList<MyContacts> myContactsList) {
                if(myContactsList.size() > 0){
                    MyUsefulFuncs.contactsList = contactsList;
                }
            }
        }.execute();
    }

    private void initFields() {
        lvSchedules = (ListView) findViewById(R.id.listViewSchedules);
        btnAddGroup = (Button) findViewById(R.id.btnGroups);
        btnAddReminder = (Button) findViewById(R.id.btnAddReminder);
        btnMessages = (Button) findViewById(R.id.btnMessages);
        btnAddGroup.setOnClickListener(this);
        btnAddReminder.setOnClickListener(this);
        btnMessages.setOnClickListener(this);

        lvSchedules.setOnItemClickListener(this);
        lvSchedules.setOnItemLongClickListener(this);
        lvSchedules.setOnItemSelectedListener(this);
        registerForContextMenu(lvSchedules);

        spinToday = (Spinner) findViewById(R.id.spinToday);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.spinToday,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinToday.setAdapter(adapter);
        spinToday.setSelection(0);
        spinToday.setOnItemSelectedListener(spinListener);

    }
    AdapterView.OnItemSelectedListener spinListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            // Set Color to the first spinner item
            ((TextView)adapterView.getChildAt(0)).setTextColor(Color.parseColor("#FFFFFF"));
            ((TextView)adapterView.getChildAt(0)).setTextSize(16f);

//            Log.d(TAG, "onItemSelected: selected item position  = " + spinToday.getSelectedItemPosition());
//            Log.d(TAG, "onItemSelected: selected item text  = " + spinToday.getSelectedItem().toString());
            if(firstReadSchedules == false)
                firstReadSchedules = true;
            else if(firstReadSchedules == true)
                ReadFromSchedulesTable(spinToday.getSelectedItemPosition());
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    public void ReadAllFromSchedulesTable(){
        final Context context = this;
        dbSchedule = new DBSchedule(this);
        final Intent intent = new Intent(this, ShowRemindersActivity.class);

        new AsyncTask<Void, Void, ArrayList<Schedule>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(MainActivity.this);

                pDialog = ProgressDialog.show(context, "", "Reading Schedules...", true);
            }

            @Override
            protected ArrayList<Schedule> doInBackground(Void... params) {
                schedules = dbSchedule.ReadAllMySchedules();
                return schedules;
            }

            @Override
            protected void onPostExecute(ArrayList<Schedule> schedules) {

                if(schedules.size() > 0){
                    intent.putExtra(MyUsefulFuncs.ALL_SCHEDULES,schedules);
                }
                try{
                    if(pDialog.isShowing()){
                        pDialog.dismiss();
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally
                {
                    pDialog.dismiss();
                }


                startActivity(intent);
            }
        }.execute();
    }

    public void ReadFromSchedulesTable(final int period) {
        reading = true;
        dbSchedule = new DBSchedule(this);
        new AsyncTask<Void, Void, ArrayList<Schedule>>() {

            @Override
            protected ArrayList<Schedule> doInBackground(Void... params) {
                switch (period){
                    case DAY:
                        schedules = dbSchedule.ReadFromDataBaseDaily();
                        break;
                    case WEEK:
                        schedules = dbSchedule.ReadFromDataBaseWeek();
                        break;
                    case MONTH:
                        schedules = dbSchedule.ReadFromDataBaseMonth();
                        break;
                    default:
                        schedules = dbSchedule.ReadFromDataBaseDaily();
                }
                return schedules;
            }

            @Override
            protected void onPostExecute(ArrayList<Schedule> schedules) {
                adapter = new ScheduleListAdapter(schedules,getApplicationContext());
                adapter.setPeriod(period);
                adapter.notifyDataSetChanged();
                lvSchedules.setAdapter(adapter);
                if(schedules.size() > 0){
                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getApplicationContext(),R.anim.list_layout_controller);
                    lvSchedules.setLayoutAnimation(controller);
                }

                reading = false;

            }
        }.execute();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        //Log.d(TAG,"onItemClick: " + schedules.get(position).toString());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        //Log.d(TAG,"onItemLongClick: " + schedules.get(position).toString());
        deleteScheduleID = schedules.get(position).getId();
        deletePosition = position;
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        //Log.d(TAG,"onItemSelected");
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.btnAddReminder:
                if(MyUsefulFuncs.myName.equals("") || MyUsefulFuncs.myPhoneNumber.equals("") || MyUsefulFuncs.myReg_ID.equals("")) { // Phone number is not Registered
                    contactsList.clear();
                    MyUsefulFuncs.contactsList.clear();
                }

                intent = new Intent(this,ReminderActivity.class);
                startActivity(intent);
                break;
            case R.id.btnGroups:
                intent = new Intent(this, ShowGroupsActivity.class);
                startActivity(intent);
                break;
            case R.id.btnMessages:
                intent = new Intent(this,MessagesActivity.class);
//                startActivityForResult(intent, MyUsefulFuncs.REQ_CODE_MESSAGES);
                startActivity(intent);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null)
            return;
    }

}
