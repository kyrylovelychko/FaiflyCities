package com.velychko.kyrylo.faiflycities.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.velychko.kyrylo.faiflycities.data.network.countriestocities.CitiesResponse;
import com.velychko.kyrylo.faiflycities.data.network.countriestocities.Country;
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

    // Диалог для индикации загрузки данных о странах и городах
    private ProgressDialogFragment dialog;

    private Button btnChooseCountry;
    private TextView tvCurrentCountry;
    private TextView tvCountOfCities;
    private RecyclerView rvCitiesList;

    // Ответ, содержащий информацию обо всех странах и городах
    private CitiesResponse citiesResponse = new CitiesResponse();
    // Курсор со списком названий стран
    private Cursor cursorCountriesList;
    // Название выбранной (текушей) сраны
    private String currentCountryName;
    // Индекс текущей страны в списке всех стран
    private int currentCountryPosition = 0;
    // Количество городов в выбранной (текущей) стране
    private int currentCountryCountOfCities = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities_list);

        /* Проверка на заполненность БД. Если база пустая, то это первый запуск программы,
         либо предыдущая загрузка не удалась. Вызываем AsyncTask для получения данных из JSON и их
         сохранения в базу*/
        if (isDatabaseEmpty()) {
            ParseCitiesJSONAsyncTask parseCitiesJSONAsyncTask = new ParseCitiesJSONAsyncTask();
            parseCitiesJSONAsyncTask.execute();
        }

        // Инициализация визуальных компонентов экрана
        initViewComponents();
    }

    // Инициализация визуальных компонентов экрана
    private void initViewComponents() {
        btnChooseCountry = (Button) findViewById(R.id.btn_choose_country);
        btnChooseCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Спиок всех стран загружается один раз при первом нажатии на кнопку выбора страны
                if (cursorCountriesList == null) {
                    cursorCountriesList = DatabaseMaster.getInstance(getApplicationContext())
                            .getCountriesList();
                }
                cursorCountriesList.moveToFirst();

                // Показываем диалог для выбора страны
                showChooseCountryDialog();
            }
        });

        tvCurrentCountry = (TextView) findViewById(R.id.tv_current_country);
        tvCountOfCities = (TextView) findViewById(R.id.tv_count_of_cities);
        rvCitiesList = (RecyclerView) findViewById(R.id.rv_cities_list);
        rvCitiesList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        rvCitiesList.addItemDecoration(new ItemDivider(this));
    }

    // Проверка на заполненность БД.
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

            // Подготовка и запуск диалога прогресса выполнения загрузки данных о странах и городах
            dialog = new ProgressDialogFragment();
            Bundle arguments = new Bundle();
            // Передача Title в диалог
            arguments.putString(Constants.BUNDLE_PROGRESS_DIALOG_TITLE,
                    getResources().getString(R.string.dialog_title_loading_countries));
            dialog.setArguments(arguments);
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), "progress dialog");
        }

        @Override
        protected Void doInBackground(Void... params) {
            String resultJson;

            try {
                // Получение данных из JSON с внешнего ресурса
                resultJson = loadCitiesAndCountriesFromUrl();

                try {
                    // Парсим JSON и заполняем citiesResponse
                    parseCitiesJSON(resultJson);

                    // Заполняем БД загруженными данными
                    boolean fillingDone = DatabaseMaster.getInstance(
                            getApplicationContext()).fillDatabase(citiesResponse);
                    // Если БД заполнена, сохраняем признак этого в SharedPreferences.
                    // При последующих запусках данные сразу будут браться из БД
                    if (fillingDone) {
                        SharedPreferences.Editor editor =
                                getSharedPreferences(Constants.SPREF_SETTINGS_FILE_NAME,
                                        Context.MODE_PRIVATE).edit();
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

        // Парсим JSON и заполняем citiesResponse
        private void parseCitiesJSON(String resultJson) throws JSONException {
            JSONObject dataJsonObj = new JSONObject(resultJson);

            for (int i = 0; i < dataJsonObj.length(); i++) {
                String country = dataJsonObj.names().get(i).toString();
                List<String> cities = new ArrayList<>();
                JSONArray jsonArray = dataJsonObj.getJSONArray(country);
                if (jsonArray != null) {
                    for (int j = 0; j < jsonArray.length(); j++) {
                        // Города с пустым названием не записываем
                        if (!jsonArray.getString(j).equals("")) {
                            cities.add(jsonArray.getString(j));
                        }
                    }
                }
                // Страны с пустым названием, а также страны без городов не записываем
                if (!country.equals("") && cities.size() > 0) {
                    citiesResponse.addCountry(new Country(country, cities));
                }
            }
        }

        @NonNull
        // Получение данных из JSON файла с внешнего ресурса и возвращение строка JSON
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

            // Когда все данные загружены, закрываем диалог прогресса
            dialog.dismiss();
        }
    }

    // Показываем диалог для выбора страны
    private void showChooseCountryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_choose_country)
                // В список в диалоге показываем названия стран из курсора со списком всех стран
                .setSingleChoiceItems(cursorCountriesList, currentCountryPosition,
                        DatabaseDescription.Cities.COLUMN_COUNTRY, null)
                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Сохраняем позицию выбранной страны в списке всех стран
                        currentCountryPosition =
                                ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        cursorCountriesList.moveToPosition(currentCountryPosition);
                        // Сохраняем название выбранной страны
                        currentCountryName = cursorCountriesList.getString(cursorCountriesList
                                .getColumnIndex(DatabaseDescription.Cities.COLUMN_COUNTRY));
                        tvCurrentCountry.setText(Constants.CURRENT_COUNTRY_STRING + " "
                                + currentCountryName);
                        currentCountryCountOfCities = cursorCountriesList.getInt(cursorCountriesList
                                .getColumnIndex(Constants.SQL_ALIAS_COUNT_OF_CITIES));
                        tvCountOfCities.setText(Constants.COUNT_OF_CITIES_STRING + " "
                                + currentCountryCountOfCities);

                        // Получаем список городов выбранной страны
                        Cursor cursorCitiesList = DatabaseMaster.getInstance(getApplicationContext())
                                .getCitiesListByCountry(currentCountryName);
                        // Создаем адаптер для вывода списка всех городов
                        CitiesListAdapter adapter =
                                new CitiesListAdapter(cursorCitiesList, currentCountryName);
                        rvCitiesList.setAdapter(adapter);
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .setCancelable(false)
                .create().show();
    }

    @Override
    // Перед закрытием главного экрана переспрашиваем у пользователя, хочет ли он выйти
    public void onBackPressed() {
        Snackbar.make(findViewById(R.id.constraintCitiesList), R.string.snack_confirm_exit,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.snack_close_app, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }).show();

    }
}