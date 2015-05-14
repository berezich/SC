package com.berezich.sportconnector.GoogleMap;

import com.berezich.sportconnector.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by berezkin on 14.05.2015.
 */
public class SpotMarker extends AbstractMarker {
    private static BitmapDescriptor shopIcon = null;

    private String description;

    public SpotMarker(String description,
                       double latitude, double longitude) {
        super(latitude, longitude);
        setDescription(description);
        setBitmapDescriptor();
        setMarker(new MarkerOptions()
                .position(new LatLng(latitude(), longitude()))
                .title("")
                .icon(shopIcon));
    }

    public static void setBitmapDescriptor() {
        /*if (shopIcon == null)
            shopIcon = BitmapDescriptorFactory.
                    fromResource(R.drawable.trademarker);*/
    }

    public String toString() {
        return "Trade place: " +  getDescription();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
