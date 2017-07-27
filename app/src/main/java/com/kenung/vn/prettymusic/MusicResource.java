package com.kenung.vn.prettymusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.LruCache;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.kenung.vn.prettymusic.listener.NotificationDismissedReceiver;
import com.kenung.vn.prettymusic.music_offline.album.Album;
import com.kenung.vn.prettymusic.music_offline.folder.FolderModel;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;
import com.kenung.vn.prettymusic.music_online.v_pop.bxh.VPopBXHAdapter;
import com.kenung.vn.prettymusic.search.csn_search.album.AlbumModelSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by sev_user on 11-Jan-17.
 */

public class MusicResource {
    public  static boolean toogleSortByViewCount = false;
    public static boolean updateCountView = true;
    public static boolean isRefreshingSharePlaylist = false;
    public static boolean checkPlaylistShareUse = false;
    public static ArrayList<Track> listSongPlaylistShare = new ArrayList<>();
    public static ArrayList<PlaylistOnlineModel> playlistOnlineShare = new ArrayList<>();
    public static ArrayList<PlaylistOnlineModel> playlistOnlineShareSorttByViewCount = new ArrayList<>();
    public static ArrayList<PlaylistOnlineModel> playlistOnlineShareSorttByDate = new ArrayList<>();
    public static ArrayList<Song> folderSelected = new ArrayList<>();
    public static int folderPositon;
    public static boolean isClickFolder = false;
    public static String folderSelectedTitle;
    public static ArrayList<String> folderPathList = new ArrayList<>();
    public static ArrayList<FolderModel> folderList = new ArrayList<>();
    public static HashMap<String, ArrayList<Song>> hashMapFolderList = new HashMap<>();
    public static PlaylistOnlineModel playlist_deleted;
    public static Track track_playing;
    public static Track track_removed;
    public static Song song_deleted;
    public static Song song_removed;
    public static Song song_playing;
    public static boolean playAtSearchAlbum = false;
    public static String qualityLevel = "128kbps";
    public static ArrayList<Track> songListOnlinePlayList = new ArrayList<>();
    public static int popupFromActivity = 1; // 1 - MainActivity, 2 - PlayActivity, 3 - SearchActivity, 4 - SearchAlbumDetailActivity
    public static final int popupFromMainActivity = 1;
    public static final int popupFromPlayActivity = 2;
    public static final int popupFromSearchActivity = 3;
    public static final int popupFromSearchAlbumDetailActivity = 4;
    public static final int popupFromSearchOfflineActivity = 5;
    public static String playListSelectedTitle;
    public static boolean checkPlaylistUse = false;
    public static boolean checkPlaylistOfflineUse = false;
    public static PlaylistOnlineModel addToPlayListModel;
    public static Track addToPlaylistTrack;
    public static Song addToPlaylistSong;
    public static boolean timerEnable = false;
    public static String timer = "";
    public static int pageOfflinePosition = 0;
    public static ArrayList<Song> songListDownload = new ArrayList<>();
    public static ArrayList<String> listOfPathDownload = new ArrayList<>();
    public static boolean isPlugHeadPhone = false;
    public static boolean isPlayingBeforeUnPlugHeadphone = false;
    public static String folderDownload = "";
    public static HashMap<Integer, VPopBXHAdapter.DataViewHolder> mapsView = new HashMap<>();
    public static String chart;
    public static String new_upload;
    public static String new_download;
    public static String track;
    public static String downloaded;
    public static String playlist;
    public static String folder;
    public static String album;
    public static String singer;
    public static String subTitle;
    public static String subArtist;
    public static Bitmap subArt;
    public static boolean subPlaySongVisible = false;
    public static boolean isPlaying = false;
    public static int Number_Item_Play_Activity = 3;
    public static BitmapTrackWorkerTask task;
    public static int repeat_mode = 0; // 0: repeatAll, 1:repeatOne
    public static boolean shuffle = false;// 0: on
    public static boolean isMainPause = false;
    public static Bitmap noti_art;
    public static String noti_title;
    public static String noti_artist;
    public static String noti_state;
    public static boolean isClickAlbum = false;
    public static boolean isCanChange = true;
    public static int online_offine_play = 0;
    public static boolean checkAlbumOnline = false;
    public static String queryOnline;
    public static boolean is_player_playing = false;
    public static boolean isHandleOnItemClickOnPlayContent = false;
    public static int album_offline_search_toogle = 0;
    public static int album_track_toogle = 0;
    public static Bitmap blurredBitmap;
    public static LruCache<String, Bitmap> mMemoryCache;
    public static boolean track_ready = true;
    public static ArrayList<Album> albumList;
    public static ArrayList<Album> albumListResult;
    public static int online_offline_toogle = 0; // 0-Offline, 1-Online
    public static ArrayList<Song> searchResultList;
    public static int search_offline_state = 0;
    public static ArrayList<Song> songList;
    public static ArrayList<Track> songListOnline;
    public static ArrayList<Track> songListOnlineSearch = new ArrayList<>();
    public static ArrayList<Track> songListOnlinePlay;
    public static ArrayList<Song> albumCollection;
    public static ArrayList<Song> albumCollectionContent;
    public static ArrayList<Song> playlistOfflineSelected;
    public static HashMap<String, ArrayList<Song>> hashMapPlaylistOffline = new HashMap<>();
    public static ArrayList<PlaylistOnlineModel> playlistOffline = new ArrayList<>();
    public static HashMap<String, ArrayList<Track>> hashMapPlaylistOnline = new HashMap<>();
    public static ArrayList<PlaylistOnlineModel> playlistOnline = new ArrayList<>();
    public static ArrayList<PlaylistOnlineModel> playlistOnlineDialog = new ArrayList<>();
    public static int playlistPosition;
    public static ArrayList<Song> albumSelected = new ArrayList<>();
    public static ArrayList<Track> BXH_VN = new ArrayList<>();
    public static ArrayList<Track> BXH_UK = new ArrayList<>();
    public static ArrayList<Track> CSNSearchSongResultTemp = new ArrayList<>();
    public static ArrayList<Track> CSNSearchResult;
    public static ArrayList<Track> CSNSearchSingerResultTemp = new ArrayList<>();
    public static ArrayList<Track> CSNSearchAlbumSelected;
    public static ArrayList<AlbumModelSearch> CSNSearchAlbumResult;
    public static ArrayList<AlbumModelSearch> CSNSearchAlbumResultTemp = new ArrayList<>();
    public static ArrayList<Track> VPop_BXH = new ArrayList<>();
    public static ArrayList<Track> VPop_BXH_Refresh = new ArrayList<>();
    public static ArrayList<Track> VPop_MCS = new ArrayList<>();
    public static ArrayList<Track> VPop_MCS_Refresh = new ArrayList<>();
    public static ArrayList<Track> VPop_MDL = new ArrayList<>();
    public static ArrayList<Track> VPop_MDL_Refresh = new ArrayList<>();
    public static ArrayList<Track> KPop_BXH = new ArrayList<>();
    public static ArrayList<Track> KPop_BXH_Refresh = new ArrayList<>();
    public static ArrayList<Track> KPop_MCS = new ArrayList<>();
    public static ArrayList<Track> KPop_MCS_Refresh = new ArrayList<>();
    public static ArrayList<Track> KPop_MDL = new ArrayList<>();
    public static ArrayList<Track> KPop_MDL_Refresh = new ArrayList<>();
    public static ArrayList<Track> JPop_BXH = new ArrayList<>();
    public static ArrayList<Track> JPop_BXH_Refresh = new ArrayList<>();
    public static ArrayList<Track> JPop_MCS = new ArrayList<>();
    public static ArrayList<Track> JPop_MCS_Refresh = new ArrayList<>();
    public static ArrayList<Track> JPop_MDL = new ArrayList<>();
    public static ArrayList<Track> JPop_MDL_Refresh = new ArrayList<>();
    public static ArrayList<Track> CPop_BXH = new ArrayList<>();
    public static ArrayList<Track> CPop_BXH_Refresh = new ArrayList<>();
    public static ArrayList<Track> CPop_MCS = new ArrayList<>();
    public static ArrayList<Track> CPop_MCS_Refresh = new ArrayList<>();
    public static ArrayList<Track> CPop_MDL = new ArrayList<>();
    public static ArrayList<Track> CPop_MDL_Refresh = new ArrayList<>();
    public static ArrayList<Track> USK_BXH = new ArrayList<>();
    public static ArrayList<Track> USK_BXH_Refresh = new ArrayList<>();
    public static ArrayList<Track> USK_MCS = new ArrayList<>();
    public static ArrayList<Track> USK_MCS_Refresh = new ArrayList<>();
    public static ArrayList<Track> USK_MDL = new ArrayList<>();
    public static ArrayList<Track> USK_MDL_Refresh = new ArrayList<>();
    public static ArrayList<Track> Other_BXH = new ArrayList<>();
    public static ArrayList<Track> Other_BXH_Refresh = new ArrayList<>();
    public static ArrayList<Track> Other_MCS = new ArrayList<>();
    public static ArrayList<Track> Other_MCS_Refresh = new ArrayList<>();
    public static ArrayList<Track> Other_MDL = new ArrayList<>();
    public static ArrayList<Track> Other_MDL_Refresh = new ArrayList<>();
    public static int VPop_MCS_PAGE = 2;
    public static int VPop_MDL_PAGE = 2;
    public static int USK_MCS_PAGE = 2;
    public static int USK_MDL_PAGE = 2;
    public static int KPop_MCS_PAGE = 2;
    public static int KPop_MDL_PAGE = 2;
    public static int JPop_MCS_PAGE = 2;
    public static int JPop_MDL_PAGE = 2;
    public static int CPop_MCS_PAGE = 2;
    public static int CPop_MDL_PAGE = 2;
    public static int Other_MCS_PAGE = 2;
    public static int Other_MDL_PAGE = 2;
    public static int Search_SONG_PAGE = 2;
    public static int Search_SINGER_PAGE = 2;
    public static int Search_ALBUM_PAGE = 2;
    public static int songPosition;
    public static int albumPosition;
    public static float rotationArt = 0.0f;
    public static boolean musicOnlline = false; //music offline
    public static String LOG_TAG = "CSN_MainActivity";
    public static float imagePosition = 0f;
    public static long imageDuration = 20000;
    public static boolean onGoing = true;
    public static int MODE = 0; // 0 - offline, 1 - BXH VN, 2 - BXH UK, 3 - albumCollection, 4 - searchOffline, 5 - CSNSearch
    // Offline-Album : 1
    // Offline-Track : 2
    // albumCollection : 3
    // searchOffline : 4
    // searchOnline : 5
    // V-Pop : 6, 7, 8
    // K-Pop : 9, 10, 11
    // J-Pop : 12, 13, 14
    // C-Pop : 15, 16, 17
    // US-UK : 18, 19, 20
    // Other : 21, 22, 23
    // PlaylistOnline : 25
    // PlaylistOffline : 31
    // Folder : 32
    public static int FRAGMENT_MODE = 0; // 0 - offline Fragment, 1 - CSN Fragment, 2 - AlbumDetail Fragment, 3 - Search Fragment
    public static int FRAGMENT_PREVIOUS_MODE = 0;
    public static boolean network_state = true;

    public static boolean sub_anim_state = false;
    public static boolean sub_anim_running = false;

    public static long startTime, endTime, activeTime;

    public static boolean set_src_complete = false;
    public static boolean bxh_vn_load_complete = false;
    public static boolean bxh_uk_load_complete = false;
    public static boolean vpop_bxh_load_complete = false;
    public static boolean STATE = true; // true - MainActivity, false - PlayActivity

    public static NotificationCompat.Builder builder;
    public static NotificationManager notificationManager;
    public static int notification_id;
    public static RemoteViews remoteViews;
    public static boolean clearNoti = false;

    public static void csn_Notification(Context context, Bitmap bitmap, String title, String artist, String state) {
        MusicResource.clearNoti = false;
        if (bitmap != null) MusicResource.noti_art = bitmap;
        if (title != null) MusicResource.noti_title = title;
        if (artist != null) MusicResource.noti_artist = artist;
        if (state != null) MusicResource.noti_state = state;
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.noti_custom_view);
        remoteViews.setImageViewBitmap(R.id.noti_art, MusicResource.noti_art);
        remoteViews.setTextViewText(R.id.noti_title, MusicResource.noti_title);
        remoteViews.setTextViewText(R.id.noti_artist, MusicResource.noti_artist);
        if (MusicResource.noti_state.equals("pause")) {
            remoteViews.setImageViewResource(R.id.noti_play, R.drawable.noti_pause);
            onGoing = false;
        } else {
            remoteViews.setImageViewResource(R.id.noti_play, R.drawable.noti_play);
            onGoing = true;
        }
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        int noti_play_id = 1;
        int noti_next_id = 2;
        int noti_back_id = 3;
        int noti_clear_id = 4;
        Intent noti_play_intent = new Intent("noti_play_clicked");
        noti_play_intent.putExtra("id", noti_play_id);
        Intent noti_next_intent = new Intent("noti_next_clicked");
        noti_next_intent.putExtra("id", noti_next_id);
        Intent noti_back_intent = new Intent("noti_back_clicked");
        noti_back_intent.putExtra("id", noti_back_id);

        Intent noti_clear_intent = new Intent("noti_clear_clicked");
        noti_clear_intent.putExtra("id", noti_clear_id);


        PendingIntent p_back_intent = PendingIntent.getBroadcast(context, 0, noti_back_intent, 0);

        PendingIntent p_play_intent = PendingIntent.getBroadcast(context, 0, noti_play_intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_play, p_play_intent);
        PendingIntent p_next_intent = PendingIntent.getBroadcast(context, 0, noti_next_intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_next, p_next_intent);

        remoteViews.setOnClickPendingIntent(R.id.noti_back, p_back_intent);
        PendingIntent p_clear_intent = PendingIntent.getBroadcast(context, 0, noti_clear_intent, 0);
        Intent notification_intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        notification_intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder = new NotificationCompat.Builder(context);
        notification_id = 1904; //KXPRO_NOTI_ID
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContent(remoteViews)
                .setAutoCancel(false)
                .setOngoing(onGoing)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setDeleteIntent(createOnDismissedIntent(context, notification_id))
                .setContentIntent(pendingIntent);

        //notification_id = (int) System.currentTimeMillis();


        notificationManager.notify(notification_id, builder.build());

    }

    public static Context context;

    public static boolean AUDIOFOCUS_STATE = false; // AUDIOFOCUS_GAIN - true, AUDIOFOCUS_LOSS - false

    public static boolean request_audio_focus(Context mContext) {

        if (AUDIOFOCUS_STATE) return true;

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        context = mContext;

        // Request focus for music stream and pass AudioManager.OnAudioFocusChangeListener
        // implementation reference
        int result = am.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Play
            AUDIOFOCUS_STATE = true;

            return true;
        }
        return false;
    }

    public static AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    switch (focusChange) {

                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                            // Lower the volume while ducking.
                            //Player.player.setVolume(0.2f, 0.2f);
                            //AUDIOFOCUS_STATE = false;
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                            Log.d("testAudio", "AUDIOFOCUS_LOSS_TRANSIENT");
                            if (Player.player != null) {
                                if (Player.player.isPlaying()) isPlaying = true;
                                else isPlaying = false;
                                Player.player.pause();
                                AUDIOFOCUS_STATE = false;
                                if (!MusicResource.clearNoti)
                                    context.sendBroadcast(new Intent("noti_audiofocus_change"));
                            }
                            //AUDIOFOCUS_STATE = false;

                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS):
                            Log.d("testAudio", "AUDIOFOCUS_LOSS");
                            //Player.player.setVolume(0.2f, 0.2f);
                            if (Player.player != null) {
                                if (Player.player.isPlaying()) isPlaying = true;
                                else isPlaying = false;
                                Player.player.pause();
                                AUDIOFOCUS_STATE = false;
                                if (!MusicResource.clearNoti)
                                    context.sendBroadcast(new Intent("noti_audiofocus_change"));
                            }
                            break;

                        case (AudioManager.AUDIOFOCUS_GAIN):
                            // Return the volume to normal and resume if paused.
                            /*mediaPlayer.setVolume(1f, 1f);*/
                            if (isPlaying) {
                                AUDIOFOCUS_STATE = true;
                                Player.player.start();
                                context.sendBroadcast(new Intent("noti_audiofocus_change"));
                            }
                            break;
                        default:
                            break;
                    }
                }
            };

    public static void addBitmapToMemCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        if (mMemoryCache != null)
            return mMemoryCache.get(key);
        else return null;
    }

    public static Bitmap decodeSampledBitmapFromUri(String art_uri, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(art_uri, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(art_uri, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public static String getQualityfromDefault(String qualityDefault, Track track) {
        LinkedHashMap<String, String> hashmapQuality = track.getStream_detail();
        ArrayList<String> values = new ArrayList<String>(hashmapQuality.values());
        String result = "";
        if (hashmapQuality.containsKey(qualityDefault))
            result = hashmapQuality.get(qualityDefault);
        else {
            if (qualityDefault.equals(Define.Q_256KBPS)) {
                result = values.get(values.size() - 1);
            } else if (qualityDefault.equals(Define.Q_192KBPS)) {
                result = values.get(values.size() - 1);
            } else
                result = values.get(0).toString();
        }
        track.setSrc(result);
        Log.d("getDetail", result.toString());
        return result;

    }

    public static String getQualityTypefromDefault(String qualityDefault, LinkedHashMap<String, String> hashmapQuality) {
        ArrayList<String> values = new ArrayList<String>(hashmapQuality.keySet());
        String result = "";
        if (hashmapQuality.containsKey(qualityDefault)) {
            result = qualityDefault;
        } else {

            if (qualityDefault.equals(Define.Q_256KBPS)) {
                result = values.get(values.size() - 1);
            } else if (qualityDefault.equals(Define.Q_192KBPS)) {
                result = values.get(values.size() - 1);
            } else
                result = values.get(0).toString();
        }
        return result;
    }

    public static void makeHashmapQuality(ArrayList<Track> list, int position, String matcher) {
        Log.d("matcher", matcher.toString());
        if (matcher.contains("/128/")) {
            if (list.get(position).getQuality().equals(Define.Q_LOSSLESS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_LOSSLESS, matcher.replaceAll("128", "flac").replaceAll(".mp3", ".flac").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_500KBPS, matcher.replaceAll("128", "m4a").replaceAll(".mp3", ".m4a").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_320KBPS, matcher.replaceAll("128", "320").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_128KBPS, matcher.replaceAll("128", "128").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
            if (list.get(position).getQuality().equals(Define.Q_500KBPS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_500KBPS, matcher.replaceAll("128", "m4a").replaceAll(".mp3", ".m4a").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_320KBPS, matcher.replaceAll("128", "320").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_128KBPS, matcher.replaceAll("128", "128").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
            if (list.get(position).getQuality().equals(Define.Q_320KBPS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_320KBPS, matcher.replaceAll("128", "320").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_128KBPS, matcher.replaceAll("128", "128").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
            if (list.get(position).getQuality().equals(Define.Q_256KBPS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_256KBPS, matcher.replaceAll("128", "320").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_128KBPS, matcher.replaceAll("128", "128").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
            if (list.get(position).getQuality().equals(Define.Q_192KBPS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_192KBPS, matcher.replaceAll("128", "320").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_128KBPS, matcher.replaceAll("128", "128").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
            if (list.get(position).getQuality().equals(Define.Q_128KBPS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_128KBPS, matcher.replaceAll("128", "128").replaceAll(".mp3", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
        } else if (matcher.contains("/32/")) {
            if (list.get(position).getQuality().equals(Define.Q_LOSSLESS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_LOSSLESS, matcher.replaceAll("32", "flac").replaceAll(".m4a", ".flac").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_500KBPS, matcher.replaceAll("32", "m4a").replaceAll(".m4a", ".m4a").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_320KBPS, matcher.replaceAll("32", "320").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_128KBPS, matcher.replaceAll("32", "128").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
            if (list.get(position).getQuality().equals(Define.Q_500KBPS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_500KBPS, matcher.replaceAll("32", "m4a").replaceAll(".m4a", ".m4a").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_320KBPS, matcher.replaceAll("32", "320").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_128KBPS, matcher.replaceAll("32", "128").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
            if (list.get(position).getQuality().equals(Define.Q_320KBPS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_320KBPS, matcher.replaceAll("32", "320").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_128KBPS, matcher.replaceAll("32", "128").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
            if (list.get(position).getQuality().equals(Define.Q_256KBPS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_256KBPS, matcher.replaceAll("32", "320").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_128KBPS, matcher.replaceAll("32", "128").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
            if (list.get(position).getQuality().equals(Define.Q_192KBPS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_192KBPS, matcher.replaceAll("32", "320").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                quality.put(Define.Q_128KBPS, matcher.replaceAll("32", "128").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
            if (list.get(position).getQuality().equals(Define.Q_128KBPS)) {
                LinkedHashMap<String, String> quality = new LinkedHashMap<>();
                quality.put(Define.Q_128KBPS, matcher.replaceAll("32", "128").replaceAll(".m4a", ".mp3").replaceAll("/stream", "/downloads"));
                list.get(position).setStream_detail(quality);
            }
        }
        Log.d("getDetail1", list.get(position).getStream_detail().toString());
    }

    private static PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("com.my.app.notificationId", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, 0);
        return pendingIntent;
    }
}
