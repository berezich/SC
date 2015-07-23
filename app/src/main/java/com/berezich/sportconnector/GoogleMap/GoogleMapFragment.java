package com.berezich.sportconnector.GoogleMap;

/**
 * Created by berezkin on 12.05.2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.MainFragment.Filters;
import com.berezich.sportconnector.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class GoogleMapFragment extends Fragment{

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static enum FiltersX {F0000,F1000,F0100,F0001,F1100,F1001,F0101,F1101,Fxx1x}
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "MyLog_GoogleMapFragment";
    private final int MARKER_OFFSET = 50;
    private final LatLng MOSCOW_loc = new LatLng(55.754357, 37.620035);
    private SpotMarker selectMarker = null;
    private float curZoom = -1;
    private  MapView mapView;
    private  GoogleMap map;
    //private static MapController mapController;
    //private OverlayManager overlayManager;
    //private Overlay overlay;
    private Resources res;
    private boolean isCourts=false;
    private boolean isCoaches=false;
    private boolean isPartners=false;
    private boolean isFavorite=false;
    private FiltersX curFilter;

    public static OnActionListenerGMapFragment listener;

    //список tiles уже отисованых на карте при данном масштабе
    //private HashMap<String,Tile> loadedTiles = new HashMap<String,Tile>();
    //список tiles в зоне видимости
    //private HashMap<String,Tile> curTiles = new HashMap<String,Tile>();


    public GoogleMapFragment setArgs(int sectionNumber, Filters filter) {

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
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
        return this;
    }

    public GoogleMapFragment() {

        //SpotsData.loadSpotsFromCache();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_googlemap, container, false);

        mapView = ((MapView) rootView.findViewById(R.id.mapview));
        if(mapView==null)
        {
            Log.e(TAG,"Error mapView = NULL");
            return null;
        }
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        if (map == null) {
            Log.e(TAG,"Error map = NULL");
            return null;
        }
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMyLocationEnabled(true);
        Clustering.initClusterManager(this.getActivity().getApplicationContext(), map, this);
        Clustering.addAllSpots(SpotsData.get_allSpots(), curFilter());
        map.setOnCameraChangeListener(Clustering.clusterManager);
        map.setInfoWindowAdapter(new Clustering.CustomInfoWindow());
        map.setOnMarkerClickListener(Clustering.clusterManager);
        map.setOnInfoWindowClickListener(Clustering.clusterManager);

        ImageButton btn;
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_coach);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());
        btn.setPressed(isCoaches);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_court);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());
        btn.setPressed(isCourts);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_partner);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());
        btn.setPressed(isPartners);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_star);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());
        btn.setPressed(isFavorite);

        if(selectMarker!=null)
            setCameraToLocation(selectMarker.getPosition(),false,curZoom);
        else
            setCameraToCurLocation();

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnActionListenerGMapFragment) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnActionListenerGMapFragment for GoogleMapFragment");
        }
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    class btnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            //Log.d(TAG, "button onClick!!!");

            return;
        }
    }
    class btnOnTouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageButton btn = (ImageButton) v;
            // show interest in events resulting from ACTION_DOWN
            if(event.getAction()==MotionEvent.ACTION_DOWN) {
                //btn.setPressed(!btn.isPressed());
                switch (v.getId())
                {
                    case R.id.map_btn_coach:
                        isCoaches = !isCoaches;
                        btn.setPressed(isCoaches);
                        if(!isCoaches)
                            activateButtons(Buttons.COURT,false);
                        break;
                    case R.id.map_btn_partner:
                        isPartners = !isPartners;
                        btn.setPressed(isPartners);
                        if(!isPartners)
                            activateButtons(Buttons.COURT,false);
                        break;
                    case R.id.map_btn_court:
                        isCourts = !isCourts;
                        btn.setPressed(isCourts);
                        if(isCourts)
                            activateButtons(Buttons.ALL,true);
                        break;
                    case R.id.map_btn_star:
                        isFavorite = !isFavorite;
                        btn.setPressed(isFavorite);
                        if(!isFavorite)
                            activateButtons(Buttons.COURT,false);
                        break;
                }
                setCurFilter();
                Clustering.addAllSpots(SpotsData.get_allSpots(), curFilter());

                //map.setOnCameraChangeListener(Clustering.clusterManager);

                return true;
            }
            if(event.getAction()==MotionEvent.ACTION_UP) {

                btn.performClick();
                return true;
            }

            return true;
        }
    }
    static enum Buttons{ALL,COUCH,PARTNER,FAVORITE,COURT}
    private void activateButtons(Buttons buttonType,boolean b) {
        ImageButton btn;
        if(buttonType == Buttons.ALL || buttonType == Buttons.COUCH) {
            isCoaches = b;
            btn = (ImageButton) getView().findViewById(R.id.map_btn_coach);
            btn.setPressed(b);
        }
        if(buttonType == Buttons.ALL || buttonType == Buttons.PARTNER) {
            isPartners = b;
            btn = (ImageButton) getView().findViewById(R.id.map_btn_partner);
            btn.setPressed(b);
        }
        if(buttonType == Buttons.ALL || buttonType == Buttons.PARTNER) {
            btn = (ImageButton) getView().findViewById(R.id.map_btn_star);
            btn.setPressed(b);
        }
        if(buttonType == Buttons.ALL || buttonType == Buttons.COURT) {
            isCourts = b;
            btn = (ImageButton) getView().findViewById(R.id.map_btn_court);
            btn.setPressed(b);
        }
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

    public class Size
    {
        double _width;
        double _height;

        public Size(double _width, double _height) {
            this._width = _width;
            this._height = _height;
        }

        public double getWidth() {
            return _width;
        }

        public double getHeight() {
            return _height;
        }
    }

    public static interface OnActionListenerGMapFragment {
        void onInfoWindowClickGMF(Long spotId);
    }

}

