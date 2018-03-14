package com.example.rowin.urchinmusicplayer.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

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

    public void storeAudioIndex(int index){
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex(){
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("audioIndex", -1);//return -1 if no data found
    }

    public Song getLastPlayedSong(){
        ArrayList<Song> listOfSongs = loadAudio();
        Integer index = loadAudioIndex();

        if(index != -1){
            return listOfSongs.get(index);
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
