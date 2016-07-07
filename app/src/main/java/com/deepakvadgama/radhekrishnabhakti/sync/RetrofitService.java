package com.deepakvadgama.radhekrishnabhakti.sync;


import com.deepakvadgama.radhekrishnabhakti.pojo.Content;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RetrofitService {

    @GET("/api/stream/latest")
    Call<List<Content>> getContent(int fromId);

    @GET("/api/favorites/get")
    Call<List<Content>> getFavorites(String email);


}
