package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by berezkin on 25.06.2015.
 */
@Entity
public class Spot {
    @Id
    Long _id;
    Long _regionId;
    String _name;
    String _description;
    List<Long> _partnerLst = new ArrayList<Long>();
    List<Long> _couchLst = new ArrayList<Long>();
    boolean _favorite = false;
    public Spot(){}

    public Long id() {
        return _id;
    }

    public String name() {
        return _name;
    }

    public String description() {
        return _description;
    }

    public List<Long> partnerLst() {
        return _partnerLst;
    }

    public List<Long> couchLst() {
        return _couchLst;
    }

    public boolean isFavorite() {
        return _favorite;
    }

    private void setId(Long id) {
        this._id = id;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public void setDescription(String _description) {
        this._description = _description;
    }

    public void setPartnerLst(List<Long> _partnerLst) {
        this._partnerLst = _partnerLst;
    }

    public void setCouchLst(List<Long> _couchLst) {
        this._couchLst = _couchLst;
    }

    public void setFavorite(boolean _favorite) {
        this._favorite = _favorite;
    }

    public Long regionId() {
        return _regionId;
    }

    public void setRegionId(Long regionId) {
        this._regionId = regionId;
    }
}
