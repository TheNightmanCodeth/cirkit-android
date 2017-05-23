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

package me.thenightmancodeth.cirkit2.view

import android.app.Activity
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import com.aditya.filebrowser.Constants
import com.aditya.filebrowser.FileChooser
import me.thenightmancodeth.cirkit2.R
import me.thenightmancodeth.cirkit2.network.Cirkit
import me.thenightmancodeth.cirkit2.network.CirkitServer
import me.thenightmancodeth.cirkit2.service.CirkitService
import java.io.File

class MainActivity : AppCompatActivity() {
    val PICK_FILE_REQUEST = 595
    val cirkit = Cirkit()
    lateinit var filePicker: ImageButton
    lateinit var devicePicker: ImageButton
    lateinit var stringMsg: EditText
    lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stringMsg = findViewById(R.id.pushET) as EditText
        fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            cirkit.sendStringPush(stringMsg.getText().toString())
        }

        devicePicker = findViewById(R.id.devicePicker) as ImageButton
        devicePicker.setOnClickListener { view ->

        }

        filePicker = findViewById(R.id.filePicker) as ImageButton
        filePicker.setOnClickListener { view ->
            val picker = Intent(applicationContext, FileChooser::class.java)
            picker.putExtra(Constants.SELECTION_MODE, Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal)
            startActivityForResult(picker, PICK_FILE_REQUEST)
        }


        if (!isServiceRunning()) println("starting service"); startService()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FILE_REQUEST && data != null) {
            if (resultCode == Activity.RESULT_OK) {
                val uri: Uri = data.data
                val file: File = File(uri.path)
                filePicker.setImageDrawable(getDrawable(R.drawable.ic_folder_closed))
                stringMsg.hint = file.name
                stringMsg.isEnabled = false
                fab.setOnClickListener{ view ->
                    cirkit.sendFilePush(file)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun startService() {
        val cirkitService: Intent = Intent(this@MainActivity, CirkitService::class.java)
        val pend: PendingIntent = PendingIntent.getService(this@MainActivity, 0, cirkitService, 0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_HALF_DAY,
                pend)
    }

    fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        manager.getRunningServices(Int.MAX_VALUE).forEach { service ->
            println(service.service.className)
            if (CirkitService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
