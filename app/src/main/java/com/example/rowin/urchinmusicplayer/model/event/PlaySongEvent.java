package com.example.rowin.urchinmusicplayer.model.event;

/**
 * Created by Rowin on 4/5/2018.
 */

public class PlaySongEvent {
    private final int songIndex;

    public PlaySongEvent(int songIndex) {
        this.songIndex = songIndex;
    }

    public int getSongIndex(){
        return songIndex;
    }
}
