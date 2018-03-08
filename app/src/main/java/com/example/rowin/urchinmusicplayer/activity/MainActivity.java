package com.example.rowin.urchinmusicplayer.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.adapter.SectionsPagerAdapter;
import com.example.rowin.urchinmusicplayer.model.MediaPlayerService;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Animations;
import com.example.rowin.urchinmusicplayer.util.ProgressObserver;
import com.example.rowin.urchinmusicplayer.util.SongManager;

import java.io.File;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    public static final String BROADCAST_ACTION = "com.example.rowin.urchinmusicplayer";
    public static final String PLAY_NEW_AUDIO = "com.example.rowin.urchinmusicplayer.PlayNewAudio";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Animations animations;
    private ServiceConnection serviceConnection;
    private MediaPlayerService playerService;
    private SongBroadCastReceiver songBroadCastReceiver;
    private ProgressObserver progressObserver;

    public ProgressBar audioProgressBar;
    public ImageView playButton, nextSongButton, backAlbumCoverView, frontAlbumCoverView;
    public View frontAlbumCoverLayout, backAlbumCoverLayout;
    public TextView songTitleView, songBandView;

    private boolean serviceBound = false;
    private Boolean isAlbumBackVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        createServiceConnection();

        songBroadCastReceiver = new SongBroadCastReceiver();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);


        //playButtonOnClick();
        nextSongButtonOnClick();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);
        registerReceiver(songBroadCastReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(serviceBound){
            unbindService(serviceConnection);
            playerService.stopSelf();
        }

        unregisterReceiver(songBroadCastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("serviceState", serviceBound);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceState");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted and now can proceed
                    SongManager manager = new SongManager(this);
                    MusicStorage musicStorage = new MusicStorage(this);
                    musicStorage.storeAudio(manager.getSongsFromMusicDirectory());

                    //listOfSongs =  manager.getSongList();

                    //listOfSongs = MusicStorage.getInstance().getListOfSongs();
                    setAdapterForViewPager();
                } else {
                    //TODO Add function to close application if permission is denied
                    // permission denied
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initializeViews(){
        animations = new Animations(this);

        playButton = findViewById(R.id.play_pause_button);
        nextSongButton = findViewById(R.id.next_song_button);
        songTitleView = findViewById(R.id.song_title_view_currently_playing_tab);
        songBandView = findViewById(R.id.song_band_name_view_currently_playing_tab);
        frontAlbumCoverLayout = findViewById(R.id.front_album_cover_layout);
        backAlbumCoverLayout = findViewById(R.id.back_album_cover_layout);

        backAlbumCoverView = findViewById(R.id.back_album_cover_view);
        frontAlbumCoverView = findViewById(R.id.front_album_cover_view);

        audioProgressBar = findViewById(R.id.audio_progress_bar);
    }

    private void createServiceConnection(){
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) iBinder;
                playerService = binder.getService();
                serviceBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                serviceBound = false;
            }
        };
    }

    public void playAudio(int index){
        if(!serviceBound){
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            MusicStorage musicStorage = new MusicStorage(this);
            musicStorage.storeAudioIndex(index);

            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            sendPlayNewSongRequest(index);
        }
    }

    private void sendPlayNewSongRequest(int index){
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(PLAY_NEW_AUDIO);
        broadCastIntent.putExtra("songIndex", index);
        sendBroadcast(broadCastIntent);
    }

    //Currently_playing_song_tab has a FrameLayout containing back and front side of an ImageView ( actually two ImageViews in FrameLayout ) back shows first in app.
    //when clicked an animation plays that flips over to the opposite ImageView and displays the album cover of the newly clicked song
    //isAlbumBackVisible keeps record of which side is on the visible side.
    private void changeAlbumCoverPicture(Bitmap newAlbumCover){
        if(!isAlbumBackVisible){
            backAlbumCoverView.setImageBitmap(newAlbumCover);
            animations.backToFrontAnimation(backAlbumCoverLayout, frontAlbumCoverLayout);
            isAlbumBackVisible = true;
        } else {
            frontAlbumCoverView.setImageBitmap(newAlbumCover);
            animations.frontToBackAnimation(frontAlbumCoverLayout, backAlbumCoverLayout);
            isAlbumBackVisible = false;
        }
    }

    private Bitmap getAlbumCoverFromMusicFile(String filePath){
        File image = new File(filePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        return BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
    }

    //Sets the song title and band name in the TextViews from the currently_playing_song_tab.xml
    private void updateTextViews(Song song){
        songTitleView.setText(song.getSongName());
        songBandView.setText(song.getArtist());
    }


    private void nextSongButtonOnClick(){
        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animations.nextSongAnimation(nextSongButton);
            }
        });
    }

//    private void playButtonOnClick(){
//        playButton.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//            @Override
//            public void onClick(View view) {
//                MediaPlayer currentlyPlayingSong = globalVars.getCurrentlyPlayingSong();
//                //If no music is playing and button is clicked, change play animation for play to pause button
//                // else play the opposite animation
//                if(!Globals.isMusicPlaying){
//                    animations.playToPauseAnimation(playButton);
//                    currentlyPlayingSong.start();
//
//                    Globals.isMusicPlaying = true;
//                } else {
//                    animations.pauseToPlayAnimation(playButton);
//                    currentlyPlayingSong.pause();
//                    Globals.isMusicPlaying = false;
//                }
//
//            }
//        });
//    }

    private void setAdapterForViewPager(){
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        ViewPager mViewPager = findViewById(R.id.container);

        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mSectionsPagerAdapter.createTabIcons(tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mSectionsPagerAdapter.changeTabToSelected(tab, tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                mSectionsPagerAdapter.changeTabToUnselected(tab, tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    class SongBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Song song = intent.getParcelableExtra("song");
            changeAlbumCoverPicture(getAlbumCoverFromMusicFile(song.getAlbumCoverPath()));
            updateTextViews(song);
        }
    }
}

