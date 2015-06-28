package com.berezich.sportconnector.backend;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.Date;

/**
 * Created by berezkin on 25.06.2015.
 */
@Entity
public class UpdateSpotInfo {
    @Id Long id;   //spotId
    @Index Long regionId;
    @Index Date updateDate;
    @Load Ref<Spot> spot;

    public UpdateSpotInfo() {}

    public UpdateSpotInfo(Long spotId ,Long regionId, Date updateDate, Spot spot) {
        this.id = spotId;
        this.regionId = regionId;
        this.updateDate = updateDate;
        setSpot(spot);
    }

    public Long getId() {
        return id;
    }

    public Long getRegionId() {
        return regionId;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public Spot getSpot() {
        return spot.get();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public void setSpot(Spot spot) {
        this.spot = Ref.create(spot);
    }
}
