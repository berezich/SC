package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Sashka on 15.08.2015.
 */
@Entity
public class AccountForConfirmation {
    @Id
    private String email;
    private String pass;
    private String name;
    private Person.TYPE type;
    private Date registerDate;
    private String uuid;

    public AccountForConfirmation() {
    }

    public AccountForConfirmation(String email, String pass, String name, Person.TYPE type,Date registerDate) {
        this.email = email;
        this.pass = pass;
        this.name = name;
        this.type = type;
        this.registerDate = registerDate;
        this.uuid = UUID.randomUUID().toString();
    }

    public String getEmail() {
        return email;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid() {
        this.uuid = UUID.randomUUID().toString();
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String  email)
    {
        this.email = email;
    }

    public void setType(Person.TYPE type) {
        this.type = type;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }
}
