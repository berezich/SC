package com.berezich.sportconnector.EndpointApi;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Pair;

import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.SportConnectorApi;
import com.berezich.sportconnector.backend.sportConnectorApi.model.AccountForConfirmation;
import com.berezich.sportconnector.backend.sportConnectorApi.model.CollectionResponseSpot;
import com.berezich.sportconnector.backend.sportConnectorApi.model.CollectionResponseUpdateSpotInfo;
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
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EndpointApi {
    private static SportConnectorApi srvApi = null;
    static String SERVICE_ACCOUNT_EMAIL = "182489181232-bbiekce9fgm6gtelunr9lp82gmdk3uju@developer.gserviceaccount.com";
    static String USERINFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    static String FILE_NAME = "file";
    final static int CONNECT_TIMEOUT_MS = 5000;
    final static int READ_TIMEOUT_MS = 5000;
    private static void setSrvApi(Context context)
    {
        setSrvApi(context, false);
    }
    private static void setSrvApi(Context context, boolean isLocalhost){
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
                    if(file!=null) {
                        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                                .setJsonFactory(androidJsonFactory)
                                .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                                .setServiceAccountScopes(Collections.singleton(USERINFO_EMAIL_SCOPE))
                                .setServiceAccountPrivateKeyFromP12File(file)
                                .setRequestInitializer(new HttpRequestInitializer() {
                                    public void initialize(HttpRequest httpRequest) {
                                        httpRequest.setConnectTimeout(CONNECT_TIMEOUT_MS);
                                        httpRequest.setReadTimeout(READ_TIMEOUT_MS);
                                    }
                                })
                                .build();
                        file.delete();
                        builder = new SportConnectorApi.Builder(
                                httpTransport, androidJsonFactory, credential).setRootUrl(url);
                    }
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
        public GetRegionAsyncTask(Context ctx, Object parentObj)
        {
            context = ctx;
            setSrvApi(context);
            try {
                listener = (OnGetRegionAsyncTaskAction) parentObj;
            } catch (ClassCastException e) {
                throw new ClassCastException(parentObj.toString() + " must implement OnGetRegionAsyncTaskAction for GetRegionAsyncTask");
            }
        }
        @Override
        protected Pair<RegionInfo,Exception> doInBackground(Long... params) {
            try {
                Long regionId = params[0];
                return new Pair<>(srvApi.getRegionInfo(regionId).execute(),null);
            } catch (Exception e) {
                return new Pair<>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<RegionInfo,Exception> result) {
            try {
                listener.onGetRegionAsyncTaskFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnGetRegionAsyncTaskAction
        {
            void onGetRegionAsyncTaskFinish(Pair<RegionInfo,Exception> result);
        }
    }

    public static class GetSpotListAsyncTask extends AsyncTask<Long, Void, Pair<List<Spot>,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public GetSpotListAsyncTask(Context ctx, Object parentObj)
        {
            context = ctx;
            setSrvApi(context);
            try {
                listener = (OnAction) parentObj;
            } catch (ClassCastException e) {
                throw new ClassCastException(parentObj.toString() + " must implement OnAction for GetSpotListAsyncTask");
            }
        }
        @Override
        protected Pair<List<Spot>,Exception> doInBackground(Long... params) {
            final int MAX_LIMIT=20;
            try {
                Long regionId = params[0];
                String nextPageToken="";
                List<Spot> spots = new ArrayList<>();
                CollectionResponseSpot response;
                while (true) {
                    if(nextPageToken == null || nextPageToken.equals(""))
                        response = srvApi.listSpotByRegId(regionId).execute();
                    else
                        response = srvApi.listSpotByRegId(regionId).setCursor(nextPageToken).execute();
                    if(response!=null) {
                        if (response.getItems() != null && response.getItems().size()>0) {
                            spots.addAll(response.getItems());
                            nextPageToken = response.getNextPageToken();
                            if (response.getItems().size() == MAX_LIMIT)
                                continue;
                        }
                    }
                    break;
                }
                return new Pair<>(spots,null);
            } catch (Exception e) {
                return new Pair<>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<List<Spot>,Exception> result) {
            try {
                listener.onGetSpotListFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onGetSpotListFinish(Pair<List<Spot>,Exception> result);
        }
    }

    public static class GetUpdatedSpotListAsyncTask extends AsyncTask<Pair<Long,DateTime>, Void, Pair<List<UpdateSpotInfo>,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public GetUpdatedSpotListAsyncTask(Context ctx, Object parentObj)
        {
            context = ctx;
            setSrvApi(context);
            try {
                listener = (OnAction) parentObj;
            } catch (ClassCastException e) {
                throw new ClassCastException(parentObj.toString() + " must implement OnAction for GetUpdatedSpotListAsyncTask");
            }
        }
        @Override
        protected Pair<List<UpdateSpotInfo>,Exception> doInBackground(Pair<Long,DateTime>... params) {
            int MAX_LIMIT = 20;
            try {
                Long regionId;
                DateTime lastUpdate;
                regionId = params[0].first;
                lastUpdate = params[0].second;
                String nextPageToken="";
                CollectionResponseUpdateSpotInfo response;
                List<UpdateSpotInfo> spotInfos = new ArrayList<>();

                while (true) {
                    if(nextPageToken == null || nextPageToken.equals(""))
                        response = srvApi.listUpdateSpotInfoByRegIdDate(lastUpdate, regionId).execute();
                    else
                        response = srvApi.listUpdateSpotInfoByRegIdDate(lastUpdate, regionId).setCursor(nextPageToken).execute();
                    if(response!=null) {
                        if (response.getItems() != null && response.getItems().size()>0) {
                            spotInfos.addAll(response.getItems());
                            nextPageToken = response.getNextPageToken();
                            if (response.getItems().size() == MAX_LIMIT)
                                continue;
                        }
                    }
                    break;
                }

                return new Pair<>(spotInfos,null);
            } catch (Exception e) {
                return new Pair<>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<List<UpdateSpotInfo>,Exception> result) {
            try {
                listener.onGetUpdateSpotListFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onGetUpdateSpotListFinish(Pair<List<UpdateSpotInfo>,Exception> result);
        }
    }

    public static class GetListPersonByIdLstAsyncTask extends AsyncTask< List<Long>, Void, Pair<List<Person>,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public GetListPersonByIdLstAsyncTask(Fragment fragment)
        {
            context = fragment.getContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for GetListPersonByIdLstAsyncTask");
            }
        }
        @Override
        protected Pair<List<Person>,Exception> doInBackground(List<Long>... params) {
            int BATCH_SIZE = 20;
            try {
                List<Long> idLst = new ArrayList<>(params[0]) ;
                List<Person> persons = new ArrayList<>();
                List<Long> batchPersons;
                int cur=0;

                while (cur < idLst.size()) {
                    batchPersons = idLst.subList(cur,(cur+BATCH_SIZE)<idLst.size() ? cur+BATCH_SIZE : idLst.size());
                    persons.addAll(srvApi.listPersonByIdLst(batchPersons).execute().getItems());
                    cur+=BATCH_SIZE;
                }
                return new Pair<>(persons,null);
            } catch (Exception e) {
                return new Pair<>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<List<Person>,Exception> result) {
            try {
                listener.onGetListPersonByIdLstFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onGetListPersonByIdLstFinish(Pair<List<Person>,Exception> result);
        }
    }

     public static class SetSpotAsFavoriteAsyncTask extends AsyncTask< Pair<Pair<Pair<Long,Boolean>,Pair<Long,String>>,String>, Void, Pair<Boolean ,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public SetSpotAsFavoriteAsyncTask(Fragment fragment)
        {
            context = fragment.getContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for SetSpotAsFavoriteAsyncTask");
            }
        }
        @Override
        protected Pair<Boolean,Exception> doInBackground(Pair<Pair<Pair<Long,Boolean>,Pair<Long,String>>,String>... params) {
            boolean isFavorite;
            try {
                isFavorite = params[0].first.first.second;
            } catch (Exception e) {
                e.printStackTrace();
                return new Pair<>(false,e);
            }

            try {
                Long spot = params[0].first.first.first;
                Long person = params[0].first.second.first;
                String pass = params[0].first.second.second;
                String personType = params[0].second;
                if(spot!=null && person!=null)

                srvApi.setSpotAsFavorite( person,spot,isFavorite,pass,personType).execute();
            } catch (IOException e) {
                return new Pair<Boolean,Exception>(isFavorite,e);
            } catch (Exception e)
            {
                return new Pair<>(isFavorite,e);
            }
            return new Pair<>(isFavorite,null);
        }

        @Override
        protected void onPostExecute(Pair<Boolean,Exception> result) {
            try {
                listener.onSetSpotAsFavoriteFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onSetSpotAsFavoriteFinish(Pair<Boolean,Exception> result);
        }
    }

    public static class AuthorizePersonAsyncTask extends AsyncTask<String, Void, Pair<Person,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public AuthorizePersonAsyncTask(Fragment fragment)
        {
            context = fragment.getContext();
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
            try {
                String email = params[0];
                String pass = params[1];
                return new Pair<>(srvApi.authorizePerson(email,pass).execute(),null);
            } catch (Exception e) {
                return new Pair<>(null, e);
            }
        }

        @Override
        protected void onPostExecute(Pair<Person,Exception> result) {
            try {
                listener.onAuthorizePersonAsyncTaskFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onAuthorizePersonAsyncTaskFinish(Pair<Person,Exception> result);
        }
    }

    public static class RegisterPersonAsyncTask extends AsyncTask<String, Void, Pair<AccountForConfirmation,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public RegisterPersonAsyncTask(Fragment fragment)
        {
            context = fragment.getContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for RegisterPersonAsyncTask");
            }
        }
        @Override
        protected Pair<AccountForConfirmation,Exception> doInBackground(String... params) {
            try {
                AccountForConfirmation account = new AccountForConfirmation();
                account.setEmail(params[0]);
                account.setName(params[1]);
                account.setPass(params[2]);
                account.setType(params[3]);
                return new Pair<>(srvApi.registerAccount(account).execute(),null);
            } catch (Exception e) {
                return new Pair<>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<AccountForConfirmation,Exception> result) {
            try {
                listener.onRegisterAccountAsyncTaskFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onRegisterAccountAsyncTaskFinish(Pair<AccountForConfirmation, Exception> result);
        }
    }

    public static class ResetPassAsyncTask extends AsyncTask<String, Void, Exception>{
        private OnAction listener=null;
        private Context context = null;
        public ResetPassAsyncTask(Fragment fragment)
        {
            context = fragment.getContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for RegisterPersonAsyncTask");
            }
        }
        @Override
        protected Exception doInBackground(String... params) {
            try {
                String email = params[0];
                srvApi.resetPass(email).execute();
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception result) {
            try {
                listener.onResetPassAsyncTaskFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onResetPassAsyncTaskFinish(Exception result);
        }
    }

    public static class UpdatePersonAsyncTask extends AsyncTask< Person, Void, Pair<Person,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public UpdatePersonAsyncTask(Fragment fragment)
        {
            context = fragment.getContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for UpdatePersonAsyncTask");
            }
        }
        @Override
        protected Pair<Person,Exception> doInBackground(Person... params) {
            try {
                Person person = params[0];
                Person updatedPerson = srvApi.updatePerson(person.getId(),person).execute();
                return new Pair<>(updatedPerson,null);
            } catch (Exception e) {
                return new Pair<>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<Person,Exception> result) {
            try {
                listener.onUpdatePersonFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onUpdatePersonFinish(Pair<Person,Exception> result);
        }
    }

    public static class ChangePassAsyncTask extends AsyncTask< Pair<Long,String>, Void, Exception >{
        private OnAction listener=null;
        private Context context = null;
        public ChangePassAsyncTask(Fragment fragment)
        {
            context = fragment.getContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for ChangePassAsyncTask");
            }
        }
        @Override
        protected Exception doInBackground(Pair<Long,String>... params) {
            try {
                Long id = params[0].first;
                String oldPass = params[0].second;
                String newPass = params[1].second;
                srvApi.changePass(id,newPass,oldPass).execute();
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception result) {
            try {
                listener.onChangePassFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onChangePassFinish(Exception result);
        }
    }

    public static class ChangeEmailAsyncTask extends AsyncTask< Pair<Pair<Long,String>,Pair<String,String>>, Void, Exception >{
        private OnAction listener=null;
        private Context context = null;
        public ChangeEmailAsyncTask(Fragment fragment)
        {
            try {
                context = fragment.getContext();
                setSrvApi(context);
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for ChangeEmailAsyncTask");
            }
        }
        @Override
        protected Exception doInBackground(Pair<Pair<Long,String>,Pair<String,String>>... params) {
            try {
                Long id = params[0].first.first;
                String pass = params[0].first.second;
                String oldEmail = params[0].second.first;
                String newEmail = params[0].second.second;
                srvApi.changeEmail(id, newEmail, oldEmail,pass).execute();
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception result) {
            try {
                listener.onChangeEmailFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onChangeEmailFinish(Exception result);
        }
    }


    public static class GetUrlForUploadAsyncTask extends AsyncTask<Void, Void, Pair<String,Exception> >{
        private OnAction listener=null;
        private Context context = null;
        public GetUrlForUploadAsyncTask(Fragment fragment)
        {
            context = fragment.getContext();
            setSrvApi(context);
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() +
                        " must implement onGetUrlForUploadAsyncTaskFinish for GetUrlForUploadAsyncTask");
            }
        }
        @Override
        protected Pair<String,Exception> doInBackground(Void... params) {
            try {
                FileUrl fileUrl = srvApi.getUrlForUpload().execute();
                return new Pair<>(fileUrl.getUrlForUpload(),null);
            } catch (Exception e) {
                return new Pair<>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<String,Exception> result) {
            try {
                listener.onGetUrlForUploadAsyncTaskFinish(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public interface OnAction
        {
            void onGetUrlForUploadAsyncTaskFinish(Pair<String, Exception> result);
        }
    }
}

