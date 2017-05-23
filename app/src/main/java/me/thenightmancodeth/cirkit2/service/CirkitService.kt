/* 
 * CIRKIT - Share messages and files between devices locally
 * Copyright (C) 2017 Joseph Diragi (TheNightman)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.thenightmancodeth.cirkit2.service

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.widget.Toast
import me.thenightmancodeth.cirkit2.R
import me.thenightmancodeth.cirkit2.model.Push
import me.thenightmancodeth.cirkit2.network.CirkitServer
import me.thenightmancodeth.cirkit2.view.MainActivity
import java.io.IOException
import java.net.BindException

/**
 * Created by TheNightman on 5/22/17.
 */

class CirkitService : Service() {
    lateinit var server: CirkitServer
    lateinit var notificationManager: NotificationManagerCompat

    interface OnPushReceivedLisener {
        fun onPushReceived(push: Push)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("Cirkit_service", "Received start id $startId: $intent")
        try {
            //Start the NanoHTTPD server
            server = CirkitServer(object: OnPushReceivedLisener {
                override fun onPushReceived(push: Push) {
                    println("Push received: ${push.stringMessage} from: ${push.device}")
                }
            })
            server.start()
        } catch (be:BindException) {
            be.printStackTrace()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        //Become foreground service
        val pendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java), 0)
        //Send persistent notification
        val cirkitNoti = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_noti)
                .setTicker(getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Cirkit is running in the background")
                .setContentIntent(pendingIntent)
                .build()
        //Make notification persistent
        cirkitNoti.flags = Notification.FLAG_NO_CLEAR
        startForeground(3918, cirkitNoti)
        return START_STICKY
    }

    override fun onDestroy() {
        stopForeground(true)
        server.stop()
        notificationManager.cancel(3918)
        Toast.makeText(this, "Cirkit service stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }
}
