package com.velychko.kyrylo.faiflycities.data.network.wikipediasearch;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class WikiRetrofit {

    private static final String BASE_URL = "http://api.geonames.org/";

    private static ApiInterface apiInterface;

    interface ApiInterface{
        @GET("wikipediaSearchJSON")
        Call<WikipediaSearchDataModel> getDataAboutCity(@Query("q") String city,
                                                        @Query("maxRows") int rows,
                                                        @Query("username") String username);
    }

    static {
        initialize();
    }

    private static void initialize() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface = retrofit.create(ApiInterface.class);
    }

    public static Call<WikipediaSearchDataModel> getDataAboutCity(String city){
        return apiInterface.getDataAboutCity(city, 1, "faiflycitiesuser");
    }

}
