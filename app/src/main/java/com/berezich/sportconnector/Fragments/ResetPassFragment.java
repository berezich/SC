package com.berezich.sportconnector.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.berezich.sportconnector.AlertDialogFragment;
import com.berezich.sportconnector.EndpointApi.EndpointApi;
import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.InputValuesValidation;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;

public class ResetPassFragment extends Fragment implements EndpointApi.ResetPassAsyncTask.OnAction,
        AlertDialogFragment.OnActionDialogListener {

    private final String TAG = "MyLog_RPassFragment";
    private final String ARG_EMAIL = "email";
    View rootView;
    String email="";
    private FragmentActivity activity;
    ResetPassFragmentAction listenerResetPass = null;

    public ResetPassFragment() {
    }

    public ResetPassFragment setArgs(String email){
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        this.setArguments(args);
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            EditText editTxt;
            rootView = inflater.inflate(R.layout.fragment_reset_pass, container, false);
            //if(( myPersonInfo = LocalDataManager.getMyPersonInfo())!=null)
            if ((editTxt = (EditText) rootView.findViewById(R.id.resetPass_email_value)) != null) {
                //editTxt.setText(myPersonInfo.getEmail());
                editTxt.setText(getArguments().getString(ARG_EMAIL));
            }
            if(rootView!=null)
            {
                Button btn = (Button) rootView.findViewById(R.id.resetPass_btn_ok);
                if(btn!=null)
                    btn.setOnClickListener(new OnClickResetPassListener());

                if(getActivity()!=null)
                    ((MainActivity) getActivity()).setupUI(rootView);
            }
            return rootView;
        } catch (Exception e) {
            e.printStackTrace();
            return rootView=null;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            super.onAttach(activity);
            this.activity = getActivity();
            Fragment targetFragment = getTargetFragment();
            if(targetFragment==null) {
                Log.e(TAG,"targetFragment should be set");
                throw new NullPointerException(String.format("For fragment %s targetFragment should be set",
                        ResetPassFragment.this.toString()));
            }
            try {
                listenerResetPass =  (LoginFragment) getTargetFragment();
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnActionListener for ResetPassFragment");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class OnClickResetPassListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            try {
                EditText editTxt;
                if((editTxt = (EditText) rootView.findViewById(R.id.resetPass_email_value))!=null) {
                    email = editTxt.getText().toString().trim();
                    if(!InputValuesValidation.isValidEmail(email)){
                        AlertDialogFragment dialog;
                        dialog = AlertDialogFragment.newInstance(activity.getString(R.string.changeEmail_errNew_invalid), false);
                        dialog.setTargetFragment(ResetPassFragment.this, 0);
                        FragmentManager ft = activity.getSupportFragmentManager();
                        if(ft!=null)
                            dialog.show(ft, "");
                        return;
                    }

                }
                setVisibleProgressBar(true);
                new EndpointApi.ResetPassAsyncTask(ResetPassFragment.this).execute(email);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setVisibleProgressBar(boolean isVisible)
    {
        View view;
        if(rootView!=null) {
            if ((view = rootView.findViewById(R.id.resetPass_linearLayout))!=null)
                view.setVisibility(!isVisible ? View.VISIBLE : View.GONE);
            if ((view = rootView.findViewById(R.id.resetPass_frameLayout))!=null)
                view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResetPassAsyncTaskFinish(Exception error) {
        try {
            if(error == null)
            {
                Log.d(TAG, "ResetPassReq was created");
                try {
                    String msg = String.format( activity.getString(R.string.resetPass_msgReqResetPass)+
                            " "+activity.getString(R.string.spam_warning_msg),email);
                    showDialog(msg);
                    listenerResetPass.onResetPass(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            Log.d(TAG,"error != null");

            String dialogMsg;
            Pair<ErrorVisualizer.ERROR_CODE,String> errTxtCode =
                    ErrorVisualizer.getTextCodeOfRespException(activity.getBaseContext(),error);
            if(errTxtCode!=null && !errTxtCode.second.equals("")){
                dialogMsg = errTxtCode.second;
                Log.d(TAG,"resetPassError code = "+errTxtCode.first+" msg = "+errTxtCode.second);
            }
            else
                dialogMsg = activity.getString(R.string.server_unknown_err);

            showDialog(dialogMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDialog(String msg){
        AlertDialogFragment dialog;
        dialog = AlertDialogFragment.newInstance(msg, false);
        dialog.setTargetFragment(this, 0);
        dialog.setCancelable(false);
        FragmentManager ft = activity.getSupportFragmentManager();
        if(ft!=null)
            dialog.show(ft, "");
    }

    @Override
    public void onPositiveClick() {
        try {
            setVisibleProgressBar(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNegativeClick() {
        try {
            setVisibleProgressBar(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCancelDialog() {
        try {
            setVisibleProgressBar(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {
            super.onCreateOptionsMenu(menu, inflater);
            menu.clear();
            android.support.v7.app.ActionBar actionBar = ((MainActivity) activity).getSupportActionBar();
            if(actionBar!=null)
                actionBar.setTitle(activity.getString(R.string.resetPass_fragmentTitle));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface ResetPassFragmentAction
    {
        void onResetPass(String msgResult);
    }

}
