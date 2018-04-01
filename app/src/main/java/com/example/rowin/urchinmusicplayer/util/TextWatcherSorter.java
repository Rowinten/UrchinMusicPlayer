package com.example.rowin.urchinmusicplayer.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.Globals;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Rowin on 3/27/2018.
 */

public class TextWatcherSorter implements TextWatcher {
    private MusicStorage musicStorage;

    private String filterType = null;
    private SortingOptions sortingOptions;
    private Song highLightedSong;
    private RecyclerViewAdapter recyclerViewAdapter;

    public TextWatcherSorter(Context context, RecyclerView recyclerView){
        sortingOptions = new SortingOptions(context);
        musicStorage = new MusicStorage(context);

        recyclerViewAdapter = (RecyclerViewAdapter) recyclerView.getAdapter();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        ArrayList<Song> queryResults = sortingOptions.getFilteredListOnTextInput(charSequence.toString());

        if(filterType == null){
            musicStorage.storeAudio(queryResults);
            correctSelectedSongHighlight(queryResults);
            recyclerViewAdapter.changeDataSet(queryResults);
        } else {
            switch (filterType) {
                case SortingOptions.SORTED_NAME_A_Z:
                    ArrayList<Song> sortedListName = sortingOptions.getSortedListAtoZ(queryResults);
                    correctSelectedSongHighlight(sortedListName);
                    recyclerViewAdapter.changeDataSet(sortedListName);
                    musicStorage.storeAudio(sortedListName);
                    break;
                case SortingOptions.SORTED_NAME_Z_A:
                    ArrayList<Song> reversedListName = sortingOptions.reverseList(queryResults);
                    correctSelectedSongHighlight(reversedListName);
                    recyclerViewAdapter.changeDataSet(reversedListName);
                    musicStorage.storeAudio(reversedListName);
                    break;
                case SortingOptions.SORTED_ARTIST_A_Z:
                    ArrayList<Song> sortedListArtist = sortingOptions.getSortedListArtist(queryResults);
                    correctSelectedSongHighlight(sortedListArtist);
                    recyclerViewAdapter.changeDataSet(sortedListArtist);
                    musicStorage.storeAudio(sortedListArtist);
                    break;
                case SortingOptions.SORTED_ARTIST_Z_A:
                    ArrayList<Song> reversedListArtist = sortingOptions.reverseList(queryResults);
                    correctSelectedSongHighlight(reversedListArtist);
                    recyclerViewAdapter.changeDataSet(reversedListArtist);
                    musicStorage.storeAudio(reversedListArtist);
                    break;
                case SortingOptions.SORTED_DURATION_0_9:
                    ArrayList<Song> sortedListDuration = sortingOptions.getSortedListDuration(queryResults);
                    correctSelectedSongHighlight(sortedListDuration);
                    recyclerViewAdapter.changeDataSet(sortedListDuration);
                    musicStorage.storeAudio(sortedListDuration);
                    break;
                case SortingOptions.SORTED_DURATION_9_0:
                    ArrayList<Song> reversedListDuration = sortingOptions.reverseList(queryResults);
                    correctSelectedSongHighlight(reversedListDuration);
                    recyclerViewAdapter.changeDataSet(reversedListDuration);
                    musicStorage.storeAudio(reversedListDuration);
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

    public void setCurrentHighlightedSong(Song highlightedSong){
        this.highLightedSong = highlightedSong;
    }

    //Makes sure that a song that is selected stays selected. for example, songs get highlighted based on index, if index changes ( and song shifts to another position )
    //it makes sure that index 3 doesn't stay selected but the proper new index of the currently selected song does.
    private void correctSelectedSongHighlight(ArrayList<Song> filteredList){
        for(int i = 0; i < filteredList.size(); i++){
            if(Objects.equals(highLightedSong.getSongName(), filteredList.get(i).getSongName())){
                if(!recyclerViewAdapter.isSelected(i)){
                    recyclerViewAdapter.setSelected(i);
                    return;
                }
            }
        }
    }
}
