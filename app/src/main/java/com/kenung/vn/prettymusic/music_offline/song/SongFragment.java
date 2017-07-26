package com.kenung.vn.prettymusic.music_offline.song;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kenung.vn.prettymusic.BitmapWorkerTask;
import com.kenung.vn.prettymusic.BlurBuilder;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.Player;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.module.ImageHelper;
import com.kenung.vn.prettymusic.music_offline.album.Album;
import com.kenung.vn.prettymusic.music_offline.folder.FolderModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sev_user on 23-Dec-16.
 */
public class SongFragment extends Fragment {
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
    private SongAdapter songAdt;

    public static SongFragment newInstance() {
        SongFragment fragment = new SongFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    public void onDestroy() {
        super.onDestroy();
    }

    BroadcastReceiver OnClickFolderSongsItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                mLastClickTime = SystemClock.elapsedRealtime();
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            onItemClick(MusicResource.hashMapFolderList
                    .get(MusicResource.folderSelectedTitle), "folder");

            MusicResource.MODE = 1;
            MusicResource.folderSelected = new ArrayList<>(MusicResource.hashMapFolderList
                    .get(MusicResource.folderSelectedTitle));
        }
    };

    BroadcastReceiver onItemSongClick = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                mLastClickTime = SystemClock.elapsedRealtime();
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            onItemClick(MusicResource.songList, "song");
            MusicResource.MODE = 0;
        }
    };

    BroadcastReceiver onItemPlaylistOfflineClick = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                mLastClickTime = SystemClock.elapsedRealtime();
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            onItemClick(MusicResource.hashMapPlaylistOffline
                    .get(MusicResource.playListSelectedTitle), "playlist_offline");
            MusicResource.MODE = 31;
            MusicResource.playlistOfflineSelected = new ArrayList<>(MusicResource.hashMapPlaylistOffline
                    .get(MusicResource.playListSelectedTitle));
        }
    };

    BroadcastReceiver onItemSearchClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                mLastClickTime = SystemClock.elapsedRealtime();
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            onItemClick(MusicResource.searchResultList, "search");
        }
    };
    BroadcastReceiver OnclickSongItemAlbumDetailReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                mLastClickTime = SystemClock.elapsedRealtime();
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            onItemClick(MusicResource.albumCollectionContent, "album");
        }
    };

    BroadcastReceiver OnclickSongItemDownloadedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                mLastClickTime = SystemClock.elapsedRealtime();
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            onItemClick(MusicResource.songListDownload, "downloaded");
            MusicResource.MODE = 2;
        }
    };


    @Override
    public void onResume() {
        IntentFilter onItemSongClickItent = new IntentFilter("OnclickSongItem");
        getActivity().registerReceiver(onItemSongClick, onItemSongClickItent);

        IntentFilter OnClickFolderSongsItem = new IntentFilter("OnClickFolderSongsItem");
        getActivity().registerReceiver(OnClickFolderSongsItemReceiver, OnClickFolderSongsItem);
        IntentFilter OnclickSongItemSearchOffline = new IntentFilter("OnclickSongItemSearchOffline");
        getActivity().registerReceiver(onItemSearchClickReceiver, OnclickSongItemSearchOffline);
        IntentFilter OnclickSongItemAlbumDetail = new IntentFilter("OnclickSongItemAlbumDetail1");
        getActivity().registerReceiver(OnclickSongItemAlbumDetailReceiver, OnclickSongItemAlbumDetail);
        IntentFilter OnclickSongItemDownloaded = new IntentFilter("OnclickSongItemDownloaded");
        getActivity().registerReceiver(OnclickSongItemDownloadedReceiver, OnclickSongItemDownloaded);
        IntentFilter getAlbumDone = new IntentFilter("getAlbumDone");
        getActivity().registerReceiver(getAlbumDoneReceiver, getAlbumDone);
        IntentFilter getSongListDone = new IntentFilter("getSongListDone");
        getActivity().registerReceiver(getSongListDoneReceiver, getSongListDone);
        IntentFilter deleteTrackDone = new IntentFilter("delete_track_done");
        getActivity().registerReceiver(deleteTrackDoneReceiver, deleteTrackDone);
        IntentFilter onClickItemPlaylistOffline = new IntentFilter("OnClickPlaylistOfflineItem");
        getActivity().registerReceiver(onItemPlaylistOfflineClick, onClickItemPlaylistOffline);
        super.onResume();
    }

    BroadcastReceiver deleteTrackDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicResource.songList.remove(MusicResource.song_deleted);
            songAdt.notifyDataSetChanged();
        }
    };

    BroadcastReceiver getSongListDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicResource.songList != null && MusicResource.songList.size() > 1)
                Collections.sort(MusicResource.songList, new Comparator<Song>() {
                    @Override
                    public int compare(Song lhs, Song rhs) {
                        return lhs.getTitle().compareTo(rhs.getTitle());
                    }
                });
            songAdt = new SongAdapter(getContext(), MusicResource.songList, "");
            recyclerView.setAdapter(songAdt);
        }
    };
    BroadcastReceiver getAlbumDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getSongList();
        }
    };

    TextView subArtist;
    TextView subTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.songs, container, false);

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

        return view;
    }

    public void setImageDefault() {
        Drawable drawable = getResources().getDrawable(R.drawable.nice_view_main_background_2);
        MusicResource.subArt = ((BitmapDrawable) drawable).getBitmap();
        subArt.setImageBitmap(MusicResource.subArt);
        MusicResource.noti_art = ImageHelper.getRoundedCornerBitmap(MusicResource.subArt, 30);
        Bitmap bitmapResized = Bitmap.createScaledBitmap(MusicResource.subArt, 200, 200, false);
        MusicResource.blurredBitmap = BlurBuilder.blur(getActivity(),
                bitmapResized);
    }

    public void onItemClick(ArrayList<Song> songs, String type) {
        if (Player.player != null) {
            Player.player.reset();
        }

        MusicResource.track_playing = null;

        MusicResource.song_playing = songs.get(MusicResource.songPosition);

        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                .EXTERNAL_CONTENT_URI, songs.get(MusicResource.songPosition).getId());

        Player.player = MediaPlayer.create(getContext(), uri);
        if (Player.player == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.not_support), Toast.LENGTH_SHORT).show();
            return;
        }
        Player.player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (songs.get(MusicResource.songPosition).getArt_uri() != null) {
            Bitmap temp = decodeSampledBitmapFromUri(
                    songs.get(MusicResource.songPosition).getArt_uri(), 512, 512);
            if (temp != null) {
                MusicResource.subArt = temp;
                MusicResource.blurredBitmap = BlurBuilder.blur(getActivity(), MusicResource.subArt);

                subArt.setImageBitmap(MusicResource.subArt);
                MusicResource.noti_art = ImageHelper.getRoundedCornerBitmap(MusicResource.subArt, 30);
            } else {
                setImageDefault();
            }
        } else setImageDefault();
        MusicResource.subTitle = songs.get(MusicResource.songPosition).getTitle();
        MusicResource.subArtist = songs.get(MusicResource.songPosition).getArtist();
        subTitle.setText(MusicResource.subTitle);
        subArtist.setText(MusicResource.subArtist);


        sub_play_btn.setBackgroundResource(R.drawable.play_btn);

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

        ImageViewAnimatedChange(getActivity(), main_background, MusicResource.blurredBitmap);

        if (MusicResource.request_audio_focus(getContext())) {
            Player.player.start();
            Player.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    if (MusicResource.repeat_mode == 0) {
                        if (MusicResource.shuffle)
                            getActivity().sendBroadcast(new Intent("intentFilterDoShuffle"));
                        else
                            getActivity().sendBroadcast(new Intent("intentFilterDoNext"));
                    } else {
                        getActivity().sendBroadcast(new Intent("intentFilterDoRepeat"));
                    }
                }
            });
        }
        MusicResource.csn_Notification(getContext(),
                MusicResource.noti_art,
                songs.get(MusicResource.songPosition).getTitle(),
                songs.get(MusicResource.songPosition).getArtist(), "p");
        subPlaySong.setVisibility(View.VISIBLE);
        MusicResource.subPlaySongVisible = true;
        MusicResource.isCanChange = true;
        recyclerView.setEnabled(false);
        subPlaySong.setEnabled(true);
        if (type.equals("album"))
            getActivity().sendBroadcast(new Intent("OnclickSongItemAlbumDetailDone"));
        if (type.equals("song")) getActivity().sendBroadcast(new Intent("OnclickSongItemDone"));
        if (type.equals("playlist_offline"))
            getActivity().sendBroadcast(new Intent("onClickPlaylistOfflineItemDone"));
        if (type.equals("search"))
            getActivity().sendBroadcast(new Intent("OnclickSongItemSearchOfflineDone"));
        if (type.equals("folder"))
            getActivity().sendBroadcast(new Intent("onClickFolderOfflineItemDone"));
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    public void getSongList() {
        MusicResource.songList = new ArrayList<>();
        MusicResource.songListDownload = new ArrayList<>();
        String art_uri = null;

        ContentResolver musicResolver = getContext().getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {

            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int PathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do {
                long thisId = musicCursor.getLong(idColumn);
                int thisAlbumId = musicCursor.getInt(albumIdColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String Path = musicCursor.getString(PathColumn);

                for (Album album : MusicResource.albumList) {
                    if (thisAlbumId == album.getId()) {
                        art_uri = album.getArt_uri();
                        break;
                    }
                }
                //Song song = new Song(thisId, thisAlbumId, thisTitle, thisArtist, art_uri);

                Song song = new Song(thisId, thisAlbumId, thisTitle, thisArtist, art_uri, Path);
                MusicResource.songList.add(song);
                if (checkExistDownloadFolder(Path)) {
                    MusicResource.songListDownload.add(song);
                }

                String[] path_split = Path.split("\\/");
                String folder_path = Path.replace(path_split[path_split.length - 1], "");

                if (MusicResource.folderPathList.contains(folder_path)) {
                    MusicResource.hashMapFolderList.get(folder_path).add(song);
                } else {
                    MusicResource.folderPathList.add(folder_path);
                    String[] folder_path_split = folder_path.split("\\/");
                    String folder_name = folder_path_split[folder_path_split.length - 1];

                    MusicResource.folderList.add(new FolderModel(folder_name, folder_path));
                    MusicResource.hashMapFolderList.put(folder_path, new ArrayList<Song>());
                    MusicResource.hashMapFolderList.get(folder_path).add(song);
                }
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
        getActivity().sendBroadcast(new Intent("getSongListDone"));
    }

    public boolean checkExistDownloadFolder(String path) {
        if (MusicResource.listOfPathDownload != null && MusicResource.listOfPathDownload.size() > 0)
            for (String a : MusicResource.listOfPathDownload) {
                if (a != null)
                    if (path.contains(a)) return true;
            }
        return false;
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
        BitmapWorkerTask task = new BitmapWorkerTask(getContext(), imageView, 128, 128);
        task.execute(art_uri);
    }
}
