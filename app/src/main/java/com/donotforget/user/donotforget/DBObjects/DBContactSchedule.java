package com.donotforget.user.donotforget.DBObjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.donotforget.user.donotforget.objects.ContactSchedule;

import java.util.ArrayList;

/**
 * Created by user on 19.07.2016.
 */
public class DBContactSchedule extends AbsrtactDBHelper {
    public static final String TABLE_NAME = "contact_schedule";
    public static final String COL_CONTACT_NAME = "contactName";
    public static final String COL_PHONE = "phone";
    public static final String COL_SCHEDULE_ID = "schedule_id";
    public static final String COL_SCHEDULE_OWNER = "schedule_owner";
    public static final String COL_IS_SENT = "isSent";
    private static final String TAG = "Alex_" + DBContactSchedule.class.getSimpleName();

    private SQLiteDatabase db;
    private ContactSchedule contactSchedule;
    private ArrayList<ContactSchedule> contactSchedulesList;
    private ContentValues cv;
    private ArrayList<Long> rowIdList = new ArrayList<>();

    public static String getSqlCommand() {
        String strSQL = "CREATE TABLE " + TABLE_NAME + " (" + getCOL_ID_Sql()
                + COL_CONTACT_NAME + " TEXT, "
                + COL_SCHEDULE_ID + " INTEGER, "
                + COL_SCHEDULE_OWNER + " TEXT, "
                + COL_PHONE + " TEXT, "
                + COL_IS_SENT + " INTEGER);";
        //Log.d(TAG,"Create contact_schedule table SQL: " + strSQL);
        return strSQL;
    }

    public DBContactSchedule(Context context) {
        super(context);
        contactSchedule = new ContactSchedule();
        try {
            db = getWritableDatabase();
        }
        catch (SQLiteException ex){
            db = getReadableDatabase();
        }
    }

    public ArrayList<Long> addToDataBase(ArrayList<ContactSchedule> objects) {
        contactSchedulesList = (ArrayList<ContactSchedule>)objects;
        long rowID = -1;
        try {
            db.beginTransaction();
            for (int i = 0; i < contactSchedulesList.size(); i++) {
                contactSchedule = new ContactSchedule();
                contactSchedule = contactSchedulesList.get(i);
                cv = new ContentValues();

                cv.put(COL_CONTACT_NAME,contactSchedule.getContactName());
                cv.put(COL_PHONE,contactSchedule.getPhone());
                cv.put(COL_IS_SENT,contactSchedule.getIsSent());
                cv.put(COL_SCHEDULE_ID,contactSchedule.getSchedule_id());
                cv.put(COL_SCHEDULE_OWNER,contactSchedule.getSchedule_owner());

                rowID = db.insert(TABLE_NAME, null, cv);
                rowIdList.add(rowID);
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
            if(db.isOpen())
                db.close();
            rowIdList = null;

        } finally {
            db.endTransaction();
        }
        if(db.isOpen())
            db.close();

        return rowIdList;

    }

    public int updateContactSchedules(ArrayList<ContactSchedule> objects) {
        contactSchedulesList = (ArrayList<ContactSchedule>)objects;
        int rowCount = 0;
        try {
            db.beginTransaction();
            for (int i = 0; i < contactSchedulesList.size(); i++) {
                contactSchedule = new ContactSchedule();
                contactSchedule = contactSchedulesList.get(i);
                cv = new ContentValues();

                cv.put(COL_IS_SENT,1);

                rowCount += db.update(TABLE_NAME, cv, COL_ID + " = ?", new String[] {String.valueOf(contactSchedule.getId())});

            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
            if(db.isOpen())
                db.close();
            return rowCount;
        } finally {
            db.endTransaction();
        }
        if(db.isOpen())
            db.close();

        return rowCount;

    }

    public ArrayList<ContactSchedule> ReadFromDataBase() {
        contactSchedulesList = new ArrayList<>();

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
                    contactSchedule = new ContactSchedule();
                    contactSchedule.setId(cursor.getLong(cursor.getColumnIndex(COL_ID)));
                    contactSchedule.setContactName(cursor.getString(cursor.getColumnIndex(COL_CONTACT_NAME)));
                    contactSchedule.setPhone(cursor.getString(cursor.getColumnIndex(COL_PHONE)));
                    contactSchedule.setIsSent(cursor.getInt(cursor.getColumnIndex(COL_IS_SENT)));
                    contactSchedule.setSchedule_id(cursor.getLong(cursor.getColumnIndex(COL_SCHEDULE_ID)));
                    contactSchedule.setSchedule_owner(cursor.getInt(cursor.getColumnIndex(COL_SCHEDULE_OWNER)));

                    contactSchedulesList.add(contactSchedule);

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

        return contactSchedulesList;
    }

    public int GetNumOfSchedules(int schedule_id){
        int count = 0;
        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_SCHEDULE_ID + " = ?; ";

        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(schedule_id)});

        count = cursor.getCount();

        if(!cursor.isClosed())
            cursor.close();
        if(db.isOpen())
            db.close();

        return count;
    }

    public ArrayList<ContactSchedule> ReadContactsWeb() {
        contactSchedulesList = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_IS_SENT
                + " = ? AND "
                + COL_SCHEDULE_OWNER + " > ? "
                + "order by " + COL_CONTACT_NAME + ";";

        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(0), String.valueOf(0)});
        //Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
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
                    contactSchedule = new ContactSchedule();
                    contactSchedule.setId(cursor.getLong(cursor.getColumnIndex(COL_ID)));
                    contactSchedule.setContactName(cursor.getString(cursor.getColumnIndex(COL_CONTACT_NAME)));
                    contactSchedule.setPhone(cursor.getString(cursor.getColumnIndex(COL_PHONE)));
                    contactSchedule.setIsSent(cursor.getInt(cursor.getColumnIndex(COL_IS_SENT)));
                    contactSchedule.setSchedule_id(cursor.getLong(cursor.getColumnIndex(COL_SCHEDULE_ID)));
                    contactSchedule.setSchedule_owner(cursor.getInt(cursor.getColumnIndex(COL_SCHEDULE_OWNER)));

                    contactSchedulesList.add(contactSchedule);

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

        return contactSchedulesList;
    }

    public ArrayList<ContactSchedule> ReadContacts() {
        contactSchedulesList = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_CONTACT_NAME
                + " <> \"\" AND "
                + COL_SCHEDULE_OWNER + " > ? "
                + "group by " + COL_CONTACT_NAME + ";";

        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(0)});
        //Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
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
                    contactSchedule = new ContactSchedule();
                    contactSchedule.setId(cursor.getLong(cursor.getColumnIndex(COL_ID)));
                    contactSchedule.setContactName(cursor.getString(cursor.getColumnIndex(COL_CONTACT_NAME)));
                    contactSchedule.setPhone(cursor.getString(cursor.getColumnIndex(COL_PHONE)));
                    contactSchedule.setIsSent(cursor.getInt(cursor.getColumnIndex(COL_IS_SENT)));
                    contactSchedule.setSchedule_id(cursor.getLong(cursor.getColumnIndex(COL_SCHEDULE_ID)));
                    contactSchedule.setSchedule_owner(cursor.getInt(cursor.getColumnIndex(COL_SCHEDULE_OWNER)));

                    contactSchedulesList.add(contactSchedule);

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

        return contactSchedulesList;
    }

    @Override
    public boolean updateDataBase(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        super.updateDataBase(cv);


        if(db.isOpen())
            db.close();
        return false;
    }

    public boolean deleteSelectedSchedules(ArrayList<Integer> delSchedulesID, int owner, String contactName) {
        int result = 0;
        if (delSchedulesID == null || delSchedulesID.size() == 0)
            return false;

        try {
            if(owner > 0 && !contactName.isEmpty()){
                for (int i = 0; i <delSchedulesID.size(); i++) {
                    result += db.delete(TABLE_NAME, COL_SCHEDULE_ID + " = ? AND " + COL_SCHEDULE_OWNER + " = ? AND " + COL_CONTACT_NAME
                             + " = ?" + ";",
                            new String[]{String.valueOf(delSchedulesID.get(i)), String.valueOf(owner), contactName});
                }
            }
            else {
                for (int i = 0; i < delSchedulesID.size(); i++) {
                    result += db.delete(TABLE_NAME, COL_SCHEDULE_ID + " = ? AND " + COL_SCHEDULE_OWNER + " = ?" + ";",
                            new String[]{String.valueOf(delSchedulesID.get(i)), String.valueOf(owner)});
                }
            }
        }
        catch (Exception e){
            Log.d(TAG, "Failed to delete a row, error message: " + e.getMessage());

            if(db.isOpen())
                db.close();

            return false;
        }

        if(db.isOpen())
            db.close();

        if(result > 0)
            return true;
        return false;
    }

    public boolean deleteFromDataBase(long deleteScheduleID, String contactName) {
        int result = 0;
        if (deleteScheduleID == -1)
            return false;

        try {
            result =  db.delete(TABLE_NAME, COL_SCHEDULE_ID + " = ? AND " + COL_CONTACT_NAME + " = ?;",
                    new String[]{String.valueOf(deleteScheduleID), contactName});

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

    public boolean deleteFromDataBase(int deleteScheduleID) {
        int result = 0;
        if (deleteScheduleID == -1)
            return false;

        try {
            result =  db.delete(TABLE_NAME, COL_SCHEDULE_ID + " = ? AND " + COL_SCHEDULE_OWNER + " = ?;",
                    new String[]{String.valueOf(deleteScheduleID), String.valueOf(0)});

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
        contactSchedule.reset();
    }

    public void DelOldSchedules(ArrayList<Integer> delSchedulesID) {
        if(delSchedulesID.size() > 0){
            try {
                for (int i = 0; i <delSchedulesID.size(); i++) {
                    db.delete(TABLE_NAME, COL_SCHEDULE_ID + " = ?"  + ";",
                            new String[]{String.valueOf(delSchedulesID.get(i))});
                }
            }
            catch (Exception e){
                Log.d(TAG, "Failed to delete a row, error message: " + e.getMessage());

                if(db.isOpen())
                    db.close();
            }
        }

        if(db.isOpen())
            db.close();
    }
}
