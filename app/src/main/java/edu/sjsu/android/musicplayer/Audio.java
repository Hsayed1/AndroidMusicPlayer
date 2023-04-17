package edu.sjsu.android.musicplayer;

import java.io.Serializable;

public class Audio implements Serializable {
    String path;
    String title;
    String length;

    public Audio(String path, String title, String length) {
        this.path = path;
        this.title = title;
        this.length = length;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
}
