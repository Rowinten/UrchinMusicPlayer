package com.example.rowin.urchinmusicplayer.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * Created by Rowin on 3/13/2018.
 */

public class Converter {

    public Converter(){

    }

    public Bitmap getAlbumCoverFromMusicFile(String filePath){
        File image = new File(filePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        return BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
    }

    //MediaStore.Audio.Media.Duration returns value in milliseconds, this function converts to minute:seconds format (example 3:22)
    public String convertToDuration(Long songDuration){
        String secondsBelowZero = "";
        long seconds = songDuration/1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;



        if(seconds < 10){
            secondsBelowZero = "0" + seconds;
            return minutes +":"+ secondsBelowZero;
        }

        return minutes +":"+ seconds;
    }

}
