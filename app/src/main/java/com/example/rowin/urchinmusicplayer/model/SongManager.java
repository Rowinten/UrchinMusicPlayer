package com.example.rowin.urchinmusicplayer.model;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rowin on 2/22/2018.
 */

public class SongManager {
    private Context context;

    @SuppressLint("UseSparseArrays")
    private HashMap<String, Song> artistSongList = new HashMap<>();

    private ArrayList<Song> listOfSongs = new ArrayList<>();
    private ArrayList<Album> listOfAlbums = new ArrayList<>();

    private ArrayList<Song> albumSongList = new ArrayList<>();

    private Long previousAlbumId = null;

    public SongManager(Context context){
        this.context = context;
    }

    //Retrieve all songs
    public void getSongsFromMusicDirectory() {
        int counter = 0;
        ContentResolver cr = context.getContentResolver();
        Uri musicDirectory = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = cr.query(musicDirectory, null, null, null, null);
        if (cursor == null) {
            Toast.makeText(context, "No media Files present",
                    Toast.LENGTH_SHORT).show();
        }
        while (cursor != null && cursor.moveToNext()) {

            int id = counter;
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

            Song song = createSongObject(id, albumId, artist, songDuration, displayName, albumCoverPath, songPath);

            //Adds new album object to the album list only when a new albumId is spotted.
            if(previousAlbumId == null){
                Album album = createAlbumObject(albumId, artist, songAlbum, albumCoverPath);
                listOfAlbums.add(album);
            }  else if(!previousAlbumId.equals(albumId)){
                Album album = createAlbumObject(albumId, artist, songAlbum, albumCoverPath);
                listOfAlbums.add(album);
            }

            artistSongList.put(artist, song);
            listOfSongs.add(song);

            counter++;
            previousAlbumId = albumId;
            cursorAlbum.close();
        }

        setSongList(listOfSongs);
        setAlbumList(listOfAlbums);

        cursor.close();

    }

    public void setSongList(ArrayList<Song> listOfSongs){
        this.listOfSongs = listOfSongs;
    }

    public ArrayList<Song> getSongList(){
        return listOfSongs;
    }

    public void setAlbumList(ArrayList<Album> listOfAlbums){
        this.listOfAlbums = listOfAlbums;
    }

    public ArrayList<Album> getAlbumList(){
        return listOfAlbums;
    }


    private Album createAlbumObject(Long id, String artist, String songAlbum, String albumCoverPath){
        Album album = new Album();
        album.setId(id);
        album.setName(songAlbum);
        album.setArtist(artist);
        album.setPath(albumCoverPath);

        return album;
    }

    private Song createSongObject(int id, Long albumId, String artist, Long songDuration, String displayName, String albumCoverPath, String songPath){
        Song song = new Song();
        song.setId(id);
        song.setAlbumId(albumId);
        song.setArtist(artist);
        song.setDuration(songDuration);
        song.setSongName(displayName);
        song.setAlbumCoverPath(albumCoverPath);
        song.setSongPath(songPath);
        return song;
    }
}
