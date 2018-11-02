package com.example.rowin.urchinmusicplayer.controller;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.model.event.ChangeMediaPositionEvent;
import com.example.rowin.urchinmusicplayer.model.event.ChangeMediaStateEvent;
import com.example.rowin.urchinmusicplayer.model.event.ListChangedEvent;
import com.example.rowin.urchinmusicplayer.model.event.PlaySongEvent;
import com.example.rowin.urchinmusicplayer.model.event.ProgressUpdateEvent;
import com.example.rowin.urchinmusicplayer.model.event.SendSongDetailsEvent;
import com.example.rowin.urchinmusicplayer.model.event.ShuffleEvent;
import com.example.rowin.urchinmusicplayer.model.event.SkipSongEvent;
import com.example.rowin.urchinmusicplayer.util.ColorReader;
import com.example.rowin.urchinmusicplayer.util.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

/**
 * Created by Rowin on 3/7/2018.
 */

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String SKIP_TO_NEXT = "com.example.rowin.urchinmusicplayer.NextSong";
    public static final String SKIP_TO_PREVIOUS = "com.example.rowin.urchinmusicplayer.PreviousSong";

    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> listOfSongs;
    private ArrayList<Song> shuffledSongList = new ArrayList<>();
    private Integer songIndex;
    private int currentlyPlayingSongID;

    //When user clicks song, this is the beginning index the onCompletion listener references, so that when user doesn't select song, and listens to all songs in list
    //it ends again at this index. resets when user selects a new song.
    private Integer selectedSongIndex;
    //path to the audio file
    private String mediaFile;
    private int resumePosition;
    private boolean songHasBeenPlayed = false;

    private AudioManager audioManager;
    private MusicStorage musicStorage;
    private ColorReader colorReader;
    private Converter converter;
    private AudioFocusRequest audioFocusRequest;
    private BecomingNoisyReceiver becomingNoisyReceiver;
    private Timer updateTimer;

    private boolean ongoingCall = false;
    private boolean isShuffled = false;

    //TODO Songs have ids, when a list changes because of filter send event to service notifying the changes. service plays new list.
    //TODO but service needs the new index in that list from where to start from, the song that is currently playing.
    //TODO to get new index look for the id from that song in the list. make sure when song gets played to temp store the id as  a variable and change the value when song changes to another song.
    //TODO start with creating onEvent in MediaPlayerService that watches for ListChangedEvent.

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();
        EventBus.getDefault().register(this);
        registerBecomingNoisyReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        colorReader = new ColorReader();
        converter = new Converter();
        musicStorage = new MusicStorage(getApplicationContext());

        //songIndex = musicStorage.loadAudioIndex();
        //listOfSongs = musicStorage.loadAudio();

        listOfSongs = intent.getParcelableArrayListExtra("listOfSongs");
        songIndex = intent.getIntExtra("songIndex", 1);
        shuffledSongList.addAll(listOfSongs);

        Song currentSong = listOfSongs.get(songIndex);
        currentlyPlayingSongID = currentSong.getId();

        if (songIndex != -1 && songIndex < listOfSongs.size()) {
            mediaFile = currentSong.getSongPath();
        } else {
            stopSelf();
        }

        if (requestAudioFocus()) {
            initAndPrepareMediaPlayer();

        } else {
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(becomingNoisyReceiver);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopProgressUpdateEvent();

        if(isShuffled && !shuffledSongList.isEmpty()){
            shuffleToNextMedia();
        } else {
            //OnCompletion of song, go to next song by +1 the song index
            int newSongIndex = songIndex + 1;
            //Checks if the index is valid and not out of bounds, then sets the newSongIndex as
            //the new index so that when the new mediaPlayer is initialized, the mediaPlayer knows which song to pick.
            if (newSongIndex != -1 && newSongIndex < listOfSongs.size()) {
                //When user selects a song by tapping on it, selectedSongIndex gets the value of that song, so that when user doesn't select a new song and listens
                //Through whole list of songs it ends playing songs at the selected song index again. When the selectedIndex is null, because for example user starts app
                //and clicks the next button first instead of selecting a song in the list. the selectedSongIndex will instead be the song the user lastly listened to in previous session.
                if(selectedSongIndex == null){
                    selectedSongIndex = songIndex;
                }

                //checks if index isn't equal to first clicked song index. so that when users clicks on song
                //and listens to all songs in playlist, it ends at the first clicked song again.
                if(newSongIndex != selectedSongIndex) {
                    //checks if index is at the end of the list, so that next index starts at beginning again.
                    if (listOfSongs.size() == newSongIndex) {
                        newSongIndex = 0;
                    }
                    songIndex = newSongIndex;
                    Song song = listOfSongs.get(songIndex);
                    mediaFile = song.getSongPath();

                    initAndPrepareMediaPlayer();
                    sendSongToActivity(song);
                }
            }

            stopSelf();
        }
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
        if (songIndex != null && !songHasBeenPlayed) {
            songHasBeenPlayed = true;
        } else {
            playMedia();
            startProgressUpdateEvent();
        }

    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invokes when an Audio focus change occurs.
        switch (focusState) {
            //When app gets audio focus over other apps, play music
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) {
                    initMediaPlayer();
                }
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
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
        }
    }


    private void sendSongToActivity(Song song) {
        int dominantColorAlbumCover = colorReader.getDominantColor(converter.getAlbumCoverFromPath(song.getAlbumCoverPath()));
        int complimentedColor = colorReader.getComplimentedColor(dominantColorAlbumCover);

        EventBus.getDefault().post(new SendSongDetailsEvent(song.getDuration(), songIndex, complimentedColor, song));
    }

    private void registerBecomingNoisyReceiver() {
        becomingNoisyReceiver = new BecomingNoisyReceiver();
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }


    private void callStateListener() {
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

    private void initAndPrepareMediaPlayer() {
        stopMedia();
        initMediaPlayer();
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private void shuffleToNextMedia(){
        //Checks if shuffledSongList is Empty, every time a song gets picked it gets removed from the shuffledSongList. That way a song cannot get selected twice,
        //and will shuffle through all the songs in random order.

        int index = new Random().nextInt(shuffledSongList.size());
        Song shufflePickedSong = shuffledSongList.get(index);
        shuffledSongList.remove(shufflePickedSong);

        //Gets the position of song in the main list, listOfSongs, this list does not change and is needed because played audio is based off of that list.
        //Otherwise wrong audio gets played due to wrong indexes (shuffledSongList is getting smaller and indexes get incorrect in comparison to main list in return )
        songIndex = listOfSongs.indexOf(shufflePickedSong);
        mediaFile = listOfSongs.get(songIndex).getSongPath();
        musicStorage.saveCurrentSong(shufflePickedSong);

//        musicStorage.storeAudioIndex(songIndex);
//        musicStorage.storeAudioName(listOfSongs.get(songIndex).getSongName());

        initAndPrepareMediaPlayer();
        sendSongToActivity(listOfSongs.get(songIndex));
    }

    private void skipTo(String skipType) {
        if (Objects.equals(skipType, "NEXT")) {
            songIndex = songIndex + 1;
        } else if (Objects.equals(skipType, "PREVIOUS")) {
            //todo media has been stopped before getCurrentPosition call, getCurrentPosition cannot be called when mediaplayer has been stopped.
            if(mediaPlayer != null && mediaPlayer.getCurrentPosition() > 5000){
                mediaPlayer.seekTo(0);
                return;
            } else {
                songIndex = songIndex - 1;
            }
        }


        if(songIndex == -1){
            songIndex = listOfSongs.size()-1;
        } else if(songIndex == listOfSongs.size()){
            songIndex = 0;
        }

        if (songIndex != -1 && songIndex < listOfSongs.size()) {
            Song nextSong = listOfSongs.get(songIndex);
            mediaFile = listOfSongs.get(songIndex).getSongPath();
            musicStorage.saveCurrentSong(nextSong);
//            musicStorage.storeAudioIndex(newSongIndex);
//            musicStorage.storeAudioName(listOfSongs.get(songIndex).getSongName());

            initAndPrepareMediaPlayer();
            sendSongToActivity(listOfSongs.get(songIndex));
        }
    }

    private void resetShuffledSongList(ArrayList<Song> newListOfSongs){
        shuffledSongList.clear();
        shuffledSongList.addAll(newListOfSongs);
    }

    private Boolean requestAudioFocus() {
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

    private void removeAudioFocus() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        }
        audioManager.abandonAudioFocus(this);
    }

    private void startProgressUpdateEvent(){
//        updateTimer = new Timer();
//        updateTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                if(mediaPlayer.isPlaying()) {
//                    EventBus.getDefault().post(new ProgressUpdateEvent(mediaPlayer.getCurrentPosition()));
//                }
//            }
//        }, 0, 1000);
    }

    private void stopProgressUpdateEvent(){
        if(updateTimer == null){
            return;
        }
        updateTimer.cancel();
        updateTimer.purge();
    }

    private Boolean areArraysIdentical(ArrayList<Song> array1, ArrayList<Song> array2){
        for(Song song: array1){
            for(Song song2: array2){
                if(!song.equals(song2)){
                    return false;
                }
            }
        }
        return true;
    }

    //gets new index of song from a filtered (and changed) list, so that when next song is played it does not use the index of the previous list.
    private int getNewIndex(ArrayList<Song> listOfSongs) {
        Log.d("newIndex", String.valueOf(currentlyPlayingSongID));

        for(int i = 0; i < listOfSongs.size(); i++){
            Log.d("Indndnd", String.valueOf(listOfSongs.get(i).getId()));
            Log.d("Indndnd", String.valueOf(listOfSongs.get(i).getSongName()));
            if(listOfSongs.get(i).getId() == currentlyPlayingSongID){
                return i;
            }
        }
        return 0;
    }

    public void onEvent(ChangeMediaStateEvent changeMediaStateEvent){
        if(mediaPlayer.isPlaying()){
            pauseMedia();
            stopProgressUpdateEvent();
        } else {
            playMedia();
            startProgressUpdateEvent();
        }
    }

    public void onEvent(PlaySongEvent playSongEvent){
        stopProgressUpdateEvent();

        //Shuffled song list gets reset in both instances, because when user selects new song, all songs will be shuffled and played again.
        resetShuffledSongList(listOfSongs);

        songIndex = playSongEvent.getSongIndex();
        selectedSongIndex = songIndex;

        Song song = listOfSongs.get(songIndex);
        mediaFile = song.getSongPath();
        musicStorage.saveCurrentSong(song);
        currentlyPlayingSongID = song.getId();

//        musicStorage.storeAudioIndex(songIndex);
//        musicStorage.storeAudioName(song.getSongName());

        initAndPrepareMediaPlayer();
        sendSongToActivity(song);
    }

    public void onEvent(SkipSongEvent skipSongEvent){
        stopProgressUpdateEvent();

        if(isShuffled){
            //if user clicks next button but all songs in list have been shuffled and played
            //already, then refill shuffledSongList and shuffle and play again
            if(!shuffledSongList.isEmpty()) {
                shuffleToNextMedia();
            } else {
                shuffledSongList.addAll(listOfSongs);
            }
        } else {
            String skipType = skipSongEvent.getSkipType();
            if (Objects.equals(skipType, SKIP_TO_NEXT)) {
                skipTo("NEXT");
            } else if (Objects.equals(skipType, SKIP_TO_PREVIOUS)) {
                skipTo("PREVIOUS");
            }
        }
    }

    public void onEvent(ShuffleEvent shuffleEvent){
        isShuffled = !isShuffled;
    }

    public void onEvent(ChangeMediaPositionEvent changeMediaPositionEvent){
        int newPosition = changeMediaPositionEvent.getNewSongPosition();
        mediaPlayer.seekTo(newPosition);
    }

    public void onEvent(ListChangedEvent listChangedEvent){
        listOfSongs = listChangedEvent.getChangedList();
        songIndex = getNewIndex(listOfSongs);
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
        }
    }
}
