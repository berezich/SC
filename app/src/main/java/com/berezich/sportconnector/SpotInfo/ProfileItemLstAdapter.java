package com.berezich.sportconnector.SpotInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.berezich.sportconnector.R;
import com.berezich.sportconnector.SportObjects.Person;
import com.berezich.sportconnector.SportObjects.Spot;

import java.util.ArrayList;

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
        if(position>=0 && position<getCount())
            return ((Person)objects.get(position)).id();
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ���������� ���������, �� �� ������������ view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_person_item, parent, false);
        }

        Person person = getPerson(position);
        if(person!=null) {
            // ��������� View � ������ ������ ������� �� �������: ������������, ����
            // � ��������
            ((TextView) view.findViewById(R.id.lstProfileItem_name)).setText(person.name() + " " + person.surname());
            ((TextView) view.findViewById(R.id.lstProfileItem_desc1)).setText(ctx.getString(R.string.person_item_age) + " " +person.age());
            ((TextView) view.findViewById(R.id.lstProfileItem_desc2)).setText(ctx.getString(R.string.person_item_rating)+ " " + person.rating());
            //((ImageView) view.findViewById(R.id.lstProfileItem_img_photo)).setImageResource(person.image());
        }

        return view;
    }
    public Person getPerson(int position) {
        if(position>=0 && position<getCount())
            return (Person) objects.get(position);
        return null;
    }
}
