package com.berezich.sportconnector.GoogleMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.SportObjects.Coach;
import com.berezich.sportconnector.SportObjects.Coordinates;
import com.berezich.sportconnector.SportObjects.Partner;
import com.berezich.sportconnector.SportObjects.Spot1;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.berezich.sportconnector.backend.sportConnectorApi.model.UpdateSpotInfo;

import java.util.HashMap;
import java.util.List;

/**
 * Created by berezkin on 14.05.2015.
 */
public class SpotsData {
    private static HashMap<Integer, Spot1> _allSpots1 = new HashMap<Integer, Spot1>();

    private static HashMap<Long, Spot> _allSpots = new HashMap<Long, Spot>();
    public static HashMap<Integer, Spot1> get_allSpots1() {
        return _allSpots1;
    }

    public static HashMap<Long, Spot> get_allSpots() {
        return _allSpots;
    }

    public static void loadSpotsFromCache()
    {
        _allSpots = LocalDataManager.getAllSpots();
    }
    public static void saveSpotsToCache(List<Spot> spotLst)
    {
        Spot spot;
        _allSpots.clear();
        for (int i = 0; i <spotLst.size() ; i++) {
            spot = spotLst.get(i);
            _allSpots.put(spot.getId(),spot);
        }
        saveSpotsToCache();
    }
    public static void saveSpotsToCache()
    {
        LocalDataManager.saveAllSpots(_allSpots);
    }
    public static void setSpotUpdatesToCache(List<UpdateSpotInfo> spotUpdates)
    {
        Spot spot;
        Long spotId;
        _allSpots.clear();
        for (int i = 0; i <spotUpdates.size() ; i++) {
            spotId = spotUpdates.get(i).getId();
            spot = spotUpdates.get(i).getSpot();
            if(spot!=null)
                _allSpots.put(spotId,spot);
            else
                _allSpots.remove(spotId);
        }
        LocalDataManager.setUpdateSpots(spotUpdates);
    }
    public static void getSpotsFromCache1()
    {
        Spot1 spot;
        Partner partner;
        Coach coach;
        spot = new Spot1(0,new Coordinates(55.778234, 37.588539),"Комета","Адрес");
        coach = new Coach(0,"Петя","Иванов",33);
        spot.set_favorite(true);
        spot.coaches().add(coach);
        _allSpots1.put(spot.id(), spot);

        spot = new Spot1(1,new Coordinates(55.796051, 37.537766),"Теннисный клуб ЦСКА","Адрес");
        partner = new Partner(0,"Вася","Клюев",44);
        spot.partners().add(partner);
        partner = new Partner(2,"Петя","Клюев",42);
        spot.partners().add(partner);
        coach = new Coach(1,"Иван","Мартирасян",30);
        spot.coaches().add(coach);
        spot.set_favorite(true);
        _allSpots1.put(spot.id(), spot);

        spot = new Spot1(2,new Coordinates(55.795504, 37.541117),"Европейская школа Тенниса","Адрес");
        spot.set_favorite(true);
        _allSpots1.put(spot.id(), spot);

        spot = new Spot1(3,new Coordinates(55.792503, 37.536984),"Европейская школа Тенниса","Адрес");
        _allSpots1.put(spot.id(), spot);

        spot = new Spot1(4,new Coordinates(55.804162, 37.561679),"Теннисенок","Адрес");
        _allSpots1.put(spot.id(), spot);

        spot = new Spot1(5,new Coordinates(55.768345, 37.693669),"Планета тенниса","Адрес");
        _allSpots1.put(spot.id(), spot);

        spot = new Spot1(6,new Coordinates(55.715099, 37.555023),"TennisVIP","Адрес");
        _allSpots1.put(spot.id(), spot);
    }
    public static void setSpotFavorite(int idSpot, boolean isFavorite)
    {
        Spot1 spot = _allSpots1.get(idSpot);
        if(spot!=null)
            spot.set_favorite(isFavorite);
    }
}
