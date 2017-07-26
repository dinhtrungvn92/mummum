package com.kenung.vn.prettymusic.search;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kenung.vn.prettymusic.R;

/**
 * Created by sev_user on 12-Apr-17.
 */

public class SearchFragment extends Fragment {
    private FragmentStatePagerAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager vPager;

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
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
        View view = inflater.inflate(R.layout.search_view, container, false);

        tabLayout = (TabLayout) view.findViewById(R.id.search_tablayout);
        adapter = new SearchofflinePagerAdapter(getChildFragmentManager());
        vPager = (ViewPager) view.findViewById(R.id.search_vPager);
        vPager.setAdapter(adapter);
        vPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(vPager);
        vPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setTabsFromPagerAdapter(adapter);
        tabLayout.setTabTextColors(Color.WHITE, Color.rgb(176, 182, 12));
        tabLayout.setSelectedTabIndicatorColor(Color.rgb(176, 182, 12));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.isSelected();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }
}