package com.example.rowin.urchinmusicplayer.events;

/**
 * Created by Rowin on 4/5/2018.
 */

public class ProgressUpdateEvent {
    private final int currentPosition;

    public ProgressUpdateEvent(int currentPosition){
        this.currentPosition = currentPosition;
    }

    public int getCurrentPosition(){
        return currentPosition;
    }
}
