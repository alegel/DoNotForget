package com.donotforget.user.donotforget.DBObjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.donotforget.user.donotforget.objects.MyMessages;

import java.util.ArrayList;

/**
 * Created by user on 17.07.2016.
 */
public class DBMessages extends AbsrtactDBHelper {
    public static final String TABLE_NAME = "messages";
    public static final String COL_MSG_TEXT = "msgText";
    private static final String TAG = "Alex_" + DBMessages.class.getSimpleName();

    private SQLiteDatabase db;
    MyMessages message;
    private ArrayList<MyMessages> messages;
    private ContentValues cv;

    public DBMessages(Context context) {
        super(context);
        message = new MyMessages();
        try {
            db = getWritableDatabase();
        }
        catch (SQLiteException ex){
            db = getReadableDatabase();
        }
    }

    public ArrayList<MyMessages> ReadFromDataBase() {
        messages = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
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
                    message = new MyMessages();
                    message.setId(cursor.getLong(cursor.getColumnIndex(COL_ID)));
                    message.setMessage(cursor.getString(cursor.getColumnIndex(COL_MSG_TEXT)));

                    messages.add(message);

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

        return messages;
    }

    @Override
    protected void resetParams() {
        super.resetParams();
        message.reset();
    }

    public int addToDataBase(ArrayList<MyMessages> objects) {
        deleteFromDataBase(db); // Delete All rows from DataBase

        messages = objects;
        if(messages.size() <= 0)
            return 3;
        long rowID = -1;
        try {
            db.beginTransaction();
            for (int i = 0; i < messages.size(); i++) {
                message = new MyMessages();
                message = messages.get(i);
                cv = new ContentValues();

                cv.put(COL_MSG_TEXT, message.getMessage());
                rowID = db.insert(TABLE_NAME, null, cv);
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


    boolean deleteFromDataBase(SQLiteDatabase db) {
        return db.delete(TABLE_NAME, null, null) > 0 ? true : false;
    }

    public static String getSqlCommand() {
        String strSQL = "CREATE TABLE " + TABLE_NAME + " (" + getCOL_ID_Sql()
                + COL_MSG_TEXT + " TEXT);";
        //Log.d(TAG, "Create messages table SQL: " + strSQL);
        return strSQL;
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Code example for adding a new column to the table
//            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME + " INTEGER;");
        }
    }
}
