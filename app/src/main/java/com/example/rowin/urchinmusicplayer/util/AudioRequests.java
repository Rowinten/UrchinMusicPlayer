package com.example.rowin.urchinmusicplayer.util;

import android.content.Context;
import android.content.Intent;

import com.example.rowin.urchinmusicplayer.activity.MainActivity;

/**
 * Created by Rowin on 3/15/2018.
 */

public class AudioRequests {

    public static final String BROADCAST_ACTION = "com.example.rowin.urchinmusicplayer.Broadcast";
    public static final String PLAY_NEW_AUDIO = "com.example.rowin.urchinmusicplayer.PlayNewAudio";
    public static final String AUDIO_PROGRESS = "com.example.rowin.urchinmusicplayer.AudioProgress";
    public static final String SKIP_SONG = "com.example.rowin.urchinmusicplayer.SkipSong";
    public static final String CHANGE_MEDIA_STATE = "com.example.rowin.urchinmusicplayer.PlayPause";
    public static final String SHUFFLE_AUDIO = "com.example.rowin.urchinmusicplayer.ShuffleAudio";

    public static final String SKIP_TO_NEXT = "com.example.rowin.urchinmusicplayer.NextSong";
    public static final String SKIP_TO_PREVIOUS = "com.example.rowin.urchinmusicplayer.PreviousSong";

    private Context context;

    public AudioRequests(Context context){
        this.context = context;
    }

    public void sendSkipSongRequest(String skipType){
        Intent skipSongIntent = new Intent();
        skipSongIntent.setAction(SKIP_SONG);
        skipSongIntent.putExtra("skipType", skipType);
        context.sendBroadcast(skipSongIntent);
    }

    public void sendChangeMediaStateRequest(){
        Intent changeMediaStateIntent = new Intent();
        changeMediaStateIntent.setAction(CHANGE_MEDIA_STATE);
        context.sendBroadcast(changeMediaStateIntent);
    }

    public void sendPlayNewSongRequest(int index){
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(PLAY_NEW_AUDIO);
        broadCastIntent.putExtra("songIndex", index);
        context.sendBroadcast(broadCastIntent);
    }

    public void sendShuffleRequest(){
        Intent shuffleIntent = new Intent();
        shuffleIntent.setAction(SHUFFLE_AUDIO);
        context.sendBroadcast(shuffleIntent);
    }

}
