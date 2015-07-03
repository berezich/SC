package com.berezich.sportconnector;

import android.util.Log;

import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

/**
 * Created by berezkin on 01.07.2015.
 */
public class LocalDataManager {
    private static final String TAG = "LocalDataManager";
    private static RegionInfo regionInfo;
    private static RegInf regInf;
    private static GsonBuilder builder = null;
    private static Gson gson = null;
    public static void loadRegionInfoFromStorage()throws IOException
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

        //urlRegionInfo = gson.toJson(regionInfo);

        GsonFactory factory= new GsonFactory();
        urlRegionInfo = factory.toString(regionInfo);

        Log.d(TAG,"toJson:"+urlRegionInfo);

        //regionInfo = gson.fromJson(urlRegionInfo, RegionInfo.class);
        regionInfo = factory.fromString(urlRegionInfo,RegionInfo.class);
        Log.d(TAG,"fromJson:"+regionInfo.toString());

        /*regInf = new RegInf();
        regInf.setId(new Long(1));
        regInf.setName("moscow");
        urlRegionInfo = gson.toJson(regInf);
        Log.d(TAG,"toJson:"+urlRegionInfo);

        regInf = gson.fromJson(urlRegionInfo, RegInf.class);
        Log.d(TAG, "fromJson:" + regInf.toString());*/
    }
    static class RegInf{
        Long id;
        DateTime update;
        String name;
        String version;

        public RegInf() {
        }

        public Long getId() {
            return id;
        }

        public DateTime getUpdate() {
            return update;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setUpdate(DateTime update) {
            this.update = update;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
