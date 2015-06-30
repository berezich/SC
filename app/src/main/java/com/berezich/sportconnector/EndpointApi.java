package com.berezich.sportconnector;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

import com.berezich.sportconnector.backend.sportConnectorApi.SportConnectorApi;
import com.berezich.sportconnector.backend.sportConnectorApi.model.RegionInfo;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

/**
 * Created by Sashka on 01.07.2015.
 */
public class EndpointApi {
    private static SportConnectorApi srvApi = null;
    /*
    public static void getRegionExec(Pair<Context, Long> params)
    {
        new GetRegionAsyncTask().execute(params);
    }
    */
    public static class GetRegionAsyncTask extends AsyncTask<Pair<Context, Long>, Void, Pair<RegionInfo,Exception> >{
        private Context context;
        @Override
        protected Pair<RegionInfo,Exception> doInBackground(Pair<Context, Long>... params) {
            Long regionId;
            String url;
            context = params[0].first;
            url = context.getString(R.string.srvApi_url);

            if(srvApi == null) {  // Only do this once
/*                SportConnectorApi.Builder builder = new SportConnectorApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl("http://192.168.162.1:8080/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
*/
                SportConnectorApi.Builder builder = new SportConnectorApi.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null).setRootUrl(url);
                // end options for devappserver

                srvApi = builder.build();
            }
            regionId = params[0].second;
            try {
                return new Pair<RegionInfo,Exception>(srvApi.getRegionInfo(regionId).execute(),null);
            } catch (IOException e) {
                return new Pair<RegionInfo,Exception>(null,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<RegionInfo,Exception> result) {
            String resText="";
            if(result.second!=null)
                resText = result.second.getMessage();
            else if(result.first!=null)
                resText = result.first.toString();

            Toast.makeText(context, resText, Toast.LENGTH_LONG).show();
        }
    }
}

