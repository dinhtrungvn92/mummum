package com.kenung.vn.prettymusic.search.csn_search.album;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.listener.OnLoadMoreListener;
import com.kenung.vn.prettymusic.module.ImageHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by sev_user on 27-Apr-17.
 */

public class CSN_Search_Album_RecyclerView_Adt extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<AlbumModelSearch> csn_search_result;
    private Context context;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private WeakReference<RecyclerView> recyclerViewWeakReference;
    private RecyclerView recyclerView;

    public CSN_Search_Album_RecyclerView_Adt(Context context,
                                             ArrayList<AlbumModelSearch> csn_search_result,
                                             RecyclerView rv) {
        this.csn_search_result = csn_search_result;
        this.context = context;

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

                // Remove loadMore func for album search

                /*if (MusicResource.CSNSearchAlbumResultTemp.size() > 10
                        && MusicResource.CSNSearchAlbumResultTemp.size() <= 100
                        && !isLoading
                        && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }*/

            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.csn_search_view, parent, false);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            DataViewHolder dataViewHolder = (DataViewHolder) holder;
            String title = csn_search_result.get(position).getTitle();
            String artist = csn_search_result.get(position).getArtist();
            int size = csn_search_result.get(position).getSize();
            if (size > 0) {
                if (size == 1)
                    dataViewHolder.Artist.setText(artist + " - " + size + " " + context.getResources().getString(R.string.one_song));
                else
                    dataViewHolder.Artist.setText(artist + " - " + size + " " + context.getResources().getString(R.string.mul_song));
            } else {
                dataViewHolder.Artist.setText(artist);
            }
            dataViewHolder.Title.setText(title);
            dataViewHolder.Title.setSelected(true);
            dataViewHolder.Artist.setSelected(true);
            dataViewHolder.progressBar.setVisibility(View.VISIBLE);

            final String art_src = csn_search_result.get(position).getArt_src();
            if (art_src != null) {
                //loadBitmap(csn_search_result.get(position).getArt_src(), holder.Art, holder.progressBar, position, 3);

                final WeakReference<ImageView> imageViewWeakReference = new WeakReference<ImageView>(dataViewHolder.Art);
                final WeakReference<ProgressBar> progressBarWeakReference = new WeakReference<ProgressBar>(dataViewHolder.progressBar);

                if (imageViewWeakReference != null) {
                    ImageView imageView = imageViewWeakReference.get();
                    if (imageView != null) {
                        imageView.setImageDrawable(null); // Possibly runOnUiThread()
                    }
                }
                Glide
                        .with(context)
                        .load(art_src)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                Bitmap bitmap = ImageHelper.getRoundedCornerBitmap(resource, 30);
                                MusicResource.addBitmapToMemCache(String.valueOf(art_src), bitmap);

                                if (imageViewWeakReference != null) {
                                    ImageView imageView = imageViewWeakReference.get();
                                    if (imageView != null) {
                                        if (imageView.getDrawable() == null)
                                            imageView.setImageBitmap(bitmap); // Possibly runOnUiThread()
                                    }
                                    if (progressBarWeakReference != null) {
                                        ProgressBar progressBar = progressBarWeakReference.get();
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                }
                            }
                        });
            } else {
                //loadBitmap(null, holder.Art, holder.progressBar, position, 3);
            }
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(false);
        }
    }

    @Override
    public int getItemCount() {
        return csn_search_result.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Context context;
        private ArrayList<AlbumModelSearch> csn_search_result;
        private TextView Title;
        private TextView Artist;
        private ImageView Art;
        private ProgressBar progressBar;

        public DataViewHolder(Context context, View itemView, ArrayList<AlbumModelSearch> csn_search_result) {
            super(itemView);

            this.context = context;
            this.csn_search_result = csn_search_result;
            itemView.setOnClickListener(this);
            Title = (TextView) itemView.findViewById(R.id.title);
            Artist = (TextView) itemView.findViewById(R.id.artist);
            Art = (ImageView) itemView.findViewById(R.id.art);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }

        @Override
        public void onClick(View v) {
            if (!MusicResource.network_state) {
                Toast.makeText(context, context.getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            int position = getAdapterPosition();

            MusicResource.albumPosition = position;

            context.sendBroadcast(new Intent("noti_CSN_Search_Album_onClicked"));
        }
    }
}