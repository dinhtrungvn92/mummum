package com.kenung.vn.prettymusic.music_offline.album;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
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

public class Album_RecyclerView_Adt extends RecyclerView.Adapter<Album_RecyclerView_Adt.ViewHolder> {

    private ArrayList<Album> albums;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    String type;

    // data is passed into the constructor
    public Album_RecyclerView_Adt(Context context, ArrayList<Album> albums, String type) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.albums = albums;
        this.type = type;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.album, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = albums.get(position).getTitle();
        holder.title.setText(title);
        if (albums.get(position).getSong_number() == 1)
            holder.numbersong.setText(albums.get(position).getArtist() + " - " + String.valueOf(albums.get(position).getSong_number()) + " " + context.getResources().getString(R.string.one_song));
        else
            holder.numbersong.setText(albums.get(position).getArtist() + " - " + String.valueOf(albums.get(position).getSong_number()) + " " + context.getResources().getString(R.string.mul_song));
        holder.title.setSelected(true);
        holder.numbersong.setSelected(true);
        if (albums.get(position).getArt_uri() != null) {
            loadBitmap(albums.get(position).getArt_uri(), holder.art);
        } else {
            loadBitmap(null, holder.art);
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return albums.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public ImageView art;
        public TextView numbersong;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.titlealbum);
            art = (ImageView) itemView.findViewById(R.id.artalbum);
            numbersong = (TextView) itemView.findViewById(R.id.songnumber);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            MusicResource.albumPosition = position;
            if (type.equals("offline")) MusicResource.album_offline_search_toogle = 0;
            if (type.equals("search")) MusicResource.album_offline_search_toogle = 1;
            context.sendBroadcast(new Intent("OnclickAlbumItem"));
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