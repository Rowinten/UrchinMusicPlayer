package com.example.rowin.urchinmusicplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;
import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.fragment.AlbumFragment;
import com.example.rowin.urchinmusicplayer.fragment.PlaylistFragment;
import com.example.rowin.urchinmusicplayer.fragment.SongListFragment;
import com.example.rowin.urchinmusicplayer.util.ColorReader;

/**
 * Created by Rowin on 2/24/2018.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private int iconColor;

    private int colorAccent;

    private ColorReader colorReader;

    public SectionsPagerAdapter(FragmentManager fm, Context context, int colorAccent) {
        super(fm);
        this.context = context;
        iconColor = context.getResources().getColor(R.color.colorAccent);
        colorReader = new ColorReader();

        this.colorAccent = colorAccent;
    }

    //Creates the layout for all the tabs
    public void createTabIcons(TabLayout tabLayout){
        ImageView songTab = (ImageView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
        songTab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_song_tab_icon_focused));
        tabLayout.getTabAt(0).setCustomView(songTab);

        ImageView albumTab = (ImageView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
        albumTab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_album_tab_icon_unfocused));
        tabLayout.getTabAt(1).setCustomView(albumTab);

        ImageView playListTab = (ImageView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
        playListTab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_playlist_tab_icon_unfocused));
        tabLayout.getTabAt(2).setCustomView(playListTab);

//        TextView songTab = (TextView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
//        songTab.setText(R.string.tab_text_1);
//        songTab.setTextColor(context.getResources().getColor(R.color.tabTextColorSelected));
//        songTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_song_tab_icon_focused, 0, 0);
//        tabLayout.getTabAt(0).setCustomView(songTab);
//
//        TextView albumTab = (TextView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
//        albumTab.setTextColor(context.getResources().getColor(R.color.tabTextColorUnselected));
//        albumTab.setText(R.string.tab_text_2);
//        albumTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_album_tab_icon_unfocused, 0, 0);
//        tabLayout.getTabAt(1).setCustomView(albumTab);
//
//        TextView playlistTab = (TextView) LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null);
//        playlistTab.setTextColor(context.getResources().getColor(R.color.tabTextColorUnselected));
//        playlistTab.setText(R.string.tab_text_3);
//        playlistTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_playlist_tab_icon_unfocused, 0, 0);
//        tabLayout.getTabAt(2).setCustomView(playlistTab);
    }

    //Changes the layout from a tab to unselected layout
    public void changeTabToUnselected(TabLayout.Tab tab, int index){
        switch(index){
            case 0:
                ImageView songTabView = (ImageView) tab.getCustomView();
                songTabView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_song_tab_icon_unfocused));

//                TextView songTabView = (TextView) tab.getCustomView();
//                songTabView.setTextColor(context.getResources().getColor(R.color.tabTextColorUnselected));
//                songTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_song_tab_icon_unfocused, 0 , 0);
                break;
            case 1:
                ImageView albumTabView = (ImageView) tab.getCustomView();
                albumTabView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_album_tab_icon_unfocused));
                break;
            case 2:
                ImageView playlistTabView = (ImageView) tab.getCustomView();
                playlistTabView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_playlist_tab_icon_unfocused));
                break;
        }
    }

    //Changes the layout from a tab to selected layout
    public void changeTabToSelected(TabLayout.Tab tab, int index){
        switch(index){
            case 0:
                ImageView songTabView = (ImageView) tab.getCustomView();
                colorReader.changeVectorColor(context, songTabView, "song_tab_icon_unfocused_path", iconColor);
                break;
            case 1:
                ImageView albumTabView = (ImageView) tab.getCustomView();
                colorReader.changeVectorColor(context, albumTabView, "album_tab_icon_unfocused_path", iconColor);
                break;
            case 2:
                ImageView playlistTabView = (ImageView) tab.getCustomView();
                colorReader.changeVectorColor(context, playlistTabView, "playlist_tab_icon_unfocused_left_path", iconColor);
                colorReader.changeVectorColor(context, playlistTabView, "playlist_tab_icon_unfocused_right_path", iconColor);
                break;
        }
    }


    //Calls the fragments to fill the tablayout
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                SongListFragment songListFragment = new SongListFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("colorAccent", colorAccent);
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
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    public void setIconColor(int color){
        this.iconColor = color;
    }
}
