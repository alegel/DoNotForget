package com.donotforget.user.donotforget.DBObjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.donotforget.user.donotforget.objects.MyUsefulFuncs;
import com.donotforget.user.donotforget.objects.Schedule;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by user on 29.06.2016.
 */
public class DBSchedule extends AbsrtactDBHelper {
    public static final String TABLE_NAME = "schedule";
    public static final String COL_RECURRING = "recurring";
    public static final String COL_ONCE_DATE = "onceDate";
    public static final String COL_FROM_DATE = "fromDate";
    public static final String COL_TO_DATE = "toDate";
    public static final String COL_ONCE_TIME = "onceTime";
    public static final String COL_AT_TIME = "atTime";
    public static final String COL_PLAY_RING = "playRing";
    public static final String COL_RING_NAME = "ringName";
    public static final String COL_VIBRATE = "vibrate";
    public static final String COL_WEEK_DAYS = "weekDays";
    public static final String COL_TEXT_ID = "textId";
    public static final String COL_MSG_TEXT = "msgText";
    public static final String COL_STATUS = "status";
    public static final String COL_SCHEDULE_FROM = "scheduleFrom";
    private static final String TAG = "Alex_" + DBSchedule.class.getSimpleName();

    private SQLiteDatabase db;
    private Schedule schedule;
    private DBContactSchedule dbContactSchedule;
    private ContentValues cv;
    private ArrayList<Schedule> schedules;
    private ArrayList<Long> rowIdList = new ArrayList<>();

    public DBSchedule(Context context) {
        super(context);
        schedule = new Schedule();
        try {
            db = getWritableDatabase();
        }
        catch (SQLiteException ex){
            db = getReadableDatabase();
        }

    }

    @Override
    public boolean updateDataBase(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        super.updateDataBase(cv);



        if(db.isOpen())
            db.close();
        return false;
    }


    public boolean deleteFromDataBase(int deleteScheduleID) {
        int result = 0;
        if (deleteScheduleID == -1)
            return false;
        try {

            result =  db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(deleteScheduleID)});
        }
        catch (Exception e){
            Log.d(TAG,"Failed to delete a row, error message: " + e.getMessage());
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

    public static String getSqlCommand() {
        String strSQL = "CREATE TABLE " + TABLE_NAME + " (" + getCOL_ID_Sql()
                + COL_RECURRING + " INTEGER, "
                + COL_ONCE_DATE + " TEXT, "
                + COL_FROM_DATE + " TEXT, "
                + COL_TO_DATE + " TEXT, "
                + COL_ONCE_TIME + " TEXT, "
                + COL_AT_TIME + " TEXT, "
                + COL_RING_NAME + " TEXT, "
                + COL_WEEK_DAYS + " TEXT, "
                + COL_PLAY_RING + " INTEGER, "
                + COL_VIBRATE + " INTEGER, "
                + COL_TEXT_ID + " INTEGER, "
                + COL_STATUS + " TEXT, "
                + COL_SCHEDULE_FROM + " TEXT, "
                + COL_MSG_TEXT + " TEXT);";
        //Log.d(TAG,"Create schedule table SQL: " + strSQL);
        return strSQL;

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
        schedule.reset();
    }

    public long addToDataBase(Object obj) {
        schedule = (Schedule) obj;
        cv = new ContentValues();
        long rowID = -1;

        cv.put(COL_RECURRING,schedule.getRecurring());
        cv.put(COL_ONCE_DATE,schedule.getOnceDate());
        cv.put(COL_FROM_DATE,schedule.getFromDate());
        cv.put(COL_TO_DATE,schedule.getToDate());
        cv.put(COL_ONCE_TIME,schedule.getOnceTime());
        cv.put(COL_AT_TIME,schedule.getAtTime());
        cv.put(COL_PLAY_RING,schedule.getPlayRingtone());
        cv.put(COL_RING_NAME,schedule.getRingtoneName());
        cv.put(COL_VIBRATE,schedule.getVibrate());
        cv.put(COL_WEEK_DAYS,schedule.getDaysOfWeek());
        cv.put(COL_TEXT_ID,schedule.getTextId());
        cv.put(COL_MSG_TEXT,schedule.getText());
        cv.put(COL_SCHEDULE_FROM,schedule.getScheduleFrom());
        cv.put(COL_STATUS,schedule.getStatus());

        try {
            rowID = db.insert(TABLE_NAME, null, cv);
        }catch (Exception e){
            Log.d(TAG,"Exception: "+ e.getMessage());
        }

        //Log.d(TAG,"row inserted, rowID = " + rowID);
        if(db.isOpen())
            db.close();
        if(rowID != -1)
            return rowID;
        return 0;
    }


    public ArrayList<Long> addToDataBase(ArrayList<Schedule> objects) {
        schedules = (ArrayList<Schedule>)objects;
        long rowID = -1;
        try {
            db.beginTransaction();
            for (int i = 0; i < schedules.size(); i++) {
                schedule = new Schedule();
                schedule = schedules.get(i);
                cv = new ContentValues();

                cv.put(COL_RECURRING, schedule.getRecurring());
                cv.put(COL_ONCE_DATE, schedule.getOnceDate());
                cv.put(COL_FROM_DATE, schedule.getFromDate());
                cv.put(COL_TO_DATE, schedule.getToDate());
                cv.put(COL_ONCE_TIME, schedule.getOnceTime());
                cv.put(COL_AT_TIME, schedule.getAtTime());
                cv.put(COL_PLAY_RING, schedule.getPlayRingtone());
                cv.put(COL_RING_NAME, schedule.getRingtoneName());
                cv.put(COL_VIBRATE, schedule.getVibrate());
                cv.put(COL_WEEK_DAYS, schedule.getDaysOfWeek());
                cv.put(COL_TEXT_ID, schedule.getTextId());
                cv.put(COL_MSG_TEXT, schedule.getText());
                cv.put(COL_SCHEDULE_FROM, schedule.getScheduleFrom());
                cv.put(COL_STATUS,schedule.getStatus());

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

    public ArrayList<Schedule> ReadContactSchedules(String contactName) {
//SELECT * FROM schedule WHERE _id IN (select schedule_id FROM contact_schedule WHERE schedule_owner > 0 AND contactName = "Milan")
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String strDayOfWeek = "";
        String strCurDate = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(day, month + 1,year));
        schedules = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID
                + " IN (select " + dbContactSchedule.COL_SCHEDULE_ID + " FROM "
                + dbContactSchedule.TABLE_NAME + " WHERE " + dbContactSchedule.COL_SCHEDULE_OWNER + " > ? "
                + "AND " + dbContactSchedule.COL_CONTACT_NAME + " = ?);"
                + "ORDER BY " + COL_FROM_DATE +
                ";";
//        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(0), contactName});
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
                    schedule = new Schedule();

                    strDayOfWeek = cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS));
                    schedule.setWeekDays(cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS)));

                    if(!strDayOfWeek.isEmpty()) {
                        if(strDayOfWeek.charAt(dayOfWeek-1) == '0') {
                            Log.d(TAG,"Day is not fit");
                            continue;
                        }
                    }

                    schedule.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setRecurring(cursor.getInt(cursor.getColumnIndex(COL_RECURRING)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setText(cursor.getString(cursor.getColumnIndex(COL_MSG_TEXT)));
                    schedule.setScheduleFrom(cursor.getString(cursor.getColumnIndex(COL_SCHEDULE_FROM)));
                    schedule.setFromDate(cursor.getString(cursor.getColumnIndex(COL_FROM_DATE)),1);
                    schedule.setToDate(cursor.getString(cursor.getColumnIndex(COL_TO_DATE)),1);
                    schedule.setOnceDate(cursor.getString(cursor.getColumnIndex(COL_ONCE_DATE)),1);
                    schedule.setOnceTime(cursor.getString(cursor.getColumnIndex(COL_ONCE_TIME)));
                    schedule.setPlayRingtone(cursor.getInt(cursor.getColumnIndex(COL_PLAY_RING)));
                    schedule.setTextId(cursor.getInt(cursor.getColumnIndex(COL_TEXT_ID)));
                    schedule.setRingtoneName(cursor.getString(cursor.getColumnIndex(COL_RING_NAME)));
                    schedule.setVibrate(cursor.getInt(cursor.getColumnIndex(COL_VIBRATE)));
                    schedule.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));

/*** Monthly and Yearly schedules should be appeared only once (from 10/08/2016 - to 15/11/2016) ***/
                    if(schedule.getRecurring() != MyUsefulFuncs.MONTHLY && schedule.getRecurring() != MyUsefulFuncs.YEARLY) {
                        schedules.add(schedule);
                    }
                    else if(MyUsefulFuncs.compareIfExists(schedules,schedule) == false){
                        schedules.add(schedule);
                    }

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

        return schedules;
    }

    public int UpdateStatus(String status, int id){
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS,status);
        int count = 0;
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String strCurDate = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(day, month + 1,year));
        // SET STATUS = EXPIRED Only in case what there are no remained future schedules
        Log.d(TAG, "In DBSchedule:UpdateStatus. The  strCurDate = " + strCurDate + ", ID = " + id);
        count = db.update(TABLE_NAME,cv,COL_ID + " = ? AND " + COL_TO_DATE + " <= ?",new String[] {String.valueOf(id), strCurDate});

        if(count <= 0) {
            calendar = Calendar.getInstance();
            /*** Add One Day to the Current Day  ***/
            Calendar cal = Calendar.getInstance();
            try {
                calendar = MyUsefulFuncs.addDate(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), Calendar.DAY_OF_MONTH, 1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String strDateFrom = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
            count = UpdateFromDate(strDateFrom, id);
        }

        if(db.isOpen())
            db.close();

        return count;
    }

    public int UpdateFromDate(String from, int id){
        ContentValues cv = new ContentValues();
        cv.put(COL_FROM_DATE,from);
        int count = 0;

        Log.d(TAG, "In DBSchedule:UpdateFromDate. The ID = " + id);
        count = db.update(TABLE_NAME,cv,COL_ID + " = ?",new String[] {String.valueOf(id)});

        return count;
    }

    public ArrayList<Integer> DelOldSchedules(Calendar cal){
        ArrayList<Integer> delSchedulesID = new ArrayList<>();

        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        String strCurDate = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(day, month + 1,year));
        Log.d(TAG,"In DelOldSchedules(), strCurDate = " + strCurDate);

        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_TO_DATE + "< ? " + ";";
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {strCurDate});

        if(cursor.getCount() <= 0) {
            if(!cursor.isClosed())
                cursor.close();
        }
        else{
            try {
                cursor.moveToFirst();
                do {
                    delSchedulesID.add(cursor.getInt(cursor.getColumnIndex(COL_ID)));
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
        if(delSchedulesID.size() > 0){
            try {
                for (int i = 0; i <delSchedulesID.size(); i++) {
                    db.delete(TABLE_NAME, COL_ID + " = ?"  + ";",
                            new String[]{String.valueOf(delSchedulesID.get(i))});
                }
            }
            catch (Exception e){
                Log.d(TAG, "Failed to delete a row, error message: " + e.getMessage());

                if(db.isOpen())
                    db.close();
            }
        }

        if(!cursor.isClosed())
            cursor.close();
        if(db.isOpen())
            db.close();

        return delSchedulesID;
    }

    public ArrayList<Integer> ReadSelectedSchedules(Schedule schedule) {
        ArrayList<Integer> delSchedulesID = new ArrayList<>();
//        Log.d(TAG,"In ReadSelectedSchedules(), schedule = " + schedule.toString());
        String sqlQuery = "SELECT " + COL_ID + " FROM " + TABLE_NAME + " WHERE " + COL_RECURRING + " = ? "
                + "AND " + COL_FROM_DATE + " = ? " + "AND " + COL_TO_DATE + " = ? " + "AND " + COL_AT_TIME + " = ? "
                + "AND " + COL_PLAY_RING + " = ? " + "AND " + COL_RING_NAME + " = ? " + "AND " + COL_VIBRATE + " = ? "
                + "AND " + COL_WEEK_DAYS + " = ? " + "AND " + COL_MSG_TEXT + " = ? " + "AND " + COL_STATUS + " = ?;";


        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(schedule.getRecurring()),MyUsefulFuncs.DateToSqlFormat(schedule.getFromDate()),
                MyUsefulFuncs.DateToSqlFormat(schedule.getToDate()),schedule.getAtTime(),String.valueOf(schedule.getPlayRingtone()),
        schedule.getRingtoneName(), String.valueOf(schedule.getVibrate()), schedule.getDaysOfWeek(), schedule.getText(), schedule.getStatus()});
/*
        Log.d(TAG,"In ReadSelectedSchedules(), COL_RECURRING = " + String.valueOf(schedule.getRecurring()));
        Log.d(TAG,"In ReadSelectedSchedules(), COL_FROM_DATE = " + MyUsefulFuncs.DateToSqlFormat(schedule.getFromDate()));
        Log.d(TAG,"In ReadSelectedSchedules(), COL_TO_DATE = " + MyUsefulFuncs.DateToSqlFormat(schedule.getToDate()));
        Log.d(TAG,"In ReadSelectedSchedules(), COL_AT_TIME = " + schedule.getAtTime());
        Log.d(TAG,"In ReadSelectedSchedules(), COL_PLAY_RING = " + String.valueOf(schedule.getPlayRingtone()));
        Log.d(TAG,"In ReadSelectedSchedules(), COL_RING_NAME = " + schedule.getRingtoneName());
        Log.d(TAG,"In ReadSelectedSchedules(), COL_VIBRATE = " + String.valueOf(schedule.getVibrate()));
        Log.d(TAG,"In ReadSelectedSchedules(), COL_WEEK_DAYS = " + schedule.getDaysOfWeek());
        Log.d(TAG,"In ReadSelectedSchedules(), COL_MSG_TEXT = " + schedule.getText());
        Log.d(TAG,"In ReadSelectedSchedules(), COL_STATUS = " + schedule.getStatus());
*/

        if(cursor.getCount() <= 0) {
            if(!cursor.isClosed())
                cursor.close();
        }
        else{
            try {
                cursor.moveToFirst();
                do {
                    delSchedulesID.add(cursor.getInt(cursor.getColumnIndex(COL_ID)));
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

        return delSchedulesID;
    }

    public ArrayList<Integer> ReadSelectedSchedulesForWeb(Schedule schedule) {
        ArrayList<Integer> delSchedulesID = new ArrayList<>();
        Log.d(TAG,"In ReadSelectedSchedules(), schedule = " + schedule.toString());
        String sqlQuery = "SELECT " + COL_ID + " FROM " + TABLE_NAME + " WHERE " + COL_RECURRING + " = ? "
                + "AND " + COL_FROM_DATE + " = ? " + "AND " + COL_TO_DATE + " = ? " + "AND " + COL_AT_TIME + " = ? "
                + "AND " + COL_PLAY_RING + " = ? " + "AND " + COL_RING_NAME + " = ? " + "AND " + COL_VIBRATE + " = ? "
                + "AND " + COL_WEEK_DAYS + " = ? " + "AND " + COL_MSG_TEXT + " = ? " + "AND " + COL_STATUS + " = ?;";


        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(schedule.getRecurring()),schedule.getFromDate(),
                schedule.getToDate(),schedule.getAtTime(),String.valueOf(schedule.getPlayRingtone()),
                schedule.getRingtoneName(), String.valueOf(schedule.getVibrate()), schedule.getDaysOfWeek(), schedule.getText(), schedule.getStatus()});

        Log.d(TAG,"In ReadSelectedSchedules(), COL_RECURRING = " + String.valueOf(schedule.getRecurring()));
        Log.d(TAG,"In ReadSelectedSchedules(), COL_FROM_DATE = " + schedule.getFromDate());
        Log.d(TAG,"In ReadSelectedSchedules(), COL_TO_DATE = " + schedule.getToDate());
        Log.d(TAG,"In ReadSelectedSchedules(), COL_AT_TIME = " + schedule.getAtTime());
        Log.d(TAG,"In ReadSelectedSchedules(), COL_PLAY_RING = " + String.valueOf(schedule.getPlayRingtone()));
        Log.d(TAG,"In ReadSelectedSchedules(), COL_RING_NAME = " + schedule.getRingtoneName());
        Log.d(TAG,"In ReadSelectedSchedules(), COL_VIBRATE = " + String.valueOf(schedule.getVibrate()));
        Log.d(TAG,"In ReadSelectedSchedules(), COL_WEEK_DAYS = " + schedule.getDaysOfWeek());
        Log.d(TAG,"In ReadSelectedSchedules(), COL_MSG_TEXT = " + schedule.getText());
        Log.d(TAG,"In ReadSelectedSchedules(), COL_STATUS = " + schedule.getStatus());


        if(cursor.getCount() <= 0) {
            if(!cursor.isClosed())
                cursor.close();
        }
        else{
            try {
                cursor.moveToFirst();
                do {
                    delSchedulesID.add(cursor.getInt(cursor.getColumnIndex(COL_ID)));
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

        return delSchedulesID;
    }

    public ArrayList<Schedule> ReadContactsScheduleWeb() {
        schedules = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String strDayOfWeek = "";
        String strCurDate = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(day, month + 1,year));
        String strCurTime = String.format("%02d:%02d:00",calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        Schedule schedule = new Schedule();
        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID
                + " IN (select " + dbContactSchedule.COL_SCHEDULE_ID + " FROM "
                + dbContactSchedule.TABLE_NAME + " WHERE " + dbContactSchedule.COL_IS_SENT
                + " = ? AND "
                + dbContactSchedule.COL_SCHEDULE_OWNER + " > ? "
                + "order by " + dbContactSchedule.COL_CONTACT_NAME + ");";

        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(0), String.valueOf(0)});
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
                    schedule = new Schedule();

                    schedule.setWeekDays(cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS)));
                    schedule.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setText(cursor.getString(cursor.getColumnIndex(COL_MSG_TEXT)));
                    schedule.setScheduleFrom(cursor.getString(cursor.getColumnIndex(COL_SCHEDULE_FROM)));
                    schedule.setFromDate(cursor.getString(cursor.getColumnIndex(COL_FROM_DATE)),1);
                    schedule.setToDate(cursor.getString(cursor.getColumnIndex(COL_TO_DATE)),1);
                    schedule.setOnceDate(cursor.getString(cursor.getColumnIndex(COL_ONCE_DATE)),1);
                    schedule.setOnceTime(cursor.getString(cursor.getColumnIndex(COL_ONCE_TIME)));
                    schedule.setPlayRingtone(cursor.getInt(cursor.getColumnIndex(COL_PLAY_RING)));
                    schedule.setTextId(cursor.getInt(cursor.getColumnIndex(COL_TEXT_ID)));
                    schedule.setRingtoneName(cursor.getString(cursor.getColumnIndex(COL_RING_NAME)));
                    schedule.setVibrate(cursor.getInt(cursor.getColumnIndex(COL_VIBRATE)));
                    schedule.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
                    schedule.setRecurring(cursor.getInt(cursor.getColumnIndex(COL_RECURRING)));

                    schedules.add(schedule);

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

        return schedules;
    }

    public ArrayList<Schedule> ReadMyNextSchedule() {
        schedules = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String strDayOfWeek = "";
        String strCurDate = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(day, month + 1,year));
        String strCurTime = String.format("%02d:%02d:00",calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        Schedule schedule = new Schedule();
        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID
                + " IN (select " + dbContactSchedule.COL_SCHEDULE_ID + " FROM "
                + dbContactSchedule.TABLE_NAME + " WHERE " + dbContactSchedule.COL_SCHEDULE_OWNER + " = ?)"
                + "AND (" + COL_FROM_DATE + " <= ? AND " + COL_TO_DATE + ">= ? AND " + COL_AT_TIME + " >= ?" + ")"
                + " AND " + COL_STATUS + " = ?"
                + " ORDER BY " + COL_AT_TIME + ";";
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(0), strCurDate, strCurDate, strCurTime, "active"});
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
                    schedule = new Schedule();
                    strDayOfWeek = cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS));

                    if(!strDayOfWeek.isEmpty()) {
                        if(strDayOfWeek.charAt(dayOfWeek-1) == '0') {
                            Log.d(TAG,"Day is not fit");
                            continue;
                        }
                    }
                    schedule.setWeekDays(cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS)));
                    schedule.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setText(cursor.getString(cursor.getColumnIndex(COL_MSG_TEXT)));
                    schedule.setScheduleFrom(cursor.getString(cursor.getColumnIndex(COL_SCHEDULE_FROM)));
                    schedule.setFromDate(cursor.getString(cursor.getColumnIndex(COL_FROM_DATE)),1);
                    schedule.setToDate(cursor.getString(cursor.getColumnIndex(COL_TO_DATE)),1);
                    schedule.setOnceDate(cursor.getString(cursor.getColumnIndex(COL_ONCE_DATE)),1);
                    schedule.setOnceTime(cursor.getString(cursor.getColumnIndex(COL_ONCE_TIME)));
                    schedule.setPlayRingtone(cursor.getInt(cursor.getColumnIndex(COL_PLAY_RING)));
                    schedule.setTextId(cursor.getInt(cursor.getColumnIndex(COL_TEXT_ID)));
                    schedule.setRingtoneName(cursor.getString(cursor.getColumnIndex(COL_RING_NAME)));
                    schedule.setVibrate(cursor.getInt(cursor.getColumnIndex(COL_VIBRATE)));
                    schedule.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
                    schedule.setRecurring(cursor.getInt(cursor.getColumnIndex(COL_RECURRING)));

                    if(schedule.getRecurring() != MyUsefulFuncs.MONTHLY && schedule.getRecurring() != MyUsefulFuncs.YEARLY) {
                        schedules.add(schedule);
                    }
                    else if (checkIfScheduleForToday(strCurDate,schedule.getOnceDate()) == true){
                        schedules.add(schedule);
                    }

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

        return schedules;
    }

    private boolean checkIfScheduleForToday(String strCurDate, String onceDate) {
        strCurDate = MyUsefulFuncs.DateFromSqlFormat(strCurDate);
        return strCurDate.equalsIgnoreCase(onceDate);
    }

    public ArrayList<Schedule> ReadAllMySchedules() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String strDayOfWeek = "";
        String strCurDate = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(day, month + 1,year));
        schedules = new ArrayList<>();

        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID
                + " IN (select " + dbContactSchedule.COL_SCHEDULE_ID + " FROM "
                + dbContactSchedule.TABLE_NAME + " WHERE " + dbContactSchedule.COL_SCHEDULE_OWNER + " = ?)"
                + "ORDER BY " + COL_FROM_DATE +
                ";";

        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(0)});

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
                    schedule = new Schedule();

                    strDayOfWeek = cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS));
                    schedule.setWeekDays(cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS)));

                    if(!strDayOfWeek.isEmpty()) {
                        if(strDayOfWeek.charAt(dayOfWeek-1) == '0') {
                            Log.d(TAG,"Day is not fit");
                            continue;
                        }
                    }

                    schedule.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setRecurring(cursor.getInt(cursor.getColumnIndex(COL_RECURRING)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setText(cursor.getString(cursor.getColumnIndex(COL_MSG_TEXT)));
                    schedule.setScheduleFrom(cursor.getString(cursor.getColumnIndex(COL_SCHEDULE_FROM)));
                    schedule.setFromDate(cursor.getString(cursor.getColumnIndex(COL_FROM_DATE)),1);
                    schedule.setToDate(cursor.getString(cursor.getColumnIndex(COL_TO_DATE)),1);
                    schedule.setOnceDate(cursor.getString(cursor.getColumnIndex(COL_ONCE_DATE)),1);
                    schedule.setOnceTime(cursor.getString(cursor.getColumnIndex(COL_ONCE_TIME)));
                    schedule.setPlayRingtone(cursor.getInt(cursor.getColumnIndex(COL_PLAY_RING)));
                    schedule.setTextId(cursor.getInt(cursor.getColumnIndex(COL_TEXT_ID)));
                    schedule.setRingtoneName(cursor.getString(cursor.getColumnIndex(COL_RING_NAME)));
                    schedule.setVibrate(cursor.getInt(cursor.getColumnIndex(COL_VIBRATE)));
                    schedule.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));

/*** Monthly and Yearly schedules should be appeared only once (from 10/08/2016 - to 15/11/2016) ***/
                    if(schedule.getRecurring() != MyUsefulFuncs.MONTHLY && schedule.getRecurring() != MyUsefulFuncs.YEARLY) {
                        schedules.add(schedule);
                    }
                    else if (MyUsefulFuncs.compareIfExists(schedules,schedule) == false){
                        schedules.add(schedule);
                    }

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

        return schedules;
    }

    public ArrayList<Schedule> ReadFromDataBaseDaily() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String strDayOfWeek = "";
        String strCurDate = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(day, month + 1,year));
        schedules = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID
                + " IN (select " + dbContactSchedule.COL_SCHEDULE_ID + " FROM "
                + dbContactSchedule.TABLE_NAME + " WHERE " + dbContactSchedule.COL_SCHEDULE_OWNER + " = ?) "
                + "AND (" + COL_FROM_DATE + " <= ? AND " + COL_TO_DATE + ">= ?) "
                + "ORDER BY " + COL_AT_TIME + ";";
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(0), strCurDate, strCurDate});
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
                    schedule = new Schedule();

                    strDayOfWeek = cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS));
                    schedule.setWeekDays(cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS)));

                    if(!strDayOfWeek.isEmpty()) {
                        if(strDayOfWeek.charAt(dayOfWeek-1) == '0') {
                            Log.d(TAG,"Day is not fit");
                            continue;
                        }
                    }

                    schedule.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setText(cursor.getString(cursor.getColumnIndex(COL_MSG_TEXT)));
                    schedule.setScheduleFrom(cursor.getString(cursor.getColumnIndex(COL_SCHEDULE_FROM)));
                    schedule.setFromDate(cursor.getString(cursor.getColumnIndex(COL_FROM_DATE)),1);
                    schedule.setToDate(cursor.getString(cursor.getColumnIndex(COL_TO_DATE)),1);
                    schedule.setOnceDate(cursor.getString(cursor.getColumnIndex(COL_ONCE_DATE)),1);
                    schedule.setOnceTime(cursor.getString(cursor.getColumnIndex(COL_ONCE_TIME)));
                    schedule.setPlayRingtone(cursor.getInt(cursor.getColumnIndex(COL_PLAY_RING)));
                    schedule.setTextId(cursor.getInt(cursor.getColumnIndex(COL_TEXT_ID)));
                    schedule.setRingtoneName(cursor.getString(cursor.getColumnIndex(COL_RING_NAME)));
                    schedule.setVibrate(cursor.getInt(cursor.getColumnIndex(COL_VIBRATE)));
                    schedule.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
                    schedule.setRecurring(cursor.getInt(cursor.getColumnIndex(COL_RECURRING)));

                    if(schedule.getRecurring() != MyUsefulFuncs.MONTHLY && schedule.getRecurring() != MyUsefulFuncs.YEARLY) {
                        schedules.add(schedule);
                    }
                    else if (checkIfScheduleForToday(strCurDate,schedule.getOnceDate()) == true){
                        schedules.add(schedule);
                    }

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
        if(db.isOpen()) {
            db.close();
        }

        return schedules;
    }

    public ArrayList<Schedule> ReadFromDataBaseWeek() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String strDayOfWeek = "";
        String strCurDate = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(day, month + 1,year));

        Calendar calWeek = Calendar.getInstance();
        try {
            calWeek = MyUsefulFuncs.addDate(day,month,year,Calendar.WEEK_OF_MONTH,1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Log.d(TAG,"calWeek = " + calWeek.get(Calendar.DAY_OF_MONTH) + "/" + calWeek.get(Calendar.MONTH) + "/" + calWeek.get(Calendar.YEAR));

        String strWeek = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(calWeek.get(Calendar.DAY_OF_MONTH),
                calWeek.get(Calendar.MONTH)+1, calWeek.get(Calendar.YEAR)));

        schedules = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID
                + " IN (select " + dbContactSchedule.COL_SCHEDULE_ID + " FROM "
                + dbContactSchedule.TABLE_NAME + " WHERE " + dbContactSchedule.COL_SCHEDULE_OWNER + " = ?) "
                + "AND ((" + COL_ONCE_DATE + " = \"\" AND (" + COL_FROM_DATE + " <= ? AND "
                + COL_TO_DATE + " >= ? )) OR ("
                + COL_ONCE_DATE + " <> \"\" AND ("
                + COL_ONCE_DATE + " >= ? AND "
                + COL_ONCE_DATE + " <= ?)))"
                + "ORDER BY " + COL_FROM_DATE +
                ";";

        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(0), strCurDate, strWeek, strCurDate, strWeek});
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
                    schedule = new Schedule();

                    strDayOfWeek = cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS));
                    schedule.setWeekDays(cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS)));

                    if(!strDayOfWeek.isEmpty()) {
                        if(strDayOfWeek.charAt(dayOfWeek-1) == '0') {
                            Log.d(TAG,"Day is not fit");
                            continue;
                        }
                    }

                    schedule.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setRecurring(cursor.getInt(cursor.getColumnIndex(COL_RECURRING)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setText(cursor.getString(cursor.getColumnIndex(COL_MSG_TEXT)));
                    schedule.setScheduleFrom(cursor.getString(cursor.getColumnIndex(COL_SCHEDULE_FROM)));
                    schedule.setFromDate(cursor.getString(cursor.getColumnIndex(COL_FROM_DATE)),1);
                    schedule.setToDate(cursor.getString(cursor.getColumnIndex(COL_TO_DATE)),1);
                    schedule.setOnceDate(cursor.getString(cursor.getColumnIndex(COL_ONCE_DATE)),1);
                    schedule.setOnceTime(cursor.getString(cursor.getColumnIndex(COL_ONCE_TIME)));
                    schedule.setPlayRingtone(cursor.getInt(cursor.getColumnIndex(COL_PLAY_RING)));
                    schedule.setTextId(cursor.getInt(cursor.getColumnIndex(COL_TEXT_ID)));
                    schedule.setRingtoneName(cursor.getString(cursor.getColumnIndex(COL_RING_NAME)));
                    schedule.setVibrate(cursor.getInt(cursor.getColumnIndex(COL_VIBRATE)));
                    schedule.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));

                    schedules.add(schedule);

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

        return schedules;
    }

    public ArrayList<Schedule> ReadFromDataBaseMonth() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String strDayOfWeek = "";
        String strCurDate = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(day, month + 1,year));

        Calendar calMonth = Calendar.getInstance();
        try {
            calMonth = MyUsefulFuncs.addDate(day,month+1,year,Calendar.MONTH,1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Log.d(TAG,"calMonth = " + calMonth.get(Calendar.DAY_OF_MONTH) + "/" + calMonth.get(Calendar.MONTH) + "/" + calMonth.get(Calendar.YEAR));

        String strMonth = MyUsefulFuncs.DateToSqlFormat(MyUsefulFuncs.DateToString(calMonth.get(Calendar.DAY_OF_MONTH),
                calMonth.get(Calendar.MONTH), calMonth.get(Calendar.YEAR)));

        schedules = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID
                + " IN (select " + dbContactSchedule.COL_SCHEDULE_ID + " FROM "
                + dbContactSchedule.TABLE_NAME + " WHERE " + dbContactSchedule.COL_SCHEDULE_OWNER + " = ?) "
                + "AND ((" + COL_ONCE_DATE + " = \"\" AND (" + COL_FROM_DATE + " <= ? AND "
                + COL_TO_DATE + " >= ? )) OR ("
                + COL_ONCE_DATE + " <> \"\" AND ("
                + COL_ONCE_DATE + " >= ? AND "
                + COL_ONCE_DATE + " <= ?)));"
                + "ORDER BY " + COL_FROM_DATE +
                ";";

        Cursor cursor = db.rawQuery(sqlQuery, new String[] {String.valueOf(0), strCurDate, strMonth, strCurDate, strMonth});
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
                    schedule = new Schedule();

                    strDayOfWeek = cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS));
                    schedule.setWeekDays(cursor.getString(cursor.getColumnIndex(COL_WEEK_DAYS)));

                    if(!strDayOfWeek.isEmpty()) {
                        if(strDayOfWeek.charAt(dayOfWeek-1) == '0') {
                            Log.d(TAG,"Day is not fit");
                            continue;
                        }
                    }

                    schedule.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setRecurring(cursor.getInt(cursor.getColumnIndex(COL_RECURRING)));
                    schedule.setAtTime(cursor.getString(cursor.getColumnIndex(COL_AT_TIME)));
                    schedule.setText(cursor.getString(cursor.getColumnIndex(COL_MSG_TEXT)));
                    schedule.setScheduleFrom(cursor.getString(cursor.getColumnIndex(COL_SCHEDULE_FROM)));
                    schedule.setFromDate(cursor.getString(cursor.getColumnIndex(COL_FROM_DATE)),1);
                    schedule.setToDate(cursor.getString(cursor.getColumnIndex(COL_TO_DATE)),1);
                    schedule.setOnceDate(cursor.getString(cursor.getColumnIndex(COL_ONCE_DATE)),1);
                    schedule.setOnceTime(cursor.getString(cursor.getColumnIndex(COL_ONCE_TIME)));
                    schedule.setPlayRingtone(cursor.getInt(cursor.getColumnIndex(COL_PLAY_RING)));
                    schedule.setTextId(cursor.getInt(cursor.getColumnIndex(COL_TEXT_ID)));
                    schedule.setRingtoneName(cursor.getString(cursor.getColumnIndex(COL_RING_NAME)));
                    schedule.setVibrate(cursor.getInt(cursor.getColumnIndex(COL_VIBRATE)));
                    schedule.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));

/*** Monthly and Yearly schedules should be appeared only once (from 10/08/2016 - to 15/11/2016) ***/
                    if(schedule.getRecurring() != MyUsefulFuncs.MONTHLY && schedule.getRecurring() != MyUsefulFuncs.YEARLY) {
                        schedules.add(schedule);
                    }
                    else if (MyUsefulFuncs.compareIfExists(schedules,schedule) == false){
                        schedules.add(schedule);
                    }

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

        return schedules;
    }
}
