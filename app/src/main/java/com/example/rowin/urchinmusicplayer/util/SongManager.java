package com.example.rowin.urchinmusicplayer.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
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
    public ArrayList<Song> getSongsFromMusicDirectory() {
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
            Long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String songPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            String albumCoverPath = null;

            Cursor cursorAlbum = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART}, MediaStore.Audio.Albums._ID + "=" + albumId, null, null);

            if(cursorAlbum != null && cursorAlbum.moveToFirst()){
                albumCoverPath = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            }

            listOfSongs.add(createSongObject(songAlbum, artist, songDuration, displayName, albumCoverPath, songPath));
            assert cursorAlbum != null;
            cursorAlbum.close();
        }

        cursor.close();

        return listOfSongs;
    }

    private Song createSongObject(String songAlbum, String artist, Long songDuration, String displayName, String albumCoverPath, String songPath){
        Song song = new Song();
        song.setAlbum(songAlbum);
        song.setArtist(artist);
        song.setDuration(songDuration);
        song.setSongName(displayName);
        song.setAlbumCoverPath(albumCoverPath);
        song.setSongPath(songPath);
        return song;
    }
}
