package edu.sjsu.android.musicplayer;

public class Track {
    String title;
    String uri;
    String imageUrl;

    public Track(String title, String uri, String imageUrl) {
        this.title = title;
        this.uri = uri;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

}
