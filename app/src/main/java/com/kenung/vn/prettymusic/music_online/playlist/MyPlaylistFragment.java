package com.kenung.vn.prettymusic.music_online.playlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kenung.vn.prettymusic.AlbumDetailActivity;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.music_online.Track;

import java.util.ArrayList;

/**
 * Created by Administrator on 16/06/2017.
 */

public class MyPlaylistFragment extends Fragment {
    private LinearLayout create_playlist;
    private PlaylistAdapter adapter;
    private RecyclerView playlist_rv;
    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter AddToPlayListSuccess = new IntentFilter("AddToPlayListSuccess");
        getActivity().registerReceiver(AddToPlayListSuccessReceiver, AddToPlayListSuccess);
        IntentFilter OnclickPlaylistOnlineItem = new IntentFilter("OnclickPlaylistOnlineItem");
        getActivity().registerReceiver(OnclickPlaylistOnlineItemReceiver, OnclickPlaylistOnlineItem);
        IntentFilter noti_CSN_Play_List_Item_onClicked = new IntentFilter("noti_CSN_Play_List_Item_onClicked");
        getActivity().registerReceiver(noti_CSN_Play_List_Item_onClickedReceiver, noti_CSN_Play_List_Item_onClicked);

        IntentFilter playlist_delete = new IntentFilter("playlist_delete");
        getActivity().registerReceiver(playlist_delete_receiver, playlist_delete);
        IntentFilter track_removed = new IntentFilter("track_removed");
        getActivity().registerReceiver(track_removed_receiver, track_removed);
        IntentFilter playlist_rename = new IntentFilter("playlist_rename");
        getActivity().registerReceiver(playlist_rename_receiver, playlist_rename);

        IntentFilter SharePlaylist = new IntentFilter("SharePlaylist");
        getActivity().registerReceiver(SharePlaylistReceiver, SharePlaylist);

        IntentFilter SharePlaylistDone = new IntentFilter("SharePlaylistDone");
        getActivity().registerReceiver(SharePlaylistDoneReceiver, SharePlaylistDone);

    }

    BroadcastReceiver SharePlaylistReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.VISIBLE);
        }
    };
    BroadcastReceiver SharePlaylistDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    };

    BroadcastReceiver playlist_rename_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.notifyDataSetChanged();
        }
    };

    BroadcastReceiver track_removed_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.notifyDataSetChanged();
        }
    };

    BroadcastReceiver playlist_delete_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.playlistOnline.remove(MusicResource.playlist_deleted);
            adapter.notifyDataSetChanged();
        }
    };

    BroadcastReceiver noti_CSN_Play_List_Item_onClickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.MODE = 25;
            MusicResource.playAtSearchAlbum = false;
            MusicResource.songListOnlinePlay = new ArrayList<>(MusicResource.hashMapPlaylistOnline.get(MusicResource.playListSelectedTitle));
            MusicResource.songListOnlinePlayList = new ArrayList<>(MusicResource.hashMapPlaylistOnline.get(MusicResource.playListSelectedTitle));
            getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));
        }
    };
    BroadcastReceiver OnclickPlaylistOnlineItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.checkPlaylistUse = true;
            Intent intentAlbumDetail = new Intent(getActivity(), AlbumDetailActivity.class);
            startActivity(intentAlbumDetail);
        }
    };
    BroadcastReceiver AddToPlayListSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter = new PlaylistAdapter(getContext(), MusicResource.playlistOnline, "playlist_online");
            playlist_rv.setAdapter(adapter);
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_playlist_fragment, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        playlist_rv = (RecyclerView) view.findViewById(R.id.playlist_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        playlist_rv.setLayoutManager(layoutManager);
        playlist_rv.setHasFixedSize(true);
        adapter = new PlaylistAdapter(getContext(), MusicResource.playlistOnline, "playlist_online");
        playlist_rv.setAdapter(adapter);

        create_playlist = (LinearLayout) view.findViewById(R.id.create_playlist);
        create_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(getActivity())
                        .title(getActivity()
                                .getResources().getString(R.string.create_playlist))
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(getActivity().getResources().getString(R.string.input_playlits_name), "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                if (!input.toString().replaceAll(" ", "").equals("")) {
                                    for (PlaylistOnlineModel p : MusicResource.playlistOnline) {
                                        if (p.getTitle()
                                                .replaceFirst(input.toString(), "")
                                                .replaceAll(" ", "")
                                                .equals("") || input.toString()
                                                .replaceFirst(p.getTitle(), "")
                                                .replaceAll(" ", "")
                                                .equals("")) {
                                            Toast.makeText(getContext(),
                                                    getResources().getString(R.string.playlist_exist),
                                                    Toast.LENGTH_LONG)
                                                    .show();
                                            return;
                                        }
                                    }
                                    MusicResource.playlistOnline.add(new PlaylistOnlineModel(input.toString()));
                                    MusicResource.hashMapPlaylistOnline.put(
                                            input.toString(),
                                            new ArrayList<Track>());
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(getContext(),
                                            getResources().getString(R.string.playlist_null),
                                            Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        }).show();
            }
        });

        return view;
    }
}
