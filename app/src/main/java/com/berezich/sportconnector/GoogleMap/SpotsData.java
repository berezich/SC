package com.berezich.sportconnector.GoogleMap;

import com.berezich.sportconnector.R;
import com.berezich.sportconnector.SportObjects.Coach;
import com.berezich.sportconnector.SportObjects.Coordinates;
import com.berezich.sportconnector.SportObjects.Partner;
import com.berezich.sportconnector.SportObjects.Spot;

import java.util.HashMap;
import java.util.List;

import static com.berezich.sportconnector.SportObjects.Spot.*;

/**
 * Created by berezkin on 14.05.2015.
 */
public class SpotsData {
    private static HashMap<Integer, Spot> _allSpots = new HashMap<Integer, Spot>();

    public static HashMap<Integer, Spot> get_allSpots() {
        return _allSpots;
    }
    public static void getSpotsFromCache()
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
        Partner partner;
        Coach coach;
        spot = new Spot(0,new Coordinates(55.778234, 37.588539),"Комета");
        coach = new Coach(0,"Петя","Иванов",33);
        spot.set_favorite(true);
        spot.coaches().add(coach);
        _allSpots.put(spot.id(), spot);

        spot = new Spot(1,new Coordinates(55.796051, 37.537766),"Теннисный клуб ЦСКА");
        partner = new Partner(0,"Вася","Клюев",44);
        spot.partners().add(partner);
        coach = new Coach(1,"Иван","Мартирасян",30);
        spot.coaches().add(coach);
        spot.set_favorite(true);
        _allSpots.put(spot.id(), spot);

        spot = new Spot(2,new Coordinates(55.795504, 37.541117),"Европейская школа Тенниса");
        spot.set_favorite(true);
        _allSpots.put(spot.id(), spot);

        spot = new Spot(3,new Coordinates(55.792503, 37.536984),"Европейская школа Тенниса");
        _allSpots.put(spot.id(), spot);

        spot = new Spot(4,new Coordinates(55.804162, 37.561679),"Теннисенок");
        _allSpots.put(spot.id(), spot);

        spot = new Spot(5,new Coordinates(55.768345, 37.693669),"Планета тенниса");
        _allSpots.put(spot.id(), spot);

        spot = new Spot(6,new Coordinates(55.715099, 37.555023),"TennisVIP");
        _allSpots.put(spot.id(), spot);
    }


    /*
    public String getDescription(GoogleMapFragment.FiltersX filter)
    {
        String description="";
        int num;
        if(filter == GoogleMapFragment.FiltersX.F1000 || filter == GoogleMapFragment.FiltersX.F1100 || filter == GoogleMapFragment.FiltersX.F1001 || filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x)
            if((num=_childSpots_1000.size())>0)
                description+= "\n "+String.valueOf(num)+" - спарринг партнер"+pluralPostfix(num);
        if(filter == GoogleMapFragment.FiltersX.F0100 || filter == GoogleMapFragment.FiltersX.F1100 || filter == GoogleMapFragment.FiltersX.F0101 || filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x)
            if((num=_childSpots_0100.size())>0)
                description+= "\n "+String.valueOf(num)+" - тренер"+pluralPostfix(num);
        if(filter == GoogleMapFragment.FiltersX.F0001 || filter == GoogleMapFragment.FiltersX.F1001 || filter == GoogleMapFragment.FiltersX.F0101 || filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x)
            if((num=_childSpots_0001.size())>0)
                description+= "\n "+String.valueOf(num)+((num==1)?" - мой спот":" - мои споты");
        return  description;
    }

    */
}
