package com.donotforget.user.donotforget.services;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.donotforget.user.donotforget.DBObjects.DBContacts;
import com.donotforget.user.donotforget.objects.MyContacts;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;

/**
 * Created by user on 27.08.2016.
 */
public class DeleteContactReceiver extends BroadcastReceiver {
    public final static String BROADCAST_DELETE_CONTACT = "com.donotforget.user.donotforget.services.DELETE_CONTACT";
    private Context context;
    private MyContacts contact;
    private String strNewPhone;
    private static final String TAG = "Alex_" + DeleteContactReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"DeleteContactReceiver.onReceive()");
        this.context = context;
        if(intent.hasExtra(MyUsefulFuncs.USER_PHONE)){
            strNewPhone = intent.getStringExtra(MyUsefulFuncs.USER_PHONE);
        }
        if(!strNewPhone.isEmpty()){
            findContact();
        }
    }

    private void findContact() {
        final DBContacts dbContacts = new DBContacts(context);

        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(Void... voids) {
                if(getContactsList() == true){
                    // ADD New contact to Contacts Table
                    return dbContacts.deleteContact(contact.getPhone());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if(result > 0){
                    Log.d(TAG,"The new contact was deleted successfully: " + contact.getName() + ", " + contact.getPhone());
                }
                else{
                    Log.d(TAG,"Failed to delete a contact: " + contact.getName() + ", " + contact.getPhone());
                }

            }

        }.execute();
    }

    private boolean getContactsList(){
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

            if(hasPhone.equalsIgnoreCase("1")){
                Cursor phoneCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + mContactId,
                        null,null);

                while (phoneCursor.moveToNext()){
                    mPhoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    //Log.d(TAG,"mPhoneNumber = " + mPhoneNumber);

                    phone = mPhoneNumber;
                    phone = phone.replace("(","");
                    phone = phone.replace("-","");
                    phone = phone.replace(" ","");
                    phone = phone.replace(")","");
                }
                if(!phone.isEmpty()) {
                    if(strNewPhone.equals(phone)){
                        contact = new MyContacts();
                        contact.setName(mContactName);
                        contact.setPhone(phone);

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
