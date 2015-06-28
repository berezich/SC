package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by berezkin on 25.06.2015.
 */
@Entity
public class Person {
    public enum TYPE{COACH,PARTNER};
    @Id
    private Long id = null;
    private String name;
    private String surname;
    private int age;
    /*for coaches*/
    private String price;
    private float rating;
    private String description;
    private TYPE type;
    private List<Picture> pictureLst;

    public Person() {
        pictureLst = new ArrayList<Picture>();
    }

    public Person(String name, String surname, int age) {
        this.name = name;
        this.surname = surname;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public int getAge() {
        return age;
    }

    public float getRating() {
        return rating;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public List<Picture> getPictureLst() {
        return pictureLst;
    }

    public TYPE getType() {
        return type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    protected void setType(TYPE _type) {
        this.type = _type;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPictureLst(List<Picture> pictureLst) {
        this.pictureLst = pictureLst;
    }
}
