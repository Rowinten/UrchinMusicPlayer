package com.example.rowin.urchinmusicplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;
import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.fragment.AlbumFragment;
import com.example.rowin.urchinmusicplayer.fragment.PlaylistFragment;
import com.example.rowin.urchinmusicplayer.fragment.SongListFragment;

/**
 * Created by Rowin on 2/24/2018.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private int iconColor;

    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        iconColor = context.getResources().getColor(R.color.colorAccent);
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
                //songTabView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_song_tab_icon_focused));

                VectorChildFinder vector = new VectorChildFinder(context, R.drawable.ic_song_tab_icon_unfocused, songTabView);
                VectorDrawableCompat.VFullPath path = vector.findPathByName("song_tab_icon_unfocused_path");
                path.setFillColor(iconColor);
                path.setStrokeColor(iconColor);

//                TextView songTabView = (TextView) tab.getCustomView();
//                songTabView.setTextColor(context.getResources().getColor(R.color.tabTextColorSelected));
//                songTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_song_tab_icon_focused, 0 , 0);
                break;
            case 1:
                ImageView albumTabView = (ImageView) tab.getCustomView();
                //songTabView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_song_tab_icon_focused));

                VectorChildFinder vector1 = new VectorChildFinder(context, R.drawable.ic_album_tab_icon_unfocused, albumTabView);
                VectorDrawableCompat.VFullPath path1 = vector1.findPathByName("album_tab_icon_unfocused_path");
                path1.setFillColor(iconColor);
                path1.setStrokeColor(iconColor);

//                TextView albumTabView = (TextView) tab.getCustomView();
//                albumTabView.setTextColor(context.getResources().getColor(R.color.tabTextColorSelected));
//                albumTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_album_tab_icon_focused, 0 , 0);
                break;
            case 2:
                ImageView playlistTabView = (ImageView) tab.getCustomView();
                //songTabView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_song_tab_icon_focused));

                VectorChildFinder vector2 = new VectorChildFinder(context, R.drawable.ic_playlist_tab_icon_unfocused, playlistTabView);
                VectorDrawableCompat.VFullPath path2 = vector2.findPathByName("playlist_tab_icon_unfocused_left_path");
                path2.setFillColor(iconColor);
                path2.setStrokeColor(iconColor);

                VectorDrawableCompat.VFullPath path3 = vector2.findPathByName("playlist_tab_icon_unfocused_right_path");
                path3.setFillColor(iconColor);
                path3.setStrokeColor(iconColor);

//                TextView playlistTabView = (TextView) tab.getCustomView();
//                playlistTabView.setTextColor(context.getResources().getColor(R.color.tabTextColorSelected));
//                playlistTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_playlist_tab_icon_focused, 0 , 0);
                break;
        }
    }


    //Calls the fragments to fill the tablayout
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
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

    public void setIconColor(int color){
        this.iconColor = color;
    }
}
