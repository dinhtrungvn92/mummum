package com.kenung.vn.prettymusic.music_online.playlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.kenung.vn.prettymusic.AlbumDetailActivity;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.ServerRequest;

/**
 * Created by Administrator on 16/06/2017.
 */

public class SharePlaylistFragment extends Fragment {
    private PlaylistAdapter adapter;
    private RecyclerView playlist_rv;
    private SwipeRefreshLayout refreshLayout;
    ProgressBar progressBar;

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter GetAllPlayListDone = new IntentFilter("GetAllPlayListDone");
        getActivity().registerReceiver(GetAllPlayListDoneReceiver, GetAllPlayListDone);
        IntentFilter OnclickPlaylistShareItem = new IntentFilter("OnclickPlaylistShareItem");
        getActivity().registerReceiver(OnclickPlaylistShareItemReceiver, OnclickPlaylistShareItem);
        IntentFilter noti_CSN_Share_Play_List_Item_onClicked = new IntentFilter("noti_CSN_Share_Play_List_Item_onClicked");
        getActivity().registerReceiver(noti_CSN_Share_Play_List_Item_onClickedReceiver, noti_CSN_Share_Play_List_Item_onClicked);

        IntentFilter OnclickPlaylistShareItemStart = new IntentFilter("OnclickPlaylistShareItemStart");
        getActivity().registerReceiver(OnclickPlaylistShareItemStartReceiver, OnclickPlaylistShareItemStart);
        IntentFilter AddToMyPlaylist = new IntentFilter("AddToMyPlaylist");
        getActivity().registerReceiver(AddToMyPlaylistReceiver, AddToMyPlaylist);

        IntentFilter AddToPlayListSuccess = new IntentFilter("AddToPlayListSuccess");
        getActivity().registerReceiver(AddToPlayListSuccessReceiver, AddToPlayListSuccess);

    }


    BroadcastReceiver AddToPlayListSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    };

    BroadcastReceiver AddToMyPlaylistReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.VISIBLE);
        }
    };

    BroadcastReceiver GetAllPlayListDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter = new PlaylistAdapter(getContext(), MusicResource.playlistOnlineShare, "share_playlist");
            playlist_rv.setAdapter(adapter);
            refreshLayout.setRefreshing(false);
            MusicResource.isRefreshingSharePlaylist = false;
        }
    };
    BroadcastReceiver OnclickPlaylistShareItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.checkPlaylistShareUse = true;
            progressBar.setVisibility(View.INVISIBLE);

            Intent intentAlbumDetail = new Intent(getActivity(), AlbumDetailActivity.class);
            startActivity(intentAlbumDetail);
            refreshLayout.setEnabled(true);
        }
    };

    BroadcastReceiver noti_CSN_Share_Play_List_Item_onClickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.MODE = 26;
            MusicResource.playAtSearchAlbum = false;
            MusicResource.songListOnlinePlay = MusicResource.listSongPlaylistShare;
            getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));
            Log.d("UpdateViewCount", MusicResource.updateCountView + "");
            if (MusicResource.updateCountView) {
                new ServerRequest.UpdateViewCount(MusicResource.playlistOnlineShare.get(MusicResource.playlistPosition).getPlaylist_id(), context).execute();
                MusicResource.updateCountView = false;
            }
        }
    };

    BroadcastReceiver OnclickPlaylistShareItemStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.VISIBLE);
            refreshLayout.setEnabled(false);

        }
    };


    public void doRefresh() {
        if (MusicResource.network_state) {
            MusicResource.isRefreshingSharePlaylist = true;
            new ServerRequest.GetAllPlayList(getActivity()).execute();
        } else {
            refreshLayout.setRefreshing(false);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.share_playlist_fragment, container, false);
        new ServerRequest.GetAllPlayList(getActivity()).execute();
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        playlist_rv = (RecyclerView) view.findViewById(R.id.playlist_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        playlist_rv.setLayoutManager(layoutManager);
        playlist_rv.setHasFixedSize(true);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });
        return view;
    }
}
