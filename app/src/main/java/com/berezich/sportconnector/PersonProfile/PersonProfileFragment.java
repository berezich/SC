package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;

import java.util.List;

/**
 * Created by berezkin on 17.07.2015.
 */
public class PersonProfileFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "MyLog_personProfileFragment";
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
        TextView txtView;
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        if(myPersonInfo!=null && rootView!=null)
        {
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_name))!=null)
                txtView.setText(myPersonInfo.getName() +" "+myPersonInfo.getSurname());
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_typeAge))!=null) {
                String str = myPersonInfo.getType().equals("PARTNER")? getString(R.string.personprofile_type_partner):getString(R.string.personprofile_type_coach);
                str+=", "+myPersonInfo.getAge();
                txtView.setText(str);
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_raiting))!=null)
                txtView.setText(getString(R.string.personprofile_rating)+" "+myPersonInfo.getRating());

            //middle block
            String email = myPersonInfo.getEmail(),phone = myPersonInfo.getPhone();
            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.profile_middleBlock);
            FrameLayout frameLayout;
            if(linearLayout!=null) {
                linearLayout.setVisibility(View.GONE);
                if((frameLayout = (FrameLayout) rootView.findViewById(R.id.profile_frame_email))!=null)
                    if (email != null && !email.equals("")) {
                        if((txtView = (TextView) rootView.findViewById(R.id.profile_txtEdt_emailValue))!=null)
                            txtView.setText(email);
                        frameLayout.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                    else
                        frameLayout.setVisibility(View.GONE);

                if((frameLayout = (FrameLayout) rootView.findViewById(R.id.profile_frame_phone))!=null)
                    if (phone != null && !phone.equals("")) {
                        if((txtView = (TextView) rootView.findViewById(R.id.profile_txtEdt_phoneValue))!=null)
                            txtView.setText(phone);
                        frameLayout.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                    else
                        frameLayout.setVisibility(View.GONE);
            }

            //favorite spots lst
            if((linearLayout = (LinearLayout) rootView.findViewById(R.id.profile_linearLayout_favoriteSpotLst))!=null) {
                List<Long> spotLst = myPersonInfo.getFavoriteSpotIdLst();
                if(spotLst!=null && spotLst.size()>0)
                    linearLayout.setVisibility(View.VISIBLE);
                else
                    linearLayout.setVisibility(View.GONE);
            }
        }
    }
}
