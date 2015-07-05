package com.berezich.sportconnector.GoogleMap;


/**
 * Created by berezkin on 23.04.2015.
 */
public class Tile {
    /*
    public static final int MAX_ZOOM = 16;
    final String TAB = "YaMap.Tile";
    public static class Bounds
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
    private String _name="";
    private int _code=0;
    private List<Spot> _spots = new ArrayList<Spot>();
    private int _numChildesSpots=0;



    private Bounds _bounds;

    public Tile( int _code, String _name, Bounds bounds) {
        this._code = _code;
        this._name = _name;
        _bounds = bounds;
    }

    public Tile(String name)
    {
        _name = name;
        while(name.startsWith("0"))
            name = name.substring(1);

        try {
            int len,i=0;
            while ((len = name.length())>1) {
                this._code += Math.pow(2, i) * Integer.parseInt(String.valueOf(name.charAt(len-1)));
                name = name.substring(0,len-2);
                i++;
            }
            if(name.length()==1)
            this._code += Math.pow(2, i) * Integer.parseInt(String.valueOf(name.charAt(len-1)));
        }
        catch(NumberFormatException ex){
            _code = 0;
            Log.d(TAB,ex.getMessage());
            Log.d(TAB,ex.getStackTrace().toString());
        }

        //_bounds = codeToRegion(_code,_name.length());
        //not used
        _bounds = new Bounds(new ScreenPoint(0,0),new ScreenPoint(0,0));
    }

    public Tile(ScreenPoint point, double zoom) {

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

    private Bounds codeToRegion(int bincode, int length)
    {
        double zoomFactor = Math.pow(2,length);
        //Bounds bounds = new Bounds(new ScreenPoint(0,0),new ScreenPoint((float)(256*zoomFactor),(float)(256*zoomFactor)));
        Bounds bounds = new Bounds(new ScreenPoint(0,0),new ScreenPoint(256,256));
        ScreenPoint center;
        int zcode,xdel,ydel;
        for (int i = 0, maxZoom = MAX_ZOOM; i < length; i++, maxZoom--) {
            //центр ноды
            center = new ScreenPoint((float)((bounds.p1().getX() + bounds.p2().getX()) * 0.5),(float)((bounds.p1().getY() + bounds.p2().getY()) * 0.5));
            zcode = 3 & (bincode >> (2 * maxZoom));
            xdel = zcode & 1;
            ydel = zcode & 2;
            //меняем границы, переходя в ребенка
            if (xdel!=0)
                bounds.p1().setX(center.getX());
            else
                bounds.p2().setX(center.getX());

            if (ydel!=0)
                bounds.p1().setY(center.getY());
            else
                bounds.p2().setY(center.getY());
        }
        return bounds;
    }

    public ScreenPoint getPixCenter()
    {
        double zoomFactor = Math.pow(2, _name.length());
        Bounds bounds = new Tile.Bounds(new ScreenPoint((float) (_bounds.p1().getX() * zoomFactor), (float) (_bounds.p1().getY() * zoomFactor)), new ScreenPoint((float) (_bounds.p2().getX() * zoomFactor), (float) (_bounds.p2().getY() * zoomFactor)));
        ScreenPoint tileGlobalCenter = new ScreenPoint((float) ((bounds.p1().getX() + bounds.p2().getX()) * .5), (float) ((bounds.p1().getY() + bounds.p2().getY()) * .5));
        return globalPxToPhonePx(tileGlobalCenter, _name.length());
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

    public void set_numChildesSpots(int _numChildesSpots) {
        this._numChildesSpots = _numChildesSpots;
    }

    public List<Spot> spots() {
        return _spots;
    }

    public Bounds bounds() {
        return _bounds;
    }
    */
}
