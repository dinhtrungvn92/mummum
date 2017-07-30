package com.kenung.vn.prettymusic;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sev_user on 05-May-17.
 */

public class ListAlertDialogAdapter extends BaseAdapter {

    private LayoutInflater alertInf;
    private Context context;
    private ArrayList<String> listAlert;
    private TextView quality;
    private TextView bitrate;
    private TextView size;
    private int color;

    public ListAlertDialogAdapter(Context context, ArrayList<String> listAlert) {
        alertInf = LayoutInflater.from(context);
        this.listAlert = listAlert;
        this.context = context;
    }

    @Override
    public int getCount() {
        return listAlert.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout = (LinearLayout) alertInf.inflate(R.layout.alert, parent, false);
        Log.d("testDownload", listAlert.get(position) +"");
        quality = (TextView) layout.findViewById(R.id.quality);
        bitrate = (TextView) layout.findViewById(R.id.bitrate);
        size = (TextView) layout.findViewById(R.id.size);
        quality.setText(listAlert.get(position).split(" ")[0]);
        bitrate.setText(listAlert.get(position).split(" ")[1]);
        size.setText(listAlert.get(position).split(" ")[2] +
                listAlert.get(position).split(" ")[3]);

        if (listAlert.get(position).split(" ")[1].toLowerCase().contains("lossless")) {
            color = Color.RED;
        } else if (listAlert.get(position).split(" ")[1].toLowerCase().contains("500")) {
            color = Color.rgb(255, 202, 130);
        } else if (listAlert.get(position).split(" ")[1].toLowerCase().contains("320")) {
            color = Color.rgb(150, 223, 234);
        } else {
            color = Color.GRAY;
        }

        quality.setTextColor(color);
        bitrate.setTextColor(color);
        size.setTextColor(color);

        return layout;
    }
}
