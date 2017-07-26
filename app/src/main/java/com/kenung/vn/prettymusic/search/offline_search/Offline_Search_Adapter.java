package com.kenung.vn.prettymusic.search.offline_search;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.kenung.vn.prettymusic.BitmapWorkerTask;
import com.kenung.vn.prettymusic.MusicResource;
import com.kenung.vn.prettymusic.R;
import com.kenung.vn.prettymusic.search.SearchResultForm;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by sev_user on 12-Apr-17.
 */

public class Offline_Search_Adapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {

    private final Context context;
    private String[] mCountries;
    private ArrayList<SearchResultForm> searchResultList;
    private int[] mSectionIndices;
    private String[] mSectionLetters;
    private LayoutInflater mInflater;

    public Offline_Search_Adapter(Context context, ArrayList<SearchResultForm> searchResult) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        //mCountries = context.getResources().getStringArray(R.array.countries);
        searchResultList = searchResult;
        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
    }

    private int[] getSectionIndices() {
        ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
        //char lastFirstChar = mCountries[0].charAt(0);
        if (searchResultList == null) return null;
        if (searchResultList != null && searchResultList.size() <= 0) return null;
        String fistSection = searchResultList.get(0).getHeader();
        sectionIndices.add(0);
        /*for (int i = 1; i < mCountries.length; i++) {
            if (mCountries[i].charAt(0) != lastFirstChar) {
                lastFirstChar = mCountries[i].charAt(0);
                sectionIndices.add(i);
            }
        }*/

        for (int i = 1; i < searchResultList.size(); i++) {
            if (!searchResultList.get(i).getHeader().equals(fistSection)) {
                fistSection = searchResultList.get(i).getHeader();
                sectionIndices.add(i);
            }
        }

        int[] sections = new int[sectionIndices.size()];
        for (int i = 0; i < sectionIndices.size(); i++) {
            sections[i] = sectionIndices.get(i);
        }
        return sections;
    }

    private String[] getSectionLetters() {
        //Character[] letters = new Character[mSectionIndices.length];
        if (mSectionIndices == null) return null;
        String[] letters = new String[mSectionIndices.length];
        for (int i = 0; i < mSectionIndices.length; i++) {
            //letters[i] = mCountries[mSectionIndices[i]].charAt(0);
            letters[i] = searchResultList.get(mSectionIndices[i]).getHeader();
        }
        return letters;
    }

    @Override
    public int getCount() {
        return searchResultList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchResultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.songlistliew, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.song_title);
            holder.artist = (TextView) convertView.findViewById(R.id.song_artist);
            holder.title.setSelected(true);
            holder.artist.setSelected(true);
            holder.art = (ImageView) convertView.findViewById(R.id.songArt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(searchResultList.get(position).getTitle());
        holder.artist.setText(searchResultList.get(position).getArtist());

        if (searchResultList.get(position).getArt_uri() != null) {
            loadBitmap(searchResultList.get(position).getArt_uri(), holder.art);
        } else {
            loadBitmap(null, holder.art);
        }

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        // set header text as first char in name
        String headerChar = searchResultList.get(position).getHeader();
        holder.text.setText(headerChar);

        return convertView;
    }

    /**
     * Remember that these have to be static, postion=1 should always return
     * the same Id that is.
     */
    @Override
    public long getHeaderId(int position) {
        // return the first character of the country as ID because this is what
        // headers are based upon
        return searchResultList.get(position).getCategory();
    }

    @Override
    public int getPositionForSection(int section) {
        if (mSectionIndices.length == 0) {
            return 0;
        }

        if (section >= mSectionIndices.length) {
            section = mSectionIndices.length - 1;
        } else if (section < 0) {
            section = 0;
        }
        return mSectionIndices[section];
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < mSectionIndices.length; i++) {
            if (position < mSectionIndices[i]) {
                return i - 1;
            }
        }
        return mSectionIndices.length - 1;
    }

    @Override
    public Object[] getSections() {
        return mSectionLetters;
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView title;
        TextView artist;
        ImageView art;
    }

    public void loadBitmap(String art_uri, ImageView imageView) {

        final String imageKey = String.valueOf(art_uri);

        final Bitmap bitmap = MusicResource.getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_music_note_black_48dp);
            final BitmapWorkerTask task = new BitmapWorkerTask(context, imageView, 96, 96);
            //task.execute(art_uri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, art_uri);
            } else {
                task.execute(art_uri);
            }
        }
    }

}