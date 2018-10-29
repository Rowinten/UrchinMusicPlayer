package com.example.rowin.urchinmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Album implements Parcelable{
    private Long id;
    private String name;
    private String path;
    private String artist;

    public Album(){

    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    private Album(Parcel in) {
        setId(in.readLong());
        setName(in.readString());
        setPath(in.readString());
        setArtist(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(path);
        parcel.writeString(artist);
    }

    public void setId(Long id){ this.id = id; }

    public Long getId(){
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtist(String artist){ this.artist = artist; }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName(){
        return name;
    }

    public String getArtist(){ return artist; }

    public String getPath(){
        return path;
    }
}
