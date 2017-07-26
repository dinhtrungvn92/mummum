package com.kenung.vn.prettymusic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by sev_user on 6/9/2017.
 */
public class Define {
    public static String Q_LOSSLESS = "Lossless";
    public static String Q_500KBPS = "500kbps";
    public static String Q_320KBPS = "320kbps";
    public static String Q_256KBPS = "256kbps";
    public static String Q_192KBPS = "192kbps";
    public static String Q_128KBPS = "128kbps";



    public static String P_TAG_SONGSPLAYLIT = "songplaylist";
    public static String P_TAG_MESSAGE = "message";
    public static String P_TAG_LISTPLAYLIST = "listPlaylist";
    public static String P_TAG_SUCCESS = "success";
    public static String P_method_post = "POST";
    public static String P_method_get = "GET";
    public static String P_AddPlayListLink = "http://fantasymusicplayer.esy.es/AddPlaylist.php";
    public static String P_playlist_id = "playlist_id";
    public static String P_playlist_name = "playlist_name";
    public static String P_song_number = "song_number";
    public static String P_view_count = "view_count";
    public static String P_UpdateVIewCount = "http://fantasymusicplayer.esy.es/UpdateViewCount.php";

    public static String P_AddSongListLink = "http://fantasymusicplayer.esy.es/AddSongList.php";
    public static String P_artist = "artist";
    public static String P_title = "title";
    public static String P_url = "url";
    public static String P_art_url = "art_url";
    public static String P_quality = "quality";
    public static String P_duration = "duration";

    public static String P_GetAllPlaylistLink = "http://fantasymusicplayer.esy.es/GetAllPlaylist.php";
    public static String P_GetAllSongPlaylistLink = "http://fantasymusicplayer.esy.es/GetAllSongPlaylist.php";


    public static String GetPlayListID(String playlist_name) {
        String playlist_id = "";
        playlist_id = playlist_name.replaceAll(" ", "");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("asia/ho_chi_minh"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
        String localTime = date.format(currentLocalTime);
        playlist_id = playlist_id + localTime;
        playlist_id = playlist_id.replaceAll(" ", "");
        playlist_id = playlist_id.replaceAll("-", "");
        playlist_id = playlist_id.replaceAll(":", "");
        return playlist_id;
    }
}
