package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Date;

/**
 * Created by berezkin on 27.08.2015.
 */
@Entity
public class ReqChangeEmail {
    @Id
    private String email;
    private String newEmail;
    private Date registerDate;
    long personId;

    public ReqChangeEmail() {
    }

    public ReqChangeEmail( String email, String newEmail, long personId,Date registerDate) {
        this.personId = personId;
        this.email = email;
        this.newEmail = newEmail;
        this.registerDate = registerDate;
    }

    public long getPersonId() {
        return personId;
    }

    public String getEmail() {
        return email;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }
}
