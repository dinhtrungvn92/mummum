package com.kenung.vn.prettymusic;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;


/**
 * Created by KXPRO on 4/8/2017.
 */

public class PlayPagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_ITEMS = 3;
    private Context context;

    public PlayPagerAdapter(Context c, FragmentManager fragmentManager) {
        super(fragmentManager);

        context = c;
    }

    @Override
    public int getCount() {
        return MusicResource.Number_Item_Play_Activity;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                context.sendBroadcast(new Intent("noti_playcontent_focus"));
                Log.d("TestPlayContentlv", "PlayContent switch");
                return PlayContentFragment.newInstance();
            }
            case 1:
                return PlayArtFragment.newInstance();
            case 2:
                return new PlayLyricFragment();
            default:
                return null;
        }
    }
}