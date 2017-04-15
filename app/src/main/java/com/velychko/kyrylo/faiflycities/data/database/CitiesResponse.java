package com.velychko.kyrylo.faiflycities.data.database;

import java.util.ArrayList;
import java.util.List;

public class CitiesResponse {

    public List<Country> countries = new ArrayList<>();

    public CitiesResponse() {
    }

    public void addCountry(Country country) {
        countries.add(country);
    }

    public static class Country {
        public String name;
        public List<String> cities;

        public Country(String name, List<String> cities) {
            this.name = name;
            this.cities = cities;
        }

    }
}
