package com.berezich.sportconnector.PersonProfile;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.InputValuesValidation;
import com.berezich.sportconnector.R;

/**
 * Created by Sashka on 09.08.2015.
 */
public class ChangePassFragment extends DialogFragment {
    private static final String TAG = "MyLog_PassDialogFragment";
    private static final String PASS = "oldPassword";
    private OnActionPassDialogListener listener;
    private View rootView;

    public static ChangePassFragment newInstance(String oldPass) {
        ChangePassFragment frag = new ChangePassFragment();
        Bundle args = new Bundle();
        args.putString(PASS, oldPass);
        frag.setArguments(args);
        return frag;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getTargetFragment()==null)
            throw new NullPointerException("Need to set targetFragment for ChangePassFragment");
        try {
            listener = (OnActionPassDialogListener)getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().toString() + " must implement OnActionPassDialogListener for ChangePassFragment");
        }
        final String oldPass = getArguments().getString(PASS);
        getDialog().setTitle(R.string.changePass_dialogTitle);
        rootView = inflater.inflate(R.layout.fragment_change_pass, null);
        if(rootView!=null) {
            rootView.findViewById(R.id.changePass_btnCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                 public void onClick(View view) {
                        dismiss();
                    }
            });

            rootView.findViewById(R.id.changePass_btnOk).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText txtEdt, edtNew, edtNew2, edtOld;
                    TextView textView;
                    String newPassStr="",newPassStr2="",oldPass1="";
                    if(rootView!=null) {
                        /*textView = (TextView) rootView.findViewById(R.id.changePass_txt_error);
                        if(textView!=null)
                            textView.setVisibility(View.GONE);*/
                        if ((edtOld = (EditText) rootView.findViewById(R.id.changePass_txtEdt_old)) != null) {
                            oldPass1 = edtOld.getText().toString();
                            edtOld.setError(null);
                        }
                        if ((edtNew = (EditText) rootView.findViewById(R.id.changePass_txtEdt_new)) != null) {
                            newPassStr = edtNew.getText().toString();
                            edtNew.setError(null);
                        }
                        if ((edtNew2 = (EditText) rootView.findViewById(R.id.changePass_txtEdt_new2)) != null) {
                            newPassStr2 = edtNew2.getText().toString();
                            edtNew2.setError(null);
                        }

                        if(!oldPass.equals(oldPass1) ) {
                            if (edtOld!=null)
                                edtOld.setError(getString( R.string.changePass_errOld));
                            return;
                        }
                        InputValuesValidation.PASS_ERROR pass_error = InputValuesValidation.
                                isValidPass(getContext(), newPassStr, newPassStr2);
                        switch (pass_error){
                            case EMPTY:
                                if (edtNew!=null)
                                    edtNew.setError(getString( R.string.changePass_errNew_empty));
                                return;
                            case NOT_MATCH:
                                if (edtNew2!=null)
                                    edtNew2.setError(getString( R.string.changePass_errNew_notMatch));
                                return;
                            case TOO_SHORT:
                                if (edtNew!=null)
                                    edtNew.setError(String.format(getString( R.string.changePass_errNew_tooShort),
                                            getResources().getInteger(R.integer.changePass_minPassLength)));
                                return;

                        }
                        if(newPassStr.equals(oldPass)){
                            if (edtNew!=null)
                                edtNew.setError(getString(R.string.changePass_errNewOld_match));
                            return;
                        }

                        listener.onChangePassClick(edtNew.getText().toString());
                    }
                    dismiss();
                }
            });



        }
        return rootView;
    }

    public static interface OnActionPassDialogListener
    {
        void onChangePassClick(String newPass);
    }
}