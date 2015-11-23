package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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

import com.berezich.sportconnector.FileManager;
import com.berezich.sportconnector.GoogleMap.SpotsData;
import com.berezich.sportconnector.ImageViewer.ImgViewPagerActivity;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.SpotInfo.SpotInfoFragment;
import com.berezich.sportconnector.UsefulFunctions;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by berezkin on 17.07.2015.
 */
public class PersonProfileFragment extends Fragment {


    private static final String ARG_SECTION_NUMBER = "sectionNumber";
    private static final String ARG_IS_MYPROFILE = "isMyProfile";
    private static final String ARG_PERSON = "personId";
    private final String TAG = "MyLog_profileFragment";
    View rootView;
    boolean isMyProfile = false;
    Person person = null;
    private static GsonFactory gsonFactory = new GsonFactory();
    MainActivity activity;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public PersonProfileFragment setArgs(int sectionNumber,boolean isMyProfile, Person person) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_MYPROFILE, isMyProfile);
        try {
            args.putString(ARG_PERSON, gsonFactory.toString(person));
        } catch (Exception e) {
            Log.e(TAG,"exception occurred in personProfile setArgs");
            e.printStackTrace();
        }
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        return this;
    }

    public PersonProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        if(args!=null){
            isMyProfile = args.getBoolean(ARG_IS_MYPROFILE);
            if(!isMyProfile){
                String personStr = args.getString(ARG_PERSON);
                try {
                    person = gsonFactory.fromString(personStr,Person.class);
                } catch (Exception e) {
                    Log.e(TAG,"exception occurred in personProfile onCreate");
                    e.printStackTrace();
                }
            }
        }
        if(savedInstanceState!=null){
            isMyProfile = savedInstanceState.getBoolean(ARG_IS_MYPROFILE);
            if(!isMyProfile) {
                String personStr = savedInstanceState.getString(ARG_PERSON);
                try {
                    person = gsonFactory.fromString(personStr, Person.class);
                } catch (Exception e) {
                    Log.e(TAG, "exception occurred in personProfile onCreate");
                    e.printStackTrace();
                }
            }
        }
        LocalDataManager.init(getActivity());
        if(isMyProfile)
            person =LocalDataManager.getMyPersonInfo();

        if (person != null && savedInstanceState==null) {
            Log.d(TAG, "run RemoveOldPersonCache");
            new FileManager.RemoveOldPersonCache().execute(new Pair<>(getActivity().getBaseContext(), person));
        }
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
        this.activity = (MainActivity)activity;
        if(isMyProfile)
            this.activity.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));


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
        if(isMyProfile)
            person = LocalDataManager.getMyPersonInfo();
        if(isMyProfile) {
            this.activity.setmTitle(activity.getString(R.string.personprofile_myProfile_fragmentTitle));
            this.activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
        }
        else {
            this.activity.setmTitle(activity.getString(R.string.personprofile_profile_fragmentTitle));
            this.activity.getSupportActionBar().setHomeAsUpIndicator(null);
        }
        this.activity.restoreActionBar();

        if(person!=null && rootView!=null)
        {
            if((imageView = (ImageView) rootView.findViewById(R.id.profile_img_photo))!=null) {
                imageView.setOnClickListener(new OnImageClick());
                Picture photoInfo = person.getPhoto();
                FileManager.providePhotoForImgView(this.getActivity().getBaseContext(), imageView,
                        photoInfo, FileManager.PERSON_CACHE_DIR + "/" + person.getId().toString());
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_name))!=null) {
                String name = person.getName(), surname = person.getSurname();
                txtView.setText( ((name!=null && !name.equals("")) ? name :"") +
                        ((surname!=null && !surname.equals("")) ? " "+surname :""));
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_typeAge))!=null) {
                String str = person.getType().equals("PARTNER")? getString(R.string.personprofile_type_partner):getString(R.string.personprofile_type_coach);
                int age = UsefulFunctions.calcPersonAge(person.getBirthday());
                if(age>=0)
                    str += ", "+age+" "+UsefulFunctions.personAgeDeclension(activity.getBaseContext(),age) ;
                txtView.setText(str);
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_raiting))!=null) {
                String ratings = getString(R.string.ratingInfo_ratingValLst);
                if(person.getRating()<1.0 && ratings!=null) {
                    String ratingArr[] = ratings.split(",");
                    if(ratingArr.length>0)
                    txtView.setText(getString(R.string.personprofile_rating) + " " + ratingArr[0]);
                }
                else
                    txtView.setText(getString(R.string.personprofile_rating) + " " + person.getRating());
            }

            //contacts block
            String email = person.getEmail(),phone = person.getPhone();
            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.profile_contactsBlock);
            LinearLayout propertyLstLayout = (LinearLayout) rootView.findViewById(R.id.profile_linearlayout_propertyLst);
            propertyLstLayout.removeAllViews();
            if(linearLayout!=null ) {
                linearLayout.setVisibility(View.GONE);
                    if (email != null && !email.equals("")) {
                        linearLayout.setVisibility(View.VISIBLE);
                        View view = getProfileDetailItem(getContext(),getString(R.string.personprofile_email),email);
                        if(view!=null) {
                            propertyLstLayout.addView(view);
                            if(!isMyProfile)
                                view.setOnClickListener(new OnComposeEmailClick());
                        }
                    }
                    if (phone != null && !phone.equals("")) {

                        linearLayout.setVisibility(View.VISIBLE);
                        View view = getProfileDetailItem(getContext(),getString(R.string.personprofile_phone),phone);
                        if(view!=null) {
                            propertyLstLayout.addView(view);
                            if(!isMyProfile)
                                view.setOnClickListener(new OnPhoneClick());
                        }
                    }
            }

            //description block
            String desc = person.getDescription();
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
                List<Long> spotLst = person.getFavoriteSpotIdLst();
                if(spotLst!=null && spotLst.size()>0) {
                    Spot spot;
                    HashMap<Long,Spot> hshMapSpot = SpotsData.get_allSpots();
                    ArrayList<Spot> spots = new ArrayList<>();
                    for(int i=0; i<spotLst.size(); i++)
                        if((spot = hshMapSpot.get(spotLst.get(i)))!=null)
                            spots.add(spot);

                    if(spots.size()>0) {
                        SpotItemLstAdapter spotItemLstAdapter = new SpotItemLstAdapter(getActivity().getApplicationContext(),spots);
                        if( linearLayout.getChildAt(0) instanceof TextView) {
                            txtView = (TextView) linearLayout.getChildAt(0);
                            linearLayout.removeAllViews();
                            linearLayout.addView(txtView);
                            for (int i = 0; i < spotItemLstAdapter.getCount(); i++) {

                                View view = spotItemLstAdapter.getView(i, null, null);
                                view.setOnClickListener(new OnSpotClick());
                                linearLayout.addView(view);
                            }
                            isVisibleSpotLst = true;

                        }
                    }

                }
                linearLayout.setVisibility(isVisibleSpotLst? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_IS_MYPROFILE,isMyProfile);
        if(!isMyProfile)
            try {
                outState.putString(ARG_PERSON, gsonFactory.toString(person));
            } catch (IOException e) {
                Log.e(TAG,"exception occurred in personProfile onSaveInstanceState");
                e.printStackTrace();
            }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        if(isMyProfile)
            inflater.inflate(R.menu.fragment_person_profile, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_profile:
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                if(fragmentManager!=null) {
                    EditProfileFragment fragment = new EditProfileFragment();
                    fragment.setTargetFragment(this,0);
                    String name = fragment.getClass().getName();
                    fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(name).commit();
                    Log.d(TAG, String.format("prev fragment replaced with %s", fragment.getClass().getName()));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class OnImageClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ImgViewPagerActivity.class);
            if(person!=null) {
                Picture picture = person.getPhoto();
                if(picture!=null) {
                    GsonFactory gsonFactory = new GsonFactory();
                    try {
                        ArrayList<String> picList = new ArrayList<String>();
                        picList.add(gsonFactory.toString(picture));
                        intent.putStringArrayListExtra(ImgViewPagerActivity.PIC_LIST_EXTRAS, picList);
                        getCurFragment().startActivity(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private class OnSpotClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            try {
                int position = ((LinearLayout) v.getParent()).indexOfChild(v);
                Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                Long spotId = myPersonInfo.getFavoriteSpotIdLst().get(position - 1);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                SpotInfoFragment fragment = SpotInfoFragment.newInstance(spotId);
                fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(fragment.getClass().getName()).commit();
                Log.d(TAG, String.format("prev fragment replaced with %s", fragment.getClass().getName()));
            }
            catch (Exception e){
                Log.e(TAG,String.format("exception occurred while hitting spotItem"));
                e.printStackTrace();
            }
        }

    }
    public String getTAG() {
        return TAG;
    }
    private Fragment getCurFragment(){
        return this;
    }
    private View getProfileDetailItem(Context context, String title, String value){
        LayoutInflater lInflater;
        View itemView=null;
        if((lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))!=null)
            if((itemView = lInflater.inflate(R.layout.profile_detail_item, null))!=null){
                TextView textView = (TextView) itemView.findViewById(R.id.profile_txt_itemTitle);
                if(textView!=null)
                    textView.setText(title);
                textView = (TextView) itemView.findViewById(R.id.profile_txt_itemValue);
                if(textView!=null)
                    textView.setText(value);
            }
        return itemView;
    }

    private class OnPhoneClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            phoneIntent(person.getPhone());
        }
    }
    public void phoneIntent(String phone){
        try {
            int validIntentFlag = 0;
            PackageManager pm = activity.getPackageManager();
            Intent intentDial = new Intent(Intent.ACTION_DIAL);
            Uri phoneData = Uri.parse("tel:" + phone);
            intentDial.setData(phoneData);
            Intent intentSms = new Intent(Intent.ACTION_VIEW);
            Uri smsData =  Uri.parse("sms:" + phone);
            intentSms.setData(smsData);  // This ensures only SMS apps respond

            if(intentDial.resolveActivity(pm)!=null)
                validIntentFlag |=  0x01;
            if(intentSms.resolveActivity(pm)!=null)
                validIntentFlag |=  0x10;

            switch (validIntentFlag){
                case 0x01:
                    startActivity(intentDial);
                    return;
                case 0x10:
                    startActivity(intentSms);
                    return;
                case 0x00:
                    return;
            }

            Intent openInChooser = Intent.createChooser(intentDial,
                    activity.getString(R.string.personprofile_intentChooserTitle));
            // Append " (for editing)" to applicable apps, otherwise they will show up twice identically
            Spannable mark = new SpannableString(" "+activity.getString(R.string.personprofile_chooserItemSmsMark));
            if(mark.toString().trim().isEmpty())
                mark = new SpannableString("");
            else
                mark.setSpan(new ForegroundColorSpan(Color.CYAN), 0, mark.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            List<ResolveInfo> resInfo = pm.queryIntentActivities(intentSms, PackageManager.MATCH_DEFAULT_ONLY);
            Intent[] extraIntents = new Intent[resInfo.size()];
            for (int i = 0; i < resInfo.size(); i++) {
                // Extract the label, append it, and repackage it in a LabeledIntent
                ResolveInfo ri = resInfo.get(i);
                String packageName = ri.activityInfo.packageName;
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(smsData);
                CharSequence label = TextUtils.concat(ri.loadLabel(pm), mark);
                extraIntents[i] = new LabeledIntent(intent, packageName, label, ri.icon);
            }

            openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
            startActivity(openInChooser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class OnComposeEmailClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            composeEmail(person.getEmail());
        }
    }
    public void composeEmail(String address) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("*/*");
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        if (intent.resolveActivity(activity.getBaseContext().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
