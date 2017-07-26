package com.kenung.vn.prettymusic.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.kenung.vn.prettymusic.MusicResource;

/**
 * Created by Administrator on 17/06/2017.
 */

public class NotificationDismissedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TestAnim", "ClearNotify");
        MusicResource.clearNoti = true;
    }
}