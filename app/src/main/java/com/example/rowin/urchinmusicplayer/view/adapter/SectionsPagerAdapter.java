package com.example.rowin.urchinmusicplayer.view.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.model.Album;
import com.example.rowin.urchinmusicplayer.model.Song;
import com.example.rowin.urchinmusicplayer.view.fragment.AlbumFragment;
import com.example.rowin.urchinmusicplayer.view.fragment.PlaylistFragment;
import com.example.rowin.urchinmusicplayer.view.fragment.SongListFragment;
import com.example.rowin.urchinmusicplayer.util.ColorReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Rowin on 2/24/2018.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private int iconColor;

    private int colorAccent;
    private ArrayList<Song> listOfSongs;
    private ArrayList<Album> listOfAlbums;
    private Song currentSong;

    private ColorReader colorReader;

    public SectionsPagerAdapter(FragmentManager fm, Context context, ArrayList<Song> listOfSongs, ArrayList<Album> listOfAlbums, Song currentSong, int colorAccent) {
        super(fm);
        this.context = context;
        iconColor = context.getResources().getColor(R.color.colorAccent);
        colorReader = new ColorReader();

        this.colorAccent = colorAccent;
        this.listOfSongs = listOfSongs;
        this.listOfAlbums = listOfAlbums;
        this.currentSong = currentSong;
    }

    //Creates the layout for all the tabs
    public void createTabIcons(TabLayout tabLayout){
        ImageView songTab = (ImageView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
        songTab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_song_tab_icon_focused));
        Objects.requireNonNull(tabLayout.getTabAt(0)).setCustomView(songTab);

        ImageView albumTab = (ImageView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
        albumTab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_album_tab_icon_unfocused));
        Objects.requireNonNull(tabLayout.getTabAt(1)).setCustomView(albumTab);

        ImageView playListTab = (ImageView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
        playListTab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_playlist_tab_icon_unfocused));
        Objects.requireNonNull(tabLayout.getTabAt(2)).setCustomView(playListTab);
    }

    //Changes the layout from a tab to unselected layout
    public void changeTabToUnselected(TabLayout.Tab tab, int index){
        switch(index){
            case 0:
                ImageView songTabView = (ImageView) tab.getCustomView();
                Objects.requireNonNull(songTabView).setImageDrawable(context.getResources().getDrawable(R.drawable.ic_song_tab_icon_unfocused));
                break;
            case 1:
                ImageView albumTabView = (ImageView) tab.getCustomView();
                Objects.requireNonNull(albumTabView).setImageDrawable(context.getResources().getDrawable(R.drawable.ic_album_tab_icon_unfocused));
                break;
            case 2:
                ImageView playlistTabView = (ImageView) tab.getCustomView();
                Objects.requireNonNull(playlistTabView).setImageDrawable(context.getResources().getDrawable(R.drawable.ic_playlist_tab_icon_unfocused));
                break;
        }
    }

    //Changes the layout from a tab to selected layout
    public void changeTabToSelected(TabLayout.Tab tab, int index){
        switch(index){
            case 0:
                ImageView songTabView = (ImageView) tab.getCustomView();
                colorReader.changeVectorColor(context, songTabView, R.drawable.ic_song_tab_icon_unfocused,"song_tab_icon_unfocused_path", iconColor);
                break;
            case 1:
                ImageView albumTabView = (ImageView) tab.getCustomView();
                colorReader.changeVectorColor(context, albumTabView, R.drawable.ic_album_tab_icon_unfocused,"album_tab_icon_unfocused_path", iconColor);
                break;
            case 2:
                ImageView playlistTabView = (ImageView) tab.getCustomView();
                colorReader.changeVectorColor(context, playlistTabView, R.drawable.ic_playlist_tab_icon_unfocused,"playlist_tab_icon_unfocused_left_path", iconColor);
                colorReader.changeVectorColor(context, playlistTabView, R.drawable.ic_playlist_tab_icon_unfocused,"playlist_tab_icon_unfocused_right_path", iconColor);
                break;
        }
    }


    //Calls the fragments to fill the tablayout
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                SongListFragment songListFragment = new SongListFragment();
                Bundle songBundle = new Bundle();
                songBundle.putInt("colorAccent", colorAccent);
                songBundle.putParcelableArrayList("listOfSongs", listOfSongs);
                songListFragment.setArguments(songBundle);

                return songListFragment;
            case 1:
                AlbumFragment albumFragment = new AlbumFragment();
                Bundle albumBundle = new Bundle();
                albumBundle.putInt("colorAccent", colorAccent);
                //albumBundle.putParcelable("currentSong", currentSong);
                albumBundle.putParcelableArrayList("listOfAlbums", listOfAlbums);
                albumBundle.putParcelableArrayList("listOfSongs", listOfSongs);
                albumFragment.setArguments(albumBundle);

                return albumFragment;
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

    public void setIconColor(int color){
        this.iconColor = color;
    }
}
