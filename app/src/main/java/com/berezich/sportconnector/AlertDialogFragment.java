package com.berezich.sportconnector;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.TextView;

import java.security.Key;

/**
 * Created by berezkin on 22.07.2015.
 */
public class AlertDialogFragment extends DialogFragment {
    private static final String TAG = "MyLog_AlertDialogFragment";
    private OnActionDialogListener listener=null;
    public static AlertDialogFragment newInstance(String msg, boolean isNegativeBtn) {
        return newInstance("",msg,isNegativeBtn,true);
    }
    public static AlertDialogFragment newInstance(String title, String msg, boolean isNegativeBtn, boolean isInterfaceNeed) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("msg", msg);
        args.putBoolean("isNegativeBtn", isNegativeBtn);
        args.putBoolean("isInterfaceNeed",isInterfaceNeed);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String msg = getArguments().getString("msg");
        boolean isNegativeBtn = getArguments().getBoolean("isNegativeBtn");
        boolean isInterfaceNeed = getArguments().getBoolean("isInterfaceNeed");

        if(getTargetFragment()==null)
            throw new NullPointerException("Need to set targetFragment for AlertDialogFragment");
        if(isInterfaceNeed)
            try {
                listener = (OnActionDialogListener)getTargetFragment();
            } catch (ClassCastException e) {
                throw new ClassCastException(getTargetFragment().toString() +
                        " must implement OnActionDialogListener for AlertDialogFragment");
            }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                //.setIcon(R.drawable.alert_dialog_icon)

        if(title!=null && !title.equals(""))
            builder.setTitle(title);
        if(msg!=null && !msg.equals(""))
            builder.setMessage(msg);
        builder.setPositiveButton(R.string.alert_dialog_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dismiss();
                        if (listener != null)
                            listener.onPositiveClick();
                    }
                }
        );

        if (isNegativeBtn)
            builder.setNegativeButton(R.string.alert_dialog_cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dismiss();
                            if(listener!=null)
                                listener.onNegativeClick();
                        }
                    }
            );
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    if (listener != null)
                        listener.onCancelDialog();
                    return true;
                }
                return false;
            }
        });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }
    public static interface OnActionDialogListener
    {
        void onPositiveClick();
        void onNegativeClick();
        void onCancelDialog();
    }
}