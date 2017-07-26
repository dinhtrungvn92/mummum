package com.kenung.vn.prettymusic.music_offline.album;

/**
 * Created by sev_user on 23-Dec-16.
 */
public class Album {
    private long id;
    private String title;
    private String artist;
    private String art_uri;
    private int song_number;

    public Album(long albumId, String albumTitle, String albumArtist, String albumArt_uri, int song_number) {
        id = albumId;
        title = albumTitle;
        artist = albumArtist;
        art_uri = albumArt_uri;
        this.song_number = song_number;
    }

    public String getArt_uri() {
        return art_uri;
    }

    public long getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public int getSong_number() {
        return song_number;
    }

    public void setSong_number(int song_number) {
        this.song_number = song_number;
    }
}