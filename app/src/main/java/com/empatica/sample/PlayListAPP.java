package com.empatica.sample;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Rallmo on 2015-12-20.
 */
public class PlayListAPP {
    List<String> playListTracksUri = new ArrayList<>();
    List<String> playListTrackName = new ArrayList<>();
    int numberOfsongs;
    String playListname;
    List<String> artistName = new ArrayList<>();

    public PlayListAPP(int numberOfsongs, String playListName){
        this.numberOfsongs = numberOfsongs;
        this.playListname = playListName;

    }

    public void addToList(int i, String trackUri){
        playListTracksUri.add(i,trackUri);
    }
    public List<String> getList(){
        return playListTracksUri;
    }
    public void setPlayListTrackName(int i, String name){
        playListTrackName.add(i,name);
    }
    public List<String> getPlayListTrackName(){
        return playListTrackName;
    }
    public String getUriTrack(int position){
        return playListTracksUri.get(position);
    }
    public String getNameOfTrack(int position){
        return playListTrackName.get(position);
    }

    public int getNumberOfsongs(){
        return numberOfsongs;
    }
    public String getPlayListname(){
        return playListname;
    }

    public void setArtist(int i, Artist a) {
        artistName.add(i, a.name);
    }

    public String getArtistName(int position) {
        return artistName.get(position);
    }


}
