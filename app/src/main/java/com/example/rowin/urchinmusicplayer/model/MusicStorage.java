package com.example.rowin.urchinmusicplayer.model;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    private String loadAudioName(){
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString("audioName", "DEFAULT");
    }

    public Integer loadAudioIndex(){
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("audioIndex", -1);//return -1 if no data found
    }

    public Song getCurrentlyPlayingSong(){
        return loadAudio().get(loadAudioIndex());
    }

    //Saves the background image to the applications' storage, so that the currently in use background will be saved and van be accessed
    //When the user re-opens the app, bitmap will be overridden when a new background is set.
    public void saveBitmapToStorage(Bitmap bitmap, Context context){
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("last_background_used", Context.MODE_PRIVATE);

        File myPath = new File(directory, "last_background_used.png");
        if(myPath.exists()) myPath.delete();

        try{
            FileOutputStream fos = new FileOutputStream(myPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e){
            Log.e("SAVE_IMAGE", e.getMessage(), e);
        }
    }

    //Loads the last background bitmap used in the previous session of the user
    public Bitmap loadBitmapFromStorage(Context context){
        try{
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("last_background_used", Context.MODE_PRIVATE);
            File myPath = new File(directory, "last_background_used.png");

            if(myPath.exists()){
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                return BitmapFactory.decodeStream(new FileInputStream(myPath), null, options);
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return null;
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
