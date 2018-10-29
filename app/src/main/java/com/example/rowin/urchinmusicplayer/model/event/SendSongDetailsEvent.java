package com.example.rowin.urchinmusicplayer.model.event;

import com.example.rowin.urchinmusicplayer.model.Song;

/**
 * Created by Rowin on 4/5/2018.
 */

public class SendSongDetailsEvent {
    private final Long duration;
    private final int songIndex;
    private final int songAlbumColor;
    private final Song song;

    public SendSongDetailsEvent(Long duration, int songIndex, int songAlbumColor, Song song) {
        this.duration = duration;
        this.songIndex = songIndex;
        this.songAlbumColor = songAlbumColor;
        this.song = song;
    }

    public Long getDuration(){
        return duration;
    }

    public int getSongIndex(){
        return songIndex;
    }

    public int getSongAlbumColor(){
        return songAlbumColor;
    }

    public Song getSong(){
        return song;
    }
}
