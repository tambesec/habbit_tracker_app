package com.example.finalproject.model;

public class SongTestGridView {
    private String title;
    private int imageThumbnail;
    private String songUrl;

    public SongTestGridView() {
    }

    public SongTestGridView(String title, int imageThumbnail, String songUrl) {
        this.title = title;
        this.imageThumbnail = imageThumbnail;
        this.songUrl = songUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImageThumbnail() {
        return imageThumbnail;
    }

    public void setImageThumbnail(int imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }
}
