package com.example.rowin.urchinmusicplayer.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.activity.MainActivity;
import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Animations;
import com.example.rowin.urchinmusicplayer.util.PlayAudio;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

//TODO RecyclerView overlaps with the currently_playing_tab_view
public class SongListFragment extends Fragment{

    private RecyclerView songListRecyclerView;
    private RecyclerViewAdapter.ViewHolder previouslyTabbedItemView;



    public SongListFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.songs_tab_fragment, container, false);
        initializeViews(view);

        MusicStorage musicStorage = new MusicStorage(getContext());
        ArrayList<Song> listOfSongs = musicStorage.loadAudio();

        songListRecyclerView.setAdapter(getRecyclerViewAdapter(listOfSongs));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        songListRecyclerView.setLayoutManager(layoutManager);

        return view;
    }

    private void initializeViews(View view){
        songListRecyclerView = view.findViewById(R.id.songRecyclerView);
    }


    private RecyclerViewAdapter getRecyclerViewAdapter(final ArrayList<Song> listOfSongs){
        return new RecyclerViewAdapter(listOfSongs, new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerViewAdapter.ViewHolder holder, Song song) {
                changeItemViewTitleColor(holder);

                //If no Music is playing, change play icon to pause icon when song is clicked
                //If song is playing then do nothing for the animation. since the pause button is already visible.
//                if(!Globals.isMusicPlaying) {
//                    animations.playToPauseAnimation(playButton);
//                    Globals.isMusicPlaying = true;
//                }

                int index = listOfSongs.indexOf(song);
                ((MainActivity) getActivity()).playAudio(index);
            }
        });
    }

    private void changeItemViewTitleColor(RecyclerViewAdapter.ViewHolder holder){
        RecyclerViewAdapter.ViewHolder currentlyTabbedItemView = holder;
        currentlyTabbedItemView.songTitleView.setTextColor(getResources().getColor(R.color.recyclerTitlePressedColor));

        //Checks if the user tapped the same itemView, so that the currently highlighted itemView stays highlighted
        if (currentlyTabbedItemView != previouslyTabbedItemView) {
            //If a previousItemView has already been tabbed, change color back to standard color. To let user know this item is no longer in focus
            if(previouslyTabbedItemView != null){
                previouslyTabbedItemView.songTitleView.setTextColor(getResources().getColor(R.color.recyclerTitleColor));
            }
        } else {
            //Do Nothing
        }
        previouslyTabbedItemView = currentlyTabbedItemView;
    }


}
