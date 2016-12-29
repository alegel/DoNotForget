package com.donotforget.user.donotforget.services;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.donotforget.user.donotforget.DBObjects.DBContacts;
import com.donotforget.user.donotforget.objects.MyContacts;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by user on 27.08.2016.
 */
public class AddContactReceiver extends BroadcastReceiver {
    private static final String TAG = "Alex_" + AddContactReceiver.class.getSimpleName();
    public final static String BROADCAST_ADD_CONTACT = "com.donotforget.user.donotforget.services.ADD_CONTACT";
    public final static String BROADCAST_REG_COMPLETE = "com.donotforget.user.donotforget.services.REGISTRATION_COMPLETE";
    public final static String BROADCAST_PUSH_NOTIFICATION = "com.donotforget.user.donotforget.services.PUSH_NOTIFICATION";
    private ArrayList<MyContacts> contactsList = new ArrayList<>();
    private Context context;
    private  MyContacts contact;
    private String strNewPhone;
    private DBContacts dbContacts;
    private SharedPreferences preferences;
    private String myPhoneNumber, myName;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"AddContactReceiver.onReceive()");
        try {
            preferences = context.getSharedPreferences("userDetails", MODE_PRIVATE);
        }
        catch (Exception e) {
            Log.d(TAG,"Impossible reading from the UserDetails file");
            return;
        }
        myName = preferences.getString(MyUsefulFuncs.USER_NAME,"error");
        myPhoneNumber = preferences.getString(MyUsefulFuncs.USER_PHONE,"error");
        this.context = context;

         if(intent.getAction().equals(BROADCAST_ADD_CONTACT)) {
            if (intent.hasExtra(MyUsefulFuncs.USER_PHONE)) {
                strNewPhone = intent.getStringExtra(MyUsefulFuncs.USER_PHONE);
            }
            if (!strNewPhone.isEmpty()) {
                findContact();
            }
        }
    }

    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public  void fillUpdatedContactList() {
        Log.d(TAG,"fillUpdatedContactList()");
/*** Will be used in case wherein while app is running the new user has been joined to the service ***/
        final DBContacts dbContacts = new DBContacts(context);
        new AsyncTask<Void,Void,ArrayList<MyContacts>>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected ArrayList<MyContacts> doInBackground(Void... voids) {
                Log.d(TAG,"Before  ReadContacts" + contactsList);
                contactsList = dbContacts.ReadContacts();
                Log.d(TAG,"After  ReadContacts" + contactsList);
                return contactsList;
            }

            @Override
            protected void onPostExecute(ArrayList<MyContacts> myContactsList) {
                if(myContactsList.size() > 0){
                    Log.d(TAG,"In onPostExecute");
                    if(MyUsefulFuncs.contactsList == null)
                    {
                        Log.d(TAG,"MyUsefulFuncs.contactsList = NULL");
                    }
                    else{
                        Log.d(TAG,"MyUsefulFuncs.contactsList Is NOT NULL");
                    }
                    MyUsefulFuncs.contactsList = contactsList;
                    Log.d(TAG,"END OF onPostExecute");
                }
            }
        }.execute();
        Log.d(TAG, "After reading the contacts from DB, In fillUpdatedContactList()");
    }

    private void changeContactNameIfExists(String name) {
        Log.d(TAG,"changeContactNameIfExists()");
        String strCount = "";
        int count = 0;
        dbContacts = new DBContacts(context);
        String newContactName = name;
        while(dbContacts.FindContactName(newContactName)){
            count ++;
            strCount = String.valueOf(count);
            newContactName = name + strCount;
            dbContacts = new DBContacts(context);
        }
        if(count > 0){
            contact.setName(newContactName);
        }
    }

    private void findContact() {
        Log.d(TAG,"findContact()");
        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(Void... voids) {
                dbContacts = new DBContacts(context);   // Check if this phone already exists in DataBase
                if(!dbContacts.getContactName(strNewPhone).equals("")) {    // This user already exists in DataBase
                    return -1;
                }
                if(myPhoneNumber.equals(strNewPhone) && !myName.equals("error")){
                    Log.d(TAG,"My name added to contacts table");
                    contact = new MyContacts();
                    contact.setName(myName);
                    contact.setPhone(myPhoneNumber);

                    dbContacts = new DBContacts(context);   // ADD my details to Contacts Table
                    if(dbContacts.addContact(contact) == 1) {
                        fillUpdatedContactList();
                        return 1;
                    }
                }
                else if(getContactsList() == true){
                    changeContactNameIfExists(contact.getName());

                    dbContacts = new DBContacts(context);   // ADD New contact to Contacts Table
                    if(dbContacts.addContact(contact) == 1) {
                        fillUpdatedContactList();
                        return 1;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if(result < 0){
                    Log.d(TAG,"This phone already exists: " + strNewPhone);
                }
                else if(result != null && result > 0){
                    Log.d(TAG,"The new contact was added successfully: " + contact.getName() + ", " + contact.getPhone());

//                    showToast("The new contact was added successfully");
                }
                else{
                    Log.d(TAG,"Failed to add a new contact: " + strNewPhone);

//                    showToast("Failed to add a new contact: " + strNewPhone);
                }

            }

        }.execute();
    }

    private boolean getContactsList(){
        Log.d(TAG,"getContactsList()");
        String phone = "";
        String mContactId, mContactName, mPhoneNumber;
        String hasPhone;

        ContentResolver contentResolver = context.getContentResolver();
        Uri contactsTable = ContactsContract.Contacts.CONTENT_URI;
        Cursor cursor = contentResolver.query(contactsTable,null,null,null,null);
        while (cursor.moveToNext()){
            mContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            mContactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
            hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
/*
            Log.d(TAG, "name: " + mContactName);
            Log.d(TAG, "hasPhone:" + hasPhone);
            Log.d(TAG, "contactId:" + mContactId);
*/
            if(hasPhone.equalsIgnoreCase("1")){
                Cursor phoneCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + mContactId,
                        null,null);

                while (phoneCursor.moveToNext()){
                    mPhoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    //Log.d(TAG,"mPhoneNumber = " + mPhoneNumber);
                    /*
                    int type = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    if(type == 0)
                        mPhoneType = "HOME";
                    else if(type == 1)
                        mPhoneType = "MOBILE";
                    else
                        mPhoneType = "OTHER";
                    */
                    // Log.d(TAG,"mPhoneType = " + mPhoneType);
                    phone = mPhoneNumber;
                    phone = phone.replace("(","");
                    phone = phone.replace("-","");
                    phone = phone.replace(" ","");
                    phone = phone.replace(")","");
                }
                if(!phone.isEmpty()) {
//                    if(strNewPhone.equals(phone)){
                    if(strNewPhone.indexOf(phone.substring(1)) >= 0){
                        contact = new MyContacts();
                        contact.setName(mContactName);
                        contact.setPhone(strNewPhone);
//                        contact.setPhone(phone);

                        phoneCursor.close();
                        cursor.close();
                        return true;
                    }
                }

                phoneCursor.close();
            }
        }
        cursor.close();
        return false;
    }
}
