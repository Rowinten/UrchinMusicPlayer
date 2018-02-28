package com.example.rowin.urchinmusicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private ArrayList<Song> listOfSongs;
    private final OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(ViewHolder holder);
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

            songTitleView = itemView.findViewById(R.id.song_title_view);
            songBandNameView = itemView.findViewById(R.id.song_band_name_view);
            songDurationView = itemView.findViewById(R.id.song_duration_view);
        }

        void bind(Song song, final ViewHolder holder, final OnItemClickListener listener){
            songTitleView.setText(song.getSongName());
            songBandNameView.setText(song.getArtist());
            songDurationView.setText(song.getDuration());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(holder);
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

    private Bitmap getImageCoverFromMusicFile(String filePath){
        File image = new File(filePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        return BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
    }
}
