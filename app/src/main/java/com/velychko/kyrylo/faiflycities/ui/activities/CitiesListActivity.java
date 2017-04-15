package com.velychko.kyrylo.faiflycities.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.velychko.kyrylo.faiflycities.R;
import com.velychko.kyrylo.faiflycities.data.database.CitiesResponse;
import com.velychko.kyrylo.faiflycities.data.database.DatabaseMaster;
import com.velychko.kyrylo.faiflycities.ui.dialogs.ProgressDialogFragment;
import com.velychko.kyrylo.faiflycities.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CitiesListActivity extends AppCompatActivity {

    CitiesResponse citiesResponse = new CitiesResponse();

    ProgressDialogFragment dialog = new ProgressDialogFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities_list);

        if (isDatabaseEmpty()) {
            ParseCitiesJSONAsyncTask pars = new ParseCitiesJSONAsyncTask();
            pars.execute();
        }
    }


    private boolean isDatabaseEmpty() {
        SharedPreferences sPref =
                getSharedPreferences(Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
        return sPref.getBoolean(Constants.SPREF_IS_DATABASE_EMPTY, true);
    }

    private class ParseCitiesJSONAsyncTask extends AsyncTask<Void, Void, Void> {
        private static final String URL_STRING = "https://raw.githubusercontent.com/David-Haim/" +
                "CountriesToCitiesJSON/master/countriesToCities.json";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog.show(getSupportFragmentManager(), "progress dialog");
        }

        @Override
        protected Void doInBackground(Void... params) {
            String resultJson;

            try {
                // получаем данные с внешнего ресурса
                resultJson = loadCitiesAndCountriesFromUrl();

                try {
                    parseCitiesJSON(resultJson);

                    boolean fillingDone = DatabaseMaster.getInstance(
                            getApplicationContext()).fillDatabase(citiesResponse);
                    if (fillingDone) {
                        SharedPreferences.Editor editor =
                                getSharedPreferences(Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE).edit();
                        editor.putBoolean(Constants.SPREF_IS_DATABASE_EMPTY, false);
                        editor.apply();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        private void parseCitiesJSON(String resultJson) throws JSONException {
            JSONObject dataJsonObj = new JSONObject(resultJson);

            for (int i = 0; i < dataJsonObj.length(); i++) {
                String country = dataJsonObj.names().get(i).toString();
                List<String> cities = new ArrayList<>();
                JSONArray jsonArray = dataJsonObj.getJSONArray(country);
                if (jsonArray != null) {
                    for (int j = 0; j < jsonArray.length(); j++) {
                        cities.add(jsonArray.getString(j));
                    }
                }
                citiesResponse.addCountry(new CitiesResponse.Country(country, cities));
            }
        }

        @NonNull
        private String loadCitiesAndCountriesFromUrl() throws IOException {
            HttpURLConnection urlConnection;
            BufferedReader reader;
            String resultJson;
            URL url = new URL(URL_STRING);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            resultJson = buffer.toString();
            return resultJson;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            dialog.dismiss();
        }
    }

}
