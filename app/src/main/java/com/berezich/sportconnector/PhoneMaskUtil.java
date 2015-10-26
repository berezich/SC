package com.berezich.sportconnector;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by Sashka on 25.10.2015.
 */
public class PhoneMaskUtil {
    private static final String prefix = "+7";
    private static final String mask10 = "(###)###-##-##";
    //private static final String mask11 = "(####)###-##-##";
    private static final String mask8 = "####-####";
    private static final String mask9 = "#####-####";

    public static String unmask(String s) {
        return s.replaceAll("^\\"+prefix+"*|[^0-9]*", "");
    }

    public static boolean validate(String phone){
        phone = unmask(phone);
        if(phone.length()==10 || phone.isEmpty())
            return true;
        return false;
    }

    public static TextWatcher insert(final EditText editText) {
        return new TextWatcher() {
            boolean isUpdating;
            String old = "";

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = PhoneMaskUtil.unmask(s.toString());

                if (isUpdating) {
                    old = str;
                    isUpdating = false;
                    return;
                }

                isUpdating = true;
                String mascara = setMask(str);
                editText.setText(mascara);
                editText.setSelection(mascara.length());
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        };
    }

    private static String getDefaultMask(String str) {
        String defaultMask = mask10;
        /*if (str.length() > 11){
            defaultMask = mask11;
        }*/
        return defaultMask;
    }
    private static String getMask(String str){
        String mask;
        String defaultMask = getDefaultMask(str);
        switch (str.length()) {
            /*case 11:
                mask =prefix + mask11;
                break;
            */
            default:
                mask = prefix + defaultMask;
                break;
        }
        return mask;
    }
    public static String setMask(String srcStr){
        String mascara = "";
        String mask = getMask(srcStr);
        int i = 0;
        for (char m : mask.toCharArray()) {
            if (m != '#'  && srcStr.length() != i) {
                mascara += m;
                continue;
            }

            try {
                mascara += srcStr.charAt(i);
            } catch (Exception e) {
                break;
            }
            i++;
        }
        return mascara;
    }
}
