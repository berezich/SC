package com.berezich.sportconnector.SpotInfo;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.berezich.sportconnector.FileManager;
import com.berezich.sportconnector.PersonProfile.PersonProfileFragment;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.UsefulFunctions;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sashka on 03.06.2015.
 */
public class ProfileItemLstAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Person> objects;

    ProfileItemLstAdapter(Context context, ArrayList<Person> persons) {
        ctx = context;
        objects = persons;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_person_item, parent, false);
        }
        Person person = getPerson(position);
        if(person!=null) {
            new FileManager.RemoveOldPersonCache().execute(new Pair<>(ctx, person));
            if (view != null) {
                Picture photo = person.getPhoto();
                ImageView imageView = (ImageView) view.findViewById(R.id.lstProfileItem_img_photo);
                FileManager.providePhotoForImgView(ctx, imageView, photo, FileManager.PERSON_CACHE_DIR + "/" + person.getId().toString());
                String name = person.getName(), surname = person.getSurname();
                ((TextView) view.findViewById(R.id.lstProfileItem_name)).setText(
                        ((name != null && !name.equals("")) ? name : "")
                                + ((name != null && !name.equals("") && surname != null && !surname.equals("")) ? " " : "")
                                + ((surname != null && !surname.equals("")) ? surname : ""));
                int age = UsefulFunctions.calcPersonAge(person.getBirthday());

                ((TextView) view.findViewById(R.id.lstProfileItem_desc1)).setText(
                        //(age >= 0 ? ctx.getString(R.string.person_item_age) + " " + age : ""));
                        (age >= 0 ? age +" "+UsefulFunctions.personAgeDeclension(ctx,age):""));

                ((TextView) view.findViewById(R.id.lstProfileItem_desc2)).setText(
                        (person.getRating() > 0) ?
                                ctx.getString(R.string.person_item_rating) + " " + person.getRating() : "");
            }
        }
        return view;
    }
    public Person getPerson(int position) {
        if(position>=0 && position<getCount())
            return objects.get(position);
        return null;
    }
}
