package com.example.rowin.urchinmusicplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private ArrayList<Song> listOfSongs;
    private final OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(ViewHolder holder, Song song);
    }

    public RecyclerViewAdapter(ArrayList<Song> listOfSongs, OnItemClickListener listener){
        this.listOfSongs = listOfSongs;
        this.listener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView songTitleView;
        TextView songBandNameView;
        TextView songDurationView;
        //Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        ViewHolder(View itemView) {
            super(itemView);

            songTitleView = itemView.findViewById(R.id.song_title_view_currently_playing_tab);
            songBandNameView = itemView.findViewById(R.id.song_band_name_view);
            songDurationView = itemView.findViewById(R.id.song_duration_view);
        }

        void bind(final Song song, final ViewHolder holder, final OnItemClickListener listener){
            songTitleView.setText(song.getSongName());
            songBandNameView.setText(song.getArtist());
            songDurationView.setText(convertToDuration(song.getDuration()));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(holder, song);
                }
            });
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View recyclerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new ViewHolder(recyclerItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        (holder).bind(listOfSongs.get(position), holder, listener);
    }

    @Override
    public int getItemCount() {
        return listOfSongs.size();
    }

    //MediaStore.Audio.Media.Duration returns value in milliseconds, this function converts to minute:seconds format (example 3:22)
    private String convertToDuration(Long songDuration){
        long seconds = songDuration/1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        if(seconds < 10){
            seconds = Long.valueOf("0" + seconds);
            Log.v("d", String.valueOf(seconds));
        }

        return minutes +":"+seconds;
    }
}
