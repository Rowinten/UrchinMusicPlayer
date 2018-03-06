package com.example.rowin.urchinmusicplayer.adapter;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.fragment.AlbumFragment;
import com.example.rowin.urchinmusicplayer.fragment.PlaylistFragment;
import com.example.rowin.urchinmusicplayer.fragment.SongListFragment;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.util.Animations;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Song> listOfSongs;
    private Context context;

    public SectionsPagerAdapter(FragmentManager fm, ArrayList<Song> listOfSongs, Context context) {
        super(fm);
        this.listOfSongs = listOfSongs;
        this.context = context;
    }

    //Creates the layout for all the tabs
    public void createTabIcons(TabLayout tabLayout){
        final TextView songTab = (TextView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
        songTab.setText(R.string.tab_text_1);
        songTab.setTextColor(context.getResources().getColor(R.color.tabSelectedTextColor));
        songTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_song_tab_icon_focused, 0, 0);
        tabLayout.getTabAt(0).setCustomView(songTab);

        TextView albumTab = (TextView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
        albumTab.setTextColor(context.getResources().getColor(R.color.tabUnselectedTextColor));
        albumTab.setText(R.string.tab_text_2);
        albumTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_album_tab_icon_unfocused, 0, 0);
        tabLayout.getTabAt(1).setCustomView(albumTab);

        TextView playlistTab = (TextView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
        playlistTab.setTextColor(context.getResources().getColor(R.color.tabUnselectedTextColor));
        playlistTab.setText(R.string.tab_text_3);
        playlistTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_playlist_tab_icon_unfocused, 0, 0);
        tabLayout.getTabAt(2).setCustomView(playlistTab);
    }

    //Changes the layout from a tab to unselected layout
    public void changeTabToUnselected(TabLayout.Tab tab, int index){
        switch(index){
            case 0:
                TextView songTabView = (TextView) tab.getCustomView();
                songTabView.setTextColor(context.getResources().getColor(R.color.tabUnselectedTextColor));
                songTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_song_tab_icon_unfocused, 0 , 0);
                break;
            case 1:
                TextView albumTabView = (TextView) tab.getCustomView();
                albumTabView.setTextColor(context.getResources().getColor(R.color.tabUnselectedTextColor));
                albumTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_album_tab_icon_unfocused, 0 , 0);
                break;
            case 2:
                TextView playlistTabView = (TextView) tab.getCustomView();
                playlistTabView.setTextColor(context.getResources().getColor(R.color.tabUnselectedTextColor));
                playlistTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_playlist_tab_icon_unfocused, 0 , 0);
                break;
        }
    }

    //Changes the layout from a tab to selected layout
    public void changeTabToSelected(TabLayout.Tab tab, int index){
        switch(index){
            case 0:
                TextView songTabView = (TextView) tab.getCustomView();
                songTabView.setTextColor(context.getResources().getColor(R.color.tabSelectedTextColor));
                songTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_song_tab_icon_focused, 0 , 0);
                break;
            case 1:
                TextView albumTabView = (TextView) tab.getCustomView();
                albumTabView.setTextColor(context.getResources().getColor(R.color.tabSelectedTextColor));
                albumTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_album_tab_icon_focused, 0 , 0);
                break;
            case 2:
                TextView playlistTabView = (TextView) tab.getCustomView();
                playlistTabView.setTextColor(context.getResources().getColor(R.color.tabSelectedTextColor));
                playlistTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_playlist_tab_icon_focused, 0 , 0);
                break;
        }
    }


    //Calls the fragments to fill the tablayout
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
//                SongListFragment songListFragment = new SongListFragment();
//                Bundle bundle = new Bundle();
//                bundle.putParcelableArrayList("listOfSongs", listOfSongs);
//                songListFragment.setArguments(bundle);
                return new SongListFragment();
            case 1:
                return new AlbumFragment();
            case 2:
                return new PlaylistFragment();
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }
}
