package com.example.rowin.urchinmusicplayer.model;

import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;

/**
 * Created by Rowin on 3/26/2018.
 */

public class Globals {
    public static final int FADE_OUT_ACTIVITY_VIEWS_DURATION = 400;
    public static final int FADE_IN_ACTIVITY_VIEWS_DURATION = 400;
    public static final float FADE_OUT_ALPHA = 0f;
    public static final float FADE_IN_ALPHA = 1f;
    public static final int IMAGE_BLUR = 5;



    private RecyclerView recyclerView;
    private EditText searchSongEditText;

    private static final Globals ourInstance = new Globals();

    public static Globals getInstance() {
        return ourInstance;
    }

    private Globals() {
    }

    public void initRecyclerView(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
    }

    public void setSearchSongEditText(EditText searchSongEditText){
        this.searchSongEditText = searchSongEditText;
    }

    public EditText getSearchSongEditText(){
        return searchSongEditText;
    }


    public RecyclerView getRecyclerView(){return recyclerView;}

}
