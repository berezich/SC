package com.berezich.sportconnector.YaMap;

import com.berezich.sportconnector.SportObjects.InfoTile;
import com.berezich.sportconnector.SportObjects.Spot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.yandex.yandexmapkit.utils.GeoPoint;

/**
 * Created by Sashka on 29.04.2015.
 */
public class TilesInfoData {
    private static HashMap<String,InfoTile> _infoTiles = new HashMap<String,InfoTile>();
    private static final int MAX_COURT_LIMIT = 1;

    public static void getTilesFromCache()
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

        _infoTiles.clear();
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
    public static void addSpot(Spot spot, boolean isNew)
    {
        String tileName;
        InfoTile tile,nextTile;
        int level=2;
        List<Spot> spots, spotsToAdd = new ArrayList<Spot>();
        while(level<=Tile.MAX_ZOOM) {
            tileName = spot.tileName().substring(0,level);

            tile = _infoTiles.get(tileName);
            if (tile == null) {
                tile = new InfoTile(tileName);
                spots = tile.spots();
                spots.add(spot);
                tile.set_averagePoint(spot.geoCoord());
                _infoTiles.put(tile.name(),tile);
                break;
            }
            else
            {
                if(tile.infoChildSpots().size()==0 && tile.spots().size()<MAX_COURT_LIMIT ||level==Tile.MAX_ZOOM){
                    spots = tile.spots();
                    spots.add(spot);
                    break;
                }
                if(tile.spots().size()>0) {
                    //tile.set_numChildesSpots(tile.spots().size());
                    tile.addInfoChildSpots(tile.spots());
                    tile.set_averagePoint(InfoTile.calcAveragePoint(tile.spots()));
                    //tile.addPoint(spot.geoCoord());
                    //tile.set_numChildesSpots(tile.numChildesSpots()+1);
                    tile.addInfoChildSpot(spot);
                    spotsToAdd.addAll(tile.spots());
                    tile.spots().clear();
                }
                else if(isNew) {
                    //tile.addPoint(spot.geoCoord());
                    //tile.set_numChildesSpots(tile.numChildesSpots() + 1);
                    tile.addInfoChildSpot(spot);
                }
            }
            level++;
        }
        for(int i=0; i<spotsToAdd.size(); i++)
            addSpot(spotsToAdd.get(i),false);
    }
    public static InfoTile findInfoTile(String key){
        int level = key.length();
        InfoTile tileInfo;
        while (level>=2)
            if((tileInfo = _infoTiles.get(key))!=null)
                return tileInfo;
            else {
                level-=1;
                key = key.substring(0,level);
            }
        return null;
    }
    public static HashMap<String, InfoTile> infoTiles() {
        return _infoTiles;
    }
}
