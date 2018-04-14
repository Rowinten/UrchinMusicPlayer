package com.example.rowin.urchinmusicplayer.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Animations;
import com.example.rowin.urchinmusicplayer.util.ColorReader;
import com.example.rowin.urchinmusicplayer.util.Converter;
import com.example.rowin.urchinmusicplayer.util.FlingGestureListener;
import com.example.rowin.urchinmusicplayer.util.WindowUtils;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Rowin on 2-4-2018.
 */

public class SongActivity extends AppCompatActivity {
    private ImageView frontAlbumImageView, backAlbumImageView;
    private AppBarLayout appBar;

    private ImageView playButton, nextButton, previousButton;
    private TextView songTitleView, songArtistView, progressCounterView, songDurationView;
    private SeekBar seekBar;

    private Converter converter;
    private WindowUtils windowUtils;
    private Animations animations;
    private ColorReader colorReader;
    private ConstraintLayout albumImageArea;

    private boolean isPlaying;
    private boolean isBackShowing;
    private boolean nextSong = false;

    private float fromDegrees = 0;


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
        registerImageClickListener();


        windowUtils.setWindowMetrics(getWindow(), appBar);
        fadeInViews();
        animations.albumImageScaleIncreaseAnimation(frontAlbumImageView, windowUtils.getCenterScreenX(frontAlbumImageView));
        animations.albumImageScaleIncreaseAnimation(backAlbumImageView, windowUtils.getCenterScreenX(backAlbumImageView));
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
        animations.albumImageScaleDecreaseAnimationSet(windowUtils.getCenterScreenX(backAlbumImageView));
        AnimationSet animationSet = animations.albumImageScaleDecreaseAnimationSet(windowUtils.getCenterScreenX(frontAlbumImageView));
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
        frontAlbumImageView.startAnimation(animationSet);
    }

    private void initializeViews(){
        frontAlbumImageView = findViewById(R.id.front_album_cover_view);
        backAlbumImageView = findViewById(R.id.back_album_cover_view);
        appBar = findViewById(R.id.appbar);
        playButton = findViewById(R.id.pause_play_button_song_view);
        nextButton = findViewById(R.id.next_song_button_song_view);
        previousButton = findViewById(R.id.previous_button_song_view);
        seekBar = findViewById(R.id.seekBar);
        songTitleView = findViewById(R.id.title_text_view_song_activity);
        songArtistView = findViewById(R.id.subtitle_text_view_song_activity);
        progressCounterView = findViewById(R.id.progress_counter);
        songDurationView = findViewById(R.id.song_duration_view_song_activity);
        albumImageArea = findViewById(R.id.album_image_area_view);
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
        frontAlbumImageView.setImageBitmap(albumBitmap);
        songTitleView.setText(songName);
        songArtistView.setText(songArtist);
        progressCounterView.setText("0:00");
        songDurationView.setText(converter.convertToDuration(songDuration));
    }

    private void registerImageClickListener(){
        albumImageArea.setOnTouchListener(new ImageViewOnTouchListener());
    }

    private void changeAlbumCoverPicture(Bitmap newAlbumCover){
        //Currently_playing_song_tab has a FrameLayout containing back and front side of an ImageView ( actually two ImageViews in FrameLayout ) back shows first in app.
        //when clicked an animation plays that flips over to the opposite ImageView and displays the album cover of the newly clicked song
        //isAlbumBackVisible keeps record of which side is on the visible side.
        if(!isBackShowing){
            backAlbumImageView.setImageBitmap(newAlbumCover);

            //Checks if nextButton has been pressed so that correct animation is played. (Next button = slide to right, Previous Button = slide to left )
            if(nextSong){
                animations.slideRightAnimation(backAlbumImageView, frontAlbumImageView);
            } else {
                animations.slideLeftAnimation(backAlbumImageView, frontAlbumImageView);
            }

            isBackShowing = true;
        } else {
            frontAlbumImageView.setImageBitmap(newAlbumCover);

            if(nextSong){
                animations.slideRightAnimation(frontAlbumImageView, backAlbumImageView);
            } else {
                animations.slideLeftAnimation(frontAlbumImageView, backAlbumImageView);
            }
            isBackShowing = false;
        }
    }

    private void registerPlayButtonClickListener(){
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPlaying) {
                    animations.playToPauseAnimation(playButton);
                    isPlaying = true;
                } else {
                    animations.pauseToPlayAnimation(playButton);
                    isPlaying = false;
                }

                EventBus.getDefault().post(new ChangeMediaStateEvent());
            }
        });
    }

    private void registerNextButtonClickListener(){
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong = true;
                EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_NEXT));
                seekBar.setProgress(0);

                if(!isPlaying){
                    animations.playToPauseAnimation(playButton);
                    isPlaying = true;
                }
            }
        });
    }

    private void registerPreviousButtonClickListener(){
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong = false;
                EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_PREVIOUS));
                seekBar.setProgress(0);

                if(!isPlaying){
                    animations.playToPauseAnimation(playButton);
                    isPlaying = true;
                }
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
        //changeAlbumCoverPicture(albumCover);
        songDurationView.setText(converter.convertToDuration(songDuration));
    }

    public void onEvent(ProgressUpdateEvent progressUpdateEvent){
        int currentPosition = progressUpdateEvent.getCurrentPosition();
        String roundedPositionValue = converter.convertToDuration((long) currentPosition);
        seekBar.setProgress(currentPosition);
        progressCounterView.setText(roundedPositionValue);
    }

    private class ImageViewOnTouchListener implements View.OnTouchListener {
        private GestureDetector gestureDetector;
        //Starting values of both sides
        float fromDegreeFrontVisible;
        float fromDegreeBackVisibleRight;
        float fromDegreeBackVisibleLeft;
//        //Values back view when swiping right and left.
//        private float fromDegreeBackViewRight;
//        private float fromDegreeBackViewLeft;
        private float degreeScrolledCounter;

        private boolean swipeRight = false;
        private boolean goneOver90Degree = false;
        private boolean isBackVisible = false;

        private ArrayList<Song> listOfSongs;
        private MusicStorage musicStorage;
        private Animations animations;

        private ImageViewOnTouchListener(){
            musicStorage = new MusicStorage(SongActivity.this);
            animations = new Animations(SongActivity.this);
            listOfSongs = musicStorage.loadAudio();

            ObjectAnimator fadeOutBack = animations.fadeOutObjectAnimator(backAlbumImageView);
            fadeOutBack.start();
        }

        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {
            //Checks if user has released screen
            if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                if(swipeRight) {
                    if(!isBackVisible) {
                        if (degreeScrolledCounter < 90) {
                            slideOnTouchRelease(fromDegreeFrontVisible, 0);
                        } else {
                            isBackVisible = true;
                            slideOnTouchRelease(fromDegreeFrontVisible, 180);
                            EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_NEXT));
                        }
                    } else {
                        if(degreeScrolledCounter < 90){
                            slideOnTouchRelease(fromDegreeBackVisibleRight, 180);
                        } else {
                            isBackVisible = false;
                            slideOnTouchRelease(fromDegreeBackVisibleRight, 360);
                            EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_NEXT));
                        }
                    }
                } else {
                    if(!isBackVisible) {
                        if (degreeScrolledCounter > -90) {
                            slideOnTouchRelease(fromDegreeFrontVisible, 0);
                        } else {
                            isBackVisible = true;
                            slideOnTouchRelease(fromDegreeFrontVisible, -180);
                            EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_PREVIOUS));
                        }
                    } else {
                        if (degreeScrolledCounter > -90) {
                            slideOnTouchRelease(fromDegreeBackVisibleLeft, -180);
                        } else {
                            isBackVisible = false;
                            slideOnTouchRelease(fromDegreeBackVisibleLeft, -360);
                            EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_PREVIOUS));
                        }
                    }
                }

                goneOver90Degree = false;
            }

            if(gestureDetector == null){
                gestureDetector = new GestureDetector(SongActivity.this, new FlingGestureListener(albumImageArea) {
                    @Override
                    public void onRightFling() {
                        view.performClick();
                        nextSong = true;
                        EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_NEXT));
                    }

                    @Override
                    public void onLeftFling() {
                        view.performClick();
                        nextSong = false;
                        EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_PREVIOUS));
                    }

                    @Override
                    public void onScrollAnimation(float degreeScrolled, int scrollingDirection) {
                        //Starting values of both sides

                        //Degree view will be animating to
                        float toDegreeFrontVisible = 0 + degreeScrolled;
                        float toDegreeBackViewRight = 180 + degreeScrolled;
                        float toDegreeBackViewLeft = -180 + degreeScrolled;

                        degreeScrolledCounter = degreeScrolled;
                        swipeRight = scrollingDirection == FlingGestureListener.SWIPE_TO_RIGHT;

                        //If over 90 degrees, front view has to fade out and the back view has to fade in, so that next or previous ( depending on way of scrolling )
                        //gets visible. if user has scrolled past 90 degrees and scrolls back again, the front view has to be visible again.
                        if(swipeRight) {
                            if(!isBackVisible) {
                                slideOnScroll(frontAlbumImageView, backAlbumImageView, fromDegreeFrontVisible, toDegreeFrontVisible);
                                changeAlbumPictureBasedOn(backAlbumImageView, FlingGestureListener.SWIPE_TO_RIGHT);

                                //Swiping Right
                                //if degreeScrolled is 90-180, and back is not visible, so front is visible, fade front out and back in;
                                //Else if degreeScrolled is 90 - 0, and back is not visible, and view has already passed the 90 degree mark
                                //fade out back and fade in front again.
                                if (degreeScrolled >= 90 && !goneOver90Degree) {
                                    playFadingAnimatorSet(frontAlbumImageView, backAlbumImageView);
                                    goneOver90Degree = true;
                                } else if (degreeScrolled < 90 && goneOver90Degree) {
                                    playFadingAnimatorSet(backAlbumImageView, frontAlbumImageView);
                                    goneOver90Degree = false;
                                }
                            } else {
                                slideOnScroll(backAlbumImageView, frontAlbumImageView, fromDegreeBackVisibleRight, toDegreeBackViewRight);
                                changeAlbumPictureBasedOn(frontAlbumImageView, FlingGestureListener.SWIPE_TO_RIGHT);

                                //Swiping Right
                                //If degree is 90-180, and back is visible, so front isn't visible and back is, fade out back and fade in front;
                                //Else if degreeScrolled is 90 - 0, and back is visible, and view has already passed the 90 degree mark
                                //fade out front and fade in back again.
                                if (degreeScrolled >= 90 && !goneOver90Degree) {
                                    playFadingAnimatorSet(backAlbumImageView, frontAlbumImageView);
                                    goneOver90Degree = true;

                                } else if (degreeScrolled < 90 && goneOver90Degree) {
                                    playFadingAnimatorSet(frontAlbumImageView, backAlbumImageView);
                                    goneOver90Degree = false;
                                }
                            }
                        } else {
                            if(!isBackVisible) {
                                slideOnScroll(frontAlbumImageView, backAlbumImageView, fromDegreeFrontVisible, toDegreeFrontVisible);
                                changeAlbumPictureBasedOn(backAlbumImageView, FlingGestureListener.SWIPE_TO_LEFT);

                                //Swiping Left
                                //If DegreeScrolled is -90 to -180, back is not visible and front is, fade out front and fade in back
                                //Else if degreeScrolled is -90 to 0 after passing the -90 degree mark, fade out back and fade in front again.
                                if (degreeScrolled <= -90 && !goneOver90Degree) {
                                    playFadingAnimatorSet(frontAlbumImageView, backAlbumImageView);
                                    goneOver90Degree = true;
                                } else if (degreeScrolled > -90 && goneOver90Degree) {
                                    playFadingAnimatorSet(backAlbumImageView, frontAlbumImageView);
                                    goneOver90Degree = false;
                                }
                            } else {
                                slideOnScroll(backAlbumImageView, frontAlbumImageView, fromDegreeBackVisibleRight, toDegreeBackViewRight);
                                changeAlbumPictureBasedOn(frontAlbumImageView, FlingGestureListener.SWIPE_TO_LEFT);

                                //Swiping Left
                                //If degreeScrolled is -90 to -180, back is visible, front not, fade out back and fade in front
                                //Else if degreeScrolled is -90 to 0 after passing the -90 degree mark, fade out front and fade in back again.
                                if (degreeScrolled <= -90 && !goneOver90Degree) {
                                    playFadingAnimatorSet(backAlbumImageView, frontAlbumImageView);
                                    goneOver90Degree = true;
                                } else if (degreeScrolled > -90 && goneOver90Degree) {
                                    playFadingAnimatorSet(frontAlbumImageView, backAlbumImageView);
                                    goneOver90Degree = false;
                                }
                            }
                        }

                        fromDegreeFrontVisible = toDegreeFrontVisible;
                        fromDegreeBackVisibleRight = toDegreeBackViewRight;
                        fromDegreeBackVisibleLeft = toDegreeBackViewLeft;
                    }
                });
            }
            return gestureDetector.onTouchEvent(motionEvent);
        }

        private void changeAlbumPictureBasedOn(ImageView imageView, int scrollingDirection){
            Song song;
            if(scrollingDirection == FlingGestureListener.SWIPE_TO_RIGHT) {
                song = listOfSongs.get(musicStorage.loadAudioIndex() + 1);
            } else {
                song = listOfSongs.get(musicStorage.loadAudioIndex() - 1);
            }
            Bitmap nextAlbumImage = converter.getAlbumCoverFromMusicFile(song.getAlbumCoverPath());
            imageView.setImageBitmap(nextAlbumImage);
        }

        private void playFadingAnimatorSet(View frontView, View backView){
            ObjectAnimator fadeOutFront = animations.fadeOutObjectAnimator(frontView);
            ObjectAnimator fadeInBack = animations.fadeInObjectAnimator(backView);
            AnimatorSet fadingAnimatorSet = new AnimatorSet();
            fadingAnimatorSet.playSequentially(fadeOutFront, fadeInBack);
            fadingAnimatorSet.start();
        }

        private void slideOnScroll(View targetFront, View targetBack, float fromDegree, float toDegree){
            ObjectAnimator flipFrontOut = ObjectAnimator.ofFloat(targetFront, "rotationY", fromDegree, toDegree);
            flipFrontOut.setDuration(0);
            ObjectAnimator flipBackIn = ObjectAnimator.ofFloat(targetBack, "rotationY", fromDegree, toDegree);
            flipBackIn.setDuration(0);

            AnimatorSet flipAnimatorSet = new AnimatorSet();
            flipAnimatorSet.playSequentially(flipFrontOut, flipBackIn);
            flipAnimatorSet.start();
        }

        private void slideOnTouchRelease(float fromDegree, float toDegree){
            ObjectAnimator flipAnimationFront = animations.slideFrontOut(frontAlbumImageView, fromDegree, toDegree);
            flipAnimationFront.setDuration(300);
            ObjectAnimator flipAnimationBack = animations.slideBackIn(backAlbumImageView, fromDegree, toDegree);
            flipAnimationBack.setDuration(300);

            flipAnimationFront.start();
            flipAnimationBack.start();
        }


    }

}
