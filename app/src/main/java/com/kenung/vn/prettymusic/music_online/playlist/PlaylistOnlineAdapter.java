package com.kenung.vn.prettymusic.music_online.playlist;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.music_online.other.bxh.OtherBXHFragment;
import com.kenung.vn.prettymusic.music_online.other.moichiase.OtherMCSFragment;
import com.kenung.vn.prettymusic.music_online.other.moidownload.OtherMDLFragment;

/**
 * Created by Administrator on 16/06/2017.
 */

public class PlaylistOnlineAdapter extends FragmentStatePagerAdapter {
    private static final int NUM_ITEMS = 2;

    public PlaylistOnlineAdapter(FragmentManager fragmentManager) {
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
                return new MyPlaylistFragment();
            case 1:
                return new SharePlaylistFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = "My Playlist";
                break;
            case 1:
                title = "Share Playlist";
                break;
            default:
                break;
        }
        return title;
    }
}
