package com.berezich.sportconnector.SportObjects;

import android.support.annotation.NonNull;


import com.berezich.sportconnector.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ru.yandex.yandexmapkit.utils.GeoPoint;

import static com.berezich.sportconnector.SportObjects.InfoTile.pluralPostfix;

/**
 * Created by berezkin on 23.04.2015.
 */
public class Spot {
    private int _id;
    private String _tileName;
    private GeoPoint _geoCoord;
    private String _name;
    private List<Coach> _coaches = new ArrayList<Coach>();
    private List<Partner> _partners = new ArrayList<Partner>() ;
    private boolean _favorite = false;

    public Spot(int _id, GeoPoint geoCoord, String tileName, String _name) {
        this._id = _id;
        this._geoCoord = geoCoord;
        this._name = _name;
        _tileName = tileName;
    }

    public List<InfoTile.Filters> getAppropriateFilters()
    {
        List<InfoTile.Filters> filters = new ArrayList<>();
        filters.add(InfoTile.Filters.Fxx1x);
        //filters OR
        boolean isPartenrs=false, isCoaches=false, isFavorite=false;
        if(_partners.size()>0)
        {
            filters.add(InfoTile.Filters.F1000);
            filters.add(InfoTile.Filters.F1100);
            filters.add(InfoTile.Filters.F1001);
            filters.add(InfoTile.Filters.F1101);
            isPartenrs=true;
        }
        if(_coaches.size()>0)
        {
            filters.add(InfoTile.Filters.F0100);
            filters.add(InfoTile.Filters.F0101);
            if(!isPartenrs)
            {
                filters.add(InfoTile.Filters.F1100);
                filters.add(InfoTile.Filters.F1101);
            }
            isCoaches = true;
        }
        if(_favorite)
        {
            filters.add(InfoTile.Filters.F0001);
            if(!isPartenrs)
                filters.add(InfoTile.Filters.F1001);
            if(!isCoaches)
                filters.add(InfoTile.Filters.F0101);
            if(!isCoaches && !isPartenrs)
                filters.add(InfoTile.Filters.F1101);
        }

        /*//filters AND
        if(spot.partners().size()>0)
        {
            filters.add(Filters.F1000);
            if(spot.coaches().size()>0) {
                filters.add(Filters.F1100);
                if(spot.favorite())
                    filters.add(Filters.F1101);
            }
            if(spot.favorite())
                filters.add(Filters.F1001);
        }
        else
        {
            if(spot.coaches().size()>0) {
                filters.add(Filters.F0100);
                if(spot.favorite())
                    filters.add(Filters.F0101);
            }
            if(spot.favorite())
                filters.add(Filters.F0001);
        }
        */
        return filters;
    }
    public int getMarkerImg(InfoTile.Filters filter)
    {
        if(filter == InfoTile.Filters.F0001||
                filter == InfoTile.Filters.F1001 && _partners.size()==0||
                filter == InfoTile.Filters.F0101 && _coaches.size()==0||
                (filter == InfoTile.Filters.F1101 || (filter == InfoTile.Filters.Fxx1x && _favorite)) && _coaches.size()==0&&_partners.size()==0)
            return  R.drawable.baloon_red;
        if(filter == InfoTile.Filters.F1000||
                filter == InfoTile.Filters.F1001 && !_favorite||
                filter == InfoTile.Filters.F1100 && _coaches.size()==0||
                (filter == InfoTile.Filters.F1101 || (filter == InfoTile.Filters.Fxx1x && _partners.size()>0)) && _coaches.size()==0&& !_favorite)
            return  R.drawable.baloon_purple;
        if(filter == InfoTile.Filters.F0100||
                filter == InfoTile.Filters.F0101 && !_favorite||
                filter == InfoTile.Filters.F1100 && _partners.size()==0||
                (filter == InfoTile.Filters.F1101 || (filter == InfoTile.Filters.Fxx1x && _coaches.size()>0)) && _partners.size()==0&& !_favorite)
            return  R.drawable.baloon_green;

        if(_partners.size()>0 && _favorite && (filter == InfoTile.Filters.F1001 || (filter == InfoTile.Filters.F1101 || filter == InfoTile.Filters.Fxx1x) && _coaches.size()==0))
            return  R.drawable.baloon_red_purple;
            //return  R.drawable.baloon_red;

        if(_partners.size()>0 && _coaches.size()>0 && (filter == InfoTile.Filters.F1100 || (filter == InfoTile.Filters.F1101 || filter == InfoTile.Filters.Fxx1x) && !_favorite))
            return  R.drawable.baloon_green_purple;
            //return  R.drawable.baloon_green;

        if(_coaches.size()>0 && _favorite && (filter == InfoTile.Filters.F0101 || (filter == InfoTile.Filters.F1101 || filter == InfoTile.Filters.Fxx1x) && _partners.size()==0))
            return  R.drawable.baloon_red_green;
            //return  R.drawable.baloon_green;

        if((filter == InfoTile.Filters.F1101 || filter == InfoTile.Filters.Fxx1x) && _partners.size()>0 && _coaches.size()>0 && _favorite )
            return  R.drawable.baloon_green_red_purple;
            //return  R.drawable.baloon_red;


        return  R.drawable.baloon_blue;
    }
    public String getDescription(InfoTile.Filters filter)
    {
        String description="";
        int num;
        if(filter == InfoTile.Filters.F1000 || filter == InfoTile.Filters.F1100 || filter == InfoTile.Filters.F1001 || filter == InfoTile.Filters.F1101 || filter == InfoTile.Filters.Fxx1x)
            if((num=_partners.size())>0)
                description+= "\n "+String.valueOf(num)+" - спарринг партнер"+pluralPostfix(num);
        if(filter == InfoTile.Filters.F0100 || filter == InfoTile.Filters.F1100 || filter == InfoTile.Filters.F0101 || filter == InfoTile.Filters.F1101 || filter == InfoTile.Filters.Fxx1x)
            if((num=_coaches.size())>0)
                description+= "\n "+String.valueOf(num)+" - тренер"+pluralPostfix(num);
        if(filter == InfoTile.Filters.F0001 || filter == InfoTile.Filters.F1001 || filter == InfoTile.Filters.F0101 || filter == InfoTile.Filters.F1101 || filter == InfoTile.Filters.Fxx1x)
            if(_favorite)
                description+= "\n мой спот";
        return  description;
    }
    public int id() {
        return _id;
    }

    public String tileName() {
        return _tileName;
    }

    public GeoPoint geoCoord() {
        return _geoCoord;
    }

    public String name() {
        return _name;
    }

    public List<Coach> coaches() {
        return _coaches;
    }

    public List<Partner> partners() {
        return _partners;
    }

    public boolean favorite() {
        return _favorite;
    }


    public void set_tileName(String _tileName) {
        this._tileName = _tileName;
    }

    public void set_geoCoord(GeoPoint _geoCoord) {
        this._geoCoord = _geoCoord;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public void set_favorite(boolean _favorite) {
        this._favorite = _favorite;
    }


}
