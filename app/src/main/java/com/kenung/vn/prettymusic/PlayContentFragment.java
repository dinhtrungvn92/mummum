package com.kenung.vn.prettymusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.kenung.vn.prettymusic.module.CenterLayoutManager;
import com.kenung.vn.prettymusic.music_offline.album.AlbumDetailAdapter;
import com.kenung.vn.prettymusic.music_offline.downloaded.SongDownloadedAdapter;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_offline.song.SongAdapter;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.search.offline_search.song.SearchOfflineSongAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by KXPRO on 4/8/2017.
 */

public class PlayContentFragment extends Fragment {

    private RecyclerView songView;
    private ImageView main_background;
    private RotateAnimation anim;
    private Calendar calendar;
    private float imagePositionOffset;
    private int height, itemHeight;
    TextView play_song_title;
    TextView play_song_artist;
    ProgressBar progressBar;
    private AdView avBanner;
    private AdRequest adRequest;
    boolean checkAd = false;
    private SongDownloadedAdapter songDownloadedAdapter;
    private SearchOfflineSongAdapter searchOfflineSongAdapter;
    private AlbumDetailAdapter albumDetailAdapter;
    private SongAdapter songAdt;

    private BroadcastReceiver receiverPlayContentFocus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //songView.setSelection(MusicResource.songPosition);
            songView.smoothScrollToPosition(10);
        }
    };

    public static PlayContentFragment newInstance() {
        PlayContentFragment fragment = new PlayContentFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilterPlayContentFocus = new IntentFilter("noti_playcontent_focus");
        getActivity().registerReceiver(receiverPlayContentFocus, intentFilterPlayContentFocus);
        IntentFilter OnclickSongItemSearchOffline = new IntentFilter("OnclickSongItemSearchOffline");
        getActivity().registerReceiver(onItemSearchClickReceiver, OnclickSongItemSearchOffline);
        IntentFilter onItemSongClickIntent = new IntentFilter("OnclickSongItem");
        getActivity().registerReceiver(onItemSongOfflineClick, onItemSongClickIntent);
        IntentFilter onItemPlaylistOfflineClickIntent = new IntentFilter("OnClickPlaylistOfflineItem");
        getActivity().registerReceiver(onItemPlaylistOfflineClickReceiver, onItemPlaylistOfflineClickIntent);
        IntentFilter OnClickFolderSongsItem = new IntentFilter("OnClickFolderSongsItem");
        getActivity().registerReceiver(onItemFolderClickReceiver, OnClickFolderSongsItem);
        IntentFilter OnclickSongItemAlbumDetail = new IntentFilter("OnclickSongItemAlbumDetail");
        getActivity().registerReceiver(OnclickSongItemAlbumDetailReceiver, OnclickSongItemAlbumDetail);
        IntentFilter OnclickSongItemDownloaded = new IntentFilter("OnclickSongItemDownloaded");
        getActivity().registerReceiver(OnclickSongItemDownloadedReceiver, OnclickSongItemDownloaded);
        IntentFilter OnclickSongItemOnlineDone = new IntentFilter("OnItemOnlineClickDone");
        getActivity().registerReceiver(OnclickSongItemOnlineDoneReceiver, OnclickSongItemOnlineDone);
        IntentFilter OnclickSongItemOnline = new IntentFilter("OnItemOnlineClick");
        getActivity().registerReceiver(OnclickSongItemOnlineReceiver, OnclickSongItemOnline);
        IntentFilter noti_audioselected_change = new IntentFilter("noti_audioselected_change");
        getActivity().registerReceiver(onAudioStateChange, noti_audioselected_change);

        IntentFilter delete_track_done = new IntentFilter("delete_track_done");
        getActivity().registerReceiver(delete_track_done_receiver, delete_track_done);
    }

    BroadcastReceiver delete_track_done_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (songDownloadedAdapter != null) songDownloadedAdapter.notifyDataSetChanged();
            if (searchOfflineSongAdapter != null) searchOfflineSongAdapter.notifyDataSetChanged();
            if (albumDetailAdapter != null) albumDetailAdapter.notifyDataSetChanged();
            if (songAdt != null) songAdt.notifyDataSetChanged();
        }
    };

    BroadcastReceiver onAudioStateChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("testNext", "onAudioStateChange");
            songView.smoothScrollToPosition(MusicResource.songPosition);
        }
    };
    BroadcastReceiver OnclickSongItemOnlineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.VISIBLE);
        }
    };
    BroadcastReceiver OnclickSongItemOnlineDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onItemClickonline(MusicResource.songListOnline);
            progressBar.setVisibility(View.INVISIBLE);
        }
    };
    private BroadcastReceiver onItemSearchClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onItemClick(MusicResource.searchResultList);
        }
    };
    private BroadcastReceiver onItemSongOfflineClick = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onItemClick(MusicResource.songList);
        }
    };

    private BroadcastReceiver onItemPlaylistOfflineClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onItemClick(MusicResource.playlistOfflineSelected);
        }
    };
    BroadcastReceiver onItemFolderClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onItemClick(MusicResource.folderSelected);
        }
    };

    private BroadcastReceiver OnclickSongItemAlbumDetailReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onItemClick(MusicResource.albumCollectionContent);
        }
    };
    private BroadcastReceiver OnclickSongItemDownloadedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onItemClick(MusicResource.songListDownload);
        }
    };


    public void onItemClickonline(ArrayList<Track> tracks) {
        play_song_title.setText(tracks.get(MusicResource.songPosition).getTitle());
        play_song_artist.setText(tracks.get(MusicResource.songPosition).getArtist());
        ImageViewAnimatedChange(getContext(), main_background, MusicResource.blurredBitmap);
        getActivity().sendBroadcast(new Intent("noti_art_change"));
        getActivity().sendBroadcast(new Intent("noti_audioselected_change"));
    }

    public void onItemClick(ArrayList<Song> songs) {

        Drawable drawable = getResources().getDrawable(R.drawable.nice_view_main_background_2);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
        MusicResource.blurredBitmap = BlurBuilder.blur(getActivity(),
                bitmapResized);

        if (songs.get(MusicResource.songPosition).getArt_uri() != null) {
            Bitmap bm = decodeSampledBitmapFromUri(
                    songs.get(MusicResource.songPosition).getArt_uri(), 512, 512);
            if (bm != null)
                MusicResource.blurredBitmap = BlurBuilder.blur(getActivity(), bm);
        }
        ImageViewAnimatedChange(getContext(), main_background, MusicResource.blurredBitmap);

        play_song_title.setText(songs.get(MusicResource.songPosition).getTitle());
        play_song_artist.setText(songs.get(MusicResource.songPosition).getArtist());
        MusicResource.notificationManager.notify(MusicResource.notification_id, MusicResource.builder.build());
        getActivity().sendBroadcast(new Intent("noti_art_change"));
        getActivity().sendBroadcast(new Intent("noti_audioselected_change"));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getActivity().unregisterReceiver(receiverPlayContentFocus);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePositionOffset = MusicResource.imagePosition;

        anim = new RotateAnimation(MusicResource.imagePosition - imagePositionOffset,
                360f + MusicResource.imagePosition - imagePositionOffset,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(MusicResource.imageDuration);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    songView.smoothScrollToPosition(MusicResource.songPosition);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_fragment_content, container, false);
        avBanner = (AdView) view.findViewById(R.id.av_banner);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        play_song_title = (TextView) getActivity().findViewById(R.id.play_song_title);
        play_song_artist = (TextView) getActivity().findViewById(R.id.play_song_artist);
        songView = (RecyclerView) view.findViewById(R.id.play_content_lv);
        CenterLayoutManager layoutManager = new CenterLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        songView.setLayoutManager(layoutManager);
        main_background = (ImageView) getActivity().findViewById(R.id.play_main_background);

        HashMap<Integer, ArrayList<Track>> hashMap = new HashMap<>();

        hashMap.put(6, new ArrayList<>(MusicResource.VPop_BXH));
        hashMap.put(7, new ArrayList<>(MusicResource.VPop_MCS));
        hashMap.put(8, new ArrayList<>(MusicResource.VPop_MDL));
        hashMap.put(9, new ArrayList<>(MusicResource.KPop_BXH));
        hashMap.put(10, new ArrayList<>(MusicResource.KPop_MCS));
        hashMap.put(11, new ArrayList<>(MusicResource.KPop_MDL));
        hashMap.put(12, new ArrayList<>(MusicResource.JPop_BXH));
        hashMap.put(13, new ArrayList<>(MusicResource.JPop_MCS));
        hashMap.put(14, new ArrayList<>(MusicResource.JPop_MDL));
        hashMap.put(15, new ArrayList<>(MusicResource.CPop_BXH));
        hashMap.put(16, new ArrayList<>(MusicResource.CPop_MCS));
        hashMap.put(17, new ArrayList<>(MusicResource.CPop_MDL));
        hashMap.put(18, new ArrayList<>(MusicResource.USK_BXH));
        hashMap.put(19, new ArrayList<>(MusicResource.USK_MCS));
        hashMap.put(20, new ArrayList<>(MusicResource.USK_MDL));
        hashMap.put(21, new ArrayList<>(MusicResource.Other_BXH));
        hashMap.put(22, new ArrayList<>(MusicResource.Other_MCS));
        hashMap.put(23, new ArrayList<>(MusicResource.Other_MDL));
        hashMap.put(24, new ArrayList<>(MusicResource.songListOnlineSearch));
        hashMap.put(25, new ArrayList<>(MusicResource.songListOnlinePlayList));
        hashMap.put(26, new ArrayList<>(MusicResource.listSongPlaylistShare));

        if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
            PlayContentOnlineAdapter adapter = new PlayContentOnlineAdapter(getContext(), hashMap.get(MusicResource.MODE), Glide.with(getContext()));
            MusicResource.songListOnlinePlay = new ArrayList<>(hashMap.get(MusicResource.MODE));
            MusicResource.songListOnline = new ArrayList<>(MusicResource.songListOnlinePlay);
            songView.setAdapter(adapter);
            if (MusicResource.songListOnlinePlay.size() <= 4) checkAd = true;
        } else

            switch (MusicResource.MODE) {
                // Offline - Normal
                case 0: {
                    songAdt = new SongAdapter(view.getContext(), MusicResource.songList, "play_activity_offline");
                    songView.setAdapter(songAdt);
                    if (MusicResource.songList.size() <= 4) checkAd = true;
                }
                break;

                case 1: {
                    songAdt = new SongAdapter(view.getContext(), MusicResource.folderSelected, "folder");
                    songView.setAdapter(songAdt);
                    if (MusicResource.folderSelected.size() <= 4) checkAd = true;
                }
                break;
                // Offline - AlbumSelected
                case 2: {
                    songDownloadedAdapter = new SongDownloadedAdapter(view.getContext(), MusicResource.songListDownload, "");
                    songView.setAdapter(songDownloadedAdapter);
                    if (MusicResource.songList.size() <= 4) checkAd = true;
                }
                break;
                case 4: {
                    searchOfflineSongAdapter = new SearchOfflineSongAdapter(view.getContext(), MusicResource.searchResultList);
                    songView.setAdapter(searchOfflineSongAdapter);
                    if (MusicResource.searchResultList.size() <= 4) checkAd = true;
                }
                break;
                // CSN - Search
                case 5: {
//                    CSN_Search_RecyclerView_Adt songAdt = new CSN_Search_RecyclerView_Adt(view.getContext(), MusicResource.CSNSearchResult);
//                    songView.setAdapter(songAdt);
                }
                break;
                case 30: {
                    albumDetailAdapter = new AlbumDetailAdapter(view.getContext(), MusicResource.albumCollectionContent);
                    songView.setAdapter(albumDetailAdapter);
                    if (MusicResource.albumCollectionContent.size() <= 4) checkAd = true;
                }
                break;
                case 31: {
                    songAdt = new SongAdapter(view.getContext(), MusicResource.playlistOfflineSelected, "playlist_offline");
                    songView.setAdapter(songAdt);
                    if (MusicResource.playlistOfflineSelected.size() <= 4) checkAd = true;
                }
                break;
                default: {

                }
                break;
            }
        if (checkAd) new Init(avBanner).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return view;
    }

    public class Init extends AsyncTask<Void, Void, Void> {
        //        private WeakReference<AdView> adViewWeakReference;
        AdView adView;

        public Init(AdView adView) {
//            adViewWeakReference = new WeakReference<AdView>(adView);
            this.adView = adView;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            adRequest = new AdRequest.Builder().build();

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            if (adViewWeakReference != null) {
//                AdView adView = adViewWeakReference.get();
            if (adView != null)
                adView.loadAd(adRequest);
        }
    }

    private void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
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

    public void loadBitmap(String art_uri, ImageView imageView) {
        BitmapWorkerTask task = new BitmapWorkerTask(getContext(), imageView, 128, 128);
        task.execute(art_uri);
    }

    private class CSN_getTrackDetail extends AsyncTask<String, Void, Bitmap> {

        private String track_url;
        private String imageKey;
        private Bitmap bitmap;

        @Override
        protected Bitmap doInBackground(String... params) {

            track_url = params[0];

            imageKey = String.valueOf(track_url);
            bitmap = MusicResource.getBitmapFromMemCache(imageKey);

            if (MusicResource.BXH_VN.get(MusicResource.songPosition).getSrc() != null) {

                if (Player.player == null) {
                    Player.player = new MediaPlayer();
                } else {
                    Player.player.reset();
                    Player.player = new MediaPlayer();
                }

                Player.player.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try {
                    Player.player.setDataSource(MusicResource.BXH_VN.get(MusicResource.songPosition).getSrc());
                } catch (IllegalArgumentException error) {
                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                } catch (SecurityException error) {
                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                } catch (IllegalStateException error) {
                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                } catch (IOException error) {
                    error.printStackTrace();
                }

                try {
                    Player.player.prepare();
                } catch (IllegalStateException error) {
                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                } catch (IOException error) {
                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                }

                MusicResource.set_src_complete = true;
                return null;
            } else try {
                Document document = Jsoup.connect(track_url).userAgent("Chrome/59.0.3071.115").get();
                Elements scripts = document.select("script");

                if (scripts != null && scripts.size() > 0) {
                    for (Element e : scripts) {
                        Pattern track = Pattern.compile("\"(.+\\.mp3)");
                        Matcher matcher = track.matcher(e.html());

                        if (matcher.find()) {
                            Log.e(MusicResource.LOG_TAG, "get Track Complete - " + matcher.group(1));

                            if (Player.player == null) {
                                Player.player = new MediaPlayer();
                            } else {
                                Player.player.reset();
                                Player.player = new MediaPlayer();
                            }

                            Player.player.setAudioStreamType(AudioManager.STREAM_MUSIC);

                            try {
                                Player.player.setDataSource(matcher.group(1));
                            } catch (IllegalArgumentException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            } catch (SecurityException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            } catch (IllegalStateException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            } catch (IOException error) {
                                error.printStackTrace();
                            }

                            try {
                                Player.player.prepare();
                            } catch (IllegalStateException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            } catch (IOException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            }

                            MusicResource.set_src_complete = true;
                            MusicResource.BXH_VN.get(MusicResource.songPosition).setSrc(matcher.group(1));
                        }
                    }
                }

                if (bitmap != null) {
                    return null;
                } else {
                    String detail_csn_art_url = document.select("meta[property=og:image]").get(0).attr("content");
                    Bitmap detail_csn_art = null;
                    try {
                        URL url = new URL(detail_csn_art_url);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        detail_csn_art = BitmapFactory.decodeStream(input);
                        connection.disconnect();
                        if (detail_csn_art != null) {
                            MusicResource.addBitmapToMemCache(imageKey, detail_csn_art);
                            return detail_csn_art;
                        } else return null;
                    } catch (IOException e) {
                        // Log exception
                        return null;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {

            super.onPostExecute(bm);

            MusicResource.track_ready = true;

            Bitmap blurredBitmap;

            if (bitmap != null) {
                blurredBitmap = BlurBuilder.blur(getActivity(), bitmap);
                MusicResource.remoteViews.setImageViewBitmap(R.id.noti_art, bm);
            } else if (bm != null) {
                blurredBitmap = BlurBuilder.blur(getActivity(), bm);
                MusicResource.remoteViews.setImageViewResource(R.id.noti_art,
                        R.drawable.ic_music_note_black_48dp);
            } else {
                blurredBitmap = BlurBuilder.blur(getActivity(),
                        BitmapFactory.decodeResource(getResources(), R.drawable.nice_view_main_background_2));
            }

            ImageViewAnimatedChange(getContext(), main_background, blurredBitmap);

            Player.player.start();

            getActivity().sendBroadcast(new Intent("noti_art_change"));
            getActivity().sendBroadcast(new Intent("noti_audioselected_change"));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}