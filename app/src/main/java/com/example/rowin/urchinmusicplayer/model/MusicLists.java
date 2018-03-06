package com.example.rowin.urchinmusicplayer.model;

import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by Rowin on 3/6/2018.
 */

public class MusicLists {
    private static ArrayList<Song> listOfSongs;
    private static ArrayList<Uri> allSongUri = new ArrayList<>();

    private static final MusicLists ourInstance = new MusicLists();

    public static MusicLists getInstance() {
        return ourInstance;
    }

    private MusicLists() {
    }

    public void setListOfSongs(ArrayList<Song> listOfSongs){
        MusicLists.listOfSongs = listOfSongs;
    }

    public ArrayList<Song> getListOfSongs(){
        return listOfSongs;
    }

    public ArrayList<Uri> getAllSongUri(){
        for(Song song: listOfSongs){
            Uri songUri = Uri.parse(song.getSongPath());
            allSongUri.add(songUri);
        }
        return allSongUri;
    }


}
