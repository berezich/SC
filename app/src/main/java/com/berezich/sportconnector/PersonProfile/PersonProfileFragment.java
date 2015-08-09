package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.berezich.sportconnector.GoogleMap.SpotsData;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.UsefulFunctions;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by berezkin on 17.07.2015.
 */
public class PersonProfileFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "MyLog_profileFragment";
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
    public void onCreate(Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
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
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume()
    {
        super.onResume();
        TextView txtView;
        getActivity().setTitle(R.string.personprofile_fragmentTitle);
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        if(myPersonInfo!=null && rootView!=null)
        {
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_name))!=null) {
                String name = myPersonInfo.getName(), surname = myPersonInfo.getSurname();
                txtView.setText( ((name!=null && !name.equals("")) ? name :"") + ((surname!=null && !surname.equals("")) ? " "+surname :""));
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_typeAge))!=null) {
                String str = myPersonInfo.getType().equals("PARTNER")? getString(R.string.personprofile_type_partner):getString(R.string.personprofile_type_coach);
                DateTime birthday;
                int age = UsefulFunctions.calcPersonAge(myPersonInfo.getBirthday());
                if(age>=0)
                    str += ", "+age ;
                txtView.setText(str);
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_raiting))!=null)
                txtView.setText(getString(R.string.personprofile_rating)+" "+myPersonInfo.getRating());

            //contacts block
            String email = myPersonInfo.getEmail(),phone = myPersonInfo.getPhone();
            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.profile_contactsBlock);
            LinearLayout propertyLstLayout = (LinearLayout) rootView.findViewById(R.id.profile_linearlayout_propertyLst);
            LinearLayout propertyLayout;
            if(linearLayout!=null ) {
                linearLayout.setVisibility(View.GONE);
                if((propertyLayout = (LinearLayout) rootView.findViewById(R.id.profile_linearlayout_email))!=null && propertyLstLayout!=null)
                    if (email != null && !email.equals("")) {
                        if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_emailValue))!=null)
                            txtView.setText(email);
                        propertyLayout.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                    else
                        propertyLstLayout.removeView(propertyLayout);

                if((propertyLayout = (LinearLayout) rootView.findViewById(R.id.profile_linearlayout_phone))!=null && propertyLstLayout!=null)
                    if (phone != null && !phone.equals("")) {
                        if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_phoneValue))!=null)
                            txtView.setText(phone);
                        propertyLayout.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                    else
                        propertyLstLayout.removeView(propertyLayout);
            }

            //description block
            String desc = myPersonInfo.getDescription();
            linearLayout = (LinearLayout) rootView.findViewById(R.id.profile_descBlock);
            if(linearLayout!=null) {
                linearLayout.setVisibility(View.GONE);
                if (desc != null && !desc.equals("")) {
                    if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_desc))!=null)
                        txtView.setText(desc);
                    linearLayout.setVisibility(View.VISIBLE);
                }

            }

            //favorite spots lst
            boolean isVisibleSpotLst = false;
            if((linearLayout = (LinearLayout) rootView.findViewById(R.id.profile_linearLayout_favoriteSpotLst))!=null) {
                List<Long> spotLst = myPersonInfo.getFavoriteSpotIdLst();
                if(spotLst!=null && spotLst.size()>0) {
                    Spot spot=null;
                    HashMap<Long,Spot> hshMapSpot = SpotsData.get_allSpots();
                    ArrayList<Spot> spots = new ArrayList<>();
                    for(int i=0; i<spotLst.size(); i++)
                        if((spot = hshMapSpot.get(spotLst.get(i)))!=null)
                            spots.add(spot);

                    if(spots.size()>0) {
                        /*ListView lstView = (ListView) rootView.findViewById(R.id.profile_lstView_favoriteSpots);
                        if(lstView!=null) {

                            SpotItemLstAdapter spotItemLstAdapter = new SpotItemLstAdapter(getActivity().getApplicationContext(),spots);
                            lstView.setAdapter(spotItemLstAdapter);
                            lstView.setScrollContainer(false);
                        }*/
                        SpotItemLstAdapter spotItemLstAdapter = new SpotItemLstAdapter(getActivity().getApplicationContext(),spots);
                        if( linearLayout.getChildAt(0) instanceof TextView) {
                            txtView = (TextView) linearLayout.getChildAt(0);
                            linearLayout.removeAllViews();
                            linearLayout.addView(txtView);
                            for (int i = 0; i < spotItemLstAdapter.getCount(); i++)
                                linearLayout.addView(spotItemLstAdapter.getView(i, null, null));
                            isVisibleSpotLst = true;
                        }
                    }

                }
                linearLayout.setVisibility(isVisibleSpotLst? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG,"onCreateOptionsMenu");
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_person_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.menu_edit_profile:
                FragmentManager fragmentManager = (FragmentManager) getFragmentManager();
                if(fragmentManager!=null)
                    fragmentManager.beginTransaction().replace(R.id.container, new EditProfileFragment()).addToBackStack(null).commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
