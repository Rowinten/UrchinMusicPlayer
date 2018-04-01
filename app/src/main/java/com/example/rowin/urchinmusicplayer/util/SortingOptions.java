package com.example.rowin.urchinmusicplayer.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.Globals;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Rowin on 3/25/2018.
 */

public class SortingOptions {
    static final String SORTED_NAME_A_Z = "com.example.rowin.urchinmusicplayer.SORTNAMEAZ";
    static final String SORTED_NAME_Z_A = "com.example.rowin.urchinmusicplayer.SORTNAMEZA";
    static final String SORTED_DURATION_0_9 = "com.example.rowin.urchinmusicplayer.SORTDURATION09";
    static final String SORTED_DURATION_9_0 = "com.example.rowin.urchinmusicplayer.SORTDURATION90";
    static final String SORTED_ARTIST_A_Z = "com.example.rowin.urchinmusicplayer.SORTARTISTAZ";
    static final String SORTED_ARTIST_Z_A = "com.example.rowin.urchinmusicplayer.SORTARTISTZA";

    private ArrayList<Song> listOfSongs;
    private ArrayList<Song> backUpListOfSongs = new ArrayList<>();

    SortingOptions(Context context){
        MusicStorage musicStorage = new MusicStorage(context);
        listOfSongs = musicStorage.loadAudio();
        backUpListOfSongs.addAll(listOfSongs);
    }

    ArrayList<Song> getSortedListAtoZ(ArrayList<Song> listOfSongs){
        Comparator<Song> comparator = new Comparator<Song>() {
            @Override
            public int compare(Song song, Song t1) {
                return song.getSongName().compareTo(t1.getSongName());
            }
        };

        Collections.sort(listOfSongs, comparator);
        return listOfSongs;
    }

    ArrayList<Song> getSortedListDuration(ArrayList<Song> listOfSongs){
        Comparator<Song> comparator = new Comparator<Song>() {
            @Override
            public int compare(Song song, Song t1) {
                return song.getDuration().compareTo(t1.getDuration());
            }
        };

        Collections.sort(listOfSongs, comparator);
        return listOfSongs;
    }

    ArrayList<Song> getSortedListArtist(ArrayList<Song> listOfSongs) {
        Comparator<Song> comparator = new Comparator<Song>() {
            @Override
            public int compare(Song song, Song t1) {
                return song.getArtist().compareTo(t1.getArtist());
            }
        };

        Collections.sort(listOfSongs, comparator);
        return listOfSongs;
    }

    ArrayList<Song> reverseList(ArrayList<Song> listOfSongs){
        Collections.reverse(listOfSongs);
        return listOfSongs;
    }

    ArrayList<Song> getFilteredListOnTextInput(String textInput){
        listOfSongs.clear();
        if (textInput.isEmpty()) {
            listOfSongs.addAll(backUpListOfSongs);
            return listOfSongs;
        } else {
            for (Song song : backUpListOfSongs) {
                if (song.getSongName().toLowerCase().contains(textInput)) {
                    listOfSongs.add(song);
                }
            }
            return listOfSongs;
        }
    }

}
