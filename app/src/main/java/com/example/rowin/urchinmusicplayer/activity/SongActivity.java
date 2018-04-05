package com.example.rowin.urchinmusicplayer.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.events.FadeInActivityEvent;
import com.example.rowin.urchinmusicplayer.events.ProgressUpdateEvent;
import com.example.rowin.urchinmusicplayer.util.Animations;
import com.example.rowin.urchinmusicplayer.util.PathToBitmapConverter;
import com.example.rowin.urchinmusicplayer.util.WindowUtils;

import de.greenrobot.event.EventBus;

/**
 * Created by Rowin on 2-4-2018.
 */

public class SongActivity extends AppCompatActivity {
    private ImageView albumImageView;
    private AppBarLayout appBar;

    private ImageView playButton, nextButton, previousButton;
    private TextView songTitleView, songArtistView;
    private SeekBar seekBar;

    private PathToBitmapConverter pathToBitmapConverter;
    private WindowUtils windowUtils;
    private Animations animations;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        EventBus.getDefault().register(this);

        initializeViews();
        initializeClasses();
        setAllViewsTransparent();
        setAlbumImage(getIntent());
        windowUtils.setWindowMetrics(getWindow(), appBar);

        fadeInViews();
        animations.albumImageScaleIncreaseAnimation(albumImageView, getCenterScreenX());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {

        fadeOutViews();
        AnimationSet animationSet = animations.albumImageScaleDecreaseAnimationSet(getCenterScreenX());
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
    }

    private void initializeClasses(){
        pathToBitmapConverter = new PathToBitmapConverter();
        windowUtils = new WindowUtils(this);
        animations = new Animations(this);
    }

    private float getCenterScreenX(){
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) albumImageView.getLayoutParams();
        float marginLeftImage = windowUtils.convertDpToPx(lp.leftMargin);
        return this.getResources().getDisplayMetrics().widthPixels / 2 - marginLeftImage;
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

    private void setAlbumImage(Intent mainActivityIntent){
        String pathToAlbumCover = mainActivityIntent.getStringExtra("albumImagePath");
        String songName = mainActivityIntent.getStringExtra("songName");
        String songArtist = mainActivityIntent.getStringExtra("songArtist");

        Bitmap albumBitmap = pathToBitmapConverter.getAlbumCoverFromMusicFile(pathToAlbumCover);
        albumImageView.setImageBitmap(albumBitmap);

        songTitleView.setText(songName);
        songArtistView.setText(songArtist);
    }

    public void onEvent(ProgressUpdateEvent progressUpdateEvent){
        int currentPosition = progressUpdateEvent.getCurrentPosition();
        //seekBar.setMax();
    }
}
