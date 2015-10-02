package com.berezich.sportconnector.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.berezich.sportconnector.EndpointApi;
import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.UsefulFunctions;
import com.berezich.sportconnector.backend.sportConnectorApi.model.AccountForConfirmation;

import java.io.IOException;

/**
 * Created by Sashka on 09.08.2015.
 */
public class RegistrationFragment extends Fragment implements EndpointApi.RegisterPersonAsyncTask.OnAction,
        AlertDialogFragment.OnActionDialogListener {

    private final String TAG = "MyLog_RegFragment";
    private String pass;
    View rootView;

    RegFragmentAction listenerCreateAccount = null;

    public RegistrationFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_registration, container, false);
        if(rootView!=null)
        {
            Button btn = (Button) rootView.findViewById(R.id.registration_btn_ok);
            if(btn!=null)
                btn.setOnClickListener(new OnClickRegisterListener());

            if(getActivity()!=null)
                ((MainActivity) getActivity()).setupUI(rootView);
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment targetFragment = getTargetFragment();
        if(targetFragment==null) {
            Log.e(TAG,"targetFragment should be set");
            throw new NullPointerException(String.format("For fragment %s targetFragment should be set", getFragment().toString()));
        }
        try {
            listenerCreateAccount =  (LoginFragment) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnActionListener for RegistrationFragment");
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();
    }

    private class OnClickRegisterListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            String email="", name="";
            pass = "";
            EditText editTxt;
            if((editTxt = (EditText) rootView.findViewById(R.id.registration_email_value))!=null) {
                email = editTxt.getText().toString();
            }
            if((editTxt = (EditText) rootView.findViewById(R.id.registration_name_value))!=null) {
                name = editTxt.getText().toString();
            }
            if((editTxt = (EditText) rootView.findViewById(R.id.registration_pass_value))!=null) {
                pass = editTxt.getText().toString();
            }
            setVisibleProgressBar(true);
            new EndpointApi.RegisterPersonAsyncTask(getFragment()).execute(email,name,pass,"PARTNER");
        }
    }

    private void setVisibleProgressBar(boolean isVisible)
    {
        View view;
        if(rootView!=null) {
            if ((view = rootView.findViewById(R.id.regFragment_linearLayout))!=null)
                view.setVisibility(!isVisible ? View.VISIBLE : View.GONE);
            if ((view = rootView.findViewById(R.id.regFragment_frameLayout))!=null)
                view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onRegisterAccountAsyncTaskFinish(Pair<AccountForConfirmation, Exception> result) {
        AlertDialogFragment dialog;
        AccountForConfirmation account = result.first;
        Exception error = result.second;
        if(getActivity()==null)
        {
            Log.e(TAG, "current fragment isn't attached to activity");
            return;
        }
        if(error == null && account!=null)
        {
            Log.d(TAG, "Account for registration was created");
            try {
                account.setPass(pass);

                LocalDataManager.saveMyPersonInfoToPref(UsefulFunctions.createPerson(account), getActivity());
                listenerCreateAccount.onCreateAccount(getString(R.string.registration_msgCreateAccount));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        Log.e(TAG, "Error Registration");
        Log.d(TAG,"person = null");

        String dialogMsg;
        Pair<ErrorVisualizer.ERROR_CODE,String> errTxtCode = ErrorVisualizer.getTextCodeOfRespException(getActivity().getBaseContext(),error);
        if(errTxtCode!=null && !errTxtCode.second.equals("")){
            dialogMsg = errTxtCode.second;
            Log.d(TAG,"registrationError code = "+errTxtCode.first+" msg = "+errTxtCode.second);
        }
        else
            dialogMsg = getString(R.string.server_unknow_err);

        dialog = AlertDialogFragment.newInstance(dialogMsg, false);
        dialog.setTargetFragment(this, 0);
        FragmentManager ft = getActivity().getSupportFragmentManager();
        if(ft!=null)
            dialog.show(ft, "");
    }

    @Override
    public void onPositiveClick() {
        setVisibleProgressBar(false);
    }

    @Override
    public void onNegativeClick() {
        setVisibleProgressBar(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        android.support.v7.app.ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if(actionBar!=null)
            actionBar.setTitle(getString(R.string.registration_fragmentTitle));
    }

    private Fragment getFragment(){
        return  this;
    }

    public interface RegFragmentAction
    {
        void onCreateAccount(String msgResult);
    }

}
