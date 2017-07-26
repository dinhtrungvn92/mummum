package com.kenung.vn.prettymusic.music_online.playlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kenung.vn.prettymusic.BitmapWorkerTask;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.module.ImageHelper;

import java.util.ArrayList;

/**
 * Created by KXPRO on 5/7/2017.
 */

public class PlaylistOnlineDialogAdapter extends RecyclerView.Adapter<PlaylistOnlineDialogAdapter.ViewHolder> {

    private ArrayList<PlaylistOnlineModel> playlist;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    public PlaylistOnlineDialogAdapter(Context context, ArrayList<PlaylistOnlineModel> albums) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.playlist = albums;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.playlist_online_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = playlist.get(position).getTitle();
        int size = playlist.get(position).getSize();
        int type = playlist.get(position).getMode();
        holder.title.setText(title);
        holder.title.setSelected(true);
        if (type == 0) {
            holder.size.setText(size + " " + context.getResources().getString(R.string.one_song));
            holder.title.setTextColor(Color.BLACK);
            holder.size.setTextColor(Color.BLACK);

            if (playlist.get(position).getArt_uri() != null) {
                loadBitmap(playlist.get(position).getArt_uri(), holder.art);
            } else {
                loadBitmap(null, holder.art);
            }
        } else {
            holder.art.setScaleType(ImageView.ScaleType.CENTER);
            holder.art.setImageResource(R.drawable.ic_add_black_48dp);
            holder.size.setVisibility(View.GONE);
            holder.title.setGravity(Gravity.CENTER_VERTICAL);
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return playlist.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public ImageView art;
        public TextView size;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            art = (ImageView) itemView.findViewById(R.id.art);
            size = (TextView) itemView.findViewById(R.id.size);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            /*MusicResource.playlistPosition = getAdapterPosition();
            context.sendBroadcast(new Intent("OnclickPlaylistOnlineItem"));*/
            int position = getAdapterPosition();
            if (position == 0) {
                switch (MusicResource.popupFromActivity) {
                    case MusicResource.popupFromMainActivity:
                        context.sendBroadcast(new Intent("addToNewPlaylistOnline"));
                        break;
                    case MusicResource.popupFromPlayActivity:
                        context.sendBroadcast(new Intent("addToNewPlaylistOnlineFromPlayActivity"));
                        break;
                    case MusicResource.popupFromSearchActivity:
                        context.sendBroadcast(new Intent("addToNewPlaylistOnlineFromSearchActivity"));
                        break;
                    case MusicResource.popupFromSearchAlbumDetailActivity:
                        context.sendBroadcast(new Intent("addToNewPlaylistOnlineFromSearchAlbumDetailActivity"));
                        break;
                }
            } else {
                MusicResource.addToPlayListModel = playlist.get(position);
                switch (MusicResource.popupFromActivity) {
                    case MusicResource.popupFromMainActivity:
                        context.sendBroadcast(new Intent("addToExistPlaylistOnline"));
                        break;
                    case MusicResource.popupFromPlayActivity:
                        context.sendBroadcast(new Intent("addToExistPlaylistOnlineFromPlayActivity"));
                        break;
                    case MusicResource.popupFromSearchActivity:
                        context.sendBroadcast(new Intent("addToExistPlaylistOnlineFromSearchActivity"));
                        break;
                    case MusicResource.popupFromSearchAlbumDetailActivity:
                        context.sendBroadcast(new Intent("addToExistPlaylistOnlineFromSearchAlbumDetailActivity"));
                        break;
                }

            }
        }
    }


    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
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
            final BitmapWorkerTask task = new BitmapWorkerTask(context, imageView, 128, 128);
            //task.execute(art_uri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, art_uri);
            } else {
                task.execute(art_uri);
            }
        }
    }
}