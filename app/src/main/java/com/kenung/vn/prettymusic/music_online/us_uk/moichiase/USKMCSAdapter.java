package com.kenung.vn.prettymusic.music_online.us_uk.moichiase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
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
import com.kenung.vn.prettymusic.listener.OnLoadMoreListener;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by KXPRO on 5/11/2017.
 */

public class USKMCSAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Track> usk_mcs;
    private Context context;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private WeakReference<RecyclerView> recyclerViewWeakReference;
    private RecyclerView recyclerView;
    RequestManager glide;

    public USKMCSAdapter(Context context, ArrayList<Track> usk_mcs, RecyclerView rv, RequestManager glide) {
        this.usk_mcs = usk_mcs;
        this.context = context;
        this.glide = glide;

        recyclerViewWeakReference = new WeakReference<>(rv);

        if (recyclerViewWeakReference != null) {
            this.recyclerView = recyclerViewWeakReference.get();
        }

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (MusicResource.USK_MCS_Refresh.size() > 20
                        && MusicResource.USK_MCS_Refresh.size() <= 160
                        && !isLoading
                        && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.track, parent, false);
            return new DataViewHolder(context, itemView, usk_mcs);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_loading_item, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return usk_mcs.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public void setLoaded() {
        isLoading = false;
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof DataViewHolder) {
            final DataViewHolder dataViewHolder = (DataViewHolder) holder;
            String title = usk_mcs.get(position).getTitle();
            String artist = usk_mcs.get(position).getArtist();
            String duration = usk_mcs.get(position).getDuration();
            String quality = usk_mcs.get(position).getQuality();
            dataViewHolder.Title.setText(title);
            dataViewHolder.Artist.setText(artist);
            dataViewHolder.Title.setSelected(true);
            dataViewHolder.Artist.setSelected(true);
            dataViewHolder.duration.setText(duration);
            dataViewHolder.quality.setText(quality);
            if (quality.toLowerCase().contains("lossless"))
                dataViewHolder.quality.setTextColor(Color.RED);
            else if (quality.toLowerCase().contains("500"))
                dataViewHolder.quality.setTextColor(Color.rgb(255, 202, 130));
            else if (quality.toLowerCase().contains("320"))
                dataViewHolder.quality.setTextColor(Color.rgb(150, 223, 234));
            else dataViewHolder.quality.setTextColor(Color.WHITE);
            dataViewHolder.progressBar.setVisibility(View.VISIBLE);

            if (usk_mcs.get(position).getUrl() != null) {
                loadBitmap(usk_mcs.get(position).getUrl(), dataViewHolder.Art, dataViewHolder.progressBar, position, 19);
            } else {
                loadBitmap(null, dataViewHolder.Art, dataViewHolder.progressBar, position, 19);
            }

            dataViewHolder.moreOpts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context wrapper = new ContextThemeWrapper(context, R.style.MyPopupMenu);
                    PopupMenu popupMenu = new PopupMenu(wrapper, dataViewHolder.moreOpts);

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
                                    MusicResource.addToPlaylistTrack = usk_mcs.get(position);
                                }
                                break;
                            }

                            return false;
                        }
                    });

                    popupMenu.show();
                }
            });
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(false);
        }
    }

    @Override
    public int getItemCount() {
        return usk_mcs.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView Title;
        private TextView Artist;
        private ImageView Art;
        private TextView duration;
        private TextView quality;
        private ProgressBar progressBar;
        private ArrayList<Track> mcs;
        private Context context;
        private ImageButton moreOpts;

        public DataViewHolder(Context context, View itemView, ArrayList<Track> mcs) {
            super(itemView);
            this.context = context;
            this.mcs = mcs;
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
                Log.d("TestRecyclerView", "onClick - " + mcs.get(position).getTitle());
                MusicResource.songPosition = position;
                context.sendBroadcast(new Intent("USKMCSonClicked"));
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