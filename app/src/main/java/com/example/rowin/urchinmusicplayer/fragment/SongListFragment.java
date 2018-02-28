package com.example.rowin.urchinmusicplayer.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

public class SongListFragment extends Fragment{

    private RecyclerView songListRecyclerView;
    private RecyclerViewAdapter.ViewHolder currentlyTabbedItemView, previouslyTabbedItemView;

    public SongListFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.songs_tab_fragment, container, false);
        initializeViews(view);

        Bundle bundle = this.getArguments();
        ArrayList<Song> listOfSongs = bundle.getParcelableArrayList("listOfSongs");

        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(listOfSongs, new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerViewAdapter.ViewHolder holder) {
                //Get current ViewHolder object from the itemView that is being pressed
                //Change Color of that itemView to give user visual feedback of it being pressed
                currentlyTabbedItemView = holder;
                currentlyTabbedItemView.songTitleView.setTextColor(getResources().getColor(R.color.recyclerTitlePressedColor));

                //If a previousItemView has already been tabbed, change color back to standard color. To let user know this item is no longer in focus
                if(previouslyTabbedItemView != null){
                    previouslyTabbedItemView.songTitleView.setTextColor(getResources().getColor(R.color.recyclerTitleColor));
                }

                previouslyTabbedItemView = currentlyTabbedItemView;
            }
        });
        songListRecyclerView.setAdapter(recyclerViewAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        songListRecyclerView.setLayoutManager(layoutManager);


        return view;
    }

    private void initializeViews(View view){
        songListRecyclerView = view.findViewById(R.id.songRecyclerView);
    }
}
