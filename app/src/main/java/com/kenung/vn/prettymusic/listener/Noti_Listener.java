package com.kenung.vn.prettymusic.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kenung.vn.prettymusic.MusicResource;

/**
 * Created by sev_user on 17-Mar-17.
 */

public class Noti_Listener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int noti_id = intent.getExtras().getInt("id");

        switch (noti_id) {
            case 1: {
                context.sendBroadcast(new Intent("noti_play_intent_action"));
            }
            break;
            case 2: {
                context.sendBroadcast(new Intent("noti_next_intent_action"));
            }
            break;
            case 3: {
                context.sendBroadcast(new Intent("noti_back_intent_action"));
            }
            break;
            case 4: {
                context.sendBroadcast(new Intent("noti_clear_intent_action"));
            }
            break;
            default:
                break;
        }
    }
}
