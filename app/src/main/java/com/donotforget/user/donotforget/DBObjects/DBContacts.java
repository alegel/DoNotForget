package com.donotforget.user.donotforget.DBObjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.donotforget.user.donotforget.objects.MyContacts;

import java.util.ArrayList;

/**
 * Created by user on 27.08.2016.
 */
public class DBContacts extends AbsrtactDBHelper {
    public static final String TABLE_NAME = "contacts";
    public static final String COL_CONTACT_NAME = "name";
    public static final String COL_PHONE = "phone";
    private static final String TAG = "Alex_" + DBContacts.class.getSimpleName();

    private SQLiteDatabase db;
    MyContacts contact;
    private ArrayList<MyContacts> myContactsList;
    private ContentValues cv;

    public DBContacts(Context context) {
        super(context);
        contact = new MyContacts();
        try {
            db = getWritableDatabase();
        }
        catch (SQLiteException ex){
            db = getReadableDatabase();
        }
    }

    public static String getSqlCommand() {
        String strSQL = "CREATE TABLE " + TABLE_NAME + " (" + getCOL_ID_Sql()
                + COL_CONTACT_NAME + " TEXT, "
                + COL_PHONE + " TEXT);";
        return strSQL;
    }


    public boolean FindContactName(String contactName) {
        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_CONTACT_NAME + " = ?;";
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {contactName});
        if(cursor.getCount() > 0) {
            if(!cursor.isClosed())
                cursor.close();
            if(db.isOpen())
                db.close();
            return true;
        }
        if(!cursor.isClosed())
            cursor.close();
        if(db.isOpen())
            db.close();

        return false;
    }

    public String getPhone(String contactName) {
        String phone = "";
        String sqlQuery = "SELECT phone FROM " + TABLE_NAME + " WHERE " + COL_CONTACT_NAME + " = ?;";
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {contactName});
        if(cursor.getCount() > 0) {
            try {
                cursor.moveToFirst();
                phone = cursor.getString(cursor.getColumnIndex(COL_PHONE));
            }catch (Exception e){
                Log.d(TAG, e.getMessage());
            }
            if(!cursor.isClosed())
                cursor.close();
            if(db.isOpen())
                db.close();
            return phone;
        }
       return phone;
    }

    public String getContactName(String phone) {
        if(phone == null || phone.isEmpty()){
            if(db.isOpen())
                db.close();
            return "";
        }
        String name = "";
        String sqlQuery = "SELECT name FROM " + TABLE_NAME + " WHERE " + COL_PHONE + " = ?;";
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {phone});
        if(cursor.getCount() > 0) {
            try {
                cursor.moveToFirst();
                name = cursor.getString(cursor.getColumnIndex(COL_CONTACT_NAME));
            }catch (Exception e){
                Log.d(TAG, e.getMessage());
            }
            if(!cursor.isClosed())
                cursor.close();
            if(db.isOpen())
                db.close();
            return name;
        }
        return name;
    }

    public ArrayList<MyContacts> ReadContacts() {
        myContactsList = new ArrayList<>();
        ArrayList<String> phoneList;
        // ORDER BY
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if(cursor.getCount() <= 0) {
            if(!cursor.isClosed())
                cursor.close();
        }
        else{
            try {
                resetParams();
                cursor.moveToFirst();
                super.load(cursor);

                do {
                    contact = new MyContacts();
                    contact.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(COL_ID))));
                    contact.setName(cursor.getString(cursor.getColumnIndex(COL_CONTACT_NAME)));
                    contact.setPhone(cursor.getString(cursor.getColumnIndex(COL_PHONE)));
                    myContactsList.add(contact);

                }while (cursor.moveToNext());
            }catch (Exception e){
                Log.d(TAG, e.getMessage());
                if(!cursor.isClosed())
                    cursor.close();
            }
            finally {
                if(!cursor.isClosed())
                    cursor.close();
            }
        }

        if(!cursor.isClosed())
            cursor.close();
        if(db.isOpen())
            db.close();

        return myContactsList;
    }

    public void addContacts(ArrayList<MyContacts> objects){
        ArrayList<MyContacts> contactsList = (ArrayList<MyContacts>) objects;
        long rowID = 0;
        try {
            db.beginTransaction();
            for (int i = 0; i < contactsList.size(); i++) {
                contact = new MyContacts();
                contact = contactsList.get(i);
                cv = new ContentValues();
                cv.put(COL_CONTACT_NAME,contact.getName());
                cv.put(COL_PHONE,contact.getPhone());

                rowID = db.insert(TABLE_NAME, null, cv);
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
            if(db.isOpen())
                db.close();

        } finally {
            db.endTransaction();
        }
        if(db.isOpen())
            db.close();
    }

    public int addContact(Object obj) {
        contact = (MyContacts) obj;
        cv = new ContentValues();
        long rowID = 0;

        cv.put(COL_CONTACT_NAME,contact.getName());
        cv.put(COL_PHONE,contact.getPhone());

        try {
            rowID = db.insert(TABLE_NAME, null, cv);
        }catch (Exception e){
            Log.d(TAG,"Exception: "+ e.getMessage());
        }

        //Log.d(TAG,"row inserted, rowID = " + rowID);
        if(db.isOpen())
            db.close();
        if(rowID > 0)
            return 1;
        return 0;
    }

    public int deleteContact(String phone) {
        int result = 0;
        if (phone.isEmpty())
            return 0;

        try {
            result =  db.delete(TABLE_NAME, COL_PHONE + " = ?;",
                    new String[]{phone});

        }
        catch (Exception e){
            Log.d(TAG, "Failed to delete a row, error message: " + e.getMessage());

            if(db.isOpen())
                db.close();

            return 0;
        }

        if(db.isOpen())
            db.close();

        if(result >= 1)
            return 1;
        return 0;
    }

    public int deleteAllContacts() {
        int result = 0;
        try {
            result =  db.delete(TABLE_NAME, COL_PHONE + " <> ?;", new String[]{""});
        }
        catch (Exception e){
            Log.d(TAG, "Failed to delete a row, error message: " + e.getMessage());

            if(db.isOpen())
                db.close();

            return 0;
        }

        if(db.isOpen())
            db.close();

        if(result >= 1)
            return 1;
        return 0;
    }


    @Override
    public boolean updateDataBase(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        super.updateDataBase(cv);



        return false;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
