package com.example.rowin.urchinmusicplayer.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rowin.urchinmusicplayer.R;

/**
 * Created by Rowin on 2/24/2018.
 */

public class SongListFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.songs_tab_fragment, container, false);
    }
}
