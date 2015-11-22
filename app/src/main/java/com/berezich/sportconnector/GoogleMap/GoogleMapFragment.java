package com.berezich.sportconnector.GoogleMap;

/**
 * Created by berezkin on 12.05.2015.
 */

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.berezich.sportconnector.EndpointApi.SyncSpots;
import com.berezich.sportconnector.Fragments.MainFragment.Filters;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class GoogleMapFragment extends Fragment implements SyncSpots.OnActionSyncSpots{

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public enum FiltersX {F0000,F1000,F0100,F0001,F1100,F1001,F0101,F1101,Fxx1x}
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "MyLog_GoogleMapFragment";
    private final LatLng MOSCOW_loc = new LatLng(55.754357, 37.620035);
    private SpotMarker selectMarker = null;
    private float curZoom = -1;
    private  MapView mapView;
    private  GoogleMap map;
    private View rootView;
    private boolean isCourts=false;
    private boolean isCoaches=false;
    private boolean isPartners=false;
    private boolean isFavorite=false;
    private FiltersX curFilter;

    private final String IS_COURTS = "isCourts_key";
    private final String IS_COACHES = "isCoaches_key";
    private final String IS_PARTNERS = "isPartners_key";
    private final String IS_FAVORITE = "isFavorite_key";

    private AlertDialog dialog=null;
    enum GMAPS_STATE{NEED_UPDATE,NEED_INSTALL,OK}
    private GMAPS_STATE gmapState = GMAPS_STATE.OK;

    public static OnActionListenerGMapFragment listener;
    private MainActivity mainActivity;
    private SyncSpots syncSpots;

    public GoogleMapFragment setArgs(Filters filter) {

        try {
            Bundle args = new Bundle();
            this.setArguments(args);
            if(filter == Filters.COUCH)
                isCoaches = true;
            if(filter == Filters.SPARRING_PARTNERS)
                isPartners = true;
            if(filter == Filters.COURT) {
                isCourts = true;
                isCoaches = true;
                isPartners = true;
                isFavorite = true;
            }
            setCurFilter();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GoogleMapFragment() {

        try {
            syncSpots = new SyncSpots(this,TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            Log.d(TAG,"GoogleMapFragment on onCreateView");
            if(getActivity()==null) {
                Log.e(TAG,"GoogleMapFragment isn't attached to any activity");
                return null;
            }
            rootView = inflater.inflate(R.layout.fragment_googlemap, container, false);
            if(isGoogleMapsInstalled()) {
                mapView = ((MapView) rootView.findViewById(R.id.mapview));
                if (mapView == null) {
                    Log.e(TAG, "Error mapView = NULL");
                    return null;
                }
                mapView.onCreate(savedInstanceState);

                // Gets to GoogleMap from the MapView and does initialization stuff
                map = mapView.getMap();
                if (map == null) {
                    Log.e(TAG, "Error map = NULL");
                    gmapState = GMAPS_STATE.NEED_UPDATE;
                    return null;
                }
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
                map.setMyLocationEnabled(true);
                Clustering.initClusterManager(getContext(), map, this);

                map.setOnCameraChangeListener(Clustering.clusterManager);
                map.setInfoWindowAdapter(new Clustering.CustomInfoWindow());
                map.setOnMarkerClickListener(Clustering.clusterManager);
                map.setOnInfoWindowClickListener(Clustering.clusterManager);

                ImageButton btn;
                btn = (ImageButton) rootView.findViewById(R.id.map_btn_coach);
                btn.setOnClickListener(new btnClickListener());
                btn.setOnTouchListener(new btnOnTouchListener());
                btn = (ImageButton) rootView.findViewById(R.id.map_btn_court);
                btn.setOnClickListener(new btnClickListener());
                btn.setOnTouchListener(new btnOnTouchListener());
                btn = (ImageButton) rootView.findViewById(R.id.map_btn_partner);
                btn.setOnClickListener(new btnClickListener());
                btn.setOnTouchListener(new btnOnTouchListener());
                btn = (ImageButton) rootView.findViewById(R.id.map_btn_star);
                btn.setOnClickListener(new btnClickListener());
                btn.setOnTouchListener(new btnOnTouchListener());

                if (selectMarker != null)
                    setCameraToLocation(selectMarker.getPosition(), false, curZoom);
                else
                    setCameraToCurLocation();

                // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
                try {
                    MapsInitializer.initialize(getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                gmapState = GMAPS_STATE.NEED_INSTALL;
                return null;
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
            mainActivity = (MainActivity) activity;
            try {
                listener = mainActivity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnActionListenerGMapFragment for GoogleMapFragment");
            }
            LocalDataManager.init(mainActivity);
            mainActivity.onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onResume() {
        try {
            Log.d(TAG, "GoogleMapFragment on onResume");
            if(mapView!=null)
                mapView.onResume();
            super.onResume();
            switch (gmapState) {
                case OK:
                    updateButtonsStates();
                    Clustering.addAllSpots(SpotsData.get_allSpots(), curFilter());
                    break;
                case NEED_INSTALL:
                    showDialog(getContext(),  getString(R.string.gmap_dialog_needInstall_msg),
                            getString(R.string.gmap_dialog_needInstall_btn));
                    break;
                case NEED_UPDATE:
                    showDialog(getContext(), getString(R.string.gmap_dialog_needUpdate_msg),
                            getString(R.string.gmap_dialog_needUpdate_btn));
                    break;
            }
            mainActivity.setmTitle(mainActivity.getString(R.string.gmap_fragmentTitle));
            ActionBar actionBar = mainActivity.getSupportActionBar();
            if(actionBar!=null)
                actionBar.setHomeAsUpIndicator(null);
            mainActivity.restoreActionBar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        try {
            super.onPause();
            if(dialog!=null){
                dialog.dismiss();
                dialog=null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            outState.putBoolean(IS_COACHES, isCoaches);
            outState.putBoolean(IS_PARTNERS, isPartners);
            outState.putBoolean(IS_COURTS, isCourts);
            outState.putBoolean(IS_FAVORITE,isFavorite);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        try {
            super.onActivityCreated(savedInstanceState);
            if(savedInstanceState!=null){
                isFavorite = savedInstanceState.getBoolean(IS_FAVORITE);
                isCoaches = savedInstanceState.getBoolean(IS_COACHES);
                isPartners = savedInstanceState.getBoolean(IS_PARTNERS);
                isCourts = savedInstanceState.getBoolean(IS_COURTS);
                setCurFilter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        try {
            Log.d(TAG,"GoogleMapFragment on destroy");
            super.onDestroy();
            if(mapView!=null)
                mapView.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {
        try {
            super.onLowMemory();
            if(mapView!=null)
                mapView.onLowMemory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class btnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            //Log.d(TAG, "button onClick!!!");
        }
    }
    class btnOnTouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                ImageButton btn = (ImageButton) v;
                // show interest in events resulting from ACTION_DOWN
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    switch (v.getId())
                    {
                        case R.id.map_btn_coach:
                            isCoaches = !isCoaches;
                            setButtonImg(btn,isCoaches,Buttons.COACH);
                            if(!isCoaches)
                                activateButtons(Buttons.COURT,false);
                            break;
                        case R.id.map_btn_partner:
                            isPartners = !isPartners;
                            setButtonImg(btn, isPartners, Buttons.PARTNER);
                            if(!isPartners)
                                activateButtons(Buttons.COURT,false);
                            break;
                        case R.id.map_btn_court:
                            isCourts = !isCourts;
                            setButtonImg(btn, isCourts, Buttons.COURT);
                            if(isCourts)
                                activateButtons(Buttons.ALL,true);
                            break;
                        case R.id.map_btn_star:
                            isFavorite = !isFavorite;
                            setButtonImg(btn, isFavorite, Buttons.FAVORITE);
                            if(!isFavorite)
                                activateButtons(Buttons.COURT,false);
                            break;
                    }
                    setCurFilter();
                    Clustering.addAllSpots(SpotsData.get_allSpots(), curFilter());
                    return true;
                }
                if(event.getAction()==MotionEvent.ACTION_UP) {

                    btn.performClick();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
    }
    private void setButtonImg(ImageButton btn, boolean isOn, Buttons btnType){
        switch (btnType){
            case COACH:
                if(isOn)
                    btn.setBackgroundDrawable(mainActivity.getResources().getDrawable(R.drawable.gmap_coach_press));
                else
                    btn.setBackgroundDrawable(mainActivity.getResources().getDrawable(R.drawable.gmap_coach));
                break;
            case PARTNER:
                if(isOn)
                    btn.setBackgroundDrawable(mainActivity.getResources().getDrawable(R.drawable.gmap_partner_press));
                else
                    btn.setBackgroundDrawable(mainActivity.getResources().getDrawable(R.drawable.gmap_partner));
                break;
            case FAVORITE:
                if(isOn)
                    btn.setBackgroundDrawable(mainActivity.getResources().getDrawable(R.drawable.gmap_star_press));
                else
                    btn.setBackgroundDrawable(mainActivity.getResources().getDrawable(R.drawable.gmap_star));
                break;
            case COURT:
                if(isOn)
                    btn.setBackgroundDrawable(mainActivity.getResources().getDrawable(R.drawable.gmap_court_press));
                else
                    btn.setBackgroundDrawable(mainActivity.getResources().getDrawable(R.drawable.gmap_court));
                break;
        }
    }
    enum Buttons{ALL,COACH,PARTNER,FAVORITE,COURT}
    private void activateButtons(Buttons buttonType,boolean b) {
        ImageButton btn;
        View view = getView();
        if(view!=null) {
            if (buttonType == Buttons.ALL || buttonType == Buttons.COACH) {
                isCoaches = b;
                btn = (ImageButton) view.findViewById(R.id.map_btn_coach);
                setButtonImg(btn, b, Buttons.COACH);
            }
            if (buttonType == Buttons.ALL || buttonType == Buttons.PARTNER) {
                isPartners = b;
                btn = (ImageButton) view.findViewById(R.id.map_btn_partner);
                setButtonImg(btn,b,Buttons.PARTNER);
            }
            if (buttonType == Buttons.ALL || buttonType == Buttons.FAVORITE) {
                isFavorite = b;
                btn = (ImageButton) view.findViewById(R.id.map_btn_star);
                setButtonImg(btn, b, Buttons.FAVORITE);
            }
            if (buttonType == Buttons.ALL || buttonType == Buttons.COURT) {
                isCourts = b;
                btn = (ImageButton) view.findViewById(R.id.map_btn_court);
                setButtonImg(btn, b, Buttons.COURT);
            }
        }
    }
    private void updateButtonsStates(){
        ImageButton btn;
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_coach);
        setButtonImg(btn, isCoaches, Buttons.COACH);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_court);
        setButtonImg(btn, isCourts, Buttons.COURT);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_partner);
        setButtonImg(btn, isPartners, Buttons.PARTNER);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_star);
        setButtonImg(btn, isFavorite, Buttons.FAVORITE);
    }
    private void setCurFilter()
    {
        if(isCourts)
            curFilter = FiltersX.Fxx1x;
        else if(isPartners)
        {
            if(isCoaches) {
                if(isFavorite)
                    curFilter = FiltersX.F1101;
                else
                    curFilter = FiltersX.F1100;
            }
            else if(isFavorite)
                curFilter = FiltersX.F1001;
            else
                curFilter = FiltersX.F1000;
        }
        else if(isCoaches)
        {
            if(isFavorite)
                curFilter = FiltersX.F0101;
            else
                curFilter = FiltersX.F0100;
        }
        else if(isFavorite)
            curFilter = FiltersX.F0001;
        else
            curFilter = FiltersX.F0000;

        Log.d(TAG,"Set curFilter = "+curFilter);

    }

    private void setCameraToCurLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
            setCameraToLocation(new LatLng(location.getLatitude(),location.getLongitude()),false,-1);
        else
            setCameraToLocation(MOSCOW_loc,false,9);

    }
    private void setCameraToLocation(LatLng latLng, boolean isAnimate, float zoom) {
        if (latLng != null) {
            if(isAnimate) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)      // Sets the center of the map to location user
                        .zoom((zoom >= 0) ? zoom : 17)                   // Sets the zoom
                                //.bearing(90)                // Sets the orientation of the camera to east
                                //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            else
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, (zoom>=0)?zoom:9));

        }

    }

    public GoogleMap map() {
        return map;
    }

    public void setCurZoom(float curZoom) {
        this.curZoom = curZoom;
    }

    public void setSelectMarker(SpotMarker selectMarker) {
        this.selectMarker = selectMarker;
    }

    public FiltersX curFilter() {
        return curFilter;
    }

    public interface OnActionListenerGMapFragment {
        void onInfoWindowClickGMF(Long spotId);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {
            menu.clear();
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.fragment_gmap, menu);
            MenuItem menuItem = menu.findItem(R.id.menu_update);
            LayoutInflater inflater1 = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ImageView iv = (ImageView) inflater1.inflate(R.layout.action_img_update, null);
            iv.setOnClickListener(new OnUpdateClickListener());
            menuItem.setActionView(iv);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private class OnUpdateClickListener implements View.OnClickListener{

        ObjectAnimator anim;
        @Override
        public void onClick(View view) {
            try {
                anim = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
                anim.setDuration(mainActivity.getResources().getInteger(R.integer.rotationDuration));
                anim.setInterpolator(new LinearInterpolator());
                anim.setRepeatCount(ValueAnimator.INFINITE);
                anim.start();
                RegionInfo regionInfo = LocalDataManager.getRegionInfo();
                syncSpots.setReqState(SyncSpots.ReqState.REQ_REGINFO);
                if(regionInfo!=null)
                    syncSpots.startSync(mainActivity.getBaseContext(), regionInfo.getId());
                view.setClickable(false);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void syncFinish(Exception ex, SyncSpots.ReqState reqState) {
        try {
            if(ex==null && reqState== SyncSpots.ReqState.EVERYTHING_LOADED){
                Clustering.addAllSpots(SpotsData.get_allSpots(), curFilter());
                mainActivity.setIsSpotsSynced(true);
            }
            else
                Toast.makeText(mainActivity.getBaseContext(),mainActivity.getString(R.string.spotinfo_req_error_msg),Toast.LENGTH_SHORT).show();
            mainActivity.invalidateOptionsMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isGoogleMapsInstalled()
    {
        try
        {
            mainActivity.getPackageManager().getApplicationInfo("com.google.android.gms", 0 );
            return true;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            return false;
        }
    }
    public DialogInterface.OnClickListener getGoogleMapsListener()
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try {
                    FragmentManager fm = GoogleMapFragment.this.getFragmentManager();
                    if(fm!=null)
                        fm.popBackStack();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.google.android.gms")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }
    private void showDialog(Context ctx, String msg, String btn){
        if(dialog==null ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage(msg);
            builder.setPositiveButton(btn, getGoogleMapsListener());
            dialog = builder.create();
            dialog.setOnKeyListener(new Dialog.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface arg0, int keyCode,
                                     KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        GoogleMapFragment.this.dialog = null;
                    }
                    FragmentManager fm = GoogleMapFragment.this.getFragmentManager();
                    if(fm!=null)
                        fm.popBackStack();
                    return true;
                }
            });
            dialog.show();
        }
    }
}

