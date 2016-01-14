package com.empatica.sample;

import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.Callback;

/**
 * Created by Joakim on 2016-01-13.
 */
public interface MoodifyAPI {
    @POST("/api/things/")
    void postSong(@Body SongPlaying songPlaying, Callback<SongPlaying> response);
}
