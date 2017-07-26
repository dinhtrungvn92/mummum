package com.kenung.vn.prettymusic.music_online.playlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kenung.vn.prettymusic.AlbumDetailActivity;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.music_online.Track;
import com.kenung.vn.prettymusic.music_online.us_uk.USKPagerAdapter;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;

import static com.kenung.vn.prettymusic.R.id.vPager;

/**
 * Created by KXPRO on 5/11/2017.
 */

public class PlaylistOnlineFragment extends Fragment {

    FragmentStatePagerAdapter adapter;
    SmartTabLayout viewPagerTab;
    ViewPager vPager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_online_fragment, container, false);
        viewPagerTab = (SmartTabLayout) view.findViewById(R.id.viewpagertab);
        adapter = new PlaylistOnlineAdapter(getChildFragmentManager());
        vPager = (ViewPager) view.findViewById(R.id.vPager);
        vPager.setAdapter(adapter);
        vPager.setOffscreenPageLimit(2);
        viewPagerTab.setViewPager(vPager);
        viewPagerTab.setSelectedIndicatorColors(Color.rgb(132, 78, 173));

        return view;
    }
}