package com.kenung.vn.prettymusic.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.search.offline_search.album.SearchOfflineAlbumFragment;
import com.kenung.vn.prettymusic.search.offline_search.song.SearchOfflineSongFragment;

/**
 * Created by sev_user on 12-Apr-17.
 */

public class SearchofflinePagerAdapter extends FragmentStatePagerAdapter {

    private static final int NUM_ITEMS = 2;

    public SearchofflinePagerAdapter(FragmentManager fragmentManager) {
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
                return new SearchOfflineSongFragment();
            case 1:
                return new SearchOfflineAlbumFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = MusicResource.track;
                break;
            case 1:
                title = MusicResource.album;
                break;
            default:
        }
        return title;
    }
}
