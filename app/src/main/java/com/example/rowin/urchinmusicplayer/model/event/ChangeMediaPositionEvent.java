package com.example.rowin.urchinmusicplayer.model.event;

/**
 * Created by Rowin on 4/5/2018.
 */

//Is used to notify service to set currently playing song to different position in song, so for example:
    // notifies service that song needs to go from 1:33 to 4:12. or vice versa.
public class ChangeMediaPositionEvent {
    private final int newSongPosition;

    public ChangeMediaPositionEvent(int newSongPosition) {
        this.newSongPosition = newSongPosition;
    }

    public int getNewSongPosition() {
        return newSongPosition;
    }
}
