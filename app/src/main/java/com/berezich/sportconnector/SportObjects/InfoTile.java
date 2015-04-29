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
    //private int _numChildesSpots=0;
    private GeoPoint _averagePoint;

    private List<ShotInfoSpot> _infoChildSpots = new ArrayList<ShotInfoSpot>();

    public InfoTile(String name)
    {
        _name = name;

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
        int numChildSpots = _infoChildSpots.size();
        String num = String.valueOf(numChildSpots)+" ";
        if(numChildSpots>=10 && numChildSpots<=19)
            return num + spotName + "ов";
        int mod = numChildSpots%10;
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
        int numSpots = _infoChildSpots.size();
        _averagePoint = new GeoPoint( (_averagePoint.getLat()*numSpots + point.getLat())/(numSpots+1),(_averagePoint.getLon()*numSpots + point.getLon())/(numSpots+1));
    }
    public void addInfoChildSpots(List<Spot> spots)
    {
        for(int i=0; i<spots.size(); i++)
            addInfoChildSpot(spots.get(i));
    }
    public void addInfoChildSpot(Spot spot)
    {
        ShotInfoSpot shotInfo = new ShotInfoSpot(spot.id());
        addPoint(spot.geoCoord());
        shotInfo.setCoachExists(!spot.coaches().isEmpty());
        shotInfo.setPartnerExists(!spot.partners().isEmpty());
        shotInfo.setMyFavorite(spot.favorite());
        _infoChildSpots.add(shotInfo);
    }

    public String name() {
        return _name;
    }

/*
    public int numChildesSpots() {
        return _numChildesSpots;
    }

    public void set_numChildesSpots(int _numChildesSpots) {
        this._numChildesSpots = _numChildesSpots;
    }
*/

    public List<Spot> spots() {
        return _spots;
    }

    public void set_averagePoint(GeoPoint _averagePoint) {
        this._averagePoint = _averagePoint;
    }

    public GeoPoint averagePoint() {
        return _averagePoint;
    }

    public List<ShotInfoSpot> infoChildSpots() {
        return _infoChildSpots;
    }

    public static class ShotInfoSpot{
        int _id;
        boolean _isPartnerExists = false;
        boolean _isCoachExists = false;
        boolean _isMyFavorite = false;

        public ShotInfoSpot(int _id) {
            this._id = _id;
        }

        public int id() {
            return _id;
        }

        public boolean isPartnerExists() {
            return _isPartnerExists;
        }

        public void setPartnerExists(boolean isPartnerExists) {
            this._isPartnerExists = isPartnerExists;
        }

        public boolean isCoachExists() {
            return _isCoachExists;
        }

        public void setCoachExists(boolean isCoachExists) {
            this._isCoachExists = isCoachExists;
        }

        public boolean isMyFavorite() {
            return _isMyFavorite;
        }

        public void setMyFavorite(boolean isMyFavorite) {
            this._isMyFavorite = isMyFavorite;
        }
    }
}
