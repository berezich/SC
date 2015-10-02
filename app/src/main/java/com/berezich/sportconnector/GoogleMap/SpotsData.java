package com.berezich.sportconnector.GoogleMap;

import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.berezich.sportconnector.backend.sportConnectorApi.model.UpdateSpotInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by berezkin on 14.05.2015.
 */
public class SpotsData {
    private static HashMap<Long, Spot> _allSpots = new HashMap<Long, Spot>();

    public static HashMap<Long, Spot> get_allSpots() {
        if(_allSpots.isEmpty())
            loadSpotsFromCache();
        return _allSpots;
    }

    public static void loadSpotsFromCache()
    {
        _allSpots = LocalDataManager.getAllSpots();
    }
    public static void saveSpotsToCache(List<Spot> spotLst)
    {
        Spot spot;
        _allSpots.clear();
        for (int i = 0; i <spotLst.size() ; i++) {
            spot = spotLst.get(i);
            LocalDataManager.initListsOfSpot(spot);
            _allSpots.put(spot.getId(),spot);
        }
        saveSpotsToCache();
    }
    public static void saveSpotsToCache()
    {
        LocalDataManager.saveAllSpots(_allSpots);
    }
    public static void setSpotUpdatesToCache(List<UpdateSpotInfo> spotUpdates)
    {
        Spot spot;
        Long spotId;
        for (int i = 0; i <spotUpdates.size() ; i++) {
            spotId = spotUpdates.get(i).getId();
            spot = spotUpdates.get(i).getSpot();
            if(spot!=null) {
                LocalDataManager.initListsOfSpot(spot);
                _allSpots.put(spotId, spot);
            }
            else
                _allSpots.remove(spotId);
        }
        LocalDataManager.setUpdateSpots(spotUpdates);
    }

    public static void setSpotFavorite(Long idSpot, boolean isFavorite)
    {
        Spot spot = _allSpots.get(idSpot);
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        Long personId;
        List<Long> favoriteSpotLst = null;
        List<Long> personLst = null;

        if(spot!=null && myPersonInfo!=null) {
            personId = myPersonInfo.getId();

            favoriteSpotLst = myPersonInfo.getFavoriteSpotIdLst();
            if(favoriteSpotLst!=null)
                if(isFavorite && !favoriteSpotLst.contains(idSpot))
                    favoriteSpotLst.add(idSpot);
                else if(!isFavorite && favoriteSpotLst.contains(idSpot))
                    favoriteSpotLst.remove(idSpot);

            if(myPersonInfo.getType().equals("COACH"))
                personLst = spot.getCoachLst();
            else if(myPersonInfo.getType().equals("PARTNER"))
                personLst = spot.getPartnerLst();

            if(personLst!=null)
                if(isFavorite && !personLst.contains(personId))
                    personLst.add(personId);
                else if(!isFavorite && personLst.contains(personId))
                    personLst.remove(personId);
            updateSpot_map_DB(spot);
        }
    }
    public static void updateSpot_map_DB(Spot spot)
    {
        Long id = spot.getId();
        _allSpots.put(id,spot);
        LocalDataManager.updateSpot(id, spot, null);
    }
    public static List<Long> getCoachIdsWithoutMe(Spot spot)
    {
        List<Long> personIds = new ArrayList<Long>(),coachLst;
        Person myPersonInfo;
        if((coachLst = spot.getCoachLst())!=null) {
            personIds.addAll(coachLst);
            if ((myPersonInfo = LocalDataManager.getMyPersonInfo())!=null && myPersonInfo.getType().equals("COACH"))
                personIds.remove(myPersonInfo.getId());
        }
        return personIds;
    }
    public static List<Long> getPartnerIdsWithoutMe(Spot spot)
    {
        List<Long> personIds = new ArrayList<Long>(), partnerLst;
        Person myPersonInfo;
        if((partnerLst = spot.getPartnerLst())!=null) {
            personIds.addAll(partnerLst);
            if ((myPersonInfo = LocalDataManager.getMyPersonInfo())!=null && myPersonInfo.getType().equals("PARTNER"))
                personIds.remove(myPersonInfo.getId());
        }
        return personIds;
    }
}
