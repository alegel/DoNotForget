package com.donotforget.user.donotforget.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by user on 14.11.2016.
 */

public class FirebaseServerIDService extends FirebaseInstanceIdService {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String TAG = "Alex_" + FirebaseServerIDService.class.getSimpleName();
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,"Token has been refreshed: " + refreshedToken);
        SaveRegIDToSharedPref(refreshedToken);

        // Subscribing to TOPIC_NEW_USER
        FirebaseMessaging.getInstance().subscribeToTopic(MyUsefulFuncs.TOPIC_NEW_USER);

        if(!MyUsefulFuncs.myName.equals("") && !MyUsefulFuncs.myPhoneNumber.equals("") && MyUsefulFuncs.registered == 0) {
            MyUsefulFuncs.registered = 1;
            SendRegistrationToServer();
        }

    }

    private void SendRegistrationToServer(){
        Log.d(TAG,"In SendRegistrationToServer");

        Intent serviceIntent = new Intent(this,SendToWebService.class);
        serviceIntent.setAction(SendToWebService.ACTION_ADD_USER);
        this.startService(serviceIntent);
    }

    private void SaveRegIDToSharedPref(final String regID){
        Log.d(TAG,"In SaveRegIDToSharedPref");
        try {
            preferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Impossible reading from the UserDetails file",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            editor = preferences.edit()
                    .putString(MyUsefulFuncs.USER_REG_ID,regID);
            editor.commit();
            MyUsefulFuncs.myReg_ID = regID;
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Impossible writing to the UserDetails file",Toast.LENGTH_LONG).show();
            return;
        }
    }
}
