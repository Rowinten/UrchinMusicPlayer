package com.example.rowin.urchinmusicplayer.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.Globals;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;

/**
 * Created by Rowin on 3/27/2018.
 */

public class TextWatcherSorter implements TextWatcher {
    private MusicStorage musicStorage;

    private String filterType = null;
    private SortingOptions sortingOptions;

    public TextWatcherSorter(Context context){
        sortingOptions = new SortingOptions(context);
        musicStorage = new MusicStorage(context);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        ArrayList<Song> queryResults = sortingOptions.getFilteredListOnTextInput(charSequence.toString());

        if(filterType == null){
            musicStorage.storeAudio(queryResults);
        } else {
            switch (filterType) {
                case SortingOptions.SORTED_NAME_A_Z:
                    ArrayList<Song> sortedListAZ = sortingOptions.getSortedListAtoZ(queryResults);
                    musicStorage.storeAudio(sortedListAZ);
                    break;
                case SortingOptions.SORTED_NAME_Z_A:
                    ArrayList<Song> sortedListZA = sortingOptions.getSortedListZtoA(queryResults);
                    musicStorage.storeAudio(sortedListZA);
                    break;
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    void setFilterType(String filterType){
        this.filterType = filterType;
    }
}
