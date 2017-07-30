package com.kenung.vn.prettymusic;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kenung.vn.prettymusic.module.ImageHelper;
import com.kenung.vn.prettymusic.music_offline.MusicFragment;
import com.kenung.vn.prettymusic.music_offline.playlist.PlaylistOfflineDialogAdapter;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.c_pop.CPopFragment;
import com.kenung.vn.prettymusic.music_online.j_pop.JPopFragment;
import com.kenung.vn.prettymusic.music_online.k_pop.KPopFragment;
import com.kenung.vn.prettymusic.music_online.other.OtherFragment;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineDialogAdapter;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineFragment;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;
import com.kenung.vn.prettymusic.music_online.us_uk.USKFragment;
import com.kenung.vn.prettymusic.music_online.v_pop.VPopFragment;
import com.kenung.vn.prettymusic.search.SearchFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    long mLastClickTime = 0;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    static NavigationView nvDrawer;
    private ActionBarDrawerToggle toggle;
    private ImageView main_background;
    private CircleImageView subArt;
    private ImageButton sub_Next_btn;
    private ImageButton sub_Back_btn;
    private Button sub_Play_btn;
    private TextView subTitle;
    private TextView subArtist;
    private LinearLayout subPlaySong;
    private RotateAnimation anim;
    private Calendar calendar;
    private float imagePositionOffset = 0f;
    ProgressBar progressBar;

    private BroadcastReceiver receiverAudioFocus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (Player.player != null)
                if (Player.player.isPlaying()) {
                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.csn_Notification(context, null, null, null, "play");
                } else {
                    sub_Play_btn.setBackgroundResource(R.drawable.stop_btn);
                    MusicResource.csn_Notification(context, null, null, null, "pause");
                }

        }
    };

    private BroadcastReceiver receiverLoadBitmapLocal = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

            /*Log.d("TestAnim", "LoadBitmapDone");
            if (Player.player != null && !Player.player.isPlaying()) {
                sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                subArt.setAnimation(null);
            } else if (Player.player != null && Player.player.isPlaying()) {
                subArt.startAnimation(anim);

                sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

                calendar = Calendar.getInstance();
                MusicResource.startTime = calendar.get(Calendar.HOUR) * 3600000 +
                        calendar.get(Calendar.MINUTE) * 60000 +
                        calendar.get(Calendar.SECOND) * 1000 +
                        calendar.get(Calendar.MILLISECOND);
            }*/
        }
    };

    private BroadcastReceiver receiverAudioStateChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Player.player != null)
                if (Player.player.isPlaying())
                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                else sub_Play_btn.setBackgroundResource(R.drawable.stop_btn);
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

            Log.d("testBroacastReceiver", "Network Change");
        }
    };

    public Fragment fragmentMusic = null;
    public Fragment fragmentSearch = null;
    public Fragment fragmentVPop = null;
    public Fragment fragmentKPop = null;
    public Fragment fragmentJPop = null;
    public Fragment fragmentCPop = null;
    public Fragment fragmentUSK = null;
    public Fragment fragmentOther = null;
    public Fragment fragmentPlaylistOnline = null;
    public Fragment fragmentAboutUs = null;

    // Make sure to be using android.support.v7.app.ActionBarDrawerToggle version.
    // The android.support.v4.app.ActionBarDrawerToggle has been deprecated.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e("UserApp", "uncaught_exception_handler: uncaught exception in thread " + thread.getName(), ex);
                String ns = Context.NOTIFICATION_SERVICE;
                NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
                nMgr.cancel(MusicResource.notification_id);
                Player.player.stop();
                android.os.Process.killProcess(android.os.Process.myPid());
                if (ex instanceof RuntimeException)
                    throw (RuntimeException) ex;
                if (ex instanceof Error)
                    throw (Error) ex;


                Log.e("UserApp", "uncaught_exception handler: unable to rethrow checked exception");

            }
        });
        Intent stickyService = new Intent(this, ClosingService.class);
        startService(stickyService);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        appPackageName = getPackageName();
        MusicResource.chart = getString(R.string.chart);
        MusicResource.new_download = getString(R.string.new_download);
        MusicResource.new_upload = getString(R.string.new_upload);
        MusicResource.track = getString(R.string.track);
        MusicResource.album = getString(R.string.album);
        MusicResource.playlist = getString(R.string.playlist);
        MusicResource.folder = getString(R.string.folder);
        MusicResource.downloaded = getString(R.string.downloaded);
        MusicResource.singer = getString(R.string.singer);
        setContentView(R.layout.activity_main);
        Timer T = new Timer();
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Calendar c = Calendar.getInstance();
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);
                        Log.d("Timer", hour + "-" + minute);
                        String timer = hour + "-" + minute;
                        if (MusicResource.timer.equals(timer)) {
                            if (Player.player != null) {
                                if (Player.player.isPlaying()) {
                                    if (MusicResource.timerEnable)
                                        sendBroadcast(new Intent("noti_play_intent_action"));
                                }
                            }
                        }
                    }
                });
            }
        }, 0, 60000);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 12;

        MusicResource.mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
        //toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        // Set a Toolbar to replace the ActionBar.
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.OpenNavigation, R.string.CloseNavigation);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        subArt = (CircleImageView) findViewById(R.id.subArt);
        subTitle = (TextView) findViewById(R.id.subTitle);
        subArtist = (TextView) findViewById(R.id.subArtist);
        subTitle.setSelected(true);
        subArtist.setSelected(true);
        sub_Play_btn = (Button) findViewById(R.id.subPlaybtn);
        sub_Next_btn = (ImageButton) findViewById(R.id.subNextbtn);
        sub_Back_btn = (ImageButton) findViewById(R.id.subBackbtn);
        subPlaySong = (LinearLayout) findViewById(R.id.subPlaySong);
        sub_Play_btn.setOnClickListener(this);
        sub_Next_btn.setOnClickListener(this);
        sub_Back_btn.setOnClickListener(this);
        subPlaySong.setOnClickListener(this);
        main_background = (ImageView) findViewById(R.id.main_background);

        imagePositionOffset = MusicResource.imagePosition;

        anim = new RotateAnimation(MusicResource.imagePosition - imagePositionOffset,
                360f + MusicResource.imagePosition - imagePositionOffset,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(MusicResource.imageDuration);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Bitmap bitmapBlur = loadBitmap(this, "background_image");
        if (bitmapBlur != null) MusicResource.blurredBitmap = bitmapBlur;
        else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Drawable drawable = getResources().getDrawable(R.drawable.splash_screen);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, 200, 200, false);

            MusicResource.blurredBitmap = BlurBuilder.blur(this,
                    bitmapResized);
        }
//        main_background.setImageBitmap(MusicResource.blurredBitmap);

        MusicResource.MODE = 0;

        if (Player.player != null) {

        } else {
            MusicResource.subArt = BitmapFactory.decodeResource(getResources(), R.drawable.music_cover_default);
            subArt.setImageBitmap(MusicResource.subArt);
        }

        nvDrawer = (NavigationView) findViewById(R.id.nvView);
//        View headerView = nvDrawer.getHeaderView(0);
//        ImageView imageHeader = (ImageView) headerView.findViewById(R.id.imageBlur);
//        Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                R.drawable.headerimage);
//        imageHeader.setImageBitmap(icon);
        setupDrawerContent(nvDrawer);

        fragmentMusic = new MusicFragment().newInstance();
        fragmentSearch = new SearchFragment().newInstance();
        fragmentVPop = new VPopFragment().newInstance();
        fragmentKPop = new KPopFragment().newInstance();
        fragmentJPop = new JPopFragment().newInstance();
        fragmentCPop = new CPopFragment().newInstance();
        fragmentUSK = new USKFragment().newInstance();
        fragmentOther = new OtherFragment().newInstance();
        fragmentPlaylistOnline = new PlaylistOnlineFragment();
        fragmentAboutUs = new AboutUsFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.flContent, fragmentMusic, "fragmentMusic").commit();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(HeadSetReceiver, filter);
        SharedPreferences sharedPreferences = getSharedPreferences("my_data", MODE_PRIVATE);
        MusicResource.qualityLevel = sharedPreferences.getString("qualityLevel", "128kbps");
        MusicResource.repeat_mode = sharedPreferences.getInt("repeat_mode", 0);
        MusicResource.shuffle = sharedPreferences.getBoolean("shuffle", false);
        MusicResource.folderDownload = sharedPreferences.getString("folder", "");
        Set<String> set = sharedPreferences.getStringSet("listOfPath", null);
        if (set != null) {
            if (!set.contains(MusicResource.folderDownload) && !MusicResource.folderDownload.equals(""))
                MusicResource.listOfPathDownload.add(MusicResource.folderDownload);
            MusicResource.listOfPathDownload.addAll(set);
        } else {
            if (!MusicResource.folderDownload.equals(""))
                MusicResource.listOfPathDownload.add(MusicResource.folderDownload);
        }

        try {
            File file = new File(getDir("FMP", MODE_PRIVATE), "hashMapPlaylistOnline");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            MusicResource.hashMapPlaylistOnline = (HashMap<String, ArrayList<Track>>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            File file = new File(getDir("FMP", MODE_PRIVATE), "playlistOnline");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            MusicResource.playlistOnline = (ArrayList<PlaylistOnlineModel>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            File file = new File(getDir("FMP", MODE_PRIVATE), "hashMapPlaylistOffline");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            MusicResource.hashMapPlaylistOffline = (HashMap<String, ArrayList<Song>>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            File file = new File(getDir("FMP", MODE_PRIVATE), "playlistOffline");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            MusicResource.playlistOffline = (ArrayList<PlaylistOnlineModel>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        selectDrawerItem(item);
                        return true;
                    }
                }
        );
    }

    protected void displayFragmentMusic() {
        itemSearch.setVisible(true);
        MusicResource.online_offline_toogle = 0;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragmentMusic.isAdded()) { // if the fragment is already in container
            ft.show(fragmentMusic);
        } else { // fragment needs to be added to frame container
            ft.add(R.id.flContent, fragmentMusic, "fragmentMusic");
        }
        if (fragmentSearch.isAdded()) {
            ft.hide(fragmentSearch);
        }
        if (fragmentVPop.isAdded()) {
            ft.hide(fragmentVPop);
        }
        if (fragmentKPop.isAdded()) {
            ft.hide(fragmentKPop);
        }
        if (fragmentJPop.isAdded()) {
            ft.hide(fragmentJPop);
        }
        if (fragmentCPop.isAdded()) {
            ft.hide(fragmentCPop);
        }
        if (fragmentUSK.isAdded()) {
            ft.hide(fragmentUSK);
        }
        if (fragmentOther.isAdded()) {
            ft.hide(fragmentOther);
        }
        if (fragmentAboutUs.isAdded()) {
            ft.hide(fragmentAboutUs);
        }
        if (fragmentPlaylistOnline.isAdded()) {
            ft.hide(fragmentPlaylistOnline);
        }
        if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment") != null) {
            if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment").isAdded()) {
                ft.remove(getSupportFragmentManager().findFragmentByTag("albumDetailFragment"));
            }
        }
        // Commit changes
        ft.commit();
    }

    protected void displayFragmentVPop() {
        itemSearch.setVisible(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.vietnamese_music));
        MusicResource.online_offline_toogle = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragmentVPop.isAdded()) {
            fragmentVPop.onResume();
            ft.show(fragmentVPop);
        } else {
            ft.add(R.id.flContent, fragmentVPop, "fragmentVPop");
        }
        if (fragmentMusic.isAdded()) { // if the fragment is already in container
            ft.hide(fragmentMusic);
        }
        if (fragmentSearch.isAdded()) {
            ft.hide(fragmentSearch);
        }
        if (fragmentKPop.isAdded()) {
            ft.hide(fragmentKPop);
        }
        if (fragmentJPop.isAdded()) {
            ft.hide(fragmentJPop);
        }
        if (fragmentCPop.isAdded()) {
            ft.hide(fragmentCPop);
        }
        if (fragmentUSK.isAdded()) {
            ft.hide(fragmentUSK);
        }
        if (fragmentAboutUs.isAdded()) {
            ft.hide(fragmentAboutUs);
        }
        if (fragmentOther.isAdded()) {
            ft.hide(fragmentOther);
        }
        if (fragmentPlaylistOnline.isAdded()) {
            ft.hide(fragmentPlaylistOnline);
        }
        if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment") != null) {
            if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment").isAdded()) {
                ft.remove(getSupportFragmentManager().findFragmentByTag("albumDetailFragment"));
            }
        }
        // Commit changes
        ft.commit();
    }

    protected void displayFragmentKPop() {
        itemSearch.setVisible(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.korean_music));
        MusicResource.online_offline_toogle = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragmentKPop.isAdded()) {
            fragmentKPop.onResume();
            ft.show(fragmentKPop);
        } else {
            ft.add(R.id.flContent, fragmentKPop, "fragmentKPop");
        }
        if (fragmentSearch.isAdded()) {
            ft.hide(fragmentSearch);
        }
        if (fragmentMusic.isAdded()) {
            ft.hide(fragmentMusic);
        }
        if (fragmentVPop.isAdded()) {
            ft.hide(fragmentVPop);
        }
        if (fragmentJPop.isAdded()) {
            ft.hide(fragmentJPop);
        }
        if (fragmentCPop.isAdded()) {
            ft.hide(fragmentCPop);
        }
        if (fragmentUSK.isAdded()) {
            ft.hide(fragmentUSK);
        }
        if (fragmentOther.isAdded()) {
            ft.hide(fragmentOther);
        }
        if (fragmentAboutUs.isAdded()) {
            ft.hide(fragmentAboutUs);
        }
        if (fragmentPlaylistOnline.isAdded()) {
            ft.hide(fragmentPlaylistOnline);
        }
        if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment") != null) {
            if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment").isAdded()) {
                ft.remove(getSupportFragmentManager().findFragmentByTag("albumDetailFragment"));
            }
        }
        // Commit changes
        ft.commit();
    }

    protected void displayFragmentJPop() {
        itemSearch.setVisible(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.japanese_music));
        MusicResource.online_offline_toogle = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragmentJPop.isAdded()) {
            fragmentJPop.onResume();
            ft.show(fragmentJPop);
        } else {
            ft.add(R.id.flContent, fragmentJPop, "fragmentJPop");
        }
        if (fragmentKPop.isAdded()) {
            ft.hide(fragmentKPop);
        }
        if (fragmentSearch.isAdded()) {
            ft.hide(fragmentSearch);
        }
        if (fragmentMusic.isAdded()) {
            ft.hide(fragmentMusic);
        }
        if (fragmentVPop.isAdded()) {
            ft.hide(fragmentVPop);
        }
        if (fragmentCPop.isAdded()) {
            ft.hide(fragmentCPop);
        }
        if (fragmentUSK.isAdded()) {
            ft.hide(fragmentUSK);
        }
        if (fragmentAboutUs.isAdded()) {
            ft.hide(fragmentAboutUs);
        }
        if (fragmentOther.isAdded()) {
            ft.hide(fragmentOther);
        }
        if (fragmentPlaylistOnline.isAdded()) {
            ft.hide(fragmentPlaylistOnline);
        }
        if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment") != null) {
            if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment").isAdded()) {
                ft.remove(getSupportFragmentManager().findFragmentByTag("albumDetailFragment"));
            }
        }
        // Commit changes
        ft.commit();
    }

    protected void displayFragmentCPop() {
        itemSearch.setVisible(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.chinese_music));
        MusicResource.online_offline_toogle = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragmentCPop.isAdded()) {
            fragmentCPop.onResume();
            ft.show(fragmentCPop);
        } else {
            ft.add(R.id.flContent, fragmentCPop, "fragmentCPop");
        }
        if (fragmentJPop.isAdded()) {
            ft.hide(fragmentJPop);
        }
        if (fragmentKPop.isAdded()) {
            ft.hide(fragmentKPop);
        }
        if (fragmentSearch.isAdded()) {
            ft.hide(fragmentSearch);
        }
        if (fragmentAboutUs.isAdded()) {
            ft.hide(fragmentAboutUs);
        }
        if (fragmentMusic.isAdded()) {
            ft.hide(fragmentMusic);
        }
        if (fragmentVPop.isAdded()) {
            ft.hide(fragmentVPop);
        }
        if (fragmentUSK.isAdded()) {
            ft.hide(fragmentUSK);
        }
        if (fragmentOther.isAdded()) {
            ft.hide(fragmentOther);
        }
        if (fragmentPlaylistOnline.isAdded()) {
            ft.hide(fragmentPlaylistOnline);
        }
        if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment") != null) {
            if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment").isAdded()) {
                ft.remove(getSupportFragmentManager().findFragmentByTag("albumDetailFragment"));
            }
        }
        // Commit changes
        ft.commit();
    }

    protected void displayFragmentAboutUs() {
        itemSearch.setVisible(false);
        getSupportActionBar().setTitle(getResources().getString(R.string.about_us_small));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragmentAboutUs.isAdded()) {
            fragmentAboutUs.onResume();
            ft.show(fragmentAboutUs);
        } else {
            ft.add(R.id.flContent, fragmentAboutUs, "fragmentAboutUs");
        }
        if (fragmentSearch.isAdded()) {
            ft.hide(fragmentSearch);
        }
        if (fragmentMusic.isAdded()) {
            ft.hide(fragmentMusic);
        }
        if (fragmentVPop.isAdded()) {
            ft.hide(fragmentVPop);
        }
        if (fragmentJPop.isAdded()) {
            ft.hide(fragmentJPop);
        }
        if (fragmentCPop.isAdded()) {
            ft.hide(fragmentCPop);
        }
        if (fragmentUSK.isAdded()) {
            ft.hide(fragmentUSK);
        }
        if (fragmentKPop.isAdded()) {
            ft.hide(fragmentKPop);
        }
        if (fragmentOther.isAdded()) {
            ft.hide(fragmentOther);
        }
        if (fragmentPlaylistOnline.isAdded()) {
            ft.hide(fragmentPlaylistOnline);
        }
        if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment") != null) {
            if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment").isAdded()) {
                ft.remove(getSupportFragmentManager().findFragmentByTag("albumDetailFragment"));
            }
        }
        ft.commit();
    }

    protected void displayFragmentUSK() {
        itemSearch.setVisible(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.us_uk_music));
        MusicResource.online_offline_toogle = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragmentUSK.isAdded()) {
            fragmentUSK.onResume();
            ft.show(fragmentUSK);
        } else {
            ft.add(R.id.flContent, fragmentUSK, "fragmentUSK");
        }
        if (fragmentSearch.isAdded()) {
            ft.hide(fragmentSearch);
        }
        if (fragmentAboutUs.isAdded()) {
            ft.hide(fragmentAboutUs);
        }
        if (fragmentMusic.isAdded()) {
            ft.hide(fragmentMusic);
        }
        if (fragmentVPop.isAdded()) {
            ft.hide(fragmentVPop);
        }
        if (fragmentKPop.isAdded()) {
            ft.hide(fragmentKPop);
        }
        if (fragmentJPop.isAdded()) {
            ft.hide(fragmentJPop);
        }
        if (fragmentCPop.isAdded()) {
            ft.hide(fragmentCPop);
        }
        if (fragmentOther.isAdded()) {
            ft.hide(fragmentOther);
        }
        if (fragmentPlaylistOnline.isAdded()) {
            ft.hide(fragmentPlaylistOnline);
        }
        if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment") != null) {
            if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment").isAdded()) {
                ft.remove(getSupportFragmentManager().findFragmentByTag("albumDetailFragment"));
            }
        }
        // Commit changes
        ft.commit();
    }

    protected void displayFragmentOther() {
        itemSearch.setVisible(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.other_music));
        MusicResource.online_offline_toogle = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragmentOther.isAdded()) {
            fragmentOther.onResume();
            ft.show(fragmentOther);
        } else {
            ft.add(R.id.flContent, fragmentOther, "fragmentOther");
        }
        if (fragmentUSK.isAdded()) {
            ft.hide(fragmentUSK);
        }
        if (fragmentSearch.isAdded()) {
            ft.hide(fragmentSearch);
        }
        if (fragmentAboutUs.isAdded()) {
            ft.hide(fragmentAboutUs);
        }
        if (fragmentMusic.isAdded()) {
            ft.hide(fragmentMusic);
        }
        if (fragmentVPop.isAdded()) {
            ft.hide(fragmentVPop);
        }
        if (fragmentKPop.isAdded()) {
            ft.hide(fragmentKPop);
        }
        if (fragmentJPop.isAdded()) {
            ft.hide(fragmentJPop);
        }
        if (fragmentCPop.isAdded()) {
            ft.hide(fragmentCPop);
        }
        if (fragmentUSK.isAdded()) {
            ft.hide(fragmentUSK);
        }
        if (fragmentPlaylistOnline.isAdded()) {
            ft.hide(fragmentPlaylistOnline);
        }
        if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment") != null) {
            if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment").isAdded()) {
                ft.remove(getSupportFragmentManager().findFragmentByTag("albumDetailFragment"));
            }
        }
        // Commit changes
        ft.commit();
    }

    protected void displayFragmentPlaylistOnline() {
        itemSearch.setVisible(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.playlist_online));
        MusicResource.online_offline_toogle = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragmentPlaylistOnline.isAdded()) {
            fragmentPlaylistOnline.onResume();
            ft.show(fragmentPlaylistOnline);
        } else {
            ft.add(R.id.flContent, fragmentPlaylistOnline, "fragmentPlayOnline");
        }
        if (fragmentOther.isAdded()) {
            ft.hide(fragmentOther);
        }
        if (fragmentUSK.isAdded()) {
            ft.hide(fragmentUSK);
        }
        if (fragmentAboutUs.isAdded()) {
            ft.hide(fragmentAboutUs);
        }
        if (fragmentSearch.isAdded()) {
            ft.hide(fragmentSearch);
        }
        if (fragmentMusic.isAdded()) {
            ft.hide(fragmentMusic);
        }
        if (fragmentVPop.isAdded()) {
            ft.hide(fragmentVPop);
        }
        if (fragmentKPop.isAdded()) {
            ft.hide(fragmentKPop);
        }
        if (fragmentJPop.isAdded()) {
            ft.hide(fragmentJPop);
        }
        if (fragmentCPop.isAdded()) {
            ft.hide(fragmentCPop);
        }
        if (fragmentUSK.isAdded()) {
            ft.hide(fragmentUSK);
        }
        if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment") != null) {
            if (getSupportFragmentManager().findFragmentByTag("albumDetailFragment").isAdded()) {
                ft.remove(getSupportFragmentManager().findFragmentByTag("albumDetailFragment"));
            }
        }
        // Commit changes
        ft.commit();
    }

    String appPackageName = "";

    public void selectDrawerItem(MenuItem menuItem) {
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (menuItem.getItemId()) {
            case R.id.nav_music_offline_album:
                MusicResource.album_track_toogle = 0;
                displayFragmentMusic();
//                ft.replace(R.id.flContent, fragmentMusic);
                sendBroadcast(new Intent("noti_change_album_track"));
                break;
            case R.id.nav_music_offline_track:
                MusicResource.album_track_toogle = 1;
                displayFragmentMusic();
                sendBroadcast(new Intent("noti_change_album_track"));
                break;
            case R.id.nav_music_offline_playlist:
                MusicResource.album_track_toogle = 2;
                displayFragmentMusic();
                sendBroadcast(new Intent("noti_change_album_track"));
                break;
            case R.id.nav_music_offline_folder:
                MusicResource.album_track_toogle = 3;
                displayFragmentMusic();
                sendBroadcast(new Intent("noti_change_album_track"));
                break;
            case R.id.nav_music_offline_downloaded:
                MusicResource.album_track_toogle = 4;
                displayFragmentMusic();
                sendBroadcast(new Intent("noti_change_album_track"));
                break;
            case R.id.nav_music_online_v_pop:
                displayFragmentVPop();
//                ft.replace(R.id.flContent, fragmentVPop);
                break;
            case R.id.aboutUs:
                displayFragmentAboutUs();
                break;
            case R.id.nav_music_online_k_pop:
                displayFragmentKPop();
                break;
            case R.id.nav_music_online_j_pop:
                displayFragmentJPop();
                break;
            case R.id.nav_music_online_c_pop:
                displayFragmentCPop();
                break;
            case R.id.nav_music_online_us_uk:
                displayFragmentUSK();
                break;
            case R.id.nav_music_online_other:
                displayFragmentOther();
                break;
            case R.id.nav_music_online_playlist:
                displayFragmentPlaylistOnline();
                break;
            case R.id.Share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + appPackageName);
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_via)));
                break;
            case R.id.VoteUs:
                // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            default:
                displayFragmentMusic();
                break;

        }
//        ft.commit();
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());


        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    MenuItem itemSearch;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        itemSearch = menu.findItem(R.id.search);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.search) {
            switch (MusicResource.online_offline_toogle) {
                case 0:
                    startActivity(new Intent(getApplicationContext(), SearchOfflineActivity.class));
                    return true;
                case 1:
                    startActivity(new Intent(getApplicationContext(), SearchOnlineActivity.class));
                    return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void setDefaultImage() {
        Drawable drawable = getResources().getDrawable(R.drawable.nice_view_main_background_2);
        MusicResource.subArt = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(MusicResource.subArt, 200, 200, false);
        MusicResource.blurredBitmap = BlurBuilder.blur(this,
                bitmapResized);
        MusicResource.noti_art = ImageHelper.getRoundedCornerBitmap(MusicResource.subArt, 30);
        subArt.setImageBitmap(MusicResource.subArt);
    }

    public void doNext() {

        if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
            if (!MusicResource.network_state) {
                Toast.makeText(this, getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            if (Player.player != null)
                if (Player.player.isPlaying()) {
                    Player.player.stop();
                    Player.player.reset();
                }
            progressBar.setVisibility(View.VISIBLE);
            MusicResource.isCanChange = false;
            subPlaySong.setEnabled(false);
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
            MusicResource.subTitle = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getTitle();
            MusicResource.subArtist = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getArtist();
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new CSN_getTrackDetail().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getUrl());
            } else {
                new CSN_getTrackDetail().execute(
                        hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getUrl());
            }
        } else {
            if (Player.player == null) return;
            if (Player.player.isPlaying()) {
                Player.player.stop();
                Player.player.reset();
            }

            switch (MusicResource.MODE) {
                case 0: {
                    MusicResource.songPosition = (MusicResource.songPosition < MusicResource.songList.size() - 1) ? MusicResource.songPosition + 1 : 0;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.songList.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (MusicResource.songList.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.songList.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.songList.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }
                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.songList.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.songList.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.songList.get(MusicResource.songPosition).getTitle(),
                            MusicResource.songList.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;

                case 1: {
                    MusicResource.songPosition = (MusicResource.songPosition < MusicResource.folderSelected.size() - 1) ? MusicResource.songPosition + 1 : 0;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.folderSelected.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.folderSelected.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.folderSelected.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.folderSelected.get(MusicResource.songPosition).getTitle(),
                            MusicResource.folderSelected.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;


                case 2: {
                    MusicResource.songPosition = (MusicResource.songPosition < MusicResource.songListDownload.size() - 1) ? MusicResource.songPosition + 1 : 0;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.songListDownload.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.songListDownload.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.songListDownload.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.songListDownload.get(MusicResource.songPosition).getTitle(),
                            MusicResource.songListDownload.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;

                case 30: {
                    MusicResource.songPosition = (MusicResource.songPosition < MusicResource.albumCollectionContent.size() - 1) ? MusicResource.songPosition + 1 : 0;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.albumCollectionContent.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.albumCollectionContent.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.albumCollectionContent.get(MusicResource.songPosition).getTitle(),
                            MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;
                case 31: {
                    MusicResource.songPosition = (MusicResource.songPosition < MusicResource.playlistOfflineSelected.size() - 1) ? MusicResource.songPosition + 1 : 0;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getTitle(),
                            MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;
                case 4: {
                    MusicResource.songPosition = (MusicResource.songPosition < MusicResource.searchResultList.size() - 1) ? MusicResource.songPosition + 1 : 0;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.searchResultList.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.searchResultList.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.searchResultList.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;

                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.searchResultList.get(MusicResource.songPosition).getTitle(),
                            MusicResource.searchResultList.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;
                default:
                    break;
            }
            ImageViewAnimatedChange(this, main_background, MusicResource.blurredBitmap);
            if (MusicResource.request_audio_focus(getApplicationContext())) {
                Player.player.start();
                Player.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        if (MusicResource.repeat_mode == 0) {
                            if (MusicResource.shuffle)
                                sendBroadcast(new Intent("intentFilterDoShuffle"));
                            else
                                sendBroadcast(new Intent("intentFilterDoNext"));
                        } else {
                            sendBroadcast(new Intent("intentFilterDoRepeat"));
                        }
                    }
                });
            }
            Log.d("dochange1", "dochange");
            sendBroadcast(new Intent("intentFilterDoNextOnPlayActivity"));
        }
    }

    public void doRepeat() {

        Player.player.start();
        Player.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                if (MusicResource.repeat_mode == 0) {
                    if (MusicResource.shuffle)
                        sendBroadcast(new Intent("intentFilterDoShuffle"));
                    else
                        sendBroadcast(new Intent("intentFilterDoNext"));
                } else {
                    sendBroadcast(new Intent("intentFilterDoRepeat"));
                }
            }
        });
        sendBroadcast(new Intent("intentFilterDoNextOnPlayActivity"));
    }

    public void doShuffle() {

        if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
            if (!MusicResource.network_state) {
                Toast.makeText(this, getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            if (Player.player != null)
                if (Player.player.isPlaying()) {
                    Player.player.stop();
                    Player.player.reset();
                }
            progressBar.setVisibility(View.VISIBLE);
            MusicResource.isCanChange = false;
            subPlaySong.setEnabled(false);
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
            Random ran = new Random();
            MusicResource.songPosition = ran.nextInt(hashMap.get(MusicResource.MODE).size());

            MusicResource.subTitle = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getTitle();
            MusicResource.subArtist = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getArtist();
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new CSN_getTrackDetail().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getUrl());
            } else {
                new CSN_getTrackDetail().execute(
                        hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getUrl());
            }
        } else {
            if (Player.player == null) return;
            if (Player.player.isPlaying()) {
                Player.player.stop();
                Player.player.reset();
            }
            MusicResource.noti_art = ImageHelper.getRoundedCornerBitmap(MusicResource.subArt, 30);
            switch (MusicResource.MODE) {
                case 0: {
                    Random ran = new Random();
                    MusicResource.songPosition = ran.nextInt(MusicResource.songList.size());
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.songList.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (MusicResource.songList.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.songList.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.songList.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }

                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.songList.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.songList.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.songList.get(MusicResource.songPosition).getTitle(),
                            MusicResource.songList.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;

                case 1: {
                    Random ran = new Random();
                    MusicResource.songPosition = ran.nextInt(MusicResource.folderSelected.size());
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.folderSelected.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.folderSelected.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.folderSelected.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.folderSelected.get(MusicResource.songPosition).getTitle(),
                            MusicResource.folderSelected.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;

                case 2: {
                    Random ran = new Random();
                    MusicResource.songPosition = ran.nextInt(MusicResource.songListDownload.size());
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.songListDownload.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.songListDownload.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.songListDownload.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.songListDownload.get(MusicResource.songPosition).getTitle(),
                            MusicResource.songListDownload.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;
                case 30: {
                    Random ran = new Random();
                    MusicResource.songPosition = ran.nextInt(MusicResource.albumCollectionContent.size());
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.albumCollectionContent.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.albumCollectionContent.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);

                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.albumCollectionContent.get(MusicResource.songPosition).getTitle(),
                            MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;
                case 4: {
                    Random ran = new Random();
                    MusicResource.songPosition = ran.nextInt(MusicResource.searchResultList.size());
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.searchResultList.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.searchResultList.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.searchResultList.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.searchResultList.get(MusicResource.songPosition).getTitle(),
                            MusicResource.searchResultList.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;
                case 31: {
                    Random ran = new Random();
                    MusicResource.songPosition = ran.nextInt(MusicResource.playlistOfflineSelected.size());
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getTitle(),
                            MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;
                default:
                    break;
            }
            ImageViewAnimatedChange(this, main_background, MusicResource.blurredBitmap);
            if (MusicResource.request_audio_focus(getApplicationContext())) {
                Player.player.start();
                Player.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        if (MusicResource.repeat_mode == 0) {
                            if (MusicResource.shuffle)
                                sendBroadcast(new Intent("intentFilterDoShuffle"));
                            else
                                sendBroadcast(new Intent("intentFilterDoNext"));
                        } else {
                            sendBroadcast(new Intent("intentFilterDoRepeat"));
                        }
                    }
                });
            }
            sendBroadcast(new Intent("intentFilterDoShuffleOnPlayActivity"));
        }
    }

    public void doBack() {


//                    subPlaySong.setEnabled(false);

        if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
            if (!MusicResource.network_state) {
                Toast.makeText(this, getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            if (Player.player != null)
                if (Player.player.isPlaying()) {
                    Player.player.stop();
                    Player.player.reset();
                }
            progressBar.setVisibility(View.VISIBLE);
            MusicResource.isCanChange = false;
            subPlaySong.setEnabled(false);
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
            MusicResource.subTitle = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getTitle();
            MusicResource.subArtist = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getArtist();
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new CSN_getTrackDetail().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getUrl());
            } else {
                new CSN_getTrackDetail().execute(
                        hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getUrl());
            }
        } else {
            if (Player.player == null) return;
            if (Player.player.isPlaying()) {
                Player.player.stop();
                Player.player.reset();
            }
            MusicResource.noti_art = ImageHelper.getRoundedCornerBitmap(MusicResource.subArt, 30);
            switch (MusicResource.MODE) {
                case 0: {

                    MusicResource.songPosition = (MusicResource.songPosition > 0) ? MusicResource.songPosition - 1 : MusicResource.songList.size() - 1;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.songList.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (MusicResource.songList.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.songList.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.songList.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }
                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

                    MusicResource.subTitle = MusicResource.songList.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.songList.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.songList.get(MusicResource.songPosition).getTitle(),
                            MusicResource.songList.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;


                case 1: {

                    MusicResource.songPosition = (MusicResource.songPosition > 0) ? MusicResource.songPosition - 1 : MusicResource.folderSelected.size() - 1;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.folderSelected.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }
                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

                    MusicResource.subTitle = MusicResource.folderSelected.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.folderSelected.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.folderSelected.get(MusicResource.songPosition).getTitle(),
                            MusicResource.folderSelected.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;


                case 2: {

                    MusicResource.songPosition = (MusicResource.songPosition > 0) ? MusicResource.songPosition - 1 : MusicResource.songListDownload.size() - 1;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.songListDownload.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

                    MusicResource.subTitle = MusicResource.songListDownload.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.songListDownload.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.songListDownload.get(MusicResource.songPosition).getTitle(),
                            MusicResource.songListDownload.get(MusicResource.songPosition).getArtist(), "play");
                }

                break;
                case 4: {
                    MusicResource.songPosition = (MusicResource.songPosition > 0) ? MusicResource.songPosition - 1 : MusicResource.searchResultList.size() - 1;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.searchResultList.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                    MusicResource.subTitle = MusicResource.searchResultList.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.searchResultList.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);

                    MusicResource.rotationArt = 0;

                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.searchResultList.get(MusicResource.songPosition).getTitle(),
                            MusicResource.searchResultList.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;
                case 30: {
                    MusicResource.songPosition = (MusicResource.songPosition > 0) ? MusicResource.songPosition - 1 : MusicResource.albumCollectionContent.size() - 1;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.albumCollectionContent.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }


                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

                    MusicResource.subTitle = MusicResource.albumCollectionContent.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);

                    MusicResource.rotationArt = 0;

                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.albumCollectionContent.get(MusicResource.songPosition).getTitle(),
                            MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;
                case 31: {

                    MusicResource.songPosition = (MusicResource.songPosition > 0) ? MusicResource.songPosition - 1 : MusicResource.playlistOfflineSelected.size() - 1;
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                            .EXTERNAL_CONTENT_URI, MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getId());
                    Player.player = MediaPlayer.create(getApplicationContext(), uri);
                    if (Player.player == null) {
                        Toast.makeText(this, getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri() != null) {
                        Bitmap temp = decodeSampledBitmapFromUri(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), 512, 512);
                        if (temp != null) {
//                            loadBitmap(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), subArt);
                            MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), temp
                            );
                            MusicResource.noti_art = temp;
                            MusicResource.subArt = temp;
                            subArt.setImageBitmap(MusicResource.subArt);
                        } else {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }
                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

                    MusicResource.subTitle = MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getTitle();
                    MusicResource.subArtist = MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArtist();
                    subTitle.setText(MusicResource.subTitle);
                    subArtist.setText(MusicResource.subArtist);
                    MusicResource.rotationArt = 0;


                    MusicResource.csn_Notification(getApplication(),
                            MusicResource.noti_art,
                            MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getTitle(),
                            MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArtist(), "play");
                }
                break;
                default:
                    break;
            }
            ImageViewAnimatedChange(this, main_background, MusicResource.blurredBitmap);
            if (MusicResource.request_audio_focus(getApplicationContext())) {
                Player.player.start();
                Player.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        if (MusicResource.repeat_mode == 0) {
                            if (MusicResource.shuffle)
                                sendBroadcast(new Intent("intentFilterDoShuffle"));
                            else
                                sendBroadcast(new Intent("intentFilterDoNext"));
                        } else {
                            sendBroadcast(new Intent("intentFilterDoRepeat"));
                        }
                    }
                });
            }
            sendBroadcast(new Intent("intentFilterDoNextOnPlayActivity"));
        }
    }

    @Override
    public void onClick(View v) {
        if (Player.player != null) {
            switch (v.getId()) {
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

                    startActivity(new Intent(getApplicationContext(), PlayActivity.class));
                    overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                }
                break;
                case R.id.subPlaybtn: {
                    if (MusicResource.isCanChange) {
                        if (Player.player != null)
                            if (Player.player.isPlaying()) {
                                sub_Play_btn.setBackgroundResource(R.drawable.stop_btn);

                                calendar = Calendar.getInstance();
                                MusicResource.endTime = calendar.get(Calendar.HOUR) * 3600000 +
                                        calendar.get(Calendar.MINUTE) * 60000 +
                                        calendar.get(Calendar.SECOND) * 1000 +
                                        calendar.get(Calendar.MILLISECOND);
                                MusicResource.activeTime = MusicResource.endTime - MusicResource.startTime;
                                MusicResource.imagePosition += (360f * MusicResource.activeTime) / 20000f;
                                if (MusicResource.imagePosition >= 360)
                                    MusicResource.imagePosition -= 360f;

                                subArt.setRotation(MusicResource.imagePosition);

                                anim.reset();

                                subArt.setAnimation(null);

                                MusicResource.sub_anim_running = false;

                                Player.player.pause();
                                MusicResource.csn_Notification(getApplication(),
                                        null,
                                        null,
                                        null, "pause");
                            } else {
                                if (MusicResource.request_audio_focus(getApplicationContext())) {
                                    Player.player.start();
                                    subArt.setRotation(MusicResource.imagePosition);
                                    subArt.startAnimation(anim);

                                    MusicResource.sub_anim_running = true;

                                    calendar = Calendar.getInstance();
                                    MusicResource.startTime = calendar.get(Calendar.HOUR) * 3600000 +
                                            calendar.get(Calendar.MINUTE) * 60000 +
                                            calendar.get(Calendar.SECOND) * 1000 +
                                            calendar.get(Calendar.MILLISECOND);

                                    sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
                                    MusicResource.csn_Notification(getApplication(),
                                            null,
                                            null,
                                            null, "play");
                                }
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
                        if (MusicResource.shuffle) doShuffle();
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
                        if (MusicResource.shuffle) doShuffle();
                        else
                            doBack();
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicResource.set_src_complete = false;
        if (Player.player != null) {
            Player.player.reset();
            Player.player = null;
        }

        saveFile(this, MusicResource.blurredBitmap, "background_image");
        unregisterReceiver(receiverAudioFocus);
        unregisterReceiver(receiverAudioStateChange);
        unregisterReceiver(receiverNetworkChange);
        unregisterReceiver(receiverLoadBitmapLocal);
        unregisterReceiver(DoBackReceiver);
        unregisterReceiver(DoNextReceiver);

        try {
            File file = new File(getDir("FMP", MODE_PRIVATE), "hashMapPlaylistOnline");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(MusicResource.hashMapPlaylistOnline);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File file = new File(getDir("FMP", MODE_PRIVATE), "playlistOnline");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(MusicResource.playlistOnline);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            File file = new File(getDir("FMP", MODE_PRIVATE), "hashMapPlaylistOffline");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(MusicResource.hashMapPlaylistOffline);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File file = new File(getDir("FMP", MODE_PRIVATE), "playlistOffline");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(MusicResource.playlistOffline);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Set<String> set = new HashSet<String>();
        set.addAll(MusicResource.listOfPathDownload);
        SharedPreferences sharedPreferences = getSharedPreferences("my_data", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putStringSet("listOfPath", set);
        edit.putInt("repeat_mode", MusicResource.repeat_mode);
        edit.putBoolean("shuffle", MusicResource.shuffle);
        edit.putString("folder", MusicResource.folderDownload);
        edit.putString("qualityLevel", MusicResource.qualityLevel);
        edit.commit();
    }

    BroadcastReceiver HeadSetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d("Headphone", "changeState0");
                        MusicResource.isPlugHeadPhone = false;
                        if (Player.player != null)
                            if (Player.player.isPlaying()) {
                                context.sendBroadcast(new Intent("noti_play_intent_action"));
                                MusicResource.isPlayingBeforeUnPlugHeadphone = true;
                            } else {
                                MusicResource.isPlayingBeforeUnPlugHeadphone = false;
                            }
                        break;
                    case 1: {
                        Log.d("Headphone", "changeState1");
                        MusicResource.isPlugHeadPhone = true;
                        if (Player.player != null)
                            if (!Player.player.isPlaying()) {
                                if (MusicResource.isPlayingBeforeUnPlugHeadphone) {
                                    context.sendBroadcast(new Intent("noti_play_intent_action"));
                                }
                                MusicResource.isPlayingBeforeUnPlugHeadphone = false;
                            }
                    }
                    break;
                }
            }
        }
    };

    BroadcastReceiver DoBackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            doBack();
        }
    };
    BroadcastReceiver DoRepeatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            doRepeat();
        }
    };
    BroadcastReceiver DoShuffleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            doShuffle();
        }
    };
    BroadcastReceiver DoNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doNext();
        }
    };

    BroadcastReceiver receiverNotiPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getApplicationContext(), "NOTI PLAY CLICK", Toast.LENGTH_LONG).show();
            if (!MusicResource.isMainPause)
                sub_Play_btn.performClick();
        }
    };

    BroadcastReceiver receiverNotiNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getApplicationContext(), "NOTI NEXT CLICK", Toast.LENGTH_LONG).show();
            if (!MusicResource.isMainPause)
                sub_Next_btn.performClick();
        }
    };
    BroadcastReceiver receiverNotiBack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getApplicationContext(), "NOTI NEXT CLICK", Toast.LENGTH_LONG).show();
            if (!MusicResource.isMainPause)
                sub_Back_btn.performClick();
        }
    };

    BroadcastReceiver receiverNotiClear = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    BroadcastReceiver EnableAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.timerEnable = true;
        }
    };
    BroadcastReceiver DisableAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.timerEnable = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        MusicResource.isMainPause = false;
//        subArt.setAnimation(null);
        anim.reset();

        imagePositionOffset = MusicResource.imagePosition;
        subArt.setRotation(MusicResource.imagePosition);

        IntentFilter EnableAlarm = new IntentFilter("EnableAlarm");
        registerReceiver(EnableAlarmReceiver, EnableAlarm);

        IntentFilter DisableAlarm = new IntentFilter("DisableAlarm");
        registerReceiver(DisableAlarmReceiver, DisableAlarm);


        IntentFilter noti_play_intent = new IntentFilter("noti_play_intent_action");
        IntentFilter noti_next_intent = new IntentFilter("noti_next_intent_action");
        IntentFilter noti_back_intent = new IntentFilter("noti_back_intent_action");
        IntentFilter noti_clear_intent = new IntentFilter("noti_clear_intent_action");

        registerReceiver(receiverNotiPlay, noti_play_intent);
        registerReceiver(receiverNotiNext, noti_next_intent);
        registerReceiver(receiverNotiBack, noti_back_intent);
        registerReceiver(receiverNotiClear, noti_clear_intent);
        IntentFilter ChangeOfflinePage = new IntentFilter(("ChangeOfflinePage"));
        registerReceiver(ChangeOfflinePageReceiver, ChangeOfflinePage);
        IntentFilter intentFilterAudioFocus = new IntentFilter("noti_audiofocus_change");
        IntentFilter intentFilterAudioStateChange = new IntentFilter("noti_audiostate_change");
        IntentFilter intentFilterNetworkChange = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        IntentFilter intentFilterLoadBitmapLocal = new IntentFilter("noti_loadBitmapLocal_done");
        IntentFilter intentFilterDoNext = new IntentFilter("intentFilterDoNext");
        IntentFilter intentFilterDoBack = new IntentFilter("intentFilterDoBack");
        IntentFilter intentFilterDoRepeat = new IntentFilter("intentFilterDoRepeat");
        registerReceiver(DoRepeatReceiver, intentFilterDoRepeat);
        IntentFilter intentFilterDoShuffle = new IntentFilter("intentFilterDoShuffle");
        registerReceiver(DoShuffleReceiver, intentFilterDoShuffle);
        registerReceiver(receiverAudioFocus, intentFilterAudioFocus);
        registerReceiver(receiverAudioStateChange, intentFilterAudioStateChange);
        registerReceiver(receiverNetworkChange, intentFilterNetworkChange);
        registerReceiver(receiverLoadBitmapLocal, intentFilterLoadBitmapLocal);
        registerReceiver(DoNextReceiver, intentFilterDoNext);
        registerReceiver(DoBackReceiver, intentFilterDoBack);
        IntentFilter OnItemOnlineClick = new IntentFilter("OnItemOnlineClick");
        IntentFilter addToNewPlaylistOffline = new IntentFilter("addToNewPlaylistOffline");
        registerReceiver(addToNewPlaylistOfflineReceiver, addToNewPlaylistOffline);
        IntentFilter addToExistPlaylistOffline = new IntentFilter("addToExistPlaylistOffline");
        registerReceiver(addToExistPlaylistOfflineReceiver, addToExistPlaylistOffline);
        IntentFilter addToNewPlaylistOnline = new IntentFilter("addToNewPlaylistOnline");
        registerReceiver(addToNewPlaylistOnlineReceiver, addToNewPlaylistOnline);
        IntentFilter addToExistPlaylistOnline = new IntentFilter("addToExistPlaylistOnline");
        registerReceiver(addToExistPlaylistOnlineReceiver, addToExistPlaylistOnline);

        if (!MusicResource.isHandleOnItemClickOnPlayContent) {
            registerReceiver(OnItemOnlineClickReceiver, OnItemOnlineClick);
            MusicResource.isHandleOnItemClickOnPlayContent = true;
        }
        ImageViewAnimatedChange(getApplicationContext(), main_background, MusicResource.blurredBitmap);
        if (Player.player != null)
            if (Player.player.isPlaying()) {
                sub_Play_btn.setBackgroundResource(R.drawable.play_btn);
            } else sub_Play_btn.setBackgroundResource(R.drawable.stop_btn);


    }

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

    BroadcastReceiver ChangeOfflinePageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ActionBar actionBar = getSupportActionBar();
            switch (MusicResource.pageOfflinePosition) {
                case 0:
                    actionBar.setTitle(getResources().getString(R.string.album));
                    break;
                case 1:
                    actionBar.setTitle(getResources().getString(R.string.track));
                    break;
                case 2:
                    actionBar.setTitle(getResources().getString(R.string.playlist_offline));
                    break;
                case 3:
                    actionBar.setTitle(getResources().getString(R.string.folder));
                    break;
                case 4:
                    actionBar.setTitle(getResources().getString(R.string.downloaded));
                    break;
            }
        }
    };

    @Override
    protected void onPause() {

        super.onPause();
//        unregisterReceiver(receiverNotiPlay);
//        unregisterReceiver(receiverNotiNext);
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
        Bitmap temp = BitmapFactory.decodeFile(art_uri, options);
        if (temp != null)
            return ImageHelper.getRoundedCornerBitmap(temp, 30);
        else return null;
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
        BitmapWorkerTaskForArt task = new BitmapWorkerTaskForArt(getApplication(), imageView, 512, 512);
        task.execute(art_uri);
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawers();
            return;
        }
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            return;
        }
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Nhấn Back thêm lần nữa để thoát!", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
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
                        Pattern track = Pattern.compile("\"(.+\\.(mp3|m4a))");
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

                            if (MusicResource.playAtSearchAlbum) {
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

                            } else {
                                MusicResource.makeHashmapQuality(hashMap.get(MusicResource.MODE), MusicResource.songPosition, matcher.group(1));
                                try {
                                    Player.player.setDataSource(MusicResource.getQualityfromDefault(MusicResource.qualityLevel, hashMap.get(MusicResource.MODE).get(MusicResource.songPosition)));
                                } catch (IllegalArgumentException error) {
                                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                                } catch (SecurityException error) {
                                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                                } catch (IllegalStateException error) {
                                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                                } catch (IOException error) {
                                    error.printStackTrace();
                                }
                            }
                            try {
                                Player.player.prepare();
                            } catch (IllegalStateException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            } catch (IOException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            }

                            MusicResource.set_src_complete = true;
                            if (MusicResource.playAtSearchAlbum) {
                                hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).setSrc(matcher.group(1));
                            }
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
            if (bitmap != null) {
                MusicResource.subArt = bitmap;
                subArt.setImageBitmap(MusicResource.subArt);
                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), MusicResource.subArt);
                MusicResource.noti_art = MusicResource.subArt;
            } else if (bm != null) {
                MusicResource.subArt = bm;
                MusicResource.noti_art = bm;
                subArt.setImageBitmap(MusicResource.subArt);
                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), MusicResource.subArt);
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.nice_view_main_background_2);
                MusicResource.subArt = ((BitmapDrawable) drawable).getBitmap();
                Bitmap bitmapResized = Bitmap.createScaledBitmap(MusicResource.subArt, 200, 200, false);
                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(),
                        bitmapResized);
                MusicResource.noti_art = ImageHelper.getRoundedCornerBitmap(MusicResource.subArt, 30);
                subArt.setImageBitmap(MusicResource.subArt);

            }


            MusicResource.subTitle = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getTitle();
            MusicResource.subArtist = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getArtist();
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);

            sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

            ImageViewAnimatedChange(getApplicationContext(), main_background, MusicResource.blurredBitmap);

            subPlaySong.setEnabled(true);

            Player.player.start();
            Player.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    if (MusicResource.repeat_mode == 0) {
                        if (MusicResource.shuffle)
                            sendBroadcast(new Intent("intentFilterDoShuffle"));
                        else
                            sendBroadcast(new Intent("intentFilterDoNext"));
                    } else {
                        sendBroadcast(new Intent("intentFilterDoRepeat"));
                    }
                }
            });

            MusicResource.isCanChange = true;
            progressBar.setVisibility(View.INVISIBLE);
            String download_url = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getDownload_url();

            if (download_url != null) {
//                Log.d("TestDL", download_url);
                new CSN_Download_DetailforAction(hashMap.get(MusicResource.MODE).get(MusicResource.songPosition))
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, download_url);
            }
            MusicResource.csn_Notification(getApplication(),
                    MusicResource.noti_art,
                    hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getTitle(),
                    hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getArtist(), "play");
            sendBroadcast(new Intent("OnItemOnlineClickDone"));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
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

    BroadcastReceiver OnItemOnlineClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.isCanChange = false;
            OnItemClick();
        }
    };

    public void OnItemClick() {
        subPlaySong.setEnabled(false);

        if (!MusicResource.network_state) {
//            Toast.makeText(getContext(), "No Network Connection!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Player.player != null && MusicResource.set_src_complete == true) {
            Player.player.reset();
        }

        MusicResource.track_playing = MusicResource.songListOnlinePlay.get(MusicResource.songPosition);

        MusicResource.track_ready = false;
        MusicResource.set_src_complete = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new CSN_getTrackDetailForAllOnline().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getUrl());
        } else {
            new CSN_getTrackDetailForAllOnline().execute(MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getUrl());
        }

        if (!MusicResource.sub_anim_running) {
            MusicResource.sub_anim_state = true;
            MusicResource.sub_anim_running = true;
            subArt.setRotation(MusicResource.imagePosition);
            subArt.startAnimation(anim);

            calendar = Calendar.getInstance();
            MusicResource.startTime = calendar.get(Calendar.HOUR) * 3600000 +
                    calendar.get(Calendar.MINUTE) * 60000 +
                    calendar.get(Calendar.SECOND) * 1000 +
                    calendar.get(Calendar.MILLISECOND);
        }
    }

    private class CSN_getTrackDetailForAllOnline extends AsyncTask<String, Void, Bitmap> {

        private String track_url;
        private String imageKey;
        private Bitmap bitmap;

        @Override
        protected Bitmap doInBackground(String... params) {

            track_url = params[0];

            imageKey = String.valueOf(track_url);
            bitmap = MusicResource.getBitmapFromMemCache(imageKey);

            if (MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getSrc() != null) {

                if (Player.player == null) {
                    Player.player = new MediaPlayer();
                } else {
                    Player.player.reset();
                    Player.player = new MediaPlayer();
                }

                Player.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                Log.d("testissue1", MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getSrc());
                try {
                    Player.player.setDataSource(MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getSrc());
//                    Log.d("TestQuality1", MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getSrc());
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
                        Pattern track = Pattern.compile("\"(.+\\.(mp3|m4a))");
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

                            if (MusicResource.playAtSearchAlbum) {

                                try {
                                    Player.player.setDataSource(matcher.group(1).replaceAll("/stream", "/downloads"));
                                } catch (IllegalArgumentException error) {
                                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                                } catch (SecurityException error) {
                                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                                } catch (IllegalStateException error) {
                                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                                } catch (IOException error) {
                                    error.printStackTrace();
                                }
                            } else {

                                MusicResource.makeHashmapQuality(MusicResource.songListOnlinePlay, MusicResource.songPosition, matcher.group(1));
                                try {
                                    Player.player.setDataSource(MusicResource.getQualityfromDefault(MusicResource.qualityLevel, MusicResource.songListOnlinePlay.get(MusicResource.songPosition)));
                                } catch (IllegalArgumentException error) {
                                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                                } catch (SecurityException error) {
                                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                                } catch (IllegalStateException error) {
                                    Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                                } catch (IOException error) {
                                    error.printStackTrace();
                                }
                            }
                            try {
                                Player.player.prepare();
                            } catch (IllegalStateException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            } catch (IOException error) {
                                Log.e(MusicResource.LOG_TAG, "You might not set the URI correctly!");
                            }

                            MusicResource.set_src_complete = true;
                            if (MusicResource.playAtSearchAlbum) {
                                MusicResource.songListOnlinePlay.get(MusicResource.songPosition).setSrc(matcher.group(1));
                            }
                        }
                    }
                }

                String download_url = document.select("div.datelast").first().select("a").last().attr("href").toString();
                MusicResource.songListOnlinePlay.get(MusicResource.songPosition).setDownload_url(download_url);
                Element lyricElement = document.select("p.genmed").first();
                if (lyricElement != null) {
                    String lyric = lyricElement.html();
                    lyric = lyric.replaceAll("<br>", "\n");
                    lyric = lyric.replaceAll("<span.*<\\/span>", "");
                    MusicResource.songListOnlinePlay.get(MusicResource.songPosition).setLyric(lyric);
//                    System.out.println(lyric);
//                    Log.d("TestLyric", MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getLyric());
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

            if (bitmap != null) {
                MusicResource.subArt = bitmap;
                subArt.setImageBitmap(MusicResource.subArt);
                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), MusicResource.subArt);
                MusicResource.noti_art = MusicResource.subArt;
            } else if (bm != null) {
                MusicResource.subArt = bm;
                MusicResource.noti_art = bm;
                subArt.setImageBitmap(MusicResource.subArt);
                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(), MusicResource.subArt);
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.nice_view_main_background_2);
                MusicResource.subArt = ((BitmapDrawable) drawable).getBitmap();
                Bitmap bitmapResized = Bitmap.createScaledBitmap(MusicResource.subArt, 200, 200, false);
                MusicResource.blurredBitmap = BlurBuilder.blur(getApplicationContext(),
                        bitmapResized);
                MusicResource.noti_art = ImageHelper.getRoundedCornerBitmap(MusicResource.subArt, 30);
                subArt.setImageBitmap(MusicResource.subArt);

            }

            MusicResource.subTitle = MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getTitle();
            MusicResource.subArtist = MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getArtist();
            subTitle.setText(MusicResource.subTitle);
            subArtist.setText(MusicResource.subArtist);

            sub_Play_btn.setBackgroundResource(R.drawable.play_btn);

            ImageViewAnimatedChange(getApplicationContext(), main_background, MusicResource.blurredBitmap);

            subPlaySong.setEnabled(true);

            if (MusicResource.request_audio_focus(getApplicationContext())) {
                Player.player.start();
                Player.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        if (MusicResource.repeat_mode == 0) {
                            if (MusicResource.shuffle)
                                sendBroadcast(new Intent("intentFilterDoShuffle"));
                            else
                                sendBroadcast(new Intent("intentFilterDoNext"));
                        } else {
                            sendBroadcast(new Intent("intentFilterDoRepeat"));
                        }
                    }
                });
            }

            MusicResource.track_ready = true;
            subPlaySong.setVisibility(View.VISIBLE);
            MusicResource.subPlaySongVisible = true;
            String download_url = MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getDownload_url();

            if (download_url != null) {
//                Log.d("TestDL", download_url);
                new CSN_Download_Detail()
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, download_url);
            }
            MusicResource.csn_Notification(getApplication(),
                    MusicResource.noti_art,
                    MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getTitle(),
                    MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getArtist(), "play");

            MusicResource.isCanChange = true;
            sendBroadcast(new Intent("OnItemOnlineClickDone"));
            sendBroadcast(new Intent("noti_audioselected_change_downloadDone"));

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private class CSN_Download_Detail extends AsyncTask<String, Void, HashMap> {

        private String download_url;
        private String regex = "<a href=\"(.+?)\" .+?: (.+?) <.+?\"color: (.+?)\">(.+?)</span> (.+?)</a><br>";
        private Pattern pattern = Pattern.compile(regex);
        private HashMap<String, String> download_detail = new HashMap();

        @Override
        protected HashMap doInBackground(String... params) {

            download_url = params[0];

            try {

                Document document = Jsoup.connect(download_url).userAgent("Chrome/59.0.3071.115").get();
                if (document == null) return null;
                Element element = document.select("div#downloadlink2").first();
                if (element == null) return null;
//                Log.d("TestDownload", element.html());
                Matcher matcher = pattern.matcher(element.html());
                String download_default_url = "";
                while (matcher.find()) {

//                    Log.d("TestDownload", matcher.group(1)); // download_url
//                    Log.d("TestDownload", matcher.group(2)); // quality
//                    Log.d("TestDownload", matcher.group(3)); // color
//                    Log.d("TestDownload", matcher.group(4)); // bitrate
//                    Log.d("TestDownload", matcher.group(5)); // size

                    String hm_key = matcher.group(2) + " " + matcher.group(4) + " " + matcher.group(5);
                    String hm_value = matcher.group(1);
                    if (download_detail.containsKey(hm_key)) continue;
                    String key = matcher.group(2).toLowerCase() + matcher.group(4).toLowerCase();
                    if (key.contains("mp3128kbps")) {
                        download_default_url = hm_value;
                    }
                    if (key.contains("m4a")) {
                        hm_value = download_default_url.replaceAll("MP3 128kbps", "M4A 500kbps");
                        hm_value = hm_value.replaceAll(".mp3", ".m4a");
                        hm_value = hm_value.replaceAll("128", "m4a");
                    }
                    if (key.contains("flac")) {
                        hm_value = download_default_url.replaceAll("MP3 128kbps", "FLAC Lossless");
                        hm_value = hm_value.replaceAll(".mp3", ".flac");
                        hm_value = hm_value.replaceAll("128", "flac");
                    }
                    download_detail.put(hm_key, hm_value);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return download_detail;
        }

        @Override
        protected void onPostExecute(HashMap hashMap) {
            super.onPostExecute(hashMap);

            if (hashMap != null) {
                MusicResource.songListOnlinePlay.get(MusicResource.songPosition).setDownload_detail(hashMap);
//                Log.d("TestDL", "getDL Done");
            }

        }
    }

    private class CSN_Download_DetailforAction extends AsyncTask<String, Void, HashMap> {

        private String download_url;
        private String regex = "href=\"(.+)\" onmouseover.+: (.+) <.+\"color:(.+)\">(.+)</span> (.+)</a>";
        private Pattern pattern = Pattern.compile(regex);
        private HashMap<String, String> download_detail = new HashMap();
        private Track track;

        public CSN_Download_DetailforAction(Track track) {
            this.track = track;
        }

        @Override
        protected HashMap doInBackground(String... params) {

            download_url = params[0];

            try {

                Document document = Jsoup.connect(download_url).userAgent("Chrome/59.0.3071.115").get();
                if (document == null) return null;
                Element element = document.select("div#downloadlink").first();
                if (element == null) return null;
//                Log.d("TestDownload", element.html());
                Matcher matcher = pattern.matcher(element.html());
                String download_default_url = "";
                while (matcher.find()) {

//                    Log.d("TestDownload", matcher.group(1)); // download_url
//                    Log.d("TestDownload", matcher.group(2)); // quality
//                    Log.d("TestDownload", matcher.group(3)); // color
//                    Log.d("TestDownload", matcher.group(4)); // bitrate
//                    Log.d("TestDownload", matcher.group(5)); // size

                    String hm_key = matcher.group(2) + " " + matcher.group(4) + " " + matcher.group(5);
                    String hm_value = matcher.group(1);
                    if (download_detail.containsKey(hm_key)) continue;
                    String key = matcher.group(2).toLowerCase() + matcher.group(4).toLowerCase();
                    if (key.contains("mp3128kbps")) {
                        download_default_url = hm_value;
                    }
                    if (key.contains("m4a")) {
                        hm_value = download_default_url.replaceAll("MP3 128kbps", "M4A 500kbps");
                        hm_value = hm_value.replaceAll(".mp3", ".m4a");
                        hm_value = hm_value.replaceAll("128", "m4a");
                    }
                    if (key.contains("flac")) {
                        hm_value = download_default_url.replaceAll("MP3 128kbps", "FLAC Lossless");
                        hm_value = hm_value.replaceAll(".mp3", ".flac");
                        hm_value = hm_value.replaceAll("128", "flac");
                    }
                    download_detail.put(hm_key, hm_value);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return download_detail;
        }

        @Override
        protected void onPostExecute(HashMap hashMap) {
            super.onPostExecute(hashMap);

            if (hashMap != null) {
                track.setDownload_detail(hashMap);
//                Log.d("TestDL", "getDL Done");
            }
            sendBroadcast(new Intent("noti_audioselected_change_downloadDone"));
        }

    }

    static void saveFile(Context context, Bitmap b, String picName) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    Bitmap loadBitmap(Context context, String picName) {
        Bitmap b = null;
        FileInputStream fis;
        try {
            fis = context.openFileInput(picName);
            b = BitmapFactory.decodeStream(fis);
            fis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
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

