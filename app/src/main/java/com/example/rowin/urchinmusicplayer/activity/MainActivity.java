package com.example.rowin.urchinmusicplayer.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.adapter.SectionsPagerAdapter;
import com.example.rowin.urchinmusicplayer.fragment.AlbumFragment;
import com.example.rowin.urchinmusicplayer.fragment.PlaylistFragment;
import com.example.rowin.urchinmusicplayer.fragment.SongListFragment;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.SongManager;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    private ArrayList<Song> listOfSongs;
    private ImageView playButton;
    private Boolean musicIsPlaying = false;

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
    }

    //TODO change view name imageButton to something more appropriate
    private void initializeViews(){
        playButton = findViewById(R.id.imageButton);
    }

    private void playButtonOnClick(){
        playButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if(!musicIsPlaying){
                    playButton.setImageResource(R.drawable.play_to_pause_animator);
                    Drawable playToPauseAnimation = playButton.getDrawable();
                    startAnimation(playToPauseAnimation);
                    musicIsPlaying = true;
                } else {
                    playButton.setImageResource(R.drawable.pause_to_play_animator);
                    Drawable pauseToPlayAnimation = playButton.getDrawable();
                    startAnimation(pauseToPlayAnimation);
                    musicIsPlaying = false;
                }

            }
        });
    }

    private void startAnimation(Drawable drawable){
        if(drawable instanceof Animatable){
            ((Animatable) drawable).start();
        }
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

