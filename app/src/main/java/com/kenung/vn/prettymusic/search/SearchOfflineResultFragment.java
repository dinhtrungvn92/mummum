package com.kenung.vn.prettymusic.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.search.offline_search.Offline_Search_Adapter;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

/**
 * Created by sev_user on 12-Apr-17.
 */

public class SearchOfflineResultFragment extends Fragment {

    private Offline_Search_Adapter mAdapter;
    //    private StickyListHeadersListView stickyList;
    private Fragment albumDetaiFragment = null;
    ImageView search_main_background;
    SmartTabLayout viewPagerTab;
    ViewPager vPager;
    FragmentStatePagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_search_offline_result, container, false);
        search_main_background = (ImageView) getActivity().findViewById(R.id.search_main_background);

        viewPagerTab = (SmartTabLayout) view.findViewById(R.id.viewpagertab);
        adapter = new SearchofflinePagerAdapter(getFragmentManager());
        vPager = (ViewPager) view.findViewById(R.id.vPager);
        vPager.setAdapter(adapter);
        vPager.setOffscreenPageLimit(2);

        viewPagerTab.setViewPager(vPager);
        viewPagerTab.setSelectedIndicatorColors(Color.rgb(132, 78, 173));

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
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
        return BitmapFactory.decodeFile(art_uri, options);
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

    private void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }
}