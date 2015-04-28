package com.berezich.sportconnector.SportObjects;

import android.support.annotation.NonNull;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ru.yandex.yandexmapkit.utils.GeoPoint;

/**
 * Created by berezkin on 23.04.2015.
 */
public class Spot {
    private int _id;
    private String _tileName;
    private GeoPoint _geoCoord;
    private String _name;
    private List<Coach> _coaches;
    private List<Partner> _partners ;


    private boolean _favorite;

    public Spot(int _id, GeoPoint geoCoord, String tileName, String _name) {
        this._id = _id;
        this._geoCoord = geoCoord;
        this._name = _name;
        _tileName = tileName;
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
