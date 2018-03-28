package com.example.rowin.urchinmusicplayer.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Rowin on 3/6/2018.
 */

public class MusicStorage {
    private Context context;
    private SharedPreferences sharedPreferences;
    private final String STORAGE = "com.example.rowin.urchinmusicplayer.STORAGE";

    public MusicStorage(Context context){
        this.context = context;
    }

    public void storeAudio(ArrayList<Song> listOfSongs){
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(listOfSongs);
        editor.putString("listOfSongs", json);
        editor.apply();
    }

    public ArrayList<Song> loadAudio(){
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("listOfSongs", null);
        Type type = new TypeToken<ArrayList<Song>>() {}.getType();
        return gson.fromJson(json, type);

    }

    public Boolean isAudioLoaded(){
        return loadAudio() != null;
    }


    public void storeAudioIndex(int index){
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public void storeAudioName(String name){
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("audioName", name);
        editor.apply();
    }

    public String loadAudioName(){
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString("audioName", "DEFAULT");
    }

    public Integer loadAudioIndex(){
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("audioIndex", -1);//return -1 if no data found
    }


    //Searches song by name not index. Because when filter is applied index from filtered list is taken, at startup the non
    //filtered list is taken and does not return the valid song because index is not correct. so store audio name at start of new song
    //and search for the song object with the same name value, return the index of that object.
    public Integer getLastPlayedSongIndex(){
        ArrayList<Song> listOfSongs = loadAudio();
        String songName = loadAudioName();
        Integer index = 0;

        for(Song song: listOfSongs){
            if(Objects.equals(song.getSongName(), songName)){
                index = listOfSongs.lastIndexOf(song);
            }
        }

        if(index != -1){
            return index;
        }

        return null;
    }

    public void clearCachedAudioPlaylist() {
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
}
