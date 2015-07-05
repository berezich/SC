package com.berezich.sportconnector.SpotInfo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.berezich.sportconnector.EndpointApi;
import com.berezich.sportconnector.GoogleMap.SpotsData;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.SportObjects.Person;
import com.berezich.sportconnector.SportObjects.Spot1;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.berezich.sportconnector.backend.sportConnectorApi.model.UpdateSpotInfo;

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
public class SpotInfoFragment extends Fragment implements EndpointApi.GetRegionAsyncTask.OnGetRegionAsyncTaskAction,
                                                          EndpointApi.GetSpotListAsyncTask.OnAction,
                                                          EndpointApi.GetUpdatedSpotListAsyncTask.OnAction
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SPOT_ID = "spotId";
    private static final  String TAB_PARTNERS = "partners";
    private static final  String TAB_COACHES = "coaches";
    private static final String TAG = "SpotInfoFragment";
    // TODO: Rename and change types of parameters
    private int spotId;
    private boolean isFavoriteChanged=false;
    private String mParam2;
    private HashMap<Integer,Spot1> spotHashMap;
    private Spot1 curSpot;
    private View spotInfoView;
    private ProfileItemLstAdapter partnersAdapter;
    private ProfileItemLstAdapter coachesAdapter;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * //@param param1 Parameter 1.
     * //@param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpotInfoFragment newInstance(int spotId) {
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
        if (getArguments() != null) {
            spotId = Integer.valueOf(getArguments().getString(ARG_SPOT_ID));
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView txtView;
        TabHost tabHost;
        ListView lstView;
        ImageButton imgButton;
        spotInfoView = inflater.inflate(R.layout.fragment_spot_info, container, false);
        spotHashMap = SpotsData.get_allSpots1();
        curSpot = spotHashMap.get(spotId);
        if(curSpot!=null)
        {
            if((txtView =(TextView) spotInfoView.findViewById(R.id.spotInfo_txt_name))!=null)
                txtView.setText(curSpot.name());
            if((txtView =(TextView) spotInfoView.findViewById(R.id.spotInfo_txt_adress))!=null)
                txtView.setText(curSpot.adress());
            if((imgButton=(ImageButton) spotInfoView.findViewById(R.id.spotInfo_btnImg_favorite))!=null) {
                imgButton.setPressed(curSpot.favorite());
                imgButton.setOnTouchListener(new StarBtnOnTouchListener());
            }

            if((curSpot.coaches().size()>0 || curSpot.partners().size()>0)&&(tabHost = (TabHost) spotInfoView.findViewById(R.id.spotInfo_tabHost))!=null)
            {
                // инициализация
                tabHost.setup();

                TabHost.TabSpec tabSpec;
                if(curSpot.partners().size()>0) {
                    // создаем вкладку и указываем тег
                    tabSpec = tabHost.newTabSpec(TAB_PARTNERS);
                    // название вкладки
                    tabSpec.setIndicator(getString(R.string.spotinfo_tab1_title));

                    lstView = (ListView) spotInfoView.findViewById(R.id.spotInfo_list_tab_partners);
                    partnersAdapter = new ProfileItemLstAdapter(getActivity().getApplicationContext(),
                            new ArrayList<Person>(curSpot.partners()));
                    lstView.setAdapter(partnersAdapter);

                    // указываем id компонента из FrameLayout, он и станет содержимым
                    tabSpec.setContent(R.id.spotInfo_list_tab_partners);
                    // добавляем в корневой элемент
                    tabHost.addTab(tabSpec);
                }
                if(curSpot.coaches().size()>0 ) {
                    tabSpec = tabHost.newTabSpec(TAB_COACHES);
                    tabSpec.setIndicator(getString(R.string.spotinfo_tab2_title));
                    lstView = (ListView) spotInfoView.findViewById(R.id.spotInfo_list_tab_coaches);
                    coachesAdapter = new ProfileItemLstAdapter(getActivity().getApplicationContext(),
                            new ArrayList<Person>(curSpot.coaches()));
                    lstView.setAdapter(coachesAdapter);
                    tabSpec.setContent(R.id.spotInfo_list_tab_coaches);
                    tabHost.addTab(tabSpec);
                }
                //tabHost.setCurrentTabByTag(TAB_PARTNERS);

    /*
                // обработчик переключения вкладок
                tabHost.setOnTabChangedListener(new OnTabChangeListener() {
                    public void onTabChanged(String tabId) {
                        Toast.makeText(getBaseContext(), "tabId = " + tabId, Toast.LENGTH_SHORT).show();
                    }
                });
    */
            }
            else if((txtView =(TextView) spotInfoView.findViewById(R.id.spotInfo_txt_noPartersCoaches))!=null) {
                if((tabHost = (TabHost) spotInfoView.findViewById(R.id.spotInfo_tabHost))!=null)
                    tabHost.setVisibility(View.GONE);
                txtView.setVisibility(View.VISIBLE);
            }
        }
        //new EndpointApi.GetRegionAsyncTask(this).execute(new Long(1));
        //new EndpointApi.GetSpotListAsyncTask(this).execute(new Long(1));
        //new EndpointApi.GetUpdatedSpotListAsyncTask(this).execute(new Pair<Long, DateTime>(new Long(1), new DateTime(new Date(new Date().getTime()-10*24*60*60*1000), TimeZone.getDefault())));
        return spotInfoView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isFavoriteChanged)
            SpotsData.setSpotFavorite(curSpot.id(),!curSpot.favorite());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    class StarBtnOnTouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageButton btn = (ImageButton) v;
            // show interest in events resulting from ACTION_DOWN
            if(event.getAction()==MotionEvent.ACTION_DOWN) {
                btn.setPressed(!btn.isPressed());
                isFavoriteChanged = !isFavoriteChanged;
                return true;
            }
            return true;
        }
    }

    @Override
    public void onGetRegionAsyncTaskFinish(Pair<RegionInfo,Exception> result) {
        String resText="";
        if(result.second!=null)
            resText = result.second.getMessage();
        else if(result.first!=null)
            resText = result.first.toString();

        Toast.makeText(this.getActivity().getBaseContext(), resText, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetSpotListFinish(Pair<List<com.berezich.sportconnector.backend.sportConnectorApi.model.Spot>, Exception> result) {
        List<com.berezich.sportconnector.backend.sportConnectorApi.model.Spot> spotLst;
        String resText="";
        if(result.second!=null)
            resText = result.second.getMessage();
        else if(result.first!=null) {
            spotLst = result.first;
            com.berezich.sportconnector.backend.sportConnectorApi.model.Spot spot;
            for(int i=0; i<spotLst.size(); i++) {
                spot = spotLst.get(i);
                resText+= spot.toString()+"\n";
            }
        }

        Toast.makeText(this.getActivity().getBaseContext(), resText, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetUpdateSpotListFinish(Pair<List<UpdateSpotInfo>, Exception> result) {
        List<UpdateSpotInfo> updateSpotInfoList;
        String resText="";
        if(result.second!=null)
            resText = result.second.getMessage();
        else if(result.first!=null) {
            updateSpotInfoList = result.first;
            UpdateSpotInfo updateSpotInfo;
            for(int i=0; i<updateSpotInfoList.size(); i++) {
                updateSpotInfo = updateSpotInfoList.get(i);
                resText+= updateSpotInfo.toString()+"\n";
            }
        }
        Toast.makeText(this.getActivity().getBaseContext(), resText, Toast.LENGTH_LONG).show();
        Log.d(TAG,resText);
    }
}
