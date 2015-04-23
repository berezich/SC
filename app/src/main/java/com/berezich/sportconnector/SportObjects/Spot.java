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
    int _id;
    private GeoPoint _wCoord;
    private String _name;
    private List<Coach> _coaches;
    private List<Partner> _partners ;


    private boolean _favorite;

    public Spot(int _id, GeoPoint wCoord, String _name) {
        this._id = _id;
        this._wCoord = wCoord;
        this._name = _name;

    }
}
