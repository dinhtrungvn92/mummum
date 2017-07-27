package com.kenung.vn.prettymusic.music_offline.album;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.kenung.vn.prettymusic.BitmapTrackWorkerTask;
import com.kenung.vn.prettymusic.BitmapWorkerTask;
import com.kenung.vn.prettymusic.BlurBuilder;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.PlayActivity;
import com.kenung.vn.prettymusic.Player;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.module.ImageHelper;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_offline.song.SongAdapter;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.search.csn_search.CSN_Search_RecyclerView_Adt;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sev_user on 06-Apr-17.
 */

public class AlbumDetailFragment extends Fragment implements View.OnClickListener {

    private ImageView album_detail_art;
    private RecyclerView album_detail_lv;
    private ImageView main_background;
    private RotateAnimation anim;
    private float imagePositionOffset;
    private TextView album_detail_title, album_detail_artist;
    ArrayList<Song> listSongCollection;
    private CircleImageView subArt;
    private ImageButton sub_Next_btn;
    private ImageButton sub_Back_btn;
    private Button sub_Play_btn;
    private TextView subTitle;
    private TextView subArtist;
    private LinearLayout subPlaySong;
    private Calendar calendar;
    private long mLastClickTime = 0;
    ProgressBar progressBar;
    private AdView avBanner;
    private AdRequest adRequest;
    private boolean checkAd = false;
    static RequestManager glide;
    private CSN_Search_RecyclerView_Adt playlist_adapter;
    private SongAdapter playlistOfflineAdapter;
    private AlbumDetailAdapter albumOfflineDetailAdapter;
    private SongAdapter folderDetailAdapter;

    @SuppressLint("ValidFragment")
    public AlbumDetailFragment() {
    }

    public static AlbumDetailFragment newInstance(RequestManager glideInput) {
        glide = glideInput;
        return new AlbumDetailFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        MusicResource.isMainPause = false;
        MusicResource.checkAlbumOnline = false;
        getActivity().unregisterReceiver(receiverNotiPlay);
        getActivity().unregisterReceiver(receiverNotiNext);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MusicResource.albumCollection = new ArrayList<>();

        imagePositionOffset = MusicResource.imagePosition;

        anim = new RotateAnimation(MusicResource.imagePosition - imagePositionOffset,
                360f + MusicResource.imagePosition - imagePositionOffset,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(MusicResource.imageDuration);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.album_detail, container, false);
        avBanner = (AdView) view.findViewById(R.id.av_banner);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
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
        album_detail_art = (ImageView) view.findViewById(R.id.album_detail_art);
        album_detail_lv = (RecyclerView) view.findViewById(R.id.album_detail_list);
        main_background = (ImageView) getActivity().findViewById(R.id.search_main_background);
        album_detail_title = (TextView) view.findViewById(R.id.album_detail_title);
        album_detail_artist = (TextView) view.findViewById(R.id.album_detail_artist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        album_detail_lv.setLayoutManager(layoutManager);

        if (MusicResource.isClickFolder) {
            album_detail_title.setText(MusicResource.folderList.get(MusicResource.folderPositon).getFolder_name());
            album_detail_artist.setText(MusicResource.hashMapFolderList.get(MusicResource.folderSelectedTitle).size() + " " + getString(R.string.track));
            Bitmap bmp = ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_thumbnail_default), 30);
            album_detail_art.setImageBitmap(bmp);

            String art_uri = MusicResource.hashMapFolderList.get(MusicResource.folderSelectedTitle)
                    .get(0).getArt_uri();

            if (art_uri != null) {
                loadBitmap(art_uri, album_detail_art);
            }

            folderDetailAdapter = new SongAdapter(getContext(),
                    MusicResource.hashMapFolderList.get(MusicResource.folderSelectedTitle), "folder");
            album_detail_lv.setAdapter(folderDetailAdapter);


            if (MusicResource.hashMapFolderList.get(MusicResource.folderSelectedTitle).size() <= 4)
                checkAd = true;

        } else if (MusicResource.checkPlaylistShareUse) {
            MusicResource.updateCountView = true;
            Log.d("UpdateViewCount", MusicResource.updateCountView + "");
            album_detail_title.setText(MusicResource.playListSelectedTitle);
            album_detail_artist.setText(MusicResource.playlistOnlineShare.get(MusicResource.playlistPosition).getSize() + " " + getString(R.string.track));
            loadBitmap(MusicResource.listSongPlaylistShare.get(0).getUrl(),
                    album_detail_art, progressBar, -2, -2);
            playlist_adapter = new CSN_Search_RecyclerView_Adt(
                    getActivity(),
                    MusicResource.listSongPlaylistShare,
                    "shareplaylist",
                    Glide.with(getActivity()),
                    album_detail_lv);
            album_detail_lv.setAdapter(playlist_adapter);
            if (MusicResource.listSongPlaylistShare.size() <= 4)
                checkAd = true;
        } else if (MusicResource.checkPlaylistOfflineUse) {
            album_detail_title.setText(MusicResource.playListSelectedTitle);
            album_detail_artist.setText(MusicResource.playlistOffline.get(MusicResource.playlistPosition).getSize() + " " + getString(R.string.track));

            Bitmap bmp = ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_thumbnail_default), 30);
            album_detail_art.setImageBitmap(bmp);

            String art_uri = MusicResource.hashMapPlaylistOffline.get(MusicResource.playListSelectedTitle)
                    .get(0).getArt_uri();

            if (art_uri != null) {
                loadBitmap(art_uri, album_detail_art);
            }

            playlistOfflineAdapter = new SongAdapter(getContext(),
                    MusicResource.hashMapPlaylistOffline.get(MusicResource.playListSelectedTitle), "playlist_offline");
            //MusicResource.albumCollection = MusicResource.hashMapPlaylistOffline.get(MusicResource.playListSelectedTitle);
            album_detail_lv.setAdapter(playlistOfflineAdapter);

            if (MusicResource.hashMapPlaylistOffline.get(MusicResource.playListSelectedTitle).size() <= 4)
                checkAd = true;
        } else if (MusicResource.checkPlaylistUse) {
            album_detail_title.setText(MusicResource.playListSelectedTitle);
            album_detail_artist.setText(MusicResource.playlistOnline.get(MusicResource.playlistPosition).getSize() + " " + getString(R.string.track));
            loadBitmap(MusicResource.hashMapPlaylistOnline.get(MusicResource.playListSelectedTitle).get(0).getUrl(),
                    album_detail_art, progressBar, -2, -2);
            playlist_adapter = new CSN_Search_RecyclerView_Adt(
                    getActivity(),
                    MusicResource.hashMapPlaylistOnline.get(MusicResource.playListSelectedTitle),
                    "playlist",
                    Glide.with(getActivity()),
                    album_detail_lv);
            album_detail_lv.setAdapter(playlist_adapter);
            if (MusicResource.hashMapPlaylistOnline.get(MusicResource.playListSelectedTitle).size() <= 4)
                checkAd = true;
        } else {
            if (!MusicResource.checkAlbumOnline) {
                ArrayList<Album> albumlistTemp = new ArrayList<Album>();
                if (MusicResource.album_offline_search_toogle == 0)
                    albumlistTemp = MusicResource.albumList;
                if (MusicResource.album_offline_search_toogle == 1)
                    albumlistTemp = MusicResource.albumListResult;
                album_detail_title.setText(albumlistTemp.get(MusicResource.albumPosition).getTitle());
                if (albumlistTemp.get(MusicResource.albumPosition).getSong_number() == 1)
                    album_detail_artist.setText(albumlistTemp.get(MusicResource.albumPosition).getArtist() + " - " + albumlistTemp.get(MusicResource.albumPosition).getSong_number() + " " + getResources().getString(R.string.one_song));
                else
                    album_detail_artist.setText(albumlistTemp.get(MusicResource.albumPosition).getArtist() + " - " + albumlistTemp.get(MusicResource.albumPosition).getSong_number() + " " + getResources().getString(R.string.mul_song));
                listSongCollection = new ArrayList<Song>();

                Bitmap bmp = ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_thumbnail_default), 30);
                album_detail_art.setImageBitmap(bmp);

                if (albumlistTemp.get(MusicResource.albumPosition).getArt_uri() != null) {
                    loadBitmap(albumlistTemp.get(MusicResource.albumPosition).getArt_uri(), album_detail_art);

                }

                for (Song song : MusicResource.songList) {
                    if (song.getAlbumid() == albumlistTemp.get(MusicResource.albumPosition).getId()) {
                        listSongCollection.add(song);
                    }
                }

                albumOfflineDetailAdapter = new AlbumDetailAdapter(getContext(), listSongCollection);
                MusicResource.albumCollection = listSongCollection;
                album_detail_lv.setAdapter(albumOfflineDetailAdapter);
                if (listSongCollection.size() <= 4) checkAd = true;

            } else {
                album_detail_title.setText(MusicResource.CSNSearchAlbumResult.get(MusicResource.albumPosition).getTitle());
                album_detail_artist.setText(MusicResource.CSNSearchAlbumResult.get(MusicResource.albumPosition).getArtist());
                getAlbumImageOnline();
                album_detail_lv.setAdapter(new CSN_Search_RecyclerView_Adt(
                        getActivity(),
                        MusicResource.CSNSearchAlbumSelected,
                        "album",
                        Glide.with(getActivity()),
                        album_detail_lv));
                if (MusicResource.CSNSearchAlbumSelected.size() <= 4) checkAd = true;
            }
        }
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
        dochange();
        if (checkAd) {
            new Init(avBanner).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        return view;
    }


    public class Init extends AsyncTask<Void, Void, Void> {
        //        private WeakReference<AdView> adViewWeakReference;
        AdView adView;

        public Init(AdView adView) {
//            adViewWeakReference = new WeakReference<AdView>(adView);
            this.adView = adView;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            adRequest = new AdRequest.Builder().build();

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            if (adViewWeakReference != null) {
//                AdView adView = adViewWeakReference.get();
            if (adView != null)
                adView.loadAd(adRequest);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    public void loadBitmap(String track_url, ImageView imageView, ProgressBar progressBar, int position, int mode) {

        final String imageKey = String.valueOf(track_url);

        final Bitmap bitmap = MusicResource.getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            progressBar.setVisibility(View.GONE);
            imageView.setImageBitmap(bitmap);
        } else {
            BitmapTrackWorkerTask task = new BitmapTrackWorkerTask(getActivity(), imageView, 128, 128, progressBar, position, mode, glide);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, track_url);
                Log.d("TestMultiTask", track_url);
            } else {
                task.execute(track_url);
            }
        }
    }


    public void getAlbumImageOnline() {
        final String art_src = MusicResource.CSNSearchAlbumResult.get(MusicResource.albumPosition).getArt_src();
        if (art_src != null) {
            //loadBitmap(csn_search_result.get(position).getArt_src(), holder.Art, holder.progressBar, position, 3);

            final WeakReference<ImageView> imageViewWeakReference = new WeakReference<ImageView>(album_detail_art);

            Glide
                    .with(getContext())
                    .load(art_src)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            Bitmap bitmap = ImageHelper.getRoundedCornerBitmap(resource, 30);
                            MusicResource.addBitmapToMemCache(String.valueOf(art_src), bitmap);

                            if (imageViewWeakReference != null) {
                                ImageView imageView = imageViewWeakReference.get();
                                if (imageView != null) {
                                    imageView.setImageBitmap(bitmap); // Possibly runOnUiThread()
                                }
                            }
                        }
                    });
        } else {
            //loadBitmap(null, holder.Art, holder.progressBar, position, 3);
        }
    }


    @Override

    public void onDestroy() {
        super.onDestroy();

//        getActivity().unregisterReceiver(receiverNotiPlay);
//        getActivity().unregisterReceiver(receiverNotiNext);
//        getActivity().unregisterReceiver(OnclickSongItemAlbumDetailReceiver);
//        getActivity().unregisterReceiver(OnItemOnlineClickDone);
//        getActivity().unregisterReceiver(receiverDoNextOnPlayActivity);
//        getActivity().unregisterReceiver(receiverDoBackOnPlayActivity);
//        getActivity().unregisterReceiver(OnItemOnlineClickReceiver);
//        getActivity().unregisterReceiver(OnItemOnlineClickDoneReceiver);
//        getActivity().unregisterReceiver(OnclickSongItemAlbumDetailDoneReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        MusicResource.isMainPause = true;
        IntentFilter noti_play_intent = new IntentFilter("noti_play_intent_action");
        IntentFilter noti_next_intent = new IntentFilter("noti_next_intent_action");
        IntentFilter noti_back_intent = new IntentFilter("noti_back_intent_action");
        getActivity().registerReceiver(receiverNotiPlay, noti_play_intent);
        getActivity().registerReceiver(receiverNotiNext, noti_next_intent);
        getActivity().registerReceiver(receiverNotiBack, noti_back_intent);
        IntentFilter OnclickSongItemAlbumDetail = new IntentFilter("OnclickSongItemAlbumDetail");
        getActivity().registerReceiver(OnclickSongItemAlbumDetailReceiver, OnclickSongItemAlbumDetail);
        IntentFilter intentFilter = new IntentFilter("OnItemOnlineClickDone");
        getActivity().registerReceiver(OnItemOnlineClickDone, intentFilter);

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
        IntentFilter OnclickSongItemAlbumDetailDone = new IntentFilter("OnclickSongItemAlbumDetailDone");
        getActivity().registerReceiver(OnclickSongItemAlbumDetailDoneReceiver, OnclickSongItemAlbumDetailDone);

        IntentFilter onClickFolderOfflineItemDone = new IntentFilter("onClickFolderOfflineItemDone");
        getActivity().registerReceiver(onClickFolderOfflineItemDoneReceiver, onClickFolderOfflineItemDone);

        IntentFilter song_removed = new IntentFilter("song_removed");
        getActivity().registerReceiver(song_removed_receiver, song_removed);

        IntentFilter track_removed = new IntentFilter("track_removed");
        getActivity().registerReceiver(track_removed_receiver, track_removed);
        IntentFilter onClickSongItemPlaylistOfflineDone = new IntentFilter("onClickPlaylistOfflineItemDone");
        getActivity().registerReceiver(onClickSongItemPlaylistOfflineDoneReceiver, onClickSongItemPlaylistOfflineDone);

        IntentFilter delete_track_done = new IntentFilter("delete_track_done");
        getActivity().registerReceiver(delete_track_done_receiver, delete_track_done);
        dochange();
    }

    BroadcastReceiver delete_track_done_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (albumOfflineDetailAdapter != null) {
                listSongCollection.remove(MusicResource.song_deleted);
                MusicResource.albumCollection = listSongCollection;

                albumOfflineDetailAdapter.notifyDataSetChanged();
            } else if (playlistOfflineAdapter != null) {
                MusicResource.hashMapPlaylistOffline
                        .get(MusicResource.playListSelectedTitle)
                        .remove(MusicResource.song_deleted);

                playlistOfflineAdapter.notifyDataSetChanged();
            } else if (folderDetailAdapter != null) {
                MusicResource.hashMapFolderList
                        .get(MusicResource.folderSelectedTitle)
                        .remove(MusicResource.song_deleted);

                folderDetailAdapter.notifyDataSetChanged();
            }
        }
    };

    BroadcastReceiver song_removed_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.hashMapPlaylistOffline
                    .get(MusicResource.playListSelectedTitle)
                    .remove(MusicResource.song_removed);

            if (MusicResource.playlistOfflineSelected != null &&
                    MusicResource.playlistOfflineSelected.contains(MusicResource.song_removed)) {
                int position = MusicResource.playlistOfflineSelected.indexOf(MusicResource.song_removed);
                MusicResource.playlistOfflineSelected.remove(MusicResource.song_removed);
                if (MusicResource.songPosition > position) {
                    MusicResource.songPosition--;
                }
            }

            int size = MusicResource.playlistOffline
                    .get(MusicResource.playlistPosition)
                    .getSize() - 1;

            MusicResource.playlistOffline
                    .get(MusicResource.playlistPosition)
                    .setSize(size);

            if (size <= 0) {
                MusicResource.playlistOffline.remove(
                        MusicResource.playlistOffline.get(
                                MusicResource.playlistPosition));
            }

            playlistOfflineAdapter.notifyDataSetChanged();
        }
    };

    BroadcastReceiver track_removed_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.hashMapPlaylistOnline
                    .get(MusicResource.playListSelectedTitle)
                    .remove(MusicResource.track_removed);

            if (MusicResource.songListOnlinePlayList != null &&
                    MusicResource.songListOnlinePlayList.contains(MusicResource.track_removed)) {
                int position = MusicResource.songListOnlinePlayList.indexOf(MusicResource.track_removed);
                MusicResource.songListOnlinePlayList.remove(MusicResource.track_removed);
                if (MusicResource.songPosition > position) {
                    MusicResource.songPosition--;
                }
            }

            int size = MusicResource.playlistOnline
                    .get(MusicResource.playlistPosition)
                    .getSize() - 1;

            MusicResource.playlistOnline
                    .get(MusicResource.playlistPosition)
                    .setSize(size);

            if (size <= 0) {
                MusicResource.playlistOnline.remove(
                        MusicResource.playlistOnline.get(
                                MusicResource.playlistPosition));
            }

            playlist_adapter.notifyDataSetChanged();
        }
    };

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

    BroadcastReceiver OnItemOnlineClickDone = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            main_background.setImageBitmap(MusicResource.blurredBitmap);
        }
    };

    public void setImageDefault() {
        Drawable drawable = getResources().getDrawable(R.drawable.nice_view_main_background_2);
        MusicResource.subArt = ((BitmapDrawable) drawable).getBitmap();
        MusicResource.noti_art = ImageHelper.getRoundedCornerBitmap(MusicResource.subArt, 30);
        subArt.setImageBitmap(MusicResource.subArt);
        Bitmap bitmapResized = Bitmap.createScaledBitmap(MusicResource.subArt, 200, 200, false);
        MusicResource.blurredBitmap = BlurBuilder.blur(getActivity(),
                bitmapResized);
        MusicResource.ImageViewAnimatedChange(getContext(), main_background, MusicResource.blurredBitmap);
    }

    BroadcastReceiver onClickSongItemPlaylistOfflineDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            subPlaySong.setVisibility(View.VISIBLE);
            MusicResource.subPlaySongVisible = true;


            String art_uri = MusicResource.hashMapPlaylistOffline.get(MusicResource.playListSelectedTitle).get(MusicResource.songPosition).getArt_uri();

            if (art_uri != null && !MusicResource.musicOnlline) {
                Bitmap temp = MusicResource.decodeSampledBitmapFromUri(art_uri, 512, 512);
                if (temp != null) {
                    MusicResource.subArt = temp;
                    loadBitmap(art_uri, subArt);
                    MusicResource.blurredBitmap = BlurBuilder.blur(getContext(), MusicResource.subArt);
                    MusicResource.ImageViewAnimatedChange(getContext(), main_background, MusicResource.blurredBitmap);
                } else {
                    setImageDefault();
                }
            } else {
                setImageDefault();
            }
            sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
            MusicResource.subTitle = MusicResource.hashMapPlaylistOffline.get(MusicResource.playListSelectedTitle).get(MusicResource.songPosition).getTitle();
            MusicResource.subArtist = MusicResource.hashMapPlaylistOffline.get(MusicResource.playListSelectedTitle).get(MusicResource.songPosition).getArtist();
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);
        }
    };

    BroadcastReceiver onClickFolderOfflineItemDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            subPlaySong.setVisibility(View.VISIBLE);
            MusicResource.subPlaySongVisible = true;

            String art_uri = MusicResource.hashMapFolderList.get(MusicResource.folderSelectedTitle).get(MusicResource.songPosition).getArt_uri();
            if (art_uri != null && !MusicResource.musicOnlline) {
                Bitmap temp = MusicResource.decodeSampledBitmapFromUri(art_uri, 512, 512);
                if (temp != null) {
                    MusicResource.subArt = temp;
                    loadBitmap(art_uri, subArt);
                    MusicResource.blurredBitmap = BlurBuilder.blur(getContext(), MusicResource.subArt);
                    MusicResource.ImageViewAnimatedChange(getContext(), main_background, MusicResource.blurredBitmap);
                } else {
                    setImageDefault();
                }
            } else {
                setImageDefault();
            }
            sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
            MusicResource.subTitle = MusicResource.hashMapFolderList.get(MusicResource.folderSelectedTitle).get(MusicResource.songPosition).getTitle();
            MusicResource.subArtist = MusicResource.hashMapFolderList.get(MusicResource.folderSelectedTitle).get(MusicResource.songPosition).getArtist();
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);

        }
    };


    BroadcastReceiver OnclickSongItemAlbumDetailDoneReceiver
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            subPlaySong.setVisibility(View.VISIBLE);
            MusicResource.subPlaySongVisible = true;


            if (MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {
                Bitmap temp = MusicResource.decodeSampledBitmapFromUri(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                if (temp != null) {
                    MusicResource.subArt = temp;
                    loadBitmap(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), subArt);
                    MusicResource.blurredBitmap = BlurBuilder.blur(getContext(), MusicResource.subArt);
                    MusicResource.ImageViewAnimatedChange(getContext(), main_background, MusicResource.blurredBitmap);
                } else {
                    setImageDefault();
                }
            } else {
                setImageDefault();
            }
            sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
            MusicResource.subTitle = MusicResource.albumCollectionContent.get(MusicResource.songPosition).getTitle();
            MusicResource.subArtist = MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArtist();
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);
        }
    };
    BroadcastReceiver OnclickSongItemAlbumDetailReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.albumCollectionContent = listSongCollection;
            MusicResource.MODE = 30;
            getActivity().sendBroadcast(new Intent("OnclickSongItemAlbumDetail1"));
        }
    };

    public static Bitmap decodeSampledBitmapFromUri(String art_uri, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(art_uri, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(art_uri, options);
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public void loadBitmap(String art_uri, ImageView imageView) {
        BitmapWorkerTask task = new BitmapWorkerTask(getContext(), imageView, 256, 256);
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
                    else
                        doBack();
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

    public void dochange() {
        Log.d("dochange", "dochange");
        subTitle.setText(MusicResource.subTitle);
        subArtist.setText(MusicResource.subArtist);
        subArt.setImageBitmap(MusicResource.subArt);
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
        MusicResource.ImageViewAnimatedChange(getActivity(), main_background, MusicResource.blurredBitmap);
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
            Log.d("dochange2", "dochange");
            dochange();
        }
    };
    BroadcastReceiver receiverDoShuffleOnPlayActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dochange();
        }
    };
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