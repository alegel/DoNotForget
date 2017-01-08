package com.donotforget.user.donotforget.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.donotforget.user.donotforget.DBObjects.DBContacts;
import com.donotforget.user.donotforget.objects.JSONParser;
import com.donotforget.user.donotforget.objects.MyContacts;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ReadContactsService extends IntentService {
    private static String url_get_all_users = "http://donotforget.info/getFromDB/getAllUsers.php";

    private static final String TAG = "Alex_" + ReadContactsService.class.getSimpleName();

    private JSONParser jsonParser = new JSONParser();
    private String errMessage = "";
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    private JSONObject jsonObject = new JSONObject();

    private MyContacts contact;
    private ArrayList<MyContacts> contactsList = new ArrayList<>();
    private ArrayList<String> usersFromWebList = new ArrayList<>();
    private static Context myContext;
    private String strPhone;
    private DBContacts dbContacts;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_READ_CONTACTS = "com.donotforget.user.donotforget.services.action.READ_CONTACTS";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.donotforget.user.donotforget.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.donotforget.user.donotforget.services.extra.PARAM2";

    public ReadContactsService() {
        super("ReadContactsService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionReadContacts(Context context, String param1, String param2) {
        myContext = context;

        Intent intent = new Intent(context, ReadContactsService.class);
        intent.setAction(ACTION_READ_CONTACTS);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"In onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_READ_CONTACTS.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionReadContacts(param1, param2);
            }
        }
    }

    private void readUsersFromWeb(){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data", "data"));

        if(sendJson(url_get_all_users, params) == true){
            Log.d(TAG, "all users received successfully");
            JSONArray jsonArr = null;
            try {
                jsonArr = jsonObject.getJSONArray("users");
                int numOfRaws = jsonArr.length();
                if (numOfRaws > 0)  {
                    for (int i = 0; i < jsonArr.length(); i++) {
                        JSONObject jsonObj = jsonArr.getJSONObject(i);
                        usersFromWebList.add(jsonObj.getString("phone"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            Log.d(TAG, "Failed to receive all users: " + errMessage);
        }
    }

    private boolean sendJson(String url, List<NameValuePair> params){
        JSONObject json = jsonParser.makeHttpRequest(url,"POST", params);
        if(json != null) {
            Log.d(TAG, json.toString());
            jsonObject = json;
            try {
                int success = json.getInt("success");
                if (success == 1) {
                    return true;

                } else {
                    errMessage = json.getString("message");
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG,"Failed to send json with exception: " + e.toString());
            }
        }
        else{
            Log.d(TAG,"Failed to connect to getAllUsers.php");
            errMessage = "Failed to connect";
            return false;
        }
        return false;
    }

    public  void fillUpdatedContactList() {
        Log.d(TAG,"fillUpdatedContactList()");
/*** Will be used in case wherein while app is running the new user has been joined to the service ***/
        final DBContacts dbContacts = new DBContacts(myContext);
        new AsyncTask<Void,Void,ArrayList<MyContacts>>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected ArrayList<MyContacts> doInBackground(Void... voids) {
//                Log.d(TAG,"Before  ReadContacts" + contactsList);
                contactsList = dbContacts.ReadContacts();
//                Log.d(TAG,"After  ReadContacts" + contactsList);
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

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionReadContacts(String param1, String param2) {
        myContext = getApplicationContext();
        readUsersFromWeb();
        if(usersFromWebList == null || usersFromWebList.size() <= 0) {
            return;
        }

        getContactsList();
  //      ArrayList<MyContacts> newContactsList = changeContactNameIfExists();
        if(contactsList == null || contactsList.size() <= 0)
            return;

        dbContacts = new DBContacts(myContext);
        dbContacts.addContacts(contactsList); // ADD Contacts to Contacts Table

        fillUpdatedContactList();
    }

    private void getContactsList(){
        Log.d(TAG,"In getContactsList");
        String phone = "";
        String mContactId, mContactName, mPhoneNumber;
        String hasPhone;

        ContentResolver contentResolver = myContext.getContentResolver();
        Uri contactsTable = ContactsContract.Contacts.CONTENT_URI;
        Cursor cursor = contentResolver.query(contactsTable,null,null,null,null);
        while (cursor.moveToNext()){
            mContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            mContactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
            hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if(hasPhone.equalsIgnoreCase("1")){
                Cursor phoneCursor = myContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + mContactId,
                        null,null);

                while (phoneCursor.moveToNext()){
                    mPhoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phone = mPhoneNumber;

                    phone = phone.replace("(","");
                    phone = phone.replace("-","");
                    phone = phone.replace(" ","");
                    phone = phone.replace(")","");

                 //   Log.d(TAG,"phone = "+ phone);
                }
                if(!phone.isEmpty()) {
                    String newPhone = FindContactName(phone);
                    if(!newPhone.equals("")) {
                        contact = new MyContacts();
                        contact.setName(mContactName);
                        contact.setPhone(newPhone);
                        contactsList.add(contact);
                    }
                }

                phoneCursor.close();
            }
        }
        cursor.close();
    }

    private String FindContactName(String phone) {
        String newPhone = "";
        for (int i = 0; i < usersFromWebList.size(); i++) {
//            if(usersFromWebList.get(i).equals(phone)) {
            if(phone.length() > 6 && usersFromWebList.get(i).indexOf(phone.substring(1)) >= 0) {
                newPhone = usersFromWebList.get(i);
                usersFromWebList.remove(i);
                return newPhone;
            }
        }
        return "";
    }
}
