package com.kenung.vn.prettymusic.music_online.j_pop;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.music_online.j_pop.bxh.JPopBXHFragment;
import com.kenung.vn.prettymusic.music_online.j_pop.moichiase.JPopMCSFragment;
import com.kenung.vn.prettymusic.music_online.j_pop.moidownload.JPopMDLFragment;

/**
 * Created by KXPRO on 5/11/2017.
 */

public class JPopPagerAdapter extends FragmentStatePagerAdapter {

    private static final int NUM_ITEMS = 3;

    public JPopPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return JPopBXHFragment.newInstance();
            case 1:
                return JPopMCSFragment.newInstance();
            case 2:
                return JPopMDLFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = MusicResource.chart;
                break;
            case 1:
                title = MusicResource.new_upload;
                break;
            case 2:
                title = MusicResource.new_download;
                break;
            default:
                break;
        }
        return title;
    }
}