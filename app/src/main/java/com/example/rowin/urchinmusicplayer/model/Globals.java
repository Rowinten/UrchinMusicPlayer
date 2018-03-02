package com.example.rowin.urchinmusicplayer.model;

import android.app.Application;
import android.media.MediaPlayer;

/**
 * Created by Rowin on 3/1/2018.
 */

public class Globals extends Application {

    private MediaPlayer currentlyPlayingSong;
    public static Boolean isMusicPlaying = false;

    public MediaPlayer getCurrentlyPlayingSong(){
        return this.currentlyPlayingSong;
    }

    public void setCurrentlyPlayingSong(MediaPlayer currentlyPlayingSong){
        this.currentlyPlayingSong = currentlyPlayingSong;
    }
}
