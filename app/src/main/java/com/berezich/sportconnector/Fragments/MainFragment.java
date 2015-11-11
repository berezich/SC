package com.berezich.sportconnector.Fragments;

/**
 * Created by berezkin on 17.04.2015.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.berezich.sportconnector.EndpointApi.SyncSpots;
import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements SyncSpots.OnActionSyncSpots {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

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
    private SyncSpots syncSpots;

    public MainFragment setArgs(int sectionNumber) {
        _sectionNumber = sectionNumber;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        return this;
    }

    public MainFragment() {
        syncSpots = new SyncSpots(this,TAG);
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
            throw new ClassCastException(activity.toString()
                    + " must implement OnActionListener for MainFragment");
        }
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
    @Override
    public void onResume()
    {
        super.onResume();
        MainActivity mainActivity = (MainActivity)activity;
        Log.d(TAG, "onResume isSpotsSynced = " + mainActivity.isSpotsSynced());
        mainActivity.setmTitle(activity.getString(R.string.mainSearch_fragmentTitle));
        mainActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
        mainActivity.restoreActionBar();
        if(mainActivity.isSpotsSynced())
            setVisibleLayouts(true,false);
        else
            reqExecute();
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

    public interface OnActionListenerMainFragment {
        void onBtnClickMF(Filters position, int section);
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
        syncSpots.startSync(activity.getBaseContext(),regionId);
        setVisibleProgressBar();
    }

    @Override
    public void syncFinish(Exception ex, SyncSpots.ReqState reqState) {
        MainActivity mainActivity = (MainActivity) activity;
        if(ex!=null) {
            ErrorVisualizer.showErrorAfterReq(activity.getBaseContext(),
                    (FrameLayout) rootView.findViewById(R.id.main_frg_frameLayout), ex, TAG);
            mainActivity.setIsSpotsSynced(false);
        }
        else if(reqState== SyncSpots.ReqState.EVERYTHING_LOADED) {
            setVisibleLayouts(true, false);
            mainActivity.setIsSpotsSynced(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

    }
}
