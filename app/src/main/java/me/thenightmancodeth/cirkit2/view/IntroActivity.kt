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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntro2Fragment
import me.thenightmancodeth.cirkit2.R

/**
 * Created by TheNightman on 5/24/17.
 */

class IntroActivity : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //First slide - Welcome to Cirkit - Your cirkit consists of all registered devices on your network
        addSlide(AppIntro2Fragment.newInstance("Welcome to Cirkit", "Your cirkit consists of all registered devices on your local network", R.drawable.logo, ContextCompat.getColor(applicationContext, R.color.colorPrimary)))
        //Second slide - Messager - Send and receive text messages to and from other devices on your cirkit
        addSlide(AppIntro2Fragment.newInstance("Messenger", "Send and receive text messages and links to and from other devices on your cirkit", R.drawable.ic_messages, ContextCompat.getColor(applicationContext, R.color.colorPrimary)))
        //Third slide - Files - Send and receive files to and from other devices on your cirkit
        addSlide(AppIntro2Fragment.newInstance("Files", "Send and receive files to and from other devices on your cirkit. Cirkit will need permission to write to your device storage for this feature.", R.drawable.ic_files, ContextCompat.getColor(applicationContext, R.color.colorPrimary)))

        //Don't show skip
        showSkipButton(false)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        //Check for permissions
        val permissionCheck: Int = ContextCompat.checkSelfPermission(this@IntroActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        println(permissionCheck)
        println(PackageManager.PERMISSION_GRANTED)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@IntroActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    690)
        }
        finish()
    }
}