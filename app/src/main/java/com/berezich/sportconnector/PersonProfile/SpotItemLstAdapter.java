package com.berezich.sportconnector.PersonProfile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.berezich.sportconnector.FileManager;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.SpotInfo.SpotInfoFragment;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by berezkin on 24.07.2015.
 */
public class SpotItemLstAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Spot> objects;

    SpotItemLstAdapter(Context context, ArrayList<Spot> spots) {
        ctx = context;
        objects = spots;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return (objects!=null)? objects.size():0;
    }

    @Override
    public Object getItem(int position) {
        if(position>=0 && position<getCount())
            return objects.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        /*
        if(position>=0 && position<getCount())
            return ((Person)objects.get(position)).getId();
        return -1;
        */
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_spot_item, parent, false);
        }

        Spot spot = getSpot(position);
        TextView txtView;
        if(spot!=null) {
            if((txtView = (TextView) view.findViewById(R.id.lstSpotItem_name))!=null)
                txtView.setText(spot.getName());
            if((txtView = (TextView) view.findViewById(R.id.lstSpotItem_address))!=null)
                txtView.setText(spot.getAddress());
            if((txtView = (TextView) view.findViewById(R.id.lstSpotItem_raiting))!=null)
                txtView.setText(ctx.getString(R.string.spot_item_rating)+" "+ (spot.getRating()!=null ? spot.getRating() : "0.0"));
            /*((TextView) view.findViewById(R.id.lstProfileItem_name)).setText(spot.getName() + " " + spot.getSurname());
            ((TextView) view.findViewById(R.id.lstProfileItem_desc1)).setText(ctx.getString(R.string.person_item_age) + " " + spot.getAge());
            ((TextView) view.findViewById(R.id.lstProfileItem_desc2)).setText(ctx.getString(R.string.person_item_rating) + " " + spot.getRating());*/
            //((ImageView) view.findViewById(R.id.lstProfileItem_img_photo)).setImageResource(person.image());
            List<Picture> pictureLst = spot.getPictureLst();
            if(pictureLst!=null && pictureLst.size()>0) {
                Picture photo = pictureLst.get(0);
                ImageView imageView = (ImageView) view.findViewById(R.id.lstSpotItem_img_photo1);
                FileManager.providePhotoForImgView(ctx, imageView, photo, FileManager.SPOT_CACHE_DIR+"/"+spot.getId().toString());
            }
        }

        return view;
    }
    public Spot getSpot(int position) {
        if(position>=0 && position<getCount())
            return (Spot) objects.get(position);
        return null;
    }
}