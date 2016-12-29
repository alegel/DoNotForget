package com.donotforget.user.donotforget;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContactGroups;
import com.donotforget.user.donotforget.interfaces.IFragmentsEventListener;
import com.donotforget.user.donotforget.objects.Group;
import com.donotforget.user.donotforget.objects.GroupListAdapter;
import com.donotforget.user.donotforget.objects.MyContacts;
import com.donotforget.user.donotforget.objects.MyMessages;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class ReminderActivity extends AppCompatActivity implements IFragmentsEventListener, View.OnClickListener, View.OnLongClickListener {
    private Spinner spinRemindTo;
    private EditText eMessage;
    private Button btnSetSchedule;
    private int curRemindToState = 0;
    private ContactsFragment contactsFragment;
    private GroupsFragment groupsFragment;
    private FragmentTransaction fragmentTransaction;
    private ListView lvContacts;
    private GroupListAdapter groupListAdapter;
    private String strMessageText;
    private ArrayList<MyContacts> contactsList = new ArrayList<>();
    private MyContacts contact;
    ArrayList<Group> groups;
    public ArrayList<String> children = new ArrayList<>();
    private ProgressDialog pDialog;
    private DBContactGroups dbContactGroups;
    private static final String TAG = "Alex_" + ReminderActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        //fillGroupList();
        initFields();
        fillContactList();
        fillGroupList();
    }

    private void fillGroupList() {
        final Context context = this;
        groups = new ArrayList<>();
        children = new ArrayList<>();
        dbContactGroups = new DBContactGroups(this);
        new AsyncTask<Void, Void, ArrayList<MyContacts>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(ReminderActivity.this);
                pDialog = ProgressDialog.show(context, "", getResources().getText(R.string.reading_contact_groups), true);
            }

            @Override
            protected ArrayList<MyContacts> doInBackground(Void... voids) {
                contactsList = dbContactGroups.ReadFromDataBase();
                return contactsList;
            }

            @Override
            protected void onPostExecute(ArrayList<MyContacts> contactsList) {
                Group group;
                String grpName;
                if(contactsList.size() > 0) {
                    for (int i = 0; i <contactsList.size();) {
                        grpName = contactsList.get(i).getGroup();
                        if(grpName != null) {
                            group = new Group(grpName);
                            while (i < contactsList.size() && contactsList.get(i).getGroup().equalsIgnoreCase(grpName)) {
                                group.children.add(contactsList.get(i++));
                            }
                            groups.add(group);
                        }
                        else {
                            i++;
                        }
                    }
                    if(groupListAdapter == null) {
                        groupListAdapter = new GroupListAdapter(context, groups);
                        groupsFragment.adapter = groupListAdapter;
                    }
                    else{
                        groupsFragment.adapter.notifyDataSetChanged();
                    }
                }
                else{
                    Log.d(TAG, "The contactGroups table is empty");

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

    private void fillContactList() {
        final Context context = this;
        // Read MyContacts and fill the contactsList
        if(MyUsefulFuncs.contactsList.size() > 0) {
            contactsFragment.contacts = MyUsefulFuncs.contactsList;
            for (int i = 0; i <contactsFragment.contacts.size(); i++) {
                contactsFragment.contacts.get(i).setSelected(0);
            }
            if(contactsFragment.adapter != null)
                contactsFragment.adapter.notifyDataSetChanged();
        }
    }

    private void initFields() {
        spinRemindTo = (Spinner) findViewById(R.id.spinRemindTo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.spinRemindTo,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRemindTo.setAdapter(adapter);
        spinRemindTo.setSelection(0);
        spinRemindTo.setOnItemSelectedListener(spinListener);

        eMessage = (EditText) findViewById(R.id.editMessage);
        eMessage.setOnLongClickListener(this);

        btnSetSchedule = (Button) findViewById(R.id.btnSchedule);
        btnSetSchedule.setOnClickListener(this);

        contactsFragment = new ContactsFragment();
        groupsFragment = new GroupsFragment();

    }

    AdapterView.OnItemSelectedListener spinListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            // Set Color to the first spinner item
            ((TextView)adapterView.getChildAt(0)).setTextColor(Color.parseColor("#FFFFFF"));
            ((TextView)adapterView.getChildAt(0)).setTextSize(16f);

//            Log.d(TAG, "onItemSelected: selected item position  = " + spinRemindTo.getSelectedItemPosition());
//            Log.d(TAG, "onItemSelected: selected item text  = " + spinRemindTo.getSelectedItem().toString());

            fragmentTransaction = getSupportFragmentManager().beginTransaction();

            switch (spinRemindTo.getSelectedItemPosition()){
                case 0:         // Remind To Myself
                    if(curRemindToState == 1){
                        fragmentTransaction.remove(contactsFragment);

                    }
                    else if(curRemindToState == 2){
                        fragmentTransaction.remove(groupsFragment);

                    }
                    curRemindToState = 0;
                    break;
                case 1:         // Remind To MyContacts
                    if(curRemindToState == 0){
                        //fillContactList();
                        fragmentTransaction.add(R.id.container,contactsFragment);

                    }
                    else if(curRemindToState == 2){
                       // fillContactList();
                        fragmentTransaction.remove(groupsFragment);
                        fragmentTransaction.add(R.id.container,contactsFragment);
                    }
                    curRemindToState = 1;
                    break;
                case 2:         // Remind To Group
                    if(curRemindToState == 0){
                        fillGroupList();
                        fragmentTransaction.add(R.id.container,groupsFragment);

                    }
                    else if(curRemindToState == 1){
                        fragmentTransaction.remove(contactsFragment);
                        fillGroupList();
                        fragmentTransaction.add(R.id.container,groupsFragment);
                    }
                    curRemindToState = 2;
                    break;
                default:        // Remind To Myself
                    if(curRemindToState == 1){
                        fragmentTransaction.remove(contactsFragment);

                    }
                    else if(curRemindToState == 2){
                        fragmentTransaction.remove(groupsFragment);

                    }
                    curRemindToState = 0;
                    break;
            }

            fragmentTransaction.commit();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    @Override
    public void ContactsEvent(ArrayList<String> contacts) {

    }

    @Override
    public void GroupsEvent(ArrayList<String> groups) {

    }

    @Override
    public void onClick(View view) {
         // "Set Schedule" Button Clicked
            MyContacts contact = new MyContacts();
            ArrayList<MyContacts> myContactsArrList = new ArrayList<>();
            if (eMessage.getText().toString().isEmpty()) {
                Toast.makeText(this, getResources().getText(R.string.msg_empty), LENGTH_LONG).show();
                eMessage.requestFocus();
                return;
            }
            strMessageText = eMessage.getText().toString();

            if (curRemindToState == 1) {     // Remind to MyContacts
                lvContacts = (ListView) contactsFragment.getView().findViewById(R.id.lvContacts);
                for (int i = 0; i < contactsFragment.contacts.size(); i++) {
                    if (contactsFragment.contacts.get(i).isSelected() == 1) {
                        myContactsArrList.add(contactsFragment.contacts.get(i));
                        Log.d(TAG, "Choosen MyContacts: " + contactsFragment.contacts.get(i).getName());
                    }
                }
                if (myContactsArrList.size() <= 0) {
                    Toast.makeText(this, getResources().getText(R.string.contacts_empty), LENGTH_LONG).show();
                    return;
                }
/***************************************************************************************************/

            } else if (curRemindToState == 2) {     // Remind to Groups
                if (groupListAdapter.dataSet.size() <= 0) {
                    Toast.makeText(this, getResources().getText(R.string.groups_empty), LENGTH_LONG).show();
                    return;
                }
                for (int i = 0; i < groupListAdapter.dataSet.size(); i++) {
                    if (groupListAdapter.dataSet.get(i).isChecked == true) {
                        for (int j = 0; j < groupListAdapter.dataSet.get(i).getChildren().size(); j++) {
                            contact = groupListAdapter.dataSet.get(i).getChildren().get(j);
                            myContactsArrList.add(contact);
                            //Log.d(TAG, "Contact from group" + groupListAdapter.dataSet.get(i).getGroup_name() + ": " + contact.getName());
                        }
                    }
                }
            }
            Intent intent = new Intent(this, ScheduleActivity.class);
            intent.putExtra(MyUsefulFuncs.MESSAGE_TEXT, strMessageText);
            intent.putExtra(MyUsefulFuncs.SCHEDULE_OWNER, spinRemindTo.getSelectedItemPosition());
            if (myContactsArrList.size() > 0) {
                intent.putExtra(MyUsefulFuncs.CONTACTS, myContactsArrList);
            }
            startActivity(intent);
    }

    private void findContact() {
        Toast.makeText(this,"Find Contact",LENGTH_LONG).show();
    }

    @Override
    public boolean onLongClick(View view) {

        Intent intent = new Intent(this,AddPreparedMsg.class);
        startActivityForResult(intent,MyUsefulFuncs.REQ_CODE_PREPARED_MSG);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null)
            return;
        if(requestCode == MyUsefulFuncs.REQ_CODE_PREPARED_MSG){
            MyMessages msg = (MyMessages) data.getSerializableExtra(MyUsefulFuncs.MESSAGE_POSITION);
            //Log.d(TAG,"message selected: " + msg.getMessage());
            eMessage.setText(msg.getMessage());
        }
    }
}
