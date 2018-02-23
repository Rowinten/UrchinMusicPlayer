package com.example.rowin.urchinmusicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
    private Cursor cursor;

    public AudioManager(Context context){
        this.context = context;
    }


    //Retrive all songs
    public void Loadmusic() {
        // TODO Auto-generated method stub
        ContentResolver cr = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        cursor = cr.query(uri, null, null, null, null);
        if (cursor == null) {
            Toast.makeText(context, "No media Files present",
                    Toast.LENGTH_SHORT).show();
        }
        while (cursor.moveToNext()) {


            String Displayname = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));

            String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            Log.v("id", id);
            Log.v("name", Displayname);
            //itemusic.add(id + "\t" + Displayname + "");
        }


        cursor.close();

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
