package com.berezich.sportconnector.backend;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Date;

/**
 * Created by berezkin on 25.06.2015.
 */
@Entity
public class RegionInfo {
    @Id
    Long id;
    String regionName;
    Date lastSpotUpdate;
    Date releaseDate;
    String version;
    public RegionInfo(){}

    public Long getId() {
        return id;
    }

    public String getRegionName() {
        return regionName;
    }

    public Date getLastSpotUpdate() {
        return lastSpotUpdate;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getVersion() {
        return version;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setLastSpotUpdate(Date lastSpotUpdate) {
        this.lastSpotUpdate = lastSpotUpdate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
