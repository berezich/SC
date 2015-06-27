package com.berezich.sportconnector;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

import com.berezich.sportconnector.SportObjects.Person;
//import com.berezich.sportconnector.backend.myApi.MyApi;
//import com.berezich.sportconnector.backend.myApi.model.PersonSrv;
import com.berezich.sportconnector.backend.personSrvApi.PersonSrvApi;
import com.berezich.sportconnector.backend.personSrvApi.model.PersonSrv;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.List;

/**
 * Created by berezkin on 22.06.2015.
 */
public class EndpointAsyncTask extends AsyncTask<Pair<Context, Long>, Void, String> {
    //private static MyApi myApiService = null;
    //private  static PersonSrvApi personSrvApi = null;
    private Context context;

    @Override
    protected String doInBackground(Pair<Context, Long>... params) {
        /*
        if(personSrvApi == null) {  // Only do this once
        */
            /*
            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
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
                    */
            //PersonSrvApi.Builder builder = new PersonSrvApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    //.setRootUrl("https://sportconnector-981.appspot.com/_ah/api/");
            // end options for devappserver

            //personSrvApi = builder.build();
        /*
        }

        context = params[0].first;
        Long id = params[0].second;

        try {
            PersonSrv person = new PersonSrv();
            person.setName("Igor Ivanov");
            person.setAge(27);
            //return personSrvApi.insert(person).execute().toString();
            String txt ="";
            List<PersonSrv> personLst = personSrvApi.list().execute().getItems();
            for (int i = 0; i <personLst.size() ; i++) {
                txt += personLst.get(i).getInfo()+ "\n";
            }
            return txt;

        } catch (IOException e) {
            return e.getMessage();
        }
    */
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
    }
}
