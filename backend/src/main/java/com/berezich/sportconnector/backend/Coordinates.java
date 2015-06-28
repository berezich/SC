package com.berezich.sportconnector.backend;

/**
 * Created by berezkin on 14.05.2015.
 */
public class Coordinates {
    private float lat;
    private float longt;

    public Coordinates() {
    }

    public Coordinates(float lat, float longt) {
        this.lat = lat;
        this.longt = longt;
    }
    public Coordinates(double lat, double longt) {
        this.lat = (float)lat;
        this.longt = (float) longt;
    }

    public float getLat() {
        return lat;
    }

    public float getLongt() {
        return longt;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLongt(float longt) {
        this.longt = longt;
    }
}
