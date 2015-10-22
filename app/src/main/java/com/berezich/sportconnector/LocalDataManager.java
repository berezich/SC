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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by berezkin on 01.07.2015.
 */
public class LocalDataManager {
    public enum DB_TYPE{READ, WRITE}
    private static final String TAG = "MyLog_LocalDataManager";
    private static RegionInfo regionInfo;
    private static Person myPersonInfo;
    private static AppPref appPref;
    private static final String RINFO_KEY = "REGION_INFO";
    private static final String MY_PERSON_INFO_KEY = "MY_PERSON_INFO";
    private static final String APP_PREF_KEY = "APP_PREF";
    private static final String SPOT_TABLE_NAME = "spotTable";
    private static final String DB_NAME = "sportConnectorDB";
    private static final int DB_VERSION = 3;
    private static DBHelper dbHelper = null;
    private static Context context = null;
    private static Activity activity = null;


    private static GsonFactory gsonFactory = new GsonFactory();
    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson;


    public static void init(Activity activity)
    {
        LocalDataManager.activity = activity;
        LocalDataManager.context = activity.getBaseContext();
        gsonBuilder.serializeNulls();
        gson = gsonBuilder.create();
    }
    private static boolean loadRegionInfoFromPref()
    {
        if(activity==null)
        {
            Log.e(TAG,"loadRegionInfoFromPref failed activity == null");
            return false;
        }
        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        String regionInfoStr = sp.getString(RINFO_KEY, "");
        if(regionInfoStr.equals("")) {
            Log.d(TAG, "no regionInfo was fetched form Preferences");
            return false;
        }
        try {
            regionInfo = gsonFactory.fromString(regionInfoStr,RegionInfo.class);
            Log.d(TAG, "regionInfo was fetched form Preferences");
            Log.d(TAG, "regionInfo: " + regionInfoStr);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading regionInfo from Preferences");
            return false;
        }

        return true;

    }



    public static void saveRegionInfoToPref(Activity activity)throws IOException {
        saveRegionInfoToPref(regionInfo, activity);
    }
    public static void saveRegionInfoToPref(RegionInfo regionInfo,Activity activity)throws IOException
    {
        if(activity==null)
        {
            Log.e(TAG,"saveRegionInfoToPref failed activity == null");
            return;
        }
        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(RINFO_KEY, gsonFactory.toString(regionInfo));
        editor.apply();
    }
    public static RegionInfo getRegionInfo() {
        if(activity==null)
        {
            Log.e(TAG,"getRegionInfo failed activity == null");
            return null;
        }
        if(regionInfo==null)
            loadRegionInfoFromPref();
        return regionInfo;
    }

    public static void setRegionInfo(RegionInfo regionInfo) {
        LocalDataManager.regionInfo = regionInfo;
    }

    private static boolean loadMyPersonInfoFromPref()
    {
        if(activity==null)
        {
            Log.e(TAG,"loadMyPersonInfoFromPref failed activity == null");
            return false;
        }
        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        String personInfoStr = sp.getString(MY_PERSON_INFO_KEY, "");
        if(personInfoStr == null || personInfoStr.equals("")) {
            Log.d(TAG, "no myPersonInfo was fetched from Preferences");
            return false;
        }
        try {
            myPersonInfo = gsonFactory.fromString(personInfoStr,Person.class);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading myPersonInfo from Preferences");
            return false;
        }

        Log.d(TAG, "myPersonInfo was fetched from Preferences");
        Log.d(TAG, "myPersonInfo: " + personInfoStr);

        return true;
    }
    public static void saveMyPersonInfoToPref(Activity activity)throws IOException {
        saveMyPersonInfoToPref(myPersonInfo, activity);
    }
    public static void saveMyPersonInfoToPref(Person personInfo,Activity activity)throws IOException {
        if(activity==null)
        {
            Log.e(TAG,"saveMyPersonInfoToPref failed activity == null");
            return;
        }
        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(MY_PERSON_INFO_KEY, gsonFactory.toString(personInfo));
        editor.apply();
        setMyPersonInfo(personInfo);
    }
    public static Person getMyPersonInfo() {
        if(activity==null)
        {
            Log.e(TAG,"getMyPersonInfo failed activity == null");
            return null;
        }
        if(myPersonInfo==null)
            loadMyPersonInfoFromPref();
        return myPersonInfo;
    }

    public static void setMyPersonInfo(Person myPersonInfo) {
        LocalDataManager.myPersonInfo = myPersonInfo;
        initListsOfPerson(myPersonInfo);
    }

    public static AppPref getAppPref() {
        if(activity==null)
        {
            Log.e(TAG,"getAppPref failed activity == null");
            return null;
        }
        if(appPref==null)
            loadAppPref();
        return appPref;
    }

    public static void setAppPref(AppPref appPref) {
        LocalDataManager.appPref = appPref;
    }

    public static void saveAppPref(Activity activity)throws IOException {
        saveAppPref(appPref, activity);
    }
    public static void saveAppPref(AppPref appPref,Activity activity)throws IOException {
        if(activity==null)
        {
            Log.e(TAG,"saveAppPref failed activity == null");
            return;
        }
        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String appStr = gson.toJson(appPref);
        editor.putString(APP_PREF_KEY, appStr);
        editor.apply();

    }

    private static boolean loadAppPref()
    {
        if(activity==null)
        {
            Log.e(TAG,"loadAppPref failed activity == null");
            return false;
        }
        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);
        String appPrefStr = sp.getString(APP_PREF_KEY, "");
        if("".equals(appPrefStr)) {
            Log.d(TAG, "no AppPref was fetched from Preferences");
            return false;
        }
        try {
            appPref = gson.fromJson(appPrefStr, AppPref.class);

            Log.d(TAG, "AppPref was fetched from Preferences");
            Log.d(TAG, "AppPref: "+appPrefStr);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading AppPref from Preferences");
            return false;
        }

        return true;
    }


    public static boolean isMyFavoriteSpot(Spot spot)
    {
        boolean isFavorite = false;
        if(myPersonInfo==null)
            loadMyPersonInfoFromPref();
        String myType = myPersonInfo.getType();
        Long myPersonId = myPersonInfo.getId();
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
            if (oldVersion != newVersion)
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
        if(activity==null)
        {
            Log.e(TAG,"getDB failed activity == null");
            return null;
        }
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
        if(activity==null)
        {
            Log.e(TAG,"getAllSpots failed activity == null");
            return null;
        }
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
                Log.d(TAG, spots.size() +" spots were fetched from "+SPOT_TABLE_NAME+" table");
            } else
                Log.d(TAG, "0 spots exist in "+SPOT_TABLE_NAME+" table");
            c.close();
        }
        else
            Log.e(TAG, "Can't open " + SPOT_TABLE_NAME + " table");
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
        // TODO: 25.09.2015 refactor this method to AsynkTask
        ContentValues cv;
        int cnt=0;
        Spot spot;

        if(activity==null)
        {
            Log.e(TAG,"saveAllSpots failed activity == null");
            return;
        }

        Log.d(TAG, "Uploading new spots in " + SPOT_TABLE_NAME + " table");
        SQLiteDatabase db = getDB(DB_TYPE.WRITE);
        if(db!=null)
        {
            int clearCount = db.delete(SPOT_TABLE_NAME, null, null);
            Log.d(TAG, clearCount+" spots were deleted form " + SPOT_TABLE_NAME + " table");

            for (int i = 0; i <spotLst.size() ; i++) {
                spot = spotLst.get(i);
                cv = new ContentValues();
                cv.put("id", spot.getId());
                cv.put("regionId", spot.getRegionId());
                try {
                    cv.put("value", gsonFactory.toString(spot));
                    db.insert(SPOT_TABLE_NAME, null, cv);
                    cnt++;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"error save Spot(id:"+spot.getId()+" name:"+spot.getName()+") to "+SPOT_TABLE_NAME +" table");
                }

            }
            Log.d(TAG, cnt+" spots were saved in " + SPOT_TABLE_NAME +" table" );

        }

        dbHelper.close();
    }
    public  static void setUpdateSpots(List<UpdateSpotInfo> updateSpotsLst)
    {
        //TODO: refactor this method to AsynkTask
        Spot spot;
        Long spotId;
        int cnt = 0;
        if(activity==null)
        {
            Log.e(TAG,"setUpdateSpots failed activity == null");
            return;
        }
        SQLiteDatabase db = getDB(DB_TYPE.WRITE);
        Log.d(TAG, "Updating spots in" + SPOT_TABLE_NAME + " table");
        if(db!=null) {
            for (int i = 0; i < updateSpotsLst.size(); i++) {
                spotId = updateSpotsLst.get(i).getId();
                spot = updateSpotsLst.get(i).getSpot();
                if(updateSpot(spotId,spot,db))
                    cnt++;
            }
        }
        dbHelper.close();
        Log.d(TAG, +cnt + " spots were updated in " + SPOT_TABLE_NAME + " table");

    }
    public static void initListsOfSpot(Spot spot)
    {
        if(spot.getCoachLst()==null)
            spot.setCoachLst(new ArrayList<Long>());
        if(spot.getPartnerLst()==null)
            spot.setPartnerLst(new ArrayList<Long>());
        if(spot.getPictureLst()==null)
            spot.setPictureLst(new ArrayList<Picture>());
    }
    public static void initListsOfPerson(Person person)
    {
        if(person.getPictureLst()==null)
            person.setPictureLst(new ArrayList<Picture>());
        if(person.getMyFriends()==null)
            person.setMyFriends(new ArrayList<String>());
        if(person.getFavoriteSpotIdLst()==null)
            person.setFavoriteSpotIdLst(new ArrayList<Long>());
    }
    public static boolean updateSpot(Long spotId, Spot spot,SQLiteDatabase db)
    {
        boolean needCloseDB = false;
        boolean isUpdate = false;
        ContentValues cv;
        if(activity==null)
        {
            Log.e(TAG,"updateSpot failed activity == null");
            return false;
        }
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
                    isUpdate=true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "error update Spot(id:" + spotId + " name:" + spot.getName() + ") to " + SPOT_TABLE_NAME + " table");
                }

            } else {
                try {
                    db.delete(SPOT_TABLE_NAME, "id = " + spotId, null);
                    Log.d(TAG, "Spot(id:" + spotId + ")was deleted from" + SPOT_TABLE_NAME + " table");
                }
                catch (Exception ex)
                {
                    Log.e(TAG,String.format("failed deleting spotInfo id = %d form SQL_DB",spotId));
                }
            }
        }
        if(needCloseDB)
            dbHelper.close();
        return isUpdate;
    }
}
