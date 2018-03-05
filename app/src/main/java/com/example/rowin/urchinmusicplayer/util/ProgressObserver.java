package com.example.rowin.urchinmusicplayer.util;

import android.media.MediaPlayer;
import android.widget.ProgressBar;

/**
 * Created by Rowin on 3/4/2018.
 */

public class ProgressObserver implements Runnable {
    private Boolean stop = false;
    private ProgressBar progressBar;
    private MediaPlayer currentlyPlayingSong;

    ProgressObserver(MediaPlayer currentlyPlayingSong, ProgressBar progressBar){
        this.currentlyPlayingSong = currentlyPlayingSong;
        this.progressBar = progressBar;
    }

    void stop(){
        stop = true;
    }

    @Override
    public void run() {
        progressBar.setProgress(0);
        progressBar.setMax(currentlyPlayingSong.getDuration());


        while(!stop){
            try {
                progressBar.setProgress(currentlyPlayingSong.getCurrentPosition());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
