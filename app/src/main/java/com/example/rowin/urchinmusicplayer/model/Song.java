package com.example.rowin.urchinmusicplayer.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rowin on 2/23/2018.
 */

public class Song implements Parcelable{

    private String songName;
    private String duration;
    private String album;
    private String artist;
    private Bitmap songCover;

    public Song(){

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

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
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

    public Bitmap getSongCover() {
        return songCover;
    }

    public void setSongCover(Bitmap songCover) {
        this.songCover = songCover;
    }


    private Song(Parcel in) {
        songName = in.readString();
        duration = in.readString();
        album = in.readString();
        artist = in.readString();
        songCover = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(songName);
        parcel.writeString(duration);
        parcel.writeString(album);
        parcel.writeString(artist);
        parcel.writeParcelable(songCover, i);
    }
}
