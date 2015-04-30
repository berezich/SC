package com.berezich.sportconnector.SportObjects;

import android.util.Log;

import com.berezich.sportconnector.MainFragment;
import com.berezich.sportconnector.YaMap.TilesInfoData;

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
    private List<Integer> _spots = new ArrayList<Integer>();
    public static enum Filters {F0000,F1000,F0100,F0001,F1100,F1001,F0101,F1101,Fxx1x}
    //filters order partner-coach-court-favorite
    private GeoPoint _avrPoint_1000;
    private GeoPoint _avrPoint_0100;
    private GeoPoint _avrPoint_0001;
    private GeoPoint _avrPoint_1100;
    private GeoPoint _avrPoint_1001;
    private GeoPoint _avrPoint_0101;
    private GeoPoint _avrPoint_1101;
    private GeoPoint _avrPoint_xx1x;

    private List<Integer> _childSpots_1000 = new ArrayList<Integer>();
    private List<Integer> _childSpots_0100 = new ArrayList<Integer>();
    private List<Integer> _childSpots_0001 = new ArrayList<Integer>();
    private List<Integer> _childSpots_1100 = new ArrayList<Integer>();
    private List<Integer> _childSpots_1001 = new ArrayList<Integer>();
    private List<Integer> _childSpots_0101 = new ArrayList<Integer>();
    private List<Integer> _childSpots_1101 = new ArrayList<Integer>();
    private List<Integer> _childSpots_xx1x = new ArrayList<Integer>();

    //private GeoPoint _averagePoint;

    //private List<ShotInfoSpot> _infoChildSpots = new ArrayList<ShotInfoSpot>();

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
    public String getNumSpotToString(String spotName,Filters filter)
    {
        int numChildSpots = getChildSpots(filter).size();
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
    public  void addPoint(GeoPoint point, Filters filter)
    {
        int numSpots = getChildSpots(filter).size();
        GeoPoint avrPoint = getAvrPoint(filter);
        if(avrPoint!=null)
            avrPoint = new GeoPoint( (avrPoint.getLat()*numSpots + point.getLat())/(numSpots+1),(avrPoint.getLon()*numSpots + point.getLon())/(numSpots+1));
        else
            avrPoint = point;
        setAvrPoint(avrPoint, filter);
    }
    public void addInfoChildSpots(List<Integer> spots)
    {
        for(int i=0; i<spots.size(); i++)
            addInfoChildSpot(TilesInfoData.allSpots().get(spots.get(i)));
    }
    public void addInfoChildSpot(Spot spot)
    {
        //ShotInfoSpot shotInfo = new ShotInfoSpot(spot.id());
        List<Filters> filters = spot.getAppropriateFilters();
        for(Filters filter: filters) {
            addPoint(spot.geoCoord(), filter);
            getChildSpots(filter).add(spot.id());
        }
    }

    public  static boolean isAppropriate(Spot spot, Filters filter)
    {

        List<Filters> filters = spot.getAppropriateFilters();
        for(Filters filterItem:filters)
            if(filter == filterItem)
                return true;
        return  false;
    }
    public List<Integer> getChildSpots(Filters filter)
    {
        if(filter == Filters.F1000)
            return _childSpots_1000;
        if(filter == Filters.F0100)
            return _childSpots_0100;
        if(filter == Filters.F0001)
            return _childSpots_0001;
        if(filter == Filters.F1100)
            return _childSpots_1100;
        if(filter == Filters.F1001)
            return _childSpots_1001;
        if(filter == Filters.F0101)
            return _childSpots_0101;
        if(filter == Filters.F1101)
            return _childSpots_1101;
        if(filter == Filters.Fxx1x)
            return _childSpots_xx1x;
        return null;
    }
    public GeoPoint getAvrPoint(Filters filter)
    {
        if(filter == Filters.F1000)
            return _avrPoint_1000;
        if(filter == Filters.F0100)
            return _avrPoint_0100;
        if(filter == Filters.F0001)
            return _avrPoint_0001;
        if(filter == Filters.F1100)
            return _avrPoint_1100;
        if(filter == Filters.F1001)
            return _avrPoint_1001;
        if(filter == Filters.F0101)
            return _avrPoint_0101;
        if(filter == Filters.F1101)
            return _avrPoint_1101;
        if(filter == Filters.Fxx1x)
            return _avrPoint_xx1x;
        return null;
    }
    public void setAvrPoint(GeoPoint avrPoint,Filters filter)
    {
        if(filter == Filters.F1000) {
            _avrPoint_1000 = avrPoint;
        }
        else if(filter == Filters.F0100)
            _avrPoint_0100 = avrPoint;
        else if(filter == Filters.F0001)
            _avrPoint_0001 = avrPoint;
        else if(filter == Filters.F1100)
            _avrPoint_1100 = avrPoint;
        else if(filter == Filters.F1001)
            _avrPoint_1001 = avrPoint;
        else if(filter == Filters.F0101)
            _avrPoint_0101 = avrPoint;
        else if(filter == Filters.F1101)
            _avrPoint_1101 = avrPoint;
        else if(filter == Filters.Fxx1x)
            _avrPoint_xx1x = avrPoint;
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

    public List<Integer> spots() {
        return _spots;
    }

    /*public void set_averagePoint(GeoPoint _averagePoint) {
        this._averagePoint = _averagePoint;
    }

    public GeoPoint averagePoint() {
        return _averagePoint;
    }
*/
/*
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
    */
}
