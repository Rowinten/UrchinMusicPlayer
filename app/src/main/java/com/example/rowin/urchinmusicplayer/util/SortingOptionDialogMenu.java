package com.example.rowin.urchinmusicplayer.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.model.event.ListChangedEvent;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Rowin on 3/25/2018.
 */

public class SortingOptionDialogMenu extends Dialog{
    private Context context;

    private SortingOptions sortingOptions;
    private MusicStorage musicStorage;
    private ImageView sortNameView, sortDurationView, sortArtistView;
    private OnSortListener sortButtonClickListener;

    private ArrayList<Song> listOfSongs;

    private int positionY;
    private boolean isSortedName = false;
    private boolean isSortedDuration = false;
    private boolean isSortedArtist = false;

    public interface OnSortListener {
        void onSort(ArrayList<Song> filteredList, String filterType);
    }

    public SortingOptionDialogMenu(@NonNull Context context, int positionY, ArrayList<Song> listOfSongs, OnSortListener sortButtonClickListener) {
        super(context);

        this.context = context;
        this.positionY = positionY;
        this.sortButtonClickListener = sortButtonClickListener;
        this.listOfSongs = listOfSongs;
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
        sortingOptions = new SortingOptions(getContext(), listOfSongs);
    }

    private void setDialogMenuPosition(){
        Window window = getWindow();
        WindowManager.LayoutParams windowLayoutParams = window.getAttributes();

        window.setGravity(Gravity.RIGHT | Gravity.TOP);
        windowLayoutParams.y = positionY;
        window.setDimAmount(0f);
    }

    private void sortNameClickListener(){
        sortNameView.setOnClickListener(new View.OnClickListener() {
            //ArrayList<Song> listOfSongs = musicStorage.loadAudio();

            @Override
            public void onClick(View view) {
                if(!isSortedName){
                    ArrayList<Song> sortedListAtoZ = sortingOptions.getSortedListAtoZ(listOfSongs);
                    sortListSequence(sortedListAtoZ, SortingOptions.SORTED_NAME_A_Z);
                    isSortedName = true;
                } else {
                    ArrayList<Song> sortedListZtoA = sortingOptions.reverseList(listOfSongs);
                    sortListSequence(sortedListZtoA, SortingOptions.SORTED_NAME_Z_A);
                    isSortedName = false;
                }
            }
        });
    }

    private void sortDurationClickListener(){
        sortDurationView.setOnClickListener(new View.OnClickListener() {
            //ArrayList<Song> listOfSongs = musicStorage.loadAudio();

            @Override
            public void onClick(View view) {
                if(!isSortedDuration){
                    ArrayList<Song> sortedList0to9 = sortingOptions.getSortedListDuration(listOfSongs);
                    sortListSequence(sortedList0to9, SortingOptions.SORTED_DURATION_0_9);
                    isSortedDuration = true;
                } else {
                    ArrayList<Song> sortedList9to0 = sortingOptions.reverseList(listOfSongs);
                    sortListSequence(sortedList9to0, SortingOptions.SORTED_DURATION_9_0);
                    isSortedDuration = false;
                }
            }
        });
    }

    private void sortArtistClickListener(){
        sortArtistView.setOnClickListener(new View.OnClickListener() {
            //ArrayList<Song> listOfSongs = musicStorage.loadAudio();

            @Override
            public void onClick(View view) {
                if(!isSortedArtist){
                    ArrayList<Song> sortedListAtoZ = sortingOptions.getSortedListArtist(listOfSongs);
                    sortListSequence(sortedListAtoZ, SortingOptions.SORTED_ARTIST_A_Z);
                    isSortedArtist = true;
                } else {
                    ArrayList<Song> sortedListZtoA = sortingOptions.reverseList(listOfSongs);
                    sortListSequence(sortedListZtoA, SortingOptions.SORTED_ARTIST_Z_A);
                    isSortedArtist = false;
                }
            }
        });
    }

    //Sequence that happens after a list has been sorted, adjust image from A-Z to Z-A, reinitialize TextWatcher so that filtered list is used instead of old one
    //and changes the data set from the RecyclerView to the new one.
    private void sortListSequence(ArrayList<Song> filteredList, String filterType){
        //musicStorage.storeAudio(filteredList);
        EventBus.getDefault().post(new ListChangedEvent(filteredList));

        sortButtonClickListener.onSort(filteredList, filterType);

        resetOtherActiveSorters(filterType);
        setImageDrawableBasedOn(filterType);
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


    private void setImageDrawableBasedOn(String filterType){
        switch (filterType){
            case SortingOptions.SORTED_ARTIST_A_Z:
                Drawable artistFilterDrawableReversed = context.getResources().getDrawable(R.drawable.ic_z_to_a_filter_option);
                sortArtistView.setImageDrawable(artistFilterDrawableReversed);
                break;
            case SortingOptions.SORTED_NAME_A_Z:
                Drawable nameFilterDrawableReversed = context.getResources().getDrawable(R.drawable.ic_z_to_a_filter_option);
                sortNameView.setImageDrawable(nameFilterDrawableReversed);
                break;
            case SortingOptions.SORTED_DURATION_0_9:
                Drawable durationFilterDrawableReversed = context.getResources().getDrawable(R.drawable.ic_9_to_0_filter_option);
                sortDurationView.setImageDrawable(durationFilterDrawableReversed);
                break;
            case SortingOptions.SORTED_ARTIST_Z_A:
                Drawable artistFilterDrawable = context.getResources().getDrawable(R.drawable.ic_a_to_z_filter_option);
                sortArtistView.setImageDrawable(artistFilterDrawable);
                break;
            case SortingOptions.SORTED_NAME_Z_A:
                Drawable nameFilterDrawable = context.getResources().getDrawable(R.drawable.ic_a_to_z_filter_option);
                sortNameView.setImageDrawable(nameFilterDrawable);
                break;
            case SortingOptions.SORTED_DURATION_9_0:
                Drawable durationFilterDrawable = context.getResources().getDrawable(R.drawable.ic_0_to_9_filter_option);
                sortDurationView.setImageDrawable(durationFilterDrawable);
                break;
        }
    }


}
