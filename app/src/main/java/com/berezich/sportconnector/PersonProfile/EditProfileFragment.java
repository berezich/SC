package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.berezich.sportconnector.DatePickerFragment;
import com.berezich.sportconnector.GoogleMap.SpotsData;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sashka on 25.07.2015.
 */
public class EditProfileFragment extends Fragment implements DatePickerFragment.OnActionDatePickerDialogListener{

    //private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "MyLog_EditProfileFrg";
    View rootView;

    /*public EditProfileFragment setArgs(int sectionNumber) {
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        return this;
    }*/

    public EditProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setHasOptionsMenu(false);
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        TextView txtView;
        EditText txtEdt;
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        if(myPersonInfo!=null && rootView!=null)
        {
            /*if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_name))!=null)
                txtView.setText(myPersonInfo.getName() +" "+myPersonInfo.getSurname());
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_typeAge))!=null) {
                String str = myPersonInfo.getType().equals("PARTNER")? getString(R.string.personprofile_type_partner):getString(R.string.personprofile_type_coach);
                str+=", "+myPersonInfo.getAge();
                txtView.setText(str);
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_raiting))!=null)
                txtView.setText(getString(R.string.personprofile_rating)+" "+myPersonInfo.getRating());
*/
            //middle block
            String email = myPersonInfo.getEmail(),phone = myPersonInfo.getPhone();
            if((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_email))!=null)
                txtView.setText(email);
            if((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_phone))!=null)
                txtView.setText(phone);

            if((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_birthday))!=null)
                txtView.setOnClickListener(new OnBirthdayClickListener());
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume()
    {
        super.onResume();
    }
    private Fragment getCurFragment(){
        return this;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.fragment_person_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private class OnBirthdayClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            if(datePickerFragment!=null) {
                datePickerFragment.setTargetFragment(getCurFragment(), -1);
                datePickerFragment.show(getFragmentManager(), null);
            }
        }
    }
    @Override
    public void onDateSet(int year, int month, int day) {
        TextView textView;
        if(rootView!=null)
            if((textView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_birthday))!=null)
            {
                textView.setText(String.format("%02d",day)+ "." + String.format("%02d",month) + "." + year);
                textView.setTextColor(0xff000000);
            }
    }
}
