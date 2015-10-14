package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Sashka on 15.10.2015.
 */
@Entity
public class ReqResetPass {
    @Id
    private String uuid;
    private Date registerDate;
    long personId;

    public ReqResetPass() {
    }

    public ReqResetPass(Long personId) {
        this.personId = personId;
        setUuid();
        registerDate = Calendar.getInstance().getTime();
    }


    public Date getRegisterDate() {
        return registerDate;
    }

    public long getPersonId() {
        return personId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public void setUuid() {
        this.uuid = UUID.randomUUID().toString();
    }
}
