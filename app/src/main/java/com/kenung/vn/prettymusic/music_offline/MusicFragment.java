package com.kenung.vn.prettymusic.music_offline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

/**
 * Created by sev_user on 22-Dec-16.
 */
public class MusicFragment extends Fragment {
    FragmentPagerAdapter adapter;
    SmartTabLayout viewPagerTab;
    ViewPager vPager;

    public static MusicFragment newInstance() {
        MusicFragment fragment = new MusicFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demo_smartablayout, container, false);

        viewPagerTab = (SmartTabLayout) view.findViewById(R.id.viewpagertab);
        adapter = new MusicPagerAdapter(getFragmentManager());
        vPager = (ViewPager) view.findViewById(R.id.vPager);
        vPager.setAdapter(adapter);
        vPager.setOffscreenPageLimit(5);
        viewPagerTab.setViewPager(vPager);

        viewPagerTab.setSelectedIndicatorColors(Color.rgb(132, 78, 173));
        vPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                MusicResource.pageOfflinePosition = position;
                getActivity().sendBroadcast(new Intent("ChangeOfflinePage"));
            }
        });
        vPager.setCurrentItem(1, true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("noti_change_album_track");
        getActivity().registerReceiver(intentFilterReceiver, intentFilter);
    }

    BroadcastReceiver intentFilterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            vPager.setCurrentItem(MusicResource.album_track_toogle, true);
        }
    };


}
