package com.example.rowin.urchinmusicplayer.events;

/**
 * Created by Rowin on 4/5/2018.
 */

public class SkipSongEvent {
    private final String skipType;

    public SkipSongEvent(String skipType) {
        this.skipType = skipType;
    }

    public String getSkipType(){
        return skipType;
    }
}
