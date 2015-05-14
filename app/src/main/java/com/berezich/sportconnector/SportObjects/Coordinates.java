package com.berezich.sportconnector.SportObjects;

/**
 * Created by berezkin on 14.05.2015.
 */
public class Coordinates {
    float lat;
    float longt;

    public Coordinates(float lat, float longt) {
        this.lat = lat;
        this.longt = longt;
    }
    public Coordinates(double lat, double longt) {
        this.lat = (float)lat;
        this.longt = (float) longt;
    }

    public float lat() {
        return lat;
    }

    public float longt() {
        return longt;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLongt(float longt) {
        this.longt = longt;
    }
}
