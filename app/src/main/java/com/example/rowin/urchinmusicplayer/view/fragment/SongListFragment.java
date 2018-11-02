package com.example.rowin.urchinmusicplayer.view.fragment;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.controller.MainActivity;
import com.example.rowin.urchinmusicplayer.model.event.PlaySongEvent;
import com.example.rowin.urchinmusicplayer.view.adapter.SongRecyclerViewAdapter;
import com.example.rowin.urchinmusicplayer.model.event.SendSongDetailsEvent;
import com.example.rowin.urchinmusicplayer.model.event.ShuffleEvent;
import com.example.rowin.urchinmusicplayer.model.MusicStorage;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Animations;
import com.example.rowin.urchinmusicplayer.view.listener.HidingScrollListener;
import com.example.rowin.urchinmusicplayer.util.SortingOptionDialogMenu;
import com.example.rowin.urchinmusicplayer.util.WindowUtils;

import java.util.ArrayList;
import java.util.Objects;

import de.greenrobot.event.EventBus;

/**
 * Created by Rowin on 2/24/2018.
 */

//TODO RecyclerView overlaps with the currently_playing_tab_view
public class SongListFragment extends Fragment{
    public SongRecyclerViewAdapter recyclerViewAdapter;

    private RecyclerView songListRecyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private AppCompatImageButton fab;

    private MusicStorage musicStorage;
    private WindowUtils windowUtils;
    private Animations animations;

    private boolean shuffleAnimated = false;

    public SongListFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.songs_tab_fragment, container, false);
        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initializeViews(view);
        initializeClasses();
        initializeRecyclerView();
        registerShuffleButtonClickListener();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }


    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    private int getColorFromBundle(){
        Bundle bundle = getArguments();
        return Objects.requireNonNull(bundle).getInt("colorAccent");
    }

    private void initializeViews(View view){
        songListRecyclerView = view.findViewById(R.id.songRecyclerView);
        fab = view.findViewById(R.id.fab_shuffle_button);
    }

    private void initializeClasses(){
        musicStorage = new MusicStorage(getContext());
        windowUtils = new WindowUtils(getContext());
        animations = new Animations(getContext());
    }

    private void initializeRecyclerView(){
        ArrayList<Song> listOfSongs = Objects.requireNonNull(getArguments()).getParcelableArrayList("listOfSongs");
        SongRecyclerViewAdapter recyclerViewAdapter = getRecyclerViewAdapter(listOfSongs);

        songListRecyclerView.setAdapter(recyclerViewAdapter);
        layoutManager = new LinearLayoutManager(getActivity());
        songListRecyclerView.setLayoutManager(layoutManager);
        songListRecyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                int margin = 32;
                int totalHeightFab = fab.getHeight() +margin;
                fab.animate().translationY(totalHeightFab).setInterpolator(new AccelerateInterpolator(2));
            }

            @Override
            public void onShow() {
                fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
            }
        });

        //Checks if a song has been played in the last session of the user, and styles the recyclerView based on that song (color from album picture and scroll
        //to the position of that song)
        Integer lastPlayedSongIndex = musicStorage.getLastPlayedSongIndex(listOfSongs);
        if(lastPlayedSongIndex != null) {
            recyclerViewAdapter.setTextColor(getColorFromBundle());
            recyclerViewAdapter.setSelected(lastPlayedSongIndex);
            fab.setBackgroundTintList(ColorStateList.valueOf(getColorFromBundle()));
            songListRecyclerView.scrollToPosition(lastPlayedSongIndex +1);
        }
    }

    private void registerShuffleButtonClickListener(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new ShuffleEvent());

                if(!shuffleAnimated){
                    animations.shuffleToCancelAnimation(fab);
                    shuffleAnimated = true;
                } else {
                    animations.cancelToShuffleAnimation(fab);
                    shuffleAnimated = false;
                }
            }
        });
    }

    private SongRecyclerViewAdapter getRecyclerViewAdapter(final ArrayList<Song> listOfSongs){
        recyclerViewAdapter =  new SongRecyclerViewAdapter(Objects.requireNonNull(getContext()), songListRecyclerView, listOfSongs, new SongRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, Song song) {
                EventBus.getDefault().post(new PlaySongEvent(position));
                windowUtils.hideSoftKeyboard(Objects.requireNonNull(getActivity()).getCurrentFocus());
            }
        }, new SongRecyclerViewAdapter.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(int viewHeight) {
                MainActivity mainActivity = ((MainActivity) getActivity());
                int currentPlayingTabHeight = Objects.requireNonNull(mainActivity).currentlyPlayingTab.getHeight();
                int positionY = windowUtils.getNavigationBarHeight()  + currentPlayingTabHeight + viewHeight;
                final Dialog filterOptionDialog = new SortingOptionDialogMenu(getContext(), positionY, listOfSongs, new SortingOptionDialogMenu.OnSortListener() {
                    @Override
                    public void onSort(ArrayList<Song> filteredList, String filterType) {
                        recyclerViewAdapter.changeDataSet(filteredList);
                        recyclerViewAdapter.reinitializeTextWatcher(filterType);
                    }
                });

                if(filterOptionDialog.isShowing()) {
                    filterOptionDialog.dismiss();
                } else {
                    filterOptionDialog.show();
                    }
            }
        });

        return recyclerViewAdapter;
    }


    private void changeSelectedTab(int position, int color){
        if(!recyclerViewAdapter.isSelected(position)){
            recyclerViewAdapter.setTextColor(color);
            recyclerViewAdapter.setSelected(position);
        }
    }

    //EventBus onEventListener, listens for an event send by EventBus with Event type SendSongDetailsEvent. Says it is not used, but it will be
    public void onEvent(SendSongDetailsEvent sendSongDetailsEvent){
        int newPosition = sendSongDetailsEvent.getSongIndex();
        int color = sendSongDetailsEvent.getSongAlbumColor();

        changeSelectedTab(newPosition, color);
        fab.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}
