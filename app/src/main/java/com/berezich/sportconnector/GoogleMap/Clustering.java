package com.berezich.sportconnector.GoogleMap;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.test.ActivityUnitTestCase;

import com.berezich.sportconnector.R;
import com.berezich.sportconnector.SportObjects.InfoTile;
import com.berezich.sportconnector.SportObjects.Spot;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by berezkin on 14.05.2015.
 */
public class Clustering {
    public static ClusterManager<AbstractMarker> clusterManager;
    private static GoogleMapFragment gmapFragment;
    public static void initClusterManager(Context context,GoogleMap gMap,GoogleMapFragment gmapFragment) {
        clusterManager = new ClusterManager<AbstractMarker>(context, gMap);
        clusterManager.setRenderer(new OwnIconRendered(context, gMap,clusterManager));
        Clustering.gmapFragment = gmapFragment;
    }
    public static void addAllSpots(HashMap<Integer, Spot> spots, GoogleMapFragment.FiltersX filter)
    {
        SpotMarker spotMarker;
        Set<Integer> keys = spots.keySet();
        Spot spot;

        // Loop over String keys.
        for (Integer key : keys) {
            spot = spots.get(key);
            spotMarker = new SpotMarker(spot.name(),spot.geoCoord().lat(),spot.geoCoord().longt(),
                    spot.partners().size(),spot.coaches().size(),spot.favorite());
            if(spotMarker.isAppropriate(filter))
                clusterManager.addItem(spotMarker);
        }
    }
    public static class OwnIconRendered extends DefaultClusterRenderer<AbstractMarker> {

        public OwnIconRendered(Context context, GoogleMap map,
                               ClusterManager<AbstractMarker> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(AbstractMarker item,
                                                   MarkerOptions markerOptions) {

            markerOptions.icon(((SpotMarker)item).getBitmap(gmapFragment.curFilter()));
            /*
            if(gmapFragment.curFilter()== GoogleMapFragment.FiltersX.F0000)
                markerOptions.icon(null);
            else if(gmapFragment.curFilter()== GoogleMapFragment.FiltersX.F1000)
                markerOptions.icon(BitmapDescriptorFactory.
                        fromResource(R.drawable.baloon_green));
            else
                markerOptions.icon(item.getMarker().getIcon());
                */
        }

    }

}
