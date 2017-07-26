package com.kenung.vn.prettymusic.music_offline.album;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kenung.vn.prettymusic.AlbumDetailActivity;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by sev_user on 23-Dec-16.
 */
public class AlbumFragment extends Fragment {

    private Fragment albumDetaiFragment = null;
    private Album_RecyclerView_Adt adapter;

    public static AlbumFragment newInstance() {
        AlbumFragment fragment = new AlbumFragment();
        return fragment;
    }

    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.albums, container, false);
        //final GridView albumView = (GridView) view.findViewById(R.id.albumlistView);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_album);
//        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);


        getAlbumList();
        if (MusicResource.albumList != null && MusicResource.albumList.size() > 1)
            Collections.sort(MusicResource.albumList, new Comparator<Album>() {
                @Override
                public int compare(Album lhs, Album rhs) {
                    return lhs.getTitle().compareTo(rhs.getTitle());
                }
            });

        adapter = new Album_RecyclerView_Adt(getContext(), MusicResource.albumList, "offline");
        //adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);


        //AlbumAdapter albumAdt = new AlbumAdapter(view.getContext(), MusicResource.albumList);
        //albumView.setAdapter(albumAdt);

        /*albumView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), String.valueOf(MusicResource.albumList.get(position).getId()), Toast.LENGTH_SHORT).show();

                MusicResource.albumPosition = position;

                albumDetaiFragment = new AlbumDetailFragment().newInstance();

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

                ft.add(R.id.flContent, albumDetaiFragment, "albumDetailFragment").addToBackStack(null);

                final Fragment fragmentMusic = getActivity().getSupportFragmentManager().findFragmentByTag("fragmentMusic");
                if (fragmentMusic != null && fragmentMusic.isAdded()) {
                    ft.hide(fragmentMusic);
                }
                final Fragment fragmentCSN = getActivity().getSupportFragmentManager().findFragmentByTag("fragmentCSN");
                if (fragmentCSN != null && fragmentCSN.isAdded()) {
                    ft.hide(fragmentCSN);
                }
                final Fragment fragmentCSNSearch = getActivity().getSupportFragmentManager().findFragmentByTag("fragmentSearch");
                if (fragmentCSNSearch != null && fragmentCSNSearch.isAdded()) {
                    ft.hide(fragmentCSNSearch);
                }

                ft.commit();
            }
        });*/

        return view;
    }

    @Override
    public void onResume() {
        IntentFilter OnclickAlbumItem = new IntentFilter("OnclickAlbumItem");
        getActivity().registerReceiver(OnclickAlbumItemReceiver, OnclickAlbumItem);
        getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        IntentFilter getAlbumDone1 = new IntentFilter("getAlbumDone");
        getActivity().registerReceiver(getAlbumDoneReceiver1, getAlbumDone1);
        IntentFilter deleteTrackDone = new IntentFilter("delete_track_done");
        getActivity().registerReceiver(deleteTrackDoneReceiver, deleteTrackDone);
        super.onResume();
    }

    BroadcastReceiver getAlbumDoneReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("testAlbum", "here");
            Collections.sort(MusicResource.albumList, new Comparator<Album>() {
                @Override
                public int compare(Album lhs, Album rhs) {
                    return lhs.getArtist().compareTo(rhs.getArtist());
                }
            });

            adapter = new Album_RecyclerView_Adt(getContext(), MusicResource.albumList, "offline");
            //adapter.setClickListener(this);
            recyclerView.setAdapter(adapter);

        }
    };
    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    getAlbumList();
                }
            }, 4000);

        }
    };

    BroadcastReceiver deleteTrackDoneReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            for (Album album : MusicResource.albumList) {
                if (album.getId() == MusicResource.song_deleted.getAlbumid()) {
                    album.setSong_number(album.getSong_number() - 1);
                    if (album.getSong_number() <= 0) {
                        MusicResource.albumList.remove(album);
                        break;
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    };

    BroadcastReceiver OnclickAlbumItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!MusicResource.isClickAlbum) {
                MusicResource.isClickAlbum = true;
                Intent intentAlbumDetail = new Intent(getActivity(), AlbumDetailActivity.class);
                startActivity(intentAlbumDetail);
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            }
        }
    };

    public void getAlbumList() {
        MusicResource.albumList = new ArrayList<>();
        ContentResolver albumResolver = getContext().getContentResolver();
        Uri albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor albumCursor = albumResolver.query(albumUri, null, null, null, null);

        if (albumCursor != null && albumCursor.moveToFirst()) {

            int albumART = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            int albumArtist = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
            int albumId = albumCursor.getColumnIndex(MediaStore.Audio.Albums._ID);
            int albumTitle = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int albumSongNumber = albumCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            do {
                long thisId = albumCursor.getLong(albumId);
                String thisART = albumCursor.getString(albumART);
                String thisArtist = albumCursor.getString(albumArtist);
                String thisTitle = albumCursor.getString(albumTitle);
                int thisSongNumber = albumCursor.getInt(albumSongNumber);
                MusicResource.albumList.add(new Album(thisId, thisTitle, thisArtist, thisART, thisSongNumber));
            }
            while (albumCursor.moveToNext());
        }
        albumCursor.close();
        getActivity().sendBroadcast(new Intent("getAlbumDone"));
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }
    }
}
