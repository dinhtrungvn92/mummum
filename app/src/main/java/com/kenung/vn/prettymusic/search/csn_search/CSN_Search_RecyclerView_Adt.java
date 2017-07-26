package com.kenung.vn.prettymusic.search.csn_search;

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
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.kenung.vn.prettymusic.AlbumDetailActivity;
import com.kenung.vn.prettymusic.BitmapTrackWorkerTask;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.SearchOnlineActivity;
import com.kenung.vn.prettymusic.listener.OnLoadMoreListener;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.playlist.PlaylistOnlineModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by sev_user on 27-Apr-17.
 */

public class CSN_Search_RecyclerView_Adt extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Track> csn_search_result;
    private Context context;
    String type;
    RequestManager glide;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private WeakReference<RecyclerView> recyclerViewWeakReference;
    private RecyclerView recyclerView;

    public CSN_Search_RecyclerView_Adt(Context context,
                                       ArrayList<Track> m_bxh_vn,
                                       final String type,
                                       RequestManager glide,
                                       RecyclerView rv) {
        csn_search_result = m_bxh_vn;
        this.context = context;
        this.type = type;
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

                if (type.equals("song")) {
                    if (MusicResource.CSNSearchSongResultTemp.size() > 20
                            && MusicResource.CSNSearchSongResultTemp.size() <= 100
                            && !isLoading
                            && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                } else if (type.equals("singer")) {
                    if (MusicResource.CSNSearchSingerResultTemp.size() > 20
                            && MusicResource.CSNSearchSingerResultTemp.size() <= 100
                            && !isLoading
                            && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.track, parent, false);
            return new DataViewHolder(context, itemView, csn_search_result);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_loading_item, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return csn_search_result.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
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
            String title = csn_search_result.get(position).getTitle();
            String artist = csn_search_result.get(position).getArtist();
            dataViewHolder.Title.setText(title);
            dataViewHolder.Artist.setText(artist);
            dataViewHolder.Title.setSelected(true);
            dataViewHolder.Artist.setSelected(true);
            if (csn_search_result.get(position).getDuration() != null)
                dataViewHolder.duration.setText(csn_search_result.get(position).getDuration());
            if (csn_search_result.get(position).getQuality() != null) {
                dataViewHolder.quality.setText(csn_search_result.get(position).getQuality());
                if (csn_search_result.get(position).getQuality().toLowerCase().contains("lossless"))
                    dataViewHolder.quality.setTextColor(Color.RED);
                else if (csn_search_result.get(position).getQuality().toLowerCase().contains("500"))
                    dataViewHolder.quality.setTextColor(Color.rgb(255, 202, 130));
                else if (csn_search_result.get(position).getQuality().toLowerCase().contains("320"))
                    dataViewHolder.quality.setTextColor(Color.rgb(150, 223, 234));
                else dataViewHolder.quality.setTextColor(Color.WHITE);
            }
            dataViewHolder.progressBar.setVisibility(View.VISIBLE);
            if (type.equals("shareplaylist")) {
                if (csn_search_result.get(position).getUrl() != null) {
                    loadBitmap(csn_search_result.get(position).getUrl(), dataViewHolder.Art, dataViewHolder.progressBar, position, 26);
                } else {
                    loadBitmap(null, dataViewHolder.Art, dataViewHolder.progressBar, position, 26);
                }
            } else if (!type.equals("playlist")) {
                if (csn_search_result.get(position).getUrl() != null) {
                    loadBitmap(csn_search_result.get(position).getUrl(), dataViewHolder.Art, dataViewHolder.progressBar, position, 24);
                } else {
                    loadBitmap(null, dataViewHolder.Art, dataViewHolder.progressBar, position, 24);
                }
            } else {
                if (csn_search_result.get(position).getUrl() != null) {
                    loadBitmap(csn_search_result.get(position).getUrl(), dataViewHolder.Art, dataViewHolder.progressBar, position, 25);
                } else {
                    loadBitmap(null, dataViewHolder.Art, dataViewHolder.progressBar, position, 25);
                }
            }

            dataViewHolder.moreOpts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context wrapper = new ContextThemeWrapper(context, R.style.MyPopupMenu);
                    PopupMenu popupMenu = new PopupMenu(wrapper, dataViewHolder.moreOpts);

                    if (type.equals("playlist")) {
                        popupMenu.inflate(R.menu.playlist_detail_options_menu);
                        Log.d("TestDialog", "playlsit");
                    } else {
                        popupMenu.inflate(R.menu.options_menu);
                        Log.d("TestDialog", "not playlist");
                    }
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.addToPlaylist: {
                                    MusicResource.playlistOnlineDialog = new ArrayList<>(MusicResource.playlistOnline);
                                    MusicResource.playlistOnlineDialog.add(0, new PlaylistOnlineModel(context.getResources().getString(R.string.create_playlist), 1));
                                    MusicResource.addToPlaylistTrack = csn_search_result.get(position);
                                    if (type.equals("album")) {
                                        AlbumDetailActivity.addToPlaylistOnline(context);
                                        MusicResource.popupFromActivity = MusicResource.popupFromSearchAlbumDetailActivity;
                                    } else {
                                        SearchOnlineActivity.addToPlaylistOnline(context);
                                        MusicResource.popupFromActivity = MusicResource.popupFromSearchActivity;
                                    }
                                }
                                break;
                                case R.id.removeOutOfPlaylist: {
                                    if (!csn_search_result.get(position).equals(MusicResource.track_playing)) {
                                        MusicResource.track_removed = csn_search_result.get(position);

                                        context.sendBroadcast(new Intent("track_removed"));
                                    } else {
                                        Toast.makeText(context,
                                                context.getString(R.string.unable_remove),
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
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(false);
        }
    }

    @Override
    public int getItemCount() {
        return csn_search_result.size();
    }

    public class DataViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Context context;
        private ArrayList<Track> csn_search_result;
        private TextView Title;
        private TextView Artist;
        private ImageView Art;
        private TextView duration;
        private TextView quality;
        private ProgressBar progressBar;
        private ImageButton moreOpts;

        public DataViewHolder(Context context, View itemView, ArrayList<Track> csn_search_result) {
            super(itemView);

            this.context = context;
            this.csn_search_result = csn_search_result;
            itemView.setOnClickListener(this);
            Title = (TextView) itemView.findViewById(R.id.title);
            Artist = (TextView) itemView.findViewById(R.id.artist);
            Art = (ImageView) itemView.findViewById(R.id.art);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            duration = (TextView) itemView.findViewById(R.id.duration);
            quality = (TextView) itemView.findViewById(R.id.quality);
            moreOpts = (ImageButton) itemView.findViewById(R.id.moreoption);
        }

        @Override
        public void onClick(View v) {
            if (!MusicResource.network_state) {
                Toast.makeText(context, context.getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            if (MusicResource.isCanChange) {
                int position = getAdapterPosition();
                MusicResource.songPosition = position;
                switch (type) {
                    case "song":
                        context.sendBroadcast(new Intent("noti_CSN_Search_Song_onClicked"));
                        break;
                    case "singer":
                        context.sendBroadcast(new Intent("noti_CSN_Search_Singer_onClicked"));
                        break;
                    case "album":
                        context.sendBroadcast(new Intent("noti_CSN_Search_Album_Item_onClicked"));
                        break;
                    case "playlist":
                        context.sendBroadcast(new Intent("noti_CSN_Play_List_Item_onClicked"));
                        break;
                    case "shareplaylist":
                        context.sendBroadcast(new Intent("noti_CSN_Share_Play_List_Item_onClicked"));
                        break;
                }
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

            Drawable drawable = imageView.getDrawable();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, track_url);
                Log.d("TestMultiTask", track_url);
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

        Log.d("TestMultiTask", "cencelPotentialDownload");
        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.track_url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
                if (bitmapUrl == null) Log.d("TestMultiTask", "Task Cancel - bitmapUrl is null");
                else {
                    Log.d("TestMultiTask", "Task Cancel - bitmapUrl not equals previous track_url");
                    Log.d("TestMultiTask", "current url : " + url);
                    Log.d("TestMultiTask", "previous url : " + bitmapUrl);
                }
            } else {
                // The same URL is already being downloaded.
                Log.d("TestMultiTask", "Task exist");
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