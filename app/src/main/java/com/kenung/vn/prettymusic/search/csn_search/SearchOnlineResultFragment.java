package com.kenung.vn.prettymusic.search.csn_search;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

/**
 * Created by Administrator on 14/05/2017.
 */

public class SearchOnlineResultFragment extends Fragment {
    FragmentPagerAdapter adapter;
    TabLayout tabLayout;
    ViewPager vPager;
    LinearLayout layoutResult;
    ProgressBar progressBar;
    SmartTabLayout viewPagerTab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_search_online_result, container, false);
        layoutResult = (LinearLayout) view.findViewById(R.id.layoutResult);
        layoutResult.setVisibility(View.INVISIBLE);
        if (MusicResource.CSNSearchSongResultTemp != null) {
            if (MusicResource.CSNSearchSongResultTemp.size() > 0)
                layoutResult.setVisibility(View.VISIBLE);
        }

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        viewPagerTab = (SmartTabLayout) view.findViewById(R.id.viewpagertab);
        adapter = new SearchOnlinePagerAdapter(getFragmentManager());
        vPager = (ViewPager) view.findViewById(R.id.vPager);
        vPager.setAdapter(adapter);
        vPager.setOffscreenPageLimit(3);
        viewPagerTab.setViewPager(vPager);
        viewPagerTab.setSelectedIndicatorColors(Color.rgb(132, 78, 173));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilterCSNSearchDone = new IntentFilter("noti_csn_search_done");
        IntentFilter intentFilterCSNSearchStart = new IntentFilter(("noti_search_online_query_submit"));
        getActivity().registerReceiver(receiverCSNSearchDone, intentFilterCSNSearchDone);
        getActivity().registerReceiver(receiverCSNSearchStart, intentFilterCSNSearchStart);
    }

    private BroadcastReceiver receiverCSNSearchDone
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            layoutResult.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    };
    private BroadcastReceiver receiverCSNSearchStart
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.VISIBLE);
        }
    };
}
