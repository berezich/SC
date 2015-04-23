package com.berezich.sportconnector.SportObjects;

/**
 * Created by berezkin on 23.04.2015.
 */
public class Partner extends  Person {
    Partner(int id, String name, String surname, int age)
    {
        super(id, name, surname, age);
        set_type(TYPE.PARTNER);
    }
}
