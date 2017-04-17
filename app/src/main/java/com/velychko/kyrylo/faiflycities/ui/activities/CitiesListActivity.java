package com.velychko.kyrylo.faiflycities.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.velychko.kyrylo.faiflycities.R;
import com.velychko.kyrylo.faiflycities.adapters.CitiesListAdapter;
import com.velychko.kyrylo.faiflycities.data.database.DatabaseDescription;
import com.velychko.kyrylo.faiflycities.data.database.DatabaseMaster;
import com.velychko.kyrylo.faiflycities.data.network.CountriesToCities.DataModel.CitiesResponse;
import com.velychko.kyrylo.faiflycities.data.network.CountriesToCities.DataModel.Country;
import com.velychko.kyrylo.faiflycities.ui.fragments.ProgressDialogFragment;
import com.velychko.kyrylo.faiflycities.utils.Constants;
import com.velychko.kyrylo.faiflycities.utils.ItemDivider;

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

    private CitiesResponse citiesResponse = new CitiesResponse();

    ProgressDialogFragment dialog = new ProgressDialogFragment();

    private Button btnChooseCountry;
    private TextView tvCurrentCountry;
    private TextView tvCountOfCities;
    private RecyclerView rvCitiesList;

    private Cursor cursorCountriesList;
    private int currentCountryPosition = 0;
    private int currentCountryCountOfCities = 0;
    private String currentCountryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities_list);
//
//        SharedPreferences.Editor editor =
//                getSharedPreferences(Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE).edit();
//        editor.putBoolean(Constants.SPREF_IS_DATABASE_EMPTY, true);
//        editor.apply();



        if (isDatabaseEmpty()) {
            ParseCitiesJSONAsyncTask pars = new ParseCitiesJSONAsyncTask();
            pars.execute();
        }

        initViewComponents();
    }

    private void initViewComponents() {
        btnChooseCountry = (Button) findViewById(R.id.btn_choose_country);
        btnChooseCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursorCountriesList == null) {
                    cursorCountriesList = DatabaseMaster.getInstance(getApplicationContext())
                            .getCountriesList();
                }
                showChooseCountryDialog();
            }
        });
        tvCurrentCountry = (TextView) findViewById(R.id.tv_current_country);
        tvCountOfCities = (TextView) findViewById(R.id.tv_count_of_cities);
        rvCitiesList = (RecyclerView) findViewById(R.id.rv_cities_list);
        rvCitiesList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        rvCitiesList.addItemDecoration(new ItemDivider(this));
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
                        if (!jsonArray.getString(j).equals("")) {
                            cities.add(jsonArray.getString(j));
                        }
                    }
                }
                if (!country.equals("") && cities.size() > 0) {
                    citiesResponse.addCountry(new Country(country, cities));
                }
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

    private void showChooseCountryDialog(){
        final String CURRENT_COUNTRY_STRING = 
                getResources().getString(R.string.tv_constant_string_current_country);
        final String COUNT_OF_CITIES_STRING =
                getResources().getString(R.string.tv_constant_string_count_of_cities);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_choose_country)
                .setSingleChoiceItems(cursorCountriesList, currentCountryPosition,
                        DatabaseDescription.Cities.COLUMN_COUNTRY, null)
                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentCountryPosition =
                                ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        cursorCountriesList.moveToPosition(currentCountryPosition);
                        currentCountryName = cursorCountriesList.getString(
                                cursorCountriesList.getColumnIndex(DatabaseDescription.Cities.COLUMN_COUNTRY));
                        tvCurrentCountry.setText(CURRENT_COUNTRY_STRING + " " + currentCountryName);
                        currentCountryCountOfCities = cursorCountriesList.getInt(
                                cursorCountriesList.getColumnIndex(Constants.SQL_ALIAS_COUNT_OF_CITIES));
                        tvCountOfCities.setText(COUNT_OF_CITIES_STRING + " "
                                + currentCountryCountOfCities);

                        Cursor cursorCitiesList = DatabaseMaster.getInstance(getApplicationContext())
                                .getCitiesListByCountry(currentCountryName);
                        CitiesListAdapter adapter = new CitiesListAdapter(cursorCitiesList);
                        rvCitiesList.setAdapter(adapter);
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .setCancelable(false)
                .create().show();
    }

}
