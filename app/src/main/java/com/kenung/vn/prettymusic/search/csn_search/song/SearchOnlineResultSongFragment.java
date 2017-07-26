package com.kenung.vn.prettymusic.search.csn_search.song;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.PlayActivity;
import com.kenung.vn.prettymusic.Player;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.listener.OnLoadMoreListener;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.search.csn_search.CSN_Search_RecyclerView_Adt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 14/05/2017.
 */

public class SearchOnlineResultSongFragment extends Fragment implements View.OnClickListener {
    ProgressBar progressBar;
    RecyclerView rv_song;
    CSN_Search csn_search;
    CSN_Search_Song_LoadMore csn_search_song_loadMore;
    TextView server_busy;
    CSN_Search_RecyclerView_Adt adapter;
    String query;
    private CircleImageView subArt;
    private ImageButton sub_Next_btn;
    private ImageButton sub_Back_btn;
    private Button sub_Play_btn;
    private TextView subTitle;
    private TextView subArtist;
    private LinearLayout subPlaySong;
    private Calendar calendar;
    private long mLastClickTime = 0;
    private Animation anim;
    private float imagePositionOffset = 0f;
    ProgressBar progressBarActivity;
    boolean checkLoadMoreDone = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_search_online_result_item, container, false);

        imagePositionOffset = MusicResource.imagePosition;
        anim = new RotateAnimation(MusicResource.imagePosition - imagePositionOffset,
                360f + MusicResource.imagePosition - imagePositionOffset,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(MusicResource.imageDuration);
        progressBarActivity = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        subArt = (CircleImageView) getActivity().findViewById(R.id.subArt);
        subTitle = (TextView) getActivity().findViewById(R.id.subTitle);
        subArtist = (TextView) getActivity().findViewById(R.id.subArtist);
        subTitle.setSelected(true);
        subArtist.setSelected(true);
        sub_Play_btn = (Button) getActivity().findViewById(R.id.subPlaybtn);
        sub_Next_btn = (ImageButton) getActivity().findViewById(R.id.subNextbtn);
        sub_Back_btn = (ImageButton) getActivity().findViewById(R.id.subBackbtn);
        subPlaySong = (LinearLayout) getActivity().findViewById(R.id.subPlaySong);
        sub_Play_btn.setOnClickListener(this);
        sub_Next_btn.setOnClickListener(this);
        sub_Back_btn.setOnClickListener(this);
        subPlaySong.setOnClickListener(this);
        sub_Play_btn.setBackgroundResource(R.drawable.ic_play_circle_filled_white);
        if (Player.player != null) {
            if (Player.player.isPlaying()) {
                sub_Play_btn.setBackgroundResource(R.drawable.ic_play_circle_filled_white);

            } else sub_Play_btn.setBackgroundResource(R.drawable.ic_pause_circle_filled_white);
        }
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        server_busy = (TextView) view.findViewById(R.id.server_busy);
//        progressBar.setVisibility(View.VISIBLE);
        rv_song = (RecyclerView) view.findViewById(R.id.rv_search_online);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_song.setLayoutManager(layoutManager);

        if (MusicResource.queryOnline != null) query = MusicResource.queryOnline;

        if (MusicResource.CSNSearchSongResultTemp != null) {
            if (MusicResource.CSNSearchSongResultTemp.size() > 0) {
                adapter = new CSN_Search_RecyclerView_Adt(
                        getActivity(),
                        MusicResource.CSNSearchSongResultTemp,
                        "song",
                        Glide.with(getActivity()),
                        rv_song);

                rv_song.setAdapter(adapter);

                adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        Log.d("haint", "Load More");
                        MusicResource.CSNSearchSongResultTemp.add(null);
                        adapter.notifyItemInserted(MusicResource.CSNSearchSongResultTemp.size() - 1);

                        //Load more data for reyclerview

                        String search_song_page_url =
                                String.format(
                                        "http://search.chiasenhac.vn/search.php?s=%s&mode=&order=quality&cat=music&page=%d",
                                        query,
                                        MusicResource.Search_SONG_PAGE);

                        doLoadMore(search_song_page_url);

                        MusicResource.Search_SONG_PAGE++;
                    }
                });
            }
        }
        HashMap<Integer, ArrayList<Track>> hashMap = new HashMap<>();

        hashMap.put(6, MusicResource.VPop_BXH);
        hashMap.put(7, MusicResource.VPop_MCS);
        hashMap.put(8, MusicResource.VPop_MDL);
        hashMap.put(9, MusicResource.KPop_BXH);
        hashMap.put(10, MusicResource.KPop_MCS);
        hashMap.put(11, MusicResource.KPop_MDL);
        hashMap.put(12, MusicResource.JPop_BXH);
        hashMap.put(13, MusicResource.JPop_MCS);
        hashMap.put(14, MusicResource.JPop_MDL);
        hashMap.put(15, MusicResource.CPop_BXH);
        hashMap.put(16, MusicResource.CPop_MCS);
        hashMap.put(17, MusicResource.CPop_MDL);
        hashMap.put(18, MusicResource.USK_BXH);
        hashMap.put(19, MusicResource.USK_MCS);
        hashMap.put(20, MusicResource.USK_MDL);
        hashMap.put(21, MusicResource.Other_BXH);
        hashMap.put(22, MusicResource.Other_MCS);
        hashMap.put(23, MusicResource.Other_MDL);
        hashMap.put(24, MusicResource.songListOnlineSearch);
        hashMap.put(25, MusicResource.songListOnlinePlayList);
        hashMap.put(26, MusicResource.listSongPlaylistShare);

        if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
            MusicResource.songListOnline = hashMap.get(MusicResource.MODE);
            MusicResource.songListOnlinePlay = new ArrayList<>(MusicResource.songListOnline);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MusicResource.isMainPause = true;
        IntentFilter noti_play_intent = new IntentFilter("noti_play_intent_action");
        IntentFilter noti_next_intent = new IntentFilter("noti_next_intent_action");
        IntentFilter noti_back_intent = new IntentFilter("noti_back_intent_action");
        getActivity().registerReceiver(receiverNotiPlay, noti_play_intent);
        getActivity().registerReceiver(receiverNotiNext, noti_next_intent);
        getActivity().registerReceiver(receiverNotiBack, noti_back_intent);
        IntentFilter intentFilterCSNSearchDone = new IntentFilter("noti_csn_search_done_song");
        IntentFilter intentFilterCSNSearchQuerySubmit = new IntentFilter("noti_search_online_query_submit");
        IntentFilter intentFilterCSNSearchOnClicked = new IntentFilter("noti_CSN_Search_Song_onClicked");
        IntentFilter noti_loadmore_csn_search_song_done = new IntentFilter("noti_loadmore_csn_search_song_done");

        IntentFilter noti_audiofocus_change = new IntentFilter("noti_audiofocus_change");
        getActivity().registerReceiver(noti_audiofocus_changeReceiver, noti_audiofocus_change);

        getActivity().registerReceiver(receiverCSNSearchDone, intentFilterCSNSearchDone);
        getActivity().registerReceiver(receiverCSNSearchQuerySubmit, intentFilterCSNSearchQuerySubmit);
        getActivity().registerReceiver(receiverCSNSearchOnClicked, intentFilterCSNSearchOnClicked);
        IntentFilter intentFilter = new IntentFilter("OnItemOnlineClickDone");
        getActivity().registerReceiver(OnItemOnlineClickDone, intentFilter);

        IntentFilter intentFilterDoShuffleOnPlayActivity = new IntentFilter("intentFilterDoShuffleOnPlayActivity");
        getActivity().registerReceiver(receiverDoShuffleOnPlayActivity, intentFilterDoShuffleOnPlayActivity);
        getActivity().registerReceiver(receiverCSNSearchSongLoadMoreDone, noti_loadmore_csn_search_song_done);
        IntentFilter intentFilterDoNextOnPlayActivity = new IntentFilter("intentFilterDoNextOnPlayActivity");
        getActivity().registerReceiver(receiverDoNextOnPlayActivity, intentFilterDoNextOnPlayActivity);
        IntentFilter intentFilterDoBackOnPlayActivity = new IntentFilter("intentFilterDoBackOnPlayActivity");
        getActivity().registerReceiver(receiverDoBackOnPlayActivity, intentFilterDoBackOnPlayActivity);
        IntentFilter OnItemOnlineClick = new IntentFilter("OnItemOnlineClick");
        getActivity().registerReceiver(OnItemOnlineClickReceiver, OnItemOnlineClick);
        IntentFilter OnItemOnlineClickDone = new IntentFilter("OnItemOnlineClickDone");
        getActivity().registerReceiver(OnItemOnlineClickDoneReceiver, OnItemOnlineClickDone);
        dochange();
    }

    private BroadcastReceiver OnItemOnlineClickDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBarActivity.setVisibility(View.GONE);
            dochange();
        }
    };
    private BroadcastReceiver OnItemOnlineClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBarActivity.setVisibility(View.VISIBLE);
        }
    };

    BroadcastReceiver receiverCSNSearchSongLoadMoreDone = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.notifyDataSetChanged();
            adapter.setLoaded();
        }
    };

    BroadcastReceiver OnItemOnlineClickDone = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            progressBar.setVisibility(View.GONE);
            dochange();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        MusicResource.isMainPause = false;
        getActivity().unregisterReceiver(receiverNotiPlay);
        getActivity().unregisterReceiver(receiverNotiNext);
    }

    private BroadcastReceiver receiverCSNSearchOnClicked
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            progressBar.setVisibility(View.VISIBLE);
            MusicResource.MODE = 24;
            MusicResource.playAtSearchAlbum = false;
            if (MusicResource.CSNSearchSongResultTemp.size() > 0 &&
                    !MusicResource.CSNSearchSongResultTemp.contains(null)) {
                MusicResource.songListOnlineSearch = new ArrayList<>(MusicResource.CSNSearchSongResultTemp);
                MusicResource.songListOnlinePlay = new ArrayList<>(MusicResource.songListOnlineSearch);

                getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));
            } else {
                MusicResource.songListOnlineSearch = new ArrayList<>(MusicResource.CSNSearchSongResultTemp);
                MusicResource.songListOnlineSearch.removeAll(Collections.singleton(null));
                MusicResource.songListOnlinePlay = new ArrayList<>(MusicResource.songListOnlineSearch);

                getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));
            }
        }
    };

    private BroadcastReceiver receiverCSNSearchQuerySubmit
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            server_busy.setVisibility(View.INVISIBLE);
            query = intent.getStringExtra("SearchQuery");
            if (query == null) return;
            if (MusicResource.queryOnline != null && query != MusicResource.queryOnline)
                MusicResource.Search_SONG_PAGE = 2;

            String uri = String.format("http://search.chiasenhac.vn/search.php?s=%s&mode=&order=quality&cat=music", query);

            //MusicResource.CSNSearchSongResult = new ArrayList<>();
            if (adapter != null) {
                MusicResource.CSNSearchSongResultTemp.clear();
                adapter.notifyDataSetChanged();
            }
            csn_search = new CSN_Search();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                csn_search.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);
            } else {
                csn_search.execute(uri);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (csn_search != null && csn_search.getStatus() == AsyncTask.Status.RUNNING)
            csn_search.cancel(true);
        if (csn_search_song_loadMore != null && csn_search_song_loadMore.getStatus() == AsyncTask.Status.RUNNING)
            csn_search_song_loadMore.cancel(true);
    }

    private BroadcastReceiver receiverCSNSearchDone
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("testserver_busySong", MusicResource.CSNSearchSongResult.size() + "");
            progressBar.setVisibility(View.GONE);
            checkLoadMoreDone = true;
            if (MusicResource.CSNSearchSongResultTemp.size() == 0) {
                server_busy.setVisibility(View.VISIBLE);
            } else {
                server_busy.setVisibility(View.INVISIBLE);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                } else {
                    adapter = new CSN_Search_RecyclerView_Adt(
                            getActivity(),
                            MusicResource.CSNSearchSongResultTemp,
                            "song",
                            Glide.with(getActivity()),
                            rv_song);
                    rv_song.setAdapter(adapter);

                    adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                        @Override
                        public void onLoadMore() {
                            Log.d("haint", "Load More");
                            MusicResource.CSNSearchSongResultTemp.add(null);
                            adapter.notifyItemInserted(MusicResource.CSNSearchSongResultTemp.size() - 1);

                            //Load more data for reyclerview

                            String search_song_page_url =
                                    String.format(
                                            "http://search.chiasenhac.vn/search.php?s=%s&mode=&order=quality&cat=music&page=%d",
                                            query,
                                            MusicResource.Search_SONG_PAGE);

                            doLoadMore(search_song_page_url);

                            MusicResource.Search_SONG_PAGE++;
                        }
                    });
                }
            }
        }
    };

    public void doLoadMore(String loadMoreUrl) {

        if (MusicResource.network_state) {
            checkLoadMoreDone = false;
            csn_search_song_loadMore = new CSN_Search_Song_LoadMore();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                csn_search_song_loadMore.executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        loadMoreUrl);
            } else {
                csn_search_song_loadMore.execute(loadMoreUrl);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.subPlaySong: {

                if (Player.player != null && Player.player.isPlaying()) {
                    calendar = Calendar.getInstance();
                    MusicResource.endTime = calendar.get(Calendar.HOUR) * 3600000 +
                            calendar.get(Calendar.MINUTE) * 60000 +
                            calendar.get(Calendar.SECOND) * 1000 +
                            calendar.get(Calendar.MILLISECOND);
                    MusicResource.activeTime = MusicResource.endTime - MusicResource.startTime;
                    MusicResource.imagePosition += (360f * MusicResource.activeTime) / 20000f;
                    if (MusicResource.imagePosition >= 360) MusicResource.imagePosition -= 360f;

                    //MusicResource.startTime = MusicResource.endTime;
                }

                startActivity(new Intent(getContext(), PlayActivity.class));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            }
            break;
            case R.id.subPlaybtn: {
                if (MusicResource.isCanChange) {
                    if (Player.player == null) return;
                    if (Player.player.isPlaying()) {
                        sub_Play_btn.setBackgroundResource(R.drawable.stop_btn);

                        MusicResource.sub_anim_running = false;

                        Log.d("TestAnim", "PauseClick - " + MusicResource.imagePosition);

                        Player.player.pause();

                        getActivity().sendBroadcast(new Intent("noti_audiostate_change"));
                        MusicResource.csn_Notification(getActivity(),
                                null,
                                null,
                                null, "pause");
                    } else if (MusicResource.request_audio_focus(getActivity())) {

                        Player.player.start();

                        sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

                        getActivity().sendBroadcast(new Intent("noti_audiostate_change"));
                        MusicResource.csn_Notification(getActivity(),
                                null,
                                null,
                                null, "play");
                    }
                }
            }
            break;
            case R.id.subNextbtn: {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    mLastClickTime = SystemClock.elapsedRealtime();
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (MusicResource.isCanChange) {
                    if (MusicResource.shuffle)
                        getActivity().sendBroadcast(new Intent("intentFilterDoShuffle"));
                    else
                        doNext();
                }
            }
            break;
            case R.id.subBackbtn: {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    mLastClickTime = SystemClock.elapsedRealtime();
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (MusicResource.isCanChange) {
                    if (MusicResource.shuffle)
                        getActivity().sendBroadcast(new Intent("intentFilterDoShuffle"));
                    doBack();
                }
            }
            break;
            default:
                break;
        }
    }

    public void doNext() {


        MusicResource.set_src_complete = false;
        if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {

            if (!MusicResource.network_state) {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            if (Player.player != null) {
                Player.player.reset();
            }
            MusicResource.track_ready = false;
            HashMap<Integer, ArrayList<Track>> hashMap = new HashMap<>();
            hashMap.put(6, MusicResource.VPop_BXH);
            hashMap.put(7, MusicResource.VPop_MCS);
            hashMap.put(8, MusicResource.VPop_MDL);
            hashMap.put(9, MusicResource.KPop_BXH);
            hashMap.put(10, MusicResource.KPop_MCS);
            hashMap.put(11, MusicResource.KPop_MDL);
            hashMap.put(12, MusicResource.JPop_BXH);
            hashMap.put(13, MusicResource.JPop_MCS);
            hashMap.put(14, MusicResource.JPop_MDL);
            hashMap.put(15, MusicResource.CPop_BXH);
            hashMap.put(16, MusicResource.CPop_MCS);
            hashMap.put(17, MusicResource.CPop_MDL);
            hashMap.put(18, MusicResource.USK_BXH);
            hashMap.put(19, MusicResource.USK_MCS);
            hashMap.put(20, MusicResource.USK_MDL);
            hashMap.put(21, MusicResource.Other_BXH);
            hashMap.put(22, MusicResource.Other_MCS);
            hashMap.put(23, MusicResource.Other_MDL);
            hashMap.put(24, MusicResource.songListOnlineSearch);
            hashMap.put(25, MusicResource.songListOnlinePlayList);
            hashMap.put(26, MusicResource.listSongPlaylistShare);

            MusicResource.songPosition = (
                    MusicResource.songPosition < hashMap.get(MusicResource.MODE).size() - 1) ?
                    MusicResource.songPosition + 1 : 0;
            getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));
        } else {

            getActivity().sendBroadcast(new Intent("intentFilterDoNext"));
        }

    }

    public void doBack() {


        MusicResource.set_src_complete = false;
        if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
            if (!MusicResource.network_state) {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            if (Player.player != null) {
                Player.player.reset();
            }
            MusicResource.track_ready = false;
            HashMap<Integer, ArrayList<Track>> hashMap = new HashMap<>();
            hashMap.put(6, MusicResource.VPop_BXH);
            hashMap.put(7, MusicResource.VPop_MCS);
            hashMap.put(8, MusicResource.VPop_MDL);
            hashMap.put(9, MusicResource.KPop_BXH);
            hashMap.put(10, MusicResource.KPop_MCS);
            hashMap.put(11, MusicResource.KPop_MDL);
            hashMap.put(12, MusicResource.JPop_BXH);
            hashMap.put(13, MusicResource.JPop_MCS);
            hashMap.put(14, MusicResource.JPop_MDL);
            hashMap.put(15, MusicResource.CPop_BXH);
            hashMap.put(16, MusicResource.CPop_MCS);
            hashMap.put(17, MusicResource.CPop_MDL);
            hashMap.put(18, MusicResource.USK_BXH);
            hashMap.put(19, MusicResource.USK_MCS);
            hashMap.put(20, MusicResource.USK_MDL);
            hashMap.put(21, MusicResource.Other_BXH);
            hashMap.put(22, MusicResource.Other_MCS);
            hashMap.put(23, MusicResource.Other_MDL);
            hashMap.put(24, MusicResource.songListOnlineSearch);
            hashMap.put(25, MusicResource.songListOnlinePlayList);
            hashMap.put(26, MusicResource.listSongPlaylistShare);

            MusicResource.songPosition = (MusicResource.songPosition > 0) ? MusicResource.songPosition - 1 :
                    hashMap.get(MusicResource.MODE).size() - 1;
            getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));
        } else {

            getActivity().sendBroadcast(new Intent("intentFilterDoBack"));
        }

    }

    BroadcastReceiver receiverDoBackOnPlayActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dochange();
        }
    };
    BroadcastReceiver receiverDoNextOnPlayActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dochange();
        }
    };
    BroadcastReceiver receiverDoShuffleOnPlayActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dochange();
        }
    };

    private class CSN_Search_Song_LoadMore extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0]).userAgent("Chrome/59.0.3071.115").get();
                Elements tables = null;
                if (document != null) tables = document.select("table");
                if (tables != null && tables.size() > 0) {
                    for (Element table : tables) {
                        Elements search_detail = table.select("tr");

                        if (search_detail != null && search_detail.size() > 0) {
                            Log.d("TestCSNSearch", "search_detail - " + search_detail.size());
                        }

                        for (Element item_detail : search_detail) {

                            Element info_basic = item_detail
                                    .getElementsByClass("tenbh")
                                    .first();

                            Element info_extra = item_detail
                                    .getElementsByClass("gen")
                                    .first();

                            if (info_basic != null) {
                                String title = info_basic.select("a").first().text();
                                String artist = info_basic.select("p").last().text();
                                String url = info_basic.select("a").first().attr("href");

                                if (info_extra != null) {

                                    String duration = info_extra.text().split(" ")[0];
                                    String quality = info_extra.text().split(" ")[1];
                                    Log.d("TestCSNSearch", "INFO_EXTRA - " + duration + "/" + quality);

                                    //csn_search_result.add(new Track(title, artist, url, duration, quality));
                                    MusicResource.CSNSearchSongResultTemp.add(new Track(title, artist, url, duration, quality));
                                } else {
                                    if (title != null)
                                        Log.d("TestCSNSearch", title + " - " + artist);
                                    //csn_search_result.add(new Track(title, artist, url));
                                    MusicResource.CSNSearchSongResultTemp.add(new Track(title, artist, url));
                                }
                            }

                        }

                        return null;
                        // table 1 - ten bai hat / ca si
                        // table 2 - album
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
            /*if (MusicResource.CSNSearchSongResultTemp.size() > 25)
                MusicResource.CSNSearchSongResultTemp.remove(MusicResource.CSNSearchSongResultTemp.size() - 26);
            else
                MusicResource.CSNSearchSongResultTemp.remove(MusicResource.CSNSearchSongResultTemp.size() - 1);*/
            MusicResource.CSNSearchSongResultTemp.removeAll(Collections.singleton(null));
            adapter.notifyItemRemoved(MusicResource.CSNSearchSongResultTemp.size());
            checkLoadMoreDone = true;
            getActivity().sendBroadcast(new Intent("noti_loadmore_csn_search_song_done"));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private class CSN_Search extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0]).userAgent("Chrome/57.0.2987.133").get();
                Elements tables = null;
                if (document != null) tables = document.select("table");
                if (tables != null && tables.size() > 0) {
                    for (Element table : tables) {
                        Elements search_detail = table.select("tr");

                        if (search_detail != null && search_detail.size() > 0) {
                            Log.d("TestCSNSearch", "search_detail - " + search_detail.size());
                        }

                        for (Element item_detail : search_detail) {

                            Element info_basic = item_detail
                                    .getElementsByClass("tenbh")
                                    .first();

                            Element info_extra = item_detail
                                    .getElementsByClass("gen")
                                    .first();

                            if (info_basic != null) {
                                String title = info_basic.select("a").first().text();
                                String artist = info_basic.select("p").last().text();
                                String url = info_basic.select("a").first().attr("href");

                                if (info_extra != null) {

                                    String duration = info_extra.text().split(" ")[0];
                                    String quality = info_extra.text().split(" ")[1];
                                    Log.d("TestCSNSearch", "INFO_EXTRA - " + duration + "/" + quality);

                                    //csn_search_result.add(new Track(title, artist, url, duration, quality));
                                    MusicResource.CSNSearchSongResultTemp.add(new Track(title, artist, url, duration, quality));
                                } else {
                                    if (title != null)
                                        Log.d("TestCSNSearch", title + " - " + artist);
                                    //csn_search_result.add(new Track(title, artist, url));
                                    MusicResource.CSNSearchSongResultTemp.add(new Track(title, artist, url));
                                }
                            }

                        }

                        return null;
                        // table 1 - ten bai hat / ca si
                        // table 2 - album
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

            //Log.e("TestCSNSearch", "Search Complete : " + MusicResource.CSNSearchSongResult.size());
            if (getActivity() != null) {
                getActivity().sendBroadcast(new Intent("noti_csn_search_done_song"));
                getActivity().sendBroadcast(new Intent("noti_csn_search_done"));
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    public void dochange() {
        subTitle.setText(MusicResource.subTitle);
        subArtist.setText(MusicResource.subArtist);
        subArt.setImageBitmap(MusicResource.subArt);
        sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
        if (Player.player != null) {
            if (Player.player.isPlaying()) {
                sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
            } else sub_Play_btn.setBackgroundResource(R.drawable.stop_btn);
        }
        if (MusicResource.subPlaySongVisible) {
            subPlaySong.setVisibility(View.VISIBLE);
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);
            subArt.setImageBitmap(MusicResource.subArt);
            subArt.startAnimation(anim);
        }
    }

    BroadcastReceiver receiverNotiPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sub_Play_btn.performClick();
        }
    };
    BroadcastReceiver receiverNotiNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sub_Next_btn.performClick();
        }
    };
    BroadcastReceiver receiverNotiBack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sub_Back_btn.performClick();
        }
    };

    BroadcastReceiver noti_audiofocus_changeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Player.player != null)
                if (Player.player.isPlaying()) {
                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                } else {
                    sub_Play_btn.setBackgroundResource(R.drawable.stop_btn);
                }
        }
    };
}
