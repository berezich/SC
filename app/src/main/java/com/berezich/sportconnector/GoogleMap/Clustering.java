package com.berezich.sportconnector.GoogleMap;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by berezkin on 14.05.2015.
 */
public class Clustering {
    private static final String TAG = "ClusteringGMap";
    public static ClusterManager<AbstractMarker> clusterManager;
    private static GoogleMapFragment gmapFragment;
    private static AbstractMarker chosenMarker;
    private  static Cluster<AbstractMarker> chosenCluster;

    public static void initClusterManager(Context context,GoogleMap gMap,GoogleMapFragment gmapFragment) {
        clusterManager = new ClusterManager<AbstractMarker>(context, gMap);
        clusterManager.setRenderer(new OwnIconRendered(context, gMap,clusterManager));
        Clustering.gmapFragment = gmapFragment;
        clusterManager.setOnClusterClickListener(new CustomClusterClickListener());
        clusterManager.setOnClusterItemClickListener(new CustomClusterItemClickListener());
        clusterManager.setOnClusterItemInfoWindowClickListener(new CustomClusterItemInfoWindowClickListener());
    }
    public static void addAllSpots(HashMap<Long, Spot> spots, GoogleMapFragment.FiltersX filter)
    {
        SpotMarker spotMarker;
        Set<Long> keys = spots.keySet();
        Spot spot;
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        clusterManager.clearItems();
        // Loop over String keys.
        for (Long key : keys) {
            spot = spots.get(key);

            spotMarker = new SpotMarker(spot.getId(),spot.getName(),spot.getCoords().getLat(),spot.getCoords().getLongt(),
                    SpotsData.getPartnerIdsWithoutMe(spot).size(),SpotsData.getCoachIdsWithoutMe(spot).size(), LocalDataManager.isMyFavoriteSpot(spot));
            if(spotMarker.isAppropriate(filter)) {
                spotMarker.setSpotIcon(filter);
                spotMarker.setDescription(filter);
                clusterManager.addItem(spotMarker);
            }
        }
        clusterManager.cluster();

    }
    public static class OwnIconRendered extends DefaultClusterRenderer<AbstractMarker> {

        private IconGenerator iconFactory;
        public OwnIconRendered(Context context, GoogleMap map,
                               ClusterManager<AbstractMarker> clusterManager) {
            super(context, map, clusterManager);
            iconFactory = new IconGenerator(context);
            iconFactory.setTextAppearance(R.style.ClusterIcon_TextAppearance);
        }

        @Override
        protected void onBeforeClusterItemRendered(AbstractMarker item,
                                                   MarkerOptions markerOptions) {

            SpotMarker spot =  ((SpotMarker)item);



            markerOptions.icon(spot.getMarker().getIcon());
            markerOptions.title(spot.description());
            markerOptions.anchor((float) 0.4, (float) 17 / 20);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<AbstractMarker> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).


            //iconFactory.setBackground(gmapFragment.getResources().getDrawable(R.drawable.gmap_cluster_green_red_purple));
            iconFactory.setBackground(gmapFragment.getResources().getDrawable(getDrawableClusterMarker(cluster, gmapFragment.curFilter())));
            iconFactory.setContentPadding(20,10,0,0);

            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(String.valueOf(cluster.getSize())));
            markerOptions.icon(descriptor).anchor((float) 0.5, (float) 0.5);

            //markerOptions.icon(BitmapDescriptorFactory.fromResource(getDrawableMarker(cluster,gmapFragment.curFilter())));
            //markerOptions.title(String.valueOf(cluster.getItems().size()));
        }
        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }

    }
    public static class CustomClusterItemClickListener implements ClusterManager.OnClusterItemClickListener<AbstractMarker>{
        @Override
        public boolean onClusterItemClick(AbstractMarker item) {
            chosenMarker = item;
            chosenCluster = null;
            return false;
        }
    }

    public static class CustomClusterClickListener implements ClusterManager.OnClusterClickListener<AbstractMarker>
    {
        @Override
        public boolean onClusterClick(Cluster<AbstractMarker> cluster) {
            chosenCluster = cluster;
            chosenMarker = null;
            return true;
        }
    }
    //for group of markers
    private static Integer getDrawableClusterMarker(Cluster<AbstractMarker> cluster, GoogleMapFragment.FiltersX filter)
    {
        int numPartners = 0, numSpots =0 , numCoaches = 0, numFavorites=0;
        
        for (AbstractMarker p : cluster.getItems()) {
            numSpots++;
            numCoaches+= ((SpotMarker) p).numCoaches();
            numPartners+= ((SpotMarker) p).numPartners();
            if(((SpotMarker) p).isFavorite())
                numFavorites++;
        }
        if(numFavorites>0 && (filter == GoogleMapFragment.FiltersX.F0001||
                filter == GoogleMapFragment.FiltersX.F1001 && numPartners==0||
                filter == GoogleMapFragment.FiltersX.F0101 && numCoaches==0||
                (filter == GoogleMapFragment.FiltersX.F1101|| filter == GoogleMapFragment.FiltersX.Fxx1x) && numCoaches==0&&numPartners==0))
            return  R.drawable.gmap_cluster_red;
        if(numPartners>0 && (filter == GoogleMapFragment.FiltersX.F1000||
                filter == GoogleMapFragment.FiltersX.F1001 && numFavorites==0||
                filter == GoogleMapFragment.FiltersX.F1100 && numCoaches==0||
                (filter == GoogleMapFragment.FiltersX.F1101|| filter == GoogleMapFragment.FiltersX.Fxx1x) && numCoaches==0&& numFavorites==0))
            return  R.drawable.gmap_cluster_purple;
        if(numCoaches>0 && (filter == GoogleMapFragment.FiltersX.F0100||
                filter == GoogleMapFragment.FiltersX.F0101 && numFavorites==0||
                filter == GoogleMapFragment.FiltersX.F1100 && numPartners==0||
                (filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x)&& numPartners==0&& numFavorites==0))
            return  R.drawable.gmap_cluster_green;

        if(numPartners>0 && numFavorites>0 && (filter == GoogleMapFragment.FiltersX.F1001 || (filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && numCoaches==0))
            return  R.drawable.gmap_cluster_red_purple;
        //return  R.drawable.baloon_red;

        if(numPartners>0 && numCoaches>0 && (filter == GoogleMapFragment.FiltersX.F1100 || (filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && numFavorites==0))
            return  R.drawable.gmap_cluster_green_purple;
        //return  R.drawable.baloon_green;

        if(numCoaches>0 && numFavorites>0 && (filter == GoogleMapFragment.FiltersX.F0101 || (filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && numPartners==0))
            return  R.drawable.gmap_cluster_green_red;
        //return  R.drawable.baloon_green;

        if((filter == GoogleMapFragment.FiltersX.F1101 || filter == GoogleMapFragment.FiltersX.Fxx1x) && numPartners>0 && numCoaches>0 && numFavorites>0 )
            return  R.drawable.gmap_cluster_green_red_purple;
        //return  R.drawable.baloon_red;


        return  R.drawable.gmap_cluster_blue;
    }
    public static String pluralPostfix(int num)
    {
        if(num>=10 && num<=19)
            return "ов";
        int mod = num%10;
        switch (mod)
        {
            case 1:
                return "";
            case 2:
            case 3:
            case 4:
                return "a";
            default:
                return "ов";
        }
    }
    public static class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
    // Setting a custom info window adapter for the google map


        // Use default InfoWindow frame
        @Override
        public View getInfoWindow(Marker arg0) {
            return null;
        }

        // Defines the contents of the InfoWindow
        @Override
        public View getInfoContents(Marker arg0) {

            // Getting view from the layout file info_window_layout
            View v = gmapFragment.getActivity().getLayoutInflater().inflate(R.layout.info_window, null);
            if(chosenMarker!=null) {
                String title = arg0.getTitle();
                String[] fields = title.split("\n");
                TextView txtView;
                if (fields.length > 0) {
                    txtView = (TextView) v.findViewById(R.id.infoWindow_name);
                    txtView.setText(fields[0]);
                    txtView.setVisibility(View.VISIBLE);
                }
                if (fields.length > 1) {
                    txtView = (TextView) v.findViewById(R.id.infoWindow_ln1);
                    txtView.setText(fields[1]);
                    txtView.setVisibility(View.VISIBLE);
                }
                if (fields.length > 2) {
                    txtView = (TextView) v.findViewById(R.id.infoWindow_ln2);
                    txtView.setText(fields[2]);
                    txtView.setVisibility(View.VISIBLE);
                }
                if (fields.length > 3) {
                    txtView = (TextView) v.findViewById(R.id.infoWindow_ln3);
                    txtView.setText(fields[3]);
                    txtView.setVisibility(View.VISIBLE);
                }
            }
            else
            {

            }
            return v;

        }
    }
    public static class CustomClusterItemInfoWindowClickListener implements ClusterManager.OnClusterItemInfoWindowClickListener<AbstractMarker>
    {
        @Override
        public void onClusterItemInfoWindowClick(AbstractMarker item) {
            SpotMarker spotMarker = (SpotMarker) item;
            gmapFragment.setSelectMarker(spotMarker);
            gmapFragment.setCurZoom(gmapFragment.map().getCameraPosition().zoom);
            GoogleMapFragment.listener.onInfoWindowClickGMF(spotMarker.id());
        }
    }
}
