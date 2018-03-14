package com.example.rowin.urchinmusicplayer.model;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.rowin.urchinmusicplayer.activity.MainActivity;
import com.example.rowin.urchinmusicplayer.util.PathToBitmapConverter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Rowin on 3/7/2018.
 */

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
       MediaPlayer.OnSeekCompleteListener, AudioManager.OnAudioFocusChangeListener {

    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> listOfSongs;
    private Integer songIndex;
    //path to the audio file
    private String mediaFile;
    private int resumePosition;
    private boolean songHasBeenPlayed = false;

    private AudioManager audioManager;
    private MusicStorage musicStorage;
    private PathToBitmapConverter pathToBitmapConverter;
    private AudioFocusRequest audioFocusRequest;
    private BecomingNoisyReceiver becomingNoisyReceiver;
    private NextSongReceiver nextSongReceiver;
    private ChangeMediaStateReceiver changeMediaStateReceiver;
    private SkipSongReceiver skipSongReceiver;

    private boolean ongoingCall = false;

    //TODO FIX PROGRESSBAR RECEIVER, STARTING NEW THREAD WHEN PLAYING NEW SONG TO FAST AFTER ANOTHER CRASHES THE APP OR CALLS ONCOMPLETE BECAUSE ERROR POPS


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();

        registerBecomingNoisyReceiver();
        registerNextSongReceiver();
        registerChangeMediaStateReceiver();
        registerSkipSongReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        pathToBitmapConverter = new PathToBitmapConverter();
        musicStorage = new MusicStorage(getApplicationContext());
        songIndex = musicStorage.loadAudioIndex();

        listOfSongs = musicStorage.loadAudio();
        Song songToBePlayed = listOfSongs.get(songIndex);


        if(songIndex != -1 && songIndex < listOfSongs.size()){
            mediaFile = songToBePlayed.getSongPath();
        } else {
            stopSelf();
        }

        if(requestAudioFocus()){
            initAndPrepareMediaPlayer();

            //sendSongProgressToActivity();
        } else {
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mediaPlayer != null){
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();

        unregisterReceiver(nextSongReceiver);
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(changeMediaStateReceiver);
        unregisterReceiver(skipSongReceiver);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopMedia();

        int newSongIndex = songIndex + 1;

        if(newSongIndex != -1 && newSongIndex < listOfSongs.size()) {
            songIndex = newSongIndex;
            Song song = listOfSongs.get(songIndex);
            mediaFile = song.getSongPath();

            initAndPrepareMediaPlayer();

            sendSongToActivity(song);
            //sendSongProgressToActivity();
        }

        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //On startup of application, the app looks if there is a song stored in SharedPreferences and loads in it the "song tab" (view that displays currently playing song)
        //When there is a song loaded in SharedPreferences we want the song prepared and initialized but not yet playing. On startup the lastly played song gets initialized
        //and prepared  but not played. all songs that the user selects afterwards get prepared and played immediately.
        if(songIndex != null && !songHasBeenPlayed) {
            songHasBeenPlayed = true;
        } else {
            playMedia();
        }

        //sendSongProgressToActivity();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invokes when an Audio focus change occurs.
        switch(focusState){
            //When app gets audio focus over other apps, play music
            case AudioManager.AUDIOFOCUS_GAIN:
                if(mediaPlayer==null) { initMediaPlayer(); }
                playMedia();
                //sendSongToActivity();
                //sendSongProgressToActivity();
                break;
            //When app loses audio focus over other apps, stop playing music if music is playing so that music
            //from multiple apps do not overlap
            case AudioManager.AUDIOFOCUS_LOSS:
                stopMedia();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            // Lost focus for a short time, but we have to stop
            // playback. We don't release the media player because playback
            // is likely to resume
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pauseMedia();
                break;
            // Lost focus for a short time, but it's ok to keep playing
            // at an attenuated level
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
        }
    }


    private void sendSongToActivity(Song song){
        Intent broadCastIntent = new Intent();

        int dominantColorAlbumCover = getDominantColor(pathToBitmapConverter.getAlbumCoverFromMusicFile(song.getAlbumCoverPath()));
        int complimentedColor = getComplimentedColor(dominantColorAlbumCover);

        broadCastIntent.setAction(MainActivity.BROADCAST_ACTION);
        broadCastIntent.putExtra("songDuration", song.getDuration());
        broadCastIntent.putExtra("song", song);
        broadCastIntent.putExtra("albumCoverColor", complimentedColor);
        broadCastIntent.putExtra("newIndex", songIndex);
        sendBroadcast(broadCastIntent);
    }

    public int getDominantColor(Bitmap bitmap) {
        if (bitmap == null) {
            return Color.TRANSPARENT;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int pixels[] = new int[size];
        //Bitmap bitmap2 = bitmap.copy(Bitmap.Config.ARGB_43444, false);
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int color;
        int r = 0;
        int g = 0;
        int b = 0;
        int a;
        int count = 0;
        for (int i = 0; i < pixels.length; i++) {
            color = pixels[i];
            a = Color.alpha(color);
            if (a > 0) {
                r += Color.red(color);
                g += Color.green(color);
                b += Color.blue(color);
                count++;
            }
        }
        r /= count;
        g /= count;
        b /= count;
        r = (r << 16) & 0x00FF0000;
        g = (g << 8) & 0x0000FF00;
        b = b & 0x000000FF;
        color = 0xFF000000 | r | g | b;
        return color;
    }

    public int getComplimentedColor(int color) {
        // get existing colors
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        double luma = 0.2126 * red + 0.7152 * green + 0.0722 * blue;

        if(luma < 40 || luma > 220){
            red = 51;
            green = 102;
            blue = 255;
        } else {
            //Adds value to the dominant value of a color, making it pop out more.
            if (red > blue && red > green && red + 60 < 255) {
                red = red + 50;
            }

            if (green > red && green > blue && green + 60 < 255) {
                green = green + 50;
            }

            if (blue > red && blue > green && blue + 60 < 255) {
                blue = blue + 50;
            }
        }


        return Color.argb(alpha, red, green, blue);
    }


//    private void sendSongProgressToActivity(){
//        ProgressRunnable progressRunnable = new ProgressRunnable();
//        new Thread(progressRunnable).start();
//    }

    private void registerBecomingNoisyReceiver(){
        becomingNoisyReceiver = new BecomingNoisyReceiver();
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private void registerNextSongReceiver(){
        nextSongReceiver = new NextSongReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.PLAY_NEW_AUDIO);
        registerReceiver(nextSongReceiver, intentFilter);
    }

    private void registerChangeMediaStateReceiver(){
        changeMediaStateReceiver = new ChangeMediaStateReceiver();
        IntentFilter intentFilter = new IntentFilter(MainActivity.CHANGE_MEDIA_STATE);
        registerReceiver(changeMediaStateReceiver, intentFilter);
    }

    private void registerSkipSongReceiver(){
        skipSongReceiver = new SkipSongReceiver();
        IntentFilter intentFilter = new IntentFilter(MainActivity.SKIP_SONG);
        registerReceiver(skipSongReceiver, intentFilter);
    }

    private void callStateListener(){
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };

        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(mediaFile);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    private void initAndPrepareMediaPlayer(){
        initMediaPlayer();
        mediaPlayer.prepareAsync();
    }

    private void playMedia(){
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }

    private void stopMedia(){
        if(mediaPlayer == null){ return;}
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void pauseMedia(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia(){
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private Boolean requestAudioFocus(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = 0;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();

            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener(this).build();

            result = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            if (audioManager != null) {
                result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        }

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

    }

    private void removeAudioFocus(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        }
        audioManager.abandonAudioFocus(this);
    }


    public class LocalBinder extends Binder {
        public MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }

//    public class ProgressRunnable implements Runnable {
//
//        @Override
//        public void run() {
//            while(mediaPlayer.isPlaying()) {
//                try {
//                    Thread.sleep(1000);
//                    Intent broadcastIntent = new Intent();
//                    broadcastIntent.setAction(MainActivity.AUDIO_PROGRESS);
//                    broadcastIntent.putExtra("currentPosition", mediaPlayer.getCurrentPosition());
//                    sendBroadcast(broadcastIntent);
//                } catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
        }
    }

    private class ChangeMediaStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
    }

    private class NextSongReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //If broadcast receives index for new song, stop media that is currently playing
            stopMedia();

            songIndex = intent.getIntExtra("songIndex", 0);
            Song song = listOfSongs.get(songIndex);
            mediaFile = song.getSongPath();
            musicStorage.storeAudioIndex(songIndex);

            initAndPrepareMediaPlayer();
            sendSongToActivity(song);
        }
    }

    private class SkipSongReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int skipType = intent.getIntExtra("skipType", 0 );
            if(skipType == MainActivity.SKIP_TO_NEXT){
                int newSongIndex = songIndex + 1;
                if(newSongIndex != -1 && newSongIndex < listOfSongs.size()){
                    stopMedia();

                    songIndex = newSongIndex;
                    mediaFile = listOfSongs.get(songIndex).getSongPath();
                    musicStorage.storeAudioIndex(newSongIndex);

                    initAndPrepareMediaPlayer();

                    sendSongToActivity(listOfSongs.get(songIndex));
                    //sendSongProgressToActivity();
                }
            }
        }
    }
}
