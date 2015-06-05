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
        Spot spot;
        Partner partner;
        Coach coach;
        spot = new Spot(0,new Coordinates(55.778234, 37.588539),"Комета","Адрес");
        coach = new Coach(0,"Петя","Иванов",33);
        spot.set_favorite(true);
        spot.coaches().add(coach);
        _allSpots.put(spot.id(), spot);

        spot = new Spot(1,new Coordinates(55.796051, 37.537766),"Теннисный клуб ЦСКА","Адрес");
        partner = new Partner(0,"Вася","Клюев",44);
        spot.partners().add(partner);
        partner = new Partner(2,"Петя","Клюев",42);
        spot.partners().add(partner);
        coach = new Coach(1,"Иван","Мартирасян",30);
        spot.coaches().add(coach);
        spot.set_favorite(true);
        _allSpots.put(spot.id(), spot);

        spot = new Spot(2,new Coordinates(55.795504, 37.541117),"Европейская школа Тенниса","Адрес");
        spot.set_favorite(true);
        _allSpots.put(spot.id(), spot);

        spot = new Spot(3,new Coordinates(55.792503, 37.536984),"Европейская школа Тенниса","Адрес");
        _allSpots.put(spot.id(), spot);

        spot = new Spot(4,new Coordinates(55.804162, 37.561679),"Теннисенок","Адрес");
        _allSpots.put(spot.id(), spot);

        spot = new Spot(5,new Coordinates(55.768345, 37.693669),"Планета тенниса","Адрес");
        _allSpots.put(spot.id(), spot);

        spot = new Spot(6,new Coordinates(55.715099, 37.555023),"TennisVIP","Адрес");
        _allSpots.put(spot.id(), spot);
    }
    public static void setSpotFavorite(int idSpot, boolean isFavorite)
    {
        Spot spot = _allSpots.get(idSpot);
        if(spot!=null)
            spot.set_favorite(isFavorite);
    }
}
