package me.thenightmancodeth.cirkit.Backend;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import me.thenightmancodeth.cirkit.MainActivity;
import me.thenightmancodeth.cirkit.R;

/***************************************
 * Created by TheNightman on 11/21/16. *
 *                                     *
 * Service that runs CirkitServer in   *
 * the background. Needs to be         *
 * launched every ~12 hours or else    *
 * android will kill it.               *
 ***************************************/

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
                    //Intent to launch when notification is clicked
                    //TODO: add push value to intent flags
                    final PendingIntent onNotiClick = PendingIntent
                            .getActivity(getApplicationContext(), 0,
                                    new Intent(getApplicationContext(), MainActivity.class), 0);
                    //Get notification sound
                    Uri alarmSound = RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    //Create notification
                    nm = NotificationManagerCompat.from(getApplicationContext());
                    Notification noti = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_noti)
                            .setTicker(push)
                            .setContentTitle("Push received")
                            .setContentText(push)
                            .setGroup(PUSHES_GROUP)
                            .setSound(alarmSound)
                            .setLights(Color.CYAN, 3000, 3000)
                            .setVibrate(new long[] {1000,1000})
                            .setContentIntent(onNotiClick)
                            .build();
                    //Show notification
                    nm.notify(NEW_PUSH_NOT++, noti);
                }
            });
            //Starts server
            server.start();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        //Become foreground service
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        //Set persistent notification so service doesn't close
        Notification cirkitNoti = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_noti)
                .setTicker("Cirkit")
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Cirkit")
                .setContentText("Cirkit is running in the background")
                .setContentIntent(pi)
                .build();
        //Make notification persistent
        cirkitNoti.flags|=Notification.FLAG_NO_CLEAR;
        startForeground(NOTIFICATION, cirkitNoti);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        //Release wakelocks
        wakeLock.release();
        wifiLock.release();
        //Stop server
        server.stop();
        //Close pers. notification
        nm.cancel(NOTIFICATION);
        Toast.makeText(this, "Cirkit service stopped...", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
