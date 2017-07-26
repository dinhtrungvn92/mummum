package com.kenung.vn.prettymusic;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.kenung.vn.prettymusic.module.ImageHelper;

import java.lang.ref.WeakReference;

/**
 * Created by sev_user on 23-Mar-17.
 */

public class BitmapWorkerTaskForArt extends AsyncTask<String, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewWeakReference;
    private String art_uri = null;
    private int width, height;
    private Context context;


    public BitmapWorkerTaskForArt(Context c, ImageView imageView, int reqWidth, int reqHeight) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewWeakReference = new WeakReference<>(imageView);
        width = reqWidth;
        height = reqHeight;
        context = c;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        final Bitmap bitmap;
        art_uri = params[0];
        if (art_uri != null) {

            bitmap = decodeSampledBitmapFromUri(art_uri, width, height);
            if (bitmap != null) MusicResource.addBitmapToMemCache(String.valueOf(art_uri), bitmap);
            return bitmap;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewWeakReference != null && bitmap != null) {
            final ImageView imageView = imageViewWeakReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
                MusicResource.subArt = bitmap;
                context.sendBroadcast(new Intent("noti_loadBitmapLocal_done"));
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromUri(String art_uri, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(art_uri, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeFile(art_uri, options), 15);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
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
