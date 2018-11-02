package com.example.rowin.urchinmusicplayer.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.rowin.urchinmusicplayer.view.adapter.SongRecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.event.ListChangedEvent;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Rowin on 3/27/2018.
 */

public class TextWatcherSorter implements TextWatcher {
    private MusicStorage musicStorage;

    private String filterType = null;
    private SortingOptions sortingOptions;
    private SongRecyclerViewAdapter recyclerViewAdapter;

    public TextWatcherSorter(Context context, ArrayList<Song> listOfSongs, RecyclerView recyclerView){
        sortingOptions = new SortingOptions(context, listOfSongs);
        musicStorage = new MusicStorage(context);

        recyclerViewAdapter = (SongRecyclerViewAdapter) recyclerView.getAdapter();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        ArrayList<Song> queryResults = sortingOptions.getFilteredListOnTextInput(charSequence.toString());

        if(filterType == null){
            //musicStorage.storeAudio(queryResults);
            recyclerViewAdapter.changeDataSet(queryResults);
        } else {
            switch (filterType) {
                case SortingOptions.SORTED_NAME_A_Z:
                    ArrayList<Song> sortedListName = sortingOptions.getSortedListAtoZ(queryResults);
                    recyclerViewAdapter.changeDataSet(sortedListName);
                    EventBus.getDefault().post(new ListChangedEvent(sortedListName));
                    //musicStorage.storeAudio(sortedListName);
                    break;
                case SortingOptions.SORTED_NAME_Z_A:
                    ArrayList<Song> reversedListName = sortingOptions.getReversedSortedListZtoA(queryResults);
                    recyclerViewAdapter.changeDataSet(reversedListName);
                    EventBus.getDefault().post(new ListChangedEvent(reversedListName));
                    //musicStorage.storeAudio(reversedListName);
                    break;
                case SortingOptions.SORTED_ARTIST_A_Z:
                    ArrayList<Song> sortedListArtist = sortingOptions.getSortedListArtist(queryResults);
                    recyclerViewAdapter.changeDataSet(sortedListArtist);
                    EventBus.getDefault().post(new ListChangedEvent(sortedListArtist));
                    //musicStorage.storeAudio(sortedListArtist);
                    break;
                case SortingOptions.SORTED_ARTIST_Z_A:
                    ArrayList<Song> reversedListArtist = sortingOptions.getReversedSortedListArtist(queryResults);
                    recyclerViewAdapter.changeDataSet(reversedListArtist);
                    EventBus.getDefault().post(new ListChangedEvent(reversedListArtist));
                    //musicStorage.storeAudio(reversedListArtist);
                    break;
                case SortingOptions.SORTED_DURATION_0_9:
                    ArrayList<Song> sortedListDuration = sortingOptions.getSortedListDuration(queryResults);
                    recyclerViewAdapter.changeDataSet(sortedListDuration);
                    EventBus.getDefault().post(new ListChangedEvent(sortedListDuration));
                    //musicStorage.storeAudio(sortedListDuration);
                    break;
                case SortingOptions.SORTED_DURATION_9_0:
                    ArrayList<Song> reversedListDuration = sortingOptions.getReversedSortedListDuration(queryResults);
                    recyclerViewAdapter.changeDataSet(reversedListDuration);
                    EventBus.getDefault().post(new ListChangedEvent(reversedListDuration));
                    //musicStorage.storeAudio(reversedListDuration);
                    break;
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    public void setFilterType(String filterType){
        this.filterType = filterType;
    }

}
