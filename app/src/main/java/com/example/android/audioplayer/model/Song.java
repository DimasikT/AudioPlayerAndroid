package com.example.android.audioplayer.model;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class Song implements Comparable<Song>, Serializable {

    private String path;
    private String name;
    private File file;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(path, song.path) &&
                Objects.equals(name, song.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(path, name);
    }

    @Override
    public int compareTo(Song s) {
        return name.compareTo(s.getName());
    }
}
