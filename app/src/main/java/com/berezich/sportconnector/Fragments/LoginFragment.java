package com.berezich.sportconnector.Fragments;

import android.app.Activity;
//import android.app.FragmentManager;
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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.berezich.sportconnector.AlertDialogFragment;
import com.berezich.sportconnector.AppPref;
import com.berezich.sportconnector.EndpointApi;
import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;

import java.io.IOException;

/**
 * Created by berezkin on 20.07.2015.
 */
public class LoginFragment extends Fragment implements EndpointApi.AuthorizePersonAsyncTask.OnAction,
        AlertDialogFragment.OnActionDialogListener,
        RegistrationFragment.RegFragmentAction,
        ResetPassFragment.ResetPassFragmentAction {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "MyLog_LoginFragment";
    private Person myPersonInfo;
    private AppPref appPref;
    private String pass;
    int _sectionNumber;
    View rootView;
    private AlertDialogFragment dialog;

    OnActionListenerLoginFragment listenerLoginFragment = null;
    public LoginFragment setArgs(int sectionNumber) {
        _sectionNumber = sectionNumber;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        return this;
    }

    public LoginFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EditText editTxt;
        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        if(rootView!=null)
        {
            Button btn = (Button) rootView.findViewById(R.id.login_btn_ok);
            if(btn!=null)
                btn.setOnClickListener(new OnClickLoginListener());

            btn = (Button) rootView.findViewById(R.id.loginRegister_btn);
            if(btn!=null)
                btn.setOnClickListener(new OnClickRegistrationListener());
            /*
            btn = (Button) rootView.findViewById(R.id.loginForgotPass);
            if(btn!=null)
                btn.setOnClickListener(new OnClickForgotPassListener());
            */
            TextView textView = (TextView) rootView.findViewById(R.id.login_text_forgotPass);
            if(textView!=null)
                textView.setOnClickListener(new OnClickForgotPassListener());

            if(( myPersonInfo = LocalDataManager.getMyPersonInfo())!=null) {

                if ((editTxt = (EditText) rootView.findViewById(R.id.login_email_value)) != null) {
                    editTxt.setText(myPersonInfo.getEmail());
                }
                if ((editTxt = (EditText) rootView.findViewById(R.id.login_pass_value)) != null) {
                    editTxt.setText(myPersonInfo.getPass());

                    if ((appPref = LocalDataManager.getAppPref()) == null) {
                        AppPref appPref = new AppPref(false);
                        try {
                            LocalDataManager.saveAppPref(appPref,getActivity());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if(getActivity()!=null)
                ((MainActivity) getActivity()).setupUI(rootView);
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        try {
            listenerLoginFragment  =  (OnActionListenerLoginFragment) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnActionListener for LoginFragment");
        }

    }
    @Override
    public void onResume()
    {
        super.onResume();

        if ((appPref = LocalDataManager.getAppPref()) != null) {
            if (appPref.isAutoLogin() && myPersonInfo.getPass()!=null && !myPersonInfo.getPass().equals("")) {
                setVisibleProgressBar(true);
                new EndpointApi.AuthorizePersonAsyncTask(this).execute(myPersonInfo.getEmail().toString(), (pass=myPersonInfo.getPass()));
            }
        }
    }

    private class OnClickLoginListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            String login="";
            pass = "";
            EditText editTxt;
            if((editTxt = (EditText) rootView.findViewById(R.id.login_email_value))!=null)
                login = editTxt.getText().toString();
            if((editTxt = (EditText) rootView.findViewById(R.id.login_pass_value))!=null)
                pass = editTxt.getText().toString();
            
            setVisibleProgressBar(true);
            new EndpointApi.AuthorizePersonAsyncTask(getFragment()).execute(login,pass);
        }
    }

    private class OnClickRegistrationListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            RegistrationFragment registrationFragment = new RegistrationFragment();
            registrationFragment.setTargetFragment(getFragment(), 0);
            if(fragmentManager!=null) {
                fragmentManager.beginTransaction().replace(R.id.container, registrationFragment)
                        .addToBackStack(registrationFragment.getClass().getName()).commit();
                Log.d(TAG, String.format("prev fragment replaced with %s", registrationFragment.getClass().getName()));

            }
        }
    }

    private class OnClickForgotPassListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            ResetPassFragment resetPassFragment = new ResetPassFragment();
            resetPassFragment.setTargetFragment(getFragment(), 0);
            if(fragmentManager!=null) {
                fragmentManager.beginTransaction().replace(R.id.container, resetPassFragment)
                        .addToBackStack(resetPassFragment.getClass().getName()).commit();
                Log.d(TAG, String.format("prev fragment replaced with %s", resetPassFragment.getClass().getName()));

            }
        }
    }

    private void setVisibleProgressBar(boolean isVisible)
    {
        View view;
        if(rootView!=null) {
            if ((view = rootView.findViewById(R.id.loginFragment_linearLayout))!=null)
                view.setVisibility(!isVisible ? View.VISIBLE : View.GONE);
            if ((view = rootView.findViewById(R.id.loginFragment_frameLayout))!=null)
                view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }



    public static interface OnActionListenerLoginFragment {
        void onAuthorized();
    }


    @Override
    public void onAuthorizePersonAsyncTaskFinish(Pair<Person,Exception> result) {
        Person person = result.first;
        Exception error = result.second;
        if(getActivity()==null)
        {
            Log.e(TAG,"current fragment isn't attached to activity");
            return;
        }
        if(error == null && person!=null)
        {
            Log.d(TAG, "AuthorizePerson OK");
            try {
                person.setPass(pass);
                LocalDataManager.saveMyPersonInfoToPref(person, getActivity());
                AppPref appPref = LocalDataManager.getAppPref();
                if( appPref==null)
                    appPref = new AppPref(true);
                else
                    appPref.setIsAutoLogin(true);
                LocalDataManager.saveAppPref(appPref,getActivity());
                listenerLoginFragment.onAuthorized();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        Log.e(TAG, "Error AuthorizePerson");
        Log.d(TAG,"person = null");

        FrameLayout frameLayout;
        String dialogMsg;
        Pair<ErrorVisualizer.ERROR_CODE,String> errTxtCode = ErrorVisualizer.getTextCodeOfRespException(getActivity().getBaseContext(),error);
        if(errTxtCode!=null && !errTxtCode.second.equals(""))
            dialogMsg = errTxtCode.second;
        else
            dialogMsg = getString(R.string.server_unknow_err);
        Log.e(TAG,"request error: "+ErrorVisualizer.getDebugMsgOfRespException(error));
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
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.login_fragmentTitle);
    }

    @Override
    public void onCreateAccount(String msgResult) {
        showDialog(msgResult);
    }

    @Override
    public void onResetPass(String msgResult) {
        showDialog(msgResult);
    }

    private void showDialog(String msgResult){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if(fragmentManager!=null)
        {
            fragmentManager.popBackStack();
            dialog = AlertDialogFragment.newInstance("",msgResult, false, false);
            dialog.setTargetFragment(this, 0);
            dialog.show(fragmentManager, "");
        }
    }

    private Fragment getFragment(){
        return  this;
    }
}
