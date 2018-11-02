package com.example.rowin.urchinmusicplayer.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Converter;
import com.example.rowin.urchinmusicplayer.util.TextWatcherSorter;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

public class SongRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private ArrayList<Song> listOfSongs;
    private RecyclerView recyclerView;
    private final OnItemClickListener listener;
    private final OnHeaderClickListener headerListener;
    private Context context;
    private Converter converter;

    private int textColor;
    private int checkedPosition = -1;
    private String selectedSongName;

    private TextWatcherSorter textWatcherSorter;

    public interface OnItemClickListener{
        void onItemClick(int position, Song song);
    }

    public interface OnHeaderClickListener{
        void onHeaderClick(int viewHeight);
    }

    public SongRecyclerViewAdapter(Context context, RecyclerView recyclerView, ArrayList<Song> listOfSongs, OnItemClickListener listener, OnHeaderClickListener headerListener){
        this.listOfSongs = listOfSongs;
        this.listener = listener;
        this.headerListener = headerListener;
        this.context = context;
        this.recyclerView = recyclerView;

        converter = new Converter();
        textColor = context.getResources().getColor(R.color.recyclerTitlePressedColor);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM) {
            View recyclerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
            return new ItemViewHolder(recyclerItem);
        } else if (viewType == TYPE_HEADER){
            View headerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.fitler_bar_view, parent, false);
            return new HeaderViewHolder(headerItem);

        }

        throw new RuntimeException("There is no type that matches " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder) {
            Song song = getItem(position);
            ((ItemViewHolder) holder).bind(song, position, (ItemViewHolder) holder, listener);
        } else if(holder instanceof HeaderViewHolder){

        }
    }

    @Override
    public int getItemCount() {
        return listOfSongs.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private Song getItem(int position){
        return listOfSongs.get(position - 1);
    }

    private boolean isPositionHeader(int position){
        return position == 0;
    }

    public void changeDataSet(ArrayList<Song> listOfSongs){
        this.listOfSongs = listOfSongs;
        notifyDataSetChanged();

        String selectedSong = getSelectedSongName();
        correctSelectedTab(selectedSong);
    }

    public boolean isSelected(int position) {
        return checkedPosition == position + 1;
    }

    public void setSelected(int position){
        int prevSelected = checkedPosition;
        checkedPosition = position + 1;
        setSelectedSongName(position);

        if(prevSelected != -1){
            notifyItemChanged(prevSelected);
        }

        notifyItemChanged(checkedPosition);
    }

    private void setSelectedSongName(int position){
        this.selectedSongName = listOfSongs.get(position).getSongName();
    }

    private String getSelectedSongName(){
        return selectedSongName;
    }

    private void correctSelectedTab(String selectedSongName){
        for(int i = 0; i < listOfSongs.size(); i++){
            if(listOfSongs.get(i).getSongName().equals(selectedSongName)){
                setSelected(i);
            }
        }
    }

    public void reinitializeTextWatcher(String filterType){
        textWatcherSorter.setFilterType(filterType);
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
                    listener.onItemClick(holder.getAdapterPosition() - 1, song);
                }
            });
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder{
        EditText searchInput;
        ImageView filterButton;

        HeaderViewHolder(final View itemView) {
            super(itemView);

            filterButton = itemView.findViewById(R.id.filter_button);
            searchInput = itemView.findViewById(R.id.search_song_edit_text);

            textWatcherSorter = new TextWatcherSorter(context, listOfSongs, recyclerView);
            searchInput.addTextChangedListener(textWatcherSorter);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    headerListener.onHeaderClick(itemView.getHeight());
                }
            });
        }

    }

}
