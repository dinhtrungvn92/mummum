package com.kenung.vn.prettymusic;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.kenung.vn.prettymusic.module.JSONParser;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sev_user on 6/14/2017.
 */
public class ServerRequest {
    public static JSONParser jsonParser = new JSONParser();

    public static class GetSongsFromPlaylist extends AsyncTask<String, String, String> {
        int success;
        String message = "";
        String playlist_id;
        Context context;
        String action;
        PlaylistOnlineModel addPlaylist;

        public GetSongsFromPlaylist(String playlist_id, Context context, String action, PlaylistOnlineModel addPlaylist) {
            this.playlist_id = playlist_id;
            this.context = context;
            this.action = action;
            this.addPlaylist = addPlaylist;
        }

        @Override
        protected String doInBackground(String... strings) {
            JSONArray jsonArray = null;
            ArrayList<Track> songsPlaylistShare = new ArrayList<>();
            List<NameValuePair> param = new ArrayList<>();
            param.add(new BasicNameValuePair(Define.P_playlist_id, playlist_id));
            JSONObject jsonObject = jsonParser.makeHttpRequest(Define.P_GetAllSongPlaylistLink, Define.P_method_get, param);
            if (jsonObject != null) {
                try {
                    success = jsonObject.getInt(Define.P_TAG_SUCCESS);
                    if (success == 1) {
                        jsonArray = jsonObject.getJSONArray(Define.P_TAG_SONGSPLAYLIT);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String artist = object.getString(Define.P_artist);
                            String title = object.getString(Define.P_title);
                            String url = object.getString(Define.P_url);
                            String art_url = object.getString(Define.P_art_url);
                            String quality = object.getString(Define.P_quality);
                            String duration = object.getString(Define.P_duration);
                            Track track = new Track(artist, title, url, art_url, quality, duration);
                            songsPlaylistShare.add(track);
                            Log.d("playlistOnlineShare", track.toString());
                        }
                        if (action.equals("view")) {
                            MusicResource.listSongPlaylistShare = new ArrayList<>(songsPlaylistShare);
                        } else {
                            MusicResource.hashMapPlaylistOnline.put(addPlaylist.getTitle(), songsPlaylistShare);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (success == 1) {
                if (action.equals("view"))
                    context.sendBroadcast(new Intent("OnclickPlaylistShareItem"));
                else {
                    MusicResource.playlistOnline.add(addPlaylist);
                    context.sendBroadcast(new Intent("AddToPlayListSuccess"));
                }
            } else Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    // ++ view count
    public static class UpdateViewCount extends AsyncTask<String, String, String> {

        int success;
        String message = "";
        Context context;
        String playlist_id;

        public UpdateViewCount(String playlist_id, Context context) {
            this.playlist_id = playlist_id;
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            JSONParser jsonParser = new JSONParser();
            List<NameValuePair> param = new ArrayList<>();
            param.add(new BasicNameValuePair(Define.P_playlist_id, playlist_id));
            JSONObject jsonObject = jsonParser.makeHttpRequest(Define.P_UpdateVIewCount, Define.P_method_post, param);
            if (jsonObject != null)
                try {
                    success = jsonObject.getInt(Define.P_TAG_SUCCESS);
                    message = jsonObject.getString(Define.P_TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public static class GetAllPlayList extends AsyncTask<String, String, String> {
        int success;
        String message = "Co loi xay ra, vui long kiem tra lai mang";

        Context context;

        public GetAllPlayList(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            JSONArray jsonArray = null;
            MusicResource.playlistOnlineShare = new ArrayList<>();
            List<NameValuePair> param = new ArrayList<>();
            JSONObject jsonObject = jsonParser.makeHttpRequest(Define.P_GetAllPlaylistLink, Define.P_method_get, param);
            if (jsonObject != null)
                try {
                    success = jsonObject.getInt(Define.P_TAG_SUCCESS);
                    if (success == 1) {
                        jsonArray = jsonObject.getJSONArray(Define.P_TAG_LISTPLAYLIST);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String playlist_name = object.getString(Define.P_playlist_name);
                            int song_number = object.getInt(Define.P_song_number);
                            String playlist_id = object.getString(Define.P_playlist_id);
                            int view_count = object.getInt(Define.P_view_count);
                            PlaylistOnlineModel playlistOnlineModel = new PlaylistOnlineModel(playlist_name, song_number, playlist_id, view_count);
                            MusicResource.playlistOnlineShare.add(playlistOnlineModel);
                            Log.d("playlistOnlineShare", playlistOnlineModel.toString());
                        }
                    }
                } catch (Exception e) {

                }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (success == 1) {
            } else Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            context.sendBroadcast(new Intent("GetAllPlayListDone"));
        }
    }

    public static class AddPlayList extends AsyncTask<String, String, String> {
        String playlist_name;
        String playlist_id;
        int song_number;
        Context context;
        int success;
        String message = "Co loi xay ra, vui long kiem tra lai mang";

        public AddPlayList(String playlist_id, String playlist_name, int song_number, Context context) {
            this.playlist_name = playlist_name;
            this.playlist_id = playlist_id;
            this.song_number = song_number;
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            JSONParser jsonParser = new JSONParser();
            List<NameValuePair> param = new ArrayList<>();
            param.add(new BasicNameValuePair(Define.P_playlist_id, playlist_id));
            param.add(new BasicNameValuePair(Define.P_playlist_name, playlist_name));
            param.add(new BasicNameValuePair(Define.P_song_number, song_number + ""));
            JSONObject jsonObject = jsonParser.makeHttpRequest(Define.P_AddPlayListLink, Define.P_method_post, param);
            if (jsonObject != null)
                try {
                    success = jsonObject.getInt(Define.P_TAG_SUCCESS);
                    message = jsonObject.getString(Define.P_TAG_MESSAGE);
//                Log.d("shareplaylist", jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (success == 1) {
            } else Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
//            new GetAllPlayList(context).execute();
//            new GetSongsFromPlaylist("anhnhoem15062017153216", context).execute();
        }
    }

    public static class AddListSong extends AsyncTask<String, String, String> {
        ArrayList<Track> listTrack;
        String playlist_id;
        private String artist = "";
        private String title = "";
        private String url = "";
        private String art_url = "";
        private String quality = "";
        private String duration = "";
        int success;
        String message = "Co loi xay ra, vui long kiem tra lai mang";
        Context context;

        public AddListSong(ArrayList<Track> listTrack, String playlist_id, Context context) {
            this.listTrack = listTrack;
            this.playlist_id = playlist_id;
            this.context = context;
            convert();
        }

        @Override
        protected String doInBackground(String... strings) {
            JSONParser jsonParser1 = new JSONParser();
            List<NameValuePair> param = new ArrayList<>();
            param.add(new BasicNameValuePair(Define.P_playlist_id, playlist_id));
            param.add(new BasicNameValuePair(Define.P_artist, artist));
            param.add(new BasicNameValuePair(Define.P_title, title));
            param.add(new BasicNameValuePair(Define.P_url, url));
            param.add(new BasicNameValuePair(Define.P_art_url, art_url));
            param.add(new BasicNameValuePair(Define.P_quality, quality));
            param.add(new BasicNameValuePair(Define.P_duration, duration));
            JSONObject jsonObject = jsonParser1.makeHttpRequest(Define.P_AddSongListLink, Define.P_method_post, param);
            if (jsonObject != null)
                try {
                    success = jsonObject.getInt(Define.P_TAG_SUCCESS);
                    message = jsonObject.getString(Define.P_TAG_MESSAGE);
                    Log.d("shareplaylist1", jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (success == 1) {
                context.sendBroadcast(new Intent("SharePlaylistDone"));
                new ServerRequest.GetAllPlayList(context).execute();
            } else Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        public void convert() {
            for (int i = 0; i < listTrack.size() - 1; i++) {
                artist += listTrack.get(i).getArtist() + "kenung";
                title += listTrack.get(i).getTitle() + "kenung";
                url += listTrack.get(i).getUrl() + "kenung";
                art_url += listTrack.get(i).getArt_url() + "kenung";
                quality += listTrack.get(i).getQuality() + "kenung";
                duration += listTrack.get(i).getDuration() + "kenung";
            }
            artist += listTrack.get(listTrack.size() - 1).getArtist();
            title += listTrack.get(listTrack.size() - 1).getTitle();
            url += listTrack.get(listTrack.size() - 1).getUrl();
            art_url += listTrack.get(listTrack.size() - 1).getArt_url();
            quality += listTrack.get(listTrack.size() - 1).getQuality();
            duration += listTrack.get(listTrack.size() - 1).getDuration();
            //http://fantasymusicplayer.esy.es/AddSongList.php?playlist_id=abc&artist=nullChi%20D%C3%A2nkenungH%C3%A0%20Anh%20Tu%E1%BA%A5nkenungTr%E1%BB%8Bnh%20Th%C4%83ng%20B%C3%ACnhkenung&title=null1%202%203%204kenungNg%C6%B0%E1%BB%9Di%20T%C3%ACnh%20M%C3%B9a%20%C4%90%C3%B4ng%20(SEE%20SING%20SHARE%202)kenungSeenkenung&url=nullhttp://chiasenhac.vn/nhac-hot/1-2-3-4~chi-dan~tsvdcqv0qmve2n.htmlkenunghttp://chiasenhac.vn/nhac-hot/nguoi-tinh-mua-dong-see-sing-share-2~ha-anh-tuan~tsvdc073qmvntw.htmlkenunghttp://chiasenhac.vn/nhac-hot/seen~trinh-thang-binh~tsvdc06zqmvnk1.htmlkenung&art_url=nullnullkenungnullkenungnullkenung&quality=nullLosslesskenung128kbpskenungLosslesskenung&duration=null17.131kenung997kenung665kenung
        }
    }

}
