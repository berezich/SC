package com.berezich.sportconnector.GoogleMap;

/**
 * Created by berezkin on 12.05.2015.
 */

import android.app.Activity;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.MainFragment.Filters;
import com.berezich.sportconnector.SportObjects.InfoTile;
import com.berezich.sportconnector.R;

import com.berezich.sportconnector.SportObjects.Spot;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;

import java.util.HashMap;
import java.util.List;

public class GoogleMapFragment extends Fragment{

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static enum FiltersX {F0000,F1000,F0100,F0001,F1100,F1001,F0101,F1101,Fxx1x}
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "GoogleMapFragment";
    private final int MARKER_OFFSET = 50;
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
    

    //список tiles уже отисованых на карте при данном масштабе
    private HashMap<String,Tile> loadedTiles = new HashMap<String,Tile>();
    //список tiles в зоне видимости
    private HashMap<String,Tile> curTiles = new HashMap<String,Tile>();


    public GoogleMapFragment setArgs(int sectionNumber, Filters filter) {

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        if(filter == Filters.COUCH)
            isCoaches = true;
        if(filter == Filters.SPARRING_PARTNERS)
            isPartners = true;
        if(filter == Filters.COURT)
            isCourts = true;
        setCurFilter();
        return this;
    }

    public GoogleMapFragment() {

        SpotsData.getSpotsFromCache();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_googlemap, container, false);

        mapView = ((MapView) rootView.findViewById(R.id.mapview));
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        if (map == null) {

        }
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMyLocationEnabled(true);
        Clustering.initClusterManager(this.getActivity().getApplicationContext(),map,this);
        Clustering.addAllSpots(SpotsData.get_allSpots(),curFilter());
        map.setOnCameraChangeListener(Clustering.clusterManager);
        map.setInfoWindowAdapter(new Clustering.CustomInfoWindow());
        map.setOnMarkerClickListener(Clustering.clusterManager);

        /*
        Marker marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(55.778234, 37.588539))
                .title("Комета")
                .snippet("1203101010333012"));

        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(55.796051, 37.537766))
                .title("Теннисный клуб ЦСКА")
                .snippet("1203101010330023"));
        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(55.795504, 37.541117))
                .title("Европейская школа Тенниса")
                .snippet("1203101010330032"));
        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(55.792503, 37.536984))
                .title("Европейская школа Тенниса")
                .snippet("1203101010330201"));
        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(55.804162, 37.561679))
                .title("Теннисенок")
                .snippet("1203101010330101"));
        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(55.768345, 37.693669))
                .title("Планета тенниса")
                .snippet("1203101011223301"));
        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(55.715099, 37.555023))
                .title("TennisVIP")
                .snippet("1203101012112302"));
        */
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Updates the location and zoom of the MapView
        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        //map.animateCamera(cameraUpdate);

    /*
    View rootView = inflater.inflate(R.layout.fragment_yamap, container, false);
    mapView = (MapView)  rootView.findViewById(R.id.map);
    mapView.showBuiltInScreenButtons(true);
    mapView.showJamsButton(false);
    mapController = mapView.getMapController();
    mapController.addMapListener(this);
    //mapController.setZoomCurrent(7);


    // Create a layer of objects for the map
    overlay = new Overlay(mapController);
    overlayManager = mapController.getOverlayManager();
    overlayManager.addOverlay(overlay);

    res = getResources();

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

    displayNewObj();
    */

    /*btn = (ImageButton) rootView.findViewById(R.id.map_btn_court_2);
    btn.setOnClickListener(new btnClickListener());
    btn.setOnTouchListener(new btnOnTouchListener());*/

    /*TextView txtView = (TextView) rootView.findViewById(R.id.map_textView);
    switch (_filter)
    {
        case SPARRING_PARTNERS:
            txtView.setText("YaMap Спарринг партнеры");
            break;
        case COUCH:
            txtView.setText("YaMap Тренеры");
            break;
        case CORT:
            txtView.setText("YaMap Корты");
            break;
    }*/
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
    /*@Override
    public void onMapActionEvent(MapEvent mapEvent) {

        mapView.showBuiltInScreenButtons(true);
        mapView.showJamsButton(false);

        switch (mapEvent.getMsg()) {
            case MapEvent.MSG_SCALE_BEGIN:
                //textView.setText("MSG_SCALE_BEGIN");
                overlay.clearOverlayItems();
                loadedTiles.clear();
                break;
            case MapEvent.MSG_SCALE_MOVE:
                //textView.setText("MSG_SCALE_MOVE");
                break;
            case MapEvent.MSG_SCALE_END:
                //textView.setText("MSG_SCALE_END");
                Log.d(TAG, "mapCenter = " + mapController.getMapCenter().toString());
                Log.d(TAG, "mapHeight = " + mapController.getHeight());
                Log.d(TAG, "mapWidth = " + mapController.getWidth());
                displayNewObj();
                mapController.notifyRepaint();
                break;

            case MapEvent.MSG_ZOOM_BEGIN:
                //textView.setText("MSG_ZOOM_BEGIN");
                overlay.clearOverlayItems();
                loadedTiles.clear();
                break;
            case MapEvent.MSG_ZOOM_MOVE:
                //textView.setText("MSG_ZOOM_MOVE");
                break;
            case MapEvent.MSG_ZOOM_END:
                Log.d(TAG, "ZOOM = " + mapController.getZoomCurrent());
                displayNewObj();
                mapController.notifyRepaint();
                Log.d(TAG, "loadedTiles num = " + loadedTiles.size());
                //textView.setText("MSG_ZOOM_END");
                break;

            case MapEvent.MSG_SCROLL_BEGIN:
                //textView.setText("MSG_SCROLL_BEGIN");
                break;
            case MapEvent.MSG_SCROLL_MOVE:
                //textView.setText("MSG_SCROLL_MOVE");
                break;
            case MapEvent.MSG_SCROLL_END:
                //textView.setText("MSG_SCROLL_END");
                Log.d(TAG, "mapCenter = " + mapController.getMapCenter().toString());
                Log.d(TAG, "mapHeight = " + mapController.getHeight());
                Log.d(TAG, "mapWidth = " + mapController.getWidth());
                displayNewObj();
                mapController.notifyRepaint();
                Log.d(TAG, "loadedTiles num = " + loadedTiles.size());
                //curTiles = visibleTileList();
                break;
            default:
                //textView.setText("MSG_EMPTY");
                break;
        }
        int i;

    }
    //получаем таблицу видимых tiles
    private HashMap<String,Tile> buildTiles()
    {


        HashMap<String,Tile> tiles = new HashMap<String,Tile>();
        try {
            Size size = new Size(mapController.getWidth(),mapController.getHeight());
            ScreenPoint pixCenterGeo = mapController.getScreenPoint(new GeoPoint(0,0));
            double mapZoom = mapController.getZoomCurrent()+1;
            double zoomFactor = Math.pow(2,-mapZoom);
            Size pixelSize = new Size(size.getWidth()*zoomFactor,size.getHeight()*zoomFactor);
            float tileSize = (float)(256 * zoomFactor);
            float mapSize = (float)(256*Math.pow(2,mapZoom));
            ScreenPoint tilePixCenter = new ScreenPoint((float)(mapSize*0.5),(float)(mapSize*0.5));
            ScreenPoint pixelCenter = new ScreenPoint(tilePixCenter.getX()-pixCenterGeo.getX()+(float)(mapController.getWidth()*0.5),tilePixCenter.getY()-pixCenterGeo.getY()+(float)(mapController.getHeight()*0.5));
            pixelCenter = new ScreenPoint(pixelCenter.getX()*(float)zoomFactor, (pixelCenter.getY()*(float)zoomFactor));
            //нам нужны пиксельные границы в пространстве нулевого зума расширенная до углов тайлов
            //Tile.Bounds pixelBounds = new Tile.Bounds(new ScreenPoint((float)Math.max(0,pixelCenter.getX() - pixelSize.getWidth() * .5), (float) Math.max(0,pixelCenter.getY() - pixelSize.getHeight() * .5)),new ScreenPoint((float) Math.min(256, pixelCenter.getX() + pixelSize.getWidth() * .5), (float)Math.min(256,pixelCenter.getY() + pixelSize.getHeight() * .5)));
            ScreenPoint pixelStart  = new ScreenPoint((float)Math.max(0,pixelCenter.getX() - pixelSize.getWidth() * .5), (float) Math.max(0,pixelCenter.getY() - pixelSize.getHeight() * .5));
            ScreenPoint pixelEnd  = new ScreenPoint((float) Math.min(256, pixelCenter.getX() + pixelSize.getWidth() * .5), (float)Math.min(256,pixelCenter.getY() + pixelSize.getHeight() * .5));
            double quadZoom = mapZoom;
            tiles.clear();
            boolean xfill = true;
            Tile tile;
            //набиваем квады, пока они не выходях за пределы экрана
            for (float x = 0; xfill; x += tileSize) {
                for (float y = 0; ; y += tileSize) {
                    tile = new Tile(new ScreenPoint( (float)(0 + pixelStart.getX() + x), (float)(0 + pixelStart.getY() + y)), quadZoom);
                    tiles.put(tile.name(), tile);
                    if (tile.bounds().p2().getY() >= pixelEnd.getY()) {
                        if (tile.bounds().p2().getX() >= pixelEnd.getX()) {
                            xfill = false;
                            break;
                        }
                        break;
                    }

                }
            }

            return tiles;
        } catch (Exception e) {
            e.printStackTrace();
            return tiles;
        }
    }
    public static ScreenPoint globalPxToPhonePx(ScreenPoint globalPix,double zoom)
    {
        float zoomFactor = (float)Math.pow(2,zoom);
        int mapPixSize = (int) (256*zoomFactor);
        ScreenPoint mapCenter = mapController.getScreenPoint(new GeoPoint(0,0));
        ScreenPoint phonePix = new ScreenPoint((float)(globalPix.getX()-mapPixSize*.5+mapCenter.getX()),(float)(globalPix.getY()-mapPixSize*.5+mapCenter.getY()));
        return phonePix;
    }
    void displayNewObj()
    {
        String str="";
        OverlayItem marker;
        ScreenPoint tileGlobalCenter;
        ScreenPoint tilePhoneCenter;
        List<Spot> spots;
        Spot spot;
        double zoomFactor;
        Tile.Bounds bounds,_bounds;
        boolean f1,f2;
        if(curFilter!= FiltersX.F0000)
        {
            curTiles = buildTiles();
            InfoTile tileInfo;
            Tile tile;
            for (String key : curTiles.keySet()) {
                str += key.toString() + ",";
                if (!loadedTiles.containsKey(key)) {
                    tile = curTiles.get(key);
                    loadedTiles.put(key, tile);
                    //tileInfo = allTiles.get(key);
                    tileInfo = TilesInfoData.findInfoTile(key);
                    if (tileInfo != null) {

                        if ((tileInfo.getChildSpots(curFilter).size() > 0) && tileInfo.name().equals(tile.name()))
                            showGrpOrChildSpot(tileInfo, curFilter);
                        else
                            showSpots(tileInfo, curFilter);
                    }
                }
            }
        }
        Log.d(TAG,"curTiles: "+str);

    }
    class btnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            Log.d(TAG,"button onClick!!!");

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
                loadedTiles.clear();
                setCurFilter();
                overlay.clearOverlayItems();
                displayNewObj();
                mapController.notifyRepaint();
                return true;
            }
            if(event.getAction()==MotionEvent.ACTION_UP) {

                btn.performClick();
                return true;
            }

            return true;
        }
    }
    */
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
            isFavorite = b;
            btn = (ImageButton) getView().findViewById(R.id.map_btn_star);
            btn.setPressed(b);
        }
        if(buttonType == Buttons.ALL || buttonType == Buttons.COURT) {
            isCourts = b;
            btn = (ImageButton) getView().findViewById(R.id.map_btn_court);
            btn.setPressed(b);
        }
    }
    /*
    void showSpots(InfoTile infoTile, FiltersX filter)
    {
        List<Integer> spots = infoTile.spots();
        Spot spot;

        OverlayItem marker;
        for(int i=0; i<spots.size(); i++){
            spot = TilesInfoData.allSpots().get(spots.get(i));
            if(InfoTile.isAppropriate(spot, filter)) {
                marker = new OverlayItem(spot.geoCoord(), res.getDrawable(spot.getMarkerImg(filter)));
                //marker = new OverlayItem(spot.geoCoord(), res.getDrawable(R.drawable.baloon_blue));
                marker.setOffsetY(MARKER_OFFSET);
                // Create a balloon model for the object
                BalloonItem balloonMarker = new BalloonItem(this.getActivity(), marker.getGeoPoint());
                balloonMarker.setText(spot.name()+spot.getDescription(filter));
                //balloonMarker.setText(String.valueOf(tile.name()));
//        // Add the balloon model to the object
                marker.setBalloonItem(balloonMarker);
                // Add the object to the layer
                overlay.addOverlayItem(marker);
            }
        }
    }
    void showGrpOrChildSpot(InfoTile tileInfo,FiltersX filter)
    {
        OverlayItem marker;
        Spot spot;
        List<Integer> spots = tileInfo.getChildSpots(filter);
        if(spots.size()>1) {
            //marker = new OverlayItem(tileInfo.getAvrPoint(filter), res.getDrawable(R.drawable.baloon_blue));
            marker = new OverlayItem(tileInfo.getAvrPoint(filter), res.getDrawable(tileInfo.getDrawableMarker(filter)));
            marker.setOffsetY(MARKER_OFFSET);
            // Create a balloon model for the object
            BalloonItem balloonMarker = new BalloonItem(this.getActivity(), marker.getGeoPoint());
            balloonMarker.setText(tileInfo.getNumSpotToString("спот", filter)+tileInfo.getDescription(filter));
            //balloonMarker.setText(String.valueOf(tile.name()));
//        // Add the balloon model to the object
            marker.setBalloonItem(balloonMarker);
            // Add the object to the layer
            overlay.addOverlayItem(marker);
        }
        else if(spots.size()==1) {
            spot = TilesInfoData.allSpots().get(spots.get(0));
            if (InfoTile.isAppropriate(spot, filter)) {
                marker = new OverlayItem(spot.geoCoord(), res.getDrawable(spot.getMarkerImg(filter)));
                //marker = new OverlayItem(spot.geoCoord(), res.getDrawable(R.drawable.baloon_blue));
                marker.setOffsetY(MARKER_OFFSET);
                // Create a balloon model for the object
                BalloonItem balloonMarker = new BalloonItem(this.getActivity(), marker.getGeoPoint());
                balloonMarker.setText(spot.name()+spot.getDescription(filter));
                //balloonMarker.setText(String.valueOf(tile.name()));
//        // Add the balloon model to the object
                marker.setBalloonItem(balloonMarker);
                // Add the object to the layer
                overlay.addOverlayItem(marker);
            }
        }
    }
    */
    private void setCurFilter()
    {
        if(isCourts) {
            curFilter = FiltersX.Fxx1x;
            return;
        }
        if(isPartners)
        {
            if(isCoaches) {
                if(isFavorite)
                {
                    curFilter = FiltersX.F1101;
                    return;
                }
                curFilter = FiltersX.F1100;
                return;
            }
            else if(isFavorite)
            {
                curFilter = FiltersX.F1001;
                return;
            }
            curFilter = FiltersX.F1000;
        }
        else if(isCoaches)
        {
            if(isFavorite)
            {
                curFilter = FiltersX.F0101;
                return;
            }
            curFilter = FiltersX.F0100;
        }
        else if(isFavorite)
            curFilter = FiltersX.F0001;
        else
            curFilter = FiltersX.F0000;

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



}

