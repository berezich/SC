package com.berezich.sportconnector.SpotInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.berezich.sportconnector.EndpointApi.EndpointApi;
import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.FileManager;
import com.berezich.sportconnector.GoogleMap.SpotsData;
import com.berezich.sportconnector.ImageViewer.ImgViewPagerActivity;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.PersonProfile.PersonProfileFragment;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.UsefulFunctions;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SpotInfoFragment extends Fragment implements EndpointApi.GetListPersonByIdLstAsyncTask.OnAction,
                                                          EndpointApi.SetSpotAsFavoriteAsyncTask.OnAction {
    private static final String ARG_SPOT_ID = "spotId";
    private static final String PARTNERS = "partners";
    private static final String COACHES = "coaches";
    private static final String TAG = "MyLog_SpotInfoFragment";
    private boolean isFavorite = false;
    private boolean isDetailsShown = false;
    HashMap<Long, Spot> spotHashMap;
    private final String SPOT_ID = "spotId";
    private Spot curSpot;
    private View spotInfoView;
    private FragmentActivity activity;

    private ArrayList<Long> personIdLst;
    List<Person> coachLst = null;
    List<Person> partnerLst = null;


    public static SpotInfoFragment newInstance(Long spotId) {
        SpotInfoFragment fragment = new SpotInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SPOT_ID, String.valueOf(spotId));
        fragment.setArguments(args);
        return fragment;
    }

    public SpotInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            LocalDataManager.init(getActivity());
            spotHashMap = SpotsData.get_allSpots();
            if (getArguments() != null) {
                String spotIdStr = getArguments().getString(ARG_SPOT_ID);
                if(spotIdStr!=null) {
                    Long spotId = Long.valueOf(spotIdStr);
                    curSpot = spotHashMap.get(spotId);
                }
            }else if(savedInstanceState!=null) {
                Long spotId = savedInstanceState.getLong(SPOT_ID);
                curSpot = spotHashMap.get(spotId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            TextView txtView;
            TabHost tabHost;
            LinearLayout linearLayout;
            ImageButton imgButton;
            spotInfoView = inflater.inflate(R.layout.fragment_spot_info, container, false);

            if ((txtView = (TextView) spotInfoView.findViewById(R.id.spotinfo_frg_tryAgain_txtView)) != null)
                txtView.setOnClickListener(new TryAgainClickListener());
            if (curSpot != null) {
                if ((txtView = (TextView) spotInfoView.findViewById(R.id.spotInfo_txt_name)) != null)
                    txtView.setText(curSpot.getName());
                if ((txtView = (TextView) spotInfoView.findViewById(R.id.spotInfo_txt_adress)) != null)
                    txtView.setText(curSpot.getAddress());
                if((linearLayout = (LinearLayout) spotInfoView.findViewById(R.id.spotInfo_layout_details))!=null)
                {
                    String value;
                    boolean enableDetails = false;
                    if ((value = curSpot.getSite()) != null && !value.equals("")) {
                        View view = getDetailItem(getContext(), getString(R.string.spotinfo_site),value, TXT_TYPE.SITE);
                        if(view!=null)
                            linearLayout.addView(view);
                        enableDetails = true;
                    }
                    if ((value = curSpot.getContact()) != null && !value.equals("")) {
                        View view = getDetailItem(getContext(), getString(R.string.spotinfo_phone), value, TXT_TYPE.PHONE);
                        if(view!=null)
                            linearLayout.addView(view);
                        enableDetails = true;
                    }
                    if ((value = curSpot.getPrice()) != null && !value.equals("")) {
                        linearLayout.addView(getDetailItem(getContext(), getString(R.string.spotinfo_prices), value));
                        enableDetails = true;
                    }
                    int numCourts;
                    value = "";
                    if((numCourts = curSpot.getOpenPlayFieldNum())>0) {
                        value = String.format("%d %s%s %s%s", numCourts, getString(R.string.spotinfo_openCourts),
                                UsefulFunctions.adjPluralPostfix(numCourts), getString(R.string.spotinfo_playField),
                                UsefulFunctions.pluralPostfix(numCourts));
                        enableDetails = true;
                    }
                    if((numCourts = curSpot.getClosedPlayFieldNum())>0){
                        value += ((value.equals(""))?"":", ")+String.format("%d %s%s %s%s", numCourts,getString(R.string.spotinfo_closedCourts),
                                UsefulFunctions.adjPluralPostfix(numCourts), getString(R.string.spotinfo_playField),
                                UsefulFunctions.pluralPostfix(numCourts));
                        enableDetails = true;
                    }
                    if(!value.equals(""))
                        linearLayout.addView(getDetailItem(getContext(),getString(R.string.spotinfo_courts),value));

                    if ((value = curSpot.getDescription()) != null && !value.equals("")) {
                        linearLayout.addView(getDetailItem(getContext(), getString(R.string.spotinfo_description), value));
                        enableDetails = true;
                    }

                    txtView = (TextView) spotInfoView.findViewById(R.id.spotInfo_txt_details);

                    if(enableDetails)
                        txtView.setVisibility(View.VISIBLE);
                    else
                        txtView.setVisibility(View.GONE);

                    txtView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isDetailsShown = !isDetailsShown;
                            TextView textView = (TextView) spotInfoView.findViewById(R.id.spotInfo_txt_details);
                            LinearLayout linearLayout = (LinearLayout) spotInfoView.findViewById(R.id.spotInfo_layout_details);
                            linearLayout.setVisibility((isDetailsShown) ? View.VISIBLE : View.GONE);
                            linearLayout = (LinearLayout) spotInfoView.findViewById(R.id.spotInfo_layout_toHide);
                            linearLayout.setVisibility((isDetailsShown) ? View.GONE : View.VISIBLE);
                            textView.setText((isDetailsShown) ? getString(R.string.spotinfo_details_hide) : getString(R.string.spotinfo_details));
                        }
                    });


                }

                if ((imgButton = (ImageButton) spotInfoView.findViewById(R.id.spotInfo_btnImg_favorite)) != null) {
                    imgButton.setOnTouchListener(new StarBtnOnTouchListener());
                }

                if ((linearLayout = (LinearLayout) spotInfoView.findViewById(R.id.spotInfo_list_photos)) != null) {
                    List<Picture> picList = curSpot.getPictureLst();
                    if (picList != null && picList.size() > 0) {
                        //linearLayout.setOnClickListener(new OnImageClick());
                        Context ctx = getContext();
                        ImageView imageView;
                        for (Picture pic : picList) {
                            imageView = new ImageView(ctx);
                            linearLayout.addView(imageView);
                            imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.spotInfo_photos_height);
                            imageView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.spotInfo_photos_width);
                            FileManager.providePhotoForImgView(ctx, imageView, pic, FileManager.SPOT_CACHE_DIR + "/" + curSpot.getId());
                            imageView.setOnClickListener(new OnImageClick());
                        }
                        linearLayout.setVisibility(View.VISIBLE);

                    } else {
                        linearLayout.setVisibility(View.GONE);
                    }
                }

                int coachesNum = SpotsData.getCoachIdsWithoutMe(curSpot).size(), partnersNum = SpotsData.getPartnerIdsWithoutMe(curSpot).size();
                if ((partnersNum > 0 || coachesNum > 0) && (tabHost = (TabHost) spotInfoView.findViewById(R.id.spotInfo_tabHost)) != null) {
                    // инициализация
                    tabHost.setup();

                    TabHost.TabSpec tabSpec;
                    if (partnersNum > 0) {
                        // создаем вкладку и указываем тег
                        tabSpec = tabHost.newTabSpec(PARTNERS);
                        // название вкладки
                        tabSpec.setIndicator(getString(R.string.spotinfo_tab1_title));

                        // указываем id компонента из FrameLayout, он и станет содержимым
                        tabSpec.setContent(R.id.spotInfo_list_tab_partners);
                        // добавляем в корневой элемент
                        tabHost.addTab(tabSpec);
                    }
                    if (coachesNum > 0) {
                        tabSpec = tabHost.newTabSpec(COACHES);
                        tabSpec.setIndicator(getString(R.string.spotinfo_tab2_title));
                        tabSpec.setContent(R.id.spotInfo_list_tab_coaches);
                        tabHost.addTab(tabSpec);
                    }
                    /*// обработчик переключения вкладок
                    tabHost.setOnTabChangedListener(new OnTabChangeListener() {
                        public void onTabChanged(String tabId) {
                            Toast.makeText(getBaseContext(), "tabId = " + tabId, Toast.LENGTH_SHORT).show();
                        }
                    });*/
                } else {
                    setVisible(View.GONE, View.GONE, View.VISIBLE);
                    return spotInfoView;
                }

                if(coachLst!=null && partnerLst!=null){
                    int personCount=0;
                    personCount+=fillPersonsList(coachLst,COACHES);
                    personCount+=fillPersonsList(partnerLst,PARTNERS);
                    if(personCount>0)
                        setVisible(View.VISIBLE,View.GONE,View.GONE);
                    else
                        setVisible(View.GONE,View.GONE,View.VISIBLE);
                }
                else {
                    personIdLst = new ArrayList<>();
                    if (curSpot.getCoachLst() != null)
                        personIdLst.addAll(SpotsData.getCoachIdsWithoutMe(curSpot));
                    if (curSpot.getPartnerLst() != null)
                        personIdLst.addAll(SpotsData.getPartnerIdsWithoutMe(curSpot));
                    if (personIdLst.size() > 0) {
                        setVisibleProgressBar();
                        new EndpointApi.GetListPersonByIdLstAsyncTask(this).execute(personIdLst);

                    }
                }
            }
            return spotInfoView;
        } catch (Exception e) {
            e.printStackTrace();
            return spotInfoView=null;
        }
    }


    @Override
    public void onResume(){
        try {
            super.onResume();
            ((MainActivity)activity).setmTitle(activity.getString(R.string.spotinfo_fragmentTitle));
            ActionBar actionBar = ((MainActivity) activity).getSupportActionBar();
            if(actionBar!=null)
                actionBar.setHomeAsUpIndicator(null);
            ((MainActivity)activity).restoreActionBar();
            ImageButton imgButton;
            if ((imgButton = (ImageButton) spotInfoView.findViewById(R.id.spotInfo_btnImg_favorite)) != null) {
                isFavorite = LocalDataManager.isMyFavoriteSpot(curSpot);
                setButtonImg(imgButton,isFavorite);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putLong(SPOT_ID, curSpot.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.activity = getActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    class StarBtnOnTouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                ImageButton btn = (ImageButton) v;
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    setButtonImg(btn, !isFavorite);
                    SpotsData.setSpotFavorite(curSpot.getId(), isFavorite);
                    Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                    if(myPersonInfo!=null)
                    new EndpointApi.SetSpotAsFavoriteAsyncTask(SpotInfoFragment.this).execute(
                            new Pair<>(new Pair<>( new Pair<>(curSpot.getId(),isFavorite),
                            new Pair<>( myPersonInfo.getId(),myPersonInfo.getPass())),myPersonInfo.getType()));
                    return true;
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }
    }

    @Override
    public void onGetListPersonByIdLstFinish(Pair<List<Person>, Exception> result) {
        try {
            List<Person> personLst = result.first;
            Exception error = result.second;
            Person person;

            if(getActivity()==null)
            {
                Log.e(TAG,"current fragment isn't attached to activity");
                return;
            }
            if(error == null && personLst!=null)
            {
                coachLst = new ArrayList<>();
                partnerLst = new ArrayList<>();
                for (int i = 0; i <personLst.size() ; i++) {
                    person = personLst.get(i);
                    if(person.getType().equals("COACH"))
                        coachLst.add(person);
                    if(person.getType().equals("PARTNER"))
                        partnerLst.add(person);
                }
                fillPersonsList(coachLst, COACHES);
                fillPersonsList(partnerLst,PARTNERS);
                if(partnerLst.size()>0 || coachLst.size()>0)
                    setVisible(View.VISIBLE,View.GONE,View.GONE);
                else
                    setVisible(View.GONE, View.GONE, View.VISIBLE);

                return;
            }
            Log.e(TAG, "Error GetListPersonByIdLst");
            if(personLst==null)
                Log.e(TAG, "personLst = null");

            FrameLayout frameLayout;
            if((frameLayout = (FrameLayout) spotInfoView.findViewById(R.id.spotinfo_frg_frameLayout))!=null)
                ErrorVisualizer.showErrorAfterReq(getContext(), frameLayout, error, TAG);
            setVisible(View.GONE, View.VISIBLE, View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int fillPersonsList(List<Person> personList, String typeOfPerson)
    {
        ListView listView=null;
        if(personList==null || personList.isEmpty())
            return 0;
        switch (typeOfPerson){
            case COACHES:
                listView = (ListView) spotInfoView.findViewById(R.id.spotInfo_list_tab_coaches);
                break;
            case PARTNERS:
                listView = (ListView) spotInfoView.findViewById(R.id.spotInfo_list_tab_partners);
                break;
            default:
                assert true;
        }

        if(listView!=null) {
            listView.getLayoutParams().height = android.app.ActionBar.LayoutParams.MATCH_PARENT;
            ProfileItemLstAdapter partnersAdapter = new ProfileItemLstAdapter(activity.getApplicationContext(),
                    new ArrayList<>(personList));
            listView.setAdapter(partnersAdapter);
            listView.setOnItemClickListener(new OnPersonClick(personList));
        }
        return personList.size();
    }

    @Override
    public void onSetSpotAsFavoriteFinish(Pair<Boolean,Exception> result) {
        try {
            boolean isFavoriteReq = result.first;
            Exception ex = result.second;

            if(ex!=null)
            {
                if(spotInfoView!=null) {
                    ImageButton btn = (ImageButton) spotInfoView.findViewById(R.id.spotInfo_btnImg_favorite);
                    if (btn!=null) {
                        if(this.isFavorite == isFavoriteReq)
                            setButtonImg(btn,!isFavoriteReq);
                        SpotsData.setSpotFavorite(curSpot.getId(), !isFavoriteReq);
                        if(activity!=null)
                            Toast.makeText(activity.getBaseContext(), R.string.spotinfo_req_error_msg, Toast.LENGTH_SHORT).show();
                        else
                            Log.e(TAG, "current fragment isn't attached to activity");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setButtonImg(ImageButton btn, boolean isFavorite){
        this.isFavorite = isFavorite;
        if (isFavorite)
            btn.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.ic_star_press_36dp));
        else
            btn.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.ic_star_36dp));
    }
    private class TryAgainClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            try {
                if(personIdLst.size()>0) {
                    setVisibleProgressBar();
                    new EndpointApi.GetListPersonByIdLstAsyncTask(SpotInfoFragment.this).execute(personIdLst);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void setVisible(int tabsVisibility, int frameLayoutVisibility, int noPersonsVisibility)
    {
        FrameLayout frameLayout1;
        View view;
        if(spotInfoView!=null) {
            if ((frameLayout1 = (FrameLayout) spotInfoView.findViewById(R.id.spotinfo_frg_frameLayout)) != null)
                frameLayout1.setVisibility(frameLayoutVisibility);
            if ((view = spotInfoView.findViewById(R.id.spotInfo_tabHost)) != null)
                view.setVisibility(tabsVisibility);
            if ((view = spotInfoView.findViewById(R.id.spotInfo_txt_noPartersCoaches)) != null)
                view.setVisibility(noPersonsVisibility);
        }
    }
    private void setVisibleProgressBar()
    {
        FrameLayout frameLayout;
        setVisible(View.GONE, View.VISIBLE, View.GONE);

        if((frameLayout = (FrameLayout) spotInfoView.findViewById(R.id.spotinfo_frg_frameLayout))!=null)
            ErrorVisualizer.showProgressBar(frameLayout);
    }
    private class OnImageClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            try {
                LinearLayout linearLayout = (LinearLayout) v.getParent();
                int index = linearLayout.indexOfChild(v);
                Intent intent = new Intent(activity, ImgViewPagerActivity.class);
                if(curSpot!=null) {
                    List<Picture> picLst = curSpot.getPictureLst();
                    if(picLst!=null) {
                        GsonFactory gsonFactory = new GsonFactory();
                        try {
                            ArrayList<String> picList = new ArrayList<>();
                            for(Picture pic:picLst)
                                picList.add(gsonFactory.toString(pic));
                            intent.putStringArrayListExtra(ImgViewPagerActivity.PIC_LIST_EXTRAS, picList);
                            intent.putExtra(ImgViewPagerActivity.PIC_INDEX_EXTRAS, index);
                            startActivity(intent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {
            menu.clear();
            super.onCreateOptionsMenu(menu, inflater);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void dialPhone(String phone) {

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        if (intent.resolveActivity(activity.getBaseContext().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    enum TXT_TYPE{SITE,PHONE,TEXT}
    private View getDetailItem(Context context,String name, String value) {
        return getDetailItem(context,name,value,TXT_TYPE.TEXT);
    }
    private View getDetailItem(Context context,String name, String value, TXT_TYPE type) {
        View itemView;
        TextView txtView;
        LayoutInflater lInflater;
        if((lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))!=null)
            if((itemView = lInflater.inflate(R.layout.spotinfo_detail_item, null))!=null) {
                txtView = (TextView) itemView.findViewById(R.id.spotInfo_detailItem_title);
                txtView.setText(name);
                txtView = (TextView) itemView.findViewById(R.id.spotInfo_detailItem_value);
                switch (type){
                    case SITE:
                        txtView.setText(value);
                        Linkify.addLinks(txtView, Linkify.WEB_URLS);
                        break;
                    case PHONE:
                        String[] phones = value.split(";");
                        txtView.setText(phones[0].trim());
                        float textSize = getResources().getDimensionPixelSize(R.dimen.spotInfo_details_textSize)/getResources().getDisplayMetrics().density;
                        txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
                        LinearLayout linearLayout = (LinearLayout) itemView.findViewById(R.id.spotInfo_detailItem_layout_value);
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String phone_no= ((TextView)v.findViewById(R.id.spotInfo_detailItem_value)).getText().toString();
                                phone_no = phone_no.split("доб")[0];
                                dialPhone(phone_no);
                            }
                        });
                        if(phones.length>1)
                        {
                            txtView = (TextView) itemView.findViewById(R.id.spotInfo_detailItem_value2);
                            txtView.setText(phones[1].trim());
                            txtView.setText(phones[1].trim());
                            txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
                            linearLayout = (LinearLayout) itemView.findViewById(R.id.spotInfo_detailItem_layout_value2);
                            linearLayout.setVisibility(View.VISIBLE);
                            linearLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String phone_no = ((TextView) v.findViewById(R.id.spotInfo_detailItem_value2)).getText().toString();
                                    phone_no = phone_no.split("доб")[0];
                                    dialPhone(phone_no);
                                }
                            });
                        }

                        break;
                    case TEXT:
                        txtView.setText(value);
                }
                return itemView;
            }
        return null;
    }

    private class OnPersonClick implements AdapterView.OnItemClickListener{
        List<Person> persons;
        public OnPersonClick(List<Person> persons) {
            this.persons = persons;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                Person person = persons.get(position);
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new PersonProfileFragment().setArgs(position,false,person))
                        .addToBackStack(PersonProfileFragment.class.getName())
                        .commit();
                Log.d(TAG, String.format("prev fragment replaced with %s", PersonProfileFragment.class.getName()));
            }catch (Exception e){
                Log.e(TAG,"exception occurred while hitting personItem in spotInfo");
                e.printStackTrace();
            }

        }
    }
}
