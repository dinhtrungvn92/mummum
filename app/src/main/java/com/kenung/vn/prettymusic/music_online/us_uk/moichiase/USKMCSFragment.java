package com.kenung.vn.prettymusic.music_online.us_uk.moichiase;

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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kenung.vn.prettymusic.BlurBuilder;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.Player;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.listener.OnLoadMoreListener;
import com.kenung.vn.prettymusic.module.ImageHelper;
import com.kenung.vn.prettymusic.music_online.Track;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by KXPRO on 5/11/2017.
 */

public class USKMCSFragment extends Fragment {

    private CircleImageView subArt;
    private Button sub_Play_btn;
    private TextView subTitle;
    private TextView subArtist;
    private ImageView main_background;
    private RotateAnimation anim;
    private Calendar calendar;
    private float imagePositionOffset;
    private ProgressBar progressBar;
    private LinearLayout subPlaySong;
    private RecyclerView recyclerView;
    private TextView cannotLoad;
    private USKMCSAdapter uskMCSAdapter;
    private SwipeRefreshLayout refreshLayout;

    private BroadcastReceiver receiverUSKMCSLoadComplete =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setHasFixedSize(true);
                    uskMCSAdapter = new USKMCSAdapter(getContext(), MusicResource.USK_MCS_Refresh, recyclerView, Glide.with(getActivity()));
                    recyclerView.setAdapter(uskMCSAdapter);
                    progressBar.setVisibility(View.GONE);

                    uskMCSAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                        @Override
                        public void onLoadMore() {
                            MusicResource.USK_MCS_Refresh.add(null);
                            uskMCSAdapter.notifyItemInserted(MusicResource.USK_MCS_Refresh.size() - 1);

                            //Load more data for reyclerview

                            String usk_mcs_page_url =
                                    String.format("http://chiasenhac.vn/mp3/us-uk/new%d.html", MusicResource.USK_MCS_PAGE);

                            doLoadMore(usk_mcs_page_url);

                            MusicResource.USK_MCS_PAGE++;
                        }
                    });
                }
            };

    private BroadcastReceiver receiverUSKMCSVNonClicked =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    recyclerView.setEnabled(false);
                    subPlaySong.setEnabled(false);

                    if (!MusicResource.network_state) {
                        Toast.makeText(getContext(), getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MusicResource.song_playing = null;

                    MusicResource.isCanChange = false;
                    progressBar.setVisibility(View.VISIBLE);

                    MusicResource.MODE = 19;
                    MusicResource.playAtSearchAlbum = false;
                    if (Player.player != null && MusicResource.set_src_complete == true) {
                        Player.player.reset();
                    }

                    MusicResource.set_src_complete = false;

                    if (MusicResource.USK_MCS_Refresh.size() > 0 && !MusicResource.USK_MCS_Refresh.contains(null)) {
                        MusicResource.USK_MCS = new ArrayList<>(MusicResource.USK_MCS_Refresh);
                    } else {
                        MusicResource.USK_MCS = new ArrayList<>(MusicResource.USK_MCS_Refresh);
                        MusicResource.USK_MCS.removeAll(Collections.singleton(null));
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new CSN_getTrackDetail().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                MusicResource.USK_MCS.get(MusicResource.songPosition).getUrl());
                    } else {
                        new CSN_getTrackDetail().execute(MusicResource.USK_MCS.get(MusicResource.songPosition).getUrl());
                    }

                    if (!MusicResource.sub_anim_running) {
                        MusicResource.sub_anim_state = true;
                        MusicResource.sub_anim_running = true;
                        subArt.setRotation(MusicResource.imagePosition);
                        subArt.startAnimation(anim);

                        calendar = Calendar.getInstance();
                        MusicResource.startTime = calendar.get(Calendar.HOUR) * 3600000 +
                                calendar.get(Calendar.MINUTE) * 60000 +
                                calendar.get(Calendar.SECOND) * 1000 +
                                calendar.get(Calendar.MILLISECOND);
                    }
                }
            };

    BroadcastReceiver receiverUSKMCSLoadMoreDone = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            uskMCSAdapter.notifyDataSetChanged();
            uskMCSAdapter.setLoaded();
        }
    };

    BroadcastReceiver receiverUSKMCSRefreshDone = new
            BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    uskMCSAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }
            };

    public static USKMCSFragment newInstance() {
        USKMCSFragment fragment = new USKMCSFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilterUSKMCSonClicked = new IntentFilter("USKMCSonClicked");
        IntentFilter intentFilterUSKMCSLoadComplete = new IntentFilter("noti_load_usk_mcs_done");
        IntentFilter load_us_uk = new IntentFilter("load_us_uk");
        IntentFilter noti_loadmore_usk_mcs_done = new IntentFilter("noti_loadmore_usk_mcs_done");
        IntentFilter noti_load_usk_mcs_refresh_done = new IntentFilter("noti_load_usk_mcs_refresh_done");
        getActivity().registerReceiver(receiverloadUSUK, load_us_uk);
        getActivity().registerReceiver(receiverUSKMCSVNonClicked, intentFilterUSKMCSonClicked);
        getActivity().registerReceiver(receiverUSKMCSLoadComplete, intentFilterUSKMCSLoadComplete);
        getActivity().registerReceiver(receiverUSKMCSLoadMoreDone, noti_loadmore_usk_mcs_done);
        getActivity().registerReceiver(receiverUSKMCSRefreshDone, noti_load_usk_mcs_refresh_done);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mcs_fragment, container, false);

        subArt = (CircleImageView) getActivity().findViewById(R.id.subArt);
        subTitle = (TextView) getActivity().findViewById(R.id.subTitle);
        subArtist = (TextView) getActivity().findViewById(R.id.subArtist);
        sub_Play_btn = (Button) getActivity().findViewById(R.id.subPlaybtn);
        main_background = (ImageView) getActivity().findViewById(R.id.main_background);
        subPlaySong = (LinearLayout) getActivity().findViewById(R.id.subPlaySong);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) view.findViewById(R.id.bxh_vn_view);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (uskMCSAdapter != null) {
                    doRefresh();
                    MusicResource.USK_MCS_PAGE = 2;
                } else refreshLayout.setRefreshing(false);
            }
        });

        cannotLoad = (TextView) view.findViewById(R.id.cannotLoad);
        cannotLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().sendBroadcast(new Intent("load_us_uk"));
            }
        });
        doLoad();
        return view;
    }

    BroadcastReceiver receiverloadUSUK = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doLoad();
        }
    };

    public void doLoad() {
        cannotLoad.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        if (MusicResource.network_state) {
            progressBar.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new CSN_USK_MCS_Collect().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        "http://chiasenhac.vn/mp3/us-uk/new.html");
            } else {
                new CSN_USK_MCS_Collect().execute("http://chiasenhac.vn/mp3/us-uk/new.html");
            }
        } else {
            cannotLoad.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void doRefresh() {
        MusicResource.USK_MCS_Refresh.clear();
//        uskMCSAdapter.notifyDataSetChanged();
        cannotLoad.setVisibility(View.INVISIBLE);
        if (MusicResource.network_state) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new CSN_USK_MCS_Refresh().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        "http://chiasenhac.vn/mp3/us-uk/new.html");
            } else {
                new CSN_USK_MCS_Refresh().execute("http://chiasenhac.vn/mp3/us-uk/new.html");
            }
        } else {
            refreshLayout.setRefreshing(false);
            cannotLoad.setVisibility(View.VISIBLE);
        }
    }

    public void doLoadMore(String loadMoreUrl) {
        if (MusicResource.network_state) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new CSN_USK_MCS_LoadMore().executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        loadMoreUrl);
            } else {
                new CSN_USK_MCS_LoadMore().execute(loadMoreUrl);
            }
        } else {
            refreshLayout.setRefreshing(false);
            cannotLoad.setVisibility(View.VISIBLE);
        }
    }

    private class CSN_USK_MCS_LoadMore extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0]).userAgent("Chrome/59.0.3071.115").get();

                if (document == null) return null;

                Element mcs = document.select("table.tbtable").first();
                if (mcs == null) return null;
                Elements moichiase = mcs.select("tr");
                if (moichiase == null || moichiase.size() <= 0) return null;
                for (Element track : moichiase) {

                    Elements info = track.select("td");
                    if (info == null) continue;
                    Element basic_info = info.get(1).select("span").first();
                    if (basic_info == null) continue;
                    Element url = basic_info.select("a").first();
                    if (url == null) continue;
                    String extra_info = info.get(2).text();
                    if (extra_info == null) continue;

                    String title = url.text();
                    String artist = basic_info.text().replaceAll(title, "");
                    String duration = extra_info.split(" ")[0];
                    String quality = extra_info.split(" ")[1];

                    Track newTrack = new Track(title, artist, url.attr("href"), duration, quality);

                    if (!MusicResource.USK_MCS_Refresh.contains(newTrack)) {
                        MusicResource.USK_MCS_Refresh.add(newTrack);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            /*if (MusicResource.USK_MCS_Refresh.size() > 40)
                MusicResource.USK_MCS_Refresh.remove(MusicResource.USK_MCS_Refresh.size() - 41);
            else
                MusicResource.USK_MCS_Refresh.remove(MusicResource.USK_MCS_Refresh.size() - 1);*/
            MusicResource.USK_MCS_Refresh.removeAll(Collections.singleton(null));
            uskMCSAdapter.notifyItemRemoved(MusicResource.USK_MCS_Refresh.size());
            getActivity().sendBroadcast(new Intent("noti_loadmore_usk_mcs_done"));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private class CSN_USK_MCS_Refresh extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0]).userAgent("Chrome/59.0.3071.115").get();

                if (document == null) return null;

                Element mcs = document.select("table.tbtable").first();
                if (mcs == null) return null;
                Elements moichiase = mcs.select("tr");
                if (moichiase == null || moichiase.size() <= 0) return null;
                for (Element track : moichiase) {

                    Elements info = track.select("td");
                    if (info == null) continue;
                    Element basic_info = info.get(1).select("span").first();
                    if (basic_info == null) continue;
                    Element url = basic_info.select("a").first();
                    if (url == null) continue;
                    String extra_info = info.get(2).text();
                    if (extra_info == null) continue;

                    String title = url.text();
                    String artist = basic_info.text().replaceAll(title, "");
                    String duration = extra_info.split(" ")[0];
                    String quality = extra_info.split(" ")[1];

                    MusicResource.USK_MCS_Refresh.add(new Track(title, artist, url.attr("href"), duration, quality));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            refreshLayout.setRefreshing(false);
            uskMCSAdapter.setLoaded();
            getActivity().sendBroadcast(new Intent("noti_load_usk_mcs_refresh_done"));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private class CSN_USK_MCS_Collect extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0]).userAgent("Chrome/59.0.3071.115").get();

                if (document == null) return null;

                Element mcs = document.select("table.tbtable").first();
                if (mcs == null) return null;
                Elements moichiase = mcs.select("tr");
                if (moichiase == null || moichiase.size() <= 0) return null;
                for (Element track : moichiase) {

                    Elements info = track.select("td");
                    if (info == null) continue;
                    Element basic_info = info.get(1).select("span").first();
                    if (basic_info == null) continue;
                    Element url = basic_info.select("a").first();
                    if (url == null) continue;
                    String extra_info = info.get(2).text();
                    if (extra_info == null) continue;

                    String title = url.text();
                    String artist = basic_info.text().replaceAll(title, "");
                    String duration = extra_info.split(" ")[0];
                    String quality = extra_info.split(" ")[1];

                    MusicResource.USK_MCS_Refresh.add(new Track(title, artist, url.attr("href"), duration, quality));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            getActivity().sendBroadcast(new Intent("noti_load_usk_mcs_done"));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
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

            if (MusicResource.USK_MCS.get(MusicResource.songPosition).getSrc() != null) {

                if (Player.player == null) {
                    Player.player = new MediaPlayer();
                } else {
                    Player.player.reset();
                    Player.player = new MediaPlayer();
                }

                Player.player.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try {
                    Player.player.setDataSource(MusicResource.USK_MCS.get(MusicResource.songPosition).getSrc());
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
                        Pattern track = Pattern.compile("\"(.+\\.(mp3|m4a))");
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

                            MusicResource.makeHashmapQuality(MusicResource.USK_MCS, MusicResource.songPosition, matcher.group(1));
                            try {
                                Player.player.setDataSource(MusicResource.getQualityfromDefault(MusicResource.qualityLevel, MusicResource.USK_MDL.get(MusicResource.songPosition)));
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
                        }
                    }
                }

                String download_url = document.select("div.datelast").first().select("a").last().attr("href").toString();
                MusicResource.USK_MCS.get(MusicResource.songPosition).setDownload_url(download_url);
                Element lyricElement = document.select("p.genmed").first();
                if (lyricElement != null) {
                    String lyric = lyricElement.html();
                    lyric = lyric.replaceAll("<br>", "\n");
                    lyric = lyric.replaceAll("<span.*<\\/span>", "");
                    MusicResource.USK_MCS.get(MusicResource.songPosition).setLyric(lyric);
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
            if (bitmap != null) {
                MusicResource.subArt = bitmap;
                subArt.setImageBitmap(MusicResource.subArt);
                MusicResource.blurredBitmap = BlurBuilder.blur(getActivity(), MusicResource.subArt);
                MusicResource.noti_art = MusicResource.subArt;
            } else if (bm != null) {
                MusicResource.subArt = bm;
                MusicResource.noti_art = bm;
                subArt.setImageBitmap(MusicResource.subArt);
                MusicResource.blurredBitmap = BlurBuilder.blur(getActivity(), MusicResource.subArt);
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.nice_view_main_background_2);
                MusicResource.subArt = ((BitmapDrawable) drawable).getBitmap();
                Bitmap bitmapResized = Bitmap.createScaledBitmap(MusicResource.subArt, 200, 200, false);
                MusicResource.blurredBitmap = BlurBuilder.blur(getActivity(),
                        bitmapResized);
                MusicResource.noti_art = ImageHelper.getRoundedCornerBitmap(MusicResource.subArt, 30);
                subArt.setImageBitmap(MusicResource.subArt);

            }
            MusicResource.csn_Notification(getContext(),
                    MusicResource.noti_art,
                    MusicResource.USK_MCS.get(MusicResource.songPosition).getTitle(),
                    MusicResource.USK_MCS.get(MusicResource.songPosition).getArtist(), "play");


            MusicResource.subTitle = MusicResource.USK_MCS.get(MusicResource.songPosition).getTitle();
            MusicResource.subArtist = MusicResource.USK_MCS.get(MusicResource.songPosition).getArtist();
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);

            sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

            ImageViewAnimatedChange(getActivity(), main_background, MusicResource.blurredBitmap);

            progressBar.setVisibility(View.GONE);
            recyclerView.setEnabled(true);
            subPlaySong.setEnabled(true);

            if (MusicResource.request_audio_focus(getContext())) {
                Player.player.start();
                Player.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        if (MusicResource.repeat_mode == 0) {
                            if (MusicResource.shuffle)
                                getActivity().sendBroadcast(new Intent("intentFilterDoShuffle"));
                            else
                                getActivity().sendBroadcast(new Intent("intentFilterDoNext"));
                        } else {
                            getActivity().sendBroadcast(new Intent("intentFilterDoRepeat"));
                        }
                    }
                });
            }

            subPlaySong.setVisibility(View.VISIBLE);
            MusicResource.subPlaySongVisible = true;
            String download_url = MusicResource.USK_MCS.get(MusicResource.songPosition).getDownload_url();

            if (download_url != null) {
                new CSN_Download_Detail()
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, download_url);
            } else MusicResource.isCanChange = true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private class CSN_Download_Detail extends AsyncTask<String, Void, HashMap> {

        private String download_url;
        //        private String regex = "href=\"(.+)\" onmouseover.+: (.+) <.+\"color:(.+)\">(.+)</span> (.+)</a>";
        private String regex = "<a href=\"(.+?)\" .+?: (.+?) <.+?\"color: (.+?)\">(.+?)</span> (.+?)</a><br>";
        private Pattern pattern = Pattern.compile(regex);
        private HashMap<String, String> download_detail = new HashMap();

        @Override
        protected HashMap doInBackground(String... params) {

            download_url = params[0];

            try {

                Document document = Jsoup.connect(download_url).userAgent("Chrome/59.0.3071.115").get();
                if (document == null) return null;
                Element element = document.select("div#downloadlink2").first();
                if (element == null) return null;
                Matcher matcher = pattern.matcher(element.html());
                String download_default_url = "";
                while (matcher.find()) {


                    String hm_key = matcher.group(2) + " " + matcher.group(4) + " " + matcher.group(5);
                    String hm_value = matcher.group(1);
                    if (download_detail.containsKey(hm_key)) continue;
                    String key = matcher.group(2).toLowerCase() + matcher.group(4).toLowerCase();
                    if (key.contains("mp3128kbps")) {
                        download_default_url = hm_value;
                    }
                    if (key.contains("m4a")) {
                        hm_value = download_default_url.replaceAll("MP3 128kbps", "M4A 500kbps");
                        hm_value = hm_value.replaceAll(".mp3", ".m4a");
                        hm_value = hm_value.replaceAll("128", "m4a");
                    }
                    if (key.contains("flac")) {
                        hm_value = download_default_url.replaceAll("MP3 128kbps", "FLAC Lossless");
                        hm_value = hm_value.replaceAll(".mp3", ".flac");
                        hm_value = hm_value.replaceAll("128", "flac");
                    }
                    download_detail.put(hm_key, hm_value);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return download_detail;
        }

        @Override
        protected void onPostExecute(HashMap hashMap) {
            super.onPostExecute(hashMap);
            MusicResource.isCanChange = true;
            if (hashMap != null) {
                MusicResource.USK_MCS.get(MusicResource.songPosition).setDownload_detail(hashMap);

            }
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
}
