package com.berezich.sportconnector;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import com.berezich.sportconnector.GoogleMap.GoogleMapFragment;
import com.berezich.sportconnector.GoogleMap.SpotsData;
import com.berezich.sportconnector.MainFragment.Filters;
import com.berezich.sportconnector.SpotInfo.SpotInfoFragment;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.berezich.sportconnector.backend.sportConnectorApi.model.UpdateSpotInfo;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        MainFragment.OnActionListenerMainFragment, GoogleMapFragment.OnActionListenerGMapFragment,
        EndpointApi.GetRegionAsyncTask.OnGetRegionAsyncTaskAction,
        EndpointApi.GetSpotListAsyncTask.OnAction,
        EndpointApi.GetUpdatedSpotListAsyncTask.OnAction {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private static final String TAG = "YaMapFragment";
    private static Long regionId = new Long(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*RegionInfo regionInfo = new RegionInfo();
        regionInfo.setId(regionId);
        regionInfo.setVersion("1.0.0.1");
        regionInfo.setLastSpotUpdate(new DateTime(new Date().getTime()-24*6*60*60*1000));
        regionInfo.setRegionName("moscow");
        try {
            LocalDataManager.saveRegionInfoToPref(regionInfo,this);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        LocalDataManager.init(this.getBaseContext());
        new EndpointApi.GetRegionAsyncTask(this).execute(regionId);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                    .replace(R.id.container, new MainFragment().setArgs(position ))
                    .commit();
                break;
            default:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new MainFragment().setArgs(position))
                        .commit();
        }
    }
    @Override
    public void onBtnClickMF(Filters filter, int sectionNumber)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.container, new YaMapFragment().setArgs(sectionNumber,filter)).commit();
        fragmentManager.beginTransaction().replace(R.id.container, new GoogleMapFragment().setArgs(sectionNumber,filter)).addToBackStack("tr1").commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.frame_main_title);
                break;
            case 1:
                mTitle = getString(R.string.frame_profile_title);
                break;
            case 2:
                mTitle = getString(R.string.frame_msg_title);
                break;
            case 3:
                mTitle = getString(R.string.frame_friends_title);
                break;
            case 4:
                mTitle = getString(R.string.frame_photo_title);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInfoWindowClickGMF(int spotId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.container, new YaMapFragment().setArgs(sectionNumber,filter)).commit();
        //fragmentManager.beginTransaction().replace(R.id.container, new GoogleMapFragment().setArgs(sectionNumber,filter)).commit();
        fragmentManager.beginTransaction().replace(R.id.container, SpotInfoFragment.newInstance(spotId)).addToBackStack("tr2").commit();

    }

    @Override
    public void onGetRegionAsyncTaskFinish(Pair<RegionInfo, Exception> result) {
        String resText="";
        Exception error = result.second;
        RegionInfo regionInfo=result.first, localRegionInfo = null;
        if(error!=null)
            resText = result.second.getMessage();
        else if(regionInfo!=null)
            resText = result.first.toString();

        Toast.makeText(getBaseContext(), resText, Toast.LENGTH_LONG).show();

        if(error==null && regionInfo!=null)
        {
            try {
                if(LocalDataManager.loadRegionInfoFromPref(this))
                    if ((localRegionInfo = LocalDataManager.getRegionInfo()) != null)
                        if (localRegionInfo.getVersion().equals(regionInfo.getVersion()))
                            if (localRegionInfo.getLastSpotUpdate().getValue() - regionInfo.getLastSpotUpdate().getValue()<0) {
                                //get list of updated spots and update existed
                                Toast.makeText(getBaseContext(),"get list of updated spots and update existed",
                                        Toast.LENGTH_LONG).show();
                                LocalDataManager.setRegionInfo(regionInfo);
                                SpotsData.loadSpotsFromCache();
                                new EndpointApi.GetUpdatedSpotListAsyncTask(this).execute(
                                        new Pair<Long, DateTime>(regionInfo.getId(), regionInfo.getLastSpotUpdate()));
                                return;
                            }
                            else {
                                //we have actual spot information
                                Toast.makeText(getBaseContext(),"we have actual spot information",Toast.LENGTH_LONG).show();
                                SpotsData.loadSpotsFromCache();
                                Toast.makeText(getBaseContext(), SpotsData.get_allSpots().toString(), Toast.LENGTH_LONG).show();
                                return;
                            }
                //get all spots from server
                LocalDataManager.setRegionInfo(regionInfo);
                new EndpointApi.GetSpotListAsyncTask(this).execute(regionId);
                //Toast.makeText(getBaseContext(),"get all spots from server",Toast.LENGTH_LONG).show();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"Error loading regionInfo from Preferences");
            }
        }

        Log.e(TAG,"Error get regionInfo from server");
        if(error!=null)
        {
            Log.e(TAG,error.getMessage());
            error.printStackTrace();
        }
        else
            Log.e(TAG,"regionInfo = null");
    }

    @Override
    public void onGetSpotListFinish(Pair<List<Spot>, Exception> result) {
        Exception error = result.second;
        List<Spot> spotLst = result.first;

        if(error == null && spotLst!=null)
        {
            try {
                LocalDataManager.saveRegionInfoToPref(this);
                SpotsData.saveSpotsToCache(spotLst);
                Toast.makeText(getBaseContext(),"got all spots from server and saveRegionInfo to pref",Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        Log.e(TAG,"Error get ListSpot from server");
        if(error!=null)
        {
            Log.e(TAG,error.getMessage());
            error.printStackTrace();
        }
        else
            Log.e(TAG,"ListSpot = null");
    }

    @Override
    public void onGetUpdateSpotListFinish(Pair<List<UpdateSpotInfo>, Exception> result) {
        Exception error = result.second;
        List<Spot> spotLst = new ArrayList<Spot>();
        List<UpdateSpotInfo> updateSpotInfoLst = result.first;

        if(error == null && updateSpotInfoLst!=null)
        {
            try {
                LocalDataManager.saveRegionInfoToPref(this);
                SpotsData.setSpotUpdatesToCache(updateSpotInfoLst);
                Toast.makeText(getBaseContext(),"got updated spots from server and saveRegionInfo to pref",Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        Log.e(TAG,"Error get updated ListSpot from server");
        if(error!=null)
        {
            Log.e(TAG,error.getMessage());
            error.printStackTrace();
        }
        else
            Log.e(TAG,"ListUpdatedSpot = null");
    }
}
