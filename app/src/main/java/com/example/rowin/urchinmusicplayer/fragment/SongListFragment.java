package com.example.rowin.urchinmusicplayer.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.activity.MainActivity;
import com.example.rowin.urchinmusicplayer.adapter.RecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.Globals;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Animations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

//TODO RecyclerView overlaps with the currently_playing_tab_view
public class SongListFragment extends Fragment{

    private RecyclerView songListRecyclerView;
    private RecyclerViewAdapter.ViewHolder previouslyTabbedItemView;
    private Animations animations;

    private ImageView playButton, frontAlbumImage, backAlbumImage;
    private TextView songTitleView, songBandView;
    private Boolean isAlbumBackVisible = false;

    private View albumBack, albumFront;



    public SongListFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.songs_tab_fragment, container, false);
        initializeViews(view);

        Bundle bundle = this.getArguments();
        ArrayList<Song> listOfSongs = bundle.getParcelableArrayList("listOfSongs");
        animations = new Animations(getContext());

        songListRecyclerView.setAdapter(getRecyclerViewAdapter(listOfSongs));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        songListRecyclerView.setLayoutManager(layoutManager);

        return view;
    }

    private void initializeViews(View view){
        songListRecyclerView = view.findViewById(R.id.songRecyclerView);
        playButton = ((MainActivity) getActivity()).playButton;
        frontAlbumImage = ((MainActivity)getActivity()).frontAlbumCoverView;
        backAlbumImage = ((MainActivity)getActivity()).backAlbumCoverView;
        songTitleView = ((MainActivity)getActivity()).songTitleView;
        songBandView = ((MainActivity)getActivity()).songBandView;
        albumBack = ((MainActivity) getActivity()).backAlbumCoverLayout;
        albumFront = ((MainActivity) getActivity()).frontAlbumCoverLayout;
    }


    private RecyclerViewAdapter getRecyclerViewAdapter(ArrayList<Song> listOfSongs){
        return new RecyclerViewAdapter(listOfSongs, new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerViewAdapter.ViewHolder holder, Song song) {
                changeItemViewTitleColor(holder);
                changeAlbumCoverPicture(getImageCoverFromMusicFile(song.getAlbumCoverPath()));
                setSongVariablesInTextViews(song);

                //If no Music is playing, change play icon to pause icon when song is clicked
                //If song is playing then do nothing for the animation. since the pause button is already visible.
                if(!Globals.isMusicPlaying) {
                    animations.playToPauseAnimation(playButton);
                    Globals.isMusicPlaying = true;
                }

                playSelectedSong(song.getSongPath());

            }
        });
    }

    //Currently_playing_song_tab has a FrameLayout containing back and front side of an ImageView ( actually two ImageViews in FrameLayout ) back shows first in app.
    //when clicked an animation plays that flips over to the opposite ImageView and displays the album cover of the newly clicked song
    //isAlbumBackVisible keeps record of which side is on the visible side.
    private void changeAlbumCoverPicture(Bitmap newAlbumCover){
        if(!isAlbumBackVisible){
            backAlbumImage.setImageBitmap(newAlbumCover);
            animations.backToFrontAnimation(albumBack, albumFront);
            isAlbumBackVisible = true;
        } else {
            frontAlbumImage.setImageBitmap(newAlbumCover);
            animations.frontToBackAnimation(albumFront, albumBack);
            isAlbumBackVisible = false;
        }
    }

    private Bitmap getImageCoverFromMusicFile(String filePath){
        File image = new File(filePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        return BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
    }

    //Sets the song title and band name in the TextViews from the currently_playing_song_tab.xml
    private void setSongVariablesInTextViews(Song song){
        songTitleView.setText(song.getSongName());
        songBandView.setText(song.getArtist());
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

    private void playSelectedSong(String songPath){
        //gets subclass of Application for global variable globalsSong
        Globals globals = ((Globals) getContext().getApplicationContext());
        //Checks if there was a song playing before the currently clicked song and stops it.
        if(globals.getCurrentlyPlayingSong() != null){
            MediaPlayer previousPlayingSong = globals.getCurrentlyPlayingSong();
            previousPlayingSong.reset();
            previousPlayingSong.release();
        }

        //TODO Solve error after tapping songs quickly (-19, 0) error
        //Starts new MediaPlayer with the current clicked song
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //sets the new clicked song as the currently playing song
        globals.setCurrentlyPlayingSong(mediaPlayer);
        ((MainActivity)getActivity()).syncProgressBarWithAudioDuration(mediaPlayer);

    }
}
