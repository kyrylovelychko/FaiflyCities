package com.velychko.kyrylo.faiflycities.ui.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.velychko.kyrylo.faiflycities.R;
import com.velychko.kyrylo.faiflycities.utils.Constants;

public class CityDetailsActivity extends AppCompatActivity {

    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_details);

        getExtra();

    }

    public void getExtra() {
        Intent intent = getIntent();
        cityName = intent.getStringExtra(Constants.EXTRA_CITY_NAME);
        setTitle(cityName);
    }
}
