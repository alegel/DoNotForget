package com.donotforget.user.donotforget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContactGroups;
import com.donotforget.user.donotforget.objects.Group;
import com.donotforget.user.donotforget.objects.MyContacts;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.ShowGroupsListAdapter;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class ShowGroupsActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemLongClickListener {
    private ExpandableListView lvShowGroups;
    private Button btnCompose;
    private ArrayList<Group> groups;
    private ArrayList<String> children = new ArrayList<>();
    private ArrayList<String> contactsArray = new ArrayList<>();
    private ShowGroupsListAdapter adapter;
    private int groupPosition = 0;
    private AlertDialog.Builder alertDlg;
    private ArrayList<MyContacts> contactsList;
    private MyContacts contact;
    private ProgressDialog pDialog;
    private DBContactGroups dbContactGroups;
    private TextView txtUpdateGrp;
    private static final String TAG = "Alex_" + ShowGroupsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_groups);

        txtUpdateGrp = (TextView) findViewById(R.id.txtUpdateGrp);

        btnCompose = (Button) findViewById(R.id.btnComposeGrp);
        btnCompose.setOnClickListener(this);

        lvShowGroups = (ExpandableListView) findViewById(R.id.lvShowGroups);
        lvShowGroups.setOnItemLongClickListener(this);
        fillGroups();
        registerForContextMenu(lvShowGroups);
    }

    private void fillGroups() {
        final Context context = this;
        groups = new ArrayList<>();
        children = new ArrayList<>();
        dbContactGroups = new DBContactGroups(this);
        new AsyncTask<Void, Void, ArrayList<MyContacts>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                pDialog = new ProgressDialog(ShowGroupsActivity.this);
                pDialog = ProgressDialog.show(context, "", "Reading Contacts...", true);
            }

            @Override
            protected ArrayList<MyContacts> doInBackground(Void... voids) {
                contactsList = dbContactGroups.ReadFromDataBase();
                return contactsList;
            }

            @Override
            protected void onPostExecute(ArrayList<MyContacts> contactsList) {
                Group group;
                int group_id;
                String grpName;
                if(contactsList.size() > 0) {
                    for (int i = 0; i <contactsList.size();) {
                        grpName = contactsList.get(i).getGroup();
                        if(grpName != null) {
                            group = new Group(grpName);
                            group.setGroup_id(Integer.parseInt(contactsList.get(i).getId()));
                            while (i < contactsList.size() && contactsList.get(i).getGroup().equalsIgnoreCase(grpName)) {
                                group.children.add(contactsList.get(i++));
                            }
                            groups.add(group);
                        }
                        else {
                            i++;
                        }
                    }

                    adapter = new ShowGroupsListAdapter(getApplicationContext(), groups);
                    lvShowGroups.setAdapter(adapter);

                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getApplicationContext(),R.anim.list_layout_controller);
                    lvShowGroups.setLayoutAnimation(controller);

                    txtUpdateGrp.setVisibility(View.VISIBLE);
                }
                else{
                    Log.d(TAG, "The contactGroups table is empty");
                    txtUpdateGrp.setVisibility(View.GONE);
                }
               // pDialog.dismiss();
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
        Log.d(TAG,"ShowGroupsActivity");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, MyUsefulFuncs.MENU_EDIT, Menu.NONE, getResources().getString(R.string.edit));
        menu.add(Menu.NONE, MyUsefulFuncs.MENU_DELETE, Menu.NONE, getResources().getString(R.string.delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case MyUsefulFuncs.MENU_EDIT:
                //Log.d(TAG, "MENU_EDIT");
                Intent intent = new Intent(this, UpdateGroupActivity.class);
                intent.putExtra(MyUsefulFuncs.GROUP_NAME,groups.get(groupPosition).getGroup_name());
//                intent.putStringArrayListExtra(MyUsefulFuncs.CONTACTS,groups.get(groupPosition).getChildren());
                intent.putExtra(MyUsefulFuncs.CONTACTS,groups.get(groupPosition).getChildren());
                startActivityForResult(intent, MyUsefulFuncs.REQ_CODE_CONTACTS);
                break;
            case MyUsefulFuncs.MENU_DELETE:
               deleteGroup();

                break;
        }

        return super.onContextItemSelected(item);
    }

    private void deleteGroup() {
        final Context context = this;
        dbContactGroups = new DBContactGroups(this);
        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Integer doInBackground(Void... voids) {

                boolean flag = dbContactGroups.deleteFromDataBase(groups.get(groupPosition).getGroup_name());
                if(flag == true)
                    return 1;
                return 0;
            }

            @Override
            protected void onPostExecute(Integer status) {
                if(status == 1){
                    Group group = groups.get(groupPosition);
                    groups.remove(groupPosition);

                    adapter = new ShowGroupsListAdapter(getApplicationContext(), groups);
                    adapter.notifyDataSetChanged();
                    lvShowGroups.setAdapter(adapter);

                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getApplicationContext(),R.anim.list_layout_controller);
                    lvShowGroups.setLayoutAnimation(controller);

                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.group_delete_successfully), LENGTH_LONG).show();

                }
                else{
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.group_delete_error), LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    DialogInterface.OnClickListener warningClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int btn) {
                switch (btn){
                    case Dialog.BUTTON_NEGATIVE:
                        break;
                    case Dialog.BUTTON_POSITIVE:
                        groups.remove(groupPosition);
                        adapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };

    @Override
    public void onClick(View view) {    // Compose a group Clicked
        Intent intent = new Intent(this, UpdateGroupActivity.class);
        if(groups != null && groups.size() > 0) {
            intent.putExtra(MyUsefulFuncs.GROUPS, groups);
        }
        startActivityForResult(intent, MyUsefulFuncs.REQ_CODE_COMPOSE_GRP);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        groupPosition = position;

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null)
            return;
        if(requestCode == MyUsefulFuncs.REQ_CODE_CONTACTS){
            groups.get(groupPosition).setChildren((ArrayList<MyContacts>) data.getSerializableExtra(MyUsefulFuncs.CONTACTS));
            groups.get(groupPosition).setGroup_name(data.getStringExtra(MyUsefulFuncs.GROUP_NAME));
            adapter.notifyDataSetChanged();
        }
        else if(requestCode == MyUsefulFuncs.REQ_CODE_COMPOSE_GRP){
            Group group = new Group(data.getStringExtra(MyUsefulFuncs.GROUP_NAME));
            ArrayList<MyContacts> newContactsList = (ArrayList<MyContacts>) data.getSerializableExtra(MyUsefulFuncs.CONTACTS);
            group.setChildren(newContactsList);
            groups.add(group);

            addToDataBase(newContactsList);
        }
    }

    private void addToDataBase(final ArrayList<MyContacts> newContactsList) {
        final DBContactGroups dbContactGroups = new DBContactGroups(this);
        final Context context = this;
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return dbContactGroups.addToDataBase(newContactsList);
            }
            @Override
            protected void onPostExecute(Integer result) {
                if (result == 1) {
                    fillGroups();

                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.add_group_db_successfully), LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.add_db_err), LENGTH_LONG).show();
                }

            }
        }.execute();
    }
}
