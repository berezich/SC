package com.berezich.sportconnector.GoogleMap;

import android.content.Context;

import com.berezich.sportconnector.SportObjects.InfoTile;
import com.berezich.sportconnector.SportObjects.Spot;
import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by berezkin on 14.05.2015.
 */
public class Clustering {
    public static ClusterManager<AbstractMarker> clusterManager;
    public static void initClusterManager(Context context,GoogleMap gMap) {
        clusterManager = new ClusterManager<AbstractMarker>(context, gMap);
    }
    public static void addAllSpots(HashMap<Integer, Spot> spots)
    {
        SpotMarker spotMarker;
        Set<Integer> keys = spots.keySet();
        Spot spot;

        // Loop over String keys.
        for (Integer key : keys) {
            spot = spots.get(key);
            spotMarker = new SpotMarker(spot.name(),spot.geoCoord().lat(),spot.geoCoord().longt());
            clusterManager.addItem(spotMarker);
        }
    }
}
