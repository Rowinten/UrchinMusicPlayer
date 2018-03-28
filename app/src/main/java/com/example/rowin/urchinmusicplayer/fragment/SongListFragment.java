package com.example.rowin.urchinmusicplayer.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.activity.MainActivity;
import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.Globals;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.AudioRequests;
import com.example.rowin.urchinmusicplayer.util.SortingOptionDialogMenu;
import com.example.rowin.urchinmusicplayer.util.HidingScrollListener;
import com.example.rowin.urchinmusicplayer.util.SortingOptions;
import com.example.rowin.urchinmusicplayer.util.TextWatcherSorter;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

//TODO RecyclerView overlaps with the currently_playing_tab_view
public class SongListFragment extends Fragment{
    public RecyclerViewAdapter recyclerViewAdapter;
    private ChangeHighlightedTabReceiver changeHighlightedTabReceiver;

    private RecyclerView songListRecyclerView;

    private CardView filterBarView;
    private EditText searchSongEditText;
    private ImageView filterButton;

    private ArrayList<Song> listOfSongs;

    private MusicStorage musicStorage;

    public SongListFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.songs_tab_fragment, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initializeViews(view);
        initializeClasses();
        //filterEditTextClickListener();

        Bundle bundle = getArguments();
        int colorAccent = bundle.getInt("colorAccent");

        listOfSongs = musicStorage.loadAudio();
        Integer lastPlayedSongIndex = ((MainActivity) getActivity()).lastPlayedSongIndex;

        initializeRecyclerView();

        searchSongEditText.addTextChangedListener(new TextWatcherSorter(getContext()));
        initializeFilterMenu();

        if(lastPlayedSongIndex != null) {
            recyclerViewAdapter.setTextColor(colorAccent);
            recyclerViewAdapter.setSelected(lastPlayedSongIndex);
            songListRecyclerView.scrollToPosition(lastPlayedSongIndex +1);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        registerChangeHighlightedTabReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(changeHighlightedTabReceiver);
    }

    private void initializeViews(View view){
        songListRecyclerView = view.findViewById(R.id.songRecyclerView);
        searchSongEditText = view.findViewById(R.id.search_song_view);
        filterBarView = view.findViewById(R.id.filter_bar_view);
        searchSongEditText = view.findViewById(R.id.search_song_view);

        Globals.getInstance().setSearchSongEditText(searchSongEditText);

        filterButton = view.findViewById(R.id.filter_button);
    }

    private void initializeClasses(){
        musicStorage = new MusicStorage(getContext());
    }

    private void initializeRecyclerView(){
        RecyclerViewAdapter recyclerViewAdapter = getRecyclerViewAdapter(listOfSongs);
        Globals.getInstance().initRecyclerViewAdapter(recyclerViewAdapter);

        songListRecyclerView.setAdapter(recyclerViewAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        songListRecyclerView.setLayoutManager(layoutManager);
        songListRecyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                int marginTop = 32;
                int totalHeight = filterBarView.getHeight() + marginTop;
                Log.d("d", String.valueOf(filterBarView.getHeight()));
                filterBarView.animate().translationY(-totalHeight).setInterpolator(new AccelerateInterpolator(2));
            }

            @Override
            public void onShow() {
                filterBarView.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
            }
        });
    }

    private void initializeFilterMenu(){
        MainActivity mainActivity = ((MainActivity) getActivity());
        int positionY = mainActivity.getNavigationBarHeight() + filterBarView.getHeight() + mainActivity.currentlyPlayingTab.getHeight() + 24;
        final Dialog filterOptionDialog = new SortingOptionDialogMenu(getContext(), positionY, recyclerViewAdapter);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(filterOptionDialog.isShowing()) {
                    filterOptionDialog.dismiss();
                } else {
                    filterOptionDialog.show();
                }
            }
        });
    }

    private void registerChangeHighlightedTabReceiver(){
        changeHighlightedTabReceiver = new ChangeHighlightedTabReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioRequests.BROADCAST_ACTION);
        getActivity().registerReceiver(changeHighlightedTabReceiver, intentFilter);
    }


    private RecyclerViewAdapter getRecyclerViewAdapter(final ArrayList<Song> listOfSongs){
        recyclerViewAdapter =  new RecyclerViewAdapter(getContext(), listOfSongs, new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, Song song) {
                int index = position;
                ((MainActivity) getActivity()).playAudio(index);
//                searchSongEditText.setCursorVisible(false);
                hideSoftKeyboard();

            }
        });

        return recyclerViewAdapter;
    }

    private void hideSoftKeyboard(){
        View view = getActivity().getCurrentFocus();

        if(view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void changeSelectedTab(int position, int color){
        if(!recyclerViewAdapter.isSelected(position)){
            recyclerViewAdapter.setTextColor(color);
            recyclerViewAdapter.setSelected(position);
        }
    }

//    private void filterEditTextClickListener(){
//        searchSongEditText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                searchSongEditText.setCursorVisible(true);
//            }
//        });
//    }

    class ChangeHighlightedTabReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int newPosition = intent.getIntExtra("newIndex", 0);
            int color = intent.getIntExtra("albumCoverColor", 0);

            changeSelectedTab(newPosition, color);
            songListRecyclerView.scrollToPosition(newPosition);

        }
    }
}
