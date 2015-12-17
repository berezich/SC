package com.berezich.sportconnector;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

    /*private static int getPositionWithoutMask(int indexInMask){
        int cnt=-1;
        String mask = prefix+mask10;
        for(int i=0; i<indexInMask && i<mask.length(); i++)
            if(mask.charAt(i)=='#')
                cnt++;
        return cnt;
    }*/

    public static TextWatcher insert(final EditText editText) {
        return new TextWatcher() {
            boolean isUpdating;
            String old = "";

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String str = PhoneMaskUtil.unmask(s.toString());

                    if (isUpdating) {
                        old = s.toString();
                        isUpdating = false;
                        return;
                    }

                    isUpdating = true;

                    int iFirstDigit = (prefix+mask10).indexOf('#');
                    if(old!=null && !old.isEmpty() && start<iFirstDigit) {
                        editText.setText(old);
                        if(iFirstDigit <= old.length())
                            editText.setSelection(iFirstDigit);
                        return;
                    }
                    String mascara = setMask(str);
                    editText.setText(mascara);
                    Log.d("TEXT_WATCHER", String.format("s=%s mask=%s slen=%d start=%d before=%d count=%d", s.toString(), mascara, s.length(), start, before, count));
                    if(before>=count) {
                        int offset = start + count;
                        if(offset >= mascara.length()) {
                            offset = mascara.length();
                        }
                        /*else if(!Character.isDigit(mascara.charAt(offset))) {
                            Log.d("TEXT_WATCHER", String.format("offset=%d str=%s",offset,str));
                            str = str.substring(0, getPositionWithoutMask(offset))+str.substring(getPositionWithoutMask(offset)+1,str.length());
                            mascara = setMask(str);
                            editText.setText(mascara);
                        }*/
                        editText.setSelection(offset);
                    }
                    else if(s.length() >= mascara.length()) {
                        int offset = start + count;
                        if(offset >= mascara.length())
                            offset = mascara.length();
                        else if(offset<mascara.length() && !Character.isDigit(mascara.charAt(offset)))
                            offset++;
                        editText.setSelection(offset);
                    }
                    else if(mascara.length() > s.length()) {
                        editText.setSelection((start + 1 + mascara.length() - s.length() < mascara.length()) ? start + 1 + mascara.length() - s.length() : mascara.length());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
