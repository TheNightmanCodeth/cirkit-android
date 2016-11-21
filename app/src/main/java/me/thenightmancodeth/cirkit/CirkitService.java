package me.thenightmancodeth.cirkit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by andrewdiragi on 11/21/16.
 */

public class CirkitService extends Service {
    CirkitServer server;
    private final IBinder binder = new LocalBinder();
    private NotificationManagerCompat nm;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    WifiManager.WifiLock wifiLock;
    private final int NOTIFICATION = 4200;
    private int NEW_PUSH_NOT = 6960;
    private final String PUSHES_GROUP = "group_pushes";

    public class LocalBinder extends Binder {
        CirkitService getService() {
            return this.getService();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Cirkit_Service", "Received start id " +startId +": " +intent);
        //Start cirkit server
        try {
            server = new CirkitServer(new MainActivity.OnPushReceivedListener() {
                @Override
                public void onPushRec(String push) {
                    final PendingIntent onNotiClick = PendingIntent
                            .getActivity(getApplicationContext(), 0,
                                    new Intent(getApplicationContext(), MainActivity.class), 0);

                    nm = NotificationManagerCompat.from(getApplicationContext());
                    Notification noti = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_noti)
                            .setTicker(push)
                            .setContentTitle("Push received")
                            .setContentText(push)
                            .setGroup(PUSHES_GROUP)
                            .setContentIntent(onNotiClick)
                            .build();

                    nm.notify(NEW_PUSH_NOT++, noti);
                    Log.e("PUSH RECEIVED", push);
                }
            });
            server.start();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        //Keep CPU awake
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Cirkit");
        wakeLock.acquire();
        //Keep wifi awake
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Cirkit");
        wifiLock.acquire();
        //Become foreground service
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        Notification cirkitNoti = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_noti)
                .setTicker("Cirkit")
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Cirkit")
                .setContentText("Cirkit is running in the background")
                .setContentIntent(pi)
                .build();
        cirkitNoti.flags|=Notification.FLAG_NO_CLEAR;
        startForeground(NOTIFICATION, cirkitNoti);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        wakeLock.release();
        wifiLock.release();
        server.stop();
        nm.cancel(NOTIFICATION);
        Toast.makeText(this, "Cirkit service stopped...", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
