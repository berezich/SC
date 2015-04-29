package com.berezich.sportconnector.SportObjects;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.yandex.yandexmapkit.utils.GeoPoint;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

/**
 * Created by berezkin on 29.04.2015.
 */
public class InfoTile {

    private String _name="";
    private List<Spot> _spots = new ArrayList<Spot>();
    private int _numChildesSpots=0;
    private GeoPoint _averagePoint;

    public InfoTile(String name)
    {
        _name = name;

    }

    public static InfoTile findInfoTile(String key, HashMap<String, InfoTile> infoTiles){
        int level = key.length();
        InfoTile tileInfo;
        while (level>=2)
            if((tileInfo = infoTiles.get(key))!=null)
                return tileInfo;
            else {
                level-=1;
                key = key.substring(0,level);
            }
        return null;
    }
    public static GeoPoint calcAveragePoint(List<Spot> spots)
    {
        GeoPoint avPoint;
        Spot spot ;
        if(spots!=null && spots.size()>=1)
            avPoint = spots.get(0).geoCoord();
        else
            return null;
        for(int i=1; i<spots.size(); i++)
        {
            spot = spots.get(i);
            avPoint = new GeoPoint((avPoint.getLat()*i+spot.geoCoord().getLat())/(i+1),(avPoint.getLon()*i+spot.geoCoord().getLon())/(i+1));
        }
        return avPoint;
    }
    public String getNumSpotToString(String spotName)
    {
        String num = String.valueOf(_numChildesSpots)+" ";
        if(_numChildesSpots>=10 && _numChildesSpots<=19)
            return num + spotName + "ов";
        int mod = _numChildesSpots%10;
        switch (mod)
        {
            case 1:
                return num + spotName;
            case 2:
            case 3:
            case 4:
                return num + spotName+"a";
            default:
                return num + spotName+"ов";
        }
    }
    public  void addPoint(GeoPoint point)
    {
        _averagePoint = new GeoPoint( (_averagePoint.getLat()*_numChildesSpots + point.getLat())/(_numChildesSpots+1),(_averagePoint.getLon()*_numChildesSpots + point.getLon())/(_numChildesSpots+1));
    }
    public String name() {
        return _name;
    }

    public int numChildesSpots() {
        return _numChildesSpots;
    }

    public void set_numChildesSpots(int _numChildesSpots) {
        this._numChildesSpots = _numChildesSpots;
    }

    public List<Spot> spots() {
        return _spots;
    }

    public void set_averagePoint(GeoPoint _averagePoint) {
        this._averagePoint = _averagePoint;
    }

    public GeoPoint averagePoint() {
        return _averagePoint;
    }
}
