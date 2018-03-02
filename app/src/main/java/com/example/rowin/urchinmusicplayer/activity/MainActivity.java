package com.example.rowin.urchinmusicplayer.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
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
    public ImageView playButton, nextSongButton, albumPictureView;
    public TextView songTitleView, songBandView;
    private Animations animations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        playButtonOnClick();
        nextSongButtonOnClick();
    }


    private void initializeViews(){
        animations = new Animations(this);
        albumPictureView = findViewById(R.id.album_picture_view);
        playButton = findViewById(R.id.play_pause_button);
        nextSongButton = findViewById(R.id.next_song_button);
        songTitleView = findViewById(R.id.song_title_view_currently_playing_tab);
        songBandView = findViewById(R.id.song_band_name_view_currently_playing_tab);
    }

    private void nextSongButtonOnClick(){
        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animations.nextSongAnimation(nextSongButton);
            }
        });
    }

    private void playButtonOnClick(){
        playButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                //If no music is playing and button is clicked, change play animation for play to pause button
                // else play the opposite animation
                if(!Globals.isMusicPlaying){
                    animations.playToPauseAnimation(playButton);
                    Globals.isMusicPlaying = true;
                } else {
                    animations.pauseToPlayAnimation(playButton);
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

