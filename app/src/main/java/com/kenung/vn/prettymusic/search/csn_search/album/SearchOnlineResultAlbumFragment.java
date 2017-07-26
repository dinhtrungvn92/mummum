package com.kenung.vn.prettymusic.search.csn_search.album;

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
import android.widget.Toast;

import com.kenung.vn.prettymusic.AlbumDetailActivity;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.listener.OnLoadMoreListener;
import com.kenung.vn.prettymusic.music_online.Track;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by Administrator on 14/05/2017.
 */

public class SearchOnlineResultAlbumFragment extends Fragment {
    ProgressBar progressBar;
    RecyclerView rv_album;
    CSN_Search_Album_Detail csn_search_album_detail = new CSN_Search_Album_Detail();
    CSN_Search csn_search = new CSN_Search();
    TextView server_busy;
    CSN_Search_Album_RecyclerView_Adt adapter;
    String query;
    CSN_Search_Album_Detail_getSize csn_search_album_detail_getSize;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_search_online_result_item, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        server_busy = (TextView) view.findViewById(R.id.server_busy);
//        progressBar.setVisibility(View.VISIBLE);
        rv_album = (RecyclerView) view.findViewById(R.id.rv_search_online);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_album.setLayoutManager(layoutManager);

        if (MusicResource.queryOnline != null) query = MusicResource.queryOnline;

        if (MusicResource.CSNSearchAlbumResultTemp != null) {
            if (MusicResource.CSNSearchAlbumResultTemp.size() > 0) {
                adapter = new CSN_Search_Album_RecyclerView_Adt(
                        getContext(),
                        MusicResource.CSNSearchAlbumResultTemp,
                        rv_album);

                rv_album.setAdapter(adapter);

                adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        //Log.d("haint", "Load More");
                        MusicResource.CSNSearchAlbumResultTemp.add(null);
                        adapter.notifyItemInserted(MusicResource.CSNSearchAlbumResultTemp.size() - 1);

                        //Load more data for reyclerview

                        String search_album_page_url =
                                String.format(
                                        "http://search.chiasenhac.vn/search.php?s=%s&mode=album&order=quality&cat=music&page=%d",
                                        query,
                                        MusicResource.Search_ALBUM_PAGE);

                        Log.d("TestSearchAlbumLoadMore", search_album_page_url);

                        doLoadMore(search_album_page_url);

                        MusicResource.Search_ALBUM_PAGE++;
                    }
                });
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilterCSNSearchDone = new IntentFilter("noti_csn_search_done_album");
        IntentFilter intentFilterCSNSearchQuerySubmit = new IntentFilter("noti_search_online_query_submit");
        IntentFilter intentFilterCSNSearchOnClicked = new IntentFilter("noti_CSN_Search_Album_onClicked");
        IntentFilter noti_csn_search_album_selected_done = new IntentFilter("noti_csn_search_album_selected_done");
        getActivity().registerReceiver(receiverCSNSearchAlbumSelectDone, noti_csn_search_album_selected_done);
        IntentFilter noti_loadmore_csn_search_album_done = new IntentFilter("noti_loadmore_csn_search_album_done");
        IntentFilter noti_CSN_Search_Album_Item_onClicked = new IntentFilter("noti_CSN_Search_Album_Item_onClicked");

        getActivity().registerReceiver(receiverCSNSearchOnItemClicked, noti_CSN_Search_Album_Item_onClicked);
        getActivity().registerReceiver(receiverCSNSearchDone, intentFilterCSNSearchDone);
        getActivity().registerReceiver(receiverCSNSearchQuerySubmit, intentFilterCSNSearchQuerySubmit);
        getActivity().registerReceiver(receiverCSNSearchOnClicked, intentFilterCSNSearchOnClicked);
        getActivity().registerReceiver(receiverCSNSearchAlbumLoadMoreDone, noti_loadmore_csn_search_album_done);

        IntentFilter intentFilter = new IntentFilter("OnItemOnlineClickDone");
        getActivity().registerReceiver(OnItemOnlineClickDone, intentFilter);
    }

    BroadcastReceiver receiverCSNSearchAlbumLoadMoreDone = new BroadcastReceiver() {
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
    BroadcastReceiver receiverCSNSearchOnItemClicked = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            progressBar.setVisibility(View.VISIBLE);
            MusicResource.playAtSearchAlbum = true;
            MusicResource.MODE = 24;
            MusicResource.songListOnlineSearch = new ArrayList<>(MusicResource.CSNSearchAlbumSelected);
            MusicResource.songListOnlinePlay = new ArrayList<>(MusicResource.songListOnlineSearch);
            getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));
        }
    };
    BroadcastReceiver receiverCSNSearchAlbumSelectDone = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.INVISIBLE);
            MusicResource.checkAlbumOnline = true;
            Intent intentAlbumDetail = new Intent(getActivity(), AlbumDetailActivity.class);
            startActivity(intentAlbumDetail);
        }
    };
    private BroadcastReceiver receiverCSNSearchOnClicked
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!MusicResource.network_state) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!MusicResource.isClickAlbum) {
                MusicResource.isClickAlbum = true;
                progressBar.setVisibility(View.VISIBLE);
                rv_album.setEnabled(false);

                MusicResource.CSNSearchAlbumSelected = new ArrayList<Track>();
                MusicResource.set_src_complete = false;

                if (MusicResource.CSNSearchAlbumResultTemp.size() > 0) {
                    MusicResource.CSNSearchAlbumResult = new ArrayList<>(MusicResource.CSNSearchAlbumResultTemp);
                }

                csn_search_album_detail = new CSN_Search_Album_Detail();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    csn_search_album_detail.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            MusicResource.CSNSearchAlbumResult.get(MusicResource.albumPosition).getUrl());
                } else {
                    csn_search_album_detail.execute(MusicResource.CSNSearchAlbumResult.get(MusicResource.albumPosition).getUrl());
                }

                if (!MusicResource.sub_anim_running) {
                    MusicResource.sub_anim_state = true;
                    MusicResource.sub_anim_running = true;
                }
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
                MusicResource.Search_ALBUM_PAGE = 2;

            String uri = String.format("http://search.chiasenhac.vn/search.php?s=%s&mode=album&order=quality&cat=music", query);

            //MusicResource.CSNSearchAlbumResult = new ArrayList<>();
            if (adapter != null) {
                MusicResource.CSNSearchAlbumResultTemp.clear();
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

    private BroadcastReceiver receiverCSNSearchDone
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.GONE);

            if (MusicResource.CSNSearchAlbumResultTemp.size() == 0) {
                server_busy.setVisibility(View.VISIBLE);
            } else {
                server_busy.setVisibility(View.INVISIBLE);
                adapter = new CSN_Search_Album_RecyclerView_Adt(
                        getContext(),
                        MusicResource.CSNSearchAlbumResultTemp,
                        rv_album);

                rv_album.setAdapter(adapter);

                adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        //Log.d("haint", "Load More");
                        MusicResource.CSNSearchAlbumResultTemp.add(null);
                        adapter.notifyItemInserted(MusicResource.CSNSearchAlbumResultTemp.size() - 1);

                        //Load more data for reyclerview

                        String search_album_page_url =
                                String.format(
                                        "http://search.chiasenhac.vn/search.php?s=%s&mode=album&order=quality&cat=music&page=%d",
                                        query,
                                        MusicResource.Search_ALBUM_PAGE);

                        doLoadMore(search_album_page_url);

                        Log.d("TestSearchAlbumLoadMore", search_album_page_url);

                        MusicResource.Search_ALBUM_PAGE++;
                    }
                });
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (csn_search != null && csn_search.getStatus() == AsyncTask.Status.RUNNING)
            csn_search.cancel(true);
        if (csn_search_album_detail != null && csn_search_album_detail.getStatus() == AsyncTask.Status.RUNNING)
            csn_search_album_detail.cancel(true);
    }

    public void doLoadMore(String loadMoreUrl) {
        if (MusicResource.network_state) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new CSN_Search_Album_LoadMore().executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        loadMoreUrl);
            } else {
                new CSN_Search_Album_LoadMore().execute(loadMoreUrl);
            }
        }
    }

    private class CSN_Search_Album_LoadMore extends AsyncTask<String, Void, Void> {

        private int added_number = 1;

        @Override
        protected Void doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0]).userAgent("Chrome/59.0.3071.115").get();
                if (document == null) return null;
                Element table = document.select("table.tbtable").first();
                if (table == null) return null;
                ArrayList<String> urls = new ArrayList<>();
                ArrayList<String> art_srcs = new ArrayList<>();

                Elements search_detail = table.select("tr");

                if (search_detail == null && search_detail.size() <= 0) return null;
                //Log.d("TestSearchAlbum", "search_detail - " + search_detail.size());

                int i = 0;
                int row = 0;
                for (Element item_detail : search_detail) {

                    Elements each_row = item_detail.select("td");
                    if (each_row == null || each_row.size() <= 0) return null;
                    //Log.d("TestSearchAlbum", "SearchOK : " + i + " - " + each_row.size());

                    int j = 0;
                    for (Element info : each_row) {
                        if (i % 2 == 0) {
                            Element url = info.select("a").first();
                            if (url == null) {
                                urls.add("");
                                art_srcs.add("");
                                j++;
                                //Log.d("TestSearchAlbum", "albumNotFound");
                                continue;
                            }
                            Element art_src = info.select("img").first();
                            urls.add(url.attr("href"));
                            art_srcs.add(art_src.attr("src"));
                            Log.d("TestSearchAlbum", "addInfo - " + j);
                        } else {
                            String title = info.select("a").first().text();
                            String artist = info.html();
                            artist = artist.split("<br>")[1];

                            int size = urls.size();

                            if (urls.get(size - 3 + j).equals("") || art_srcs.get(size - 3 + j).equals("")) {
                            } else {
                                added_number++;
                                MusicResource.CSNSearchAlbumResultTemp
                                        .add(new AlbumModelSearch(title, artist, urls.get(size - 3 + j), art_srcs.get(size - 3 + j)));
                            }
                            Log.d("TestSearchAlbum", j + " - addNewAlbum Done - " + row);
                            Log.d("TestSearchAlbum", "title : " + title + " - artist : " + artist);
                        }
                        j++;
                    }
                    i++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (MusicResource.CSNSearchAlbumResultTemp.size() > 10)
                MusicResource.CSNSearchAlbumResultTemp.remove(MusicResource.CSNSearchAlbumResultTemp.size() - added_number);
            else
                MusicResource.CSNSearchAlbumResultTemp.remove(MusicResource.CSNSearchAlbumResultTemp.size() - 1);

            adapter.notifyItemRemoved(MusicResource.CSNSearchAlbumResultTemp.size());
            getActivity().sendBroadcast(new Intent("noti_loadmore_csn_search_album_done"));
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
                Document document = Jsoup.connect(params[0]).userAgent("Chrome/59.0.3071.115").get();
                if (document == null) return null;
                Element table = document.select("table.tbtable").first();
                if (table == null) return null;
                ArrayList<String> urls = new ArrayList<>();
                ArrayList<String> art_srcs = new ArrayList<>();

                Elements search_detail = table.select("tr");

                if (search_detail == null && search_detail.size() <= 0) return null;
                //Log.d("TestSearchAlbum", "search_detail - " + search_detail.size());

                int i = 0;
                int row = 0;
                for (Element item_detail : search_detail) {

                    Elements each_row = item_detail.select("td");
                    if (each_row == null || each_row.size() <= 0) return null;
                    //Log.d("TestSearchAlbum", "SearchOK : " + i + " - " + each_row.size());

                    int j = 0;
                    for (Element info : each_row) {
                        if (i % 2 == 0) {
                            Element url = info.select("a").first();
                            if (url == null) {
                                urls.add("");
                                art_srcs.add("");
                                j++;
                                //Log.d("TestSearchAlbum", "albumNotFound");
                                continue;
                            }
                            Element art_src = info.select("img").first();
                            urls.add(url.attr("href"));
                            art_srcs.add(art_src.attr("src"));
                            //Log.d("TestSearchAlbum", "addInfo - " + j);
                        } else {
                            String title = info.select("a").first().text();
                            String artist = info.html();
                            artist = artist.split("<br>")[1];

                            int size = urls.size();

                            if (urls.get(size - 3 + j).equals("") || art_srcs.get(size - 3 + j).equals("")) {
                            } else
                                MusicResource.CSNSearchAlbumResultTemp
                                        .add(new AlbumModelSearch(title, artist, urls.get(size - 3 + j), art_srcs.get(size - 3 + j)));

                            //Log.d("TestSearchAlbum", j + " - addNewAlbum Done - " + row);
                            //Log.d("TestSearchAlbum", "title : " + title + " - artist : " + artist);
                        }
                        j++;
                    }
                    i++;
                }
                //Log.d("TestSearchAlbum", "art size : " + art_srcs.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            //Log.e("TestCSNSearch", "Search Complete : " + MusicResource.CSNSearchAlbumResult.size());
            if (getActivity() != null) {
                getActivity().sendBroadcast(new Intent("noti_csn_search_done_album"));
                getActivity().sendBroadcast(new Intent("noti_csn_search_done"));
            }
            int albumPosition = 0;
            for (AlbumModelSearch albumModelSearch : MusicResource.CSNSearchAlbumResultTemp) {
                csn_search_album_detail_getSize = new CSN_Search_Album_Detail_getSize(albumPosition);
                if (!csn_search_album_detail_getSize.isCancelled())
                    csn_search_album_detail_getSize.executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            albumModelSearch.getUrl()
                    );
                albumPosition++;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private class CSN_Search_Album_Detail extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0]).userAgent("Chrome/59.0.3071.115").get();
                if (document == null) return null;
                Elements playlist = document.select("tr");
                if (playlist == null) return null;
                for (Element e : playlist) {
                    Element element = e.select("td").last();
                    if (element == null) continue;
                    Elements info = element.select("a");
                    if (info == null || info.size() <= 0) continue;
                    if (info.size() == 3) {

                        String url = info.last().attr("href");
                        String title = info.last().text();
                        String artist = element.select("span").first().text();
                        String download_url = info.first().attr("href");
                        Track track = new Track(title, artist, url, download_url);
                        MusicResource.CSNSearchAlbumSelected.add(track);
                        Log.d("testAlbum", track.getTitle() + "\n" + track.getArtist() + "\n" + track.getUrl() + "\n" + track.getDownload_url());
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
            if (getActivity() != null) {
                getActivity().sendBroadcast(new Intent("noti_csn_search_album_selected_done"));
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private class CSN_Search_Album_Detail_getSize extends AsyncTask<String, Void, Void> {

        private int albumPosition;
        private int albumSize = 0;

        public CSN_Search_Album_Detail_getSize(int albumPosition) {
            this.albumPosition = albumPosition;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0]).userAgent("Chrome/59.0.3071.115").get();
                if (document == null) return null;
                Elements playlist = document.select("tr");
                if (playlist == null) return null;
                for (Element e : playlist) {
                    Element element = e.select("td").last();
                    if (element == null) continue;
                    Elements info = element.select("a");
                    if (info == null || info.size() <= 0) continue;
                    if (info.size() == 3) {

                        albumSize++;
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

            //getActivity().sendBroadcast(new Intent("noti_csn_search_album_selected_done"));
            if (MusicResource.CSNSearchAlbumResultTemp.size() > 0)
                MusicResource.CSNSearchAlbumResultTemp.get(albumPosition).setSize(albumSize);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

}
