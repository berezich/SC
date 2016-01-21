package com.berezich.sportconnector;

import android.content.Context;
import android.text.TextUtils;

public class InputValuesValidation {
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    public enum PASS_ERROR{OK,EMPTY,TOO_SHORT,NOT_MATCH}
    public static PASS_ERROR isValidPass(Context ctx,String pass){
        return isValidPass(ctx,pass,pass);
    }
    public static PASS_ERROR isValidPass(Context ctx,String pass, String confirmPass){
        if(pass==null)
            pass = "";
        if(confirmPass == null)
            confirmPass = "";
        if(pass.isEmpty() && confirmPass.isEmpty())
            return PASS_ERROR.EMPTY;
        int minLength = ctx.getResources().getInteger(R.integer.passMinLength_edtTxt);
        if(pass.length()< minLength)
            return PASS_ERROR.TOO_SHORT;
        if(!pass.equals(confirmPass))
            return PASS_ERROR.NOT_MATCH;

        return PASS_ERROR.OK;
    }
}
