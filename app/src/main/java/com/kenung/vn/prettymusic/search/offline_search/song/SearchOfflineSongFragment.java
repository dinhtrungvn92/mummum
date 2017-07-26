package com.kenung.vn.prettymusic.search.offline_search.song;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kenung.vn.prettymusic.BitmapWorkerTaskForArt;
import com.kenung.vn.prettymusic.BlurBuilder;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.PlayActivity;
import com.kenung.vn.prettymusic.Player;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.module.ImageHelper;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_online.Track;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sev_user on 5/15/2017.
 */
public class SearchOfflineSongFragment extends Fragment implements View.OnClickListener {
    RecyclerView recyclerView;
    SearchOfflineSongAdapter adapter;
    ImageView search_main_background;
    ArrayList<Song> searchResultListTemp;
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
    ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_search_ofline_result_item, container, false);
        search_main_background = (ImageView) getActivity().findViewById(R.id.search_main_background);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_search_offline);
        imagePositionOffset = MusicResource.imagePosition;
        anim = new RotateAnimation(MusicResource.imagePosition - imagePositionOffset,
                360f + MusicResource.imagePosition - imagePositionOffset,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(MusicResource.imageDuration);

        String query = "";
        searchResultListTemp = new ArrayList<>();
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
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
        if (MusicResource.songList != null)
            for (Song song : MusicResource.songList) {
                if (song != null)
                    if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                        searchResultListTemp.add(song);
                    }
            }

        Collections.sort(searchResultListTemp, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });
        MusicResource.searchResultList = searchResultListTemp;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchOfflineSongAdapter(getActivity(), MusicResource.searchResultList);
        recyclerView.setAdapter(adapter);
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
            MusicResource.songListOnlinePlay = MusicResource.songListOnline;
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MusicResource.isMainPause = true;
        Log.d("onResume", "SearchOfflineSongFragment");
        IntentFilter noti_play_intent = new IntentFilter("noti_play_intent_action");
        IntentFilter noti_next_intent = new IntentFilter("noti_next_intent_action");
        IntentFilter noti_back_intent = new IntentFilter("noti_back_intent_action");
        getActivity().registerReceiver(receiverNotiPlay, noti_play_intent);
        getActivity().registerReceiver(receiverNotiNext, noti_next_intent);
        getActivity().registerReceiver(receiverNotiBack, noti_back_intent);
        IntentFilter intentFilterSearchQueryChange = new IntentFilter("noti_searchquery_change");
        getActivity().registerReceiver(receiverSearchQueryChange, intentFilterSearchQueryChange);
        IntentFilter OnclickSongItemSearchOfflineDone = new IntentFilter("OnclickSongItemSearchOfflineDone");
        getActivity().registerReceiver(onItemClickReceiver, OnclickSongItemSearchOfflineDone);


        IntentFilter noti_audiofocus_change = new IntentFilter("noti_audiofocus_change");
        getActivity().registerReceiver(noti_audiofocus_changeReceiver, noti_audiofocus_change);

        IntentFilter intentFilterDoShuffleOnPlayActivity = new IntentFilter("intentFilterDoShuffleOnPlayActivity");
        getActivity().registerReceiver(receiverDoShuffleOnPlayActivity, intentFilterDoShuffleOnPlayActivity);
        IntentFilter intentFilterDoNextOnPlayActivity = new IntentFilter("intentFilterDoNextOnPlayActivity");
        getActivity().registerReceiver(receiverDoNextOnPlayActivity, intentFilterDoNextOnPlayActivity);
        IntentFilter intentFilterDoBackOnPlayActivity = new IntentFilter("intentFilterDoBackOnPlayActivity");
        getActivity().registerReceiver(receiverDoBackOnPlayActivity, intentFilterDoBackOnPlayActivity);
        IntentFilter OnItemOnlineClick = new IntentFilter("OnItemOnlineClick");
        getActivity().registerReceiver(OnItemOnlineClickReceiver, OnItemOnlineClick);
        IntentFilter OnItemOnlineClickDone = new IntentFilter("OnItemOnlineClickDone");
        getActivity().registerReceiver(OnItemOnlineClickDoneReceiver, OnItemOnlineClickDone);

        IntentFilter delete_track_done = new IntentFilter("delete_track_done");
        getActivity().registerReceiver(delete_track_done_receiver, delete_track_done);
        dochange();
    }

    BroadcastReceiver delete_track_done_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            searchResultListTemp.remove(MusicResource.song_deleted);
            MusicResource.searchResultList = searchResultListTemp;
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiverNotiPlay);
        getActivity().unregisterReceiver(receiverNotiNext);
        MusicResource.isMainPause = false;
    }

    private BroadcastReceiver OnItemOnlineClickDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.GONE);
            dochange();
        }
    };
    private BroadcastReceiver OnItemOnlineClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.VISIBLE);
        }
    };

    private BroadcastReceiver onItemClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.MODE = 4;

            subPlaySong.setVisibility(View.VISIBLE);
            MusicResource.subPlaySongVisible = true;
            if (MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {
                loadBitmap(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), subArt);
                MusicResource.blurredBitmap = BlurBuilder.blur(getContext(),
                        MusicResource.decodeSampledBitmapFromUri(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), 512, 512));

                MusicResource.ImageViewAnimatedChange(getContext(), search_main_background, MusicResource.blurredBitmap);


            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.nice_view_main_background_2);
                MusicResource.subArt = ((BitmapDrawable) drawable).getBitmap();
                MusicResource.noti_art = ImageHelper.getRoundedCornerBitmap(MusicResource.subArt, 30);
                subArt.setImageBitmap(MusicResource.subArt);
                Bitmap bitmapResized = Bitmap.createScaledBitmap(MusicResource.subArt, 200, 200, false);
                MusicResource.blurredBitmap = BlurBuilder.blur(getActivity(),
                        bitmapResized);

                MusicResource.ImageViewAnimatedChange(getContext(), search_main_background, MusicResource.blurredBitmap);
            }
            sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
            MusicResource.subTitle = MusicResource.searchResultList.get(MusicResource.songPosition).getTitle();
            MusicResource.subArtist = MusicResource.searchResultList.get(MusicResource.songPosition).getArtist();
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);

        }
    };
    private BroadcastReceiver receiverSearchQueryChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String query = intent.getStringExtra("SearchQuery");
            Log.d("TestReceiver", "Query - " + query);
            if (query == null) return;
            searchResultListTemp = new ArrayList<>();


            for (Song song : MusicResource.songList) {
                if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    searchResultListTemp.add(song);
                }
            }

            Collections.sort(searchResultListTemp, new Comparator<Song>() {
                @Override
                public int compare(Song lhs, Song rhs) {
                    return lhs.getTitle().compareTo(rhs.getTitle());
                }
            });
            MusicResource.searchResultList = searchResultListTemp;
            adapter = new SearchOfflineSongAdapter(getActivity(), searchResultListTemp);
            recyclerView.setAdapter(adapter);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unregisterReceiver(receiverSearchQueryChange);

    }

    public void loadBitmap(String art_uri, ImageView imageView) {
        BitmapWorkerTaskForArt task = new BitmapWorkerTaskForArt(getActivity(), imageView, 512, 512);
        task.execute(art_uri);
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
                    else doBack();
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

    public void dochange() {
        subTitle.setText(MusicResource.subTitle);
        subArtist.setText(MusicResource.subArtist);
        subArt.setImageBitmap(MusicResource.subArt);
        MusicResource.ImageViewAnimatedChange(getContext(), search_main_background, MusicResource.blurredBitmap);
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
