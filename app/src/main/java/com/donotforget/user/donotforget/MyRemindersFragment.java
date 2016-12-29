package com.donotforget.user.donotforget;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContactSchedule;
import com.donotforget.user.donotforget.DBObjects.DBSchedule;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;
import com.donotforget.user.donotforget.objects.ScheduleListAdapter;
import com.donotforget.user.donotforget.services.AlarmService;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyRemindersFragment extends Fragment implements AdapterView.OnItemLongClickListener {
    private ListView lvMyReminders;
    public ArrayList<Schedule> schedules = new ArrayList<>();
    public ScheduleListAdapter adapter;
    public Context context;
    private int deleteScheduleID = -1, deletePosition = -1;
    public AlertDialog.Builder alertDlg;
    private DBSchedule dbSchedule;
    private int delMenu = -1;
    private ProgressDialog pDialog;
    private ArrayList<Integer> delSchedulesID = new ArrayList<>();
    private static final String TAG = "Alex_" + MyRemindersFragment.class.getSimpleName();

    public MyRemindersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_reminders, null);
        if(schedules.size() > 0) {
            lvMyReminders = (ListView) view.findViewById(R.id.lvMyReminders);
            adapter = new ScheduleListAdapter(schedules, context);
            adapter.notifyDataSetChanged();
            lvMyReminders.setAdapter(adapter);
            lvMyReminders.setOnItemLongClickListener(this);
            LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.list_layout_controller);
            lvMyReminders.setLayoutAnimation(controller);
            registerForContextMenu(lvMyReminders);
        }
        else {
            Toast.makeText(context,getResources().getString(R.string.my_schedules_empty),Toast.LENGTH_LONG).show();
        }

        return view;
    }

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
                menu.add(Menu.NONE, MyUsefulFuncs.MENU_DELETE_REPEATING, Menu.NONE, getResources().getString(R.string.delete_repeating));
                break;
            case 3:
            case 4:
                menu.add(Menu.NONE, MyUsefulFuncs.MENU_DELETE, Menu.NONE, getResources().getString(R.string.delete));
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
                        context.startService(intent);
                    }

                    adapter = new ScheduleListAdapter(schedules,context);
                    adapter.notifyDataSetChanged();
                    lvMyReminders.setAdapter(adapter);

                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context,R.anim.list_layout_controller);
                    lvMyReminders.setLayoutAnimation(controller);
                    Toast.makeText(context, getResources().getText(R.string.schedule_delete_successfully), LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context, getResources().getText(R.string.schedule_delete_error), LENGTH_LONG).show();
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
        final DBContactSchedule dbContactSchedule = new DBContactSchedule(context);
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

                    adapter = new ScheduleListAdapter(schedules,context);
                    adapter.notifyDataSetChanged();
                    lvMyReminders.setAdapter(adapter);
                    UpdateSchedulestable(deleteScheduleID);
                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context,R.anim.list_layout_controller);
                    lvMyReminders.setLayoutAnimation(controller);
                    Toast.makeText(context, getResources().getText(R.string.reminder_delete_successfully), LENGTH_LONG).show();

                    /*** CANCEL ALARM ***/
                    Intent intent = new Intent(context,AlarmService.class);
                    intent.setAction(AlarmService.ACTION_CANCEL);
                    intent.putExtra(MyUsefulFuncs.SCHEDULE,delSchedule);
                    intent.putExtra(AlarmService.SCHEDULE_ID,deleteScheduleID);

                    context.startService(intent);
                    /*** CANCEL ALARM ***/
                }
                else{
                    Toast.makeText(context, getResources().getText(R.string.reminder_delete_error), LENGTH_LONG).show();
                }
            }
        }.execute();
    }
}
