package com.kenung.vn.prettymusic.music_offline.folder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kenung.vn.prettymusic.AlbumDetailActivity;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;

/**
 * Created by sev_user on 23-Dec-16.
 */
public class FolderFragment extends Fragment {
    FolderAdapter adapter;

    public static FolderFragment newInstance() {
        FolderFragment fragment = new FolderFragment();
        return fragment;
    }

    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.folder_fragment, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_folder);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onResume() {

        super.onResume();

        IntentFilter getSonglistDone = new IntentFilter("getSongListDone");
        getActivity().registerReceiver(getSonglistDoneReceiver, getSonglistDone);
        IntentFilter OnclickFolderItem = new IntentFilter("OnclickFolderItem");
        getActivity().registerReceiver(OnclickFolderItemReceiver, OnclickFolderItem);
        IntentFilter delete_track_done = new IntentFilter("delete_track_done");
        getActivity().registerReceiver(delete_track_done_receiver, delete_track_done);
    }

    BroadcastReceiver delete_track_done_receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            for (FolderModel folder : MusicResource.folderList) {
                if (MusicResource.hashMapFolderList
                        .get(folder.getFolder_path()).contains(MusicResource.song_deleted)) {
                    folder.setFolder_size(folder.getFolder_size() - 1);
                    if (folder.getFolder_size() <= 0) {
                        MusicResource.folderList.remove(folder);
                        break;
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    };

    BroadcastReceiver OnclickFolderItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.isClickFolder = true;
            Intent intentAlbumDetail = new Intent(getActivity(), AlbumDetailActivity.class);
            startActivity(intentAlbumDetail);

        }
    };
    BroadcastReceiver getSonglistDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter = new FolderAdapter(getActivity(), MusicResource.folderList);
            recyclerView.setAdapter(adapter);
        }
    };
}