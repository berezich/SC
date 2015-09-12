package com.berezich.sportconnector;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Pair;

import com.berezich.sportconnector.backend.sportConnectorApi.SportConnectorApi;
import com.berezich.sportconnector.backend.sportConnectorApi.model.AccountForConfirmation;
import com.berezich.sportconnector.backend.sportConnectorApi.model.FileUrl;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.berezich.sportconnector.backend.sportConnectorApi.model.UpdateSpotInfo;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sashka on 01.07.2015.
 */
public class EndpointApi {
    private static SportConnectorApi srvApi = null;
    private static String SERVICE_ACCOUNT_EMAIL = "182489181232-bbiekce9fgm6gtelunr9lp82gmdk3uju@developer.gserviceaccount.com";
    private static String USERINFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private static String FILE_NAME = "file";
    private static void setSrvApi(Context context)
    {
        setSrvApi(context, false);
    }
    private static void setSrvApi(Context context, boolean isLocalhost)
    {
        if(srvApi == null) {  // Only do this once
            SportConnectorApi.Builder builder=null;
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
                HttpTransport httpTransport =  AndroidHttp.newCompatibleTransport();
                AndroidJsonFactory androidJsonFactory =  new AndroidJsonFactory();
                InputStream ins = context.getResources().openRawResource(R.raw.key);

                try {
                    File file = createFileFromInputStream(context,ins);
                    GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                            .setJsonFactory(androidJsonFactory)
                            .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                            .setServiceAccountScopes(Collections.singleton(USERINFO_EMAIL_SCOPE))
                            .setServiceAccountPrivateKeyFromP12File(file)
                            .build();
                    file.delete();
                    builder = new SportConnectorApi.Builder(
                            httpTransport,androidJsonFactory , credential).setRootUrl(url);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            // end options for devappserver
            if(builder!=null)
                srvApi = builder.build();
        }
    }
    private static File createFileFromInputStream(Context context, InputStream inputStream) {

        try {
            File f = new File(context.getFilesDir(), FILE_NAME);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
            } catch (Exception e) {
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
            } catch (Exception e) {
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
            } catch (Exception e) {
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
                return new Pair<List<Person>,Exception>(srvApi.listPersonByIdLst(idLst).execute().getItems(),null);
            } catch (Exception e) {
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
            } catch (Exception e) {
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

    public static class SetSpotAsFavoriteAsyncTask extends AsyncTask< Pair<Long,String>, Void, Pair<Boolean ,Exception> >{
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
        protected Pair<Boolean,Exception> doInBackground(Pair<Long,String>... params) {
            Long spot = params[0].first;
            String person = params[0].second;
            boolean isFavorite = (params[1].first==0) ? false : true ;
            String personType = params[1].second;
            if(spot!=null && person!=null)
            try {
                srvApi.setSpotAsFavorite( Long.valueOf(person),spot,isFavorite,personType).execute();
            } catch (IOException e) {
                return new Pair<Boolean,Exception>(isFavorite,e);
            } catch (Exception e)
            {
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

    public static class AuthorizePersonAsyncTask extends AsyncTask<String, Void, Pair<Person,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public AuthorizePersonAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for AuthorizePersonAsyncTask");
            }
        }
        public AuthorizePersonAsyncTask(Activity activity)
        {
            context = activity.getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnAction for AuthorizePersonAsyncTask");
            }
        }
        @Override
        protected Pair<Person,Exception> doInBackground(String... params) {
            String email,pass;
            String url;
            email = params[0];
            pass = params[1];
            try {
                return new Pair<Person,Exception>(srvApi.authorizePerson(email,pass).execute(),null);
            } catch (Exception e) {
                return new Pair<Person, Exception>(null, e);
            }
        }

        @Override
        protected void onPostExecute(Pair<Person,Exception> result) {
            listener.onAuthorizePersonAsyncTaskFinish(result);
        }

        public static interface OnAction
        {
            void onAuthorizePersonAsyncTaskFinish(Pair<Person,Exception> result);
        }
    }

    public static class RegisterPersonAsyncTask extends AsyncTask<String, Void, Pair<AccountForConfirmation,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public RegisterPersonAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for RegisterPersonAsyncTask");
            }
        }
        @Override
        protected Pair<AccountForConfirmation,Exception> doInBackground(String... params) {
            String url;
            AccountForConfirmation account = new AccountForConfirmation();
            account.setEmail(params[0]);
            account.setName(params[1]);
            account.setPass(params[2]);
            account.setType(params[3]);
            try {
                return new Pair<AccountForConfirmation,Exception>(srvApi.registerAccount(account).execute(),null);
            } catch (Exception e) {
                return new Pair<AccountForConfirmation,Exception>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<AccountForConfirmation,Exception> result) {
            listener.onRegisterAccountAsyncTaskFinish(result);
        }

        public static interface OnAction
        {
            void onRegisterAccountAsyncTaskFinish(Pair<AccountForConfirmation, Exception> result);
        }
    }

    public static class UpdatePersonAsyncTask extends AsyncTask< Person, Void, Pair<Person,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public UpdatePersonAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for UpdatePersonAsyncTask");
            }
        }
        @Override
        protected Pair<Person,Exception> doInBackground(Person... params) {
            Person person = params[0];
            Person updatedPerson;
            try {
                updatedPerson = srvApi.updatePerson(person.getId(),person).execute();
                return new Pair<Person,Exception>(updatedPerson,null);
            } catch (Exception e) {
                return new Pair<Person,Exception>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<Person,Exception> result) {
            listener.onUpdatePersonFinish(result);
        }

        public static interface OnAction
        {
            void onUpdatePersonFinish(Pair<Person,Exception> result);
        }
    }

    public static class ChangePassAsyncTask extends AsyncTask< Pair<Long,String>, Void, Exception >{
        private OnAction listener=null;
        private Context context = null;
        public ChangePassAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for ChangePassAsyncTask");
            }
        }
        @Override
        protected Exception doInBackground(Pair<Long,String>... params) {
            Long id = params[0].first;
            String oldPass = params[0].second;
            String newPass = params[1].second;
            try {
                srvApi.changePass(id,newPass,oldPass).execute();
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception result) {
            listener.onChangePassFinish(result);
        }

        public static interface OnAction
        {
            void onChangePassFinish(Exception result);
        }
    }

    public static class ChangeEmailAsyncTask extends AsyncTask< Pair<Long,String>, Void, Exception >{
        private OnAction listener=null;
        private Context context = null;
        public ChangeEmailAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for ChangeEmailAsyncTask");
            }
        }
        @Override
        protected Exception doInBackground(Pair<Long,String>... params) {
            Long id = params[0].first;
            String oldEmail = params[0].second;
            String newEmail = params[1].second;
            try {
                srvApi.changeEmail(id, newEmail, oldEmail).execute();
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception result) {
            listener.onChangeEmailFinish(result);
        }

        public static interface OnAction
        {
            void onChangeEmailFinish(Exception result);
        }
    }


    public static class GetUrlForUploadAsyncTask extends AsyncTask<String, Void, Pair<List<String>,Exception> >{
        private OnGetRegionAsyncTaskAction listener=null;
        private Context context = null;
        public GetUrlForUploadAsyncTask(Fragment fragment)
        {
            context = fragment.getActivity().getBaseContext();
            setSrvApi(context);
            try {
                listener = (OnGetRegionAsyncTaskAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement onGeUrlForUploadAsyncTaskFinish for GetUrlForUploadAsyncTask");
            }
        }
        @Override
        protected Pair<List<String>,Exception> doInBackground(String ...params) {
            String fileForUpload = params[0];
            List<String> strings = new ArrayList<>();
            strings.add(fileForUpload);
            try {
                FileUrl fileUrl = srvApi.getUrlForUpload().execute();
                strings.add( fileUrl.getUrlForUpload());
                return new Pair<List<String>,Exception>(strings,null);
            } catch (Exception e) {
                return new Pair<List<String>,Exception>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<List<String>,Exception> result) {
            listener.onGeUrlForUploadAsyncTaskFinish(result);
        }

        public static interface OnGetRegionAsyncTaskAction
        {
            void onGeUrlForUploadAsyncTaskFinish(Pair<List<String>,Exception> result);
        }
    }
}

