package com.physis.correction.chair.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.physis.correction.chair.data.DeviceInfo;

import java.util.LinkedList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = DBHelper.class.getName();

    public static final String DEVICE_TABLE= "CCA";

    private static final String DATABASE = "CCA.db";
    private static final int VERSION = 1;

    public static final String COL_ADDR = "addr";
    public static final String COL_NAME = "name";
    public static final String COL_PRESSURE = "pressure";

    public DBHelper(Context context){
        super(context, DATABASE, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DEVICE_TABLE + " (" +
                COL_ADDR + " TEXT UNIQUE NOT NULL, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_PRESSURE + " TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DEVICE_TABLE);
        onCreate(db);
    }

    public boolean insertData(ContentValues values){
        return getWritableDatabase().insert(DEVICE_TABLE, null, values) > 0;
    }

    public boolean updateData(ContentValues values, String targetColumn, String targetValue){
        return getWritableDatabase().update(DEVICE_TABLE, values, targetColumn + " = '" + targetValue + "'",null) > 0;
    }

    public boolean deleteData(String targetColumn, String targetValue){
        return getWritableDatabase().delete(DEVICE_TABLE, targetColumn + " = '" + targetValue + "'",null) > 0;
    }

    public List<DeviceInfo> getDevices(){
        List<DeviceInfo> devices = new LinkedList<>();
        @SuppressLint("Recycle")
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + DEVICE_TABLE, null);

        if (cursor.moveToFirst()) {
            do {
                devices.add(new DeviceInfo(
                        cursor.getString(cursor.getColumnIndex(COL_NAME)),
                        cursor.getString(cursor.getColumnIndex(COL_ADDR)),
                        cursor.getString(cursor.getColumnIndex(COL_PRESSURE))
                ));
            } while (cursor.moveToNext());
        }
        return devices;
    }
}