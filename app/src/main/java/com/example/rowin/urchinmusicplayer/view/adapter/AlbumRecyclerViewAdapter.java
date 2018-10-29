package com.example.rowin.urchinmusicplayer.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.model.Album;
import com.example.rowin.urchinmusicplayer.util.Converter;
import com.example.rowin.urchinmusicplayer.util.WindowUtils;

import java.util.ArrayList;

public class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Converter converter;
    private WindowUtils windowUtils;
    private ArrayList<Album> listOfAlbums;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(View view, Album album);
    }

    public AlbumRecyclerViewAdapter(Context context, ArrayList<Album> listOfAlbums, OnItemClickListener listener){
        this.listOfAlbums = listOfAlbums;
        this.listener = listener;

        converter = new Converter();
        windowUtils = new WindowUtils(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View recyclerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_recycler_item, parent, false);
        return new ViewHolder(recyclerItem);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Album album = getItem(position);
        ((ViewHolder) holder).bind(album);
    }

    @Override
    public int getItemCount() {
        return listOfAlbums.size();
    }

    private Album getItem(int position){
        return listOfAlbums.get(position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder{

        private ConstraintLayout parentCardView;
        private CardView albumImageHolder;
        private ImageView albumImageView;
        private TextView albumTitleHolder, albumArtistHolder;

        ViewHolder(View itemView) {
            super(itemView);

            parentCardView = itemView.findViewById(R.id.album_card_view);
            albumImageHolder = itemView.findViewById(R.id.album_image_grid_view);
            albumTitleHolder = itemView.findViewById(R.id.album_title_textview);
            albumImageView = itemView.findViewById(R.id.album_image_view_activity);
            albumArtistHolder = itemView.findViewById(R.id.album_artist_title_view);
        }

        void bind(final Album album){
            String albumName = album.getName();
            String albumArtist = album.getArtist();
            String albumImagePath = album.getPath();
            Bitmap albumImage = converter.getAlbumCoverFromPath(albumImagePath);

            int screenWidth = windowUtils.getScreenWidth();
            int marginCard = (int) Math.round(screenWidth * 0.025);

            int widthCard = (int) Math.round(screenWidth * 0.45);
            int heightCard = widthCard + albumTitleHolder.getHeight();

            itemView.setClipToOutline(false);

            setMarginsCardView(marginCard);
            setXYCardView(widthCard, heightCard);

            albumImageView.setImageBitmap(albumImage);
            albumTitleHolder.setText(albumName);
            albumArtistHolder.setText(albumArtist);

            parentCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnItemClick(albumImageHolder, album);
                }
            });
        }

        //Sets margin as percentage of the total width of the screen
        void setMarginsCardView(int marginCard){
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) parentCardView.getLayoutParams();

            layoutParams.setMargins(marginCard, marginCard, marginCard, marginCard);
        }

        //Sets width as percentage of the total width of the screen
        void setXYCardView(int widthCard, int heightCard){
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)
                    albumImageHolder.getLayoutParams();

            layoutParams.width = widthCard;
            layoutParams.height = heightCard;
        }
    }
}
