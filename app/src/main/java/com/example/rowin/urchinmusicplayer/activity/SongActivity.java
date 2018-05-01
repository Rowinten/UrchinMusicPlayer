package com.example.rowin.urchinmusicplayer.activity;

import android.animation.Animator;
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
import com.example.rowin.urchinmusicplayer.util.BlurBitmap;
import com.example.rowin.urchinmusicplayer.util.ColorReader;
import com.example.rowin.urchinmusicplayer.util.Converter;
import com.example.rowin.urchinmusicplayer.util.FlingGestureListener;
import com.example.rowin.urchinmusicplayer.util.WindowUtils;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_IN_ACTIVITY_VIEWS_DURATION;
import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_IN_ALPHA;
import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_OUT_ACTIVITY_VIEWS_DURATION;
import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_OUT_ALPHA;

/**
 * Created by Rowin on 2-4-2018.
 */

public class SongActivity extends AppCompatActivity {
    private static final int FADE_OUT_BACKGROUND_ON_SCROLL_DURATION = 0;
    private static final int FADE_OUT_BACKGROUND_ON_RELEASE_DURATION = 400;
    private static final int IMAGE_VIEW_SCROLL_DURATION = 0;
    private static final int IMAGE_VIEW_ON_RELEASE_DURATION = 400;
    private static final int RIGHT_SCROLL_THRESHOLD = 90;
    private static final int LEFT_SCROLL_THRESHOLD = -90;

    private ImageView frontAlbumImageView, backAlbumImageView;
    private AppBarLayout appBar;
    private ImageView playButton, nextButton, previousButton;
    private TextView songTitleView, songArtistView, progressCounterView, songDurationView;
    private SeekBar seekBar;

    private Converter converter;
    private MusicStorage musicStorage;
    private WindowUtils windowUtils;
    private Animations animations;
    private ColorReader colorReader;
    private ConstraintLayout albumImageArea, blurImgageViewContainer;
    private ImageView frontBlurImageView;
    private ImageView backBlurImageView;

    private Bitmap nextAlbumImage, previousAlbumImage;

    private boolean isPlaying;
    private boolean isBackShowing;
    private boolean isBackVisible = false;
    private boolean nextSong = false;

    private float fromDegree = 0;


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

    private void initializeViews() {
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

        blurImgageViewContainer = findViewById(R.id.blur_image_view_container);
        frontBlurImageView = findViewById(R.id.front_blur_image_view_song_view);
        backBlurImageView = findViewById(R.id.back_blur_image_view_song_view);
    }

    private void initializeClasses() {
        converter = new Converter();
        windowUtils = new WindowUtils(this);
        animations = new Animations(this);
        colorReader = new ColorReader();
        musicStorage = new MusicStorage(this);
    }

    private void initializeSeekBar(int albumColor, int songDuration) {
        seekBar.setMax(songDuration);
        seekBar.setThumbTintList(ColorStateList.valueOf(albumColor));
        seekBar.getProgressDrawable().setColorFilter(albumColor, PorterDuff.Mode.SRC_IN);
    }


    private void bindViews(Intent mainActivityIntent) {
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

        Bitmap blurredBitmap = BlurBitmap.blur(this, albumBitmap);
        frontBlurImageView.bringToFront();
        frontBlurImageView.setImageBitmap(blurredBitmap);
    }

    private void registerImageClickListener() {
        albumImageArea.setOnTouchListener(new ImageViewOnTouchListener());
    }

    /**
     *
     * @param imageView The imageView that will display the blurredBitmap
     * @param bitmap    The Bitmap that will be blurred out
     */
    private void setNewBlurredBitmap(ImageView imageView, Bitmap bitmap){
        Bitmap blurredBitmap = BlurBitmap.blur(this, bitmap);
        imageView.setImageBitmap(blurredBitmap);
    }


    /**
     *
     * @param newAlbumCover, the new albumCover that is passed to the method, used to change the album image and the background, as the background is a
     *                       blurred version of the albumCover
     */
    private void changeImageAndBackgroundOnButtonPress(Bitmap newAlbumCover) {
        //Currently_playing_song_tab has a FrameLayout containing back and front side of an ImageView ( actually two ImageViews in FrameLayout ) back shows first in app.
        //when clicked an animation plays that flips over to the opposite ImageView and displays the album cover of the newly clicked song
        //isAlbumBackVisible keeps record of which side is on the visible side.
        if(nextSong) {
            changeImageAndBackground(isBackVisible, newAlbumCover, -180, fromDegree, 180, 360);
        }
        else {
            changeImageAndBackground(isBackVisible, newAlbumCover, 180, fromDegree, -180, -360);
        }
    }

    /**
     *
     * @param isBackVisible  boolean to check if back is visible or not. if back isn't visible it means that when a new song is selected the front facing imageView
     *                       has to be faded out, and the back facing imageView has to be faded in when starting the slide animation.
     * @param newAlbumCover     the new albumCover that is passed to the method, used to change the album image and the background, as the background is a
     *                          blurred version of the albumCover
     * @param degreeAlreadyScrolled     the amount of degrees the image has already been scrolled. for example, when the user clicks the previous button the image will slide -180 so that it
     *                                  slides to the left. now when the user clicks the next button for the next song you want to know how much degrees there already has been slided.
     *                                  so that in this example the degree will slide from -180 back to 0 again. same goes the other way around ( 180 to 0, 1 slide = 180 degrees ).
     * @param fromDegree    the degree the imageView has to start sliding from
     * @param toDegreeFrontVisible  the degree the imageView has to start sliding TO when the front is still visible. (when front is visible the starting degree will be be 0,
     *                              and end point will be 180 or -180 depending on direction of sliding.
     * @param toDegreeBackVisible   the degree the image will be sliding TO when back is visible. ( when back is visible the starting degree will be 180 and end point 360,
     *                              or -180 & -360 depending on direction of sliding);
     */
    private void changeImageAndBackground(boolean isBackVisible, Bitmap newAlbumCover, int degreeAlreadyScrolled, float fromDegree, float toDegreeFrontVisible, float toDegreeBackVisible){
        if (!isBackVisible) {
            backBlurImageView.setAlpha(1f);
            backAlbumImageView.setImageBitmap(newAlbumCover);
            setNewBlurredBitmap(backBlurImageView, newAlbumCover);

            fadeBackground(frontBlurImageView, 1f, 0f, FADE_OUT_BACKGROUND_ON_RELEASE_DURATION, true);
            animations.slideAndFadeImageView(frontAlbumImageView, backAlbumImageView, fromDegree, toDegreeFrontVisible);

            this.fromDegree = toDegreeFrontVisible;
            this.isBackVisible = true;
        } else {
            frontBlurImageView.setAlpha(1f);
            frontAlbumImageView.setImageBitmap(newAlbumCover);
            setNewBlurredBitmap(frontBlurImageView, newAlbumCover);

            fadeBackground(backBlurImageView, 1f, 0f, FADE_OUT_BACKGROUND_ON_RELEASE_DURATION, true);
            if(fromDegree == degreeAlreadyScrolled) {
                animations.slideAndFadeImageView(backAlbumImageView, frontAlbumImageView, fromDegree, 0);
            } else {
                animations.slideAndFadeImageView(backAlbumImageView,frontAlbumImageView, fromDegree, toDegreeBackVisible);
            }

            this.fromDegree = 0;
            this.isBackVisible = false;
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
                ArrayList<Song> listOfSongs = musicStorage.loadAudio();
                Song nextSong = listOfSongs.get(musicStorage.loadAudioIndex() + 1);
                Bitmap nextAlbumCover = converter.getAlbumCoverFromMusicFile(nextSong.getAlbumCoverPath());

                changeImageAndBackgroundOnButtonPress(nextAlbumCover);
                EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_NEXT));
                seekBar.setProgress(0);

                if(!isPlaying){
                    animations.playToPauseAnimation(playButton);
                    isPlaying = true;
                }
            }
        });
    }

    //TODO GETTING MUSIC ALBUM CODE IS USED IN MULTIPLE INSTANCES, DUPLICATE CODE

    private void registerPreviousButtonClickListener(){
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong = false;

                ArrayList<Song> listOfSongs = musicStorage.loadAudio();
                Song previousSong = listOfSongs.get(musicStorage.loadAudioIndex() - 1);
                Bitmap previousAlbumCover = converter.getAlbumCoverFromMusicFile(previousSong.getAlbumCoverPath());

                changeImageAndBackgroundOnButtonPress(previousAlbumCover);
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
        animations.fadeAnimation(playButton, FADE_IN_ALPHA, FADE_IN_ACTIVITY_VIEWS_DURATION);
        animations.fadeAnimation(nextButton, FADE_IN_ALPHA, FADE_IN_ACTIVITY_VIEWS_DURATION);
        animations.fadeAnimation(previousButton, FADE_IN_ALPHA, FADE_IN_ACTIVITY_VIEWS_DURATION);
        animations.fadeAnimation(seekBar, FADE_IN_ALPHA, FADE_IN_ACTIVITY_VIEWS_DURATION);
        animations.fadeAnimation(songTitleView, FADE_IN_ALPHA, FADE_IN_ACTIVITY_VIEWS_DURATION);
        animations.fadeAnimation(songArtistView, FADE_IN_ALPHA, FADE_IN_ACTIVITY_VIEWS_DURATION);

        animations.fadeAnimation(blurImgageViewContainer, FADE_IN_ALPHA, FADE_IN_ACTIVITY_VIEWS_DURATION);
    }

    private void fadeOutViews(){
        animations.fadeAnimation(playButton, FADE_OUT_ALPHA, FADE_OUT_ACTIVITY_VIEWS_DURATION);
        animations.fadeAnimation(nextButton, FADE_OUT_ALPHA, FADE_OUT_ACTIVITY_VIEWS_DURATION);
        animations.fadeAnimation(previousButton, FADE_OUT_ALPHA, FADE_OUT_ACTIVITY_VIEWS_DURATION);
        animations.fadeAnimation(seekBar, FADE_OUT_ALPHA, FADE_OUT_ACTIVITY_VIEWS_DURATION);
        animations.fadeAnimation(songTitleView, FADE_OUT_ALPHA, FADE_OUT_ACTIVITY_VIEWS_DURATION);
        animations.fadeAnimation(songArtistView, FADE_OUT_ALPHA, FADE_OUT_ACTIVITY_VIEWS_DURATION);

        animations.fadeAnimation(blurImgageViewContainer, FADE_OUT_ALPHA, FADE_OUT_ACTIVITY_VIEWS_DURATION);
    }

    private void fadeBackground(ImageView fadeOutView, float fromAlpha, float toAlpha, int duration, boolean onEndAnimationListener){
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(fadeOutView, "alpha", fromAlpha, toAlpha);
        fadeOut.setDuration(duration);

        if(onEndAnimationListener){
            fadeOut.addListener(animatorListener(fadeOutView));
        }

        fadeOut.start();
    }

    private void setBackgroundOnTop(ImageView fadingView){
        if(fadingView == frontBlurImageView){
            backBlurImageView.bringToFront();
        } else if(fadingView == backBlurImageView) {
            frontBlurImageView.bringToFront();
        }
    }

    private Animator.AnimatorListener animatorListener(final ImageView fadeOutView){
        return new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setBackgroundOnTop(fadeOutView);
            }


            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };
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
        float fromDegreeFront = 0;
        float fromDegreeBackRight = 0;
        float fromDegreeBackLeft = 0;

        float fromAlpha;

        private float degreeScrolled;

        private boolean swipeRight = false;
        private boolean goneOver90Degree = false;
        private boolean pastThreshold = false;
        private boolean beforeThreshold = false;
        private boolean setNewImage = false;

        private ArrayList<Song> listOfSongs;
        private MusicStorage musicStorage;
        private Animations animations;

        private ImageViewOnTouchListener(){
            musicStorage = new MusicStorage(SongActivity.this);
            animations = new Animations(SongActivity.this);
            listOfSongs = musicStorage.loadAudio();

            ObjectAnimator fadeOutBack = animations.fadeOutObjectAnimator(backAlbumImageView, 0);
            fadeOutBack.start();
        }

        //TODO als naar nieuwe image slide terwijl hij met onRelease bezig is dan white background
        //TODO ZORGEN DAT SCROLL AND BUTTONPRESS MET ELKAAR OVERKOMEN MET DEGREES DIE AL GESCROLLED ZIJN

        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {
            //Checks if user has released screen
            if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                onReleaseScreen();
            }

            if(gestureDetector == null){
                gestureDetector = new GestureDetector(SongActivity.this, new FlingGestureListener(albumImageArea) {
                    @Override
                    public void onLeftScroll(float toDegreeFront, float toDegreeBackLeft, float toAlpha, float degreeScrolled) {
                        ImageViewOnTouchListener.this.degreeScrolled = degreeScrolled;
                        swipeRight = false;

                        if(!isBackVisible) {
                            setNewImages(backAlbumImageView, backBlurImageView, SWIPE_TO_LEFT);
                            animations.slideImageView(frontAlbumImageView, backAlbumImageView, fromDegreeFront, toDegreeFront, IMAGE_VIEW_SCROLL_DURATION);
                            fadeBackground(frontBlurImageView, fromAlpha, toAlpha, FADE_OUT_BACKGROUND_ON_SCROLL_DURATION, false);
                            fadeImagesAtThreshold(LEFT_SCROLL_THRESHOLD, frontAlbumImageView, backAlbumImageView);
                        } else {
                            setNewImages(frontAlbumImageView, frontBlurImageView, SWIPE_TO_LEFT);
                            animations.slideImageView(backAlbumImageView, frontAlbumImageView, fromDegreeBackLeft, toDegreeBackLeft, IMAGE_VIEW_SCROLL_DURATION);
                            fadeBackground(backBlurImageView, fromAlpha, toAlpha, FADE_OUT_BACKGROUND_ON_SCROLL_DURATION, false);
                            fadeImagesAtThreshold(LEFT_SCROLL_THRESHOLD, backAlbumImageView, frontAlbumImageView);
                        }

                        fromDegreeFront = toDegreeFront;
                        fromDegreeBackLeft = toDegreeBackLeft;
                        fromAlpha = toAlpha;
                    }

                    @Override
                    public void onRightScroll(float toDegreeFront, float toDegreeBackRight, float toAlpha, float degreeScrolled) {
                        ImageViewOnTouchListener.this.degreeScrolled = degreeScrolled;
                        swipeRight = true;

                        if(!isBackVisible) {
                            setNewImages(backAlbumImageView, backBlurImageView, SWIPE_TO_RIGHT);
                            animations.slideImageView(frontAlbumImageView, backAlbumImageView, fromDegreeFront, toDegreeFront, IMAGE_VIEW_SCROLL_DURATION);
                            fadeBackground(frontBlurImageView, fromAlpha, toAlpha, FADE_OUT_BACKGROUND_ON_SCROLL_DURATION, false);
                            fadeImagesAtThreshold(RIGHT_SCROLL_THRESHOLD, frontAlbumImageView, backAlbumImageView);
                        } else {
                            setNewImages(frontAlbumImageView, frontBlurImageView, SWIPE_TO_RIGHT);
                            animations.slideImageView(backAlbumImageView, frontAlbumImageView, fromDegreeBackRight, toDegreeBackRight, IMAGE_VIEW_SCROLL_DURATION);
                            fadeBackground(backBlurImageView, fromAlpha, toAlpha, FADE_OUT_BACKGROUND_ON_SCROLL_DURATION, false);
                            fadeImagesAtThreshold(RIGHT_SCROLL_THRESHOLD, backAlbumImageView, frontAlbumImageView);
                        }

                        fromDegreeFront = toDegreeFront;
                        fromDegreeBackRight = toDegreeBackRight;
                        fromAlpha = toAlpha;
                    }
                });
            }
            return gestureDetector.onTouchEvent(motionEvent);
        }

        private void changeAlbumPictureBasedOn(ImageView imageView, ImageView background, int scrollingDirection){
            Song song;
            if(scrollingDirection == FlingGestureListener.SWIPE_TO_RIGHT) {
                song = listOfSongs.get(musicStorage.loadAudioIndex() + 1);

            } else {
                song = listOfSongs.get(musicStorage.loadAudioIndex() - 1);
            }
            Bitmap nextAlbumImage = converter.getAlbumCoverFromMusicFile(song.getAlbumCoverPath());
            Bitmap blurredAlbumImage = BlurBitmap.blur(SongActivity.this, nextAlbumImage);
            imageView.setImageBitmap(nextAlbumImage);
            background.setImageBitmap(blurredAlbumImage);
        }

        private void playFadingAnimatorSet(View frontView, View backView){
            ObjectAnimator fadeOutFront = animations.fadeOutObjectAnimator(frontView, 0);
            ObjectAnimator fadeInBack = animations.fadeInObjectAnimator(backView, 0);
            AnimatorSet fadingAnimatorSet = new AnimatorSet();
            fadingAnimatorSet.playSequentially(fadeOutFront, fadeInBack);
            fadingAnimatorSet.start();
        }

        private void onReleaseScreen(){
            if(swipeRight) {
                if(!isBackVisible) {
                    slideImageOnRelease(RIGHT_SCROLL_THRESHOLD, fromDegreeFront, 0, 180);
                    fadeBackgroundOnRelease(frontBlurImageView);
                } else {
                    slideImageOnRelease(RIGHT_SCROLL_THRESHOLD, fromDegreeBackRight, 180, 360);
                    fadeBackgroundOnRelease(backBlurImageView);
                }
            } else {
                if(!isBackVisible) {
                    slideImageOnRelease(LEFT_SCROLL_THRESHOLD, fromDegreeFront, 0, -180);
                    fadeBackgroundOnRelease(frontBlurImageView);
                } else {
                    slideImageOnRelease(LEFT_SCROLL_THRESHOLD, fromDegreeBackLeft, -180, -360);
                    fadeBackgroundOnRelease(backBlurImageView);
                }
            }

            goneOver90Degree = false;
        }

        private void slideImageOnRelease(int threshold, float fromDegree, float degreeBeforeThreshold, float degreePastThreshold){
            setThresholds(threshold);

            boolean pastThreshold = getPastThreshold();
            boolean beforeThreshold = getBeforeThreshold();

            if (beforeThreshold) {
                animations.slideImageView(frontAlbumImageView, backAlbumImageView, fromDegree, degreeBeforeThreshold, IMAGE_VIEW_ON_RELEASE_DURATION);
            } else if(pastThreshold){
                isBackVisible = !isBackVisible;
                animations.slideImageView(frontAlbumImageView, backAlbumImageView, fromDegree, degreePastThreshold, IMAGE_VIEW_ON_RELEASE_DURATION);

                if(threshold == RIGHT_SCROLL_THRESHOLD){
                    EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_NEXT));
                } else if(threshold == LEFT_SCROLL_THRESHOLD){
                    EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_PREVIOUS));
                }
            }
        }

        private void fadeBackgroundOnRelease(ImageView fadingView){
            boolean pastThreshold = getPastThreshold();
            boolean beforeThreshold = getBeforeThreshold();

            if(beforeThreshold){
                fadeBackground(fadingView, fromAlpha, 1f, FADE_OUT_BACKGROUND_ON_RELEASE_DURATION, false);
            } else if(pastThreshold){
                fadeBackground(fadingView, fromAlpha, 0f, FADE_OUT_BACKGROUND_ON_RELEASE_DURATION, true);
                setNewImage = false;
            }
        }

        private void fadeImagesAtThreshold(int threshold, View fadeOutView, View fadeInView){
            setThresholds(threshold);

            boolean pastThreshold = getPastThreshold();
            boolean beforeThreshold = getBeforeThreshold();

            //if user scrolls past 90 degrees, the front will be faded out and back will be faded in. if scrolling back from 90 degrees, back will be faded out
            // and front will be faded in again
            if (pastThreshold && !goneOver90Degree) {
                playFadingAnimatorSet(fadeOutView, fadeInView);
                goneOver90Degree = true;
            } else if (beforeThreshold && goneOver90Degree) {
                playFadingAnimatorSet(fadeInView, fadeOutView);
                goneOver90Degree = false;
            }
        }

        private void setThresholds(int threshold){
            if(threshold == 90){
                setPastThreshold(degreeScrolled  >= threshold);
                setBeforeThreshold(beforeThreshold = degreeScrolled < threshold);
            } else if (threshold == -90){
                setPastThreshold(degreeScrolled <= threshold);
                setBeforeThreshold(beforeThreshold = degreeScrolled > threshold);
            }
        }

        private void setPastThreshold(boolean pastThreshold){
            this.pastThreshold = pastThreshold;
        }

        private void setBeforeThreshold(boolean beforeThreshold){
            this.beforeThreshold = beforeThreshold;
        }

        private void setNewImages(ImageView albumImageBack, ImageView previouslyFadedView, int scrollingDirection){
            if(!setNewImage) {
                previouslyFadedView.setAlpha(1f);
                changeAlbumPictureBasedOn(albumImageBack, previouslyFadedView, scrollingDirection);
                setNewImage = true;
            }
        }

        private Boolean getPastThreshold(){
            return pastThreshold;
        }

        private Boolean getBeforeThreshold(){
            return beforeThreshold;
        }

        //        private void changeAlbumPictureOnFling(){
//            if(!isBackShowing){
//                //Checks if nextButton has been pressed so that correct animation is played. (Next button = slide to right, Previous Button = slide to left )
//                if(nextSong){
//                    Song nextSong = listOfSongs.get(musicStorage.loadAudioIndex() + 1);
//                    Bitmap nextSongAlbumCover = converter.getAlbumCoverFromMusicFile(nextSong.getAlbumCoverPath());
//                    backAlbumImageView.setImageBitmap(nextSongAlbumCover);
//
//                    animations.slideRightAnimation(backAlbumImageView, frontAlbumImageView);
//                } else {
//                    Song previousSong = listOfSongs.get(musicStorage.loadAudioIndex() - 1);
//                    Bitmap nextSongAlbumCover = converter.getAlbumCoverFromMusicFile(previousSong.getAlbumCoverPath());
//                    backAlbumImageView.setImageBitmap(nextSongAlbumCover);
//
//                    animations.slideLeftAnimation(backAlbumImageView, frontAlbumImageView);
//                }
//
//                isBackShowing = true;
//            } else {
//
//                if(nextSong){
//                    Song nextSong = listOfSongs.get(musicStorage.loadAudioIndex() + 1);
//                    Bitmap nextSongAlbumCover = converter.getAlbumCoverFromMusicFile(nextSong.getAlbumCoverPath());
//                    frontAlbumImageView.setImageBitmap(nextSongAlbumCover);
//
//                    animations.slideRightAnimation(frontAlbumImageView, backAlbumImageView);
//                } else {
//                    Song previousSong = listOfSongs.get(musicStorage.loadAudioIndex() - 1);
//                    Bitmap nextSongAlbumCover = converter.getAlbumCoverFromMusicFile(previousSong.getAlbumCoverPath());
//                    frontAlbumImageView.setImageBitmap(nextSongAlbumCover);
//
//                    animations.slideLeftAnimation(frontAlbumImageView, backAlbumImageView);
//                }
//                isBackShowing = false;
//            }
//        }
    }

}
