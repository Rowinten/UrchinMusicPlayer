package com.example.rowin.urchinmusicplayer.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.adapter.SectionsPagerAdapter;
import com.example.rowin.urchinmusicplayer.model.Globals;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Animations;
import com.example.rowin.urchinmusicplayer.util.SongManager;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    private ArrayList<Song> listOfSongs;
    public ImageView playButton, nextSongButton, backAlbumCoverView, frontAlbumCoverView;
    public View frontAlbumCoverLayout, backAlbumCoverLayout;
    public TextView songTitleView, songBandView;
    private Animations animations;
    private Globals globalVars;
    private ProgressBar audioProgressBar;

    private Handler progressBarHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);


        playButtonOnClick();
        nextSongButtonOnClick();
    }


    private void initializeViews(){
        animations = new Animations(this);
        globalVars = ((Globals)getApplicationContext());

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

    private void nextSongButtonOnClick(){
        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animations.nextSongAnimation(nextSongButton);
            }
        });
    }

    //TODO App crashes when new song is clicked after the first song

    public void syncProgressBarWithAudioDuration(final MediaPlayer currentlyPlayingSong){
        audioProgressBar.setProgress(0);
        audioProgressBar.setMax(currentlyPlayingSong.getDuration());

        //Start a new thread so that we can update the progressBar at 1000 ms intervals by calling Thread.sleep(1000)
        new Thread(new Runnable() {
            @Override
            public void run() {
                //While the time the song is currently at is smaller than the total time of the song, keep updating the progressBar
                while(currentlyPlayingSong.getCurrentPosition() < currentlyPlayingSong.getDuration() && Globals.isMusicPlaying){

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    progressBarHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            audioProgressBar.setProgress(currentlyPlayingSong.getCurrentPosition());
                        }
                    });
                }
            }
        }).start();

    }





    private void playButtonOnClick(){
        playButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                MediaPlayer currentlyPlayingSong = globalVars.getCurrentlyPlayingSong();
                //If no music is playing and button is clicked, change play animation for play to pause button
                // else play the opposite animation
                if(!Globals.isMusicPlaying){
                    animations.playToPauseAnimation(playButton);
                    currentlyPlayingSong.start();

                    Globals.isMusicPlaying = true;
                } else {
                    animations.pauseToPlayAnimation(playButton);
                    currentlyPlayingSong.pause();
                    Globals.isMusicPlaying = false;
                }

            }
        });
    }

    private void setAdapterForViewPager(){
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), listOfSongs);
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted and now can proceed
                    SongManager manager = new SongManager(this);
                    listOfSongs =  manager.getSongList();

                    setAdapterForViewPager();
                } else {
                    //TODO Add function to close application if permission is denied
                    // permission denied
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

