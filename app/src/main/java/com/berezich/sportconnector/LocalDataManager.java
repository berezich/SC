package com.berezich.sportconnector;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.berezich.sportconnector.backend.sportConnectorApi.SportConnectorApi;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
    private static Context context = null;

    //private static GsonBuilder builder = null;
    private static GsonFactory gsonFactory = new GsonFactory();
    public static void init(Context context)
    {
        LocalDataManager.context = context;
    }
    public static boolean loadRegionInfoFromPref(Activity activity)throws IOException
    {
        /*String urlRegionInfo="";
        regionInfo = new RegionInfo();
        regionInfo.setId(new Long(1));
        regionInfo.setRegionName("moscow");*/

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

    public static void setRegionInfo(RegionInfo regionInfo) {
        LocalDataManager.regionInfo = regionInfo;
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
                    + "regionId integer,"
                    + "value text);");
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

    private static SQLiteDatabase getDB(DB_TYPE type)
    {
        if(type==DB_TYPE.READ)
            return getDbHelper(context).getReadableDatabase();
        else if(type==DB_TYPE.WRITE)
            return getDbHelper(context).getWritableDatabase();
        return null;
    }
    public static HashMap<Long, Spot> getAllSpots()
    {
        HashMap<Long, Spot> spots = new HashMap<Long, Spot>();
        String spotVal;
        Spot spot;
        SQLiteDatabase db = getDB(DB_TYPE.READ);
        if(db!=null)
        {
            // делаем запрос всех данных из таблицы mytable, получаем Cursor
            Cursor c = db.query(SPOT_TABLE_NAME, null, null, null, null, null, null);

            // ставим позицию курсора на первую строку выборки
            // если в выборке нет строк, вернется false
            if (c.moveToFirst()) {

                // определяем номера столбцов по имени в выборке
                int valColIndex = c.getColumnIndex("value");

                do {
                    spotVal = c.getString(valColIndex);
                    try {
                        spot = gsonFactory.fromString(spotVal,Spot.class);
                        spots.put(spot.getId(), spot);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            } else
                Log.d(TAG, "0 spots in "+SPOT_TABLE_NAME+" table");
            c.close();
        }
        dbHelper.close();
        return spots;
    }

    //delete old spots and save new
    public static void saveAllSpots(HashMap<Long, Spot> spotHsh)
    {
        List<Spot> spotLst = new ArrayList<Spot>(spotHsh.values());
        saveAllSpots(spotLst);
    }
    public static void saveAllSpots(List<Spot> spotLst)
    {
        ContentValues cv;

        Spot spot;
        SQLiteDatabase db = getDB(DB_TYPE.WRITE);
        if(db!=null)
        {
            int clearCount = db.delete(SPOT_TABLE_NAME, null, null);
            Log.d(TAG, "deleted spots form "+SPOT_TABLE_NAME +" table count = " + clearCount);

            for (int i = 0; i <spotLst.size() ; i++) {
                spot = spotLst.get(i);
                cv = new ContentValues();
                cv.put("spotId", spot.getId());
                cv.put("regionId", spot.getRegionId());
                try {
                    cv.put("value", gsonFactory.toString(spot));
                    db.insert(SPOT_TABLE_NAME, null, cv);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"error save Spot(id:"+spot.getId()+" name:"+spot.getName()+") to "+SPOT_TABLE_NAME +" table");
                }

            }

        }
        dbHelper.close();
    }

}
