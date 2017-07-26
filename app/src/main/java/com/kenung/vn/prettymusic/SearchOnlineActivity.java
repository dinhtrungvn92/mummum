package com.kenung.vn.prettymusic;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineDialogAdapter;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;
import com.kenung.vn.prettymusic.search.csn_search.SearchOnlineResultFragment;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 14/05/2017.
 */

public class SearchOnlineActivity extends AppCompatActivity {
    private Intent intent;
    ImageView search_main_background;
    SearchOnlineResultFragment searchOnlineResultFragment;
    private CircleImageView subArt;
    private ImageButton sub_Next_btn;
    private ImageButton sub_Back_btn;
    private Button sub_Play_btn;
    private TextView subTitle;
    private TextView subArtist;
    private LinearLayout subPlaySong;
    private ProgressBar progressBar;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.search_online_activity);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        subArt = (CircleImageView) findViewById(R.id.subArt);
        subTitle = (TextView) findViewById(R.id.subTitle);
        subArtist = (TextView) findViewById(R.id.subArtist);
        subTitle.setSelected(true);
        subArtist.setSelected(true);
        sub_Play_btn = (Button) findViewById(R.id.subPlaybtn);
        sub_Next_btn = (ImageButton) findViewById(R.id.subNextbtn);
        sub_Back_btn = (ImageButton) findViewById(R.id.subBackbtn);
        subPlaySong = (LinearLayout) findViewById(R.id.subPlaySong);
        searchOnlineResultFragment = new SearchOnlineResultFragment();
        search_main_background = (ImageView) findViewById(R.id.search_main_background);
        search_main_background.setImageBitmap(MusicResource.blurredBitmap);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.search_online));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().add(R.id.flSearchContent, searchOnlineResultFragment, "searchOnlineResultFragment").commit();

    }

    BroadcastReceiver OnItemOnlineClickDone = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.ImageViewAnimatedChange(context, search_main_background, MusicResource.blurredBitmap);
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("OnItemOnlineClickDone");
        registerReceiver(OnItemOnlineClickDone, intentFilter);

        IntentFilter addToNewPlaylistOnline = new IntentFilter("addToNewPlaylistOnlineFromSearchActivity");
        registerReceiver(addToNewPlaylistOnlineReceiver, addToNewPlaylistOnline);
        IntentFilter addToExistPlaylistOnline = new IntentFilter("addToExistPlaylistOnlineFromSearchActivity");
        registerReceiver(addToExistPlaylistOnlineReceiver, addToExistPlaylistOnline);
    }

    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_item, menu);
        MenuItem itemSearch = menu.findItem(R.id.search);
        itemSearch.expandActionView();
        searchView = (SearchView) itemSearch.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.search_online));
        if (MusicResource.queryOnline != null)
            if (!MusicResource.queryOnline.equals(""))
                searchView.setQueryHint(MusicResource.queryOnline);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!MusicResource.network_state) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                } else {
                    MusicResource.queryOnline = query;
                    sendBroadcast(new Intent("noti_search_online_query_submit").putExtra("SearchQuery", query));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //displayFragmentCSNSearch();
//                sendBroadcast(new Intent("noti_searchquery_change").putExtra("SearchQuery", newText));
                return false;
            }
        });
        searchView.setIconifiedByDefault(true);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        Log.d("TestSearch", "onOptionsItemSelected");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.search).expandActionView();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        InputMethodManager inputManager = (InputMethodManager)
//                getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        Log.d("TestSearch", "onOptionsItemSelected");
        int id = item.getItemId();

        if (id == R.id.search) {
            return true;
        }
        if (id == android.R.id.home)
            onBackPressed();


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public static MaterialDialog.Builder playlistOnlineDialog;
    public static Dialog playListDialog;

    public static void addToPlaylistOnline(final Context context) {
        playlistOnlineDialog = new MaterialDialog.Builder(context).adapter(new PlaylistOnlineDialogAdapter(context, MusicResource.playlistOnlineDialog), null);
        playListDialog = playlistOnlineDialog.build();
        playListDialog.show();

    }
}
