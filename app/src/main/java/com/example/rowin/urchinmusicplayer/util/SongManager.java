package com.example.rowin.urchinmusicplayer.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rowin.urchinmusicplayer.activity.MainActivity;
import com.example.rowin.urchinmusicplayer.model.Globals;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Rowin on 2/22/2018.
 */

public class SongManager {
    private Context context;
    private ArrayList<Song> listOfSongs = new ArrayList<>();
    private Globals globals;
    private ProgressObserver progressObserver = null;
    private Boolean isAlbumBackVisible = false;
    private Animations animations;

    private View albumBack, albumFront;
    private ImageView frontAlbumImage, backAlbumImage;


    public SongManager(Context context){
        this.context = context;
        animations = new Animations(context);
        initializeViews();
    }

    private void initializeViews(){
        frontAlbumImage = ((MainActivity) context).frontAlbumCoverView;
        backAlbumImage = ((MainActivity) context).backAlbumCoverView;
        albumBack = ((MainActivity) context).backAlbumCoverLayout;
        albumFront = ((MainActivity) context).frontAlbumCoverLayout;
    }

    //Plays the sequence for playing a newly selected song ( stops currently playing song if one is playing, starts the new one and starts progressBar )
    public void playSelectedSongSequence(String songPath, ProgressBar progressBar){
        //gets subclass of Application for global variable globalsSong
        globals = ((Globals) context.getApplicationContext());
        MediaPlayer currentlyPlayingSong = globals.getCurrentlyPlayingSong();

        //Checks if there was a song playing before the currently clicked song and stops it.
        stopCurrentlyPlayingSong(currentlyPlayingSong);
        //Starts new MediaPlayer with the current clicked song
        startSelectedSong(songPath, progressBar);

    }

    private void startSelectedSong(String songPath, final ProgressBar progressBar){
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    progressObserver.stop();
                    progressBar.setProgress(mediaPlayer.getCurrentPosition());
                }
            });
            progressObserver = new ProgressObserver(mediaPlayer, progressBar);
            mediaPlayer.prepare();
            mediaPlayer.start();
            new Thread(progressObserver).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //sets the new clicked song as the currently playing song
        globals.setCurrentlyPlayingSong(mediaPlayer);

    }

    private void stopCurrentlyPlayingSong(MediaPlayer currentlyPlayingSong){
        if(currentlyPlayingSong != null){
            progressObserver.stop();
            currentlyPlayingSong.reset();
            currentlyPlayingSong.release();
        }
    }

    //Currently_playing_song_tab has a FrameLayout containing back and front side of an ImageView ( actually two ImageViews in FrameLayout ) back shows first in app.
    //when clicked an animation plays that flips over to the opposite ImageView and displays the album cover of the newly clicked song
    //isAlbumBackVisible keeps record of which side is on the visible side.
    public void changeAlbumCoverPicture(Bitmap newAlbumCover){
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

    public Bitmap getAlbumCoverFromMusicFile(String filePath){
        File image = new File(filePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        return BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
    }

    //Sets the song title and band name in the TextViews from the currently_playing_song_tab.xml
    public void setSongVariablesInTextViews(Song song){
        TextView songTitleView = ((MainActivity) context).songTitleView;
        TextView songBandView = ((MainActivity) context).songBandView;

        songTitleView.setText(song.getSongName());
        songBandView.setText(song.getArtist());
    }

    //Retrieve all songs
    public ArrayList<Song> getSongList() {
        ContentResolver cr = context.getContentResolver();
        Uri musicDirectory = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = cr.query(musicDirectory, null, null, null, null);
        if (cursor == null) {
            Toast.makeText(context, "No media Files present",
                    Toast.LENGTH_SHORT).show();
        }
        while (cursor != null && cursor.moveToNext()) {

            String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String songAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            Long songDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            Long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String songPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            String albumCoverPath = null;

            Cursor cursorAlbum = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART}, MediaStore.Audio.Albums._ID + "=" + albumId, null, null);

            if(cursorAlbum != null && cursorAlbum.moveToFirst()){
                albumCoverPath = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            }

            listOfSongs.add(createSongObject(songAlbum, artist, songDuration, displayName, albumCoverPath, songPath));
            assert cursorAlbum != null;
            cursorAlbum.close();
        }
        assert cursor != null;
        cursor.close();
        return listOfSongs;
    }

    private Song createSongObject(String songAlbum, String artist, Long songDuration, String displayName, String albumCoverPath, String songPath){
        Song song = new Song();
        song.setAlbum(songAlbum);
        song.setArtist(artist);
        song.setDuration(convertToDuration(songDuration));
        song.setSongName(displayName);
        song.setAlbumCoverPath(albumCoverPath);
        song.setSongPath(songPath);
        return song;
    }

    //MediaStore.Audio.Media.Duration returns value in milliseconds, this function converts to minute:seconds format (example 3:22)
    private String convertToDuration(Long songDuration){
        long seconds = songDuration/1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes +":"+seconds;
    }
}
