package com.donotforget.user.donotforget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBMessages;
import com.donotforget.user.donotforget.objects.MyMessages;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class MessagesActivity extends AppCompatActivity {
    private EditText eMsgTxt;
    private Button btnAdd, btnEdit, btnDelete, btnSave;
    private ListView lvMessages;
    private ArrayList<MyMessages> messages;// = new ArrayList<>();
    private ArrayAdapter<MyMessages> adapter;
    private MyMessages message;
    private DBMessages dbMessages;
    private int itemPos = 0;
    private AlertDialog.Builder alertDlg;
    private boolean flag = true;
    private ProgressDialog pDialog;
    private static final String TAG = "Alex_" + MessagesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        initFields();
        fillMessages();
//        adapter = new ArrayAdapter<MyMessages>(this,android.R.layout.simple_list_item_single_choice,messages);
//        lvMessages.setAdapter(adapter);
    }

    private void initAlertDialog() {
        alertDlg = new AlertDialog.Builder(this);
        alertDlg.setTitle(getResources().getString(R.string.warning_title));
        alertDlg.setIcon(android.R.drawable.ic_dialog_alert);
        alertDlg.setMessage(getResources().getString(R.string.warning_msg));
        alertDlg.setNegativeButton(getResources().getString(R.string.warning_no),warningClickListener);
        alertDlg.setPositiveButton(getResources().getString(R.string.warning_yes),warningClickListener);
    }

    DialogInterface.OnClickListener warningClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int btn) {
            switch (btn){
                case Dialog.BUTTON_NEGATIVE:
                    UncheckItems();
                    break;
                case Dialog.BUTTON_POSITIVE:
                    messages.remove(itemPos);
                    adapter.notifyDataSetChanged();
                    eMsgTxt.requestFocus();
                    UncheckItems();
                    break;
                default:
                    UncheckItems();
                    break;
            }
        }
    };

    private void fillMessages() {
        final Context context = this;
        dbMessages = new DBMessages(this);
        new AsyncTask<Void, Void, ArrayList<MyMessages>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(MessagesActivity.this);
                pDialog = ProgressDialog.show(context, "", "Reading Messages...", true);
            }

            @Override
            protected ArrayList<MyMessages> doInBackground(Void... params) {
                messages = dbMessages.ReadFromDataBase();
                return messages;
            }
            @Override
            protected void onPostExecute(ArrayList<MyMessages> messages) {
                if(messages.size() > 0){
                    adapter = new ArrayAdapter<MyMessages>(context,android.R.layout.simple_list_item_single_choice,messages);
                    adapter.notifyDataSetChanged();
                    lvMessages.setAdapter(adapter);
                    for (int i = 0; i <messages.size(); i++) {
                        Log.d(TAG,"row[" + i + "] =" + messages.get(i).toString() );
                    }
                }
                else{
                    Log.d(TAG,"The MyMessages Table is Empty");
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
            }
        }.execute();

    }

    private void initFields() {
        eMsgTxt = (EditText) findViewById(R.id.eMsgText);
        btnAdd = (Button) findViewById(R.id.btnAddMsg);
        btnEdit = (Button) findViewById(R.id.btnEditMsg);
        btnDelete = (Button) findViewById(R.id.btnDeleteMsg);
        btnSave = (Button) findViewById(R.id.btnMsgSave);

        lvMessages = (ListView) findViewById(R.id.lvMessages);


    }

    public void onClickAddMessage(View view) {
        if(eMsgTxt.getText().toString().isEmpty()){
            Toast.makeText(this,getResources().getString(R.string.msg_empty),Toast.LENGTH_LONG).show();
            eMsgTxt.requestFocus();
            return;
        }
        message = new MyMessages();
        message.setMessage(eMsgTxt.getText().toString());
        messages.add(message);
        if(adapter == null){
            adapter = new ArrayAdapter<MyMessages>(this,android.R.layout.simple_list_item_single_choice,messages);
            lvMessages.setAdapter(adapter);
        }
        else {
            adapter.notifyDataSetChanged();
        }
        eMsgTxt.setText("");
        eMsgTxt.requestFocus();

        UncheckItems();
    }

    public void onClickEditMsg(View view) {
        int pos = lvMessages.getCheckedItemPosition();
        if(pos < 0 || messages.size() <= 0 ){       // Item was not selected
            Toast.makeText(this,getResources().getString(R.string.select_msg_edit_err),Toast.LENGTH_LONG).show();
            eMsgTxt.requestFocus();
            return;
        }

        Log.d(TAG,"Selected message is: " + messages.get(pos));
        String strMsg = eMsgTxt.getText().toString();
        if(strMsg.isEmpty()){
            Toast.makeText(this,getResources().getString(R.string.msg_empty),Toast.LENGTH_LONG).show();
            eMsgTxt.requestFocus();
            return;
        }
        message = new MyMessages();
        message.setMessage(strMsg);
        messages.set(pos,message);
        adapter.notifyDataSetChanged();
        eMsgTxt.setText("");
        eMsgTxt.requestFocus();
        UncheckItems();
    }

    public void onClickDeleteMsg(View view) {
        int pos = lvMessages.getCheckedItemPosition();
        if(pos < 0 || messages.size() <= 0 ){       // Item was not selected
            Toast.makeText(this,getResources().getString(R.string.select_msg_delete_err),Toast.LENGTH_LONG).show();
            eMsgTxt.requestFocus();
            return;
        }
        Log.d(TAG,"Selected message is: " + messages.get(pos));
        itemPos = pos;
        initAlertDialog();
        alertDlg.show();

    }

    private void UncheckItems() {
        for (int i = 0; i <lvMessages.getCount(); i++) {
            lvMessages.setItemChecked(i,false);
        }
    }

    public void onClickSaveMsg(View view) {

        if(addToDataBase(messages) == true){
            setResult(RESULT_OK);
            finish();
        }
        else{
            return;
        }
    }

    private boolean addToDataBase(final ArrayList<MyMessages> messages) {
        final DBMessages dbMessages = new DBMessages(this);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return dbMessages.addToDataBase(messages);
            }
            @Override
            protected void onPostExecute(Integer result) {
                if(result != 3) {       // Where were no data to Save
                    if (result == 1) {
                        Toast.makeText(getApplicationContext(), getResources().getText(R.string.add_msg_db_successfully), LENGTH_LONG).show();
                        flag = true;
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getText(R.string.add_db_err), LENGTH_LONG).show();
                        flag = false;
                    }
                }

            }
        }.execute();

        return flag;
    }
}
