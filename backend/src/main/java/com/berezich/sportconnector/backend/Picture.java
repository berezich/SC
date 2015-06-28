package com.berezich.sportconnector.backend;

/**
 * Created by Sashka on 28.06.2015.
 */
public class Picture {
    String name;
    byte[] data;
    public Picture(){}

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
