package com.berezich.sportconnector.Fragments;

/**
 * Created by berezkin on 17.04.2015.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.berezich.sportconnector.EndpointApi;
import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.GoogleMap.SpotsData;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.berezich.sportconnector.backend.sportConnectorApi.model.UpdateSpotInfo;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements
        EndpointApi.GetRegionAsyncTask.OnGetRegionAsyncTaskAction,
        EndpointApi.GetSpotListAsyncTask.OnAction,
        EndpointApi.GetUpdatedSpotListAsyncTask.OnAction{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private enum ReqState{
        REQ_REGINFO,REQ_SPOT_LIST,REQ_UPDATE_SPOTS,EVERYTHING_LOADED
    }
    public enum Filters {
        SPARRING_PARTNERS, COUCH, COURT
    };
    Activity activity;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "MyLog_mainFragment";
    int _sectionNumber;
    View rootView;
    OnActionListenerMainFragment listener;
    private static Long regionId = new Long(1);
    private RegionInfo regionInfo=null, localRegionInfo = null;
    private boolean isSpotsLoaded = false;
    private ReqState reqState = ReqState.REQ_REGINFO;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public MainFragment setArgs(int sectionNumber) {
        _sectionNumber = sectionNumber;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        return this;
    }

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Button btn = (Button) rootView.findViewById(R.id.main_frg_btn1);
        btn.setOnClickListener(new BtnClickListener());
        btn = (Button) rootView.findViewById(R.id.main_frg_btn2);
        btn.setOnClickListener(new BtnClickListener());
        btn = (Button) rootView.findViewById(R.id.main_frg_btn3);
        btn.setOnClickListener(new BtnClickListener());

        TextView textView = (TextView) rootView.findViewById(R.id.main_frg_tryAgain_txtView);
        if(textView!=null) {
            textView.setOnClickListener(new TryAgainClickListener());
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        try {
            listener = (OnActionListenerMainFragment) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnActionListener for MainFragment");
        }
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
    @Override
    public void onResume()
    {
        super.onResume();
        if(reqState==ReqState.EVERYTHING_LOADED)
            setVisibleLayouts(true,false);
        else {
            reqExecute();
        }
    }
    class BtnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            Button btn;
            try {
                btn = (Button) view;
            }
            catch (ClassCastException e)
            {
                throw new ClassCastException(view.toString() + "must be a Button");
            }
            switch (btn.getId())
            {
                case R.id.main_frg_btn1:
                    listener.onBtnClickMF(Filters.SPARRING_PARTNERS, _sectionNumber);
                    break;
                case R.id.main_frg_btn2:
                    listener.onBtnClickMF(Filters.COUCH, _sectionNumber);
                    break;
                case R.id.main_frg_btn3:
                    listener.onBtnClickMF(Filters.COURT, _sectionNumber);
                    break;
            }

        }
    }

    public static interface OnActionListenerMainFragment {
        void onBtnClickMF(Filters position, int section);
    }

    @Override
    public void onGetRegionAsyncTaskFinish(Pair<RegionInfo, Exception> result) {
        Exception exception = result.second;
        regionInfo=result.first;
        if(getActivity()==null)
        {
            Log.e(TAG,"current fragment isn't attached to activity");
            return;
        }

        if(exception!=null) {
            Log.e(TAG, "Error get regionInfo from server");
            ErrorVisualizer.showErrorAfterReq(activity.getBaseContext(), (FrameLayout) rootView.findViewById(R.id.main_frg_frameLayout), exception, TAG);
            //Toast.makeText(getBaseContext(), resText, Toast.LENGTH_LONG).show();
        }
        else if (regionInfo!=null)
        {
                    if ((localRegionInfo = LocalDataManager.getRegionInfo()) != null)
                        if (localRegionInfo.getVersion().equals(regionInfo.getVersion()))
                            if (localRegionInfo.getLastSpotUpdate().getValue() - regionInfo.getLastSpotUpdate().getValue()<0) {
                                //get list of updated spots and update existed
                                /*Toast.makeText(getBaseContext(),"get list of updated spots and update existed",
                                        Toast.LENGTH_LONG).show();*/
                                //LocalDataManager.setRegionInfo(regionInfo);
                                SpotsData.loadSpotsFromCache();
                                reqState = ReqState.REQ_UPDATE_SPOTS;
                                new EndpointApi.GetUpdatedSpotListAsyncTask(this).execute(
                                        new Pair<Long, DateTime>(regionInfo.getId(), localRegionInfo.getLastSpotUpdate()));
                                return;
                            }
                            else {
                                //we have actual spot information
                                //Toast.makeText(getBaseContext(),"we have actual spot information",Toast.LENGTH_LONG).show();
                                SpotsData.loadSpotsFromCache();
                                //Toast.makeText(getBaseContext(), SpotsData.get_allSpots().toString(), Toast.LENGTH_LONG).show();
                                setVisibleLayouts(true,false);
                                reqState = ReqState.EVERYTHING_LOADED;
                                return;
                            }
                //get all spots from server
                //LocalDataManager.setRegionInfo(regionInfo);
                reqState = ReqState.REQ_SPOT_LIST;
                new EndpointApi.GetSpotListAsyncTask(this).execute(regionId);
                //Toast.makeText(getBaseContext(),"get all spots from server",Toast.LENGTH_LONG).show();


        }
    }

    @Override
    public void onGetSpotListFinish(Pair<List<Spot>, Exception> result) {
        Exception error = result.second;
        List<Spot> spotLst = result.first;
        if(getActivity()==null)
        {
            Log.e(TAG,"current fragment isn't attached to activity");
            return;
        }
        if(spotLst==null)
            spotLst = new ArrayList<Spot>();
        if(error == null && spotLst!=null)
        {
            try {
                SpotsData.saveSpotsToCache(spotLst);
                LocalDataManager.setRegionInfo(regionInfo);
                LocalDataManager.saveRegionInfoToPref(activity);
                //Toast.makeText(getBaseContext(),"got all spots from server and saveRegionInfo to pref",Toast.LENGTH_LONG).show();
                setVisibleLayouts(true, false);
                reqState = ReqState.EVERYTHING_LOADED;
            } catch (IOException e) {
                e.printStackTrace();
                ErrorVisualizer.showErrorAfterReq(getActivity().getBaseContext(),
                        (FrameLayout) rootView.findViewById(R.id.main_frg_frameLayout), error, TAG);
            }
            return;
        }
        Log.e(TAG, "Error get ListSpot from server");
        if(error!=null)
        {
            Log.e(TAG,error.getMessage());
            error.printStackTrace();
        }
        else
            Log.e(TAG,"ListSpot = null");
        ErrorVisualizer.showErrorAfterReq(getActivity().getBaseContext(),
                (FrameLayout) rootView.findViewById(R.id.main_frg_frameLayout), error, TAG);
    }

    @Override
    public void onGetUpdateSpotListFinish(Pair<List<UpdateSpotInfo>, Exception> result) {
        Exception error = result.second;
        if(getActivity()==null)
        {
            Log.e(TAG,"current fragment isn't attached to activity");
            return;
        }
        List<Spot> spotLst = new ArrayList<Spot>();
        List<UpdateSpotInfo> updateSpotInfoLst = result.first;

        if(error == null && updateSpotInfoLst!=null)
        {
            try {
                SpotsData.setSpotUpdatesToCache(updateSpotInfoLst);
                LocalDataManager.setRegionInfo(regionInfo);
                LocalDataManager.saveRegionInfoToPref(activity);
                //Toast.makeText(getBaseContext(), "got updated spots num=" + updateSpotInfoLst.size() + " from server and saveRegionInfo to pref", Toast.LENGTH_LONG).show();
                reqState = ReqState.EVERYTHING_LOADED;
                setVisibleLayouts(true,false);
            } catch (IOException e) {
                e.printStackTrace();
                ErrorVisualizer.showErrorAfterReq(getActivity().getBaseContext(),
                        (FrameLayout) rootView.findViewById(R.id.main_frg_frameLayout), error, TAG);
            }
            return;
        }
        Log.e(TAG, "Error get updated ListSpot from server");
        if(error!=null)
        {
            Log.e(TAG,error.getMessage());
            error.printStackTrace();
        }
        else
            Log.e(TAG, "ListUpdatedSpot = null");
        ErrorVisualizer.showErrorAfterReq(getActivity().getBaseContext(),
                (FrameLayout)rootView.findViewById(R.id.main_frg_frameLayout),error,TAG);
    }
    private void setVisibleLayouts(boolean relativeLayout, boolean frameLayout)
    {
        if(relativeLayout)
            rootView.findViewById(R.id.main_frg_relativeLayout).setVisibility(View.VISIBLE);
        else
            rootView.findViewById(R.id.main_frg_relativeLayout).setVisibility(View.GONE);
        if(frameLayout)
            rootView.findViewById(R.id.main_frg_frameLayout).setVisibility(View.VISIBLE);
        else
            rootView.findViewById(R.id.main_frg_frameLayout).setVisibility(View.GONE);
    }
    private void setVisibleProgressBar()
    {
        RelativeLayout mainLayout;
        FrameLayout frameLayout;
        if(rootView!=null) {
            if((frameLayout = (FrameLayout) rootView.findViewById(R.id.main_frg_frameLayout))!=null)
                ErrorVisualizer.showProgressBar(frameLayout);
            if((mainLayout = (RelativeLayout) rootView.findViewById(R.id.main_frg_relativeLayout))!=null)
                mainLayout.setVisibility(View.GONE);
        }
    }
    private class TryAgainClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {

            reqExecute();
        }
    }
    private void reqExecute()
    {
        switch (reqState){
            case REQ_REGINFO:
                new EndpointApi.GetRegionAsyncTask(this).execute(regionId);
                break;
            case REQ_SPOT_LIST:
                new EndpointApi.GetSpotListAsyncTask(this).execute(regionId);
                break;
            case REQ_UPDATE_SPOTS:
                if(regionInfo!=null && localRegionInfo!=null)
                    new EndpointApi.GetUpdatedSpotListAsyncTask(this).execute(
                            new Pair<Long, DateTime>(regionInfo.getId(), localRegionInfo.getLastSpotUpdate()));
                break;
        }
        setVisibleProgressBar();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }
}
