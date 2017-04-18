package com.velychko.kyrylo.faiflycities.data.network.countriestocities;

import java.util.ArrayList;
import java.util.List;

public class CitiesResponse {

    public List<Country> countries = new ArrayList<>();

    public CitiesResponse() {
    }

    public void addCountry(Country country) {
        countries.add(country);
    }


}
