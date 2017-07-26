package com.kenung.vn.prettymusic.search.csn_search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.search.csn_search.album.SearchOnlineResultAlbumFragment;
import com.kenung.vn.prettymusic.search.csn_search.singer.SearchOnlineResultSingerFragment;
import com.kenung.vn.prettymusic.search.csn_search.song.SearchOnlineResultSongFragment;

/**
 * Created by Administrator on 14/05/2017.
 */

public class SearchOnlinePagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_ITEMS = 3;

    public SearchOnlinePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new SearchOnlineResultSongFragment();
                break;
            case 1:
                fragment = new SearchOnlineResultSingerFragment();
                break;
            case 2:
                fragment = new SearchOnlineResultAlbumFragment();
                break;
            default:
                return null;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = MusicResource.track;
                break;
            case 1:
                title = MusicResource.singer;
                break;
            case 2:
                title = MusicResource.album;
                break;
            default:
        }
        return title;
    }
}
