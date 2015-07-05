package com.berezich.sportconnector;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;

/**
 * Created by berezkin on 01.07.2015.
 */
public class LocalDataManager {
    public enum DB_TYPE{READ, WRITE}
    private static final String TAG = "LocalDataManager";
    private static RegionInfo regionInfo;
    private static final String RINFO_KEY = "REGION_INFO";
    private static final String SPOT_TABLE_NAME = "spotTable";
    private static final String DB_NAME = "sportConnectorDB";
    private static DBHelper dbHelper = null;

    //private static GsonBuilder builder = null;
    private static GsonFactory gsonFactory = new GsonFactory();;
    public static boolean loadRegionInfoFromPref(Activity activity)throws IOException
    {
        String urlRegionInfo="";
        regionInfo = new RegionInfo();
        regionInfo.setId(new Long(1));
        regionInfo.setRegionName("moscow");

        /*if(gsonFactory!=null)
            gsonFactory = new GsonFactory();*/

        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        String regionInfoStr = sp.getString(RINFO_KEY, "");
        if(regionInfoStr.equals(""))
            return false;
        regionInfo = gsonFactory.fromString(regionInfoStr,RegionInfo.class);
        Log.d(TAG, "fromJson:" + regionInfo.toString());
        return true;

    }

    public static void saveRegionInfoToPref(Activity activity)throws IOException {
        saveRegionInfoToPref(regionInfo,activity );
    }
    public static void saveRegionInfoToPref(RegionInfo regionInfo,Activity activity)throws IOException
    {
        /*if(gsonFactory!=null)
            gsonFactory = new GsonFactory();*/

        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(RINFO_KEY, gsonFactory.toString(regionInfo));
        editor.commit();
    }

    public static RegionInfo getRegionInfo() {
        return regionInfo;
    }

    private static class DBHelper extends SQLiteOpenHelper
    {
        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context,DB_NAME , null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table "+SPOT_TABLE_NAME+" ("
                    + "id integer primary key,"
                    + "spotId integer,"
                    + "regionId integer);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private static DBHelper getDbHelper(Context context) {
        if(dbHelper==null)
            dbHelper = new DBHelper(context);
        return dbHelper;
    }

    public static SQLiteDatabase getDB(Context context,DB_TYPE type)
    {
        if(type==DB_TYPE.READ)
            return getDbHelper(context).getReadableDatabase();
        else if(type==DB_TYPE.WRITE)
            return getDbHelper(context).getWritableDatabase();
        return null;
    }
}
