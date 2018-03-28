package com.example.rowin.urchinmusicplayer.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.adapter.SectionsPagerAdapter;
import com.example.rowin.urchinmusicplayer.model.MediaPlayerService;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Animations;
import com.example.rowin.urchinmusicplayer.util.AudioRequests;
import com.example.rowin.urchinmusicplayer.util.ColorReader;
import com.example.rowin.urchinmusicplayer.util.PathToBitmapConverter;
import com.example.rowin.urchinmusicplayer.util.SongManager;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private Boolean serviceBound = false;
    private Boolean isAlbumBackVisible = false;
    private Boolean statePlaying = false;

    public Song lastPlayedSong = null;
    public Integer lastPlayedSongIndex = null;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private PathToBitmapConverter pathToBitmapConverter;
    private AudioRequests audioRequests;
    private Animations animations;
    private ServiceConnection serviceConnection;
    private MediaPlayerService playerService;
    private SongBroadCastReceiver songBroadCastReceiver;
    private AudioProgressBroadcastReceiver audioProgressBroadcastReceiver;
    private MusicStorage musicStorage;

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
    private BlurImageView blurImageView;
    private TabLayout tabLayout;


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


        //TODO window code own method;

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        toolbar.setPadding(0, getStatusBarHeight() , 0, 0);
        toolbar.getLayoutParams().height = toolbar.getLayoutParams().height + getStatusBarHeight();
        tabs.setPadding(0,0,0, getNavigationBarHeight());

        registerSongBroadcastReceiver();
        registerAudioProgressBroadcastReceiver();

        playButtonOnClick();
        nextSongButtonOnClick();
        previousButtonOnClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(serviceBound){
            unbindService(serviceConnection);
            playerService.stopSelf();
        }

        unregisterReceiver(songBroadCastReceiver);
        unregisterReceiver(audioProgressBroadcastReceiver);
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
                        Bitmap albumCover = pathToBitmapConverter.getAlbumCoverFromMusicFile(lastPlayedSong.getAlbumCoverPath());
                        initializeSongTab(lastPlayedSong);
                        playAudio(lastPlayedSongIndex);

                        setAllViewsTransparent();
                        blurBackgroundImage(albumCover);

                        int dominantColor = colorReader.getDominantColor(albumCover);
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

    private void initializeViews(){
        toolbar = findViewById(R.id.toolbar);
        container = findViewById(R.id.container);
        tabs = findViewById(R.id.tabs);
        currentlyPlayingTab = findViewById(R.id.include_play_tab);
        appBar = findViewById(R.id.appbar);
        mainView = findViewById(R.id.main_view);

        blurImageView = findViewById(R.id.blur_image_view);

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
        pathToBitmapConverter = new PathToBitmapConverter();
        audioRequests = new AudioRequests(this);
        musicStorage = new MusicStorage(this);
    }

    private void initializeSongTab(Song song){
        songTitleView.setText(song.getSongName());
        songArtistView.setText(song.getArtist());
        Bitmap albumCover = pathToBitmapConverter.getAlbumCoverFromMusicFile(song.getAlbumCoverPath());
        frontAlbumCoverView.setImageBitmap(albumCover);
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

    public void playAudio(int index){
        if(!serviceBound){
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            MusicStorage musicStorage = new MusicStorage(this);
            musicStorage.storeAudioIndex(index);

            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            audioRequests.sendPlayNewSongRequest(index);
        }
    }


    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight(){
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    //Currently_playing_song_tab has a FrameLayout containing back and front side of an ImageView ( actually two ImageViews in FrameLayout ) back shows first in app.
    //when clicked an animation plays that flips over to the opposite ImageView and displays the album cover of the newly clicked song
    //isAlbumBackVisible keeps record of which side is on the visible side.
    private void changeAlbumCoverPicture(Bitmap newAlbumCover){
        if(!isAlbumBackVisible){
            backAlbumCoverView.setImageBitmap(newAlbumCover);
            animations.backToFrontAnimation(backAlbumCoverLayout, frontAlbumCoverLayout);
            isAlbumBackVisible = true;
        } else {
            frontAlbumCoverView.setImageBitmap(newAlbumCover);
            animations.frontToBackAnimation(frontAlbumCoverLayout, backAlbumCoverLayout);
            isAlbumBackVisible = false;
        }
    }

    private void blurBackgroundImage(Bitmap backgroundImage){
        blurImageView.setImageBitmap(backgroundImage);
        blurImageView.setBlur(15);
    }


    private void nextSongButtonOnClick(){
        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animations.nextSongAnimation(nextSongButton);
                audioRequests.sendSkipSongRequest(AudioRequests.SKIP_TO_NEXT);
            }
        });
    }

    private void previousButtonOnClick(){
        previousSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animations.nextSongAnimation(previousSongButton);
                audioRequests.sendSkipSongRequest(AudioRequests.SKIP_TO_PREVIOUS);
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

                audioRequests.sendChangeMediaStateRequest();
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

    private void registerSongBroadcastReceiver(){
        songBroadCastReceiver = new SongBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioRequests.BROADCAST_ACTION);
        registerReceiver(songBroadCastReceiver, intentFilter);
    }

    private void registerAudioProgressBroadcastReceiver(){
        audioProgressBroadcastReceiver = new AudioProgressBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioRequests.AUDIO_PROGRESS);
        registerReceiver(audioProgressBroadcastReceiver, intentFilter);
    }

    class SongBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Song song = intent.getParcelableExtra("song");
            Long songDuration = intent.getLongExtra("songDuration", 0);
            int albumCoverColor = intent.getIntExtra("albumCoverColor", 0);

            String albumCoverPath = song.getAlbumCoverPath();
            Bitmap albumCoverPicture = pathToBitmapConverter.getAlbumCoverFromMusicFile(albumCoverPath);

            changeAlbumCoverPicture(albumCoverPicture);
            initProgressBar(songDuration);
            setNewSongDescriptionAnimation(song);

            if(!statePlaying) {
                changePlayButtonStateToPause();
            }

            setAllViewsTransparent();
            blurBackgroundImage(albumCoverPicture);

            changeSelectedTabIconColor(albumCoverColor);

        }

        private void setNewSongDescriptionAnimation(Song song){
            Animation fadeInAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in_animation);
            fadeInAnimation.setAnimationListener(setArtistTitleWithAnimation(song));

            songTitleView.startAnimation(fadeInAnimation);
            songArtistView.startAnimation(fadeInAnimation);
        }

        private void changeSelectedTabIconColor(int albumCoverColor){
            int pos = tabLayout.getSelectedTabPosition();
            TabLayout.Tab selectedTab = tabLayout.getTabAt(pos);

            mSectionsPagerAdapter.setIconColor(albumCoverColor);
            mSectionsPagerAdapter.changeTabToSelected(selectedTab, pos);
        }

        private void initProgressBar(Long songDuration){
            audioProgressBar.setProgress(0);
            audioProgressBar.setMax((int) (long) songDuration);
        }
    }

    class AudioProgressBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int currentPositionInSong = intent.getIntExtra("currentPosition", 0);
            audioProgressBar.setProgress(currentPositionInSong);
        }
    }
}

