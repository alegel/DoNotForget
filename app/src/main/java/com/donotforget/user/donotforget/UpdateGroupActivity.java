package com.donotforget.user.donotforget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.donotforget.user.donotforget.objects.ContactsListAdapter;
import com.donotforget.user.donotforget.objects.Group;
import com.donotforget.user.donotforget.objects.MyContacts;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class UpdateGroupActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText eGroupName;
    private ListView lvShowContacts;
    private Button btnSaveContacts;
    private static final String TAG = "Alex_" + UpdateGroupActivity.class.getSimpleName();
//    private ArrayAdapter<MyContacts> adapter;
    private ContactsListAdapter adapter;
    private ArrayList<Group> groups;
    public ArrayList<MyContacts> contactsList = new ArrayList<>();
    public ArrayList<MyContacts> newContactsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group);

        btnSaveContacts = (Button) findViewById(R.id.btnSaveContacts);
        btnSaveContacts.setOnClickListener(this);
        eGroupName = (EditText) findViewById(R.id.eGroupName);
        lvShowContacts = (ListView) findViewById(R.id.lvShowContacts);

        Intent intent = getIntent();
        if(intent.hasExtra(MyUsefulFuncs.GROUP_NAME)) {
            eGroupName.setText(intent.getStringExtra(MyUsefulFuncs.GROUP_NAME));
        }
        if(intent.hasExtra(MyUsefulFuncs.GROUPS))
        {
            groups = (ArrayList<Group>) intent.getSerializableExtra(MyUsefulFuncs.GROUPS);
        }
        for (int i = 0; i <MyUsefulFuncs.contactsList.size(); i++) {
            MyUsefulFuncs.contactsList.get(i).setSelected(0);
        }
        adapter = new ContactsListAdapter(MyUsefulFuncs.contactsList,this);
        lvShowContacts.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {        // Save Button Clicked
        if(eGroupName.getText().toString().isEmpty()){
            Toast.makeText(this,getResources().getString(R.string.group_name_empty),Toast.LENGTH_LONG).show();
            eGroupName.requestFocus();
            return;
        }
        if(groups != null && groups.size() > 0){
            for (int i = 0; i < groups.size(); i++) {
                if(eGroupName.getText().toString().equalsIgnoreCase(groups.get(i).getGroup_name())){
                    Toast.makeText(this,getResources().getString(R.string.group_name_exists),Toast.LENGTH_LONG).show();
                    eGroupName.requestFocus();
                    return;
                }
            }
        }
        MyContacts contact;
        for (int i = 0; i <adapter.contacts.size(); i++) {
            if(adapter.contacts.get(i).isSelected() == 1){

                contact = new MyContacts();
                contact.setName(adapter.contacts.get(i).getName());
                contact.setGroup(eGroupName.getText().toString());
                contact.setSelected(1);
                contact.setPhone(adapter.contacts.get(i).getPhone());

                newContactsList.add(contact);
                Log.d(TAG, "Choosen MyContacts: " + adapter.contacts.get(i).getName());
            }
        }
        if(newContactsList.size() <= 0){
            Toast.makeText(this,getResources().getText(R.string.contacts_empty), LENGTH_LONG).show();
            return;
        }
/*****************************************************************************************/
        Intent intent = new Intent();
        intent.putExtra(MyUsefulFuncs.CONTACTS, newContactsList);   // Selected contacts
        intent.putExtra(MyUsefulFuncs.GROUP_NAME,eGroupName.getText().toString());
        setResult(RESULT_OK,intent);
        finish();
    }
}
