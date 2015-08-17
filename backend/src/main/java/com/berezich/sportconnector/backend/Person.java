package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by berezkin on 25.06.2015.
 */
@Entity
public class Person {
    public enum TYPE{COACH,PARTNER};
    public enum SEX{MALE,FEMALE};
    @Id
    private String id = null;
    private String pass;
    private String name;
    private String surname;
    private Date birthday;
    private String email;
    private String phone;
    private SEX sex;
    /*for coaches*/
    private String price;
    private float rating;
    private String description;
    private TYPE type;
    private List<Picture> pictureLst;
    private List<Long> favoriteSpotIdLst;
    private List<String> myFriends;

    public Person() {
        pictureLst = new ArrayList<Picture>();
        favoriteSpotIdLst = new ArrayList<Long>();
    }

    public Person(String name, String surname, Date birthday) {
        this.name = name;
        this.surname = surname;
        this.birthday = birthday;
    }
    public Person(AccountForConfirmation account){
        this.id = account.getId();
        this.email = account.getId();
        this.name = account.getName();
        this.pass = account.getPass();
        this.type = account.getType();
    }
    public Person(Person anotherPerson){
        id = anotherPerson.getId();
        pass = anotherPerson.getPass();
        name = anotherPerson.getName();
        surname = anotherPerson.getSurname();
        birthday = anotherPerson.getBirthday();
        email = anotherPerson.getEmail();
        phone = anotherPerson.getPhone();
        price = anotherPerson.getPrice();
        rating = anotherPerson.getRating();
        description = anotherPerson.getDescription();
        type = anotherPerson.getType();
        if(anotherPerson.getPictureLst()!=null)
            pictureLst = new ArrayList<>(anotherPerson.getPictureLst());
        if(anotherPerson.getFavoriteSpotIdLst()!=null)
            favoriteSpotIdLst = new ArrayList<>(anotherPerson.getFavoriteSpotIdLst());
        if(anotherPerson.getMyFriends()!=null)
            myFriends = new ArrayList<>(anotherPerson.getMyFriends());
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

    public String getSurname() {
        return surname;
    }

    public Date getBirthday() {
        return birthday;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public float getRating() {
        return rating;
    }

    public SEX getSex() {
        return sex;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public List<Picture> getPictureLst() {
        if(pictureLst==null)
            pictureLst = new ArrayList<Picture>();
        return pictureLst;
    }

    public TYPE getType() {
        return type;
    }

    public List<Long> getFavoriteSpotIdLst() {
        if(favoriteSpotIdLst==null)
            favoriteSpotIdLst = new ArrayList<Long>();
        return favoriteSpotIdLst;
    }

    public List<String> getMyFriends() {
        if(myFriends==null)
            myFriends = new ArrayList<String>();
        return myFriends;
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

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setSex(SEX sex) {
        this.sex = sex;
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

    public void setFavoriteSpotIdLst(List<Long> favoriteSpotIdLst) {
        this.favoriteSpotIdLst = favoriteSpotIdLst;
    }

    public void setMyFriends(List<String> myFriends) {
        this.myFriends = myFriends;
    }
}
