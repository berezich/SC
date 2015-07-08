package com.berezich.sportconnector.SportObjects;

/**
 * Created by berezkin on 23.04.2015.
 */
public class Person1 {
    public enum TYPE{COACH,PARTNER};

    private int _id;
    private String _name;
    private String _surname;
    private int _age;
    private int _rating;
    private TYPE _type;

    public Person1(int id, String name, String surname, int age) {
        _id = id;
        _name = name;
        _surname = surname;
        _age = age;
    }

    public int id() {
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

    public void set_name(String _name) {
        this._name = _name;
    }

    public void set_surname(String _surname) {
        this._surname = _surname;
    }

    public void set_age(int _age) {
        this._age = _age;
    }

    public void set_rating(int _rating) {
        this._rating = _rating;
    }

    protected void set_type(TYPE _type) {
        this._type = _type;
    }

}
