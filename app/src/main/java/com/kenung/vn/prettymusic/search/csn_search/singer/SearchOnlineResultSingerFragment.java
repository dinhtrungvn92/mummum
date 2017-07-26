package com.kenung.vn.prettymusic.search.csn_search.singer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.listener.OnLoadMoreListener;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.search.csn_search.CSN_Search_RecyclerView_Adt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Administrator on 14/05/2017.
 */

public class SearchOnlineResultSingerFragment extends Fragment {
    ProgressBar progressBar;
    RecyclerView rv_singer;
    CSN_Search csn_search;
    CSN_Search_Singer_LoadMore csn_search_singer_loadMore;
    TextView server_busy;
    CSN_Search_RecyclerView_Adt adapter;
    String query;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_search_online_result_item, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        server_busy = (TextView) view.findViewById(R.id.server_busy);
//        progressBar.setVisibility(View.VISIBLE);
        rv_singer = (RecyclerView) view.findViewById(R.id.rv_search_online);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_singer.setLayoutManager(layoutManager);

        if (MusicResource.queryOnline != null) query = MusicResource.queryOnline;
        if (MusicResource.CSNSearchSingerResultTemp != null) {
            if (MusicResource.CSNSearchSingerResultTemp.size() > 0) {
                adapter = new CSN_Search_RecyclerView_Adt(
                        getActivity(),
                        MusicResource.CSNSearchSingerResultTemp,
                        "singer",
                        Glide.with(getActivity()),
                        rv_singer);
                rv_singer.setAdapter(adapter);

                adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        Log.d("haint", "Load More");
                        MusicResource.CSNSearchSingerResultTemp.add(null);
                        adapter.notifyItemInserted(MusicResource.CSNSearchSingerResultTemp.size() - 1);

                        //Load more data for reyclerview

                        String search_singer_page_url =
                                String.format(
                                        "http://search.chiasenhac.vn/search.php?s=%s&mode=artist&order=quality&cat=music&page=%d",
                                        query,
                                        MusicResource.Search_SINGER_PAGE);

                        doLoadMore(search_singer_page_url);

                        MusicResource.Search_SINGER_PAGE++;
                    }
                });
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilterCSNSearchDone = new IntentFilter("noti_csn_search_done_singer");
        IntentFilter intentFilterCSNSearchQuerySubmit = new IntentFilter("noti_search_online_query_submit");
        IntentFilter intentFilterCSNSearchOnClicked = new IntentFilter("noti_CSN_Search_Singer_onClicked");
        IntentFilter noti_loadmore_csn_search_singer_done = new IntentFilter("noti_loadmore_csn_search_singer_done");

        getActivity().registerReceiver(receiverCSNSearchDone, intentFilterCSNSearchDone);
        getActivity().registerReceiver(receiverCSNSearchQuerySubmit, intentFilterCSNSearchQuerySubmit);
        getActivity().registerReceiver(receiverCSNSearchOnClicked, intentFilterCSNSearchOnClicked);
        IntentFilter intentFilter = new IntentFilter("OnItemOnlineClickDone");
        getActivity().registerReceiver(OnItemOnlineClickDone, intentFilter);
        getActivity().registerReceiver(receiverCSNSearchSingerLoadMoreDone, noti_loadmore_csn_search_singer_done);
    }

    BroadcastReceiver receiverCSNSearchSingerLoadMoreDone = new BroadcastReceiver() {
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
        }
    };
    private BroadcastReceiver receiverCSNSearchOnClicked
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            progressBar.setVisibility(View.VISIBLE);
            MusicResource.MODE = 24;
            MusicResource.playAtSearchAlbum = false;
            if (MusicResource.CSNSearchSingerResultTemp.size() > 0 &&
                    !MusicResource.CSNSearchSingerResultTemp.contains(null)) {
                MusicResource.songListOnlineSearch = new ArrayList<>(MusicResource.CSNSearchSingerResultTemp);
                MusicResource.songListOnlinePlay = new ArrayList<>(MusicResource.songListOnlineSearch);
                getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));
            } else {
                MusicResource.songListOnlineSearch = new ArrayList<>(MusicResource.CSNSearchSingerResultTemp);
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
                MusicResource.Search_SINGER_PAGE = 2;

            String uri = String.format("http://search.chiasenhac.vn/search.php?s=%s&mode=artist&order=quality&cat=music", query);

            //MusicResource.CSNSearchSingerResult = new ArrayList<>();
            if (adapter != null) {
                MusicResource.CSNSearchSingerResultTemp.clear();
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
        if (csn_search_singer_loadMore != null && csn_search_singer_loadMore.getStatus() == AsyncTask.Status.RUNNING)
            csn_search_singer_loadMore.cancel(true);
    }

    private BroadcastReceiver receiverCSNSearchDone
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.GONE);

            if (MusicResource.CSNSearchSingerResultTemp.size() == 0) {
                server_busy.setVisibility(View.VISIBLE);
            } else {
                server_busy.setVisibility(View.INVISIBLE);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                } else {
                    adapter = new CSN_Search_RecyclerView_Adt(
                            getActivity(),
                            MusicResource.CSNSearchSingerResultTemp,
                            "singer",
                            Glide.with(getActivity()),
                            rv_singer);
                    rv_singer.setAdapter(adapter);

                    adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                        @Override
                        public void onLoadMore() {
                            Log.d("haint", "Load More");
                            MusicResource.CSNSearchSingerResultTemp.add(null);
                            adapter.notifyItemInserted(MusicResource.CSNSearchSingerResultTemp.size() - 1);

                            //Load more data for reyclerview

                            String search_singer_page_url =
                                    String.format(
                                            "http://search.chiasenhac.vn/search.php?s=%s&mode=artist&order=quality&cat=music&page=%d",
                                            query,
                                            MusicResource.Search_SINGER_PAGE);

                            doLoadMore(search_singer_page_url);

                            MusicResource.Search_SINGER_PAGE++;
                        }
                    });
                }
            }

        }
    };

    public void doLoadMore(String loadMoreUrl) {
        if (MusicResource.network_state) {
            csn_search_singer_loadMore = new CSN_Search_Singer_LoadMore();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                csn_search_singer_loadMore.executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        loadMoreUrl);
            } else {
                csn_search_singer_loadMore.execute(loadMoreUrl);
            }
        }
    }

    private class CSN_Search_Singer_LoadMore extends AsyncTask<String, Void, Void> {

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
                                    MusicResource.CSNSearchSingerResultTemp.add(new Track(title, artist, url, duration, quality));
                                } else {
                                    if (title != null)
                                        Log.d("TestCSNSearch", title + " - " + artist);
                                    //csn_search_result.add(new Track(title, artist, url));
                                    MusicResource.CSNSearchSingerResultTemp.add(new Track(title, artist, url));
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
            /*if (MusicResource.CSNSearchSingerResultTemp.size() > 25)
                MusicResource.CSNSearchSingerResultTemp.remove(MusicResource.CSNSearchSingerResultTemp.size() - 26);
            else
                MusicResource.CSNSearchSingerResultTemp.remove(MusicResource.CSNSearchSingerResultTemp.size() - 1);*/
            MusicResource.CSNSearchSingerResultTemp.removeAll(Collections.singleton(null));
            adapter.notifyItemRemoved(MusicResource.CSNSearchSingerResultTemp.size());
            getActivity().sendBroadcast(new Intent("noti_loadmore_csn_search_singer_done"));
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
                                    MusicResource.CSNSearchSingerResultTemp.add(new Track(title, artist, url, duration, quality));
                                } else {
                                    if (title != null)
                                        Log.d("TestCSNSearch", title + " - " + artist);
                                    //csn_search_result.add(new Track(title, artist, url));
                                    MusicResource.CSNSearchSingerResultTemp.add(new Track(title, artist, url));
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

            //Log.e("TestCSNSearch", "Search Complete : " + MusicResource.CSNSearchSingerResult.size());
            if (getActivity() != null) {
                getActivity().sendBroadcast(new Intent("noti_csn_search_done"));
                getActivity().sendBroadcast(new Intent("noti_csn_search_done_singer"));
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
