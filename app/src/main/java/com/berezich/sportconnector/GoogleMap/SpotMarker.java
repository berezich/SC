package com.berezich.sportconnector.GoogleMap;

import android.content.Context;

import com.berezich.sportconnector.R;
import com.berezich.sportconnector.UsefulFunctions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class SpotMarker extends AbstractMarker {
    private String _name;
    private Long _id;
    private String _description;
    private int _numPartners=0;
    private int _numCoaches=0;
    private boolean _isFavorite=false;
    private Context ctx;

    public SpotMarker(Context context, Long id, String name,
                       double latitude, double longitude,int numPartners, int numCoaches,
                       boolean isFavorite) {
        super(latitude, longitude);
        ctx = context;
        set_id(id);
        setName(name);
        set_numPartners(numPartners);
        set_numCoaches(numCoaches);
        set_isFavorite(isFavorite);

        setMarker(new MarkerOptions()
                .position(new LatLng(latitude(), longitude()))
                .title(name()));
    }
    public List<GoogleMapFragment.FiltersX> getAppropriateFilters()
    {
        List<GoogleMapFragment.FiltersX> filters = new ArrayList<>();
        filters.add(GoogleMapFragment.FiltersX.Fxx1x);
        //filters OR
        boolean isPartenrs=false, isCoaches=false;
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

        return filters;
    }

    public void setSpotIcon(GoogleMapFragment.FiltersX filter)
    {
            getMarker().icon(BitmapDescriptorFactory.fromResource(getMarkerImg(filter)));
    }
    public int getMarkerImg(GoogleMapFragment.FiltersX filter)
    {
        if(filter == GoogleMapFragment.FiltersX.F0001
                ||filter == GoogleMapFragment.FiltersX.F1001 && _numPartners==0
                ||filter == GoogleMapFragment.FiltersX.F0101 && _numCoaches==0
                ||(filter == GoogleMapFragment.FiltersX.F1101
                || (filter == GoogleMapFragment.FiltersX.Fxx1x && _isFavorite))
                && _numCoaches==0&&_numPartners==0)
            return  R.drawable.baloon_red;
        if(filter == GoogleMapFragment.FiltersX.F1000
                ||filter == GoogleMapFragment.FiltersX.F1001 && !_isFavorite
                ||filter == GoogleMapFragment.FiltersX.F1100 && _numCoaches==0
                ||(filter == GoogleMapFragment.FiltersX.F1101
                || (filter == GoogleMapFragment.FiltersX.Fxx1x && _numPartners>0))
                && _numCoaches==0&& !_isFavorite)
            return  R.drawable.baloon_purple;
        if(filter == GoogleMapFragment.FiltersX.F0100
                ||filter == GoogleMapFragment.FiltersX.F0101 && !_isFavorite
                ||filter == GoogleMapFragment.FiltersX.F1100 && _numPartners==0
                ||(filter == GoogleMapFragment.FiltersX.F1101
                ||(filter == GoogleMapFragment.FiltersX.Fxx1x && _numCoaches>0))
                && _numPartners==0&& !_isFavorite)
            return  R.drawable.baloon_green;

        if(_numPartners>0 && _isFavorite && (filter == GoogleMapFragment.FiltersX.F1001
                ||(filter == GoogleMapFragment.FiltersX.F1101
                || filter == GoogleMapFragment.FiltersX.Fxx1x)
                && _numCoaches==0))
            return  R.drawable.baloon_red_purple;

        if(_numPartners>0 && _numCoaches>0 && (filter == GoogleMapFragment.FiltersX.F1100
                ||(filter == GoogleMapFragment.FiltersX.F1101
                || filter == GoogleMapFragment.FiltersX.Fxx1x)
                && !_isFavorite))
            return  R.drawable.baloon_green_purple;

        if(_numCoaches>0 && _isFavorite && (filter == GoogleMapFragment.FiltersX.F0101 ||
                (filter == GoogleMapFragment.FiltersX.F1101
                || filter == GoogleMapFragment.FiltersX.Fxx1x)
                && _numPartners==0))
            return  R.drawable.baloon_red_green;

        if((filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x)
                && _numPartners>0 && _numCoaches>0 && _isFavorite )
            return  R.drawable.baloon_green_red_purple;


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

    public String description() {
        return _description;
    }

    public void setDescription(GoogleMapFragment.FiltersX filter)
    {
        String description=_name;
        int num;
        if(filter == GoogleMapFragment.FiltersX.F1000
                || filter == GoogleMapFragment.FiltersX.F1100
                || filter == GoogleMapFragment.FiltersX.F1001
                || filter == GoogleMapFragment.FiltersX.F1101
                || filter == GoogleMapFragment.FiltersX.Fxx1x)
            if((num=_numPartners)>0)
                description+= " \n"+String.valueOf(num)+" - "+
                        ctx.getString(R.string.gmap_desc_partner)+ UsefulFunctions.pluralPostfix(num);
        if(filter == GoogleMapFragment.FiltersX.F0100
                || filter == GoogleMapFragment.FiltersX.F1100
                || filter == GoogleMapFragment.FiltersX.F0101
                || filter == GoogleMapFragment.FiltersX.F1101
                || filter == GoogleMapFragment.FiltersX.Fxx1x)
            if((num=_numCoaches)>0)
                description+= " \n"+String.valueOf(num)+" - "
                        +ctx.getString(R.string.gmap_desc_coach)+UsefulFunctions.pluralPostfix(num);
        if(filter == GoogleMapFragment.FiltersX.F0001
                || filter == GoogleMapFragment.FiltersX.F1001
                || filter == GoogleMapFragment.FiltersX.F0101
                || filter == GoogleMapFragment.FiltersX.F1101
                || filter == GoogleMapFragment.FiltersX.Fxx1x)
            if(_isFavorite)
                description+= " \n"+ctx.getString(R.string.gmap_desc_favourite);
        _description = description;
    }
    
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

    public Long id() {
        return _id;
    }

    public void set_id(Long id) {
        this._id = id;
    }
}
