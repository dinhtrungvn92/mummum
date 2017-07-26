package com.kenung.vn.prettymusic.music_offline.downloaded;

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
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.music_offline.song.Song;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sev_user on 23-Dec-16.
 */
public class DownLoadedFragment extends Fragment {
    private LinearLayout subPlaySong;
    private RecyclerView recyclerView;
    private CircleImageView subArt;
    private TextView subSongTitle;
    private ImageView main_background;
    private Button sub_play_btn;
    private RotateAnimation anim;
    private Calendar calendar;
    private float imagePositionOffset;
    public long mLastClickTime = 0;
    TextView subArtist;
    TextView subTitle;
    private SongDownloadedAdapter songAdt;

    public static DownLoadedFragment newInstance() {
        DownLoadedFragment fragment = new DownLoadedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter getSongListDone = new IntentFilter("getSongListDone");
        getActivity().registerReceiver(getSongListDoneReceiver, getSongListDone);
        IntentFilter deleteTrackDone = new IntentFilter("delete_track_done");
        getActivity().registerReceiver(deleteTrackDoneReceiver, deleteTrackDone);
    }

    BroadcastReceiver deleteTrackDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.songListDownload.remove(MusicResource.song_deleted);
            songAdt.notifyDataSetChanged();
        }
    };

    BroadcastReceiver getSongListDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Collections.sort(MusicResource.songListDownload, new Comparator<Song>() {
                @Override
                public int compare(Song lhs, Song rhs) {
                    return lhs.getTitle().compareTo(rhs.getTitle());
                }
            });

            songAdt = new SongDownloadedAdapter(getContext(), MusicResource.songListDownload, "");
            recyclerView.setAdapter(songAdt);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.downloaded, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.bxh_vn_view);
        subArt = (CircleImageView) getActivity().findViewById(R.id.subArt);
        subTitle = (TextView) getActivity().findViewById(R.id.subTitle);
        subArtist = (TextView) getActivity().findViewById(R.id.subArtist);
        subTitle.setSelected(true);
        subArtist.setSelected(true);
        main_background = (ImageView) getActivity().findViewById(R.id.main_background);
        sub_play_btn = (Button) getActivity().findViewById(R.id.subPlaybtn);
        subPlaySong = (LinearLayout) getActivity().findViewById(R.id.subPlaySong);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        Collections.sort(MusicResource.songListDownload, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });

        songAdt = new SongDownloadedAdapter(getContext(), MusicResource.songListDownload, "");
        recyclerView.setAdapter(songAdt);

        return view;
    }
}
