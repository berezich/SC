package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;

/**
 * Created by berezkin on 17.07.2015.
 */
public class PersonProfileFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "PERSON_PROFILE_FRAGMENT";
    int _sectionNumber;
    View rootView;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public PersonProfileFragment setArgs(int sectionNumber) {
        _sectionNumber = sectionNumber;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        return this;
    }

    public PersonProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_person_profile, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }
    @Override
    public void onResume()
    {
        super.onResume();

    }
}
