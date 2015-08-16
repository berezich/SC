package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Date;

/**
 * Created by Sashka on 15.08.2015.
 */
@Entity
public class AccountForConfirmation {
    @Id
    private String id = null;
    private String pass;
    private String name;
    private Person.TYPE type;
    private Date registerDate;

    public AccountForConfirmation() {
    }

    public AccountForConfirmation(String id, String pass, String name, Person.TYPE type,Date registerDate) {
        this.id = id;
        this.pass = pass;
        this.name = name;
        this.type = type;
        this.registerDate = registerDate;
    }

    public String getId() {
        return id;
    }

    public String getPass() {
        return pass;
    }

    public String getName() {
        return name;
    }

    public Person.TYPE getType() {
        return type;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Person.TYPE type) {
        this.type = type;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }
}
