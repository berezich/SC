package com.berezich.sportconnector.YaMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.MainFragment.Filters;
import com.berezich.sportconnector.SportObjects.InfoTile;
import com.berezich.sportconnector.SportObjects.Spot;
import com.berezich.sportconnector.YaMap.Tile;
import com.berezich.sportconnector.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.OverlayManager;
import ru.yandex.yandexmapkit.map.MapEvent;
import ru.yandex.yandexmapkit.map.OnMapListener;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.overlay.balloon.BalloonItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

/**
 * Created by berezkin on 17.04.2015.
 */
public class YaMapFragment extends Fragment implements OnMapListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "YaMapFragment";
    private final int MARKER_OFFSET = 50;
    private MapView mapView;
    private static MapController mapController;
    private OverlayManager overlayManager;
    private Overlay overlay;
    private Resources res;
    private boolean isCourts=false;
    private boolean isCoaches=false;
    private boolean isPartners=false;
    private boolean isFavorite=false;

    //список tiles уже отисованых на карте при данном масштабе
    private HashMap<String,Tile> loadedTiles = new HashMap<String,Tile>();
    //список tiles в зоне видимости
    private HashMap<String,Tile> curTiles = new HashMap<String,Tile>();


    public YaMapFragment setArgs(int sectionNumber, Filters filter) {

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        if(filter == Filters.COUCH)
            isCoaches = true;
        if(filter == Filters.SPARRING_PARTNERS)
            isPartners = true;
        if(filter == Filters.COURT)
            isCourts = true;

        return this;
    }

    public YaMapFragment() {

        TilesInfoData.getTilesFromCache();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                if(tileInfo!=null ) {
                    if((tileInfo.infoChildSpots().size()>0)&&tileInfo.name().equals(tile.name()))
                        showGrpSpot(tileInfo);
                    else
                        showSpots(tileInfo);
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
            loadedTiles.clear();
            displayNewObj();
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
                btn.setPressed(!btn.isPressed());
                if(v.getId() == R.id.map_btn_coach)
                switch (v.getId())
                {
                    case R.id.map_btn_coach:
                        isCoaches = !isCoaches;
                        break;
                    case R.id.map_btn_partner:
                        isPartners = !isPartners;
                        break;
                    case R.id.map_btn_court:
                        isCourts = !isCourts;
                        break;
                    case R.id.map_btn_star:
                        isFavorite = !isFavorite;
                        break;
                }

                return true;
            }
            if(event.getAction()==MotionEvent.ACTION_UP) {
                btn.performClick();
                return true;
            }

            return false;
        }
    }
    void showSpots(InfoTile infoTile)
    {
        List<Spot> spots = infoTile.spots();
        Spot spot;
        OverlayItem marker;
        for(int i=0; i<spots.size(); i++){
            spot = spots.get(i);
            marker = new OverlayItem(spot.geoCoord(), res.getDrawable(R.drawable.baloon_purple));
            marker.setOffsetY(MARKER_OFFSET);
            // Create a balloon model for the object
            BalloonItem balloonMarker = new BalloonItem(this.getActivity(), marker.getGeoPoint());
            balloonMarker.setText(spot.name());
            //balloonMarker.setText(String.valueOf(tile.name()));
//        // Add the balloon model to the object
            marker.setBalloonItem(balloonMarker);
            // Add the object to the layer
            overlay.addOverlayItem(marker);
        }
    }
    void showGrpSpot(InfoTile tileInfo)
    {
        OverlayItem marker;
        marker = new OverlayItem(tileInfo.averagePoint(), res.getDrawable(R.drawable.baloon_purple));
        marker.setOffsetY(MARKER_OFFSET);
        // Create a balloon model for the object
        BalloonItem balloonMarker = new BalloonItem(this.getActivity(), marker.getGeoPoint());
        balloonMarker.setText(tileInfo.getNumSpotToString("спот"));
        //balloonMarker.setText(String.valueOf(tile.name()));
//        // Add the balloon model to the object
        marker.setBalloonItem(balloonMarker);
        // Add the object to the layer
        overlay.addOverlayItem(marker);
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
