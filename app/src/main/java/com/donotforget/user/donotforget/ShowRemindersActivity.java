package com.donotforget.user.donotforget;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContactSchedule;
import com.donotforget.user.donotforget.objects.ContactSchedule;
import com.donotforget.user.donotforget.objects.MyContacts;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;

import java.util.ArrayList;

public class ShowRemindersActivity extends AppCompatActivity {
    private ContactRemindersFragment contactRemindersFragment;
    private MyRemindersFragment myRemindersFragment;
    private FragmentTransaction fragmentTransaction;
    private Spinner spinReminders;
    private int curRemindersToState = 0;
    public ArrayList<Schedule> schedules = new ArrayList<>();
    private TextView tvShowReminders;
    private ArrayList<ContactSchedule> contactSchedulesList = new ArrayList<>();
    private MyContacts contact;
    private ProgressDialog pDialog;
    private DBContactSchedule dbContactSchedule;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_reminders);

        initFields();
        context = this;
    }

    private void initFields() {
        spinReminders = (Spinner) findViewById(R.id.spinReminders);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.spinRemindersTo,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinReminders.setAdapter(adapter);
        spinReminders.setSelection(0);
        spinReminders.setOnItemSelectedListener(spinListener);

        tvShowReminders = (TextView) findViewById(R.id.txtMessageReminders);
        myRemindersFragment = new MyRemindersFragment();
        contactRemindersFragment = new ContactRemindersFragment();
        contactRemindersFragment.context = this;
        Intent intent = getIntent();
        if(intent.hasExtra(MyUsefulFuncs.ALL_SCHEDULES)){
            schedules = (ArrayList<Schedule>) intent.getSerializableExtra(MyUsefulFuncs.ALL_SCHEDULES);
            myRemindersFragment.schedules = schedules;
            if(myRemindersFragment.adapter != null){
                myRemindersFragment.adapter.notifyDataSetChanged();
            }
        }
        myRemindersFragment.context = this;
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.reminders_container,myRemindersFragment);
        fragmentTransaction.commit();
    }

    private void fillContactList() {
        // Read MyContacts and fill the contacts Spin
        if(contactSchedulesList.size() > 0) {
            contactRemindersFragment.contactSchedulesList = contactSchedulesList;
            fragmentTransaction.add(R.id.reminders_container,contactRemindersFragment);
            fragmentTransaction.commit();
        }
        else {
            dbContactSchedule = new DBContactSchedule(this);
            new AsyncTask<Void, Void, ArrayList<ContactSchedule>>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    pDialog = new ProgressDialog(ShowRemindersActivity.this);

                    pDialog = ProgressDialog.show(context, "", "Reading Contacts...", true);
                }

                @Override
                protected ArrayList<ContactSchedule> doInBackground(Void... voids) {
                    contactSchedulesList = dbContactSchedule.ReadContacts();
                    return contactSchedulesList;
                }

                @Override
                protected void onPostExecute(ArrayList<ContactSchedule> contactsList) {
                    contactRemindersFragment.contactSchedulesList = contactSchedulesList;
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
                    if(contactRemindersFragment.contactSchedulesList.size() <= 0){
                        Toast.makeText(context,getResources().getString(R.string.contact_schedules_empty), Toast.LENGTH_LONG).show();
                    }
                    fragmentTransaction.add(R.id.reminders_container,contactRemindersFragment);
                    fragmentTransaction.commit();

                }

            }.execute();
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
    }

    AdapterView.OnItemSelectedListener spinListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            // Set Color to the first spinner item
            ((TextView)adapterView.getChildAt(0)).setTextColor(Color.parseColor("#FFFFFF"));
            ((TextView)adapterView.getChildAt(0)).setTextSize(16f);
            //Log.d(TAG, "onItemSelected: selected item position  = " + spinReminders.getSelectedItemPosition());

            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            switch (spinReminders.getSelectedItemPosition()){
                case 0:
                    if(curRemindersToState == 1) {
                        fragmentTransaction.remove(contactRemindersFragment);
                        fragmentTransaction.add(R.id.reminders_container,myRemindersFragment);
                        fragmentTransaction.commit();
                    }
                    curRemindersToState = 0;
                    break;
                case 1:
                    if(curRemindersToState == 0){
                        fragmentTransaction.remove(myRemindersFragment);

                        fillContactList();

                    }
                    curRemindersToState = 1;
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
}
