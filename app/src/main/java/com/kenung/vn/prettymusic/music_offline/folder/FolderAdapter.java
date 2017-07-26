package com.kenung.vn.prettymusic.music_offline.folder;

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
import android.widget.Toast;

import com.kenung.vn.prettymusic.BitmapWorkerTask;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.module.ImageHelper;

import java.util.ArrayList;

/**
 * Created by KXPRO on 5/7/2017.
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private ArrayList<FolderModel> folders;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    String type;

    // data is passed into the constructor
    public FolderAdapter(Context context, ArrayList<FolderModel> folders) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.folders = folders;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.folder_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String folder_name = folders.get(position).getFolder_name();
        String folder_path = folders.get(position).getFolder_path();

        folders.get(position).setFolder_size(MusicResource.hashMapFolderList
                .get(folder_path).size());

        int folder_size = folders.get(position).getFolder_size();

        holder.folder_name.setText(folder_name);
        holder.folder_path.setText(folder_path);

        if (folder_size == 1)
            holder.folder_size.setText(folder_size + " "
                    + context.getResources().getString(R.string.one_song) + " :");
        else
            holder.folder_size.setText(folder_size + " "
                    + context.getResources().getString(R.string.mul_song) + " :");

        holder.folder_name.setSelected(true);
        holder.folder_path.setSelected(true);
        if (folders.get(position).getArt_uri() != null) {
            loadBitmap(folders.get(position).getArt_uri(), holder.art);
        } else {
            loadBitmap(null, holder.art);
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return folders.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView folder_name;
        public TextView folder_path;
        public TextView folder_size;
        public ImageView art;

        public ViewHolder(View itemView) {
            super(itemView);
            folder_name = (TextView) itemView.findViewById(R.id.folder_name);
            folder_path = (TextView) itemView.findViewById(R.id.folder_path);
            folder_size = (TextView) itemView.findViewById(R.id.folder_size);
            art = (ImageView) itemView.findViewById(R.id.art);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            MusicResource.folderPositon = position;
            MusicResource.folderSelectedTitle = MusicResource.folderPathList.get(position);
            context.sendBroadcast(new Intent("OnclickFolderItem"));
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