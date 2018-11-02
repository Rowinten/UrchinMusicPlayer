package com.example.rowin.urchinmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rowin on 2/23/2018.
 */

public class Song implements Parcelable{

    private int id;
    private Long albumId;
    private String songPath;
    private String songName;
    private Long duration;
    private String artist;
    private String albumCoverPath;

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

    private Song(Parcel in) {
        setId(in.readInt());
        setAlbumId(in.readLong());
        setSongPath(in.readString());
        setSongName(in.readString());
        setDuration(in.readLong());
        setArtist(in.readString());
        setAlbumCoverPath(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeLong(albumId);
        parcel.writeString(songPath);
        parcel.writeString(songName);
        parcel.writeLong(duration);
        parcel.writeString(artist);
        parcel.writeString(albumCoverPath);
    }

    public int getId() {
        return id;
    }

    public Long getAlbumId(){ return albumId;}

    public String getSongPath() {
        return songPath;
    }

    public String getSongName() {
        return songName;
    }

    public Long getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbumCoverPath() {
        return albumCoverPath;
    }


    public void setId(int id) {
        this.id = id;
    }

    void setAlbumId(Long albumId){ this.albumId = albumId;}

    void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    void setSongName(String songName) {
        this.songName = songName;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    void setArtist(String artist) {
        this.artist = artist;
    }

    void setAlbumCoverPath(String albumCoverPath) {
        this.albumCoverPath = albumCoverPath;
    }
}
