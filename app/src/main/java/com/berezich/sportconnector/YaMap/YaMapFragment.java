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
    private final int MAX_COURT_LIMIT = 1;
    private static Filters _filter;
    private MapView mapView;
    private MapController mapController;
    private OverlayManager overlayManager;
    private Overlay overlay;
    private Resources res;

    //список tiles уже отисованых на карте при данном масштабе
    private HashMap<String,Tile> loadedTiles = new HashMap<String,Tile>();
    //список tiles показываемых на экране
    private HashMap<String,Tile> curTiles = new HashMap<String,Tile>();
    private HashMap<String,InfoTile> infoTiles = new HashMap<String,InfoTile>();

    public YaMapFragment setArgs(int sectionNumber, Filters filter) {

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        _filter = filter;
        return this;
    }

    public YaMapFragment() {
        getTilesFromCache();
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
        btn.setPressed(_filter == Filters.COUCH);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_court);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());
        btn.setPressed(_filter == Filters.COURT);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_partner);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());
        btn.setPressed(_filter == Filters.SPARRING_PARTNERS);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_star);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());

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
                addNewObj();
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
                addNewObj();
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
                addNewObj();
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
            //ScreenPoint center = mapController.getScreenPoint(mapController.getMapCenter());
            ScreenPoint pixCenterGeo = mapController.getScreenPoint(new GeoPoint(0,0));
            //Log.d(TAG,"CENTER = "+center.getX()+" "+center.getY());
            double mapZoom = mapController.getZoomCurrent()+1;
            double zoomFactor = Math.pow(2,-mapZoom);
            //ScreenPoint pixelCenter = new ScreenPoint(center.getX()*(float)zoomFactor, (center.getY()*(float)zoomFactor));
            //Log.d(TAG,"PIX CENTER = "+pixelCenter.getX()+" "+pixelCenter.getY());
            Log.d(TAG,"PIX GEOCENTER = "+pixCenterGeo.getX()+" "+pixCenterGeo.getY());
            Size pixelSize = new Size(size.getWidth()*zoomFactor,size.getHeight()*zoomFactor);
            float tileSize = (float)(256 * zoomFactor);
            float mapSize = (float)(256*Math.pow(2,mapZoom));
            ScreenPoint tilePixCenter = new ScreenPoint((float)(mapSize*0.5),(float)(mapSize*0.5));
            ScreenPoint pixelCenter = new ScreenPoint(tilePixCenter.getX()-pixCenterGeo.getX()+(float)(mapController.getWidth()*0.5),tilePixCenter.getY()-pixCenterGeo.getY()+(float)(mapController.getHeight()*0.5));
            pixelCenter = new ScreenPoint(pixelCenter.getX()*(float)zoomFactor, (pixelCenter.getY()*(float)zoomFactor));
            Log.d(TAG,"PIX CENTER = "+pixelCenter.getX()+" "+pixelCenter.getY());
            //нам нужны пиксельные границы в пространстве нулевого зума расширенная до углов тайлов
            //Tile.Bounds pixelBounds = new Tile.Bounds(new ScreenPoint((float)Math.max(0,pixelCenter.getX() - pixelSize.getWidth() * .5), (float) Math.max(0,pixelCenter.getY() - pixelSize.getHeight() * .5)),new ScreenPoint((float) Math.min(256, pixelCenter.getX() + pixelSize.getWidth() * .5), (float)Math.min(256,pixelCenter.getY() + pixelSize.getHeight() * .5)));
            ScreenPoint pixelStart  = new ScreenPoint((float)Math.max(0,pixelCenter.getX() - pixelSize.getWidth() * .5), (float) Math.max(0,pixelCenter.getY() - pixelSize.getHeight() * .5));
            ScreenPoint pixelEnd  = new ScreenPoint((float) Math.min(256, pixelCenter.getX() + pixelSize.getWidth() * .5), (float)Math.min(256,pixelCenter.getY() + pixelSize.getHeight() * .5));
            double quadZoom = mapZoom;
            //quadZoom = mapZoom - this.zoomOffset,
            double quadFactor = Math.pow(2, -quadZoom);
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
    private ScreenPoint globalPxToPhonePx(ScreenPoint globalPix,double zoom)
    {
        float zoomFactor = (float)Math.pow(2,zoom);
        int mapPixSize = (int) (256*zoomFactor);
        ScreenPoint mapCenter = mapController.getScreenPoint(new GeoPoint(0,0));
        ScreenPoint phonePix = new ScreenPoint((float)(globalPix.getX()-mapPixSize*.5+mapCenter.getX()),(float)(globalPix.getY()-mapPixSize*.5+mapCenter.getY()));
        return phonePix;
    }
    void addNewObj()
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
                tileInfo = InfoTile.findInfoTile(key, infoTiles);
                if(tileInfo!=null ) {
                    f1 = tileInfo.numChildesSpots()>0;
                    f2 = tileInfo.name().equals(tile.name());
                    if(f1&&f2)
                    {
                    //if((f1 = (tileInfo.numChildesSpots()>0)) && (f2 = (tileInfo.name() == tile.name()))) {
                        // Create an object for the layer
                        zoomFactor = Math.pow(2, tile.name().length());
                        bounds = tile.bounds();
                        _bounds = new Tile.Bounds(new ScreenPoint((float) (bounds.p1().getX() * zoomFactor), (float) (bounds.p1().getY() * zoomFactor)), new ScreenPoint((float) (bounds.p2().getX() * zoomFactor), (float) (bounds.p2().getY() * zoomFactor)));
                        tileGlobalCenter = new ScreenPoint((float) ((_bounds.p1().getX() + _bounds.p2().getX()) * .5), (float) ((_bounds.p1().getY() + _bounds.p2().getY()) * .5));
                        tilePhoneCenter = globalPxToPhonePx(tileGlobalCenter, tile.name().length());

                        //marker = new OverlayItem(mapController.getGeoPoint(tilePhoneCenter), res.getDrawable(R.drawable.court_2));
                        marker = new OverlayItem(tileInfo.averagePoint(), res.getDrawable(R.drawable.court_2));

                        // Create a balloon model for the object
                        BalloonItem balloonMarker = new BalloonItem(this.getActivity(), marker.getGeoPoint());
                        balloonMarker.setText(tileInfo.getNumSpotToString("спот"));
                        //balloonMarker.setText(String.valueOf(tile.name()));
//        // Add the balloon model to the object
                        marker.setBalloonItem(balloonMarker);
                        // Add the object to the layer
                        overlay.addOverlayItem(marker);
                    }
                    else
                    {
                        if(!tileInfo.name().equals(tile.name())) {
                            int i;
                            i = 1 + 1;
                        }
                        spots = tileInfo.spots();
                        for(int i=0; i<spots.size(); i++){
                            spot = spots.get(i);
                            marker = new OverlayItem(spot.geoCoord(), res.getDrawable(R.drawable.court_2));

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
                btn.setPressed(!btn.isPressed());
                return true;
            }
            if(event.getAction()==MotionEvent.ACTION_UP) {
                btn.performClick();
                return true;
            }

            return false;
        }
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

    private void getTilesFromCache()
    {
        /*
        Tile tile = new Tile("00");
        tile.set_numChildesSpots(1);
        allTiles.put(tile.name(),tile);

        tile = new Tile("01");
        tile.set_numChildesSpots(2);
        allTiles.put(tile.name(),tile);

        tile = new Tile("02");
        tile.set_numChildesSpots(3);
        allTiles.put(tile.name(),tile);

        tile = new Tile("03");
        tile.set_numChildesSpots(4);
        allTiles.put(tile.name(),tile);

        tile = new Tile("20");
        tile.set_numChildesSpots(1);
        allTiles.put(tile.name(),tile);

        tile = new Tile("21");
        tile.set_numChildesSpots(2);
        allTiles.put(tile.name(),tile);

        tile = new Tile("22");
        tile.set_numChildesSpots(3);
        allTiles.put(tile.name(),tile);

        tile = new Tile("23");
        tile.set_numChildesSpots(4);
        allTiles.put(tile.name(),tile);
        */

        Spot spot;
        spot = new Spot(0,new GeoPoint(55.778234, 37.588539),"1203101010333012","Комета");
        addSpot(spot,true);
        spot = new Spot(1,new GeoPoint(55.796051, 37.537766),"1203101010330023","Теннисный клуб ЦСКА");
        addSpot(spot,true);
        spot = new Spot(2,new GeoPoint(55.795504, 37.541117),"1203101010330032","Европейская школа Тенниса");
        addSpot(spot,true);
        spot = new Spot(3,new GeoPoint(55.792503, 37.536984),"1203101010330201","Европейская школа Тенниса");
        addSpot(spot,true);

        spot = new Spot(4,new GeoPoint(55.804162, 37.561679),"1203101010330101","Теннисенок");
        addSpot(spot,true);

        spot = new Spot(5,new GeoPoint(55.768345, 37.693669),"1203101011223301","Планета тенниса");
        addSpot(spot,true);

        spot = new Spot(6,new GeoPoint(55.715099, 37.555023),"1203101012112302","TennisVIP");
        addSpot(spot,true);




        int i=1;
    }
    private void addSpot(Spot spot, boolean isNew)
    {
        String tileName;
        InfoTile tile,nextTile;
        int level=2;
        List<Spot> spots, spotsToAdd = new ArrayList<Spot>();
        while(level<=Tile.MAX_ZOOM) {
            tileName = spot.tileName().substring(0,level);

            tile = infoTiles.get(tileName);
            if (tile == null) {
                tile = new InfoTile(tileName);
                spots = tile.spots();
                spots.add(spot);
                tile.set_averagePoint(spot.geoCoord());
                infoTiles.put(tile.name(),tile);
                break;
            }
            else
            {
                if(tile.numChildesSpots()==0 && tile.spots().size()<MAX_COURT_LIMIT ||level==Tile.MAX_ZOOM){
                    spots = tile.spots();
                    spots.add(spot);
                    break;
                }
                if(tile.spots().size()>0) {
                    tile.set_numChildesSpots(tile.spots().size());
                    tile.set_averagePoint(InfoTile.calcAveragePoint(tile.spots()));
                    tile.addPoint(spot.geoCoord());
                    tile.set_numChildesSpots(tile.numChildesSpots()+1);
                    spotsToAdd.addAll(tile.spots());
                    tile.spots().clear();
                }
                else if(isNew) {
                    tile.addPoint(spot.geoCoord());
                    tile.set_numChildesSpots(tile.numChildesSpots() + 1);
                }
            }
            level++;
        }
        for(int i=0; i<spotsToAdd.size(); i++)
            addSpot(spotsToAdd.get(i),false);
    }


}
