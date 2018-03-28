package com.example.rowin.urchinmusicplayer.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.Globals;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;

/**
 * Created by Rowin on 3/25/2018.
 */

public class SortingOptionDialogMenu extends Dialog{
    private Context context;

    private int positionY;
    private ArrayList<Song> listOfSongs;

    private SortingOptions sortingOptions;
    private RecyclerViewAdapter recyclerViewAdapter;
    private MusicStorage musicStorage;

    private boolean isSortedName = false;
    private boolean isSortedDuration = false;
    private boolean isSortedArtist = false;

    private ImageView sortNameView, sortDurationView, sortArtistView;

    public SortingOptionDialogMenu(@NonNull Context context, int positionY, RecyclerViewAdapter recyclerViewAdapter) {
        super(context);

        this.context = context;
        this.positionY = positionY;
        this.recyclerViewAdapter = recyclerViewAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_option_menu);
        initializeViews();
        initializeClasses();

        listOfSongs = musicStorage.loadAudio();

        Window window = getWindow();
        WindowManager.LayoutParams windowLayoutParams = window.getAttributes();

        window.setGravity(Gravity.RIGHT | Gravity.TOP);
        windowLayoutParams.y = positionY;
        window.setDimAmount(0f);

        sortNameClickListener();
        sortArtisClickListener();
        sortDurationClickListener();
    }

    private void initializeViews(){
        sortNameView = findViewById(R.id.sort_name_view);
        sortArtistView = findViewById(R.id.sort_artist_view);
        sortDurationView = findViewById(R.id.sort_duration_view);
    }

    private void initializeClasses(){
        musicStorage = new MusicStorage(getContext());
        sortingOptions = new SortingOptions(getContext());
    }

    private void reinitializeTextWatcher(String filterType){
        TextWatcherSorter textWatcherSorter = new TextWatcherSorter(getContext());
        textWatcherSorter.setFilterType(filterType);
        EditText a = Globals.getInstance().getSearchSongEditText();
        a.addTextChangedListener(textWatcherSorter);
    }

    private void sortNameClickListener(){
        sortNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSortedName){
                    ArrayList<Song> listOfSongs = musicStorage.loadAudio();
                    ArrayList<Song> sortedListAtoZ = sortingOptions.getSortedListAtoZ(listOfSongs);
                    sortNameView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_z_to_a_filter_option));

                    musicStorage.storeAudio(sortedListAtoZ);
                    reinitializeTextWatcher(SortingOptions.SORTED_NAME_A_Z);
                    recyclerViewAdapter.changeDataSet(sortedListAtoZ);
                    isSortedName = true;
                } else {
                    ArrayList<Song> listOfSongs = musicStorage.loadAudio();
                    ArrayList<Song> sortedListZtoA= sortingOptions.getSortedListZtoA(listOfSongs);

                    reinitializeTextWatcher(SortingOptions.SORTED_NAME_Z_A);
                    sortNameView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_a_to_z_filter_option));
                    musicStorage.storeAudio(sortedListZtoA);

                    recyclerViewAdapter.changeDataSet(sortedListZtoA);
                    isSortedName = false;
                }
            }
        });
    }

    private void sortDurationClickListener(){
        sortDurationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSortedDuration){
                    ArrayList<Song> listOfSongs = musicStorage.loadAudio();
                    ArrayList<Song> sortedListDuration = sortingOptions.getSortedListDuration(listOfSongs);
                    sortDurationView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_9_to_0_filter_option));

                    musicStorage.storeAudio(sortedListDuration);
                    reinitializeTextWatcher(SortingOptions.SORTED_DURATION_0_9);
                    recyclerViewAdapter.changeDataSet(sortedListDuration);
                    isSortedDuration = false;
                } else {
                    ArrayList<Song> listOfSongs = musicStorage.loadAudio();
                    ArrayList<Song> sortedListDuration = sortingOptions.getSortedListZtoA(listOfSongs);
                    sortDurationView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_0_to_9_filter_option));

                    musicStorage.storeAudio(sortedListDuration);
                    reinitializeTextWatcher(SortingOptions.SORTED_DURATION_9_0);
                    recyclerViewAdapter.changeDataSet(sortedListDuration);
                    isSortedDuration = true;
                }
            }
        });
    }

    private void sortArtisClickListener(){
        sortArtistView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSortedArtist){
                    ArrayList<Song> listOfSongs = musicStorage.loadAudio();
                    ArrayList<Song> sortedListDuration = sortingOptions.getSortedListArtist(listOfSongs);
                    sortArtistView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_a_to_z_filter_option));

                    musicStorage.storeAudio(sortedListDuration);
                    reinitializeTextWatcher(SortingOptions.SORTED_ARTIST_A_Z);
                    recyclerViewAdapter.changeDataSet(sortedListDuration);
                    isSortedArtist = false;
                } else {
                    ArrayList<Song> listOfSongs = musicStorage.loadAudio();
                    ArrayList<Song> sortedListDuration = sortingOptions.getSortedListZtoA(listOfSongs);
                    sortArtistView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_9_to_0_filter_option));

                    musicStorage.storeAudio(sortedListDuration);
                    reinitializeTextWatcher(SortingOptions.SORTED_ARTIST_Z_A);
                    recyclerViewAdapter.changeDataSet(sortedListDuration);
                    isSortedArtist = true;
                }
            }
        });
    }


}
