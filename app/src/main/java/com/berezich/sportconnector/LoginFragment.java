package com.berezich.sportconnector;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.berezich.sportconnector.SpotInfo.ProfileItemLstAdapter;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by berezkin on 20.07.2015.
 */
public class LoginFragment extends Fragment implements EndpointApi.GetListPersonByIdLstAsyncTask.OnAction{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "LOGIN_FRAGMENT";
    private Person myPersonInfo;
    private AppPref appPref;
    int _sectionNumber;
    View rootView;

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

        try {
            if(LocalDataManager.loadMyPersonInfoFromPref(getActivity())) {
                myPersonInfo = LocalDataManager.getMyPersonInfo();
                if (LocalDataManager.loadAppPref(getActivity())) {
                    appPref = LocalDataManager.getAppPref();
                    if (appPref!=null && appPref.isAutoLogin())
                    {

                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_login, container, false);

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

    }

    private class OnClickLoginListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {

        }
    }

    public static interface OnActionListenerLoginFragment {
        void onAuthorized();
    }


    @Override
    public void onGetListPersonByIdLstFinish(Pair<List<Person>, Exception> result) {
        List<Person> personLst = result.first;
        Exception error = result.second;
        Person person;
        if(error == null && personLst!=null && personLst.size()==1)
        {
            person = personLst.get(0);
            if(person!=null)
                try {
                    LocalDataManager.saveMyPersonInfoToPref(person,getActivity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return;
        }
        Log.e(TAG, "Error GetListPersonByIdLst");
        if(error!=null)
        {
            FrameLayout frameLayout;
            /*if((frameLayout = (FrameLayout) rootView.findViewById(R.id.spotinfo_frg_frameLayout))!=null)
                ErrorVisualizer.showErrorAfterReq(getActivity().getBaseContext(), frameLayout,error,TAG);
            setVisible(View.GONE,View.VISIBLE,View.GONE);*/
        }
        else
            Log.d(TAG,"personLst = null");
    }
}
