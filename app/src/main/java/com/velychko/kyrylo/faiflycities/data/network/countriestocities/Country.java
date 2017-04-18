package com.velychko.kyrylo.faiflycities.data.network.countriestocities;

import java.util.ArrayList;
import java.util.List;

public class Country {
    public String name = new String();
    public List<String> cities = new ArrayList<>();

    public Country(String name, List<String> cities) {
        this.name = name;
        this.cities = cities;
    }

    public Country() {

    }

}
