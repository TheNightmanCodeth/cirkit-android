package me.thenightmancodeth.cirkit.backend.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by joe on 12/4/16.
 */

public class NotificationDismissReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("receiver", "received");
        CirkitService.resetPendingPushes();
    }
}
