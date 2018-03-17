package com.example.rowin.urchinmusicplayer.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.activity.MainActivity;
import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.AudioRequests;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

//TODO RecyclerView overlaps with the currently_playing_tab_view
public class SongListFragment extends Fragment{
    private ChangeHighlightedTabReceiver changeHighlightedTabReceiver;

    private RecyclerView songListRecyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private EditText searchSongEditText;


    public SongListFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.songs_tab_fragment, container, false);
        initializeViews(view);

        Bundle bundle = getArguments();
        int colorAccent = bundle.getInt("colorAccent");


        MusicStorage musicStorage = new MusicStorage(getContext());
        ArrayList<Song> listOfSongs = musicStorage.loadAudio();

        songListRecyclerView.setAdapter(getRecyclerViewAdapter(listOfSongs));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        songListRecyclerView.setLayoutManager(layoutManager);


        if(musicStorage.loadAudioIndex() != null) {
            searchSongEditText.getBackground().setColorFilter(colorAccent, PorterDuff.Mode.SRC_IN);
            recyclerViewAdapter.setTextColor(colorAccent);
            recyclerViewAdapter.setSelected(musicStorage.loadAudioIndex());
            songListRecyclerView.scrollToPosition(musicStorage.loadAudioIndex());
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
        searchSongEditText = view.findViewById(R.id.search_song_edit_text);
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
                int index = listOfSongs.indexOf(song);
                ((MainActivity) getActivity()).playAudio(index);
            }
        });

        return recyclerViewAdapter;
    }


    private void changeSelectedTab(int position, int color){
        if(!recyclerViewAdapter.isSelected(position)){
            recyclerViewAdapter.setTextColor(color);
            recyclerViewAdapter.setSelected(position);
        }
    }

    class ChangeHighlightedTabReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int newPosition = intent.getIntExtra("newIndex", 0);
            int color = intent.getIntExtra("albumCoverColor", 0);

            changeSelectedTab(newPosition, color);
            searchSongEditText.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);


            songListRecyclerView.scrollToPosition(newPosition);

        }
    }
}
