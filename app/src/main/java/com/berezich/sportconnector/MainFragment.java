package com.berezich.sportconnector;

/**
 * Created by berezkin on 17.04.2015.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public enum Filters {
        SPARRING_PARTNERS, COUCH, COURT
    };
    private static final String ARG_SECTION_NUMBER = "section_number";
    int _sectionNumber;
    OnActionListener listener;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public MainFragment setArgs(int sectionNumber) {
        _sectionNumber = sectionNumber;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        return this;
    }

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Button btn = (Button) rootView.findViewById(R.id.main_frg_btn1);
        btn.setOnClickListener(new BtnClickListener());
        btn = (Button) rootView.findViewById(R.id.main_frg_btn2);
        btn.setOnClickListener(new BtnClickListener());
        btn = (Button) rootView.findViewById(R.id.main_frg_btn3);
        btn.setOnClickListener(new BtnClickListener());
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnActionListener for MainFragment");
        }
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
    class BtnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            Button btn;
            try {
                btn = (Button) view;
            }
            catch (ClassCastException e)
            {
                throw new ClassCastException(view.toString() + "must be a Button");
            }
            switch (btn.getId())
            {
                case R.id.main_frg_btn1:
                    listener.onBtnClick(Filters.SPARRING_PARTNERS,_sectionNumber);
                    break;
                case R.id.main_frg_btn2:
                    listener.onBtnClick(Filters.COUCH,_sectionNumber);
                    break;
                case R.id.main_frg_btn3:
                    listener.onBtnClick(Filters.COURT,_sectionNumber);
                    break;
            }

        }
    }

    public static interface OnActionListener{
        void onBtnClick(Filters position, int section);
    }

}
