package com.velychko.kyrylo.faiflycities.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.velychko.kyrylo.faiflycities.R;
import com.velychko.kyrylo.faiflycities.data.network.wikipediasearch.Geoname;
import com.velychko.kyrylo.faiflycities.data.network.wikipediasearch.WikiRetrofit;
import com.velychko.kyrylo.faiflycities.data.network.wikipediasearch.WikipediaSearchDataModel;
import com.velychko.kyrylo.faiflycities.ui.fragments.ProgressDialogFragment;
import com.velychko.kyrylo.faiflycities.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CityDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Сохраняем для выдачи Snackbar
    private View containerLayout;
    private TextView tvCity;
    private TextView tvCountry;
    private TextView tvSummary;
    private TextView tvWikiLink;

    GoogleMap map;
    private String cityName;
    private String countryName;
    ProgressDialogFragment dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_details);

        // Инициализация визуальных компонентов экрана
        initViewComponents();

        // Получение Extra данных из Intent
        getExtra();

        // Запуск диалога прогресса загрузки информации о городе
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "progress dialog");
    }

    // Инициализация визуальных компонентов экрана
    private void initViewComponents() {
        containerLayout = findViewById(R.id.container);

        tvCity = (TextView) findViewById(R.id.tv_city);
        tvCountry = (TextView) findViewById(R.id.tv_country);
        tvSummary = (TextView) findViewById(R.id.tv_summary);
        tvWikiLink = (TextView) findViewById(R.id.tv_wiki_link);
        tvWikiLink.setMovementMethod(LinkMovementMethod.getInstance());

        dialog = new ProgressDialogFragment();
        Bundle arguments = new Bundle();
        // Передача Title в диалог
        arguments.putString(Constants.BUNDLE_PROGRESS_DIALOG_TITLE,
                getResources().getString(R.string.dialog_title_loading_information_about_city));
        dialog.setArguments(arguments);

        // Запуск карты
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);
    }

    // Получение Extra данных из Intent
    private void getExtra() {
        Intent intent = getIntent();
        cityName = intent.getStringExtra(Constants.EXTRA_CITY_NAME);
        countryName = intent.getStringExtra(Constants.EXTRA_COUNTRY_NAME);
        setTitle(cityName);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Инициализация карты
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        // Получение данных о городе из Wikipedia Search
        WikiRetrofit.getDataAboutCity(cityName).enqueue(new Callback<WikipediaSearchDataModel>() {
            @Override
            public void onResponse(Call<WikipediaSearchDataModel> call,
                                   Response<WikipediaSearchDataModel> response) {
                // Если ответ не пустой, значит информация получена.
                if (response.body().geonames.size() > 0) {
                    Geoname geoname = response.body().geonames.get(0);
                    tvCity.setText(geoname.title);
                    tvCountry.setText(countryName);
                    tvSummary.setText(geoname.summary);
                    tvWikiLink.setText(geoname.wikipediaUrl);

                    map.clear();
                    map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(geoname.lat, geoname.lng), 6));
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(geoname.lat, geoname.lng))
                            .title(geoname.title)
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED)));
                } else {
                    // Если ответ пусто - скажем об этом пользователю
                    Toast.makeText(getApplicationContext(), String.format(getResources()
                            .getString(R.string.toast_city_info_not_available), cityName),
                            Toast.LENGTH_LONG).show();
                    // Возвращаемся на предыдущий экран для выбора города тукщей страны
                    onBackPressed();
                }
                // Закрываем диалог прогресса
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<WikipediaSearchDataModel> call, Throwable t) {
                // Если не вернулся ответ от сервера, скорее всего проблемы с подключением к интернету
                Snackbar.make(containerLayout, R.string.snack_check_internet_connection,
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
