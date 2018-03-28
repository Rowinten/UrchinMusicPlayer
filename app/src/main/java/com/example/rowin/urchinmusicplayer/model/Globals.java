package com.example.rowin.urchinmusicplayer.model;

import android.app.Activity;
import android.app.Application;
import android.widget.EditText;

import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.util.SortingOptions;

/**
 * Created by Rowin on 3/26/2018.
 */

public class Globals {
    private RecyclerViewAdapter recyclerViewAdapter;
    private EditText searchSongEditText;

    private static final Globals ourInstance = new Globals();

    public static Globals getInstance() {
        return ourInstance;
    }

    private Globals() {
    }

    public void initRecyclerViewAdapter(RecyclerViewAdapter recyclerViewAdapter){
        this.recyclerViewAdapter = recyclerViewAdapter;
    }

    public void setSearchSongEditText(EditText searchSongEditText){
        this.searchSongEditText = searchSongEditText;
    }

    public EditText getSearchSongEditText(){
        return searchSongEditText;
    }

    public RecyclerViewAdapter getRecyclerViewAdapter(){
        return recyclerViewAdapter;
    }

}
