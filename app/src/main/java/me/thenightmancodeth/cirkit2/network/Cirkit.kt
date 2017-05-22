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

package me.thenightmancodeth.cirkit2.network

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import java.io.File

/**
 * Created by joe on 5/16/17.
 */
class Cirkit {
    init {
        FuelManager.instance.basePath = "http://10.0.0.245:5500"
    }

    fun sendStringPush(msg: String) {
        Fuel.post("/msg").body("{ \"msg\" : \"$msg\" }")
                .header(mapOf("Content-Type" to "application/json"))
                .response { request, response, result ->
                    println(response)
                    val (bytes, error) = result
                    if (bytes != null) {
                        println(bytes)
                    }
                }
    }

    fun sendFilePush(file: File) {
        Fuel.upload("/file").source { request, url ->
            file
        }.name { "file" }.responseString {request, response, result ->
            println(response)
            val (bytes, error) = result
            if (bytes != null) {
                println(bytes)
            }
        }
    }
}