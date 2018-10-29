package com.example.rowin.urchinmusicplayer.model.event;

import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;

public class ListChangedEvent {

    private ArrayList<Song> lisOfSongs;

    public ListChangedEvent(ArrayList<Song> listOfSongs){
        this.lisOfSongs = listOfSongs;
    }

    public ArrayList<Song> getChangedList(){
        return lisOfSongs;
    }
}
