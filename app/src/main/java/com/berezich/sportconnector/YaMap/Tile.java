package com.berezich.sportconnector.YaMap;

import com.berezich.sportconnector.SportObjects.Spot;

import java.util.List;

import ru.yandex.yandexmapkit.utils.ScreenPoint;

/**
 * Created by berezkin on 23.04.2015.
 */
public class Tile {

    public class Bounds
    {
        private ScreenPoint _p1;
        private ScreenPoint _p2;

        public Bounds(ScreenPoint _p1, ScreenPoint _p2) {
            this._p1 = _p1;
            this._p2 = _p2;
        }

        public ScreenPoint p1() {
            return _p1;
        }

        public ScreenPoint p2() {
            return _p2;
        }
    }
    private int _id;
    private String _name;
    private int _code;
    private List<Spot> _spots;
    private int _numChildesSpots;



    private Bounds _bounds;

    public Tile(int id, int _code, String _name, Bounds bounds) {
        _id = id;
        this._code = _code;
        this._name = _name;
        _bounds = bounds;
    }

    public Tile(ScreenPoint point, double zoom) {
        _id = 0;
        final int MAX_ZOOM = 17;
        Bounds bounds = new Bounds(new ScreenPoint(0,0), new ScreenPoint(256,256));
        String code = "";
        int binCode = 0;
        int xdel,ydel,path;
        zoom = Math.min(MAX_ZOOM, zoom);
        ScreenPoint center;
        for (int i = 0, maxZoom = MAX_ZOOM; i < zoom; i++, maxZoom--) {
            //центр ноды
            center = new ScreenPoint((float)((bounds.p1().getX()+bounds.p2().getX())*0.5),(float)((bounds.p1().getY()+bounds.p2().getY())*0.5));

            xdel = point.getX() > center.getX() ? 1 : 0;
            ydel = point.getY() > center.getY() ? 2 : 0;
            //меняем границы, переходя в ребенка
            if (xdel>0)
                bounds.p1().setX(center.getX());
            else
                bounds.p2().setX(center.getX());

            if (ydel>0)
                bounds.p1().setY(center.getY());
            else
                bounds.p2().setY(center.getY());

            path = xdel + ydel;
            code += String.valueOf(path);
            binCode |= path << (2 * maxZoom);

            _code = binCode;
            _name = code;
        }
        _bounds = bounds;
    }

    public String name() {
        return _name;
    }

    public int code() {
        return _code;
    }

    public int numChildesSpots() {
        return _numChildesSpots;
    }
    public Bounds bounds() {
        return _bounds;
    }
}
