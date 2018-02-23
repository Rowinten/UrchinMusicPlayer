package com.example.rowin.urchinmusicplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AudioManager manager = new AudioManager(this);
        manager.getSongsList();
    }

    private void readMusicFiles(){

    }
}

