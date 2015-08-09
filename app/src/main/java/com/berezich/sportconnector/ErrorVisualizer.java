package com.berezich.sportconnector;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import java.net.UnknownHostException;

/**
 * Created by Sashka on 12.07.2015.
 */
public class ErrorVisualizer {
    public enum  ERROR_CODE {UNKNOWN_SRV_ERR,HOST_UNREACHABLE,AUTH_FAILED,REGISTRATION_FAILED};
    public static void showErrorAfterReq(Context context, FrameLayout layout, Exception e, String TAG)
    {
        ProgressBar progressBar;
        TextView textView;
        LinearLayout linearLayout;
        if(e!=null) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        Pair<ERROR_CODE,String> errCodeTxt = getTextCodeOfRespException(context,e);
        if(layout!=null)
        {
            layout.setVisibility(View.VISIBLE);
            if((progressBar = (ProgressBar) layout.getChildAt(0))!=null)
                progressBar.setVisibility(View.GONE);
            if((linearLayout = (LinearLayout) layout.getChildAt(1))!=null)
            {
                linearLayout.setVisibility(View.VISIBLE);
                if((textView = (TextView) linearLayout.getChildAt(0))!=null) {
                    textView.setText(errCodeTxt.second);
                    textView.setVisibility(View.VISIBLE);
                }
                if((textView = (TextView) linearLayout.getChildAt(1))!=null)
                    if((errCodeTxt.first == ERROR_CODE.HOST_UNREACHABLE)) {
                        textView.setText(context.getString(R.string.try_again));
                        textView.setVisibility(View.VISIBLE);
                    }
                    else
                        textView.setVisibility(View.GONE);

            }
        }
    }
    public static void showProgressBar(FrameLayout layout)
    {
        ProgressBar progressBar;
        TextView textView;
        LinearLayout linearLayout;
        if(layout!=null)
        {
            layout.setVisibility(View.VISIBLE);
            if((progressBar = (ProgressBar) layout.getChildAt(0))!=null)
                progressBar.setVisibility(View.VISIBLE);
            if((linearLayout = (LinearLayout) layout.getChildAt(1))!=null)
                linearLayout.setVisibility(View.GONE);
        }
    }
    public static Pair<ERROR_CODE,String> getTextCodeOfRespException(Context context, Exception exception)
    {
        String errMsg=context.getString(R.string.server_unknow_err);
        ERROR_CODE error_code = ERROR_CODE.UNKNOWN_SRV_ERR;
        try {
            if(exception!=null)
                if(exception instanceof UnknownHostException){
                    errMsg = context.getString(R.string.host_unreachable_err);
                    error_code = ERROR_CODE.HOST_UNREACHABLE;
                }
                else if (exception instanceof GoogleJsonResponseException){
                    GoogleJsonResponseException appError = (GoogleJsonResponseException) exception;
                    String errExceptMsg = ((GoogleJsonResponseException) exception).getDetails().getMessage();
                    if(errExceptMsg.indexOf("AuthFailed@:")==0) {
                        errMsg = context.getString(R.string.login_err_authorized);
                        error_code = ERROR_CODE.AUTH_FAILED;
                    }
                    else if(errExceptMsg.indexOf("loginExists@:")==0) {
                        errMsg = context.getString(R.string.registration_err_loginExists);
                        error_code = ERROR_CODE.REGISTRATION_FAILED;
                    }
                    else if(errExceptMsg.indexOf("idNull@:")==0) {
                        errMsg = context.getString(R.string.registration_err_idNull);
                        error_code = ERROR_CODE.REGISTRATION_FAILED;
                    }
                    else if(errExceptMsg.indexOf("nameNull@:")==0) {
                        errMsg = context.getString(R.string.registration_err_nameNull);
                        error_code = ERROR_CODE.REGISTRATION_FAILED;
                    }
                    else if(errExceptMsg.indexOf("passNull@:")==0) {
                        errMsg = context.getString(R.string.registration_err_passNull);
                        error_code = ERROR_CODE.REGISTRATION_FAILED;
                    }
                }
        } finally {
            return  new Pair<>(error_code,errMsg);
        }

    }
    public static String getDebugMsgOfRespException(Exception exception)
    {
        String errMsg="exception == null";
        try {
            if(exception!=null)
                if (exception instanceof GoogleJsonResponseException) {
                    GoogleJsonResponseException appError = (GoogleJsonResponseException) exception;
                    errMsg = ((GoogleJsonResponseException) exception).getDetails().getMessage();

                }
                else
                    errMsg = exception.getMessage();

        }
        catch (Exception ex){
            errMsg = "get error msg failed";
        }
        finally {
            return  errMsg;
        }

    }
}
