package com.berezich.sportconnector;

import android.util.Log;

import com.google.api.client.util.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public static DateTime parseDateTime(String dtStr)
    {
        DateFormat df = new SimpleDateFormat("dd.mm.yyyy");
        DateTime dtBirthday = null;
        try {
            Date dBirthday = df.parse(dtStr);
            dtBirthday = new DateTime(dBirthday);
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            return dtBirthday;
        }
    }
}
