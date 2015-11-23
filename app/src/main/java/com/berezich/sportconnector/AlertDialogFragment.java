package com.berezich.sportconnector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;

public class AlertDialogFragment extends DialogFragment {
    private OnActionDialogListener listener=null;

    public AlertDialogFragment() {
    }

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
    @NonNull
    public Dialog onCreateDialog( Bundle savedInstanceState) {
        if(getTargetFragment()==null)
            throw new NullPointerException("Need to set targetFragment for AlertDialogFragment");
        boolean isInterfaceNeed = getArguments().getBoolean("isInterfaceNeed");
        if(isInterfaceNeed)
            try {
                listener = (OnActionDialogListener)getTargetFragment();
            } catch (ClassCastException e) {
                throw new ClassCastException(getTargetFragment().toString() +
                        " must implement OnActionDialogListener for AlertDialogFragment");
            }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            String title = getArguments().getString("title");
            String msg = getArguments().getString("msg");
            if(title!=null && !title.equals(""))
                builder.setTitle(title);
            if(msg!=null && !msg.equals(""))
                builder.setMessage(msg);
            builder.setPositiveButton(R.string.alert_dialog_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                dismiss();
                                if (listener != null)
                                    listener.onPositiveClick();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );

            boolean isNegativeBtn = getArguments().getBoolean("isNegativeBtn");
            if (isNegativeBtn)
                builder.setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    dismiss();
                                    if(listener!=null)
                                        listener.onNegativeClick();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    try {
                        if(keyCode == KeyEvent.KEYCODE_BACK) {
                            dismiss();
                            if (listener != null)
                                listener.onCancelDialog();
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.create();
    }
    public interface OnActionDialogListener
    {
        void onPositiveClick();
        void onNegativeClick();
        void onCancelDialog();
    }
}