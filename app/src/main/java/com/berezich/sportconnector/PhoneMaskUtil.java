package com.berezich.sportconnector;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class PhoneMaskUtil {
    private static final String prefix = "+7";
    private static final String mask10 = "(###)###-##-##";

    public static String unmask(String s) {
        return s.replaceAll("^\\"+prefix+"*|[^0-9]*", "");
    }

    public static boolean validate(String phone){
        phone = unmask(phone);
        return phone.length()==10 || phone.isEmpty();
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

    private static String getDefaultMask() {
        return mask10;
    }
    private static String getMask(){
        String defaultMask = getDefaultMask();
        return prefix + defaultMask;
    }
    public static String setMask(String srcStr){
        String mascara = "";
        String mask = getMask();
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
