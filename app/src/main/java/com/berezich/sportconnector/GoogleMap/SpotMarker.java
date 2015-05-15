package com.berezich.sportconnector.GoogleMap;

import android.content.res.Resources;

import com.berezich.sportconnector.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by berezkin on 14.05.2015.
 */
public class SpotMarker extends AbstractMarker {
    private static BitmapDescriptor spotIcon = null;

    private String _name;
    private int _id;
    private int _numPartners=0;
    private int _numCoaches=0;
    private boolean _isFavorite=false;

    public SpotMarker(String name,
                       double latitude, double longitude,int numPartners, int numCoaches,
                       boolean isFavorite) {
        super(latitude, longitude);
        setName(name);
        set_numPartners(numPartners);
        set_numCoaches(numCoaches);
        set_isFavorite(isFavorite);

        setMarker(new MarkerOptions()
                .position(new LatLng(latitude(), longitude()))
                .title(name()));
                //.icon(spotIcon));
    }
    public List<GoogleMapFragment.FiltersX> getAppropriateFilters()
    {
        List<GoogleMapFragment.FiltersX> filters = new ArrayList<>();
        filters.add(GoogleMapFragment.FiltersX.Fxx1x);
        //filters OR
        boolean isPartenrs=false, isCoaches=false, isFavorite=false;
        if(_numPartners>0)
        {
            filters.add(GoogleMapFragment.FiltersX.F1000);
            filters.add(GoogleMapFragment.FiltersX.F1100);
            filters.add(GoogleMapFragment.FiltersX.F1001);
            filters.add(GoogleMapFragment.FiltersX.F1101);
            isPartenrs=true;
        }
        if(_numCoaches>0)
        {
            filters.add(GoogleMapFragment.FiltersX.F0100);
            filters.add(GoogleMapFragment.FiltersX.F0101);
            if(!isPartenrs)
            {
                filters.add(GoogleMapFragment.FiltersX.F1100);
                filters.add(GoogleMapFragment.FiltersX.F1101);
            }
            isCoaches = true;
        }
        if(_isFavorite)
        {
            filters.add(GoogleMapFragment.FiltersX.F0001);
            if(!isPartenrs)
                filters.add(GoogleMapFragment.FiltersX.F1001);
            if(!isCoaches)
                filters.add(GoogleMapFragment.FiltersX.F0101);
            if(!isCoaches && !isPartenrs)
                filters.add(GoogleMapFragment.FiltersX.F1101);
        }

        /*//filters AND
        if(spot.partners().size()>0)
        {
            filters.add(FiltersX.F1000);
            if(spot.coaches().size()>0) {
                filters.add(FiltersX.F1100);
                if(spot.favorite())
                    filters.add(FiltersX.F1101);
            }
            if(spot.favorite())
                filters.add(FiltersX.F1001);
        }
        else
        {
            if(spot.coaches().size()>0) {
                filters.add(FiltersX.F0100);
                if(spot.favorite())
                    filters.add(FiltersX.F0101);
            }
            if(spot.favorite())
                filters.add(FiltersX.F0001);
        }
        */
        return filters;
    }
    public BitmapDescriptor getBitmap(GoogleMapFragment.FiltersX filter)
    {
        if(isAppropriate(filter)) {
            return BitmapDescriptorFactory.fromResource(getMarkerImg(filter));
        }
        return null;

    }
    public int getMarkerImg(GoogleMapFragment.FiltersX filter)
    {
        if(filter == GoogleMapFragment.FiltersX.F0001||
                filter == GoogleMapFragment.FiltersX.F1001 && _numPartners==0||
                filter == GoogleMapFragment.FiltersX.F0101 && _numCoaches==0||
                (filter == GoogleMapFragment.FiltersX.F1101 || (filter == GoogleMapFragment.FiltersX.Fxx1x && _isFavorite)) && _numCoaches==0&&_numPartners==0)
            return  R.drawable.baloon_red;
        if(filter == GoogleMapFragment.FiltersX.F1000||
                filter == GoogleMapFragment.FiltersX.F1001 && !_isFavorite||
                filter == GoogleMapFragment.FiltersX.F1100 && _numCoaches==0||
                (filter == GoogleMapFragment.FiltersX.F1101 || (filter == GoogleMapFragment.FiltersX.Fxx1x && _numPartners>0)) && _numCoaches==0&& !_isFavorite)
            return  R.drawable.baloon_purple;
        if(filter == GoogleMapFragment.FiltersX.F0100||
                filter == GoogleMapFragment.FiltersX.F0101 && !_isFavorite||
                filter == GoogleMapFragment.FiltersX.F1100 && _numPartners==0||
                (filter == GoogleMapFragment.FiltersX.F1101 || (filter == GoogleMapFragment.FiltersX.Fxx1x && _numCoaches>0)) && _numPartners==0&& !_isFavorite)
            return  R.drawable.baloon_green;

        if(_numPartners>0 && _isFavorite && (filter == GoogleMapFragment.FiltersX.F1001 ||
                (filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && _numCoaches==0))
            return  R.drawable.baloon_red_purple;
        //return  R.drawable.baloon_red;

        if(_numPartners>0 && _numCoaches>0 && (filter == GoogleMapFragment.FiltersX.F1100 ||
                (filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && !_isFavorite))
            return  R.drawable.baloon_green_purple;
        //return  R.drawable.baloon_green;

        if(_numCoaches>0 && _isFavorite && (filter == GoogleMapFragment.FiltersX.F0101 ||
                (filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && _numPartners==0))
            return  R.drawable.baloon_red_green;
        //return  R.drawable.baloon_green;

        if((filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && _numPartners>0 && _numCoaches>0 && _isFavorite )
            return  R.drawable.baloon_green_red_purple;
        //return  R.drawable.baloon_red;


        return  R.drawable.baloon_blue;
    }
    public boolean isAppropriate(GoogleMapFragment.FiltersX filter)
    {

        List<GoogleMapFragment.FiltersX> filters = getAppropriateFilters();
        for(GoogleMapFragment.FiltersX filterItem:filters)
            if(filter == filterItem)
                return true;
        return  false;
    }
    //for group of markers
    /*
    Integer getDrawableMarker(GoogleMapFragment.FiltersX filter)
    {
        if(filter == GoogleMapFragment.FiltersX.F0001||
                filter == GoogleMapFragment.FiltersX.F1001 && _childSpots_1000.size()==0||
                filter == GoogleMapFragment.FiltersX.F0101 && _childSpots_0100.size()==0||
                filter == GoogleMapFragment.FiltersX.F1101 && _childSpots_0100.size()==0&&_childSpots_1000.size()==0)
            return  R.drawable.baloon_red;
        if(filter == GoogleMapFragment.FiltersX.F1000||
                filter == GoogleMapFragment.FiltersX.F1001 && _childSpots_0001.size()==0||
                filter == GoogleMapFragment.FiltersX.F1100 && _childSpots_0100.size()==0||
                filter == GoogleMapFragment.FiltersX.F1101 && _childSpots_0100.size()==0&& _childSpots_0001.size()==0)
            return  R.drawable.baloon_purple;
        if(filter == GoogleMapFragment.FiltersX.F0100||
                filter == GoogleMapFragment.FiltersX.F0101 && _childSpots_0001.size()==0||
                filter == GoogleMapFragment.FiltersX.F1100 && _childSpots_1000.size()==0||
                filter == GoogleMapFragment.FiltersX.F1101 && _childSpots_1000.size()==0&& _childSpots_0001.size()==0)
            return  R.drawable.baloon_green;

        if(_childSpots_1000.size()>0 && _childSpots_0001.size()>0 && (filter == GoogleMapFragment.FiltersX.F1001 || (filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && _childSpots_0100.size()==0))
            return  R.drawable.baloon_red_purple;
        //return  R.drawable.baloon_red;

        if(_childSpots_1000.size()>0 && _childSpots_0100.size()>0 && (filter == GoogleMapFragment.FiltersX.F1100 || (filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && _childSpots_0001.size()==0))
            return  R.drawable.baloon_green_purple;
        //return  R.drawable.baloon_green;

        if(_childSpots_0100.size()>0 && _childSpots_0001.size()>0 && (filter == GoogleMapFragment.FiltersX.F0101 || (filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && _childSpots_1000.size()==0))
            return  R.drawable.baloon_red_green;
        //return  R.drawable.baloon_green;

        if((filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && _childSpots_1000.size()>0 && _childSpots_0100.size()>0 && _childSpots_0001.size()>0 )
            return  R.drawable.baloon_green_red_purple;
        //return  R.drawable.baloon_red;


        return  R.drawable.baloon_blue;
    }
    */
    
    public String toString() {
        return "Trade place: " +  name();
    }

    public String name() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public void set_numPartners(int _numPartners) {
        this._numPartners = _numPartners;
    }

    public void set_numCoaches(int _numCoaches) {
        this._numCoaches = _numCoaches;
    }

    public void set_isFavorite(boolean _isFavorite) {
        this._isFavorite = _isFavorite;
    }

    public int numPartners() {
        return _numPartners;
    }

    public int numCoaches() {
        return _numCoaches;
    }

    public boolean isFavorite() {
        return _isFavorite;
    }

    public int id() {
        return _id;
    }
}
