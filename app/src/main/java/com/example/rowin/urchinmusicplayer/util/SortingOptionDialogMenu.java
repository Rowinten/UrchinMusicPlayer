package com.example.rowin.urchinmusicplayer.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import java.util.Objects;

/**
 * Created by Rowin on 3/25/2018.
 */

public class SortingOptionDialogMenu extends Dialog{
    private Context context;

    private SortingOptions sortingOptions;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private MusicStorage musicStorage;
    private ImageView sortNameView, sortDurationView, sortArtistView;

    private int positionY;
    private boolean isSortedName = false;
    private boolean isSortedDuration = false;
    private boolean isSortedArtist = false;

    public SortingOptionDialogMenu(@NonNull Context context, int positionY) {
        super(context);

        this.context = context;
        this.positionY = positionY;

        recyclerView = Globals.getInstance().getRecyclerView();
        recyclerViewAdapter = (RecyclerViewAdapter) recyclerView.getAdapter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_option_menu);
        initializeViews();
        initializeClasses();
        setDialogMenuPosition();

        sortNameClickListener();
        sortArtistClickListener();
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

    private void setDialogMenuPosition(){
        Window window = getWindow();
        WindowManager.LayoutParams windowLayoutParams = window.getAttributes();

        window.setGravity(Gravity.RIGHT | Gravity.TOP);
        windowLayoutParams.y = positionY;
        window.setDimAmount(0f);
    }

    private void reinitializeTextWatcher(String filterType, Song currentlySelectedSong){
        TextWatcherSorter textWatcherSorter = new TextWatcherSorter(getContext(), recyclerView);
        textWatcherSorter.setFilterType(filterType);
        textWatcherSorter.setCurrentHighlightedSong(currentlySelectedSong);
        EditText a = Globals.getInstance().getSearchSongEditText();
        a.addTextChangedListener(textWatcherSorter);
    }

    private void sortNameClickListener(){
        sortNameView.setOnClickListener(new View.OnClickListener() {
            ArrayList<Song> listOfSongs = musicStorage.loadAudio();
            Song highlightedSong = listOfSongs.get(musicStorage.loadAudioIndex());

            @Override
            public void onClick(View view) {
                if(!isSortedName){
                    ArrayList<Song> sortedListAtoZ = sortingOptions.getSortedListAtoZ(listOfSongs);
                    sortListSequence(sortedListAtoZ, highlightedSong, SortingOptions.SORTED_NAME_A_Z);
                    isSortedName = true;
                } else {
                    ArrayList<Song> sortedListZtoA = sortingOptions.reverseList(listOfSongs);
                    sortListSequence(sortedListZtoA, highlightedSong, SortingOptions.SORTED_NAME_Z_A);
                    isSortedName = false;
                }
            }
        });
    }

    private void sortDurationClickListener(){
        sortDurationView.setOnClickListener(new View.OnClickListener() {
            ArrayList<Song> listOfSongs = musicStorage.loadAudio();
            Song highlightedSong = listOfSongs.get(musicStorage.loadAudioIndex());

            @Override
            public void onClick(View view) {
                if(!isSortedDuration){
                    ArrayList<Song> sortedList0to9 = sortingOptions.getSortedListDuration(listOfSongs);
                    sortListSequence(sortedList0to9, highlightedSong, SortingOptions.SORTED_DURATION_0_9);
                    isSortedDuration = true;
                } else {
                    ArrayList<Song> sortedList9to0 = sortingOptions.reverseList(listOfSongs);
                    sortListSequence(sortedList9to0, highlightedSong, SortingOptions.SORTED_DURATION_9_0);
                    isSortedDuration = false;
                }
            }
        });
    }

    private void sortArtistClickListener(){
        sortArtistView.setOnClickListener(new View.OnClickListener() {
            ArrayList<Song> listOfSongs = musicStorage.loadAudio();
            Song highlightedSong = listOfSongs.get(musicStorage.loadAudioIndex());

            @Override
            public void onClick(View view) {
                if(!isSortedArtist){
                    ArrayList<Song> sortedListAtoZ = sortingOptions.getSortedListArtist(listOfSongs);
                    sortListSequence(sortedListAtoZ, highlightedSong, SortingOptions.SORTED_ARTIST_A_Z);
                    isSortedArtist = true;
                } else {
                    ArrayList<Song> sortedListZtoA = sortingOptions.reverseList(listOfSongs);
                    sortListSequence(sortedListZtoA, highlightedSong, SortingOptions.SORTED_ARTIST_Z_A);
                    isSortedArtist = false;
                }
            }
        });
    }

    //reset or disables the last sort option that was selected, gets used when a new sorter gets selected.
    private void resetOtherActiveSorters(String newSortOption){
       switch (newSortOption){
           case SortingOptions.SORTED_NAME_A_Z:
               if(isSortedArtist){
                   isSortedArtist = false;
                   sortArtistView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_a_to_z_filter_option));
               } else if(isSortedDuration){
                   isSortedDuration = false;
                   sortDurationView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_0_to_9_filter_option));
               }
               break;
           case SortingOptions.SORTED_ARTIST_A_Z:
               if(isSortedName){
                   isSortedName = false;
                   sortNameView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_a_to_z_filter_option));
               } else if(isSortedDuration){
                   isSortedDuration = false;
                   sortDurationView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_0_to_9_filter_option));
               }
               break;
           case SortingOptions.SORTED_DURATION_0_9:
               if(isSortedArtist){
                   isSortedArtist = false;
                   sortArtistView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_a_to_z_filter_option));
               } else if(isSortedName){
                   isSortedName = false;
                   sortNameView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_a_to_z_filter_option));
               }
               break;
       }
    }

    //Makes sure that a song that is selected stays selected. for example, songs get highlighted based on index, if index changes ( and song shifts to another position )
    //it makes sure that index 3 doesn't stay selected but the proper new index of the currently selected song does.
    private void correctSelectedSongHighlight(ArrayList<Song> filteredList, Song highlightedSong){
        for(int i = 0; i < filteredList.size(); i++){
            if(Objects.equals(highlightedSong.getSongName(), filteredList.get(i).getSongName())){
                if(!recyclerViewAdapter.isSelected(i)){
                    recyclerViewAdapter.setSelected(i);
                    return;
                }
            }
        }
    }

    //Sequence that happens after a list has been sorted, adjust image from A-Z to Z-A, reinitialize TextWatcher so that filtered list is used instead of old one
    //and changes the data set from the RecyclerView to the new one.
    private void sortListSequence(ArrayList<Song> filteredList, Song highLightedSong, String filterType){
        musicStorage.storeAudio(filteredList);
        correctSelectedSongHighlight(filteredList, highLightedSong);
        recyclerViewAdapter.changeDataSet(filteredList);

        reinitializeTextWatcher(filterType, highLightedSong);
        resetOtherActiveSorters(filterType);
        sortNameView.setImageDrawable(getImageDrawableBasedOn(filterType));
    }

    private Drawable getImageDrawableBasedOn(String filterType){
        switch (filterType){
            case SortingOptions.SORTED_ARTIST_A_Z:
            case SortingOptions.SORTED_NAME_A_Z:
                return context.getResources().getDrawable(R.drawable.ic_z_to_a_filter_option);
            case SortingOptions.SORTED_DURATION_0_9:
                return context.getResources().getDrawable(R.drawable.ic_9_to_0_filter_option);
            case SortingOptions.SORTED_ARTIST_Z_A:
            case SortingOptions.SORTED_NAME_Z_A:
                return context.getResources().getDrawable(R.drawable.ic_a_to_z_filter_option);
            case SortingOptions.SORTED_DURATION_9_0:
                return context.getResources().getDrawable(R.drawable.ic_0_to_9_filter_option);
        }
        throw new NullPointerException("This " + filterType + "is invalid");
    }


}
