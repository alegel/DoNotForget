package com.donotforget.user.donotforget;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.services.SendToWebService;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String queryString = "@echo";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final int SEC = 10;
    private static String receivedSMS = "";
    private ProgressDialog pDialog;
    private EditText editName, editPhone;
    private TextView tvPhoneErr;
    private Button btnSignIn, btnCancel;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String TAG = "Alex_" + SignInActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editName = (EditText) findViewById(R.id.editName);
        editPhone = (EditText) findViewById(R.id.editPhone);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        tvPhoneErr = (TextView) findViewById(R.id.tvPhoneErr);

        btnSignIn.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(SmsReceiver, new IntentFilter(SMS_RECEIVED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(SmsReceiver);
    }


    BroadcastReceiver SmsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle myBundle = intent.getExtras();
            SmsMessage[] messages = null;
            String strMessage = "";

            if (intent.getAction().equals(SMS_RECEIVED)) {
                if (myBundle != null) {
                    Object[] pdus = (Object[]) myBundle.get("pdus");

                    messages = new SmsMessage[pdus.length];

                    for (int i = 0; i < messages.length; i++) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            String format = myBundle.getString("format");
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        } else {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }

                        strMessage += messages[i].getOriginatingAddress();
//                        strMessage += "SMS From: " + messages[i].getOriginatingAddress();
//                        strMessage += " : ";
//                        strMessage += messages[i].getMessageBody();
//                        strMessage += "\n";

                    }

                    receivedSMS = strMessage;
   //                 Log.d(TAG,"receivedSMS = " + receivedSMS);

                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        tvPhoneErr.setVisibility(View.INVISIBLE);
        if(view.getId() == R.id.btnCancel){
            Toast.makeText(this,getResources().getString(R.string.details_missing),Toast.LENGTH_LONG).show();
            finish();
        }
        else {
            if (editName.getText().toString().isEmpty()) {
                Toast.makeText(this, getResources().getString(R.string.txtName), Toast.LENGTH_LONG).show();
                return;
            }
            if (editPhone.getText().toString().isEmpty()) {
                Toast.makeText(this, getResources().getString(R.string.txtPhone), Toast.LENGTH_LONG).show();
                return;
            }

            verifyPhoneNumber(editPhone.getText().toString());    // SHOULD BE OPENED (closed for Emulator)
//            SaveDetails();                                          // SHOULD BE CLOSED
        }

    }

    private void verifyPhoneNumber(final String sPhone) {
        SmsManager smsManager = SmsManager.getDefault();
        String smsNumber = sPhone;
        String smsText = getResources().getString(R.string.verify_sms);
        smsManager.sendTextMessage(smsNumber, null, smsText, null, null);
/*************************************************************************************/
        final Context context = this;
        new AsyncTask<Void, Void, Void>() {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(context, "", getResources().getString(R.string.verify_phone), true);
            }

        @Override
        protected Void doInBackground(Void... voids) {
            int count = 0;
            while (receivedSMS.isEmpty()){
                if(count == SEC * 30){
                    return null;
                }
                try {
                    Thread.sleep(100);
                    count ++;
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try{
                if(pDialog.isShowing()){
                    pDialog.dismiss();
                    }
                }
            catch(Exception e){
                e.printStackTrace();
            }
            finally{
                pDialog.dismiss();
            }
            if(!receivedSMS.isEmpty() && receivedSMS.indexOf(sPhone.substring(1)) >= 0){
                SaveDetails();
            }
            else {
                tvPhoneErr.setVisibility(View.VISIBLE);
            }
        }
        }.execute();

/*************************************************************************************/
    }

    private void SaveDetails() {
        String name = editName.getText().toString();
        int ind = name.indexOf(39);
        if(ind > -1){
            char ch = name.charAt(ind);
            name = name.replaceAll(String.valueOf(ch),"");
        }
        Log.d(TAG,"Save New User details");
        try {
            preferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Impossible reading from the UserDetails file",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            editor = preferences.edit()
                    .putString(MyUsefulFuncs.USER_NAME,name)
                    .putString(MyUsefulFuncs.USER_PHONE,receivedSMS);
            editor.commit();

            MyUsefulFuncs.myName = name;
            MyUsefulFuncs.myPhoneNumber = receivedSMS;

            Toast.makeText(this,getResources().getString(R.string.registration_process),Toast.LENGTH_LONG).show();

/****************** Wait for refreshedToken for maximum 20 seconds ***************************/
            if(MyUsefulFuncs.myReg_ID.equals("")){
                Toast.makeText(this,"Privet Bufet !!!", Toast.LENGTH_LONG).show();
                for (int i = 0; i <20; i++) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!MyUsefulFuncs.myReg_ID.equals("")){
                        break;
                    }
                }
            }
/*********************************************************************************************/
            if(!MyUsefulFuncs.myReg_ID.equals("") && MyUsefulFuncs.registered == 0) {
//                MyUsefulFuncs.registered = 1;
                Intent serviceIntent = new Intent(this, SendToWebService.class);
                serviceIntent.setAction(SendToWebService.ACTION_ADD_USER);
                this.startService(serviceIntent);
            }
            else if(MyUsefulFuncs.myReg_ID.equals("")){
                Toast.makeText(this,getString(R.string.database_err),Toast.LENGTH_LONG).show();
                DeleteDetailsFromSharedPreferences();
            }
            finish();
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Impossible writing to the UserDetails file",Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void DeleteDetailsFromSharedPreferences() {
        try {
            preferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Impossible reading from the UserDetails file",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            editor = preferences.edit()
                    .putString(MyUsefulFuncs.USER_NAME, "error")
                    .putString(MyUsefulFuncs.USER_PHONE, "error");
            editor.commit();

            MyUsefulFuncs.myName = "";
            MyUsefulFuncs.myPhoneNumber = "";
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Impossible writing to the UserDetails file",Toast.LENGTH_LONG).show();
            return;
        }
    }
}
