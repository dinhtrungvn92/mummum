package com.kenung.vn.prettymusic;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kenung.vn.prettymusic.music_offline.playlist.PlaylistOfflineDialogAdapter;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineDialogAdapter;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by KXPRO on 4/8/2017.
 */

public class PlayActivity extends AppCompatActivity implements View.OnClickListener {
    private long mLastClickTime = 0;
    private ImageButton btnBack, btnNext;
    private Intent intent;
    private RelativeLayout play_control;
    private ImageView exit_btn;
    private TextView songTimer, songDuration;
    private boolean activity_live = false;
    private SeekBar playSeekBar;
    private TextView play_song_title;
    private TextView play_song_artist;
    private Button playBtn;
    private ImageView play_background;
    private ImageButton repeatBtn;
    private ImageButton shuffleBtn;
    private Calendar calendar;
    ImageView timer;

    private Fragment fragmentPlay = null;

    BroadcastReceiver addToExistPlaylistOfflineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (playlistOnlineDialog != null)
                playListDialog.dismiss();
            if (MusicResource.hashMapPlaylistOffline.get(MusicResource.addToPlayListModel.getTitle()).contains(MusicResource.addToPlaylistSong)) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.track_exist), Toast.LENGTH_SHORT).show();
                return;
            }
            MusicResource.hashMapPlaylistOffline.get(MusicResource.addToPlayListModel.getTitle()).add(MusicResource.addToPlaylistSong);
            int size = MusicResource.playlistOffline.get(MusicResource.playlistOffline.indexOf(MusicResource.addToPlayListModel)).getSize();
            MusicResource.playlistOffline.get(MusicResource.playlistOffline.indexOf(MusicResource.addToPlayListModel)).setSize(
                    size + 1);
            sendBroadcast(new Intent("AddToPlayListOfflineSuccess"));
        }
    };

    BroadcastReceiver addToNewPlaylistOfflineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("TestDialog", "addToNewPlaylistOfflineReceiver");

            if (playlistOnlineDialog != null)
                playListDialog.dismiss();

            new MaterialDialog.Builder(context)
                    .title(getResources().getString(R.string.create_playlist))
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("My Playlist", "", new MaterialDialog.InputCallback() {
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
                                        Toast.makeText(getApplicationContext(),
                                                getResources().getString(R.string.playlist_exist),
                                                Toast.LENGTH_LONG)
                                                .show();
                                        if (playlistOnlineDialog != null)
                                            playListDialog.show();
                                        return;
                                    }
                                }
                                PlaylistOnlineModel playlistOnlineModel = new PlaylistOnlineModel(input.toString());
                                MusicResource.playlistOffline.add(playlistOnlineModel);
                                if (playListDialog != null) {
                                    playListDialog.dismiss();
                                }
                                MusicResource.hashMapPlaylistOffline.put(input.toString(), new ArrayList<Song>());
                                MusicResource.hashMapPlaylistOffline.get(input.toString()).add(MusicResource.addToPlaylistSong);
                                int size = MusicResource.playlistOffline.get(MusicResource.playlistOffline.indexOf(playlistOnlineModel)).getSize();
                                MusicResource.playlistOffline.get(MusicResource.playlistOffline.indexOf(playlistOnlineModel)).setSize(
                                        size + 1);
                                sendBroadcast(new Intent("AddToPlayListOfflineSuccess"));
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getResources().getString(R.string.playlist_null),
                                        Toast.LENGTH_LONG)
                                        .show();
                                if (playlistOnlineDialog != null)
                                    playListDialog.show();
                            }
                        }
                    }).show();
        }
    };

    BroadcastReceiver addToExistPlaylistOnlineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (playlistOnlineDialog != null)
                playListDialog.dismiss();
            if (MusicResource.hashMapPlaylistOnline.get(MusicResource.addToPlayListModel.getTitle()).contains(MusicResource.addToPlaylistTrack)) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.track_exist), Toast.LENGTH_SHORT).show();
                return;
            }
            MusicResource.hashMapPlaylistOnline.get(MusicResource.addToPlayListModel.getTitle()).add(MusicResource.addToPlaylistTrack);
            int size = MusicResource.playlistOnline.get(MusicResource.playlistOnline.indexOf(MusicResource.addToPlayListModel)).getSize();
            MusicResource.playlistOnline.get(MusicResource.playlistOnline.indexOf(MusicResource.addToPlayListModel)).setSize(
                    size + 1);
            sendBroadcast(new Intent("AddToPlayListSuccess"));
        }
    };

    BroadcastReceiver addToNewPlaylistOnlineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (playlistOnlineDialog != null)
                playListDialog.dismiss();

            new MaterialDialog.Builder(context)
                    .title(getResources().getString(R.string.create_playlist))
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("My Playlist", "", new MaterialDialog.InputCallback() {
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
                                        Toast.makeText(getApplicationContext(),
                                                getResources().getString(R.string.playlist_exist),
                                                Toast.LENGTH_LONG)
                                                .show();
                                        if (playlistOnlineDialog != null)
                                            playListDialog.show();
                                        return;
                                    }
                                }
                                PlaylistOnlineModel playlistOnlineModel = new PlaylistOnlineModel(input.toString());
                                MusicResource.playlistOnline.add(playlistOnlineModel);
                                if (playlistOnlineDialog != null)
                                    playListDialog.dismiss();
                                MusicResource.hashMapPlaylistOnline.put(input.toString(), new ArrayList<Track>());
                                MusicResource.hashMapPlaylistOnline.get(input.toString()).add(MusicResource.addToPlaylistTrack);
                                int size = MusicResource.playlistOnline.get(MusicResource.playlistOnline.indexOf(playlistOnlineModel)).getSize();
                                MusicResource.playlistOnline.get(MusicResource.playlistOnline.indexOf(playlistOnlineModel)).setSize(
                                        size + 1);
                                sendBroadcast(new Intent("AddToPlayListSuccess"));
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getResources().getString(R.string.playlist_null),
                                        Toast.LENGTH_LONG)
                                        .show();
                                if (playlistOnlineDialog != null)
                                    playListDialog.show();
                            }
                        }
                    }).show();
        }
    };

    private BroadcastReceiver receiverAudioFocus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(getApplicationContext(), "AudioFocus Loss", Toast.LENGTH_SHORT).show();

            Log.d("testLog", "AudioFocus Loss");
            if (Player.player != null)
                if (Player.player.isPlaying()) {
                    playBtn.setBackgroundResource(R.drawable.ic_play_circle_filled_white);
                } else {
                    playBtn.setBackgroundResource(R.drawable.ic_pause_circle_filled_white);
                }
        }
    };

    private BroadcastReceiver receiverAudioStateChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Player.player != null)
                if (Player.player.isPlaying()) {
                    playBtn.setBackgroundResource(R.drawable.ic_play_circle_filled_white);
                } else {
                    playBtn.setBackgroundResource(R.drawable.ic_pause_circle_filled_white);
                }
        }
    };

    private BroadcastReceiver receiverAudioSelectedChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.nice_view_main_background_2);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(),
                        bitmapResized);
                switch (MusicResource.MODE) {
                    case 0: {
                        play_song_title.setText(MusicResource.songList.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.songList.get(MusicResource.songPosition).getArtist());
                        if (MusicResource.songList.get(MusicResource.songPosition).getArt_uri() != null) {
                            Bitmap temp = decodeSampledBitmapFromUri(MusicResource.songList.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                            if (temp != null)
                                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp);

                        }
                    }
                    break;

                    case 1: {
                        play_song_title.setText(MusicResource.folderSelected.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.folderSelected.get(MusicResource.songPosition).getArtist());
                        if (MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri() != null) {
                            Bitmap temp = decodeSampledBitmapFromUri(MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                            if (temp != null)
                                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp);

                        }
                    }
                    break;

                    case 2: {
                        play_song_title.setText(MusicResource.songListDownload.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.songListDownload.get(MusicResource.songPosition).getArtist());
                        if (MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri() != null) {
                            Bitmap temp = decodeSampledBitmapFromUri(MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                            if (temp != null)
                                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp);

                        }
                    }
                    break;
                    case 4: {
                        play_song_title.setText(MusicResource.searchResultList.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.searchResultList.get(MusicResource.songPosition).getArtist());
                        if (MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri() != null) {
                            Bitmap temp = decodeSampledBitmapFromUri(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                            if (temp != null)
                                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp);


                        }
                    }
                    break;
                    case 30: {
                        play_song_title.setText(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArtist());
                        if (MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri() != null) {
                            Bitmap temp = decodeSampledBitmapFromUri(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                            if (temp != null)
                                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp);

                        }
                    }
                    break;
                    case 31: {
                        play_song_title.setText(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArtist());
                        if (MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri() != null) {
                            Bitmap temp = decodeSampledBitmapFromUri(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                            if (temp != null)
                                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp);

                        }
                    }
                    break;
                    default:
                        break;
                }
                MusicResource.ImageViewAnimatedChange(getApplicationContext(), play_background, MusicResource.blurredBitmap);
            }
            if (Player.player != null) {
                if (Player.player.isPlaying()) {
                    playSeekBar.setMax(Player.player.getDuration());
                    songDuration.setText(check_minute(Player.player.getDuration() / 60000)
                            + ":" + check_second(Player.player.getDuration() % 60000));
                }
                if (Player.player.isPlaying()) {
                    playBtn.setBackgroundResource(R.drawable.ic_play_circle_filled_white);
                } else {
                    playBtn.setBackgroundResource(R.drawable.ic_pause_circle_filled_white);
                }
            }
        }
    };

    private BroadcastReceiver receiverNetworkChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService
                            (Context.CONNECTIVITY_SERVICE);
            if (connectivityManager.getActiveNetworkInfo() != null) {
                MusicResource.network_state = true;
            } else {
                MusicResource.network_state = false;
            }

//            Log.d("testBroacastReceiver", "Network Change");
        }
    };


    BroadcastReceiver receiverNotiPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getApplicationContext(), "NOTI PLAY CLICK", Toast.LENGTH_LONG).show();
            playBtn.performClick();
        }
    };
    BroadcastReceiver receiverNotiNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getApplicationContext(), "NOTI NEXT CLICK", Toast.LENGTH_LONG).show();
            btnNext.performClick();
        }
    };
    BroadcastReceiver receiverNotiBack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getApplicationContext(), "NOTI NEXT CLICK", Toast.LENGTH_LONG).show();
            btnBack.performClick();
        }
    };

    BroadcastReceiver EnableAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            timer.setImageResource(R.drawable.ic_alarm_on_white_24dp);
        }
    };
    BroadcastReceiver DisableAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            timer.setImageResource(R.drawable.ic_alarm_off_white_24dp);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        MusicResource.isMainPause = true;
        if (MusicResource.timerEnable) timer.setImageResource(R.drawable.ic_alarm_on_white_24dp);
        else timer.setImageResource(R.drawable.ic_alarm_off_white_24dp);
        IntentFilter EnableAlarm = new IntentFilter("EnableAlarm");
        registerReceiver(EnableAlarmReceiver, EnableAlarm);

        IntentFilter DisableAlarm = new IntentFilter("DisableAlarm");
        registerReceiver(DisableAlarmReceiver, DisableAlarm);

        IntentFilter addToNewPlaylistOffline = new IntentFilter("addToNewPlaylistOfflineFromPlayActivity");
        registerReceiver(addToNewPlaylistOfflineReceiver, addToNewPlaylistOffline);
        IntentFilter addToExistPlaylistOffline = new IntentFilter("addToExistPlaylistOfflineFromPlayActivity");
        registerReceiver(addToExistPlaylistOfflineReceiver, addToExistPlaylistOffline);

        IntentFilter addToNewPlaylistOnline = new IntentFilter("addToNewPlaylistOnlineFromPlayActivity");
        registerReceiver(addToNewPlaylistOnlineReceiver, addToNewPlaylistOnline);
        IntentFilter addToExistPlaylistOnline = new IntentFilter("addToExistPlaylistOnlineFromPlayActivity");
        registerReceiver(addToExistPlaylistOnlineReceiver, addToExistPlaylistOnline);

        IntentFilter noti_play_intent = new IntentFilter("noti_play_intent_action");
        IntentFilter noti_next_intent = new IntentFilter("noti_next_intent_action");
        IntentFilter noti_back_intent = new IntentFilter("noti_back_intent_action");
        registerReceiver(receiverNotiPlay, noti_play_intent);
        registerReceiver(receiverNotiNext, noti_next_intent);
        registerReceiver(receiverNotiBack, noti_back_intent);
        IntentFilter intentFilterDoNextOnPlayActivity = new IntentFilter("intentFilterDoNextOnPlayActivity");
        registerReceiver(receiverDoNextOnPlayActivity, intentFilterDoNextOnPlayActivity);
        IntentFilter intentFilterDoBackOnPlayActivity = new IntentFilter("intentFilterDoBackOnPlayActivity");
        registerReceiver(receiverDoBackOnPlayActivity, intentFilterDoBackOnPlayActivity);
        IntentFilter intentFilterDoShuffleOnPlayActivity = new IntentFilter("intentFilterDoShuffleOnPlayActivity");
        registerReceiver(receiverDoShuffleOnPlayActivity, intentFilterDoShuffleOnPlayActivity);

        IntentFilter intentFilterAudioFocus = new IntentFilter("noti_audiofocus_change");
        IntentFilter intentFilterAudioStateChange = new IntentFilter("noti_audiostate_change");
        IntentFilter intentFilterAudioSelectedChange = new IntentFilter("noti_audioselected_change");
        IntentFilter intentFilterNetworkChange = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");

        registerReceiver(receiverAudioFocus, intentFilterAudioFocus);
        registerReceiver(receiverAudioStateChange, intentFilterAudioStateChange);
        registerReceiver(receiverNetworkChange, intentFilterNetworkChange);
        registerReceiver(receiverAudioSelectedChange, intentFilterAudioSelectedChange);
        IntentFilter OnclickSongItemOnlineDone = new IntentFilter("OnItemOnlineClickDone");
        registerReceiver(OnclickSongItemOnlineDoneReceiver, OnclickSongItemOnlineDone);
    }

    BroadcastReceiver receiverDoShuffleOnPlayActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doshuffleOnPlayActivity();
        }
    };
    BroadcastReceiver receiverDoBackOnPlayActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dobackOnPlayActivity();
        }
    };
    BroadcastReceiver receiverDoNextOnPlayActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            donextOnPlayActivity();
        }
    };
    BroadcastReceiver OnclickSongItemOnlineDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
//        MusicResource.isMainPause = false;
//        unregisterReceiver(receiverNotiPlay);
//        unregisterReceiver(receiverNotiNext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLastClickTime = SystemClock.elapsedRealtime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.play_activity);
        timer = (ImageView) findViewById(R.id.timer);
        activity_live = true;

        MusicResource.STATE = false;

        songTimer = (TextView) findViewById(R.id.songTimer);
        songDuration = (TextView) findViewById(R.id.songDuration);
        playSeekBar = (SeekBar) findViewById(R.id.seekBar);
        playBtn = (Button) findViewById(R.id.btn_play);
        play_song_title = (TextView) findViewById(R.id.play_song_title);
        play_song_artist = (TextView) findViewById(R.id.play_song_artist);
        play_song_title.setSelected(true);
        play_song_artist.setSelected(true);
        btnNext = (ImageButton) findViewById(R.id.btn_next);
        btnBack = (ImageButton) findViewById(R.id.btn_back);
        exit_btn = (ImageView) findViewById(R.id.exit_btn);
        repeatBtn = (ImageButton) findViewById(R.id.btn_repeat);
        shuffleBtn = (ImageButton) findViewById(R.id.btn_shuffle);
        play_control = (RelativeLayout) findViewById(R.id.play_control);
        play_control.setAlpha(0.8f);
        playBtn.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        exit_btn.setOnClickListener(this);
        repeatBtn.setOnClickListener(this);
        shuffleBtn.setOnClickListener(this);
        if (MusicResource.repeat_mode == 0) {
            repeatBtn.setImageResource(R.drawable.ic_repeat_white_24dp);
        } else repeatBtn.setImageResource(R.drawable.ic_repeat_one_white_24dp);
        if (MusicResource.shuffle)
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_white_24dp);
        else shuffleBtn.setImageResource(R.drawable.ic_shuffle_off_white_24dp);
        play_background = (ImageView) findViewById(R.id.play_main_background);

        fragmentPlay = new PlayFragment().newInstance();

        Thread update_seekbar = new Thread() {

            @Override
            public void run() {
                while (activity_live) {
                    try {
                        sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (Player.player != null && MusicResource.track_ready) {
                                    int currentPosition = Player.player.getCurrentPosition();
                                    playSeekBar.setProgress(currentPosition);
                                    songTimer.setText(check_minute(Player.player.getCurrentPosition() / 60000)
                                            + ":" + check_second(Player.player.getCurrentPosition() % 60000));
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        playSeekBar.setMax(Player.player.getDuration());
        playSeekBar.setProgress(Player.player.getCurrentPosition());
        songDuration.setText(check_minute(Player.player.getDuration() / 60000)
                + ":" + check_second(Player.player.getDuration() % 60000));
        songTimer.setText(check_minute(Player.player.getCurrentPosition() / 60000)
                + ":" + check_second(Player.player.getCurrentPosition() % 60000));

        update_seekbar.start();

        timer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
        intent = getIntent();
        if (intent != null) {
            MusicResource.STATE = false;

            if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
                MusicResource.online_offine_play = 1;
                MusicResource.Number_Item_Play_Activity = 3;
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

                MusicResource.songListOnlinePlay = MusicResource.songListOnline = hashMap.get(MusicResource.MODE);
                play_song_title.setText(
                        hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getTitle());
                play_song_artist.setText(
                        hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getArtist());
            } else {
                MusicResource.online_offine_play = 0;
                MusicResource.Number_Item_Play_Activity = 2;
                switch (MusicResource.MODE) {
                    // Offline - Track
                    case 0: {

                        play_song_title.setText(MusicResource.songList.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.songList.get(MusicResource.songPosition).getArtist());

                    }
                    break;
                    // CSN BXH VN
                    case 1: {

//                        Log.d("TestAlbum", "BXH VN");

                        play_song_title.setText(MusicResource.folderSelected.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.folderSelected.get(MusicResource.songPosition).getArtist());

                    }
                    break;
                    // CSN BXH UK
                    case 2: {
                        play_song_title.setText(MusicResource.songListDownload.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.songListDownload.get(MusicResource.songPosition).getArtist());

                    }
                    break;
                    // Offline - AlbumCollection
                    case 3: {

//                        Log.d("TestAlbum", "Album OK");

                        play_song_title.setText(MusicResource.albumSelected.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.albumSelected.get(MusicResource.songPosition).getArtist());

                    }
                    break;
                    // Offline Search
                    case 4: {

                        play_song_title.setText(MusicResource.searchResultList.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.searchResultList.get(MusicResource.songPosition).getArtist());

                    }
                    break;
                    // CSN Search
                    case 5: {

                        play_song_title.setText(MusicResource.CSNSearchResult.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.CSNSearchResult.get(MusicResource.songPosition).getArtist());


                    }
                    break;
                    case 30: {

                        play_song_title.setText(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArtist());

                    }
                    break;
                    case 31: {
                        play_song_title.setText(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getTitle());
                        play_song_artist.setText(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArtist());
                    }
                    break;
                    default:
                        break;
                }
            }
            play_background.setImageBitmap(MusicResource.blurredBitmap);
            playSeekBar.setMax(Player.player.getDuration());
            playSeekBar.setProgress(Player.player.getCurrentPosition());
            if (Player.player != null)
                if (Player.player.isPlaying()) {
                    playBtn.setBackgroundResource(R.drawable.ic_play_circle_filled_white);

                    //playArt.startAnimation(anim);
                } else playBtn.setBackgroundResource(R.drawable.ic_pause_circle_filled_white);


            //playArt.setImageAlpha(700);
        }

        playSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Player.player.seekTo(seekBar.getProgress());
            }
        });

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.play_flContent, fragmentPlay, "fragmentPlay")
                .commit();
    }

    public void donextOnPlayActivity() {
        sendBroadcast(new Intent("noti_audioselected_change"));
    }

    public void dobackOnPlayActivity() {
        sendBroadcast(new Intent("noti_audioselected_change"));
    }

    public void doshuffleOnPlayActivity() {
        sendBroadcast(new Intent("noti_audioselected_change"));
    }


    public void doNext() {


        MusicResource.set_src_complete = false;
        if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {

            if (!MusicResource.network_state) {
                Toast.makeText(this, getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
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
            sendBroadcast(new Intent("OnItemOnlineClick"));
        } else {

            sendBroadcast(new Intent("intentFilterDoNext"));
        }

    }

    public void doBack() {


        MusicResource.set_src_complete = false;
        if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
            if (!MusicResource.network_state) {
                Toast.makeText(this, getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
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
            sendBroadcast(new Intent("OnItemOnlineClick"));
        } else {

            sendBroadcast(new Intent("intentFilterDoBack"));
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_play: {
                if (MusicResource.isCanChange) {
                    if (Player.player == null) return;
                    if (Player.player.isPlaying()) {
                        playBtn.setBackgroundResource(R.drawable.ic_pause_circle_filled_white);

                        MusicResource.sub_anim_running = false;

//                        Log.d("TestAnim", "PauseClick - " + MusicResource.imagePosition);

                        Player.player.pause();

                        sendBroadcast(new Intent("noti_audiostate_change"));
                        MusicResource.csn_Notification(getApplication(),
                                null,
                                null,
                                null, "pause");
                    } else if (MusicResource.request_audio_focus(getApplicationContext())) {

                        Player.player.start();

                        playBtn.setBackgroundResource(R.drawable.ic_play_circle_filled_white);

                        sendBroadcast(new Intent("noti_audiostate_change"));
                        MusicResource.csn_Notification(getApplication(),
                                null,
                                null,
                                null, "play");
                    }
                }
            }
            break;
            case R.id.btn_next: {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    mLastClickTime = SystemClock.elapsedRealtime();
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (MusicResource.isCanChange) {
                    if (MusicResource.shuffle)
                        sendBroadcast(new Intent("intentFilterDoShuffle"));
                    else
                        doNext();
                }

            }
            break;
            case R.id.btn_back: {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    mLastClickTime = SystemClock.elapsedRealtime();
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (MusicResource.isCanChange) {
                    if (MusicResource.shuffle)
                        sendBroadcast(new Intent("intentFilterDoShuffle"));
                    doBack();
                }
            }
            break;
            case R.id.btn_repeat: {
                if (MusicResource.repeat_mode == 0) {
                    MusicResource.repeat_mode = 1;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                } else {
                    MusicResource.repeat_mode = 0;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_white_24dp);
                }
            }
            break;
            case R.id.btn_shuffle: {
                if (MusicResource.shuffle) {
                    MusicResource.shuffle = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off_white_24dp);
                } else {
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_white_24dp);
                    MusicResource.shuffle = true;
                }
            }
            break;
            case R.id.exit_btn: {

                MusicResource.STATE = true;
                activity_live = false;

                finish();
                overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
            }
            break;
            default:
                break;
        }
    }

    private class CSN_getTrackDetail extends AsyncTask<String, Void, Bitmap> {

        private String track_url;
        private String imageKey;
        private Bitmap bitmap;
        HashMap<Integer, ArrayList<Track>> hashMap = new HashMap<>();

        @Override
        protected Bitmap doInBackground(String... params) {

            track_url = params[0];

            imageKey = String.valueOf(track_url);
            bitmap = MusicResource.getBitmapFromMemCache(imageKey);

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

            if (hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getSrc() != null) {

                if (Player.player == null) {
                    Player.player = new MediaPlayer();
                } else {
                    Player.player.reset();
                    Player.player = new MediaPlayer();
                }

                Player.player.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try {
                    Player.player.setDataSource(
                            hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getSrc());
                } catch (IllegalArgumentException error) {
                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                } catch (SecurityException error) {
                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                } catch (IllegalStateException error) {
                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                } catch (IOException error) {
                    error.printStackTrace();
                }

                try {
                    Player.player.prepare();
                } catch (IllegalStateException error) {
                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                } catch (IOException error) {
                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                }

                MusicResource.set_src_complete = true;
                return null;
            } else try {
                Document document = Jsoup.connect(track_url).userAgent("Chrome/59.0.3071.115").get();
                Elements scripts = document.select("script");

                if (scripts != null && scripts.size() > 0) {
                    for (Element e : scripts) {
                        Pattern track = Pattern.compile("\"(.+\\.mp3)");
                        Matcher matcher = track.matcher(e.html());

                        if (matcher.find()) {
                            Log.e(MusicResource.LOG_TAG, "get Track Complete - " + matcher.group(1));

                            if (Player.player == null) {
                                Player.player = new MediaPlayer();
                            } else {
                                Player.player.reset();
                                Player.player = new MediaPlayer();
                            }

                            Player.player.setAudioStreamType(AudioManager.STREAM_MUSIC);

                            try {
                                Player.player.setDataSource(matcher.group(1));
                            } catch (IllegalArgumentException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            } catch (SecurityException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            } catch (IllegalStateException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            } catch (IOException error) {
                                error.printStackTrace();
                            }

                            try {
                                Player.player.prepare();
                            } catch (IllegalStateException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            } catch (IOException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            }

                            MusicResource.set_src_complete = true;
                            hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).setSrc(matcher.group(1));
                        }
                    }
                }

                if (bitmap != null) {
                    return null;
                } else {
                    String detail_csn_art_url = document.select("meta[property=og:image]").get(0).attr("content");
                    Bitmap detail_csn_art = null;
                    try {
                        URL url = new URL(detail_csn_art_url);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        detail_csn_art = BitmapFactory.decodeStream(input);
                        connection.disconnect();
                        if (detail_csn_art != null) {
                            MusicResource.addBitmapToMemCache(imageKey, detail_csn_art);
                            return detail_csn_art;
                        } else return null;
                    } catch (IOException e) {
                        // Log exception
                        return null;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {

            super.onPostExecute(bm);

/*            if (bitmap != null) {
                subArt.setImageBitmap(bitmap);
                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), bitmap);
            } else if (bm != null) {
                subArt.setImageBitmap(bm);
                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), bm);
            } else {
                subArt.setImageResource(R.drawable.music_cover_default);
                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(),
                        BitmapFactory.decodeResource(getResources(), R.drawable.nice_view_main_background_2));
            }

            subTitle.setText(
                    hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getTitle());
            subArtist.setText(
                    hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getArtist());
            sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

            ImageViewAnimatedChange(getApplicationContext(), main_background, MusicResource.blurredBitmap);

            subPlaySong.setEnabled(true);*/

            Player.player.start();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);

        MusicResource.STATE = true;
        activity_live = false;

        if (Player.player != null && Player.player.isPlaying()) {
            calendar = Calendar.getInstance();
            MusicResource.endTime = calendar.get(Calendar.HOUR) * 3600000 +
                    calendar.get(Calendar.MINUTE) * 60000 +
                    calendar.get(Calendar.SECOND) * 1000 +
                    calendar.get(Calendar.MILLISECOND);
            MusicResource.activeTime = MusicResource.endTime - MusicResource.startTime;
            MusicResource.imagePosition += (360f * MusicResource.activeTime) / 20000f;
            if (MusicResource.imagePosition >= 360f)
                MusicResource.imagePosition -= 360f;

        }

//        Log.d("TestAnim", "onBackPressed - " + MusicResource.imagePosition);

        finish();
    }

    @Override
    protected void onDestroy() {

        activity_live = false;

        unregisterReceiver(receiverAudioFocus);
        unregisterReceiver(receiverAudioStateChange);
        unregisterReceiver(receiverNetworkChange);
        unregisterReceiver(receiverAudioSelectedChange);

        super.onDestroy();
    }

    private String check_minute(int number) {
        return number < 10 ? "0" + number : String.valueOf(number);
    }

    private String check_second(int number) {
        return number / 1000 < 10 ? "0" + number / 1000 : String.valueOf(number / 1000);
    }

    private void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public static Bitmap decodeSampledBitmapFromUri(String art_uri, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(art_uri, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(art_uri, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
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
        BitmapWorkerTask task = new BitmapWorkerTask(getApplication(), imageView, 512, 512);
        task.execute(art_uri);
    }

    public static MaterialDialog.Builder playlistOnlineDialog;
    public static Dialog playListDialog;

    public static void addToPlaylistOffline(final Context context) {
        playlistOnlineDialog = new MaterialDialog.Builder(context).adapter(new PlaylistOfflineDialogAdapter(context, MusicResource.playlistOnlineDialog), null);
        playListDialog = playlistOnlineDialog.build();
        playListDialog.show();

    }

    public static void addToPlaylistOnline(final Context context) {
        playlistOnlineDialog = new MaterialDialog.Builder(context).adapter(new PlaylistOnlineDialogAdapter(context, MusicResource.playlistOnlineDialog), null);
        playListDialog = playlistOnlineDialog.build();
        playListDialog.show();

    }
}
