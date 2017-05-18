package me.thenightmancodeth.cirkit2.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val stringMsg = findViewById(R.id.pushET) as EditText
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            cirkit.sendStringPush(stringMsg.getText().toString())
        }

        val filePicker = findViewById(R.id.filePicker) as ImageButton
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
                cirkit.sendImagePush(file)
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
