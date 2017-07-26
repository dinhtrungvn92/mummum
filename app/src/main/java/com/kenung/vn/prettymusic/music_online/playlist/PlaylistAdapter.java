package com.kenung.vn.prettymusic.music_online.playlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kenung.vn.prettymusic.BitmapWorkerTask;
import com.kenung.vn.prettymusic.Define;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.ServerRequest;
import com.kenung.vn.prettymusic.module.ImageHelper;
import com.kenung.vn.prettymusic.music_offline.song.Song;
import com.kenung.vn.prettymusic.music_online.Track;

import java.util.ArrayList;

/**
 * Created by KXPRO on 5/7/2017.
 */

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private ArrayList<PlaylistOnlineModel> playlist;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private String type;

    // data is passed into the constructor
    public PlaylistAdapter(Context context, ArrayList<PlaylistOnlineModel> albums, String type) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.playlist = albums;
        this.type = type;
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String title = playlist.get(position).getTitle();
        final int size = playlist.get(position).getSize();
        holder.title.setText(title);
        holder.title.setSelected(true);
        holder.size.setText(size + " " + context.getResources().getString(R.string.one_song));
        holder.title.setTextColor(Color.WHITE);
        holder.size.setTextColor(Color.WHITE);

        if (playlist.get(position).getArt_uri() != null) {
            loadBitmap(playlist.get(position).getArt_uri(), holder.art);
        } else {
            loadBitmap(null, holder.art);
        }

        if (type.equals("share_playlist")) {
            holder.view_count.setVisibility(View.VISIBLE);
            holder.view_count.setText(playlist.get(position).getView_count() + "");
            holder.moreOpts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context wrapper = new ContextThemeWrapper(context, R.style.MyPopupMenu);
                    PopupMenu popupMenu = new PopupMenu(wrapper, holder.moreOpts);

                    popupMenu.inflate(R.menu.share_playlist);
                    popupMenu.setGravity(Gravity.END);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.sharePlaylist: {
                                    String title = MusicResource.playlistOnlineShare.get(position).getTitle();
                                    int size = MusicResource.playlistOnlineShare.get(position).getSize();
                                    String playlist_id = MusicResource.playlistOnlineShare.get(position).getPlaylist_id();
                                    PlaylistOnlineModel playlistOnlineModel = new PlaylistOnlineModel(title, size, playlist_id);
                                    context.sendBroadcast(new Intent("AddToMyPlaylist"));
                                    new ServerRequest.GetSongsFromPlaylist(MusicResource.playlistOnlineShare.get(position).getPlaylist_id(), context, "add", playlistOnlineModel).execute();
                                }
                                break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        } else if (type.equals("playlist_online")) {
            holder.moreOpts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context wrapper = new ContextThemeWrapper(context, R.style.MyPopupMenu);
                    PopupMenu popupMenu = new PopupMenu(wrapper, holder.moreOpts);

                    popupMenu.inflate(R.menu.my_playlist);
                    popupMenu.setGravity(Gravity.END);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.playlist_rename: {

                                    new MaterialDialog.Builder(context)
                                            .title(context.getString(R.string.rename_playlist))
                                            .inputType(InputType.TYPE_CLASS_TEXT)
                                            .input(context.getString(R.string.input_playlits_name), "", new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                                    if (!input.toString().replaceAll(" ", "").equals("")) {
                                                        for (PlaylistOnlineModel p : type.equals("playlist_online") ?
                                                                MusicResource.playlistOnline : MusicResource.playlistOffline) {
                                                            if (p.getTitle()
                                                                    .replaceFirst(input.toString(), "")
                                                                    .replaceAll(" ", "")
                                                                    .equals("") || input.toString()
                                                                    .replaceFirst(p.getTitle(), "")
                                                                    .replaceAll(" ", "")
                                                                    .equals("")) {
                                                                Toast.makeText(context,
                                                                        context.getString(R.string.playlist_exist),
                                                                        Toast.LENGTH_LONG)
                                                                        .show();
                                                                return;
                                                            }
                                                        }

                                                        if (type.equals("playlist_online")) {

                                                            ArrayList<Track> playlistOnlineModels
                                                                    = MusicResource.hashMapPlaylistOnline.get(title);

                                                            MusicResource.hashMapPlaylistOnline.remove(title);
                                                            String new_title = input.toString();
                                                            MusicResource.hashMapPlaylistOnline.put(new_title, playlistOnlineModels);

                                                            MusicResource.playlistOnline.get(position)
                                                                    .setTitle(new_title);

                                                            context.sendBroadcast(new Intent("playlist_rename"));
                                                        } else if (type.equals("playlist_offline")) {

                                                            ArrayList<Song> playlistOfflineModels
                                                                    = MusicResource.hashMapPlaylistOffline.get(title);

                                                            MusicResource.hashMapPlaylistOffline.remove(title);
                                                            String new_title = input.toString();
                                                            MusicResource.hashMapPlaylistOffline.put(new_title, playlistOfflineModels);

                                                            MusicResource.playlistOffline.get(position)
                                                                    .setTitle(new_title);

                                                            context.sendBroadcast(new Intent("playlist_offline_rename"));
                                                        }
                                                    } else {
                                                        Toast.makeText(context,
                                                                context.getString(R.string.playlist_null),
                                                                Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                }
                                            }).show();

                                }
                                break;
                                case R.id.playlist_delete: {

                                    if (type.equals("playlist_online")) {
                                        if (!MusicResource.hashMapPlaylistOnline.get(title)
                                                .contains(MusicResource.track_playing)) {

                                            MusicResource.playlist_deleted = playlist.get(position);
                                            context.sendBroadcast(new Intent("playlist_delete"));
                                        } else {
                                            Toast.makeText(context,
                                                    context.getResources().getString(R.string.unable_delete),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else if (type.equals("playlist_offline")) {
                                        if (!MusicResource.hashMapPlaylistOffline.get(title)
                                                .contains(MusicResource.song_playing)) {

                                            MusicResource.playlist_deleted = playlist.get(position);
                                            context.sendBroadcast(new Intent("playlist_offline_delete"));
                                        } else {
                                            Toast.makeText(context,
                                                    context.getResources().getString(R.string.unable_delete),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                break;
                                case R.id.playlist_share:
                                    String playlist_name = title;
                                    int song_number = size;
                                    String playlist_id = Define.GetPlayListID(playlist_name);
                                    new ServerRequest.AddPlayList(playlist_id, playlist_name, song_number, context).execute();
                                    new ServerRequest.AddListSong(MusicResource.hashMapPlaylistOnline.get(MusicResource.playlistOnline.get(position).getTitle()), playlist_id, context).execute();
                                    context.sendBroadcast(new Intent("SharePlaylist"));
                                    break;
                            }

                            return false;
                        }
                    });

                    popupMenu.show();
                }
            });
        } else {
            holder.moreOpts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context wrapper = new ContextThemeWrapper(context, R.style.MyPopupMenu);
                    PopupMenu popupMenu = new PopupMenu(wrapper, holder.moreOpts);

                    popupMenu.inflate(R.menu.playlist_offline);
                    popupMenu.setGravity(Gravity.END);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.playlist_rename: {

                                    new MaterialDialog.Builder(context)
                                            .title(context.getString(R.string.rename_playlist))
                                            .inputType(InputType.TYPE_CLASS_TEXT)
                                            .input(context.getString(R.string.input_playlits_name), "", new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                                    if (!input.toString().replaceAll(" ", "").equals("")) {
                                                        for (PlaylistOnlineModel p : type.equals("playlist_online") ?
                                                                MusicResource.playlistOnline : MusicResource.playlistOffline) {
                                                            if (p.getTitle()
                                                                    .replaceFirst(input.toString(), "")
                                                                    .replaceAll(" ", "")
                                                                    .equals("") || input.toString()
                                                                    .replaceFirst(p.getTitle(), "")
                                                                    .replaceAll(" ", "")
                                                                    .equals("")) {
                                                                Toast.makeText(context,
                                                                        context.getString(R.string.playlist_exist),
                                                                        Toast.LENGTH_LONG)
                                                                        .show();
                                                                return;
                                                            }
                                                        }

                                                        if (type.equals("playlist_online")) {

                                                            ArrayList<Track> playlistOnlineModels
                                                                    = MusicResource.hashMapPlaylistOnline.get(title);

                                                            MusicResource.hashMapPlaylistOnline.remove(title);
                                                            String new_title = input.toString();
                                                            MusicResource.hashMapPlaylistOnline.put(new_title, playlistOnlineModels);

                                                            MusicResource.playlistOnline.get(position)
                                                                    .setTitle(new_title);

                                                            context.sendBroadcast(new Intent("playlist_rename"));
                                                        } else if (type.equals("playlist_offline")) {

                                                            ArrayList<Song> playlistOfflineModels
                                                                    = MusicResource.hashMapPlaylistOffline.get(title);

                                                            MusicResource.hashMapPlaylistOffline.remove(title);
                                                            String new_title = input.toString();
                                                            MusicResource.hashMapPlaylistOffline.put(new_title, playlistOfflineModels);

                                                            MusicResource.playlistOffline.get(position)
                                                                    .setTitle(new_title);

                                                            context.sendBroadcast(new Intent("playlist_offline_rename"));
                                                        }
                                                    } else {
                                                        Toast.makeText(context,
                                                                context.getString(R.string.playlist_null),
                                                                Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                }
                                            }).show();

                                }
                                break;
                                case R.id.playlist_delete: {

                                    if (type.equals("playlist_online")) {
                                        if (!MusicResource.hashMapPlaylistOnline.get(title)
                                                .contains(MusicResource.track_playing)) {

                                            MusicResource.playlist_deleted = playlist.get(position);
                                            context.sendBroadcast(new Intent("playlist_delete"));
                                        } else {
                                            Toast.makeText(context,
                                                    context.getResources().getString(R.string.unable_delete),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else if (type.equals("playlist_offline")) {
                                        if (!MusicResource.hashMapPlaylistOffline.get(title)
                                                .contains(MusicResource.song_playing)) {

                                            MusicResource.playlist_deleted = playlist.get(position);
                                            context.sendBroadcast(new Intent("playlist_offline_delete"));
                                        } else {
                                            Toast.makeText(context,
                                                    context.getResources().getString(R.string.unable_delete),
                                                    Toast.LENGTH_SHORT).show();
                                        }
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
        private ImageButton moreOpts;
        public TextView view_count;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            art = (ImageView) itemView.findViewById(R.id.art);
            size = (TextView) itemView.findViewById(R.id.size);
            itemView.setOnClickListener(this);
            moreOpts = (ImageButton) itemView.findViewById(R.id.moreoption);
            view_count = (TextView) itemView.findViewById(R.id.view_count);
        }

        @Override
        public void onClick(View view) {
            if (type.equals("playlist_online")) {
                MusicResource.playlistPosition = getAdapterPosition();
                MusicResource.playListSelectedTitle = MusicResource.playlistOnline.get(MusicResource.playlistPosition).getTitle();
                if (MusicResource.playlistOnline.get(MusicResource.playlistPosition).getSize() == 0) {
                    Toast.makeText(context, context.getResources().getString(R.string.playlist_no_track), Toast.LENGTH_SHORT).show();
                } else context.sendBroadcast(new Intent("OnclickPlaylistOnlineItem"));
            } else if (type.equals("playlist_offline")) {
                MusicResource.playlistPosition = getAdapterPosition();
                MusicResource.playListSelectedTitle = MusicResource.playlistOffline.get(MusicResource.playlistPosition).getTitle();
                if (MusicResource.playlistOffline.get(MusicResource.playlistPosition).getSize() == 0) {
                    Toast.makeText(context, context.getString(R.string.playlist_no_track), Toast.LENGTH_SHORT).show();
                } else context.sendBroadcast(new Intent("OnclickPlaylistOfflineItem"));
            } else if (type.equals("share_playlist")) {
                if (!MusicResource.isRefreshingSharePlaylist) {
                    MusicResource.playlistPosition = getAdapterPosition();
                    MusicResource.playListSelectedTitle = MusicResource.playlistOnlineShare.get(MusicResource.playlistPosition).getTitle();
                    context.sendBroadcast(new Intent("OnclickPlaylistShareItemStart"));
                    new ServerRequest.GetSongsFromPlaylist(MusicResource.playlistOnlineShare.get(MusicResource.playlistPosition).getPlaylist_id(), context, "view", null).execute();
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