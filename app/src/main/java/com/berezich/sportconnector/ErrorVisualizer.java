package com.berezich.sportconnector;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.UnknownHostException;

/**
 * Created by Sashka on 12.07.2015.
 */
public class ErrorVisualizer {
    public static void showErrorAfterReq(Context context, FrameLayout layout, Exception e, String TAG)
    {
        ProgressBar progressBar;
        TextView textView;
        LinearLayout linearLayout;
        if(e!=null) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            //if((UnknownHostException)e!=null)
            if(e!=null)
            {
                if(layout!=null)
                {
                    layout.setVisibility(View.VISIBLE);
                    if((progressBar = (ProgressBar) layout.getChildAt(0))!=null)
                        progressBar.setVisibility(View.GONE);
                    if((linearLayout = (LinearLayout) layout.getChildAt(1))!=null)
                    {
                        linearLayout.setVisibility(View.VISIBLE);
                        if((textView = (TextView) linearLayout.getChildAt(0))!=null) {
                            textView.setText(context.getString(R.string.host_unreacheble_err));
                            textView.setVisibility(View.VISIBLE);
                        }
                        if((textView = (TextView) linearLayout.getChildAt(1))!=null) {
                            textView.setText(context.getString(R.string.try_again));
                            textView.setVisibility(View.VISIBLE);
                        }
                    }
                }
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
}
