package com.example.rowin.urchinmusicplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Converter;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<Song> listOfSongs;
    private final OnItemClickListener listener;
    private Context context;
    private Converter converter;

    private int textColor;
    private int checkedPosition = -1;

    public interface OnItemClickListener{
        void onItemClick(int position, Song song);
    }

    public RecyclerViewAdapter(Context context, ArrayList<Song> listOfSongs, OnItemClickListener listener){
        this.listOfSongs = listOfSongs;
        this.listener = listener;
        this.context = context;

        converter = new Converter();
        textColor = context.getResources().getColor(R.color.recyclerTitlePressedColor);;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View recyclerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new ItemViewHolder(recyclerItem);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ItemViewHolder)holder).bind(listOfSongs.get(position), position, (ItemViewHolder) holder, listener);
    }

    @Override
    public int getItemCount() {
        return listOfSongs.size();
    }

    public void changeDataSet(ArrayList<Song> listOfSongs){
        this.listOfSongs = listOfSongs;
        notifyDataSetChanged();
    }

    public boolean isSelected(int position) {
        return checkedPosition == position;
    }

    public void setSelected(int position){
        int prevSelected = checkedPosition;
        checkedPosition = position;

        if(prevSelected != -1){
            notifyItemChanged(prevSelected);
        }

        notifyItemChanged(checkedPosition);
    }

    public void setTextColor(int color){
        this.textColor = color;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView songTitleView;
        TextView songBandNameView;
        TextView songDurationView;
        //Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        ItemViewHolder(View itemView) {
            super(itemView);

            songTitleView = itemView.findViewById(R.id.song_title_view_currently_playing_tab);
            songBandNameView = itemView.findViewById(R.id.song_band_name_view);
            songDurationView = itemView.findViewById(R.id.song_duration_view_song_activity);
        }

        void bind(final Song song, int position, final ItemViewHolder holder, final OnItemClickListener listener){
            songTitleView.setText(song.getSongName());
            songBandNameView.setText(song.getArtist());
            songDurationView.setText(converter.convertToDuration(song.getDuration()));

            if(position == checkedPosition){
                songTitleView.setTextColor(textColor);
                songBandNameView.setTextColor(textColor);
                songDurationView.setTextColor(textColor);
            } else {
                songTitleView.setTextColor(context.getResources().getColor(R.color.recyclerTitleColor));
                songBandNameView.setTextColor(context.getResources().getColor(R.color.recyclerTitleColor));
                songDurationView.setTextColor(context.getResources().getColor(R.color.recyclerTitleColor));
            }


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(holder.getAdapterPosition(), song);
                }
            });
        }
    }

}
