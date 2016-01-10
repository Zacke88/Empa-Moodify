package com.empatica.sample;

import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rallmo on 2015-12-19.
 */
public class SpotifyPlayList {
    HashMap<String,String> spotifyList = new HashMap<>();

    public SpotifyPlayList(){

        spotifyList.put("Damien Rice - Coconut Skins ", "spotify:track:29ZwHWh3lI43nAFnVBOagN");
        spotifyList.put("Spor - Empire ", "spotify:track:6t5sDjXwMr9vDlzfKAPNM9");

    }
    public HashMap<String,String> getList(){
        return spotifyList;
    }
}
