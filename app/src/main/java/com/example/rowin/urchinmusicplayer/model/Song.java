package com.example.rowin.urchinmusicplayer.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Rowin on 2/23/2018.
 */

public class Song implements Parcelable {

    private String songPath;
    private String songName;
    private Long duration;
    private String album;
    private String artist;
    private String albumCoverPath;

    public Song(){

    }

    private Song(Parcel in) {
        songPath = in.readString();
        songName = in.readString();
        duration = in.readLong();
        album = in.readString();
        artist = in.readString();
        albumCoverPath = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(songPath);
        parcel.writeString(songName);
        parcel.writeLong(duration);
        parcel.writeString(album);
        parcel.writeString(artist);
        parcel.writeString(albumCoverPath);
    }

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumCoverPath() {
        return albumCoverPath;
    }

    public void setAlbumCoverPath(String albumCoverPath) {
        this.albumCoverPath = albumCoverPath;
    }
}
