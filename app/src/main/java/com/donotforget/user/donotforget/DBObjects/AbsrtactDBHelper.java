package com.donotforget.user.donotforget.DBObjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.donotforget.user.donotforget.objects.MyUsefulFuncs;

/**
 * Created by user on 29.06.2016.
 */
public abstract class AbsrtactDBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "donotforget.db";
    public static final int DB_VERSION = 1;
    public static final String COL_ID = "_id";
    protected long id;

    static String getCOL_ID_Sql() {
        return MyUsefulFuncs.concat(COL_ID, " INTEGER PRIMARY KEY AUTOINCREMENT, ");
    }

    public AbsrtactDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBSchedule.getSqlCommand());
        db.execSQL(DBMessages.getSqlCommand());
        db.execSQL(DBContactGroups.getSqlCommand());
        db.execSQL(DBContactSchedule.getSqlCommand());
        db.execSQL(DBContacts.getSqlCommand());
    }

    void updateDataBase(ContentValues cv) {
        cv.put(COL_ID, id);
    }

    void load(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndex(COL_ID));
    }

    protected void resetParams() {
        id = 0;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }



    public abstract boolean updateDataBase(SQLiteDatabase db);


}
