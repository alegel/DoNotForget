package com.donotforget.user.donotforget;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBMessages;
import com.donotforget.user.donotforget.objects.MyMessages;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;

import java.util.ArrayList;

public class AddPreparedMsg extends AppCompatActivity implements View.OnClickListener {
    private ListView lvPrepMsg;
    private Button btnSelect, btnPrepMsg;
    private ArrayList<MyMessages> messages;// = new ArrayList<>();
    private ArrayAdapter<MyMessages> adapter;
    private MyMessages message;
    private DBMessages dbMessages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_prepared_msg);

        btnPrepMsg = (Button) findViewById(R.id.btnPrepMsg);
        btnPrepMsg.setOnClickListener(this);
        btnSelect = (Button) findViewById(R.id.btnSelect);
        btnSelect.setOnClickListener(this);
        lvPrepMsg = (ListView) findViewById(R.id.lvPreparedMsg);

        fillMessages();
    }

    private void fillMessages() {
        final Context context = this;
        dbMessages = new DBMessages(this);
        new AsyncTask<Void, Void, ArrayList<MyMessages>>() {
            @Override
            protected ArrayList<MyMessages> doInBackground(Void... params) {
                messages = dbMessages.ReadFromDataBase();
                return messages;
            }
            @Override
            protected void onPostExecute(ArrayList<MyMessages> messages) {
                if(messages.size() > 0){
                    adapter = new ArrayAdapter<MyMessages>(context, android.R.layout.simple_list_item_single_choice, messages);
                    lvPrepMsg.setAdapter(adapter);
                }
                else{
                    btnSelect.setVisibility(View.GONE);
                    Toast.makeText(context,getResources().getString(R.string.msg_list_empty_err),Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if(view.getId() == R.id.btnPrepMsg){
            intent = new Intent(this,MessagesActivity.class);
            startActivityForResult(intent, MyUsefulFuncs.REQ_CODE_MESSAGES);
        }
        else if(view.getId() == R.id.btnSelect) {
            int pos = lvPrepMsg.getCheckedItemPosition();
            if(pos < 0 || messages.size() <= 0 ){       // Item was not selected
                Toast.makeText(this,getResources().getString(R.string.select_msg_err),Toast.LENGTH_LONG).show();
                return;
            }
            intent = new Intent();
            intent.putExtra(MyUsefulFuncs.MESSAGE_POSITION,messages.get(pos));
            setResult(RESULT_OK,intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == MyUsefulFuncs.REQ_CODE_MESSAGES){

            fillMessages();
        }
    }
}
