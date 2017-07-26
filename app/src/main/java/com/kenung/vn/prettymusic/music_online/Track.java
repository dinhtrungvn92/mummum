package com.kenung.vn.prettymusic.music_online;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by sev_user on 14-Feb-17.
 */

public class Track implements Serializable {

    private String artist;
    private String title;
    private String url;
    private String download_url;
    private String art_url;
    private String src;
    private String quality;
    private String duration;
    private String lyric;
    private HashMap<String, String> download_detail;
    private LinkedHashMap<String, String> stream_detail;

    public Track(String title, String artist, String url) {
        this.artist = artist;
        this.title = title;
        this.url = url;
    }

    public Track(String artist, String title, String url, String art_url, String quality, String duration) {
        this.artist = artist;
        this.title = title;
        this.url = url;
        this.art_url = art_url;
        this.quality = quality;
        this.duration = duration;
    }

    public Track(String title, String artist, String url, String download_url) {
        this.artist = artist;
        this.title = title;
        this.url = url;
        this.download_url = download_url;
    }

    public Track(String title, String artist, String url, String duration, String quality) {
        this.artist = artist;
        this.title = title;
        this.url = url;
        this.quality = quality;
        this.duration = duration;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public String getQuality() {
        return quality;
    }

    public String getDuration() {
        return duration;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrc() {
        return this.src;
    }

    public void setArt_url(String art_url) {
        this.art_url = art_url;
    }

    public String getArt_url() {
        return art_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getDownload_url() {
        return download_url;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {

        return url;
    }

    public HashMap<String, String> getDownload_detail() {
        return download_detail;
    }

    public void setDownload_detail(HashMap<String, String> download_detail) {
        this.download_detail = download_detail;
    }

    public LinkedHashMap<String, String> getStream_detail() {
        return stream_detail;
    }

    public void setStream_detail(LinkedHashMap<String, String> stream_detail) {
        this.stream_detail = stream_detail;
    }


    @Override
    public String toString() {
        return "Track{" +
                "artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", download_url='" + download_url + '\'' +
                ", art_url='" + art_url + '\'' +
                ", src='" + src + '\'' +
                ", quality='" + quality + '\'' +
                ", duration='" + duration + '\'' +
                ", lyric='" + lyric + '\'' +
                ", download_detail=" + download_detail +
                '}';
    }
}
