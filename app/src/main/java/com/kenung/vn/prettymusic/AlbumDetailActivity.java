package com.kenung.vn.prettymusic;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.kenung.vn.prettymusic.music_offline.album.AlbumDetailFragment;
import com.kenung.vn.prettymusic.music_offline.playlist.PlaylistOfflineDialogAdapter;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineDialogAdapter;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 16/05/2017.
 */

public class AlbumDetailActivity extends AppCompatActivity {
    ImageView search_main_background;
    private Bitmap blurredBitmap;
    private Intent intent;
    AlbumDetailFragment albumDetailFragment;
    private CircleImageView subArt;
    private ImageButton sub_Next_btn;
    private ImageButton sub_Back_btn;
    private Button sub_Play_btn;
    private TextView subTitle;
    private TextView subArtist;
    private LinearLayout subPlaySong;
    private Animation anim;
    private float imagePositionOffset = 0f;

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

            if (playlistOnlineDialog != null)
                playListDialog.dismiss();

            new MaterialDialog.Builder(context)
                    .title(getResources().getString(R.string.create_playlist))
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(getResources().getString(R.string.input_playlits_name), "", new MaterialDialog.InputCallback() {
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
                                if (playlistOnlineDialog != null)
                                    playListDialog.dismiss();
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
                    .input(getResources().getString(R.string.input_playlits_name), "", new MaterialDialog.InputCallback() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().freeMemory();
        Runtime.getRuntime().gc();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.album_detail_activity);
        search_main_background = (ImageView) findViewById(R.id.search_main_background);
        imagePositionOffset = MusicResource.imagePosition;
        anim = new RotateAnimation(MusicResource.imagePosition - imagePositionOffset,
                360f + MusicResource.imagePosition - imagePositionOffset,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(MusicResource.imageDuration);
        setContentView(R.layout.search_offline_activity);
        subArt = (CircleImageView) findViewById(R.id.subArt);
        subTitle = (TextView) findViewById(R.id.subTitle);
        subArtist = (TextView) findViewById(R.id.subArtist);
        subTitle.setSelected(true);
        subArtist.setSelected(true);
        sub_Play_btn = (Button) findViewById(R.id.subPlaybtn);
        sub_Next_btn = (ImageButton) findViewById(R.id.subNextbtn);
        sub_Back_btn = (ImageButton) findViewById(R.id.subBackbtn);
        subPlaySong = (LinearLayout) findViewById(R.id.subPlaySong);
        if (MusicResource.subPlaySongVisible) {
            subPlaySong.setVisibility(View.VISIBLE);
            subArt.startAnimation(anim);
        }
        search_main_background.setImageBitmap(MusicResource.blurredBitmap);
        albumDetailFragment = AlbumDetailFragment.newInstance(Glide.with(this));
        getSupportFragmentManager().beginTransaction().add(R.id.flSearchContent, albumDetailFragment, "albumDetailFragment").commit();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (MusicResource.isClickFolder) {
            toolbar.setTitle(getResources().getString(R.string.folder));
        } else if (MusicResource.checkPlaylistUse)
            toolbar.setTitle(getResources().getString(R.string.playlist_online));
        else if (MusicResource.checkPlaylistOfflineUse)
            toolbar.setTitle(getResources().getString(R.string.playlist_offline));
        else if (MusicResource.checkPlaylistShareUse) {
            toolbar.setTitle(getResources().getString(R.string.playlist_online));
        } else
            toolbar.setTitle(getResources().getString(R.string.album_detail));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter addToNewPlaylistOffline = new IntentFilter("addToNewPlaylistOfflineFromSearchAlbumDetailActivity");
        registerReceiver(addToNewPlaylistOfflineReceiver, addToNewPlaylistOffline);
        IntentFilter addToExistPlaylistOffline = new IntentFilter("addToExistPlaylistOfflineFromSearchAlbumDetailActivity");
        registerReceiver(addToExistPlaylistOfflineReceiver, addToExistPlaylistOffline);
        IntentFilter addToNewPlaylistOnline = new IntentFilter("addToNewPlaylistOnlineFromSearchAlbumDetailActivity");
        registerReceiver(addToNewPlaylistOnlineReceiver, addToNewPlaylistOnline);
        IntentFilter addToExistPlaylistOnline = new IntentFilter("addToExistPlaylistOnlineFromSearchAlbumDetailActivity");
        registerReceiver(addToExistPlaylistOnlineReceiver, addToExistPlaylistOnline);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.d("TestSearch", "onOptionsItemSelected");
        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicResource.isClickAlbum = false;
    }

    public static MaterialDialog.Builder playlistOnlineDialog;
    public static Dialog playListDialog;

    public static void addToPlaylistOnline(final Context context) {
        playlistOnlineDialog = new MaterialDialog.Builder(context).adapter(new PlaylistOnlineDialogAdapter(context, MusicResource.playlistOnlineDialog), null);
        playListDialog = playlistOnlineDialog.build();
        playListDialog.show();

    }

    public static void addToPlaylistOffline(final Context context) {
        playlistOnlineDialog = new MaterialDialog.Builder(context).adapter(new PlaylistOfflineDialogAdapter(context, MusicResource.playlistOnlineDialog), null);
        playListDialog = playlistOnlineDialog.build();
        playListDialog.show();

    }
}
