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
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.text.Html
import android.util.Log
import android.widget.Toast
import io.realm.Realm
import me.thenightmancodeth.cirkit2.R
import me.thenightmancodeth.cirkit2.model.Push
import me.thenightmancodeth.cirkit2.model.RealmPush
import me.thenightmancodeth.cirkit2.network.Cirkit
import me.thenightmancodeth.cirkit2.network.CirkitServer
import me.thenightmancodeth.cirkit2.view.MainActivity
import java.io.IOException
import java.net.BindException

/**
 * Created by TheNightman on 5/22/17.
 */

class CirkitService : Service() {
    lateinit var server: CirkitServer
    var notificationManager: NotificationManagerCompat? = null
    var pendingPushes: Int = 0
    var style: NotificationCompat.InboxStyle = NotificationCompat.InboxStyle()
    val NEW_PUSH_NOTI: Int = 3917

    interface OnPushReceivedLisener {
        fun onPushReceived(push: Push)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("Cirkit_service", "Received start id $startId: $intent")
        notificationManager = NotificationManagerCompat.from(this@CirkitService)
        try {
            //Start the NanoHTTPD server
            server = CirkitServer(object: OnPushReceivedLisener {
                override fun onPushReceived(push: Push) {
                    var message :String?
                    if (push.stringMessage == "" || push.stringMessage == null) {
                        message = push.filePath
                    } else message = push.stringMessage
                    println("Push received: $message from: ${push.device}")
                    pendingPushes++
                    val onNotiClick: PendingIntent = PendingIntent
                            .getActivity(this@CirkitService, 0,
                                    Intent(this@CirkitService, MainActivity::class.java), 0)
                    val alarmSound: Uri = RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    style.setBigContentTitle(getString(R.string.app_name))
                    style.addLine("${push.device}: $message")
                    style.setSummaryText(Html.fromHtml("<b>${push.device}</b>: ${message}"))

                    //Bind notification dismiss receiver
                    val receiverIntent: Intent = Intent(this@CirkitService, NotificationDismissReceiver::class.java)
                    receiverIntent.putExtra("me.thenightmancodeth.cirkit2.4200", 4200)
                    val pendingIntent: PendingIntent = PendingIntent.getBroadcast(applicationContext, 4200, receiverIntent,0)

                    //Create notification
                    val noti: Notification = NotificationCompat.Builder(this@CirkitService)
                            .setSmallIcon(R.drawable.ic_noti)
                            .setStyle(style)
                            .setGroupSummary(true)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(Html.fromHtml("<b>${push.device}</b>: ${message}"))
                            .setNumber(pendingPushes)
                            .setSound(alarmSound)
                            .setDeleteIntent(pendingIntent)
                            .setLights(Color.CYAN, 3000, 3000)
                            .setVibrate(longArrayOf(1000, 1000))
                            .setContentIntent(onNotiClick)
                            .build()
                    notificationManager?.notify(NEW_PUSH_NOTI, noti)

                    val realm: Realm = Realm.getDefaultInstance()
                    realm.beginTransaction()
                    var toRealm: RealmPush = realm.createObject(RealmPush::class.java)
                    toRealm.device = push.device
                    if (push.stringMessage == null || push.stringMessage == "") {
                        toRealm.filePath = push.filePath
                    } else toRealm.stringMessage = push.stringMessage
                    realm.commitTransaction()
                }
            }, applicationContext)
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
        notificationManager?.cancel(3918)
        Toast.makeText(this, "Cirkit service stopped", Toast.LENGTH_SHORT).show()
    }

    fun resetPendingPushes() {
        pendingPushes = 0
        style = NotificationCompat.InboxStyle()
        notificationManager?.cancel(NEW_PUSH_NOTI)
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }
}
