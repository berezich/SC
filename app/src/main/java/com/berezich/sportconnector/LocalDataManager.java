package com.berezich.sportconnector;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.berezich.sportconnector.backend.sportConnectorApi.model.UpdateSpotInfo;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by berezkin on 01.07.2015.
 */
public class LocalDataManager {
    public enum DB_TYPE{READ, WRITE}
    private static final String TAG = "LocalDataManager";
    private static RegionInfo regionInfo;
    private static Person myPersonInfo;
    private static AppPref appPref;
    private static final String RINFO_KEY = "REGION_INFO";
    private static final String MY_PERSON_INFO_KEY = "MY_PERSON_INFO";
    private static final String APP_PREF_KEY = "APP_PREF";
    private static final String SPOT_TABLE_NAME = "spotTable";
    private static final String DB_NAME = "sportConnectorDB";
    private static final int DB_VERSION = 2;
    private static DBHelper dbHelper = null;
    private static Context context = null;


    //private static GsonBuilder builder = null;
    private static GsonFactory gsonFactory = new GsonFactory();
    public static void init(Context context)
    {
        LocalDataManager.context = context;
    }
    public static boolean loadRegionInfoFromPref(Activity activity)
    {

        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        String regionInfoStr = sp.getString(RINFO_KEY, "");
        if(regionInfoStr!=null && regionInfoStr.equals(""))
            return false;

        try {
            regionInfo = gsonFactory.fromString(regionInfoStr,RegionInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading regionInfo from Preferences");
            return false;
        }
        Log.d(TAG, "fromJson:" + regionInfo.toString());
        return true;

    }



    public static void saveRegionInfoToPref(Activity activity)throws IOException {
        saveRegionInfoToPref(regionInfo, activity);
    }
    public static void saveRegionInfoToPref(RegionInfo regionInfo,Activity activity)throws IOException
    {
        /*if(gsonFactory!=null)
            gsonFactory = new GsonFactory();*/

        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(RINFO_KEY, gsonFactory.toString(regionInfo));
        editor.apply();
    }
    public static RegionInfo getRegionInfo() {
        return regionInfo;
    }

    public static void setRegionInfo(RegionInfo regionInfo) {
        LocalDataManager.regionInfo = regionInfo;
    }

    public static boolean loadMyPersonInfoFromPref(Activity activity)throws IOException
    {

        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        String personInfoStr = sp.getString(MY_PERSON_INFO_KEY, "");
        if(personInfoStr != null && personInfoStr.equals("")) {
            return false;
        }
        try {
            myPersonInfo = gsonFactory.fromString(personInfoStr,Person.class);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading myPersonInfo from Preferences");
            return false;
        }

        /*myPersonInfo = new Person();
        myPersonInfo.setType("PARTNER");
        myPersonInfo.setId(new Long("5705241014042624"));*/

        return true;
    }
    public static void saveMyPersonInfoToPref(Activity activity)throws IOException {
        saveMyPersonInfoToPref(myPersonInfo, activity);
    }
    public static void saveMyPersonInfoToPref(Person personInfo,Activity activity)throws IOException {
        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(MY_PERSON_INFO_KEY, gsonFactory.toString(personInfo));
        editor.apply();
        setMyPersonInfo(myPersonInfo);
    }
    public static Person getMyPersonInfo() {
        return myPersonInfo;
    }

    public static void setMyPersonInfo(Person myPersonInfo) {
        LocalDataManager.myPersonInfo = myPersonInfo;
    }

    public static AppPref getAppPref() {
        return appPref;
    }

    public static void setAppPref(AppPref appPref) {
        LocalDataManager.appPref = appPref;
    }

    public static void saveAppPref(Activity activity)throws IOException {
        saveAppPref(appPref, activity);
    }
    public static void saveAppPref(AppPref appPref,Activity activity)throws IOException {
        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(APP_PREF_KEY, gsonFactory.toString(appPref));
        editor.apply();

    }

    public static boolean loadAppPref(Activity activity)throws IOException
    {

        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        String appPrefStr = sp.getString(APP_PREF_KEY, "");
        if(appPrefStr != null && "".equals(appPrefStr)) {
            return false;
        }
        try {
            appPref = gsonFactory.fromString(appPrefStr,AppPref.class);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading AppPref from Preferences");
            return false;
        }
        Log.d(TAG, "fromJson:" + regionInfo.toString());

        /*myPersonInfo = new Person();
        myPersonInfo.setType("PARTNER");
        myPersonInfo.setId(new Long("5705241014042624"));*/

        return true;
    }


    public static boolean isMyFavoriteSpot(Spot spot)
    {
        boolean isFavorite = false;
        String myType = myPersonInfo.getType();
        String myPersonId = myPersonInfo.getId();
        if( myType.equals("COACH"))
            isFavorite = spot.getCoachLst().contains(myPersonId);
        else if(myType.equals("PARTNER"))
            isFavorite = spot.getPartnerLst().contains(myPersonId);
        return isFavorite;
    }

    private static class DBHelper extends SQLiteOpenHelper
    {
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "--- onCreate database ---");
            db.execSQL("create table " + SPOT_TABLE_NAME + " ("
                    + "id integer primary key,"
                    + "regionId integer,"
                    + "value text);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1 && newVersion == 2)
            {
                db.beginTransaction();
                try{
                    db.execSQL("drop table "+ SPOT_TABLE_NAME+";");

                    db.execSQL("create table "+ SPOT_TABLE_NAME+" ("
                            + "id integer primary key,"
                            + "regionId integer,"
                            + "value text);");
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }

            }
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
        HashMap<Long, Spot> spots = new HashMap<>();
        String spotVal;
        Spot spot;
        SQLiteDatabase db = getDB(DB_TYPE.READ);
        if(db!=null)
        {
            Cursor c = db.query(SPOT_TABLE_NAME, null, null, null, null, null, null);

            if (c.moveToFirst()) {

                int valColIndex = c.getColumnIndex("value");

                do {
                    spotVal = c.getString(valColIndex);
                    try {
                        spot = gsonFactory.fromString(spotVal,Spot.class);
                        initListsOfSpot(spot);
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
        List<Spot> spotLst = new ArrayList<>(spotHsh.values());
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
            Log.d(TAG, "deleted spots form " + SPOT_TABLE_NAME + " table count = " + clearCount);

            for (int i = 0; i <spotLst.size() ; i++) {
                spot = spotLst.get(i);
                cv = new ContentValues();
                cv.put("id", spot.getId());
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
    public  static void setUpdateSpots(List<UpdateSpotInfo> updateSpotsLst)
    {
        Spot spot;
        Long spotId;
        SQLiteDatabase db = getDB(DB_TYPE.WRITE);
        if(db!=null) {
            Log.d(TAG, "--- Update " + SPOT_TABLE_NAME + " ---");
            for (int i = 0; i < updateSpotsLst.size(); i++) {
                spotId = updateSpotsLst.get(i).getId();
                spot = updateSpotsLst.get(i).getSpot();
                updateSpot(spotId,spot,db);
            }
        }
        dbHelper.close();

    }
    public static void initListsOfSpot(Spot spot)
    {
        if(spot.getCoachLst()==null)
            spot.setCoachLst(new ArrayList<String>());
        if(spot.getPartnerLst()==null)
            spot.setPartnerLst(new ArrayList<String>());
        if(spot.getPictureLst()==null)
            spot.setPictureLst(new ArrayList<Picture>());
    }
    public static void updateSpot(Long spotId, Spot spot,SQLiteDatabase db)
    {
        boolean needCloseDB = false;
        ContentValues cv;
        if(db==null)
        {
            db = getDB(DB_TYPE.WRITE);
            needCloseDB = true;
        }
        if(db!=null) {
            if (spot != null) {
                cv = new ContentValues();
                cv.put("id", spotId);
                cv.put("regionId", spot.getRegionId());
                try {
                    cv.put("value", gsonFactory.toString(spot));
                    int updCount = db.update(SPOT_TABLE_NAME, cv, "id = ?", new String[]{spotId.toString()});
                    if (updCount == 0)
                        db.insert(SPOT_TABLE_NAME, null, cv);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "error update Spot(id:" + spotId + " name:" + spot.getName() + ") to " + SPOT_TABLE_NAME + " table");
                }

            } else {
                Log.d(TAG, "--- Delete from" + SPOT_TABLE_NAME + " : ---");
                db.delete(SPOT_TABLE_NAME, "spotId = " + spotId, null);
            }
        }
        if(needCloseDB)
            dbHelper.close();
    }
}
