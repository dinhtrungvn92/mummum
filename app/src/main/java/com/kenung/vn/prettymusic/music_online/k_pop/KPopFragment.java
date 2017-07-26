package com.kenung.vn.prettymusic.music_online.k_pop;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.music_online.Track;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by KXPRO on 5/11/2017.
 */

public class KPopFragment extends Fragment {
    FragmentStatePagerAdapter adapter;
    SmartTabLayout viewPagerTab;
    ViewPager vPager;

    static KPopFragment fragment = new KPopFragment();

    public static KPopFragment newInstance() {
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
        adapter = new KPopPagerAdapter(getChildFragmentManager());
        vPager.setAdapter(adapter);
        vPager.setOffscreenPageLimit(3);

        viewPagerTab.setViewPager(vPager);
        viewPagerTab.setSelectedIndicatorColors(Color.rgb(132, 78, 173));

        if (MusicResource.network_state) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new CSN_KPop_Collect().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        "http://chiasenhac.vn/mp3/korea/");
            } else {
                new CSN_KPop_Collect().execute("http://chiasenhac.vn/mp3/korea/");
            }
        } else {
            Toast.makeText(getActivity(), "No Network Connection!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private class CSN_KPop_Collect extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0]).get();

                if (document == null) return null;

                Element bxh = document.select("div.h-main4").first();

                Elements bangxephang = bxh.getElementsByClass("list-r list-1");
                Log.d("TestKPop", "BXH" + bangxephang.size());
                if (bangxephang == null || bangxephang.size() <= 0) return null;
                for (Element track : bangxephang) {
                    String title = track.select("a").text();
                    String artist = track.getElementsByClass("text2").first().select("p").text();
                    String url = track.select("a").attr("href");
                    String duration = track.getElementsByClass("texte2").first().select("p").first().text();
                    String quality = track.getElementsByClass("texte2").first().select("p").last().text();

                    MusicResource.KPop_BXH.add(new Track(title, artist, url, duration, quality));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Log.d("TestKPop", "onPostExecute");
            getActivity().sendBroadcast(new Intent("noti_load_kpop_done"));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}