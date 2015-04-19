package com.berezich.sportconnector;

import android.app.Activity;
import android.app.Application;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.berezich.sportconnector.MainFragment.Filters;

import ru.yandex.yandexmapkit.MapView;

/**
 * Created by berezkin on 17.04.2015.
 */
public class YaMapFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static Filters _filter;
    private static final String TAG = "YaMapFragment";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public YaMapFragment setArgs(int sectionNumber, Filters filter) {

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        _filter = filter;
        return this;
    }

    public YaMapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_yamap, container, false);
        final MapView mapView = (MapView)  rootView.findViewById(R.id.map);
        mapView.showBuiltInScreenButtons(true);
        mapView.showJamsButton(false);

        ImageButton btn;
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_coach);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());
        btn.setPressed(_filter == Filters.COUCH);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_court);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());
        btn.setPressed(_filter == Filters.COURT);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_partner);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());
        btn.setPressed(_filter == Filters.SPARRING_PARTNERS);
        btn = (ImageButton) rootView.findViewById(R.id.map_btn_star);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());

        /*btn = (ImageButton) rootView.findViewById(R.id.map_btn_court_2);
        btn.setOnClickListener(new btnClickListener());
        btn.setOnTouchListener(new btnOnTouchListener());*/

        /*TextView txtView = (TextView) rootView.findViewById(R.id.map_textView);
        switch (_filter)
        {
            case SPARRING_PARTNERS:
                txtView.setText("YaMap Спарринг партнеры");
                break;
            case COUCH:
                txtView.setText("YaMap Тренеры");
                break;
            case CORT:
                txtView.setText("YaMap Корты");
                break;
        }*/
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
    class btnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            Log.d(TAG,"button onClick!!!");
            return;
        }
    }
    class btnOnTouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageButton btn = (ImageButton) v;
            // show interest in events resulting from ACTION_DOWN
            if(event.getAction()==MotionEvent.ACTION_DOWN) {
                btn.setPressed(!btn.isPressed());
                return true;
            }
            if(event.getAction()==MotionEvent.ACTION_UP) {
                btn.performClick();
                return true;
            }

            return false;
        }
    }
}
