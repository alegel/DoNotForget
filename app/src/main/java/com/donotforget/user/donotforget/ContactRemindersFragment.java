package com.donotforget.user.donotforget;


import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContactSchedule;
import com.donotforget.user.donotforget.DBObjects.DBContacts;
import com.donotforget.user.donotforget.DBObjects.DBSchedule;
import com.donotforget.user.donotforget.objects.ContactSchedule;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;
import com.donotforget.user.donotforget.objects.ScheduleListAdapter;
import com.donotforget.user.donotforget.services.AlarmService;
import com.donotforget.user.donotforget.services.MarkToDeleteSchedulesReceiver;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactRemindersFragment extends Fragment implements AdapterView.OnItemLongClickListener {
    private Spinner spinContacts;
    private ListView lvSchedules;
    private ArrayList<Schedule> schedules = new ArrayList<>();
    public ArrayList<ContactSchedule> contactSchedulesList = new ArrayList<>();
    public ScheduleListAdapter adapter;
    public Context context;
    private DBSchedule dbSchedule;
    private DBContacts dbContacts;
    private String phone;
    private ProgressDialog pDialog;
    private String contactName;
    ArrayAdapter<ContactSchedule> adapterSpin;
    private int deleteScheduleID = -1, deletePosition = -1;
    public AlertDialog.Builder alertDlg;
    private int delMenu = -1;
    private Intent intent;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private ArrayList<Integer> delSchedulesID = new ArrayList<>();
    private static final String TAG = "Alex_" + ContactRemindersFragment.class.getSimpleName();


    public ContactRemindersFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_reminders, null);

        if(contactSchedulesList != null && contactSchedulesList.size() > 0) {
            spinContacts = (Spinner) view.findViewById(R.id.spinContacts);
            adapterSpin = new ArrayAdapter<ContactSchedule>(context, android.R.layout.simple_spinner_item, contactSchedulesList);
            adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinContacts.setAdapter(adapterSpin);
            spinContacts.setSelection(0);
            contactName = spinContacts.getSelectedItem().toString();
            spinContacts.setOnItemSelectedListener(spinListener);
            ReadAllFromSchedulesTable();

            lvSchedules = (ListView) view.findViewById(R.id.lvContactSchedules);
            lvSchedules.setOnItemLongClickListener(this);

            registerForContextMenu(lvSchedules);
        }

        return view;
    }

    public void ReadAllFromSchedulesTable(){
        dbSchedule = new DBSchedule(context);
        new AsyncTask<Void, Void, ArrayList<Schedule>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected ArrayList<Schedule> doInBackground(Void... params) {
                schedules = dbSchedule.ReadContactSchedules(contactName);
                return schedules;
            }

            @Override
            protected void onPostExecute(ArrayList<Schedule> schedules) {
                adapter = new ScheduleListAdapter(schedules,context);
                adapter.notifyDataSetChanged();
                lvSchedules.setAdapter(adapter);
                if(schedules.size() > 0){

                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context,R.anim.list_layout_controller);
                    lvSchedules.setLayoutAnimation(controller);
                }
                else{
                    //Log.d(TAG,"The Schedules Table is Empty");
                }


            }
        }.execute();
    }

    AdapterView.OnItemSelectedListener spinListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            // Set Color to the first spinner item
            ((TextView)adapterView.getChildAt(0)).setTextColor(Color.parseColor("#FFFFFF"));
            Log.d(TAG, "onItemSelected: selected item position  = " + spinContacts.getSelectedItemPosition());
            //Log.d(TAG, "onItemSelected: selected item text  = " + spinContacts.getSelectedItem().toString());
            contactName = spinContacts.getSelectedItem().toString();
            ReadAllFromSchedulesTable();

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        //Log.d(TAG,"onItemLongClick: " + schedules.get(position).toString());
        deleteScheduleID = schedules.get(position).getId();
        deletePosition = position;
        return false;
    }

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
            case 3:
            case 4:
                menu.add(Menu.NONE, MyUsefulFuncs.MENU_DELETE_REPEATING, Menu.NONE, getResources().getString(R.string.delete_repeating));
                break;
        }
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
        alertDlg = new AlertDialog.Builder(context);
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
        final Schedule schedule = schedules.get(deletePosition);
        dbContacts = new DBContacts(context);
        dbSchedule = new DBSchedule(context);
        final DBContactSchedule dbContactSchedule = new DBContactSchedule(context);
        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected void onPreExecute() {
                pDialog = new ProgressDialog(context);
                pDialog = ProgressDialog.show(context, "", "Deleting Schedules...", true);
            }

            @Override
            protected Integer doInBackground(Void... voids) {
                delSchedulesID = dbSchedule.ReadSelectedSchedules(schedule);

             //   phone = dbContacts.getPhone(contactName);   // SHOULD BE OPENED FOR TESTS ONLY
/* SHOULD BE CLOSED FOR TESTS ONLY */
                if(delSchedulesID.size() <= 0 )
                    return 0;
                boolean flag = dbContactSchedule.deleteSelectedSchedules(delSchedulesID,1,contactName);
                if(flag == true) {
                    phone = dbContacts.getPhone(contactName);
                    if(phone.equals(""))
                        return 0;
                    return 1;
                }
                return 0;
/**/
  //             return 1;   // SHOULD BE OPENED FOR TESTS ONLY
            }

            @Override
            protected void onPostExecute(Integer status) {
                if(status == 1){
/*                    // SHOULD BE OPENED  */
                    for (int i = 0; i <delSchedulesID.size(); i++) {
                        UpdateSchedulestable(delSchedulesID.get(i));
                        for (int j = 0; j <schedules.size(); j++) {
                            if(schedules.get(j).getId() == delSchedulesID.get(i)) {
                                schedules.remove(j);
                                break;
                            }
                        }
                    }
/**/
                    adapter = new ScheduleListAdapter(schedules,context);
                    adapter.notifyDataSetChanged();
                    lvSchedules.setAdapter(adapter);

                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context,R.anim.list_layout_controller);
                    lvSchedules.setLayoutAnimation(controller);
/**************** Call to BroadcastReceiver in order to update the Web *****************************/
                    intent = new Intent(context,MarkToDeleteSchedulesReceiver.class);
                    intent.setAction(MarkToDeleteSchedulesReceiver.BROADCAST_MARK_DELETE_SCHEDULES);
                    intent.putExtra(MyUsefulFuncs.DELETE_SCHEDULES,delSchedulesID);
                    intent.putExtra(MyUsefulFuncs.USER_PHONE,phone);

                    pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + 1,pendingIntent);
/***************************************************************************************************/
//                    Toast.makeText(context, getResources().getText(R.string.reminder_delete_successfully), LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context, getResources().getText(R.string.reminder_delete_error), LENGTH_LONG).show();
                }
                //pDialog.dismiss();
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
            }
        }.execute();

    }

    private void UpdateSchedulestable(final int deleteScheduleID) {
        dbSchedule = new DBSchedule(context);
        final DBContactSchedule dbContactSchedule = new DBContactSchedule(context);
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

    private void deleteSchedule() {
        dbContacts = new DBContacts(context);
        final DBContactSchedule dbContactSchedule = new DBContactSchedule(context);
        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected Integer doInBackground(Void... voids) {

    //            phone = dbContacts.getPhone(contactName);   // SHOULD BE OPENED FOR TESTS ONLY
/* SHOULD BE OPENED FOR TESTS ONLY ***/
              boolean flag = dbContactSchedule.deleteFromDataBase(deleteScheduleID,contactName);
                if(flag == true) {
                    phone = dbContacts.getPhone(contactName);
                    if(phone.equals(""))
                        return 0;
                    return 1;
                }
                return 0;
/**/
  //              return 1;   // SHOULD BE OPENED FOR TESTS ONLY
            }

            @Override
            protected void onPostExecute(Integer status) {
                if(status == 1){
                    schedules.remove(deletePosition);

                    adapter = new ScheduleListAdapter(schedules,context);
                    adapter.notifyDataSetChanged();
                    lvSchedules.setAdapter(adapter);
                    UpdateSchedulestable(deleteScheduleID);   // SHOULD BE CLOSED FOR TESTS ONLY
                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context,R.anim.list_layout_controller);
                    lvSchedules.setLayoutAnimation(controller);
/**************** Call to BroadcastReceiver in order to update the Web *****************************/
                    intent = new Intent(context,MarkToDeleteSchedulesReceiver.class);
                    intent.setAction(MarkToDeleteSchedulesReceiver.BROADCAST_MARK_DELETE_SCHEDULES);
                    intent.putExtra(MyUsefulFuncs.DELETE_SCHEDULE,deleteScheduleID);
                    intent.putExtra(MyUsefulFuncs.USER_PHONE,phone);

                    pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + 1,pendingIntent);
/***************************************************************************************************/
                }
                else{
                    Toast.makeText(context, getResources().getText(R.string.reminder_delete_error), LENGTH_LONG).show();
                }
            }
        }.execute();
    }
}
