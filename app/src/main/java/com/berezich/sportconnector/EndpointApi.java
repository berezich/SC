package com.berezich.sportconnector;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.widget.Toast;

import com.berezich.sportconnector.backend.sportConnectorApi.SportConnectorApi;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.berezich.sportconnector.backend.sportConnectorApi.model.UpdateSpotInfo;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.DateTime;
import com.google.maps.android.geometry.Bounds;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Sashka on 01.07.2015.
 */
public class EndpointApi {
    private static SportConnectorApi srvApi = null;

    private static void setSrvApi(Context context)
    {
        setSrvApi(context, false);
    }
    private static void setSrvApi(Context context, boolean isLocalhost)
    {
        if(srvApi == null) {  // Only do this once
            SportConnectorApi.Builder builder;
            if(isLocalhost) {
                builder = new SportConnectorApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // options for running against local devappserver
                        // - 10.0.2.2 is localhost's IP address in Android emulator
                        // - turn off compression when running against local devappserver
                        .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
            }
            else {
                String url = context.getString(R.string.srvApi_url);
                builder = new SportConnectorApi.Builder(
                        AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null).setRootUrl(url);
            }
            // end options for devappserver

            srvApi = builder.build();
        }
    }

    public static class GetRegionAsyncTask extends AsyncTask<Long, Void, Pair<RegionInfo,Exception> >{
        private OnGetRegionAsyncTaskAction listener=null;
        private Context context = null;
        public GetRegionAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnGetRegionAsyncTaskAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnGetRegionAsyncTaskAction for GetRegionAsyncTask");
            }
        }
        public GetRegionAsyncTask(Activity activity)
        {
            context = activity.getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnGetRegionAsyncTaskAction) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnGetRegionAsyncTaskAction for GetRegionAsyncTask");
            }
        }
        @Override
        protected Pair<RegionInfo,Exception> doInBackground(Long... params) {
            Long regionId;
            String url;
            regionId = params[0];
            try {
                return new Pair<RegionInfo,Exception>(srvApi.getRegionInfo(regionId).execute(),null);
            } catch (IOException e) {
                return new Pair<RegionInfo,Exception>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<RegionInfo,Exception> result) {
            listener.onGetRegionAsyncTaskFinish(result);
        }

        public static interface OnGetRegionAsyncTaskAction
        {
            void onGetRegionAsyncTaskFinish(Pair<RegionInfo,Exception> result);
        }
    }

    public static class GetSpotListAsyncTask extends AsyncTask<Long, Void, Pair<List<Spot>,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public GetSpotListAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for GetSpotListAsyncTask");
            }
        }
        @Override
        protected Pair<List<Spot>,Exception> doInBackground(Long... params) {
            Long regionId;
            String url;
            regionId = params[0];
            try {
                return new Pair<List<Spot>,Exception>(srvApi.listSpotByRegId(regionId).execute().getItems(),null);
            } catch (IOException e) {
                return new Pair<List<Spot>,Exception>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<List<Spot>,Exception> result) {
            listener.onGetSpotListFinish(result);
        }

        public static interface OnAction
        {
            void onGetSpotListFinish(Pair<List<Spot>,Exception> result);
        }
    }


    public static class GetUpdatedSpotListAsyncTask extends AsyncTask<Pair<Long,DateTime>, Void, Pair<List<UpdateSpotInfo>,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public GetUpdatedSpotListAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for GetUpdatedSpotListAsyncTask");
            }
        }
        @Override
        protected Pair<List<UpdateSpotInfo>,Exception> doInBackground(Pair<Long,DateTime>... params) {
            Long regionId;
            DateTime lastUpdate;
            String url;
            regionId = params[0].first;
            lastUpdate = params[0].second;
            try {
                return new Pair<List<UpdateSpotInfo>,Exception>(srvApi.listUpdateSpotInfoByRegIdDate(lastUpdate, regionId).execute().getItems(),null);
            } catch (IOException e) {
                return new Pair<List<UpdateSpotInfo>,Exception>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<List<UpdateSpotInfo>,Exception> result) {
            listener.onGetUpdateSpotListFinish(result);
        }

        public static interface OnAction
        {
            void onGetUpdateSpotListFinish(Pair<List<UpdateSpotInfo>,Exception> result);
        }
    }

    public static class GetListPersonByIdLstAsyncTask extends AsyncTask< List<Long>, Void, Pair<List<Person>,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public GetListPersonByIdLstAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for GetListPersonByIdLstAsyncTask");
            }
        }
        @Override
        protected Pair<List<Person>,Exception> doInBackground(List<Long>... params) {
            List<Long> idLst = new ArrayList<Long>(params[0]) ;
            try {
                return new Pair<List<Person>,Exception>(srvApi.listPersonByIdLst(new ArrayList<Long>(idLst)).execute().getItems(),null);
            } catch (IOException e) {
                return new Pair<List<Person>,Exception>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<List<Person>,Exception> result) {
            listener.onGetListPersonByIdLstFinish(result);
        }

        public static interface OnAction
        {
            void onGetListPersonByIdLstFinish(Pair<List<Person>,Exception> result);
        }
    }

    public static class UpdateSpotAsyncTask extends AsyncTask< Spot, Void, Pair<Spot,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public UpdateSpotAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for UpdateSpotAsyncTask");
            }
        }
        @Override
        protected Pair<Spot,Exception> doInBackground(Spot... params) {
            Spot spot = params[0];
            Spot updatedSpot;
            try {
                updatedSpot = srvApi.updateSpot(spot.getId(),spot).execute();
                return new Pair<Spot,Exception>(updatedSpot,null);
            } catch (IOException e) {
                return new Pair<Spot,Exception>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<Spot,Exception> result) {
            listener.onUpdateSpotFinish(result);
        }

        public static interface OnAction
        {
            void onUpdateSpotFinish(Pair<Spot,Exception> result);
        }
    }

    public static class SetSpotAsFavoriteAsyncTask extends AsyncTask< Long, Void, Pair<Boolean ,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public SetSpotAsFavoriteAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for SetSpotAsFavoriteAsyncTask");
            }
        }
        @Override
        protected Pair<Boolean,Exception> doInBackground(Long... params) {
            Long spot = params[0];
            Long person = params[1];
            boolean isFavorite = (params[2]==0) ? false : true ;
            String personType = (params[3]==1) ? "PARTNER":"COACH";
            if(spot!=null && person!=null)
            try {
                srvApi.setSpotAsFavorite(person,spot,isFavorite,personType).execute();
            } catch (IOException e) {
                return new Pair<Boolean,Exception>(isFavorite,e);
            }
            return new Pair<Boolean,Exception>(isFavorite,null);
        }

        @Override
        protected void onPostExecute(Pair<Boolean,Exception> result) {
            listener.onSetSpotAsFavoriteFinish(result);
        }

        public static interface OnAction
        {
            void onSetSpotAsFavoriteFinish(Pair<Boolean,Exception> result);
        }
    }


}

