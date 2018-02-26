package com.example.rowin.urchinmusicplayer.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.rowin.urchinmusicplayer.R;
import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/24/2018.
 */

public class PlaylistFragment extends Fragment{

    public PlaylistFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_tab_fragment, container, false);

        Bundle bundle = this.getArguments();
        if(bundle!=null){
            ArrayList<Song> listOfSongs = bundle.getParcelableArrayList("listOfSongs");
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setImageBitmap(listOfSongs.get(0).getSongCover());
        }

        return view;
    }
}
