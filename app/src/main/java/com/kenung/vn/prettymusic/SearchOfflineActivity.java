package com.kenung.vn.prettymusic;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.kenung.vn.prettymusic.music_offline.playlist.PlaylistOfflineDialogAdapter;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineDialogAdapter;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;
import com.kenung.vn.prettymusic.search.SearchOfflineResultFragment;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sev_user on 24-Mar-17.
 */

public class SearchOfflineActivity extends AppCompatActivity {
    ImageView search_main_background;
    SearchOfflineResultFragment searchResultFragment;
    private CircleImageView subArt;
    private ImageButton sub_Next_btn;
    private ImageButton sub_Back_btn;
    private Button sub_Play_btn;
    private TextView subTitle;
    private TextView subArtist;
    private LinearLayout subPlaySong;
    private ProgressBar progressBar;

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

    @Override
    protected void onResume() {
        super.onResume();
        MusicResource.ImageViewAnimatedChange(getApplicationContext(), search_main_background, MusicResource.blurredBitmap);

        IntentFilter addToNewPlaylistOffline = new IntentFilter("addToNewPlaylistOfflineFromSearchOfflineActivity");
        registerReceiver(addToNewPlaylistOfflineReceiver, addToNewPlaylistOffline);
        IntentFilter addToExistPlaylistOffline = new IntentFilter("addToExistPlaylistOfflineFromSearchOfflineActivity");
        registerReceiver(addToExistPlaylistOfflineReceiver, addToExistPlaylistOffline);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.search_offline_activity);
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

        MusicResource.search_offline_state = 0;
        searchResultFragment = new SearchOfflineResultFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.flSearchContent, searchResultFragment, "searchResultFragment").commit();
        search_main_background = (ImageView) findViewById(R.id.search_main_background);

        search_main_background.setImageBitmap(MusicResource.blurredBitmap);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.search_offline));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static SearchView searchView;

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

    @Override
    public void onBackPressed() {
        if (MusicResource.search_offline_state == 1) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            if (searchResultFragment.isAdded()) { // if the fragment is already in container
                ft.show(searchResultFragment);
            } else { // fragment needs to be added to frame container
                ft.add(R.id.flContent, searchResultFragment, "searchResultFragment");
            }
            final Fragment fragmentMusic = getSupportFragmentManager().findFragmentByTag("albumDetailFragment");
            if (fragmentMusic != null && fragmentMusic.isAdded()) {
                ft.hide(fragmentMusic);
            }
            ft.commit();
            MusicResource.search_offline_state = 0;
            searchView.setIconifiedByDefault(true);
            searchView.setFocusable(true);
            searchView.setIconified(false);
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_item, menu);
        MenuItem itemSearch = menu.findItem(R.id.search);
        itemSearch.expandActionView();
        searchView = (SearchView) itemSearch.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.search_offline));
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                if (searchResultFragment.isAdded()) { // if the fragment is already in container
                    ft.show(searchResultFragment);
                } else { // fragment needs to be added to frame container
                    ft.add(R.id.flContent, searchResultFragment, "searchResultFragment");
                }
                final Fragment fragmentMusic = getSupportFragmentManager().findFragmentByTag("albumDetailFragment");
                if (fragmentMusic != null && fragmentMusic.isAdded()) {
                    ft.hide(fragmentMusic);
                }
                ft.commit();
                MusicResource.search_offline_state = 0;
                sendBroadcast(new Intent("noti_searchquery_change").putExtra("SearchQuery", ""));
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                sendBroadcast(new Intent("noti_searchquery_submit").putExtra("SearchQuery", query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //displayFragmentCSNSearch();
                sendBroadcast(new Intent("noti_searchquery_change").putExtra("SearchQuery", newText));
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
            this.onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    public static MaterialDialog.Builder playlistOnlineDialog;
    public static Dialog playListDialog;

    public static void addToPlaylistOffline(final Context context) {
        playlistOnlineDialog = new MaterialDialog.Builder(context).adapter(new PlaylistOfflineDialogAdapter(context, MusicResource.playlistOnlineDialog), null);
        playListDialog = playlistOnlineDialog.build();
        playListDialog.show();

    }
}
