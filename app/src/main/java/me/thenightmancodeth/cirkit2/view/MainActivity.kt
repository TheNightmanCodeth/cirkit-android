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
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.aditya.filebrowser.Constants
import com.aditya.filebrowser.FileChooser
import io.realm.Realm
import me.thenightmancodeth.cirkit2.R
import me.thenightmancodeth.cirkit2.network.Cirkit
import me.thenightmancodeth.cirkit2.service.CirkitService
import java.io.File
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import me.thenightmancodeth.cirkit2.model.RealmPush

class MainActivity : AppCompatActivity() {
    val PICK_FILE_REQUEST = 595
    val cirkit = Cirkit()
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("Cirkit", Context.MODE_PRIVATE)

        if (prefs.getBoolean("first_launch", true)) {
            val intro: Intent = Intent(this@MainActivity, IntroActivity::class.java)
            startActivity(intro)
            val e: SharedPreferences.Editor = prefs.edit()
            e.putBoolean("first_launch", false)
            e.apply()
        }

        Realm.init(this@MainActivity)

        fab.setOnClickListener {
            cirkit.sendStringPush(stringMsg.text.toString())
        }

        devicePicker.setOnClickListener {
            TODO("Implement device picker")
        }

        filePicker.setOnClickListener {
            val picker = Intent(applicationContext, FileChooser::class.java)
            picker.putExtra(Constants.SELECTION_MODE, Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal)
            startActivityForResult(picker, PICK_FILE_REQUEST)
        }

        pushRecycler()

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
                fab.setOnClickListener{
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

    override fun onResume() {
        super.onResume()
        CirkitService().resetPendingPushes()
    }

    fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        manager.getRunningServices(Int.MAX_VALUE).forEach { service ->
            if (CirkitService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun pushRecycler() {
        pushRecycler.layoutManager = LinearLayoutManager(this)

        val adapter: RealmRVAdapter = RealmRVAdapter(Realm.getDefaultInstance()
                .where(RealmPush::class.java).findAllAsync(), { msg: String ->
            val cb: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE)
                    as ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.app_name), msg)

            cb.primaryClip = clip
            Snackbar.make(coordinator, "Copied push to clipboard", Snackbar.LENGTH_SHORT).show()
        }, {push, adapter ->
            val r: Realm = Realm.getDefaultInstance()
            r.transaction { push.deleteFromRealm() }
            adapter.notifyDataSetChanged()
            Snackbar.make(coordinator, "Push removed", Snackbar.LENGTH_LONG).setAction("UNDO",
                    {_ ->
                        try {
                            r.transaction {
                                val copy = createObject(RealmPush::class.java)
                                copy.device = push.device
                                copy.filePath = push.filePath
                                copy.stringMessage = push.stringMessage
                            }
                            adapter.notifyDataSetChanged()
                        } catch (e: Exception) {
                            Log.e("Cirkit2", e.toString())
                        }
                    }).show()
        })

        pushRecycler.adapter = adapter
        pushRecycler.setHasFixedSize(true)
    }
    inline fun Realm.transaction(body: Realm.() -> Unit) {
        beginTransaction()
        body(this)
        commitTransaction()
    }
}
