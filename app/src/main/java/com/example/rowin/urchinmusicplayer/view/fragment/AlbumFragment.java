package com.example.rowin.urchinmusicplayer.view.fragment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;


import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.controller.AlbumSongsActivity;
import com.example.rowin.urchinmusicplayer.model.Album;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.model.event.EndActivityEvent;
import com.example.rowin.urchinmusicplayer.util.Animations;
import com.example.rowin.urchinmusicplayer.util.WindowUtils;
import com.example.rowin.urchinmusicplayer.view.adapter.AlbumRecyclerViewAdapter;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Rowin on 2/24/2018.
 */

public class AlbumFragment extends Fragment {

    private ArrayList<Album> albumList = new ArrayList<>();
    private ArrayList<Song> listOfSongs = new ArrayList<>();

    private MusicStorage musicStorage;

    private RecyclerView albumRecyclerView;
    private View albumImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.albums_tab_fragment, container, false);
        initializeViews(view);

        Bundle bundle = getArguments();

        if (bundle != null) {
            albumList = bundle.getParcelableArrayList("listOfAlbums");
            listOfSongs = bundle.getParcelableArrayList("listOfSongs");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void initializeViews(View view){
        albumRecyclerView = view.findViewById(R.id.album_recycler_view);
    }

    private void initializeRecyclerView(){
        AlbumRecyclerViewAdapter myAdapter = new AlbumRecyclerViewAdapter(getContext(), albumList, new AlbumRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View itemView, final Album album) {
                albumImageView = itemView;
                openAlbumWithAnimation(itemView, album);
            }
        });
        albumRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        albumRecyclerView.setAdapter(myAdapter);
    }

    private void openAlbum(Album album,  ArrayList<Song> albumSongs){
        Intent songIntent = new Intent(getContext(), AlbumSongsActivity.class);
        songIntent.putExtra("albumBitmapPath", album.getPath());
        songIntent.putExtra("albumTitle", album.getName());
        songIntent.putExtra("albumArtist", album.getArtist());
        songIntent.putParcelableArrayListExtra("listOfSongs", albumSongs);
        startActivity(songIntent);
    }

    private void openAlbumWithAnimation(View itemView, final Album album){
        ScaleAnimation fade_in =  new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fade_in.setDuration(200);     // animation duration in milliseconds
        fade_in.setFillAfter(true);    // If fillAfter is true, the transformation that this animation performed will persist when it is finished.
        fade_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ArrayList<Song> albumSongs = getAlbumSongs(album);
                openAlbum(album, albumSongs);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        itemView.startAnimation(fade_in);
    }

    private void closeAlbumWithAnimation(View view){
        ScaleAnimation fade_in =  new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fade_in.setDuration(200);     // animation duration in milliseconds
        fade_in.setFillAfter(true);    // If fillAfter is true, the transformation that this animation performed will persist when it is finished.
        view.startAnimation(fade_in);
    }

    private ArrayList<Song> getAlbumSongs(Album album){
        ArrayList<Song> albumSongs = new ArrayList<>();

        for(Song song: listOfSongs){
            if(song.getAlbumId().equals(album.getId())){
                albumSongs.add(song);
            }
        }

        return albumSongs;
    }

    public void onEvent(EndActivityEvent endActivityEvent){
        closeAlbumWithAnimation(albumImageView);
    }

}
