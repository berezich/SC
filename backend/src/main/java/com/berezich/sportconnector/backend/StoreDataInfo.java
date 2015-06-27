package com.berezich.sportconnector.backend;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Date;

/**
 * Created by berezkin on 25.06.2015.
 */
@Entity
public class StoreDataInfo {
    @Id
    Long _id;
    String _regionName;
    Date _lastSpotUpdate;
    Date _releaseDate;
    String _version;
    public StoreDataInfo(){}

    public Long get_id() {
        return _id;
    }

    public String get_regionName() {
        return _regionName;
    }

    public Date get_lastSpotUpdate() {
        return _lastSpotUpdate;
    }

    public Date get_releaseDate() {
        return _releaseDate;
    }

    public String get_version() {
        return _version;
    }

    private void set_id(Long _id) {
        this._id = _id;
    }

    public void set_regionName(String _regionName) {
        this._regionName = _regionName;
    }

    public void set_lastSpotUpdate(Date _lastSpotUpdate) {
        this._lastSpotUpdate = _lastSpotUpdate;
    }

    public void set_releaseDate(Date _releaseDate) {
        this._releaseDate = _releaseDate;
    }

    public void set_version(String _version) {
        this._version = _version;
    }
}
