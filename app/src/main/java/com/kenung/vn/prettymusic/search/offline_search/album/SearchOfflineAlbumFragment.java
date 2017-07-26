package com.kenung.vn.prettymusic.search.offline_search.album;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.music_offline.album.Album;
import com.kenung.vn.prettymusic.music_offline.album.Album_RecyclerView_Adt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by sev_user on 5/15/2017.
 */
public class SearchOfflineAlbumFragment extends Fragment {
    RecyclerView recyclerView;
    Album_RecyclerView_Adt adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_search_ofline_result_item, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_search_offline);
        String query = "";
        ArrayList<Album> albumListResultTemp = new ArrayList<Album>();
        if (MusicResource.albumList != null)
            for (Album album : MusicResource.albumList) {
                if (album != null)
                    if (album.getTitle().toLowerCase().contains(query.toLowerCase())) {
                        albumListResultTemp.add(album);
                    }
            }
        MusicResource.albumListResult = albumListResultTemp;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new Album_RecyclerView_Adt(getContext(), MusicResource.albumListResult, "search");
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilterSearchQueryChange = new IntentFilter("noti_searchquery_change");
        getActivity().registerReceiver(receiverSearchQueryChange, intentFilterSearchQueryChange);
        IntentFilter delete_track_done = new IntentFilter("delete_track_done");
        getActivity().registerReceiver(delete_track_done_receiver, delete_track_done);
    }

    BroadcastReceiver delete_track_done_receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            adapter.notifyDataSetChanged();
        }
    };

    private BroadcastReceiver receiverSearchQueryChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String query = intent.getStringExtra("SearchQuery");
            if (query == null) return;
            ArrayList<Album> albumListResultTemp = new ArrayList<Album>();

            for (Album album : MusicResource.albumList) {
                if (album.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    albumListResultTemp.add(album);
                }
            }
            Collections.sort(albumListResultTemp, new Comparator<Album>() {
                @Override
                public int compare(Album lhs, Album rhs) {
                    return lhs.getTitle().compareTo(rhs.getTitle());
                }
            });
            MusicResource.albumListResult = albumListResultTemp;
            adapter = new Album_RecyclerView_Adt(getContext(), MusicResource.albumListResult, "search");
            recyclerView.setAdapter(adapter);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unregisterReceiver(receiverSearchQueryChange);

    }

}
