package com.example.rowin.urchinmusicplayer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rowin on 2/22/2018.
 */

public class AudioManager {
    private Context context;
    private ArrayList songsList = new ArrayList();

    public AudioManager(Context context){
        this.context = context;
    }


    public ArrayList getSongsList(){
        File musicDirectory = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));

        Log.v(musicDirectory.toString(), "hj");
        if(musicDirectory.listFiles(new FileExtensionFilter()).length > 0){
            for(File file : musicDirectory.listFiles(new FileExtensionFilter())) {
                HashMap song = new HashMap();
                //song.put("songTitle", file.getName());
                Log.v("SongTitle", file.getName());
            }
        }
        return songsList;
    }


    class FileExtensionFilter implements FilenameFilter {

        @Override
        public boolean accept(File directory, String fileName) {
            return (fileName.endsWith(".mp3") || fileName.endsWith(".MP3"));
        }
    }
}
