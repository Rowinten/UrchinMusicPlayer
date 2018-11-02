package com.example.rowin.urchinmusicplayer.controller;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.model.Album;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.model.event.EndActivityEvent;
import com.example.rowin.urchinmusicplayer.util.Animations;
import com.example.rowin.urchinmusicplayer.util.BlurBitmap;
import com.example.rowin.urchinmusicplayer.util.Converter;
import com.example.rowin.urchinmusicplayer.util.WindowUtils;
import com.example.rowin.urchinmusicplayer.view.adapter.SongRecyclerViewAdapter;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;

public class AlbumSongsActivity extends AppCompatActivity {

    private ArrayList<Song> listOfSongs;
    private String albumName;
    private String albumArtist;
    private String albumImagePath;
    private String currentSongName;
    private String currentSongArtist;
    private String currentSongImagePath;

    private RecyclerView recyclerView;
    private ConstraintLayout backgroundView, currentlyPlayingTab;
    private ImageView albumImageHolder;
    private TextView albumNameView;
    private TextView albumArtistView;
    private TextView currentSongNameView;
    private TextView currentSongArtistView;
    private FrameLayout albumInfoHolder;
    private CircleImageView frontAlbumImage, backAlbumImage;

    private Converter converter;
    private WindowUtils windowUtils;
    private MusicStorage musicStorage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EventBus.getDefault().register(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getIntents(getIntent());

        setContentView(R.layout.album_songs_activity);
        initializeViews();
        initializeClasses();

        adjustLayoutParams();

        Bitmap background = musicStorage.loadBitmapFromStorage(this);
        if(background != null){
            Drawable drawableBackground = new BitmapDrawable(getResources(), background);
            backgroundView.setBackground(drawableBackground);


        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int color = getResources().getColor(R.color.colorPrimaryDark, getTheme());
                backgroundView.setBackgroundColor(color);
                currentlyPlayingTab.setBackgroundColor(getResources().getColor(R.color.colorPrimary, getTheme()));

            } else {
                int color = getResources().getColor(R.color.colorPrimaryDark);
                backgroundView.setBackgroundColor(color);
                currentlyPlayingTab.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        }

        albumImageHolder.setImageBitmap(getAlbumImage());
        albumInfoHolder.bringToFront();
        albumArtistView.setText(albumArtist);
        albumNameView.setText(albumName);

        startActivityAnimation();
        initializeRecyclerView();
        initializeCurrentSongTab();
    }

    @Override
    public void onBackPressed() {
        ScaleAnimation fade_in =  new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fade_in.setDuration(200);     // animation duration in milliseconds
        fade_in.setFillAfter(true);    // If fillAfter is true, the transformation that this animation performed will persist when it is finished.
        albumInfoHolder.startAnimation(fade_in);

        Animation fadeIn = new AlphaAnimation(1, 0);
        fade_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                AlbumSongsActivity.super.onBackPressed();
                overridePendingTransition(0, 0);
                EventBus.getDefault().post(new EndActivityEvent());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(300);
        backgroundView.startAnimation(fadeIn);
    }

    private void initializeViews(){
        albumImageHolder = findViewById(R.id.album_activity_image);
        albumNameView = findViewById(R.id.album_song_title_view);
        albumArtistView = findViewById(R.id.album_name_song_activity);
        recyclerView = findViewById(R.id.album_activity_recycler_view);
        albumInfoHolder = findViewById(R.id.album_info_holder);
        backgroundView = findViewById(R.id.album_song_background_view);
        currentlyPlayingTab = findViewById(R.id.currently_playing_song_tab_album_activity);
        frontAlbumImage = findViewById(R.id.front_album_cover_view);
        backAlbumImage = findViewById(R.id.back_album_cover_view);
        currentSongNameView = findViewById(R.id.song_title_view_currently_playing_tab);
        currentSongArtistView = findViewById(R.id.song_band_name_view_currently_playing_tab);
    }

    private void initializeClasses(){
        converter = new Converter();
        windowUtils = new WindowUtils(this);
        musicStorage = new MusicStorage(this);
    }

    private void initializeCurrentSongTab(){
        if(currentSongImagePath != null) {
            Bitmap imageBitmap = converter.getAlbumCoverFromPath(currentSongImagePath);
            frontAlbumImage.setImageBitmap(imageBitmap);
        }

        if(currentSongName != null) {
            currentSongNameView.setText(currentSongName);
        }

        if(currentSongArtist !=  null) {
            currentSongArtistView.setText(currentSongArtist);
        }
    }

    private void initializeRecyclerView(){
        SongRecyclerViewAdapter recyclerViewAdapter = new SongRecyclerViewAdapter(this, recyclerView, listOfSongs, new SongRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, Song song) {

            }
        }, new SongRecyclerViewAdapter.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(int viewHeight) {

            }
        });

        recyclerView.setAdapter(recyclerViewAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.scrollToPosition(1);
    }

    private void adjustLayoutParams(){
        albumInfoHolder.getLayoutParams().height = (int) Math.round((windowUtils.getScreenHeight() * 0.35) + windowUtils.getStatusBarHeight());
        currentlyPlayingTab.setBackgroundColor(getResources().getColor(R.color.currentlyPlayingTabTransparency));
        currentlyPlayingTab.setPadding(0, 0, 0, windowUtils.getNavigationBarHeight());
    }

    private void getIntents(Intent intent){
        listOfSongs = intent.getParcelableArrayListExtra("listOfSongs");
        albumImagePath = intent.getStringExtra("albumBitmapPath");
        albumName = intent.getStringExtra("albumTitle");
        albumArtist = intent.getStringExtra("albumArtist");

        currentSongName = intent.getStringExtra("currentSongName");
        currentSongArtist = intent.getStringExtra("currentSongArtist");
        currentSongImagePath = intent.getStringExtra("currentSongImagePath");
    }

    private void startActivityAnimation(){
        ScaleAnimation fade_in =  new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fade_in.setDuration(200);     // animation duration in milliseconds
        fade_in.setFillAfter(true);    // If fillAfter is true, the transformation that this animation performed will persist when it is finished.
        albumInfoHolder.setAnimation(fade_in);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(300);
        backgroundView.setAnimation(fadeIn);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(fade_in);
        animationSet.addAnimation(fadeIn);
        animationSet.start();
    }

    private void endActivityAnimation(){
        ScaleAnimation fade_in =  new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fade_in.setDuration(200);     // animation duration in milliseconds
        fade_in.setFillAfter(true);    // If fillAfter is true, the transformation that this animation performed will persist when it is finished.
        albumInfoHolder.setAnimation(fade_in);

        Animation fadeIn = new AlphaAnimation(1, 0);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(300);
        backgroundView.setAnimation(fadeIn);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(fade_in);
        animationSet.addAnimation(fadeIn);
        animationSet.start();


        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d("Kappaa", "123");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d("Kappa", "123");
                AlbumSongsActivity.super.onBackPressed();
                overridePendingTransition(0, 0);
                new EndActivityEvent();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private Bitmap getAlbumImage(){
        return converter.getAlbumCoverFromPath(albumImagePath);
    }

    private BitmapDrawable getBlurredAlbumImage(){
        Bitmap albumImage = getAlbumImage();
        Bitmap blurredAlbumImage = BlurBitmap.blur(this, albumImage);
        return new BitmapDrawable(getResources(), blurredAlbumImage);
    }

}
