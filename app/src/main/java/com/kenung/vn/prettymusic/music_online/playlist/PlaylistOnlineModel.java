package com.kenung.vn.prettymusic.music_online.playlist;

import java.io.Serializable;

/**
 * Created by sev_user on 31-May-17.
 */

public class PlaylistOnlineModel implements Serializable {
    private String title;
    private int size = 0;
    private String art_uri;
    private int mode = 0;
    private String playlist_id;
    private int view_count = 0;

    public PlaylistOnlineModel(String title) {
        this.title = title;
    }

    public PlaylistOnlineModel(String title, int mode) {
        this.title = title;
        this.mode = mode;
    }

    public PlaylistOnlineModel(String title, int size, String playlist_id) {
        this.title = title;
        this.size = size;
        this.playlist_id = playlist_id;
    }
    public PlaylistOnlineModel(String title, int size, String playlist_id, int view_count) {
        this.title = title;
        this.size = size;
        this.playlist_id = playlist_id;
        this.view_count = view_count;
    }

    public int getMode() {
        return mode;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public String getArt_uri() {
        return art_uri;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setArt_uri(String art_uri) {
        this.art_uri = art_uri;
    }

    public String getPlaylist_id() {
        return playlist_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getView_count() {
        return view_count;
    }

    public void setView_count(int view_count) {
        this.view_count = view_count;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setPlaylist_id(String playlist_id) {
        this.playlist_id = playlist_id;
    }

    @Override
    public String toString() {
        return "PlaylistOnlineModel{" +
                "title='" + title + '\'' +
                ", size=" + size +
                ", art_uri='" + art_uri + '\'' +
                ", mode=" + mode +
                ", playlist_id='" + playlist_id + '\'' +
                '}';
    }
}