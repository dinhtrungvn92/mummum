package com.kenung.vn.prettymusic.music_offline;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.music_offline.album.AlbumFragment;
import com.kenung.vn.prettymusic.music_offline.downloaded.DownLoadedFragment;
import com.kenung.vn.prettymusic.music_offline.folder.FolderFragment;
import com.kenung.vn.prettymusic.music_offline.song.SongFragment;
import com.kenung.vn.prettymusic.music_offline.playlist.PlaylistOfflineFragment;

/**
 * Created by sev_user on 23-Dec-16.
 */
public class MusicPagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_ITEMS = 5;

    public MusicPagerAdapter(FragmentManager fragmentManager) {
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
                return AlbumFragment.newInstance();
            case 1:
                return SongFragment.newInstance();
            case 2:
                return PlaylistOfflineFragment.newInstance();
            case 3:
                return FolderFragment.newInstance();
            case 4:
                return new DownLoadedFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = MusicResource.album;
                break;
            case 1:
                title = MusicResource.track;
                break;
            case 2:
                title = MusicResource.playlist;
                break;
            case 3:
                title = MusicResource.folder;
                break;
            case 4:
                title = MusicResource.downloaded;
                break;
            default:
        }
        return title;
    }
}
