package com.kenung.vn.prettymusic.music_offline.song;

import java.io.Serializable;

/**
 * Created by sev_user on 23-Dec-16.
 */
public class Song implements Serializable {
    private long id;
    private int albumid;
    private String title;
    private String artist;
    private String art_uri;
    private String quality;
    private String song_path;

    public Song(long songID, int albumID, String songTitle, String songArtist, String songArt_uri) {
        id = songID;
        albumid = albumID;
        title = songTitle;
        artist = songArtist;
        art_uri = songArt_uri;
    }

    public Song(long songID, int albumID, String songTitle, String songArtist, String songArt_uri, String song_path) {
        id = songID;
        albumid = albumID;
        title = songTitle;
        artist = songArtist;
        art_uri = songArt_uri;
        this.song_path = song_path;
    }

    /*public Song(long songID, int albumID, String songTitle, String songArtist, String songArt_uri, String songQuality) {
        id = songID;
        albumid = albumID;
        title = songTitle;
        artist = songArtist;
        art_uri = songArt_uri;
        quality = songQuality;
    }*/

    public String getSong_path() {
        return song_path;
    }

    public String getQuality() {
        return quality;
    }

    public String getArt_uri() {
        return art_uri;
    }

    public long getId() {
        return id;
    }

    public int getAlbumid() {
        return albumid;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", albumid=" + albumid +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", art_uri='" + art_uri + '\'' +
                ", quality='" + quality + '\'' +
                '}' + '\n';
    }
}
