package com.example.rowin.urchinmusicplayer.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * Created by Rowin on 3/13/2018.
 */

public class PathToBitmapConverter {

    public PathToBitmapConverter(){

    }

    public Bitmap getAlbumCoverFromMusicFile(String filePath){
        File image = new File(filePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        return BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
    }

}
