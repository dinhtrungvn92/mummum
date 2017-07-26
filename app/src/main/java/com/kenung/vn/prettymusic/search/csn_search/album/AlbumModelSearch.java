package com.kenung.vn.prettymusic.search.csn_search.album;

/**
 * Created by sev_user on 16-May-17.
 */

public class AlbumModelSearch {

    private String title;
    private String artist;
    private String art_src;
    private String url;
    private int size;

    public AlbumModelSearch(String title, String artist, String url, String art_src) {
        this.title = title;
        this.artist= artist;
        this.art_src = art_src;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getArt_src() {
        return art_src;
    }

    public String getUrl() {
        return url;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
