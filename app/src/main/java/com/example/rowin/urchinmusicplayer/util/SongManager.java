package com.example.rowin.urchinmusicplayer.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.example.rowin.urchinmusicplayer.model.Song;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/22/2018.
 */

public class SongManager {
    private Context context;
    private ArrayList<Song> listOfSongs = new ArrayList<>();

    public SongManager(Context context){
        this.context = context;
    }


    //Retrieve all songs
    public ArrayList<Song> getSongList() {
        ContentResolver cr = context.getContentResolver();
        Uri musicDirectory = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = cr.query(musicDirectory, null, null, null, null);
        if (cursor == null) {
            Toast.makeText(context, "No media Files present",
                    Toast.LENGTH_SHORT).show();
        }
        while (cursor != null && cursor.moveToNext()) {

            String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String songAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            Long songDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            Uri songUri = Uri.parse(musicDirectory + "/" + cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));

            //TODO Fix path to each music file so album cover can be extracted
            //Bitmap songCover = getImageCoverFromMusicFile(songUri);

            Song song = new Song();
            song.setAlbum(songAlbum);
            song.setArtist(artist);
            song.setDuration(convertToDuration(songDuration));
            song.setSongName(displayName);

            Log.v("SongName", displayName);
            //song.setSongCover(songCover);

            listOfSongs.add(song);
        }

        assert cursor != null;
        cursor.close();
        return listOfSongs;
    }

    private Bitmap getImageCoverFromMusicFile(Uri uri){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        byte[] rawArt;
        Bitmap art = null;
        BitmapFactory.Options bfo = new BitmapFactory.Options();

        mediaMetadataRetriever.setDataSource(context.getApplicationContext(), uri);
        rawArt = mediaMetadataRetriever.getEmbeddedPicture();

        if(rawArt != null){
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
        }

        return art;
    }

    //MediaStore.Audio.Media.Duration returns value in milliseconds, this function converts to minute:seconds format (example 3:22)
    private String convertToDuration(Long songDuration){
        long seconds = songDuration/1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes +":"+seconds;
    }
}
