package com.kenung.vn.prettymusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.kenung.vn.prettymusic.module.ImageHelper;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.v_pop.bxh.VPopBXHAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sev_user on 25-Mar-17.
 */

public class BitmapTrackWorkerTask extends AsyncTask<String, Void, String> {

    private final WeakReference<ImageView> imageViewWeakReference;
    private final WeakReference<ProgressBar> progressBarWeakReference;
    private final WeakReference<Context> contextWeakReference;
    private WeakReference<VPopBXHAdapter.DataViewHolder> holderWeakReference = null;
    public String track_url = null;
    private int width, height;
    private Context context;
    private int position;
    RequestManager glide;
    private int mode;
    SimpleTarget simpleTarget;

    public BitmapTrackWorkerTask(Context c,
                                 ImageView imageView,
                                 int reqWidth,
                                 int reqHeight,
                                 ProgressBar progressBar,
                                 int position,
                                 int mode,
                                 VPopBXHAdapter.DataViewHolder holder) {
        context = c;
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewWeakReference = new WeakReference<>(imageView);
        progressBarWeakReference = new WeakReference<>(progressBar);
        holderWeakReference = new WeakReference<>(holder);
        contextWeakReference = new WeakReference<>(c);
        width = reqWidth;
        height = reqHeight;
        this.position = position;
        this.mode = mode;
    }

    public BitmapTrackWorkerTask(Context c, ImageView imageView, int reqWidth, int reqHeight, ProgressBar progressBar, int position, int mode, RequestManager glide) {
        context = c;
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewWeakReference = new WeakReference<>(imageView);
        progressBarWeakReference = new WeakReference<>(progressBar);
        contextWeakReference = new WeakReference<Context>(c);
        this.glide = glide;
        width = reqWidth;
        height = reqHeight;
        this.position = position;
        this.mode = mode;
    }

    public BitmapTrackWorkerTask(Context c, ImageView imageView, int reqWidth, int reqHeight, ProgressBar progressBar, int position, int mode) {
        context = c;
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewWeakReference = new WeakReference<>(imageView);
        progressBarWeakReference = new WeakReference<>(progressBar);
        contextWeakReference = new WeakReference<Context>(c);
        width = reqWidth;
        height = reqHeight;
        this.position = position;
        this.mode = mode;
    }

    // Decode image in background.
    @Override
    protected String doInBackground(String... params) {
        track_url = params[0];
//        Log.d("testNetwork", track_url + "");
        if (track_url != null) {
            try {
                Document document = Jsoup.connect(track_url).userAgent("Chrome/59.0.3071.115").get();

                String detail_csn_art_url = document.select("meta[property=og:image]").get(0).attr("content");
//                Log.d("detail_csn_art_url", detail_csn_art_url + "");
//                Log.d("detail_csn_art_url", mode + " " + position);

                Elements table = document.select("table");
                if (position >= 0)
                    if (table != null && table.size() >= 0) {
                        for (Element e : table) {
                            Elements tr = e.select("tr");
                            if (tr != null && tr.size() == 26) {
                                Element download_element = tr.get(position + 1);
                                String download_url =
                                        download_element
                                                .select("a")
                                                .first()
                                                .attr("href");


                                HashMap<Integer, ArrayList<Track>> hashMap = new HashMap<>();
                                hashMap.put(6, MusicResource.VPop_BXH_Refresh);
                                hashMap.put(7, MusicResource.VPop_MCS_Refresh);
                                hashMap.put(8, MusicResource.VPop_MDL_Refresh);
                                hashMap.put(9, MusicResource.KPop_BXH_Refresh);
                                hashMap.put(10, MusicResource.KPop_MCS_Refresh);
                                hashMap.put(11, MusicResource.KPop_MDL_Refresh);
                                hashMap.put(12, MusicResource.JPop_BXH_Refresh);
                                hashMap.put(13, MusicResource.JPop_MCS_Refresh);
                                hashMap.put(14, MusicResource.JPop_MDL_Refresh);
                                hashMap.put(15, MusicResource.CPop_BXH_Refresh);
                                hashMap.put(16, MusicResource.CPop_MCS_Refresh);
                                hashMap.put(17, MusicResource.CPop_MDL_Refresh);
                                hashMap.put(18, MusicResource.USK_BXH_Refresh);
                                hashMap.put(19, MusicResource.USK_MCS_Refresh);
                                hashMap.put(20, MusicResource.USK_MDL_Refresh);
                                hashMap.put(21, MusicResource.Other_BXH_Refresh);
                                hashMap.put(22, MusicResource.Other_MCS_Refresh);
                                hashMap.put(23, MusicResource.Other_MDL_Refresh);
                                hashMap.put(24, MusicResource.songListOnlineSearch);
                                hashMap.put(25, MusicResource.hashMapPlaylistOnline.get(MusicResource.playListSelectedTitle));
                                hashMap.put(26, MusicResource.listSongPlaylistShare);
                                if (mode >= 6 && mode != 30) {
                                    hashMap.get(mode).get(position).setDownload_url(download_url);
                                }
                            }
                        }
                    }
                return detail_csn_art_url;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String art_src) {

//        Log.d("testNetwork", "AsyncTask Finish onPostExecute!");
//        Log.d("testNetwork", imageViewWeakReference + "");
//        Log.d("testNetwork art_src", art_src + "");
//        Log.d("testNetwork", progressBarWeakReference + "");
        if (imageViewWeakReference != null && art_src != null) {
//            Log.d("testNetwork", "AsyncTask Finish! Done");

            final ImageView imageView = imageViewWeakReference.get();
            if (imageView != null && progressBarWeakReference != null) {
                final ProgressBar progressBar = progressBarWeakReference.get();

                simpleTarget = new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        Bitmap drawBitmap = ThumbnailUtils.extractThumbnail(resource,
                                (resource.getWidth() >= resource.getHeight() ? resource.getHeight() : resource.getWidth()),
                                (resource.getWidth() >= resource.getHeight() ? resource.getHeight() : resource.getWidth()));
                        Bitmap bitmap;
                        if (resource.getWidth() != resource.getHeight())
                            bitmap = ImageHelper.getRoundedCornerBitmap(drawBitmap, 20);
                        else
                            bitmap = ImageHelper.getRoundedCornerBitmap(drawBitmap, 30);
                        MusicResource.addBitmapToMemCache(String.valueOf(track_url), bitmap);
                        if (imageView.getDrawable() != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                if (imageView.getDrawable().getAlpha() <= 0.5) {
                                    imageView.setImageBitmap(bitmap); // Possibly runOnUiThread()
                                }
                            } else imageView.setImageBitmap(bitmap);
                        } else imageView.setImageBitmap(bitmap);
                        //imageView.setImageBitmap(bitmap); // Possibly runOnUiThread()
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                };
                Context context1 = contextWeakReference.get();
                if (glide != null)
                    glide
                            .load(art_src)
                            .asBitmap()
                            .into(simpleTarget);
                else Glide.with(context1).load(art_src)
                        .asBitmap()
                        .into(simpleTarget);
            }
        }
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
}
