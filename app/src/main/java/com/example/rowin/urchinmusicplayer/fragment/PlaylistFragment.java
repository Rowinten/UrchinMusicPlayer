package com.example.rowin.urchinmusicplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rowin.urchinmusicplayer.R;

/**
 * Created by Rowin on 2/24/2018.
 */

public class PlaylistFragment extends Fragment{

    public PlaylistFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_tab_fragment, container, false);


        return view;
    }
}
