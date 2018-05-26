package com.example.rowin.urchinmusicplayer.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.adapter.SectionsPagerAdapter;
import com.example.rowin.urchinmusicplayer.events.ChangeMediaStateEvent;
import com.example.rowin.urchinmusicplayer.events.FadeInActivityEvent;
import com.example.rowin.urchinmusicplayer.events.PlaySongEvent;
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
import com.example.rowin.urchinmusicplayer.util.SongManager;
import com.example.rowin.urchinmusicplayer.util.WindowUtils;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_IN_ACTIVITY_VIEWS_DURATION;
import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_IN_ALPHA;
import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_OUT_ACTIVITY_VIEWS_DURATION;
import static com.example.rowin.urchinmusicplayer.model.Globals.FADE_OUT_ALPHA;


public class MainActivity extends AppCompatActivity {
    public static final String FADE_IN = "com.example.rowin.urchinmusicplayer.FADEIN";
    private static final int FADE_OUT_BACKGROUND_DURATION = 400;
    private static final int FADE_IN_BACKGROUND_DURATION = 400;

    private int currentPositionSong;

    private Boolean serviceBound = false;
    private Boolean isAlbumBackVisible = false;
    private Boolean statePlaying = false;

    public Song lastPlayedSong = null;
    public Integer lastPlayedSongIndex = null;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Converter converter;
    private Animations animations;
    private ServiceConnection serviceConnection;
    private MediaPlayerService playerService;
    private MusicStorage musicStorage;
    private WindowUtils windowUtils;

    public ProgressBar audioProgressBar;
    public ImageView playButton, nextSongButton, previousSongButton, backAlbumCoverView, frontAlbumCoverView;
    public View frontAlbumCoverLayout, backAlbumCoverLayout;
    public TextView songTitleView, songArtistView;
    public RelativeLayout mainView;

    private Toolbar toolbar;
    private ViewPager container;
    private TabLayout tabs;
    public ConstraintLayout currentlyPlayingTab;
    private AppBarLayout appBar;

    private ImageView backgroundImage;
    private Bitmap OldBlurredAlbumCover;
    private TabLayout tabLayout;

    //TODO FIX WARNINGS


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createServiceConnection();
        initializeViews();
        initializeClasses();

        //Requests permission to read external storage for music files
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        windowUtils.setWindowMetrics(getWindow(), toolbar, tabs);


        EventBus.getDefault().register(this);

        playButtonOnClick();
        nextSongButtonOnClick();
        previousButtonOnClick();

        openSongActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(serviceBound){
            unbindService(serviceConnection);
            playerService.stopSelf();
        }

        EventBus.getDefault().unregister(this);
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

    //TODO CLEAN ONREQUESTPERMISSIONRESULT CODE, MESSY

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted and now can proceed

                    SongManager manager = new SongManager(this);
                    ArrayList<Song> listOfSongs = manager.getSongsFromMusicDirectory();
                    musicStorage.storeAudio(listOfSongs);

                    ColorReader colorReader = new ColorReader();
                    lastPlayedSongIndex = musicStorage.getLastPlayedSongIndex();
                    if(lastPlayedSongIndex != null){
                        lastPlayedSong = listOfSongs.get(lastPlayedSongIndex);

                        Bitmap oldAlbumCover = converter.getAlbumCoverFromMusicFile(lastPlayedSong.getAlbumCoverPath());
                        OldBlurredAlbumCover = BlurBitmap.blur(this, oldAlbumCover);
                        backgroundImage.setImageBitmap(OldBlurredAlbumCover);

                        initializeSongTab(lastPlayedSong);
                        playAudio(lastPlayedSongIndex);

                        setAllViewsTransparent();


                        int dominantColor = colorReader.getDominantColor(OldBlurredAlbumCover);
                        int complimentedDominantColor = colorReader.getComplimentedColor(dominantColor);

                        setAdapterForViewPager(complimentedDominantColor);
                        mSectionsPagerAdapter.setIconColor(complimentedDominantColor);
                        mSectionsPagerAdapter.changeTabToSelected(tabLayout.getTabAt(0), 0);
                    } else {
                        setAdapterForViewPager(getResources().getColor(R.color.colorAccent));
                    }

                } else {
                    //TODO Add function to close application if permission is denied
                    // permission denied
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
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

    private void initializeViews(){
        toolbar = findViewById(R.id.toolbar);
        container = findViewById(R.id.container);
        tabs = findViewById(R.id.tabs);
        currentlyPlayingTab = findViewById(R.id.include_play_tab);
        appBar = findViewById(R.id.appbar);
        mainView = findViewById(R.id.main_view);
        backgroundImage = findViewById(R.id.background_image);
        playButton = findViewById(R.id.play_pause_button);
        nextSongButton = findViewById(R.id.next_song_button);
        previousSongButton = findViewById(R.id.previous_song_button);
        songTitleView = findViewById(R.id.song_title_view_currently_playing_tab);
        songArtistView = findViewById(R.id.song_band_name_view_currently_playing_tab);
        frontAlbumCoverLayout = findViewById(R.id.front_album_cover_layout);
        backAlbumCoverLayout = findViewById(R.id.back_album_cover_layout);
        backAlbumCoverView = findViewById(R.id.back_album_cover_view);
        frontAlbumCoverView = findViewById(R.id.front_album_cover_view);
        audioProgressBar = findViewById(R.id.audio_progress_bar);
    }

    private void initializeClasses(){
        animations = new Animations(this);
        converter = new Converter();
        musicStorage = new MusicStorage(this);
        windowUtils = new WindowUtils(this);
    }

    private void initializeSongTab(Song song){
        songTitleView.setText(song.getSongName());
        songArtistView.setText(song.getArtist());
        Bitmap albumCover = converter.getAlbumCoverFromMusicFile(song.getAlbumCoverPath());
        frontAlbumCoverView.setImageBitmap(albumCover);
    }

    private void initializeProgressBar(int duration, int color){
        audioProgressBar.setMax(duration);
        audioProgressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public void playAudio(int index){
        if(!serviceBound){
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            MusicStorage musicStorage = new MusicStorage(this);
            musicStorage.storeAudioIndex(index);

            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            EventBus.getDefault().post(new PlaySongEvent(index));
        }
    }

    private void openSongActivity(){
        currentlyPlayingTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Song> lisOfSongs = musicStorage.loadAudio();
                Song currentSong = lisOfSongs.get(musicStorage.loadAudioIndex());
                Intent songIntent = new Intent(MainActivity.this, SongActivity.class);
                songIntent.putExtra("albumImagePath", currentSong.getAlbumCoverPath());
                songIntent.putExtra("songName", currentSong.getSongName());
                songIntent.putExtra("songArtist", currentSong.getArtist());
                songIntent.putExtra("songDuration", currentSong.getDuration());
                songIntent.putExtra("currentPositionSong", currentPositionSong);
                startActivity(songIntent);
            }
        });
    }

    private void changeAlbumCoverPicture(Bitmap newAlbumCover){
        //Currently_playing_song_tab has a FrameLayout containing back and front side of an ImageView ( actually two ImageViews in FrameLayout ) back shows first in app.
        //when clicked an animation plays that flips over to the opposite ImageView and displays the album cover of the newly clicked song
        //isAlbumBackVisible keeps record of which side is on the visible side.
        if(!isAlbumBackVisible){
            backAlbumCoverView.setImageBitmap(newAlbumCover);
            animations.verticalSlideAnimation(backAlbumCoverLayout, frontAlbumCoverLayout);
            isAlbumBackVisible = true;
        } else {
            frontAlbumCoverView.setImageBitmap(newAlbumCover);
            animations.verticalSlideAnimation(frontAlbumCoverLayout, backAlbumCoverLayout);
            isAlbumBackVisible = false;
        }
    }

    private void nextSongButtonOnClick(){
        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animations.nextSongAnimation(nextSongButton);
                EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_NEXT));
            }
        });
    }

    private void previousButtonOnClick(){
        previousSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animations.nextSongAnimation(previousSongButton);
                EventBus.getDefault().post(new SkipSongEvent(MediaPlayerService.SKIP_TO_PREVIOUS));
            }
        });
    }

    private void playButtonOnClick(){
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If no music is playing and button is clicked, change play animation for play to pause button
                // else play the opposite animation
                if(!statePlaying) {
                    changePlayButtonStateToPause();
                } else {
                    changePlayButtonStateToPlay();
                }

                EventBus.getDefault().post(new ChangeMediaStateEvent());
            }
        });
    }

    private void changePlayButtonStateToPause(){
        animations.playToPauseAnimation(playButton);
        statePlaying = true;
    }

    private void changePlayButtonStateToPlay(){
        animations.pauseToPlayAnimation(playButton);
        statePlaying = false;
    }

    private void setAdapterForViewPager(int colorAccent){
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this, colorAccent);
        ViewPager mViewPager = findViewById(R.id.container);

        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = findViewById(R.id.tabs);
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

    private void setAllViewsTransparent(){
        tabs.setBackground(getResources().getDrawable(R.color.currentlyPlayingTabTransparency));
        currentlyPlayingTab.setBackground(getResources().getDrawable(R.color.currentlyPlayingTabTransparency));
        toolbar.setBackground(getResources().getDrawable(R.drawable.toolbar_gradient_background));

        appBar.setBackground(getResources().getDrawable(R.color.transparent));
        container.setBackground(getResources().getDrawable(R.color.viewPagerTransparency));
    }

    private Animation.AnimationListener setArtistTitleWithAnimation(final Song song){
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                songArtistView.setVisibility(View.GONE);
                songTitleView.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation a = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out_animation);
                songArtistView.setVisibility(View.VISIBLE);
                songArtistView.startAnimation(a);

                songTitleView.setVisibility(View.VISIBLE);
                songTitleView.startAnimation(a);

                songArtistView.setText(song.getArtist());
                songTitleView.setText(song.getSongName());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
    }

    private void setNewSongDescriptionAnimation(Song song){
        Animation fadeInAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in_animation);
        fadeInAnimation.setAnimationListener(setArtistTitleWithAnimation(song));

        songTitleView.startAnimation(fadeInAnimation);
        songArtistView.startAnimation(fadeInAnimation);
    }

    private void changeBackground(Bitmap newAlbumCover){
        Drawable oldBackground = new BitmapDrawable(getResources(), OldBlurredAlbumCover);

        Bitmap newBlurredBackground = BlurBitmap.blur(this, newAlbumCover);
        Drawable newBackground = new BitmapDrawable(getResources(), newBlurredBackground);

        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = oldBackground;
        backgrounds[1] = newBackground;

        TransitionDrawable crossfader = new TransitionDrawable(backgrounds);
        backgroundImage.setImageDrawable(crossfader);
        crossfader.startTransition(400);

        OldBlurredAlbumCover = newBlurredBackground;
    }

    private void changeSelectedTabIconColor(int albumCoverColor){
        int pos = tabLayout.getSelectedTabPosition();
        TabLayout.Tab selectedTab = tabLayout.getTabAt(pos);

        mSectionsPagerAdapter.setIconColor(albumCoverColor);
        mSectionsPagerAdapter.changeTabToSelected(selectedTab, pos);
    }

    public void onEvent(SendSongDetailsEvent sendSongDetailsEvent){
        Song song = sendSongDetailsEvent.getSong();
        String albumCoverPath = song.getAlbumCoverPath();
        Bitmap albumCoverPicture = converter.getAlbumCoverFromMusicFile(albumCoverPath);
        int albumCoverColor = sendSongDetailsEvent.getSongAlbumColor();
        int duration = sendSongDetailsEvent.getDuration().intValue();


        changeAlbumCoverPicture(albumCoverPicture);
        setNewSongDescriptionAnimation(song);
        initializeProgressBar(duration, albumCoverColor);


        if(!statePlaying){
            changePlayButtonStateToPause();
        }

        setAllViewsTransparent();
        changeBackground(albumCoverPicture);
        changeSelectedTabIconColor(albumCoverColor);
    }

    public void onEvent(ProgressUpdateEvent progressUpdateEvent) {
        currentPositionSong = progressUpdateEvent.getCurrentPosition();
        audioProgressBar.setProgress(currentPositionSong);
    }
}

