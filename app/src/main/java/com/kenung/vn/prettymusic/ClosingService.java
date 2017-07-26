package com.kenung.vn.prettymusic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Administrator on 19/05/2017.
 */

public class ClosingService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        // Handle application closing
        if (MusicResource.notification_id != 0)
            MusicResource.notificationManager.cancel(MusicResource.notification_id);

        // Destroy the service
        stopSelf();
    }
}