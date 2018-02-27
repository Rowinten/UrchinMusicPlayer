package com.example.rowin.urchinmusicplayer.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.rowin.urchinmusicplayer.fragment.AlbumFragment;
import com.example.rowin.urchinmusicplayer.fragment.PlaylistFragment;
import com.example.rowin.urchinmusicplayer.fragment.SongListFragment;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Song> listOfSongs;

    public SectionsPagerAdapter(FragmentManager fm, ArrayList<Song> listOfSongs) {
        super(fm);
        this.listOfSongs = listOfSongs;
    }

    //Calls the fragments to fill the tablayout
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                SongListFragment songListFragment = new SongListFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("listOfSongs", listOfSongs);
                songListFragment.setArguments(bundle);
                return songListFragment;
            case 1:
                return new AlbumFragment();
            case 2:
                return new PlaylistFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Songs";
            case 1:
                return "Albums";
            case 2:
                return "Playlists";
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
