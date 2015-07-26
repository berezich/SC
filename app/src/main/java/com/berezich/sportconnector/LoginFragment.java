package com.berezich.sportconnector;

import android.app.Activity;
//import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.berezich.sportconnector.SpotInfo.ProfileItemLstAdapter;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by berezkin on 20.07.2015.
 */
public class LoginFragment extends Fragment implements EndpointApi.AuthorizePersonAsyncTask.OnAction,
                                                       AlertDialogFragment.OnActionDialogListener{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "MyLog_LoginFragment";
    private Person myPersonInfo;
    private AppPref appPref;
    int _sectionNumber;
    View rootView;
    private AlertDialogFragment dialog;

    OnActionListenerLoginFragment listenerLoginFragment = null;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        if(rootView!=null)
        {
            Button btn = (Button) rootView.findViewById(R.id.login_btn_ok);
            if(btn!=null)
                btn.setOnClickListener(new OnClickLoginListener());
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

        if(( myPersonInfo = LocalDataManager.getMyPersonInfo())!=null)
            if ((appPref = LocalDataManager.getAppPref())!=null) {
                if ((appPref != null) && appPref.isAutoLogin())
                {
                    new EndpointApi.AuthorizePersonAsyncTask(this).execute(myPersonInfo.getId(),myPersonInfo.getPass());
                }

            }

    }
    @Override
    public void onResume()
    {
        super.onResume();

    }

    private class OnClickLoginListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            String pass="", login="";
            EditText editTxt;
            if((editTxt = (EditText) rootView.findViewById(R.id.login_email_value))!=null) {
                login = editTxt.getText().toString();
                if(login.length()<5) {
                    //editTxt.setHint(R.string.login_err_hint);
                    return;
                }
            }
            if((editTxt = (EditText) rootView.findViewById(R.id.login_pass_value))!=null) {
                pass = editTxt.getText().toString();
                if(pass.length()<8) {
                    //editTxt.setHint(R.string.login_pass_err_hint);
                    return;
                }
            }
            setVisibleProgressBar(true);
            new EndpointApi.AuthorizePersonAsyncTask(getFragment()).execute(login,pass);
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
                LocalDataManager.saveMyPersonInfoToPref(person, getActivity());
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

        dialog = AlertDialogFragment.newInstance(dialogMsg, false);
        dialog.setTargetFragment(this, 0);
        FragmentManager ft = getFragmentManager();
        dialog.show(getFragmentManager(), "");

        /*if((frameLayout = (FrameLayout) rootView.findViewById(R.id.spotinfo_frg_frameLayout))!=null)
            ErrorVisualizer.showErrorAfterReq(getActivity().getBaseContext(), frameLayout,error,TAG);
        setVisible(View.GONE,View.VISIBLE,View.GONE);*/

    }

    @Override
    public void onPositiveClick() {
        setVisibleProgressBar(false);
    }

    @Override
    public void onNegativeClick() {
        setVisibleProgressBar(false);
    }

    private Fragment getFragment(){
        return  this;
    }
}
