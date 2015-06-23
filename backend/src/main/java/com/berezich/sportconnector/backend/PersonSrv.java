package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
/**
 * Created by berezkin on 22.06.2015.
 */

@Entity
public class PersonSrv {
    @Id
    Long id;
    String name;
    int age;

    public PersonSrv() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    public String getInfo()
    {
        return String.format("id = %d name = %s age = %d",id,name,age);
    }
}