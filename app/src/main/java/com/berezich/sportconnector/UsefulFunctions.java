package com.berezich.sportconnector;

import com.google.api.client.util.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sashka on 26.07.2015.
 */
public class UsefulFunctions {
    public static int calcPersonAge(DateTime birthday) {
        Calendar calendar2 = Calendar.getInstance(),calendar1 = Calendar.getInstance();
        if(birthday==null)
            return -1;

        Date date1 = new Date(birthday.getValue());
        calendar1.setTime(date1);
        int age = calendar2.get(Calendar.YEAR) - calendar1.get(Calendar.YEAR);
        if(age>0)
            if(calendar2.get(Calendar.MONTH)<calendar1.get(Calendar.MONTH))
                age--;
            else if(calendar2.get(Calendar.MONTH)==calendar1.get(Calendar.MONTH)&&
                    calendar2.get(Calendar.DAY_OF_MONTH)<calendar1.get(Calendar.DAY_OF_MONTH))
                age--;

        return age;
    }
}
