package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Date;

/**
 * Created by berezkin on 25.06.2015.
 */
@Entity
public class UpdateSpotInfo {
    @Id
    Long _id;
    Long _regionId;
    Long _spotId;
    Date _udateDate;

    public UpdateSpotInfo() {}

    public UpdateSpotInfo(Long regionId, Long spotId, Date udateDate) {
        _id = null;
        _regionId = regionId;
        _spotId = spotId;
        _udateDate = udateDate;
    }

    public Long id() {
        return _id;
    }

    public Date udateDate() {
        return _udateDate;
    }

    public Long spotId() {
        return _spotId;
    }

    public void setSpotId(Long spotId) {
        this._spotId = spotId;
    }

    public void setUdateDate(Date udateDate) {
        this._udateDate = udateDate;
    }

    public Long regionId() {
        return _regionId;
    }

    public void setRegionId(Long regionId) {
        this._regionId = regionId;
    }
}
