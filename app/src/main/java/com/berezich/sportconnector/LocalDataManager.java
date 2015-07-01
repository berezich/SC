package com.berezich.sportconnector;

import android.util.Log;

import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by berezkin on 01.07.2015.
 */
public class LocalDataManager {
    private static final String TAG = "LocalDataManager";
    private static RegionInfo regionInfo;
    private static GsonBuilder builder = null;
    private static Gson gson = null;
    public static void loadRegionInfoFromStorage()
    {
        String urlRegionInfo="";
        if(builder==null) {
            builder = new GsonBuilder();
            builder.setPrettyPrinting().serializeNulls();
            gson = builder.create();
        }
        regionInfo = new RegionInfo();
        regionInfo.setId(new Long(1));
        regionInfo.setRegionName("moscow");
        urlRegionInfo = gson.toJson(regionInfo);
        Log.d(TAG,"toJson:"+urlRegionInfo);

        regionInfo = gson.fromJson(urlRegionInfo, RegionInfo.class);
        Log.d(TAG,"fromJson:"+regionInfo.toString());
    }
}
