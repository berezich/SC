package com.berezich.sportconnector;

import android.util.Log;

import com.berezich.sportconnector.backend.sportConnectorApi.model.AccountForConfirmation;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.google.api.client.util.Base64;
import com.google.api.client.util.DateTime;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    public static Person createPerson(AccountForConfirmation account)
    {
        Person person = new Person();
        person.setEmail(account.getEmail());
        person.setName(account.getName());
        person.setPass(account.getPass());
        person.setType(account.getType());
        return person;
    }
    public static String getDigest(String stringToEncrypt)
    {
        MessageDigest messageDigest;
        if(stringToEncrypt==null)
            return "";
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
        messageDigest.update(stringToEncrypt.getBytes());
        return bytesToHex(messageDigest.digest());
        //return encryptedString;
    }
    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean isSameStrValue(String v1, String v2)
    {
        if(v1==null && v2==null)
            return true;
        if(v1!=null && v2!=null) {
            if (!v1.equals(v2))
                return false;
        }
        else if(v1==null && !v2.equals("") || v2==null && !v1.equals(""))
            return false;
        return true;
    }
    public static String pluralPostfix(int num){
        if(num>=10 && num<=19)
            return "ов";
        int mod = num%10;
        switch (mod)
        {
            case 1:
                return "";
            case 2:
            case 3:
            case 4:
                return "a";
            default:
                return "ов";
        }
    }
    public static String adjPluralPostfix(int num){
        if(num == 11)
            return "ых";
        int mod = num%10;
        switch (mod)
        {
            case 1:
                return "ый";
            default:
                return "ых";
        }
    }
}
