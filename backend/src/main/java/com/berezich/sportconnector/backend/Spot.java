package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

/**
 * Created by berezkin on 25.06.2015.
 */
@Entity
public class Spot {
    private static final Logger logger = getLogger(Spot.class.getName());
    enum TYPE{OPEN,CLOSED,MIX,NONE}
    @Id Long id;
    @Index Long regionId;
    String name;
    String address;
    Coordinates coords;
    String price;
    String workHours;
    String contact;
    String site;
    float rating;
    String description;
    TYPE type;
    int openPlayFieldNum;
    int closedPlayFieldNum;
    List<Long> partnerLst;
    List<Long> coachLst;
    List<Picture> pictureLst;
    public Spot(){
        pictureLst = new ArrayList<Picture>();
        coachLst = new ArrayList<Long>();
        partnerLst = new ArrayList<Long>();
    }
    public Spot(String spotStr)
    {
        int i=0;
        String[] spotProps;
        if(spotStr!=null && !spotStr.equals("")){
            spotProps = spotStr.split("\t",13);
            if(spotProps!=null && spotProps.length==13){
                name = spotProps[i++].trim();
                switch (spotProps[i++].trim()){
                    case "Москва":
                        regionId = new Long(1);
                        break;
                    default:
                        regionId = new Long(1);
                }
                address = spotProps[i++].trim();
                contact = spotProps[i++].trim();
                site = spotProps[i++].trim();
                float longtitude = Float.parseFloat(spotProps[i++].trim());
                float latitude = Float.parseFloat(spotProps[i++].trim());
                coords = new Coordinates(latitude,longtitude);
                description = spotProps[i++].trim();
                int type = Integer.parseInt(spotProps[i++].trim());
                switch (type){
                    case 1:
                        this.type = TYPE.MIX;
                        break;
                    case 2:
                        this.type = TYPE.OPEN;
                        break;
                    case 3:
                        this.type = TYPE.CLOSED;
                        break;
                    default:
                        this.type = TYPE.NONE;
                }
                openPlayFieldNum = Integer.parseInt(spotProps[i++].trim());
                closedPlayFieldNum = Integer.parseInt(spotProps[i++].trim());
                price = spotProps[i].trim();

            }
            else if(spotProps!=null)
                logger.info(String.format("err spotPropertiesNum = %d spotStr = %s",spotProps.length,spotStr));
        }
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
        site = anotherSpot.getSite();
        description = anotherSpot.getDescription();
        rating = anotherSpot.getRating();
        type = anotherSpot.getType();
        openPlayFieldNum = anotherSpot.getOpenPlayFieldNum();
        closedPlayFieldNum = anotherSpot.getClosedPlayFieldNum();
        List<Long> longList;
        if((longList = anotherSpot.getPartnerLst())!=null)
            partnerLst = new ArrayList<Long>(longList);
        else
            partnerLst = new ArrayList<Long>();

        if((longList = anotherSpot.getCoachLst())!=null)
            coachLst = new ArrayList<Long>(longList);
        else
            coachLst = new ArrayList<Long>();
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

    public String getSite() {
        return site;
    }

    public String getDescription() {
        return description;
    }

    public float getRating() {
        return rating;
    }

    public TYPE getType() {
        return type;
    }

    public int getOpenPlayFieldNum() {
        return openPlayFieldNum;
    }

    public int getClosedPlayFieldNum() {
        return closedPlayFieldNum;
    }

    public List<Long> getPartnerLst() {

        if(partnerLst==null)
            partnerLst = new ArrayList<Long>();
        return partnerLst;
    }

    public List<Long> getCoachLst() {

        if(coachLst==null)
            coachLst = new ArrayList<Long>();
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

    public void setSite(String site) {
        this.site = site;
    }

    public void setDescription(String _description) {
        this.description = _description;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public void setOpenPlayFieldNum(int openPlayFieldNum) {
        this.openPlayFieldNum = openPlayFieldNum;
    }

    public void setClosedPlayFieldNum(int closedPlayFieldNum) {
        this.closedPlayFieldNum = closedPlayFieldNum;
    }

    public void setPartnerLst(List<Long> _partnerLst) {
        this.partnerLst = _partnerLst;
    }

    public void setCoachLst(List<Long> _couchLst) {
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
