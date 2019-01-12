package com.example.android.audioplayer;

import java.io.Serializable;
import java.util.List;

public class SongsDTO implements Serializable {

    private List<Song> songs;

    public SongsDTO() {
    }

    public SongsDTO(List<Song> songs) {
        this.songs = songs;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
