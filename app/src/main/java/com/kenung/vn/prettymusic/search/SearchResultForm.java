package com.kenung.vn.prettymusic.search;

/**
 * Created by KXPRO on 4/22/2017.
 */

public class SearchResultForm {

    private long id;
    private String title;
    private String artist;
    private String art_uri;
    private long category; // 1 - artist, 2 - track, 3 - song, 4 - artist
    private String header;

    public SearchResultForm(long id, String title, String artist, String art_uri, long category, String header) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.art_uri = art_uri;
        this.category = category;
        this.header = header;
    }

    public long getId() {
        return id;
    }

    public String getArt_uri() {
        return art_uri;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public long getCategory() {
        return category;
    }

    public String getHeader() {
        return header;
    }
}
