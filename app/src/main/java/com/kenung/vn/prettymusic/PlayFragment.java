package com.kenung.vn.prettymusic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.relex.circleindicator.CircleIndicator;

/**
 * Created by KXPRO on 4/8/2017.
 */

public class PlayFragment extends Fragment {
    FragmentPagerAdapter adapter;
    ViewPager vPager;

    public static PlayFragment newInstance() {
        PlayFragment fragment = new PlayFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_fragment, container, false);

        CircleIndicator indicator = (CircleIndicator) view.findViewById(R.id.play_indicator);
        adapter = new PlayPagerAdapter(getContext(), getFragmentManager());
        vPager = (ViewPager) view.findViewById(R.id.play_vPager);
        vPager.setAdapter(adapter);
        vPager.setOffscreenPageLimit(2);
        vPager.setCurrentItem(1, true);
        indicator.setViewPager(vPager);
        adapter.registerDataSetObserver(indicator.getDataSetObserver());

        return view;
    }
}