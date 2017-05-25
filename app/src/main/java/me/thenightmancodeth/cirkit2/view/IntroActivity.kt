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
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import me.thenightmancodeth.cirkit2.R

/**
 * Created by TheNightman on 5/24/17.
 */

class IntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //First slide - Welcome to Cirkit - Your cirkit consists of all registered devices on your network
        addSlide(AppIntroFragment.newInstance("Welcome to Cirkit", "Your cirkit consists of all registered devices on your local network", 0, resources.getColor(R.color.colorAccent)))
        //Second slide - Messager - Send and receive text messages to and from other devices on your cirkit
        addSlide(AppIntroFragment.newInstance("Messenger", "Send text messages and links to other users on your local network", 0, resources.getColor(R.color.colorAccent)))
        //Third slide - Files - Send and receive files to and from other devices on your cirkit
        addSlide(AppIntroFragment.newInstance("Files", "Send and receive files to and from other devices on your cirkit", 0, resources.getColor(R.color.colorAccent)))

        //Ask for file read/write permissions
        askForPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        // Override bar/separator color.
        setBarColor(Color.parseColor("#3F51B5"))
        setSeparatorColor(Color.parseColor("#2196F3"))
        //Don't show skip
        showSkipButton(false)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }
}