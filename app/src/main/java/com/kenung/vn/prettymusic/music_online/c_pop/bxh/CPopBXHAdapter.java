package com.kenung.vn.prettymusic.music_online.c_pop.bxh;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.kenung.vn.prettymusic.BitmapTrackWorkerTask;
import com.kenung.vn.prettymusic.MainActivity;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by KXPRO on 5/11/2017.
 */

public class CPopBXHAdapter extends RecyclerView.Adapter<CPopBXHAdapter.DataViewHolder> {

    private ArrayList<Track> c_pop_bxh;
    private Context context;
    private RequestManager glide;

    public CPopBXHAdapter(Context context, ArrayList<Track> c_pop_bxh, RequestManager glide) {
        this.c_pop_bxh = c_pop_bxh;
        this.context = context;
        this.glide = glide;
    }

    @Override
    public CPopBXHAdapter.DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;

        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.track, parent, false);

        return new DataViewHolder(context, itemView, c_pop_bxh);
    }

    @Override
    public void onBindViewHolder(final DataViewHolder holder, final int position) {
        String title = c_pop_bxh.get(position).getTitle();
        String artist = c_pop_bxh.get(position).getArtist();
        String duration = c_pop_bxh.get(position).getDuration();
        String quality = c_pop_bxh.get(position).getQuality();
        holder.Title.setText(title);
        holder.Artist.setText(artist);
        holder.Title.setSelected(true);
        holder.Artist.setSelected(true);
        holder.duration.setText(duration);
        holder.quality.setText(quality);
        if (quality.toLowerCase().contains("lossless"))
            holder.quality.setTextColor(Color.RED);
        else if (quality.toLowerCase().contains("500"))
            holder.quality.setTextColor(Color.rgb(255, 202, 130));
        else if (quality.toLowerCase().contains("320"))
            holder.quality.setTextColor(Color.rgb(150, 223, 234));
        else holder.quality.setTextColor(Color.WHITE);
        holder.progressBar.setVisibility(View.VISIBLE);

        if (c_pop_bxh.get(position).getUrl() != null) {
            loadBitmap(c_pop_bxh.get(position).getUrl(), holder.Art, holder.progressBar, position, 15);
        } else {
            loadBitmap(null, holder.Art, holder.progressBar, position, 15);
        }

        holder.moreOpts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(context, R.style.MyPopupMenu);
                PopupMenu popupMenu = new PopupMenu(wrapper, holder.moreOpts);

                popupMenu.inflate(R.menu.options_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.addToPlaylist: {
                                MusicResource.playlistOnlineDialog = new ArrayList<PlaylistOnlineModel>(MusicResource.playlistOnline);
                                MusicResource.playlistOnlineDialog.add(0, new PlaylistOnlineModel(context.getResources().getString(R.string.create_playlist), 1));
                                MainActivity.addToPlaylistOnline(context);
                                MusicResource.popupFromActivity = MusicResource.popupFromMainActivity;
                                MusicResource.addToPlaylistTrack = c_pop_bxh.get(position);
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
        return c_pop_bxh.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView Title;
        private TextView Artist;
        private ImageView Art;
        private TextView duration;
        private TextView quality;
        private ProgressBar progressBar;
        private ArrayList<Track> bxh;
        private Context context;
        private ImageButton moreOpts;

        public DataViewHolder(Context context, View itemView, ArrayList<Track> bxh) {
            super(itemView);
            this.context = context;
            this.bxh = bxh;
            itemView.setOnClickListener(this);
            Title = (TextView) itemView.findViewById(R.id.title);
            Artist = (TextView) itemView.findViewById(R.id.artist);
            Art = (ImageView) itemView.findViewById(R.id.art);
            duration = (TextView) itemView.findViewById(R.id.duration);
            quality = (TextView) itemView.findViewById(R.id.quality);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            moreOpts = (ImageButton) itemView.findViewById(R.id.moreoption);
        }

        @Override
        public void onClick(View v) {
            if (MusicResource.isCanChange) {
                int position = getAdapterPosition();
                Log.d("TestRecyclerView", "onClick - " + bxh.get(position).getTitle());
                MusicResource.songPosition = position;
                context.sendBroadcast(new Intent("CPopBXHonClicked"));
            }
        }
    }

    public void loadBitmap(String track_url, ImageView imageView, ProgressBar progressBar, int position, int mode) {

        final String imageKey = String.valueOf(track_url);

        final Bitmap bitmap = MusicResource.getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            progressBar.setVisibility(View.GONE);
            imageView.setImageBitmap(bitmap);
        } else if (cancelPotentialDownload(track_url, imageView)) {
            BitmapTrackWorkerTask task = new BitmapTrackWorkerTask(context, imageView, 128, 128, progressBar, position, mode, glide);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, track_url);
            } else {
                task.execute(track_url);
            }
        }
    }

    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapTrackWorkerTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapTrackWorkerTask bitmapDownloaderTask) {
            super(Color.TRANSPARENT);
            bitmapDownloaderTaskReference =
                    new WeakReference<>(bitmapDownloaderTask);
        }

        public BitmapTrackWorkerTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapTrackWorkerTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.track_url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);

            } else {
                return false;
            }
        }
        return true;
    }

    private static BitmapTrackWorkerTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();

            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }
}