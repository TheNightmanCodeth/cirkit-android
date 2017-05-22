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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import java.io.File

class MainActivity : AppCompatActivity() {
    val PICK_FILE_REQUEST = 595
    val cirkit = Cirkit()
    lateinit var filePicker: ImageButton
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

        filePicker = findViewById(R.id.filePicker) as ImageButton
        filePicker.setOnClickListener { view ->
            val picker = Intent(applicationContext, FileChooser::class.java)
            picker.putExtra(Constants.SELECTION_MODE, Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal)
            startActivityForResult(picker, PICK_FILE_REQUEST)
        }
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
}
