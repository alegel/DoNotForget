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
 * Created by user on 18.07.2016.
 */
public class DBContactGroups extends AbsrtactDBHelper {
    public static final String TABLE_NAME = "contact_groups";
    public static final String COL_CONTACT_NAME = "name";
    public static final String COL_GROUP_NAME = "group_name";
    public static final String COL_SELECTED = "selected";
    public static final String COL_PHONE = "phone";
    private static final String TAG = "Alex_" + DBContactGroups.class.getSimpleName();

    private SQLiteDatabase db;
    MyContacts contact;
    private ArrayList<MyContacts> myContactsList;
    private ContentValues cv;

    public DBContactGroups(Context context) {
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
                + COL_GROUP_NAME + " TEXT, "
                + COL_SELECTED + " INTEGER, "
                + COL_PHONE + " TEXT);";
        //Log.d(TAG,"Create Contacts table SQL: " + strSQL);
        return strSQL;
    }

    public ArrayList<MyContacts> ReadFromDataBase() {
        myContactsList = new ArrayList<>();
        ArrayList<String> phoneList;
                                                                            // ORDER BY
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_GROUP_NAME);
        if(cursor.getCount() <= 0) {
            if(!cursor.isClosed())
                cursor.close();
        }
        else{
            try {
                resetParams();
                cursor.moveToFirst();
                super.load(cursor); // Save the _id

                do {
                    contact = new MyContacts();
                    contact.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(COL_ID))));
                    contact.setName(cursor.getString(cursor.getColumnIndex(COL_CONTACT_NAME)));
                    contact.setGroup(cursor.getString(cursor.getColumnIndex(COL_GROUP_NAME)));
                    contact.setSelected(cursor.getInt(cursor.getColumnIndex(COL_SELECTED)));
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

    public int addToDataBase(ArrayList<MyContacts> objects) {
        myContactsList = objects;
        long rowID = -1;
        try {
            db.beginTransaction();
            for (int i = 0; i < myContactsList.size(); i++) {
                if(myContactsList.get(i).isSelected() == 1) {
                    contact = new MyContacts();
                    contact = myContactsList.get(i);
                    cv = new ContentValues();

                    cv.put(COL_CONTACT_NAME, contact.getName());
                    cv.put(COL_GROUP_NAME, contact.getGroup());
                    cv.put(COL_SELECTED, contact.isSelected());
                    cv.put(COL_PHONE, contact.getPhone());

                    rowID = db.insert(TABLE_NAME, null, cv);
                }
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
            if(db.isOpen())
                db.close();
            rowID = -1;
        } finally {
            db.endTransaction();
        }
        if(db.isOpen())
            db.close();

        if(rowID != -1)
            return 1;
        return 0;
    }

    @Override
    public boolean updateDataBase(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        super.updateDataBase(cv);



        return false;
    }


    public boolean deleteFromDataBase(String groupName) {
        int result = 0;
        if (groupName.isEmpty())
            return false;

        try {
            result =  db.delete(TABLE_NAME, COL_GROUP_NAME + " = ?;",
                    new String[]{groupName});

        }
        catch (Exception e){
            Log.d(TAG, "Failed to delete a row, error message: " + e.getMessage());

            if(db.isOpen())
                db.close();

            return false;
        }

        if(db.isOpen())
            db.close();

        if(result >= 1)
            return true;
        return false;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Code example for adding a new column to the table
//            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME + " INTEGER;");
        }
    }

    @Override
    protected void resetParams() {
        super.resetParams();
        contact.reset();
    }
}
