package com.example.rowin.urchinmusicplayer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.activity.MainActivity;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rowin on 3/6/2018.
 */

public class PlayAudio {
    private ProgressObserver progressObserver = null;
    private Boolean isAlbumBackVisible = false;
    private Animations animations;
    private Integer positionOfSongUriInArray;

    private View albumBack, albumFront;
    private ImageView frontAlbumImage, backAlbumImage;

    private Context context;
    private ProgressBar audioProgressBar;


    public PlayAudio(Context context){
        this.context = context;
        animations = new Animations(context);
        initializeViews();
    }

    private void initializeViews(){
        frontAlbumImage = ((MainActivity) context).frontAlbumCoverView;
        backAlbumImage = ((MainActivity) context).backAlbumCoverView;
        albumBack = ((MainActivity) context).backAlbumCoverLayout;
        albumFront = ((MainActivity) context).frontAlbumCoverLayout;
        audioProgressBar = ((MainActivity) context).audioProgressBar;
    }

    //Plays the sequence for playing a newly selected song ( stops currently playing song if one is playing, starts the new one and starts progressBar )
//    public void playSelectedSongSequence(Song song){
//        //gets subclass of Application for global variable globalsSong
//        globals = ((Globals) context.getApplicationContext());
//
//        updateCurrentlyPlayingSongTab(song);
//        //Checks if there was a song playing before the currently clicked song and stops it.
//        stopCurrentlyPlayingSong();
//        //Starts new MediaPlayer with the current clicked song
//        startSelectedSong(song);
//
//    }

//    private void startSelectedSong(Song song){
//        allSongUri = MusicStorage.getInstance().getAllSongUri();
//        positionOfSongUriInArray = getPositionInArray(allSongUri, song);
//
//        MediaPlayer mediaPlayer = MediaPlayer.create(context, allSongUri.get(positionOfSongUriInArray));
//        goToNextSongInPlayList(mediaPlayer);
//        progressObserver = new ProgressObserver(mediaPlayer, audioProgressBar);
//        mediaPlayer.start();
//        new Thread(progressObserver).start();
//
//        //sets the new clicked song as the currently playing song
//        globals.setCurrentlyPlayingSong(mediaPlayer);
//
//    }

//    private void stopCurrentlyPlayingSong(){
//        MediaPlayer currentlyPlayingSong = globals.getCurrentlyPlayingSong();
//        if(currentlyPlayingSong != null){
//            progressObserver.stop();
//            currentlyPlayingSong.reset();
//            currentlyPlayingSong.release();
//        }
//    }

//    private void goToNextSongInPlayList(MediaPlayer mediaPlayer){
//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                progressObserver.stop();
//                mediaPlayer.release();
//
//                positionOfSongUriInArray++;
//                mediaPlayer = MediaPlayer.create(context, allSongUri.get(positionOfSongUriInArray));
//                goToNextSongInPlayList(mediaPlayer);
//                mediaPlayer.start();
//
//                progressObserver = new ProgressObserver(mediaPlayer, audioProgressBar);
//                new Thread(progressObserver).start();
//                globals.setCurrentlyPlayingSong(mediaPlayer);
//            }
//        });
//    }

    private void updateCurrentlyPlayingSongTab(Song song){
        Bitmap songAlbumCover = getAlbumCoverFromMusicFile(song.getAlbumCoverPath());
        changeAlbumCoverPicture(songAlbumCover);
        updateTextViews(song);
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

    private Bitmap getAlbumCoverFromMusicFile(String filePath){
        File image = new File(filePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        return BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
    }

    //Sets the song title and band name in the TextViews from the currently_playing_song_tab.xml
    private void updateTextViews(Song song){
        TextView songTitleView = ((MainActivity) context).songTitleView;
        TextView songBandView = ((MainActivity) context).songBandView;

        songTitleView.setText(song.getSongName());
        songBandView.setText(song.getArtist());
    }

    private Integer getPositionInArray(ArrayList<Uri> allSongUri, Song song){
        Integer position = null;
        if(allSongUri.contains(Uri.parse(song.getSongPath()))){
            position = allSongUri.indexOf(Uri.parse(song.getSongPath()));
        }
        return position;
    }
}
