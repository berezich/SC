package com.berezich.sportconnector.EndpointApi;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;

import com.berezich.sportconnector.GoogleMap.SpotsData;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.berezich.sportconnector.backend.sportConnectorApi.model.UpdateSpotInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by berezkin on 11.11.2015.
 */
public class SyncSpots implements
        EndpointApi.GetRegionAsyncTask.OnGetRegionAsyncTaskAction,
        EndpointApi.GetSpotListAsyncTask.OnAction,
        EndpointApi.GetUpdatedSpotListAsyncTask.OnAction{
    public enum ReqState {REQ_REGINFO,REQ_SPOT_LIST,REQ_UPDATE_SPOTS,EVERYTHING_LOADED}
    private Context ctx;
    private String TAG;
    private Fragment targetFragment;
    private ReqState reqState = ReqState.REQ_REGINFO;
    private RegionInfo regionInfo;
    private RegionInfo localRegionInfo;
    private OnActionSyncSpots listener;
    private Activity activity;

    public SyncSpots( Fragment fragment,String tag) {
        TAG = tag;
        targetFragment = fragment;
        try{
            listener = (OnActionSyncSpots) fragment;
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void setReqState(ReqState reqState) {
        this.reqState = reqState;
    }

    public void startSync(Context ctx,Long regionId){
        this.ctx = ctx;
        activity = targetFragment.getActivity();
        switch (reqState){
            case REQ_REGINFO:
                new EndpointApi.GetRegionAsyncTask(ctx,this).execute(regionId);
                break;
            case REQ_SPOT_LIST:
                new EndpointApi.GetSpotListAsyncTask(ctx,this).execute(regionId);
                break;
            case REQ_UPDATE_SPOTS:
                if(regionInfo!=null && localRegionInfo!=null)
                    new EndpointApi.GetUpdatedSpotListAsyncTask(ctx,this).execute(
                            new Pair(regionInfo.getId(), localRegionInfo.getLastSpotUpdate()));
                break;
            default:
                listener.syncFinish(null,reqState);
        }
    }

    @Override
    public void onGetRegionAsyncTaskFinish(Pair<RegionInfo, Exception> result) {
        Exception exception = result.second;
        regionInfo=result.first;
        if(activity==null)
        {
            Log.e(TAG, "current fragment isn't attached to activity");
            return;
        }

        if(exception!=null) {
            Log.e(TAG, "Error get regionInfo from server");
            listener.syncFinish(exception, reqState);
            return;
            /*ErrorVisualizer.showErrorAfterReq(activity.getBaseContext(),
                    (FrameLayout) rootView.findViewById(R.id.main_frg_frameLayout), exception, TAG);*/
        }
        else if (regionInfo!=null)
        {
            Log.d(TAG,String.format("regionInfo load from server\n%s",regionInfo.toString()));
            if ((localRegionInfo = LocalDataManager.getRegionInfo()) != null) {
                Log.d(TAG, String.format("localRegionInfo: %s", localRegionInfo.toString()));
                if (localRegionInfo.getVersion().equals(regionInfo.getVersion())) {
                    SpotsData.loadSpotsFromCache();
                    if (localRegionInfo.getLastSpotUpdate().getValue()
                            - regionInfo.getLastSpotUpdate().getValue() < 0) {
                        Log.d(TAG, "Get list of updated spots and update existed");
                        reqState = ReqState.REQ_UPDATE_SPOTS;
                        Log.d(TAG, "reqState = " + reqState);
                        new EndpointApi.GetUpdatedSpotListAsyncTask(ctx,this).execute(
                                new Pair<>(regionInfo.getId(), localRegionInfo.getLastSpotUpdate()));
                    } else {
                        Log.d(TAG, "Actual spot information");
                        //setVisibleLayouts(true, false);
                        reqState = ReqState.EVERYTHING_LOADED;
                        Log.d(TAG, "reqState = " + reqState);
                        listener.syncFinish(null, reqState);
                    }
                    return;
                }
            }
            Log.d(TAG, "get all spots from server");
            reqState = ReqState.REQ_SPOT_LIST;
            Log.d(TAG, "reqState = " + reqState);
            new EndpointApi.GetSpotListAsyncTask(ctx,this).execute(regionInfo.getId());
        }
        else {
            Log.e(TAG, "regionInfo from server == null");
            listener.syncFinish(new NullPointerException("regionInfo from server == null"),reqState);
        }
    }

    @Override
    public void onGetSpotListFinish(Pair<List<Spot>, Exception> result) {
        Exception error = result.second;
        List<Spot> spotLst = result.first;
        if(activity==null)
        {
            Log.e(TAG,"current fragment isn't attached to activity");
            return;
        }
        if(spotLst==null)
            spotLst = new ArrayList<>();
        if(error == null && spotLst!=null)
        {
            try {
                Log.d(TAG,String.format("got all spots (%s items) from server",spotLst.size()));
                SpotsData.saveSpotsToCache(spotLst);
                Log.d(TAG, "all spots saved to cache");
                LocalDataManager.setRegionInfo(regionInfo);
                LocalDataManager.saveRegionInfoToPref(activity);
                Log.d(TAG, "updated regionInfo saved to cache");
                //setVisibleLayouts(true, false);
                reqState = ReqState.EVERYTHING_LOADED;
                Log.d(TAG, "reqState = " + reqState);
                listener.syncFinish(null,reqState);
            } catch (IOException e) {
                /*ErrorVisualizer.showErrorAfterReq(getActivity().getBaseContext(),
                        (FrameLayout) rootView.findViewById(R.id.main_frg_frameLayout), error, TAG);*/
                listener.syncFinish(e, reqState);
            }
        }
        Log.e(TAG, "Error get ListSpot from server");
        if(error==null)
            Log.e(TAG,"ListSpot = null");

        /*ErrorVisualizer.showErrorAfterReq(getActivity().getBaseContext(),
                (FrameLayout) rootView.findViewById(R.id.main_frg_frameLayout), error, TAG);*/
        listener.syncFinish(new NullPointerException("ListSpot == null"),reqState);
    }

    @Override
    public void onGetUpdateSpotListFinish(Pair<List<UpdateSpotInfo>, Exception> result) {
        Exception error = result.second;
        if(activity==null)
        {
            Log.e(TAG,"current fragment isn't attached to activity");
            return;
        }
        List<UpdateSpotInfo> updateSpotInfoLst = result.first;

        if(error == null && updateSpotInfoLst!=null)
        {
            try {
                Log.d(TAG, String.format("got updateSpotList (%d items)from server", updateSpotInfoLst.size()));
                SpotsData.setSpotUpdatesToCache(updateSpotInfoLst);
                Log.d(TAG, "updated spots saved to cache");
                LocalDataManager.setRegionInfo(regionInfo);
                LocalDataManager.saveRegionInfoToPref(activity);
                Log.d(TAG, "updated regionInfo saved to cache");
                reqState = ReqState.EVERYTHING_LOADED;
                Log.d(TAG, "reqState = " + reqState);
                //setVisibleLayouts(true,false);
                listener.syncFinish(null,reqState);

            } catch (IOException e) {
                /*ErrorVisualizer.showErrorAfterReq(getActivity().getBaseContext(),
                        (FrameLayout) rootView.findViewById(R.id.main_frg_frameLayout), error, TAG);*/
                listener.syncFinish(e, reqState);
            }
            return;
        }
        Log.e(TAG, "Error get updated ListSpot from server");
        if(error==null)
            Log.e(TAG, "ListUpdatedSpot = null");
        listener.syncFinish(new NullPointerException("ListUpdatedSpot = null"),reqState);
        /*ErrorVisualizer.showErrorAfterReq(getActivity().getBaseContext(),
                (FrameLayout)rootView.findViewById(R.id.main_frg_frameLayout),error,TAG);*/
    }
    public static interface OnActionSyncSpots{
        void syncFinish(Exception ex, ReqState reqState);
    }
}
