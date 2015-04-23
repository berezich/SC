package com.berezich.sportconnector.YaMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
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
import com.berezich.sportconnector.R;

import java.util.HashMap;
import java.util.List;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.map.MapEvent;
import ru.yandex.yandexmapkit.map.OnMapListener;
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
    private static Filters _filter;
    private static final String TAG = "YaMapFragment";
    private MapView mapView;
    private MapController mapController;

    //список tiles уже отисованых на карте при данном масштабе
    private HashMap<String,Tile> loadedTiles = new HashMap<String,Tile>();
    //список tiles показываемых на экране
    private HashMap<String,Tile> curTiles = new HashMap<String,Tile>();

    public YaMapFragment setArgs(int sectionNumber, Filters filter) {

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        _filter = filter;
        return this;
    }

    public YaMapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_yamap, container, false);
        mapView = (MapView)  rootView.findViewById(R.id.map);
        mapView.showBuiltInScreenButtons(true);
        mapView.showJamsButton(false);
        MapController mapController = mapView.getMapController();
        mapController.addMapListener(this);
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
        mapController = mapView.getMapController();
        switch (mapEvent.getMsg()) {
            case MapEvent.MSG_SCALE_BEGIN:
                //textView.setText("MSG_SCALE_BEGIN");
                break;
            case MapEvent.MSG_SCALE_MOVE:
                //textView.setText("MSG_SCALE_MOVE");
                break;
            case MapEvent.MSG_SCALE_END:
                //textView.setText("MSG_SCALE_END");
                Log.d(TAG, "mapCenter = " + mapController.getMapCenter().toString());
                Log.d(TAG, "mapHeight = " + mapController.getHeight());
                Log.d(TAG, "mapWidth = " + mapController.getWidth());
                break;

            case MapEvent.MSG_ZOOM_BEGIN:
                //textView.setText("MSG_ZOOM_BEGIN");
                break;
            case MapEvent.MSG_ZOOM_MOVE:
                //textView.setText("MSG_ZOOM_MOVE");
                break;
            case MapEvent.MSG_ZOOM_END:
                Log.d(TAG, "ZOOM = " + mapController.getZoomCurrent());
                loadedTiles.clear();
                curTiles = visibleTileList();
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
                curTiles = visibleTileList();
                break;
            default:
                //textView.setText("MSG_EMPTY");
                break;
        }
    }

    //получаем таблицу видимых tiles
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private HashMap<String,Tile> visibleTileList()
    {

        HashMap<String,Tile> tiles = new HashMap<String,Tile>();
        Size size = new Size(mapController.getWidth(),mapController.getHeight());
        ScreenPoint center = mapController.getScreenPoint(mapController.getMapCenter());
        double mapZoom = mapController.getZoomCurrent()+1;
        double zoomFactor = Math.pow(2,-mapZoom);
        ScreenPoint pixelCenter = new ScreenPoint(center.getX()*(float)zoomFactor, (center.getY()*(float)zoomFactor));
        Size pixelSize = new Size((int)(size.getWidth()*zoomFactor),(int)(size.getHeight()*zoomFactor));
        int tileSize = (int)(256 * zoomFactor);
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
        for (int x = 0; xfill; x += tileSize) {
            for (int y = 0; ; y += tileSize) {
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
}
