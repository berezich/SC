package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.berezich.sportconnector.EndpointApi;
import com.berezich.sportconnector.FileManager;
import com.berezich.sportconnector.GoogleMap.SpotsData;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.UsefulFunctions;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.google.api.client.util.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by berezkin on 17.07.2015.
 */
public class PersonProfileFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "MyLog_profileFragment";
    //FileManager.PicInfo picInfo;
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
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        if(myPersonInfo!=null)
            new FileManager.RemoveOldPersonCache().execute(new Pair<>(getActivity().getBaseContext(),myPersonInfo));
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
        ImageView imageView;
        getActivity().setTitle(R.string.personprofile_fragmentTitle);
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        if(myPersonInfo!=null && rootView!=null)
        {
            if((imageView = (ImageView) rootView.findViewById(R.id.profile_img_photo))!=null) {
                //imageView.setOnClickListener(new ImageOnClick());
                Picture photoInfo = myPersonInfo.getPhoto();
                FileManager.providePhotoForImgView(this.getActivity().getBaseContext(),imageView,
                        photoInfo,FileManager.PERSON_CACHE_DIR+"/"+ myPersonInfo.getId().toString());
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_name))!=null) {
                String name = myPersonInfo.getName(), surname = myPersonInfo.getSurname();
                txtView.setText( ((name!=null && !name.equals("")) ? name :"") + ((surname!=null && !surname.equals("")) ? " "+surname :""));
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_typeAge))!=null) {
                String str = myPersonInfo.getType().equals("PARTNER")? getString(R.string.personprofile_type_partner):getString(R.string.personprofile_type_coach);
                int age = UsefulFunctions.calcPersonAge(myPersonInfo.getBirthday());
                if(age>=0)
                    str += ", "+age ;
                txtView.setText(str);
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_raiting))!=null) {
                String ratings = getString(R.string.ratingInfo_ratingValLst);
                if(myPersonInfo.getRating()<1.0 && ratings!=null) {
                    String ratingArr[] = ratings.split(",");
                    if(ratingArr.length>0)
                    txtView.setText(getString(R.string.personprofile_rating) + " " + ratingArr[0]);
                }
                else
                    txtView.setText(getString(R.string.personprofile_rating) + " " + myPersonInfo.getRating());
            }

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
                    Spot spot;
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



    /*
    @Override
    public void onDownloadFileFinish(Bitmap bitmap,String imgId,Exception exception) {
        if (exception==null) {
            if(bitmap==null) {
                Log.e(TAG, "bitmap not loaded from server cause: bitmap == null");
                Toast.makeText(getActivity().getBaseContext(),getString(R.string.personprofile_reqError),Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "bitmap loaded from server");
            if(rootView!=null) {
                ImageView imageView = (ImageView) rootView.findViewById(R.id.profile_img_photo);
                imageView.setImageBitmap(bitmap);
                Person myPresonInfo = LocalDataManager.getMyPersonInfo();
                if(myPresonInfo!=null) {
                    FileManager.savePicPreviewToCache(TAG, getActivity().getBaseContext(), imgId, myPresonInfo.getId(), bitmap);
                }
            }
        }
        else {
            Log.e(TAG,"bitmap not loaded from server");
            Log.e(TAG, exception.getMessage());
            exception.printStackTrace();
            Toast.makeText(getActivity().getBaseContext(),getString(R.string.personprofile_reqError),Toast.LENGTH_SHORT).show();
            return;
        }
    }
    */


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG,"onCreateOptionsMenu");
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_person_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_profile:
                FragmentManager fragmentManager = getFragmentManager();
                if(fragmentManager!=null)
                    fragmentManager.beginTransaction().replace(R.id.container, new EditProfileFragment()).addToBackStack(null).commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getTAG() {
        return TAG;
    }
}
