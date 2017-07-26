package com.kenung.vn.prettymusic.search.offline_search.song;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kenung.vn.prettymusic.BitmapWorkerTask;
import com.kenung.vn.prettymusic.MainActivity;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.SearchOfflineActivity;
import com.kenung.vn.prettymusic.module.ImageHelper;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by KXPRO on 5/11/2017.
 */

public class SearchOfflineSongAdapter extends RecyclerView.Adapter<SearchOfflineSongAdapter.DataViewHolder> {

    private ArrayList<Song> songs;
    private Context context;

    public SearchOfflineSongAdapter(Context context, ArrayList<Song> songs) {
        this.songs = songs;
        this.context = context;
    }

    @Override
    public SearchOfflineSongAdapter.DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;

        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song, parent, false);

        return new DataViewHolder(context, itemView, songs);
    }

    @Override
    public void onBindViewHolder(final DataViewHolder holder, final int position) {
        String title = songs.get(position).getTitle();
        String artist = songs.get(position).getArtist();
        holder.Title.setText(title);
        holder.Artist.setText(artist);
        holder.Title.setSelected(true);
        holder.Artist.setSelected(true);
        if (songs.get(position).getArt_uri() != null) {
            loadBitmap(songs.get(position).getArt_uri(), holder.Art);
        } else {
            loadBitmap(null, holder.Art);
        }

        holder.moreOpts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(context, R.style.MyPopupMenu);
                PopupMenu popupMenu = new PopupMenu(wrapper, holder.moreOpts);

                popupMenu.inflate(R.menu.song_options_menu);
                popupMenu.setGravity(Gravity.END);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.addToPlaylist: {
                                MusicResource.playlistOnlineDialog = new ArrayList<>(MusicResource.playlistOffline);
                                MusicResource.playlistOnlineDialog.add(0, new PlaylistOnlineModel(context.getResources().getString(R.string.create_playlist), 1));

                                SearchOfflineActivity.addToPlaylistOffline(context);
                                MusicResource.popupFromActivity = MusicResource.popupFromSearchOfflineActivity;

                                MusicResource.addToPlaylistSong = songs.get(position);
                            }
                            break;
                            case R.id.delete_track: {

                                if (!songs.get(position).equals(MusicResource.song_playing)) {
                                    MusicResource.song_deleted = songs.get(position);
                                    File file = new File(songs.get(position).getSong_path());
                                    file.delete();
                                    if (!file.exists()) {
                                        ContentResolver musicResolver = context.getContentResolver();
                                        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media
                                                .EXTERNAL_CONTENT_URI, MusicResource.song_deleted.getId());
                                        musicResolver.delete(uri, null, null);
                                        Log.d("TestDelete", songs.get(position).getSong_path());
                                        context.sendBroadcast(new Intent("delete_track_done"));
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.unable_delete),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(context,
                                            context.getResources().getString(R.string.unable_delete),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;
                        }

                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView Title;
        private TextView Artist;
        private ImageView Art;
        private ArrayList<Song> songs;
        private Context context;
        private ImageButton moreOpts;

        public DataViewHolder(Context context, View itemView, ArrayList<Song> songs) {
            super(itemView);
            this.context = context;
            this.songs = songs;
            itemView.setOnClickListener(this);
            Title = (TextView) itemView.findViewById(R.id.title);
            Artist = (TextView) itemView.findViewById(R.id.artist);
            Art = (ImageView) itemView.findViewById(R.id.art);
            moreOpts = (ImageButton) itemView.findViewById(R.id.moreoption);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            MusicResource.songPosition = position;
            context.sendBroadcast(new Intent("OnclickSongItemSearchOffline"));
        }
    }

    public void loadBitmap(String art_uri, ImageView imageView) {

        final String imageKey = String.valueOf(art_uri);

        final Bitmap bitmap = MusicResource.getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageBitmap(ImageHelper.getRoundedCornerBitmap(
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_thumbnail_default),
                    30));
            final BitmapWorkerTask task = new BitmapWorkerTask(context, imageView, 96, 96);
            //task.execute(art_uri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, art_uri);
            } else {
                task.execute(art_uri);
            }
        }
    }

}

