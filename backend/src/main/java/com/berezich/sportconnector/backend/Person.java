package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by berezkin on 25.06.2015.
 */
@Entity
public class Person {
    public enum TYPE{COACH,PARTNER};
    @Id
    private Long _id;
    private String _name;
    private String _surname;
    private int _age;
    private int _rating;
    private TYPE _type;

    public Person(Long id, String name, String surname, int age) {
        _id = id;
        _name = name;
        _surname = surname;
        _age = age;
    }

    public Long id() {
        return _id;
    }

    public String name() {
        return _name;
    }

    public String surname() {
        return _surname;
    }

    public int age() {
        return _age;
    }

    public int rating() {
        return _rating;
    }

    public TYPE type() {
        return _type;
    }

    public void setId(Long id) {
        this._id = id;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public void setSurname(String _surname) {
        this._surname = _surname;
    }

    public void setAge(int _age) {
        this._age = _age;
    }

    public void setRating(int _rating) {
        this._rating = _rating;
    }

    protected void setType(TYPE _type) {
        this._type = _type;
    }

}
