package com.example.rowin.urchinmusicplayer.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.events.ChangeMediaPositionEvent;
import com.example.rowin.urchinmusicplayer.events.ChangeMediaStateEvent;
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
import com.example.rowin.urchinmusicplayer.util.ScrollGestureListener;
import com.example.rowin.urchinmusicplayer.util.WindowUtils;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_IN_ACTIVITY_VIEWS_DURATION;
import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_IN_ALPHA;
import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_OUT_ACTIVITY_VIEWS_DURATION;
import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_OUT_ALPHA;

/**
 * Created by Rowin on 2-4-2018.
 * Song activity class, displaying a single song when tapped on the currently_playing_song_tab
 * Shows the specific song and has handling like next, previous, pause, play, SeekBar, and animations when skipping to next or previous song
 * also has animations when scrolling to next song or previous.
 */

public class SongActivity extends AppCompatActivity {
    private static final int FADE_OUT_BACKGROUND_ON_SCROLL_DURATION = 0;
    private static final int FADE_OUT_BACKGROUND_ON_RELEASE_DURATION = 250;
    private static final int IMAGE_VIEW_SCROLL_DURATION = 0;
    private static final int IMAGE_VIEW_ON_RELEASE_DURATION = 250;
    private static final int RIGHT_SCROLL_THRESHOLD = 90;
    private static final int LEFT_SCROLL_THRESHOLD = -90;

    private ImageView frontAlbumImageView, backAlbumImageView;
    private Toolbar appBar;
    private ImageView playButton, nextButton, previousButton;
    private TextView songTitleView, songArtistView, progressCounterView, songDurationView;
    private SeekBar seekBar;

    private Converter converter;
    private MusicStorage musicStorage;
    private WindowUtils windowUtils;
    private Animations animations;
    private ColorReader colorReader;
    private ConstraintLayout albumImageArea, blurImageViewContainer;
    private ConstraintLayout buttonContainer;
    private ConstraintLayout mainView;
    private ImageView frontBlurImageView;
    private ImageView backBlurImageView;

    private Bitmap oldBlurredAlbumCover;

    private boolean isPlaying;
    private boolean isAlbumBackVisible = false;
    private boolean nextSong = false;
    private boolean setFirstTime = true;
    private boolean backgroundSwitched = false;

    private float fromDegreeButton = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        EventBus.getDefault().register(this);

        initializeViews();
        initializeClasses();
        bindViews(getIntent());

        registerNextButtonClickListener();
        registerPlayButtonClickListener();
        registerPreviousButtonClickListener();
        registerSeekBarChangeListener();
        registerImageClickListener();

        fadeInViews();

        //Checks if layout has been drawn, and therefore we can be able to get the height of the specified view
        albumImageArea.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once
                albumImageArea.getViewTreeObserver().removeOnGlobalLayoutListener(this);


                //set padding after views has been drawn, mainly albumImageArea, otherwise height of albumImageArea is not correct
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                appBar.setPadding(0, windowUtils.getStatusBarHeight(), 0, 0);
                buttonContainer.setPadding(0, 0, 0, windowUtils.getNavigationBarHeight());

                //Calculates the amount of times the imageView, that will be scaled, can fit inside its container. So that for all screen sizes the scaled view will fit
                //inside the albumImageArea view.
                float amountTimesFit = albumImageArea.getHeight() / frontAlbumImageView.getHeight();

                //Scales animation to the centerX of screen, and enlarges the imageView by amountTimesFit times minus a small proportion
                animations.albumImageScaleIncreaseAnimation(frontAlbumImageView, windowUtils.getCenterScreenX(frontAlbumImageView), amountTimesFit - 0.5f);
                animations.albumImageScaleIncreaseAnimation(backAlbumImageView, windowUtils.getCenterScreenX(backAlbumImageView), amountTimesFit - 0.5f);
                blurImageViewContainer.setBackgroundColor(getResources().getColor(R.color.viewPagerTransparency));
            }
        });

        seekBar.setThumb(getResources().getDrawable(R.drawable.song_tab_icon_fill_animation));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        ImageView decreaseView;

        fadeOutViews();

        if(!isAlbumBackVisible){
            decreaseView = frontAlbumImageView;
        } else {
            decreaseView = backAlbumImageView;
        }

        AnimationSet animationSet = animations.albumImageScaleDecreaseAnimationSet(windowUtils.getCenterScreenX(decreaseView));
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
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
        decreaseView.startAnimation(animationSet);
    }

    private void initializeViews() {
        mainView = findViewById(R.id.song_parent_view);
        buttonContainer = findViewById(R.id.button_container);
        blurImageViewContainer = findViewById(R.id.blur_image_view_container);

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
        int currentPositionSong = mainActivityIntent.getIntExtra("currentPositionSong", 0);
        Bitmap albumBitmap = converter.getAlbumCoverFromMusicFile(pathToAlbumCover);
        int albumColor = colorReader.getComplimentedColor(colorReader.getDominantColor(albumBitmap));

        initializeSeekBar(albumColor, songDuration.intValue());
        seekBar.setProgress(currentPositionSong);
        frontAlbumImageView.setImageBitmap(albumBitmap);
        songTitleView.setText(songName);
        songArtistView.setText(songArtist);
        progressCounterView.setText(getResources().getString(R.string.startTimer));
        songDurationView.setText(converter.convertToDuration(songDuration));

        Bitmap blurredBitmap = BlurBitmap.blur(this, albumBitmap);
        oldBlurredAlbumCover = blurredBitmap;
        frontBlurImageView.bringToFront();
        frontBlurImageView.setImageBitmap(blurredBitmap);
    }

    private void registerImageClickListener() {
        albumImageArea.setOnTouchListener(new ScrollingArea());
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
            slideImageOnClick(isAlbumBackVisible, newAlbumCover, -180, fromDegreeButton, 180, 360);
        }
        else {
            slideImageOnClick(isAlbumBackVisible, newAlbumCover, 180, fromDegreeButton, -180, -360);
        }
    }

    /**
     *
     * @param isBackVisible  boolean to check if back is visible or not. if back isn't visible it means that when a new song is selected the front facing imageView
     *                       has to be faded out, and the back facing imageView has to be faded in when starting the slide animation.
     * @param newAlbumCover     the new albumCover that is passed to the method, used to change the album image and the background, as the background is a
     *                          blurred version of the albumCover
     * @param degreeScrolled     the amount of degrees the image has already been scrolled. for example, when the user clicks the previous button the image will slide -180 so that it
     *                                  slides to the left. now when the user clicks the next button for the next song you want to know how much degrees there already has been slided.
     *                                  so that in this example the degree will slide from -180 back to 0 again. same goes the other way around ( 180 to 0, 1 slide = 180 degrees ).
     * @param fromDegree    the degree the imageView has to start sliding from
     * @param toDegreeFrontVisible  the degree the imageView has to start sliding TO when the front is still visible. (when front is visible the starting degree will be be 0,
     *                              and end point will be 180 or -180 depending on direction of sliding.
     * @param toDegreeBackVisible   the degree the image will be sliding TO when back is visible. ( when back is visible the starting degree will be 180 and end point 360,
     *                              or -180 & -360 depending on direction of sliding);
     */
    private void slideImageOnClick(boolean isBackVisible, Bitmap newAlbumCover, int degreeScrolled, float fromDegree, float toDegreeFrontVisible, float toDegreeBackVisible){
        if (!isBackVisible) {
            backBlurImageView.setAlpha(1f);
            backAlbumImageView.setImageBitmap(newAlbumCover);
            
            animations.slideAndFadeImageView(frontAlbumImageView, backAlbumImageView, fromDegree, toDegreeFrontVisible);

            this.fromDegreeButton = toDegreeFrontVisible;
            this.isAlbumBackVisible = true;
        } else {
            frontBlurImageView.setAlpha(1f);
            frontAlbumImageView.setImageBitmap(newAlbumCover);
            
            if(fromDegree == degreeScrolled) {
                animations.slideAndFadeImageView(backAlbumImageView, frontAlbumImageView, fromDegree, 0);
            } else {
                animations.slideAndFadeImageView(backAlbumImageView,frontAlbumImageView, fromDegree, toDegreeBackVisible);
            }

            this.fromDegreeButton = 0;
            this.isAlbumBackVisible = false;
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
                
                changeBackgroundOnClick(nextAlbumCover);

                changeImageAndBackgroundOnButtonPress(nextAlbumCover);
                EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_NEXT));
                seekBar.setProgress(0);

                changePlayButtonState();
            }
        });
    }

    /**
     * used to change the state of the playButton from play to pause. when user starts song the play button has to turn to pause button so user can pause the song
     */
    private void changePlayButtonState(){
        if(!isPlaying){
            animations.playToPauseAnimation(playButton);
            isPlaying = true;
        }
    }

    private void registerPreviousButtonClickListener(){
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong = false;
                ArrayList<Song> listOfSongs = musicStorage.loadAudio();
                Song previousSong = listOfSongs.get(musicStorage.loadAudioIndex() - 1);
                Bitmap previousAlbumCover = converter.getAlbumCoverFromMusicFile(previousSong.getAlbumCoverPath());

                changeBackgroundOnClick(previousAlbumCover);
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

    /**
     * used to fade in particular views, when starting the activity
     */
    private void fadeInViews(){
        animations.fadeAnimation(mainView, FADE_IN_ALPHA, FADE_IN_ACTIVITY_VIEWS_DURATION);
    }

    /**
     * used to fade out particular views, when closing the activity
     */
    private void fadeOutViews(){
        animations.fadeAnimation(mainView, FADE_OUT_ALPHA, FADE_OUT_ACTIVITY_VIEWS_DURATION);
    }


    private void changeBackgroundOnClick(Bitmap newAlbumCover){
        Drawable oldBackground = new BitmapDrawable(getResources(), oldBlurredAlbumCover);

        Bitmap newBlurredBackground = BlurBitmap.blur(this, newAlbumCover);
        Drawable newBackground = new BitmapDrawable(getResources(), newBlurredBackground);

        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = oldBackground;
        backgrounds[1] = newBackground;

        TransitionDrawable crossfader = new TransitionDrawable(backgrounds);

        if(!backgroundSwitched) {
            frontBlurImageView.setImageDrawable(crossfader);
        } else {

            backBlurImageView.setImageDrawable(crossfader);
        }

        crossfader.startTransition(400);
        oldBlurredAlbumCover = newBlurredBackground;
    }


    private void setFromDegree(float fromDegree){
        if(fromDegree >= 360 || fromDegree <= -360){
            fromDegree = 0;
        }
        this.fromDegreeButton = fromDegree;
    }

    public void onEvent(SendSongDetailsEvent sendSongDetailsEvent){
        Song newSong = sendSongDetailsEvent.getSong();
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

    private class ScrollingArea implements View.OnTouchListener {
        private GestureDetector gestureDetector;
        //Starting values of both sides
        private float fromDegreeScrolling = 0;
        private float fromDegreeBackRight = 0;
        private float fromDegreeBackLeft = 0;
        private float fromAlpha;
        private float degreeScrolled;

        private boolean scrollingRight = false;
        private boolean goneOver90Degree = false;
        private boolean pastThreshold = false;
        private boolean beforeThreshold = false;
        private boolean setNewAlbumImage = false;
        private boolean setNewBackground = false;
        private boolean isReleasing = false;
        private boolean hasScrolled = false;
        private boolean lastSongInList = false;

        private Bitmap nextAlbumImageBlurred;
        private Song nextSong;

        private ArrayList<Song> listOfSongs;
        private MusicStorage musicStorage;
        private Animations animations;

        private ScrollingArea(){
            musicStorage = new MusicStorage(SongActivity.this);
            animations = new Animations(SongActivity.this);
            listOfSongs = musicStorage.loadAudio();

            if(setFirstTime) {
                ObjectAnimator fadeOutBack = animations.fadeOutObjectAnimator(backAlbumImageView, 0);
                fadeOutBack.start();
            }

            setFirstTime = false;
        }

        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {
            //Checks if user has released screen

            if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                //checks if the user has been scrolling before releasing the screen. otherwise it plays animations without the right variables being set. like background,
                //and the degrees scrolled
                if(hasScrolled) {
                    onReleaseScreen();
                    hasScrolled = false;
                }
            }

            if(gestureDetector == null){
                gestureDetector = new GestureDetector(SongActivity.this, new ScrollGestureListener(albumImageArea) {
                    /**
                     *
                     * @param toDegreeScrolling the degree the front imageView has to be scrolling to
                     * @param toDegreeBackLeft the degree the back imageView has to be scrolling to when scrolling left
                     * @param toAlpha the transparency level the background has to fade to
                     * @param degreeScrolled the total amount of degrees scrolled
                     */
                    @Override
                    public void onLeftScroll(float toDegreeScrolling, float toDegreeBackLeft, float toAlpha, float degreeScrolled) {
                        if(!isReleasing) {
                            ScrollingArea.this.degreeScrolled = degreeScrolled;

                            scrollingRight = false;
                            hasScrolled = true;

                            int nextIndex = musicStorage.loadAudioIndex() - 1;

                            if (nextIndex < 0 || nextIndex >= listOfSongs.size()) {
                                lastSongInList = true;
                                if(!isAlbumBackVisible){
                                    slideImageView(frontAlbumImageView, backAlbumImageView, fromDegreeScrolling, toDegreeScrolling / 6, IMAGE_VIEW_SCROLL_DURATION, false);
                                } else {
                                    slideImageView(backAlbumImageView, frontAlbumImageView, fromDegreeScrolling, toDegreeScrolling / 6, IMAGE_VIEW_SCROLL_DURATION, false);
                                }
                            } else {
                                lastSongInList = false;
                                if (!setNewAlbumImage) {
                                    nextSong = listOfSongs.get(nextIndex);
                                }

                                if (!isAlbumBackVisible) {
                                    setNewAlbumImage(backAlbumImageView, nextSong);
                                    slideImageView(frontAlbumImageView, backAlbumImageView, fromDegreeScrolling, toDegreeScrolling, IMAGE_VIEW_SCROLL_DURATION, false);
                                    fadeImagesAtThreshold(LEFT_SCROLL_THRESHOLD, frontAlbumImageView, backAlbumImageView);
                                } else {
                                    setNewAlbumImage(frontAlbumImageView, nextSong);
                                    slideImageView(backAlbumImageView, frontAlbumImageView, fromDegreeBackLeft, toDegreeBackLeft, IMAGE_VIEW_SCROLL_DURATION, false);
                                    fadeImagesAtThreshold(LEFT_SCROLL_THRESHOLD, backAlbumImageView, frontAlbumImageView);
                                }

                                if(!backgroundSwitched) {
                                    setNewBackgroundImage(backBlurImageView, nextSong);
                                    fadeBackgroundOnScroll(frontBlurImageView, fromAlpha, toAlpha, FADE_OUT_BACKGROUND_ON_SCROLL_DURATION, false);
                                } else {
                                    setNewBackgroundImage(frontBlurImageView, nextSong);
                                    fadeBackgroundOnScroll(backBlurImageView, fromAlpha, toAlpha, FADE_OUT_BACKGROUND_ON_SCROLL_DURATION, false);
                                }
                            }

                            fromDegreeScrolling = toDegreeScrolling;
                            fromDegreeBackLeft = toDegreeBackLeft;
                            fromAlpha = toAlpha;
                        }
                    }

                    /**
                     *
                     * @param toDegreeScrolling the degree the front imageView has to be scrolling to
                     * @param toDegreeBackRight the degree the back imageView has to be scrolling to when scrolling right
                     * @param toAlpha the transparency level the background has to fade to
                     * @param degreeScrolled the total amount of degrees scrolled
                     */
                    @Override
                    public void onRightScroll(float toDegreeScrolling, float toDegreeBackRight, float toAlpha, float degreeScrolled) {
                        if (!isReleasing) {
                            ScrollingArea.this.degreeScrolled = degreeScrolled;
                            scrollingRight = true;
                            hasScrolled = true;

                            int nextIndex = musicStorage.loadAudioIndex() + 1;

                            if (nextIndex < 0 || nextIndex >= listOfSongs.size()) {
                                lastSongInList = true;
                                if(!isAlbumBackVisible){
                                    slideImageView(frontAlbumImageView, backAlbumImageView, fromDegreeScrolling, toDegreeScrolling / 6, IMAGE_VIEW_SCROLL_DURATION, false);
                                } else {
                                    slideImageView(backAlbumImageView, frontAlbumImageView, fromDegreeScrolling, toDegreeScrolling / 6, IMAGE_VIEW_SCROLL_DURATION, false);
                                }
                            } else {
                                lastSongInList = false;
                                if(!setNewAlbumImage) {
                                    nextSong = listOfSongs.get(nextIndex);
                                }
                                if (!isAlbumBackVisible) {
                                    setNewAlbumImage(backAlbumImageView, nextSong);
                                    slideImageView(frontAlbumImageView, backAlbumImageView, fromDegreeScrolling, toDegreeScrolling, IMAGE_VIEW_SCROLL_DURATION, false);
                                    fadeImagesAtThreshold(RIGHT_SCROLL_THRESHOLD, frontAlbumImageView, backAlbumImageView);
                                } else {
                                    setNewAlbumImage(frontAlbumImageView, nextSong);
                                    slideImageView(backAlbumImageView, frontAlbumImageView, fromDegreeBackRight, toDegreeBackRight, IMAGE_VIEW_SCROLL_DURATION, false);
                                    fadeImagesAtThreshold(RIGHT_SCROLL_THRESHOLD, backAlbumImageView, frontAlbumImageView);
                                }

                                if(!backgroundSwitched) {
                                    setNewBackgroundImage(backBlurImageView, nextSong);
                                    fadeBackgroundOnScroll(frontBlurImageView, fromAlpha, toAlpha, FADE_OUT_BACKGROUND_ON_SCROLL_DURATION, false);
                                } else {
                                    setNewBackgroundImage(frontBlurImageView, nextSong);
                                    fadeBackgroundOnScroll(backBlurImageView, fromAlpha, toAlpha, FADE_OUT_BACKGROUND_ON_SCROLL_DURATION, false);
                                }
                            }


                            fromDegreeScrolling = toDegreeScrolling;
                            fromDegreeBackRight = toDegreeBackRight;
                            fromAlpha = toAlpha;
                        }
                    }
                });
            }
            return gestureDetector.onTouchEvent(motionEvent);
        }


        /**
         * Set of animations to fade front imageView out and back imageView in when degreeScrolled gets over 90 degrees
         * @param frontView view that gets faded out
         * @param backView view that gets faded in
         */
        private void playFadingAnimatorSet(View frontView, View backView){
            ObjectAnimator fadeOutFront = animations.fadeOutObjectAnimator(frontView, 0);
            ObjectAnimator fadeInBack = animations.fadeInObjectAnimator(backView, 0);
            AnimatorSet fadingAnimatorSet = new AnimatorSet();
            fadingAnimatorSet.playSequentially(fadeOutFront, fadeInBack);
            fadingAnimatorSet.start();
        }

        private void onReleaseScreen(){
            //animates only the imageView, nothing else. gets used when user is at last song in the list, so that user cannot scroll to new album image, since there is none.
            //fromDegreeScrolling gets divided by 6 so that user cant scroll enough anymore to scroll past 90 degrees.
            if(lastSongInList){
                if(!isAlbumBackVisible) {
                    slideImageOnRelease(fromDegreeScrolling / 6, 0);
                } else {
                    if(scrollingRight) {
                        slideImageOnRelease(fromDegreeScrolling / 6, 180);
                    } else {
                        slideImageOnRelease(fromDegreeScrolling / 6, -180);
                    }
                }
            } else {
                if (scrollingRight) {
                    if (!isAlbumBackVisible) {
                        slideImageOnRelease(RIGHT_SCROLL_THRESHOLD, fromDegreeScrolling, 0, 180);
                    } else {
                        slideImageOnRelease(RIGHT_SCROLL_THRESHOLD, fromDegreeBackRight, 180, 360);
                    }
                } else {
                    if (!isAlbumBackVisible) {
                        slideImageOnRelease(LEFT_SCROLL_THRESHOLD, fromDegreeScrolling, 0, -180);
                    } else {
                        slideImageOnRelease(LEFT_SCROLL_THRESHOLD, fromDegreeBackLeft, -180, -360);
                    }
                }

                if(!backgroundSwitched){
                    fadeBackgroundOnRelease(frontBlurImageView);
                } else {
                    fadeBackgroundOnRelease(backBlurImageView);
                }

                oldBlurredAlbumCover = nextAlbumImageBlurred;
                goneOver90Degree = false;
            }
        }

        private void slideImageOnRelease(float fromDegree, float toDegree){
            slideImageView(frontAlbumImageView, backAlbumImageView, fromDegree, toDegree, IMAGE_VIEW_ON_RELEASE_DURATION, true);
        }

        /**
         * function to slide imageViews back to starting position when user releases screen and degreeScrolled is lower than 90 or to end position when
         * degreeScrolled is higher than 90. also used to start new or previous song if scrolled past certain threshold.
         * @param threshold threshold user has to scroll past to to fade out images
         * @param fromDegree degree the images have to animate from
         * @param degreeBeforeThreshold the degree the images have to scroll to when the user has scrolled below the threshold
         * @param degreePastThreshold the degree the images have to scroll to when the user has scrolled past the threshold
         */
        private void slideImageOnRelease(int threshold, float fromDegree, float degreeBeforeThreshold, float degreePastThreshold){
            setThresholds(threshold);

            boolean pastThreshold = getPastThreshold();
            boolean beforeThreshold = getBeforeThreshold();

            if (beforeThreshold) {
                slideImageView(frontAlbumImageView, backAlbumImageView, fromDegree, degreeBeforeThreshold, IMAGE_VIEW_ON_RELEASE_DURATION, true);
            } else if(pastThreshold){
                changePlayButtonState();
                isAlbumBackVisible = !isAlbumBackVisible;
                backgroundSwitched = !backgroundSwitched;

                slideImageView(frontAlbumImageView, backAlbumImageView, fromDegree, degreePastThreshold, IMAGE_VIEW_ON_RELEASE_DURATION, true);

                if(threshold == RIGHT_SCROLL_THRESHOLD){
                    setFromDegree(fromDegreeButton + 180);
                    EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_NEXT));
                } else if(threshold == LEFT_SCROLL_THRESHOLD){
                    setFromDegree(fromDegreeButton - 180);
                    EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_PREVIOUS));
                }
            }
        }

        /**
         * animation set used to slide the album imageView when the user is scrolling
         * @param targetFront front facing imageView, visible to the user
         * @param targetBack back facing imageView, not visible to the user
         * @param fromDegree the degree the imageView has to start scrolling from
         * @param toDegree the degree the imageView has to start scrolling to
         * @param duration duration of the animations
         * @param release boolean to check if the user has let go of the screen or not, gets set to true in the onRelease function
         *                if true then set animator listener to the animations that makes sure user cannot scroll during onRelease.
         */
        private void slideImageView(View targetFront, View targetBack, float fromDegree, float toDegree, int duration, boolean release){
            ObjectAnimator flipFrontOut = ObjectAnimator.ofFloat(targetFront, "rotationY", fromDegree, toDegree);
            flipFrontOut.setDuration(duration);

            ObjectAnimator flipBackIn = ObjectAnimator.ofFloat(targetBack, "rotationY", fromDegree, toDegree);
            flipBackIn.setDuration(duration);

            if(release){
                isReleasing = true;
                //remove on touch listener so scrolling is not monitored during animation (If user scrolls during animation and for example, scrolls 60 degrees by that time,
                //the next image springs to 60 degrees instantly. scrolling during that time needs to be disabled to prevent that
                albumImageArea.setOnTouchListener(null);
                flipFrontOut.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        isReleasing = false;
                        albumImageArea.setOnTouchListener(new ScrollingArea());
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }

            flipFrontOut.start();
            flipBackIn.start();
        }

        /**
         * used to fade backgroundView
         * @param fadeOutView background that has to be faded out
         * @param fromAlpha transparency the background is starting from
         * @param toAlpha transparency the background has to fade out, or in, to
         * @param duration duration of the animations
         * @param animationListener boolean that checks if there is need of an animationListener, set true when user releases screen.
         */
        private void fadeBackgroundOnScroll(ImageView fadeOutView, float fromAlpha, float toAlpha, int duration, boolean animationListener){
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(fadeOutView, "alpha", fromAlpha, toAlpha);
            fadeOut.setDuration(duration);

            if(animationListener){
                fadeOut.addListener(animatorListener(fadeOutView));
            }

            fadeOut.start();
        }

        /**
         * animatorListener used when user releases screen, used to listen for the end of the animation so that when animation ends and, for example
         * frontView has faded out, the backView will be put on top and therefore be visible to the user.
         * @param fadeOutView view that is getting faded out
         * @return the animatorListener
         */
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

        /**
         * used to fade background when user releases screen. fades back to 1f if degreeScrolled is lower than 90 and fades to 0f is higher than 90 degrees scrolled
         * revealing the new background underneath.
         * @param fadingView background that will be faded
         */
        private void fadeBackgroundOnRelease(ImageView fadingView){
            boolean pastThreshold = getPastThreshold();
            boolean beforeThreshold = getBeforeThreshold();

            if(beforeThreshold){
                fadeBackgroundOnScroll(fadingView, fromAlpha, 1f, FADE_OUT_BACKGROUND_ON_RELEASE_DURATION, false);
            } else if(pastThreshold){
                fadeBackgroundOnScroll(fadingView, fromAlpha, 0f, FADE_OUT_BACKGROUND_ON_RELEASE_DURATION, true);
                setNewAlbumImage = false;
                setNewBackground = false;
            }
        }

        /**
         * used to fade front facing imageView when user scrolls past the degree threshold, 90.
         * @param threshold threshold the user has to scroll past in order to fade front facing imageView out and back facing imageView in
         * @param fadeOutView view that gets faded out
         * @param fadeInView view that gets faded in
         */
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

        /**
         *  checks the threshold and checks if its past or before it and sets these values
         * @param threshold the threshold the user has to scroll past to to switch images
         */
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

//        /**
//         *
//         * @param albumImageBack the back side of the album imageView, the non visible side
//         * @param previouslyFadedView the previous imageView that was faded out, used to set transparency back to 1f again after it has been faded out.
//         *                            this happens after the new view has faded in and set in front of the previouslyFadedView. otherwise the previouslyFadedView
//         *                            in the back stays transparent
//         * @param song the song which the album picture has to be retrieved
//         */
//        private void setNewImages(ImageView albumImageBack, ImageView previouslyFadedView, Song song){
//            if(!setNewAlbumImage) {
//                previouslyFadedView.setAlpha(1f);
//                changeAlbumPictureBasedOn(albumImageBack, previouslyFadedView, song);
//                setNewAlbumImage = true;
//            }
//        }

        /**
         * used to set the new background on top, when frontView gets faded out the backView has to be on top. otherwise the frontView will still be on top and back will not be shown
         * @param fadingView View that is getting faded out, opposite view has to be put on top to be visible.
         */
        private void setBackgroundOnTop(ImageView fadingView){
            if(fadingView == frontBlurImageView){
                backBlurImageView.bringToFront();
            } else if(fadingView == backBlurImageView) {
                frontBlurImageView.bringToFront();
            }
        }

        private void setNewAlbumImage(ImageView albumImageView, Song song){
            if(!setNewAlbumImage){
                Bitmap albumImage = converter.getAlbumCoverFromMusicFile(song.getAlbumCoverPath());
                albumImageView.setImageBitmap(albumImage);

                setNewAlbumImage = true;
            }
        }

        private void setNewBackgroundImage(ImageView background, Song song){
            if(!setNewBackground){
                background.setAlpha(1f);
                Bitmap albumImage = converter.getAlbumCoverFromMusicFile(song.getAlbumCoverPath());
                nextAlbumImageBlurred = BlurBitmap.blur(SongActivity.this, albumImage);
                background.setImageBitmap(nextAlbumImageBlurred);

                setNewBackground = true;
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
