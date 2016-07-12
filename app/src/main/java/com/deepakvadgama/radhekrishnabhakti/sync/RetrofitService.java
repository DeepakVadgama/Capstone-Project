package com.deepakvadgama.radhekrishnabhakti.sync;


import com.deepakvadgama.radhekrishnabhakti.pojo.Content;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitService {

    @POST("/api/stream/latest")
    Call<List<Content>> getContent(@Query("low") int fromId);

    @POST("/api/favorites/get")
    Call<List<Content>> getFavorites(@Query("email") String email);

    @POST("/api/favorites/add")
    Call<Boolean> addFavorite(@Query("email") String email, @Query("contentId") int contentId);

    @POST("/api/favorites/remove")
    Call<Boolean> removeFavorite(@Query("email") String email, @Query("contentId") int contentId);


    @POST("/api/favorites/addSet")
    Call<Boolean> addFavorites(@Query("email") String email, @Query("contentIds") Set<String> contentIds);

    @POST("/api/favorites/removeSet")
    Call<Boolean> removeFavorites(@Query("email") String email, @Query("contentIds") Set<String> contentIds);
}
