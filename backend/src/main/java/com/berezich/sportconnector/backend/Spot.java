package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by berezkin on 25.06.2015.
 */
@Entity
public class Spot {
    @Id Long id;
    @Index Long regionId;
    String name;
    String address;
    Coordinates coords;
    String price;
    String workHours;
    String contact;
    String description;
    List<String> partnerLst;
    List<String> coachLst;
    List<Picture> pictureLst;
    public Spot(){
        pictureLst = new ArrayList<Picture>();
        coachLst = new ArrayList<String>();
        partnerLst = new ArrayList<String>();
    }
    public Spot(Spot anotherSpot)
    {
        id = anotherSpot.getId();
        regionId = anotherSpot.getId();
        name = anotherSpot.getName();
        address = anotherSpot.getAddress();
        Coordinates coords1;
        if((coords1 = anotherSpot.getCoords())!=null)
            coords = new Coordinates(coords1.getLat(),coords1.getLongt());
        else
            coords = null;
        price = anotherSpot.getPrice();
        workHours = anotherSpot.getWorkHours();
        contact = anotherSpot.getContact();
        description = anotherSpot.getDescription();
        List<String> longList;
        if((longList = anotherSpot.getPartnerLst())!=null)
            partnerLst = new ArrayList<String>(longList);
        else
            partnerLst = new ArrayList<String>();

        if((longList = anotherSpot.getCoachLst())!=null)
            coachLst = new ArrayList<String>(longList);
        else
            coachLst = new ArrayList<String>();
        List<Picture> pictureLst1;

        if((pictureLst1 = anotherSpot.getPictureLst())!=null)
            pictureLst = new ArrayList<Picture>(pictureLst1);
        else
            pictureLst = new ArrayList<Picture>();
    }


    public Long getId() {
        return id;
    }

    public Long getRegionId() {
        return regionId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public String getPrice() {
        return price;
    }

    public String getWorkHours() {
        return workHours;
    }

    public String getContact() {
        return contact;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPartnerLst() {

        if(partnerLst==null)
            partnerLst = new ArrayList<String>();
        return partnerLst;
    }

    public List<String> getCoachLst() {

        if(coachLst==null)
            coachLst = new ArrayList<String>();
        return coachLst;
    }

    public List<Picture> getPictureLst() {
        if(pictureLst==null)
            pictureLst = new ArrayList<Picture>();
        return pictureLst;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String _name) {
        this.name = _name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCoords(Coordinates coords) {
        this.coords = coords;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setWorkHours(String workHours) {
        this.workHours = workHours;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setDescription(String _description) {
        this.description = _description;
    }

    public void setPartnerLst(List<String> _partnerLst) {
        this.partnerLst = _partnerLst;
    }

    public void setCoachLst(List<String> _couchLst) {
        this.coachLst = _couchLst;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public void setPictureLst(List<Picture> pictureLst) {
        this.pictureLst = pictureLst;
    }

    @Override
    public String toString() {
        return String.format("id:%d name:%s",id,name);
    }
}
