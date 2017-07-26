package com.kenung.vn.prettymusic.music_offline.playlist;

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
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kenung.vn.prettymusic.AlbumDetailActivity;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.music_offline.album.Album;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistAdapter;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineAdapter;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;

import java.util.ArrayList;

/**
 * Created by KXPRO on 5/11/2017.
 */

public class PlaylistOfflineFragment extends Fragment {

    private LinearLayout create_playlist;
    private PlaylistAdapter adapter;
    private RecyclerView playlist_rv;

    static PlaylistOfflineFragment fragment = new PlaylistOfflineFragment();

    public static PlaylistOfflineFragment newInstance() {
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter AddToPlayListSuccess = new IntentFilter("AddToPlayListOfflineSuccess");
        getActivity().registerReceiver(AddToPlayListSuccessReceiver, AddToPlayListSuccess);
        IntentFilter OnclickPlaylistOfflineItem = new IntentFilter("OnclickPlaylistOfflineItem");
        getActivity().registerReceiver(OnclickPlaylistOfflineItemReceiver, OnclickPlaylistOfflineItem);
        IntentFilter noti_CSN_Play_List_Item_onClicked = new IntentFilter("noti_CSN_Play_List_Item_onClicked");
        getActivity().registerReceiver(noti_CSN_Play_List_Item_onClickedReceiver, noti_CSN_Play_List_Item_onClicked);
        IntentFilter playlist_delete = new IntentFilter("playlist_offline_delete");
        getActivity().registerReceiver(playlist_delete_receiver, playlist_delete);
        IntentFilter track_removed = new IntentFilter("song_removed");
        getActivity().registerReceiver(track_removed_receiver, track_removed);
        IntentFilter playlist_rename = new IntentFilter("playlist_offline_rename");
        getActivity().registerReceiver(playlist_rename_receiver, playlist_rename);
        IntentFilter delete_track_done = new IntentFilter("delete_track_done");
        getActivity().registerReceiver(delete_track_done_receiver, delete_track_done);
    }

    BroadcastReceiver delete_track_done_receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            for (PlaylistOnlineModel playlist : MusicResource.playlistOffline) {
                if (MusicResource.hashMapPlaylistOffline
                        .get(playlist.getTitle()).contains(MusicResource.song_deleted)) {
                    playlist.setSize(playlist.getSize() - 1);
                    if (playlist.getSize() <= 0) {
                        MusicResource.playlistOffline.remove(playlist);
                        break;
                    }
                }
            }
            adapter.notifyDataSetChanged();
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
            MusicResource.playlistOffline.remove(MusicResource.playlist_deleted);
            adapter.notifyDataSetChanged();
        }
    };

    BroadcastReceiver noti_CSN_Play_List_Item_onClickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*MusicResource.MODE = 26;
            MusicResource.playAtSearchAlbum = false;
            MusicResource.songListOnlinePlay = MusicResource.hashMapPlaylistOffline.get(MusicResource.playListSelectedTitle);
            MusicResource.songListOnlinePlayList = MusicResource.hashMapPlaylistOffline.get(MusicResource.playListSelectedTitle);
            getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));*/
        }
    };
    BroadcastReceiver OnclickPlaylistOfflineItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.checkPlaylistOfflineUse = true;
            Intent intentAlbumDetail = new Intent(getActivity(), AlbumDetailActivity.class);
            startActivity(intentAlbumDetail);
        }
    };
    BroadcastReceiver AddToPlayListSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter = new PlaylistAdapter(getContext(), MusicResource.playlistOffline, "playlist_offline");
            playlist_rv.setAdapter(adapter);
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_playlist_fragment, container, false);

        playlist_rv = (RecyclerView) view.findViewById(R.id.playlist_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        playlist_rv.setLayoutManager(layoutManager);
        playlist_rv.setHasFixedSize(true);
        adapter = new PlaylistAdapter(getContext(), MusicResource.playlistOffline, "playlist_offline");
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
                                    for (PlaylistOnlineModel p : MusicResource.playlistOffline) {
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
                                    MusicResource.playlistOffline.add(new PlaylistOnlineModel(input.toString()));
                                    MusicResource.hashMapPlaylistOffline.put(
                                            input.toString(),
                                            new ArrayList<Song>());
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