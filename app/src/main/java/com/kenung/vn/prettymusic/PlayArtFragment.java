package com.kenung.vn.prettymusic;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.kenung.vn.prettymusic.music_online.Track;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by KXPRO on 4/8/2017.
 */

public class PlayArtFragment extends Fragment {
    String qualityLevel;
    private CircleImageView playArt;
    private RotateAnimation anim;
    private float imagePositionOffset;
    private Calendar calendar;
    private Button csn_download_btn;
    private ArrayList<String> listAlert;
    private ArrayList<String> listQuality;
    private ArrayList<String> listResourceListen;
    private ArrayList<String> listQualityListen;
    private ArrayList<String> listResource;
    private ArrayList<String> listDownloadUrl;
    private AdView avBanner;
    private AdRequest adRequest;
    boolean checkClickDownload = false;
    boolean isCheckClickChooseQuality = false;
    Button choosequality;
    int position = -1;
    private int FILE_CODE = 0; //SDCard Mode
    private Button directory_btn;
    public int checkModeSelectQuality = 1; // 0: set as default; 1: choose without set a default
    int listChoicePosition;
    int listChoicePositionTemp;

    private BroadcastReceiver receiverAudioStateChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (Player.player != null) {
                if (Player.player.isPlaying()) {
                    calendar = Calendar.getInstance();
                    MusicResource.startTime = calendar.get(Calendar.HOUR) * 3600000 +
                            calendar.get(Calendar.MINUTE) * 60000 +
                            calendar.get(Calendar.SECOND) * 1000 +
                            calendar.get(Calendar.MILLISECOND);

                    playArt.startAnimation(anim);

                    Log.d("TestTimer", "startTime - " + MusicResource.startTime);
                } else {
                    calendar = Calendar.getInstance();
                    MusicResource.endTime = calendar.get(Calendar.HOUR) * 3600000 +
                            calendar.get(Calendar.MINUTE) * 60000 +
                            calendar.get(Calendar.SECOND) * 1000 +
                            calendar.get(Calendar.MILLISECOND);
                    MusicResource.activeTime = MusicResource.endTime - MusicResource.startTime;
                    MusicResource.imagePosition += (360f * MusicResource.activeTime) / 20000f;
                    if (MusicResource.imagePosition >= 360f)
                        MusicResource.imagePosition -= 360f;

                    playArt.setRotation(MusicResource.imagePosition);

                    anim.reset();

                    playArt.setAnimation(null);

                    MusicResource.sub_anim_running = false;

                    Log.d("TestAnim", "onAudioStateChange - " + MusicResource.imagePosition);
                    Log.d("TestTimer", "endTime - " + MusicResource.endTime + " / startTime - " + MusicResource.startTime);
                }
            }
        }
    };

    private BroadcastReceiver receiverLoadBitmapLocal = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

//            Log.d("TestAnim", "LoadBitmapDone");
//            if (Player.player != null && !Player.player.isPlaying()) {
//                playArt.setAnimation(null);
//            } else if (Player.player != null && Player.player.isPlaying()) {
//                playArt.startAnimation(anim);
//
//                calendar = Calendar.getInstance();
//                MusicResource.startTime = calendar.get(Calendar.HOUR) * 3600000 +
//                        calendar.get(Calendar.MINUTE) * 60000 +
//                        calendar.get(Calendar.SECOND) * 1000 +
//                        calendar.get(Calendar.MILLISECOND);
//
//                Log.d("TestTimer", "startTime - " + MusicResource.startTime);
//            }
        }
    };
    BroadcastReceiver receiverAudioSelectedChangeDownloadDone = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            if (MusicResource.playAtSearchAlbum) {
                HashMap<String, String> download_detail;
                download_detail =
                        MusicResource.songListOnline.get(MusicResource.songPosition).getDownload_detail();

                if (download_detail != null) {
                    listQuality = new ArrayList<>();
                    listResource = new ArrayList<>();
                    Iterator it = download_detail.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        String[] Quality = pair.getKey().toString().split(" ");
                        String[] Resource = pair.getValue().toString().split(" ");
                        listQuality.add(Quality[0] + " " + Quality[1]);
                        listResource.add(Resource[0] + "." + Quality[0].toLowerCase());
                    }
                    position = listResource.indexOf(MusicResource.songListOnline.get(MusicResource.songPosition).getSrc());
                    if (position != -1) {
                        choosequality.setText(listQuality.get(position).split(" ")[1]);
                        if (listQuality.get(position).split(" ")[1].toLowerCase().contains("lossless"))
                            choosequality.setTextColor(Color.RED);
                        else if (listQuality.get(position).split(" ")[1].toLowerCase().contains("500"))
                            choosequality.setTextColor(Color.rgb(255, 202, 130));
                        else if (listQuality.get(position).split(" ")[1].toLowerCase().contains("320"))
                            choosequality.setTextColor(Color.rgb(150, 223, 234));
                        else choosequality.setTextColor(Color.WHITE);
                    }
                }
            } else {


                listQualityListen = new ArrayList<String>(MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getStream_detail().keySet());
                listResourceListen = new ArrayList<String>(MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getStream_detail().values());
                listChoicePosition = listResourceListen.indexOf(MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getSrc());
                qualityLevel = listQualityListen.get(listChoicePosition);
                choosequality.setText(qualityLevel);
                if (qualityLevel.contains(Define.Q_LOSSLESS))
                    choosequality.setTextColor(Color.RED);
                else if (qualityLevel.contains(Define.Q_500KBPS))
                    choosequality.setTextColor(Color.rgb(255, 202, 130));
                else if (qualityLevel.contains(Define.Q_320KBPS))
                    choosequality.setTextColor(Color.rgb(150, 223, 234));
                else choosequality.setTextColor(Color.WHITE);


            }
        }
    };

    private BroadcastReceiver receiverAudioSelectedChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
                String imageKey = String.valueOf(MusicResource.songListOnline.get(MusicResource.songPosition).getUrl());
                Bitmap bitmap = MusicResource.getBitmapFromMemCache(imageKey);

                if (bitmap != null && !MusicResource.musicOnlline) {
                    playArt.setImageBitmap(bitmap);
                } else {
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.music_cover_default);
                    playArt.setImageBitmap(bmp);
                }

            }
            switch (MusicResource.MODE) {
                // Offline - Track
                case 0: {
                    if (MusicResource.songList.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.songList.get(MusicResource.songPosition).getArt_uri(), playArt);
                    } else {
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.nice_view_main_background);
                        playArt.setImageBitmap(bmp);
                    }
                }
                break;
                // CSN BXH VN
                case 1: {

                    if (MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri(), playArt);
                    } else {
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.nice_view_main_background);
                        playArt.setImageBitmap(bmp);
                    }
                }
                break;
                case 2: {
                    if (MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri(), playArt);
                    } else {
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.nice_view_main_background);
                        playArt.setImageBitmap(bmp);
                    }
                }
                break;
                // Offline - AlbumCollection
                case 30: {
                    if (MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), playArt);
                    } else {
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.nice_view_main_background);
                        playArt.setImageBitmap(bmp);
                    }
                }
                break;
                // Offline - PlaylistOffline
                case 31: {
                    if (MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), playArt);
                    } else {
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.nice_view_main_background);
                        playArt.setImageBitmap(bmp);
                    }
                }
                break;
                case 4: {
                    if (MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), playArt);
                    } else {
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.nice_view_main_background);
                        playArt.setImageBitmap(bmp);
                    }
                }
                break;
                // CSN - Search
                case 5: {
                    String imageKey = String.valueOf(MusicResource.CSNSearchResult.get(MusicResource.songPosition).getUrl());
                    Bitmap bitmap = MusicResource.getBitmapFromMemCache(imageKey);

                    if (bitmap != null && !MusicResource.musicOnlline) {
                        playArt.setImageBitmap(bitmap);
                        Log.d("TestArtFragment", "bitmap not null");
                    } else {
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.music_cover_default);
                        playArt.setImageBitmap(bmp);
                        Log.d("TestArtFragment", "bitmap is null");
                    }
                }
                break;
                default:

                    break;
            }
        }
    };

    public static PlayArtFragment newInstance() {
        PlayArtFragment fragment = new PlayArtFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilterAudioStateChange = new IntentFilter("noti_audiostate_change");
        IntentFilter intentFilterAudioSelectedChange = new IntentFilter("noti_audioselected_change");
        getActivity().registerReceiver(receiverAudioSelectedChange, intentFilterAudioSelectedChange);
        IntentFilter intentFilterLoadBitmapLocal = new IntentFilter("noti_loadBitmapLocal_done");

        getActivity().registerReceiver(receiverAudioStateChange, intentFilterAudioStateChange);

        getActivity().registerReceiver(receiverLoadBitmapLocal, intentFilterLoadBitmapLocal);
        IntentFilter noti_audioselected_change_downloadDone = new IntentFilter("noti_audioselected_change_downloadDone");
        getActivity().registerReceiver(receiverAudioSelectedChangeDownloadDone, noti_audioselected_change_downloadDone);

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getActivity().unregisterReceiver(receiverAudioStateChange);
        getActivity().unregisterReceiver(receiverAudioSelectedChange);
        getActivity().unregisterReceiver(receiverLoadBitmapLocal);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            StringBuilder sb = new StringBuilder();
            for (Uri uri : files) {
                File file = Utils.getFileForUri(uri);
                // Do something with the result...
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(FILE_CODE == requestCode ?
                        Utils.getFileForUri(uri).toString() :
                        uri.toString());
            }
            String[] src = sb.toString().split("/");
            MusicResource.folderDownload = src[src.length - 1];
            MusicResource.listOfPathDownload.add(MusicResource.folderDownload);
            Set<String> set = new HashSet<String>();
            set.addAll(MusicResource.listOfPathDownload);
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("my_data", MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putStringSet("listOfPath", set);
            edit.putString("folder", MusicResource.folderDownload);
            edit.commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_fragment_art, container, false);
        avBanner = (AdView) view.findViewById(R.id.av_banner);
        choosequality = (Button) view.findViewById(R.id.choosequality);
        playArt = (CircleImageView) view.findViewById(R.id.playArt);
        csn_download_btn = (Button) view.findViewById(R.id.csn_download_btn);

        directory_btn = (Button) view.findViewById(R.id.directory);
        directory_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i, FILE_CODE);
            }
        });
        if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
        } else {
            csn_download_btn.setVisibility(View.INVISIBLE);
            choosequality.setVisibility(View.INVISIBLE);
            directory_btn.setVisibility(View.INVISIBLE);
        }
        imagePositionOffset = MusicResource.imagePosition;


        playArt.setRotation(MusicResource.imagePosition);
        anim = new RotateAnimation(MusicResource.imagePosition - imagePositionOffset,
                360f + MusicResource.imagePosition - imagePositionOffset,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(MusicResource.imageDuration);
        if (Player.player != null) {
            if (Player.player.isPlaying()) {
                calendar = Calendar.getInstance();
                MusicResource.startTime = calendar.get(Calendar.HOUR) * 3600000 +
                        calendar.get(Calendar.MINUTE) * 60000 +
                        calendar.get(Calendar.SECOND) * 1000 +
                        calendar.get(Calendar.MILLISECOND);

                playArt.startAnimation(anim);

                Log.d("TestTimer", "startTime - " + MusicResource.startTime);
            } else {
                calendar = Calendar.getInstance();
                MusicResource.endTime = calendar.get(Calendar.HOUR) * 3600000 +
                        calendar.get(Calendar.MINUTE) * 60000 +
                        calendar.get(Calendar.SECOND) * 1000 +
                        calendar.get(Calendar.MILLISECOND);
                MusicResource.activeTime = MusicResource.endTime - MusicResource.startTime;
                MusicResource.imagePosition += (360f * MusicResource.activeTime) / 20000f;
                if (MusicResource.imagePosition >= 360f)
                    MusicResource.imagePosition -= 360f;

                playArt.setRotation(MusicResource.imagePosition);

                anim.reset();

                playArt.setAnimation(null);

                MusicResource.sub_anim_running = false;

                Log.d("TestAnim", "onAudioStateChange - " + MusicResource.imagePosition);
                Log.d("TestTimer", "endTime - " + MusicResource.endTime + " / startTime - " + MusicResource.startTime);
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
            String imageKey = "";

            imageKey = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getUrl();

            Bitmap bitmap = MusicResource.getBitmapFromMemCache(imageKey);

            if (bitmap != null) {
                playArt.setImageBitmap(bitmap);
            } else {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.music_cover_default);
                playArt.setImageBitmap(bmp);
            }

            if (MusicResource.playAtSearchAlbum) {
                HashMap<String, String> download_detail;
                download_detail =
                        MusicResource.songListOnline.get(MusicResource.songPosition).getDownload_detail();

                if (download_detail != null) {
                    listQuality = new ArrayList<>();
                    listResource = new ArrayList<>();
                    Iterator it = download_detail.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        String[] Quality = pair.getKey().toString().split(" ");
                        String[] Resource = pair.getValue().toString().split(" ");
                        listQuality.add(Quality[0] + " " + Quality[1]);
                        listResource.add(Resource[0] + "." + Quality[0].toLowerCase());
                    }
                    position = listResource.indexOf(hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getSrc());
                    if (position != -1) {
                        choosequality.setText(listQuality.get(position).split(" ")[1]);
                        if (listQuality.get(position).split(" ")[1].toLowerCase().contains("lossless"))
                            choosequality.setTextColor(Color.RED);
                        else if (listQuality.get(position).split(" ")[1].toLowerCase().contains("500"))
                            choosequality.setTextColor(Color.rgb(255, 202, 130));
                        else if (listQuality.get(position).split(" ")[1].toLowerCase().contains("320"))
                            choosequality.setTextColor(Color.rgb(150, 223, 234));
                        else choosequality.setTextColor(Color.WHITE);
                    }
                }
            } else {


                listQualityListen = new ArrayList<String>(MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getStream_detail().keySet());
                listResourceListen = new ArrayList<String>(MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getStream_detail().values());
                listChoicePosition = listResourceListen.indexOf(MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getSrc());
                qualityLevel = listQualityListen.get(listChoicePosition);
                choosequality.setText(qualityLevel);
                if (qualityLevel.contains(Define.Q_LOSSLESS))
                    choosequality.setTextColor(Color.RED);
                else if (qualityLevel.contains(Define.Q_500KBPS))
                    choosequality.setTextColor(Color.rgb(255, 202, 130));
                else if (qualityLevel.contains(Define.Q_320KBPS))
                    choosequality.setTextColor(Color.rgb(150, 223, 234));
                else choosequality.setTextColor(Color.WHITE);


            }

        } else {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.nice_view_main_background);
            playArt.setImageBitmap(bmp);
            switch (MusicResource.MODE) {
                // Offline - Track

                case 0: {
                    if (MusicResource.songList.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {
                        loadBitmap(MusicResource.songList.get(MusicResource.songPosition).getArt_uri(), playArt);
                    }
                }
                break;
                case 1: {
                    if (MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {
                        loadBitmap(MusicResource.folderSelected.get(MusicResource.songPosition).getArt_uri(), playArt);
                    }
                }
                break;
                case 2: {
                    if (MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.songListDownload.get(MusicResource.songPosition).getArt_uri(), playArt);
                    }
                }
                break;
                // Album Selected
                case 3: {
                    if (MusicResource.albumSelected.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.albumSelected.get(MusicResource.songPosition).getArt_uri(), playArt);
                    }
                }
                break;
                case 4: {
                    if (MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.searchResultList.get(MusicResource.songPosition).getArt_uri(), playArt);
                    }
                }
                break;
                // CSN - Search
                case 5: {
                    String imageKey = String.valueOf(MusicResource.CSNSearchResult.get(MusicResource.songPosition).getUrl());
                    Bitmap bitmap = MusicResource.getBitmapFromMemCache(imageKey);

                    if (bitmap != null && !MusicResource.musicOnlline) {
                        playArt.setImageBitmap(bitmap);
                    }
                }
                break;
                case 30: {
                    if (MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.albumCollectionContent.get(MusicResource.songPosition).getArt_uri(), playArt);
                    }
                }
                break;
                case 31: {
                    if (MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri() != null && !MusicResource.musicOnlline) {

                        loadBitmap(MusicResource.playlistOfflineSelected.get(MusicResource.songPosition).getArt_uri(), playArt);
                    }
                }
                break;
                default:
                    break;
            }
        }
        choosequality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (MusicResource.playAtSearchAlbum) {
                    final HashMap<Integer, ArrayList<Track>> hashMap = new HashMap<>();

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

                    HashMap<String, String> download_detail;
                    download_detail =
                            MusicResource.songListOnline.get(MusicResource.songPosition).getDownload_detail();

                    if (download_detail != null) {
                        listQuality = new ArrayList<>();
                        listResource = new ArrayList<>();
                        Iterator it = download_detail.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            String[] Quality = pair.getKey().toString().split(" ");
                            String[] Resource = pair.getValue().toString().split(" ");
                            listQuality.add(Quality[0] + " " + Quality[1]);
                            listResource.add(Resource[0] + "." + Quality[0].toLowerCase());
                        }
                        position = listResource.indexOf(hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getSrc());
                    }
                    new MaterialDialog.Builder(getActivity()).title(getActivity().getResources().getString(R.string.quality)).items(listQuality).itemsCallbackSingleChoice(position, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            position = which;
                            choosequality.setText(listQuality.get(position).split(" ")[1]);
                            if (listQuality.get(position).split(" ")[1].toLowerCase().contains("lossless"))
                                choosequality.setTextColor(Color.RED);
                            else if (listQuality.get(position).split(" ")[1].toLowerCase().contains("500"))
                                choosequality.setTextColor(Color.rgb(255, 202, 130));
                            else if (listQuality.get(position).split(" ")[1].toLowerCase().contains("320"))
                                choosequality.setTextColor(Color.rgb(150, 223, 234));
                            else choosequality.setTextColor(Color.WHITE);
                            dialog.dismiss();
                            hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).setSrc(listResource.get(position));
                            getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));
                            return true;
                        }


                    }).show();
                } else {


                    new MaterialDialog.Builder(getActivity()).title(getActivity().getResources().getString(R.string.quality)).items(listQualityListen).itemsCallbackSingleChoice(listChoicePosition, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            Log.d("testDialog", "onSelection");
                            listChoicePosition = which;
                            return true;
                        }


                    }).alwaysCallSingleChoiceCallback().positiveText(getResources().getString(R.string.choose))
                            .negativeText(getResources().getString(R.string.set_as_default))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    MusicResource.songListOnlinePlay.get(MusicResource.songPosition).setSrc(listResourceListen.get(listChoicePosition));
                                    getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    MusicResource.songListOnlinePlay.get(MusicResource.songPosition).setSrc(listResourceListen.get(listChoicePosition));
                                    MusicResource.qualityLevel = listQualityListen.get(listChoicePosition);
                                    Log.d("testissue4", listResourceListen.get(listChoicePosition));
                                    getActivity().sendBroadcast(new Intent("OnItemOnlineClick"));

                                }
                            }).show();
                }

            }
        });
        csn_download_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkClickDownload) return;
                if (MusicResource.folderDownload.equals("")) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.create_folder), Toast.LENGTH_SHORT).show();
                    return;
                }
                checkClickDownload = true;
                final HashMap<Integer, ArrayList<Track>> hashMap = new HashMap<>();
                HashMap<String, String> download_detail;

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

                final String download_url;

                download_detail =
                        hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getDownload_detail();
                Log.d("TestDownload", download_detail + "");
                if (download_detail != null) {
                    listAlert = new ArrayList<>();
                    listDownloadUrl = new ArrayList<>();

                    Iterator it = download_detail.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        Log.d("TestDownload", pair.getKey() + " = " + pair.getValue());
                        listAlert.add(pair.getKey().toString());
                        listDownloadUrl.add(pair.getValue().toString());
                        //it.remove(); // avoids a ConcurrentModificationException
                    }

                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
                    builderSingle.setTitle(getActivity().getResources().getString(R.string.quality));
                    final ListAlertDialogAdapter arrayAdapter = new ListAlertDialogAdapter(getContext(), listAlert);

                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //String strName = arrayAdapter.getItem(which);
                            Toast.makeText(getActivity(), "" + listAlert.get(which), Toast.LENGTH_LONG).show();
                            Log.d("testDownload", listDownloadUrl.get(which));
//                            new DownloadTask(getContext(),
//                                    hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getTitle(),
//                                    listAlert.get(which).split(" ")[0].toLowerCase())
//                                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, listDownloadUrl.get(which));

                            final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(listDownloadUrl.get(which)));
                            request.setNotificationVisibility(DownloadManager.Request.NETWORK_MOBILE);
                            request.setDestinationInExternalPublicDir(MusicResource.folderDownload, hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getTitle() + "." + listAlert.get(which).split(" ")[0].toLowerCase());

                            final DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.enqueue(request);
                            checkClickDownload = false;
                        }
                    });
                    builderSingle.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            checkClickDownload = false;
                        }
                    });
                    builderSingle.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            checkClickDownload = false;
                        }
                    });

                    builderSingle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            checkClickDownload = false;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            checkClickDownload = false;
                        }
                    });
                    builderSingle.show();
                } else {
                    checkClickDownload = false;
                    download_url = hashMap.get(MusicResource.MODE).get(MusicResource.songPosition).getDownload_url();
                    if (download_url != null) {
                        new CSN_Download(getContext())
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, download_url);
                    }
                }
            }
        });
//        adRequest = new AdRequest.Builder().build();
//        avBanner.loadAd(adRequest);
        new Init(avBanner).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return view;
    }

    public class Init extends AsyncTask<Void, Void, Void> {
        //        private WeakReference<AdView> adViewWeakReference;
        AdView adView;

        public Init(AdView adView) {
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
        BitmapWorkerTask task = new BitmapWorkerTask(getContext(), imageView, 512, 512);
        task.execute(art_uri);
    }

    private class CSN_Download extends AsyncTask<String, Void, HashMap> {

        private String download_url;
        private Context context;
        private String regex = "href=\"(.+)\" onmouseover.+: (.+) <.+\"color:(.+)\">(.+)</span> (.+)</a>";
        private Pattern pattern = Pattern.compile(regex);
        private HashMap<String, String> download_detail = new HashMap();

        public CSN_Download(Context context) {
            this.context = context;
        }

        @Override
        protected HashMap doInBackground(String... params) {

            download_url = params[0];

            try {

                Document document = Jsoup.connect(download_url).userAgent("Chrome/57.0.2987.133").get();
                if (document == null) return null;
                Element element = document.select("div#downloadlink").first();
                if (element == null) return null;
                Log.d("TestDownload", element.html());
                Matcher matcher = pattern.matcher(element.html());
                while (matcher.find()) {

                    Log.d("TestDownload", matcher.group(1));
                    Log.d("TestDownload", matcher.group(2));
                    Log.d("TestDownload", matcher.group(3));
                    Log.d("TestDownload", matcher.group(4));
                    Log.d("TestDownload", matcher.group(5));

                    final String hm_key = matcher.group(2) + " " + matcher.group(4) + " " + matcher.group(5);
                    final String hm_value = matcher.group(1);
                    if (download_detail.containsKey(hm_key)) continue;
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

            listAlert = new ArrayList<String>();

            if (hashMap != null) {

                Iterator it = hashMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Log.d("TestDownload", pair.getKey() + " = " + pair.getValue());
                    listAlert.add(pair.getKey().toString());
                    //it.remove(); // avoids a ConcurrentModificationException
                }

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
                builderSingle.setTitle("Quality");
                final ListAlertDialogAdapter arrayAdapter = new ListAlertDialogAdapter(getContext(), listAlert);

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //String strName = arrayAdapter.getItem(which);
                        Toast.makeText(getActivity(), "" + listAlert.get(which), Toast.LENGTH_LONG).show();
                    }
                });
                builderSingle.show();
            }
        }
    }
}