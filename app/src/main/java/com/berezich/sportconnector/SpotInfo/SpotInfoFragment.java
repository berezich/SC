package com.berezich.sportconnector.SpotInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import com.berezich.sportconnector.EndpointApi;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SpotInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SpotInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpotInfoFragment extends Fragment implements EndpointApi.GetListPersonByIdLstAsyncTask.OnAction,
                                                          /*EndpointApi.UpdateSpotAsyncTask.OnAction,*/
                                                          EndpointApi.SetSpotAsFavoriteAsyncTask.OnAction {
    private static final String ARG_SPOT_ID = "spotId";
    private static final String PARTNERS = "partners";
    private static final String COACHES = "coaches";
    private static final String TAG = "MyLog_SpotInfoFragment";
    private boolean isFavorite = false;
    private boolean isDetailsShown = false;
    private HashMap<Long, Spot> spotHashMap;
    private final String SPOT_ID = "spotId";
    private Spot curSpot;
    private View spotInfoView;
    private FragmentActivity activity;

    private ArrayList<Long> personIdLst;
    List<Person> coachLst = null;
    List<Person> partnerLst = null;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p/>
     * //@param param1 Parameter 1.
     * //@param param2 Parameter 2.
     *
     * @return A new instance of fragment BlankFragment.
     */
    public static SpotInfoFragment newInstance(Long spotId) {
        SpotInfoFragment fragment = new SpotInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SPOT_ID, String.valueOf(spotId));
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SpotInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LocalDataManager.init(getActivity());
        spotHashMap = SpotsData.get_allSpots();
        if (getArguments() != null) {
            Long spotId = Long.valueOf(getArguments().getString(ARG_SPOT_ID));
            curSpot = spotHashMap.get(spotId);
        }else if(savedInstanceState!=null) {
            Long spotId = savedInstanceState.getLong(SPOT_ID);
            curSpot = spotHashMap.get(spotId);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                    linearLayout.addView(getDetailItem(getContext(), getString(R.string.spotinfo_site), value, TXT_TYPE.SITE));
                    enableDetails = true;
                }
                if ((value = curSpot.getContact()) != null && !value.equals("")) {
                    linearLayout.addView(getDetailItem(getContext(), getString(R.string.spotinfo_phone), value,TXT_TYPE.PHONE));
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
    /*
                // обработчик переключения вкладок
                tabHost.setOnTabChangedListener(new OnTabChangeListener() {
                    public void onTabChanged(String tabId) {
                        Toast.makeText(getBaseContext(), "tabId = " + tabId, Toast.LENGTH_SHORT).show();
                    }
                });
    */
            } else {
                setVisible(View.GONE, View.GONE, View.VISIBLE);
                return spotInfoView;
            }
            /*else if((txtView =(TextView) spotInfoView.findViewById(R.id.spotInfo_txt_noPartersCoaches))!=null) {
                if((tabHost = (TabHost) spotInfoView.findViewById(R.id.spotInfo_tabHost))!=null)
                    tabHost.setVisibility(View.GONE);
                txtView.setVisibility(View.VISIBLE);
            }*/


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
                personIdLst = new ArrayList<Long>();
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
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity)activity).setmTitle(activity.getString(R.string.spotinfo_fragmentTitle));
        ((MainActivity)activity).getSupportActionBar().setHomeAsUpIndicator(null);
        ((MainActivity)activity).restoreActionBar();
        ImageButton imgButton;
        if ((imgButton = (ImageButton) spotInfoView.findViewById(R.id.spotInfo_btnImg_favorite)) != null) {
            isFavorite = LocalDataManager.isMyFavoriteSpot(curSpot);
            //imgButton.setPressed(LocalDataManager.isMyFavoriteSpot(curSpot));
            setButtonImg(imgButton,isFavorite);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(SPOT_ID, curSpot.getId());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        /*if(isFavoriteChanged) {

            //new EndpointApi.UpdateSpotAsyncTask(this).execute(spotHashMap.get(curSpot.getId()));


        }*/
    }


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    class StarBtnOnTouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageButton btn = (ImageButton) v;
            // show interest in events resulting from ACTION_DOWN
            if(event.getAction()==MotionEvent.ACTION_DOWN) {
                /*btn.setPressed(!btn.isPressed());
                isFavoriteChanged = !isFavoriteChanged;*/
                setButtonImg(btn, !isFavorite);
                //SpotsData.setSpotFavorite(curSpot.getId(), btn.isPressed());
                SpotsData.setSpotFavorite(curSpot.getId(), isFavorite);
                /*new EndpointApi.SetSpotAsFavoriteAsyncTask(getFragmentRef()).execute(new Pair<>(new Pair<>( new Pair<>(curSpot.getId(),btn.isPressed()),
                        new Pair<>( LocalDataManager.getMyPersonInfo().getId(),LocalDataManager.getMyPersonInfo().getPass())),
                        LocalDataManager.getMyPersonInfo().getType()));*/
                new EndpointApi.SetSpotAsFavoriteAsyncTask(getFragmentRef()).execute(new Pair<>(new Pair<>( new Pair<>(curSpot.getId(),isFavorite),
                        new Pair<>( LocalDataManager.getMyPersonInfo().getId(),LocalDataManager.getMyPersonInfo().getPass())),
                        LocalDataManager.getMyPersonInfo().getType()));
                return true;
            }
            return true;
        }
    }

    @Override
    public void onGetListPersonByIdLstFinish(Pair<List<Person>, Exception> result) {
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

    }

    private int fillPersonsList(List<Person> personList, String typeOfPerson)
    {
        ListView listView=null;
        if(personList==null || personList.isEmpty())
            return 0;
        if(typeOfPerson.equals(COACHES))
            listView = (ListView) spotInfoView.findViewById(R.id.spotInfo_list_tab_coaches);
        else if(typeOfPerson.equals(PARTNERS))
            listView = (ListView) spotInfoView.findViewById(R.id.spotInfo_list_tab_partners);
        else
            assert true;
        listView.getLayoutParams().height = android.app.ActionBar.LayoutParams.MATCH_PARENT;
        ProfileItemLstAdapter partnersAdapter = new ProfileItemLstAdapter(activity.getApplicationContext(),
                new ArrayList<>(personList));
        listView.setAdapter(partnersAdapter);
        listView.setOnItemClickListener(new OnPersonClick(personList));
        return personList.size();
    }
    /*@Override
    public void onUpdateSpotFinish(Pair<Spot, Exception> result) {
        Spot spot = result.first;
        Exception error = result.second;
        if(error == null && spot!=null) {
            Log.d(TAG, "Spot updated success");
            return;
        }
            Log.e(TAG, "Error UpdateSpot");
        if(error!=null)
        {
            Log.e(TAG, error.getMessage());
            error.printStackTrace();
        }
        else
            Log.d(TAG,"UpdatedSpot = null");
    }
*/

    @Override
    public void onSetSpotAsFavoriteFinish(Pair<Boolean,Exception> result) {
        boolean isFavoriteReq = result.first;
        Exception ex = result.second;

        if(ex!=null)
        {
            if(spotInfoView!=null) {
                ImageButton btn = (ImageButton) spotInfoView.findViewById(R.id.spotInfo_btnImg_favorite);
                if (btn!=null) {
                    /*if(btn.isPressed()==isFavorite)
                        btn.setPressed(!isFavorite);*/
                    if(this.isFavorite == isFavoriteReq)
                        setButtonImg(btn,!isFavoriteReq);
                    SpotsData.setSpotFavorite(curSpot.getId(), !isFavoriteReq);
                    if(activity!=null)
                        Toast.makeText(activity.getBaseContext(), R.string.spotinfo_req_error_msg, Toast.LENGTH_SHORT).show();
                    else{
                        Log.e(TAG, "current fragment isn't attached to activity");
                        return;
                    }

                }
            }
        }
    }
    private void setButtonImg(ImageButton btn, boolean isFavorite){
        this.isFavorite = isFavorite;
        if (isFavorite)
            btn.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.simple_star_press));
        else
            btn.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.simple_star));
    }
    private class TryAgainClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            if(personIdLst.size()>0) {
                setVisibleProgressBar();
                new EndpointApi.GetListPersonByIdLstAsyncTask(getFragmentRef()).execute(personIdLst);

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
            LinearLayout linearLayout = (LinearLayout) v.getParent();
            int index = linearLayout.indexOfChild(v);
            Intent intent = new Intent(activity, ImgViewPagerActivity.class);
            if(curSpot!=null) {
                List<Picture> picLst = curSpot.getPictureLst();
                if(picLst!=null) {
                    GsonFactory gsonFactory = new GsonFactory();
                    try {
                        ArrayList<String> picList = new ArrayList<String>();
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
        }
    }
    private Fragment getFragmentRef()
    {
        return  this;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);

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
                        /*ImageView imageView = (ImageView) itemView.findViewById(R.id.spotInfo_detailItem_image_phone);
                        imageView.setVisibility(View.VISIBLE);*/
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
