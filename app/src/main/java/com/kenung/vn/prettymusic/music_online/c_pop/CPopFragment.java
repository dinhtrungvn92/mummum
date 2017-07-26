package com.kenung.vn.prettymusic.music_online.c_pop;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kenung.vn.prettymusic.R;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

/**
 * Created by KXPRO on 5/11/2017.
 */

public class CPopFragment extends Fragment {
    FragmentStatePagerAdapter adapter;
    SmartTabLayout viewPagerTab;
    ViewPager vPager;

    static CPopFragment fragment = new CPopFragment();

    public static CPopFragment newInstance() {
        return fragment;
    }

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
        View view = inflater.inflate(R.layout.demo_smartablayout, container, false);
        viewPagerTab = (SmartTabLayout) view.findViewById(R.id.viewpagertab);
        vPager = (ViewPager) view.findViewById(R.id.vPager);
        adapter = new CPopPagerAdapter(getChildFragmentManager());
        vPager.setAdapter(adapter);
        vPager.setOffscreenPageLimit(3);

        viewPagerTab.setViewPager(vPager);
        viewPagerTab.setSelectedIndicatorColors(Color.rgb(132, 78, 173));

        return view;
    }


}