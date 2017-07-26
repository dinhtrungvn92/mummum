package com.kenung.vn.prettymusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by KXPRO on 4/8/2017.
 */

public class PlayLyricFragment extends Fragment {
    TextView lyricView;
    TextView title;
    TextView artist;
    AdView adView;
    AdRequest adRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilterAudioSelectedChange = new IntentFilter("noti_audioselected_change");
        getActivity().registerReceiver(receiverAudioSelectedChange, intentFilterAudioSelectedChange);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private BroadcastReceiver receiverAudioSelectedChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicResource.MODE >= 6 && MusicResource.MODE < 30) {
                String lyric = MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getLyric();
                if (lyric != null)
                    lyricView.setText(
                            "Bài hát : " + MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getTitle() + "\n" +
                                    "Ca sĩ : " + MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getArtist() + "\n" +
                                    "**************************\n" + lyric);
                else lyricView.setText(
                        "Bài hát : " + MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getTitle() + "\n" +
                                "Ca sĩ : " + MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getArtist() + "\n" +
                                "**************************\n" + "Chưa có lời bài hát!");

            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_fragment_lyric, container, false);
        adView = (AdView) view.findViewById(R.id.av_banner);

        lyricView = (TextView) view.findViewById(R.id.lyric);
        String lyric = MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getLyric();
        if (lyric != null)
            lyricView.setText(
                    "Bài hát : " + MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getTitle() + "\n" +
                            "Ca sĩ : " + MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getArtist() + "\n" +
                            "**************************\n" + lyric);
        else lyricView.setText(
                "Bài hát : " + MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getTitle() + "\n" +
                        "Ca sĩ : " + MusicResource.songListOnlinePlay.get(MusicResource.songPosition).getArtist() + "\n" +
                        "**************************\n" + "Chưa có lời bài hát!");
        new Init(adView).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return view;
    }

    public class Init extends AsyncTask<Void, Void, Void> {
        //        private WeakReference<AdView> adViewWeakReference;
        AdView adView;

        public Init(AdView adView) {
//            adViewWeakReference = new WeakReference<AdView>(adView);
            this.adView = adView;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            adRequest = new AdRequest.Builder().build();

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            if (adViewWeakReference != null) {
//                AdView adView = adViewWeakReference.get();
            if (adView != null)
                adView.loadAd(adRequest);
        }
    }

}