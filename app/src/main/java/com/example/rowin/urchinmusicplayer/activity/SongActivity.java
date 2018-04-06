package com.example.rowin.urchinmusicplayer.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.events.ChangeMediaPositionEvent;
import com.example.rowin.urchinmusicplayer.events.ChangeMediaStateEvent;
import com.example.rowin.urchinmusicplayer.events.FadeInActivityEvent;
import com.example.rowin.urchinmusicplayer.events.ProgressUpdateEvent;
import com.example.rowin.urchinmusicplayer.events.SendSongDetailsEvent;
import com.example.rowin.urchinmusicplayer.events.SkipSongEvent;
import com.example.rowin.urchinmusicplayer.model.MediaPlayerService;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Animations;
import com.example.rowin.urchinmusicplayer.util.ColorReader;
import com.example.rowin.urchinmusicplayer.util.Converter;
import com.example.rowin.urchinmusicplayer.util.WindowUtils;

import de.greenrobot.event.EventBus;

/**
 * Created by Rowin on 2-4-2018.
 */

public class SongActivity extends AppCompatActivity {
    private ImageView albumImageView;
    private AppBarLayout appBar;

    private ImageView playButton, nextButton, previousButton;
    private TextView songTitleView, songArtistView, progressCounterView, songDurationView;
    private SeekBar seekBar;

    private Converter converter;
    private WindowUtils windowUtils;
    private Animations animations;
    private ColorReader colorReader;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        EventBus.getDefault().register(this);

        initializeViews();
        initializeClasses();
        setAllViewsTransparent();
        bindViews(getIntent());

        registerNextButtonClickListener();
        registerPlayButtonClickListener();
        registerPreviousButtonClickListener();
        registerSeekBarChangeListener();


        windowUtils.setWindowMetrics(getWindow(), appBar);
        fadeInViews();
        animations.albumImageScaleIncreaseAnimation(albumImageView, windowUtils.getCenterScreenX(albumImageView));
        seekBar.setThumb(getResources().getDrawable(R.drawable.song_tab_icon_fill_animation));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {

        fadeOutViews();
        AnimationSet animationSet = animations.albumImageScaleDecreaseAnimationSet(windowUtils.getCenterScreenX(albumImageView));
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //sends event so that main activity views get faded in again ( visible )
                EventBus.getDefault().post(new FadeInActivityEvent());
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                SongActivity.super.onBackPressed();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        albumImageView.startAnimation(animationSet);
    }

    private void initializeViews(){
        albumImageView = findViewById(R.id.front_album_cover_view);
        appBar = findViewById(R.id.appbar);
        playButton = findViewById(R.id.pause_play_button_song_view);
        nextButton = findViewById(R.id.next_song_button_song_view);
        previousButton = findViewById(R.id.previous_button_song_view);
        seekBar = findViewById(R.id.seekBar);
        songTitleView = findViewById(R.id.title_text_view_song_activity);
        songArtistView = findViewById(R.id.subtitle_text_view_song_activity);
        progressCounterView = findViewById(R.id.progress_counter);
        songDurationView = findViewById(R.id.song_duration_view_song_activity);
    }

    private void initializeClasses(){
        converter = new Converter();
        windowUtils = new WindowUtils(this);
        animations = new Animations(this);
        colorReader = new ColorReader();
    }

    private void initializeSeekBar(int albumColor, int songDuration){
        seekBar.setMax(songDuration);
        seekBar.setThumbTintList(ColorStateList.valueOf(albumColor));
        seekBar.getProgressDrawable().setColorFilter(albumColor, PorterDuff.Mode.SRC_IN);
    }

    private void bindViews(Intent mainActivityIntent){
        String pathToAlbumCover = mainActivityIntent.getStringExtra("albumImagePath");
        String songName = mainActivityIntent.getStringExtra("songName");
        String songArtist = mainActivityIntent.getStringExtra("songArtist");
        Long songDuration = mainActivityIntent.getLongExtra("songDuration", 0);
        Bitmap albumBitmap = converter.getAlbumCoverFromMusicFile(pathToAlbumCover);
        int albumColor = colorReader.getComplimentedColor(colorReader.getDominantColor(albumBitmap));

        initializeSeekBar(albumColor, songDuration.intValue());
        albumImageView.setImageBitmap(albumBitmap);
        songTitleView.setText(songName);
        songArtistView.setText(songArtist);
        progressCounterView.setText("0:00");
        songDurationView.setText(converter.convertToDuration(songDuration));
    }

    private void registerPlayButtonClickListener(){
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animations.playToPauseAnimation(playButton);
                EventBus.getDefault().post(new ChangeMediaStateEvent());
            }
        });
    }

    private void registerNextButtonClickListener(){
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_NEXT));
                seekBar.setProgress(0);
            }
        });
    }

    private void registerPreviousButtonClickListener(){
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_PREVIOUS));
                seekBar.setProgress(0);
            }
        });
    }

    private void registerSeekBarChangeListener(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                EventBus.getDefault().post(new ChangeMediaPositionEvent(seekBar.getProgress()));
            }
        });
    }

    private void setAllViewsTransparent(){
        appBar.setBackground(getResources().getDrawable(R.color.transparent));
    }

    private void fadeInViews(){
        animations.fadeInAnimation(playButton);
        animations.fadeInAnimation(nextButton);
        animations.fadeInAnimation(previousButton);
        animations.fadeInAnimation(seekBar);
        animations.fadeInAnimation(songTitleView);
        animations.fadeInAnimation(songArtistView);
    }

    private void fadeOutViews(){
        animations.fadeOutAnimation(playButton);
        animations.fadeOutAnimation(nextButton);
        animations.fadeOutAnimation(previousButton);
        animations.fadeOutAnimation(seekBar);
        animations.fadeOutAnimation(songTitleView);
        animations.fadeOutAnimation(songArtistView);
    }

    public void onEvent(SendSongDetailsEvent sendSongDetailsEvent){
        Song newSong = sendSongDetailsEvent.getSong();
        Bitmap albumCover = converter.getAlbumCoverFromMusicFile(newSong.getAlbumCoverPath());
        String songName = newSong.getSongName();
        String songArtist = newSong.getArtist();
        int albumColor = sendSongDetailsEvent.getSongAlbumColor();
        Long songDuration = sendSongDetailsEvent.getDuration();

        initializeSeekBar(albumColor, songDuration.intValue());
        songTitleView.setText(songName);
        songArtistView.setText(songArtist);
        albumImageView.setImageBitmap(albumCover);
        songDurationView.setText(converter.convertToDuration(songDuration));
    }

    public void onEvent(ProgressUpdateEvent progressUpdateEvent){
        int currentPosition = progressUpdateEvent.getCurrentPosition();
        String roundedPositionValue = converter.convertToDuration((long) currentPosition);
        seekBar.setProgress(currentPosition);
        progressCounterView.setText(roundedPositionValue);
    }
}
