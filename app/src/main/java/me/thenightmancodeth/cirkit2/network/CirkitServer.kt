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

import android.os.Environment
import fi.iki.elonen.NanoHTTPD
import me.thenightmancodeth.cirkit2.model.Push
import me.thenightmancodeth.cirkit2.service.CirkitService
import java.io.*
import java.net.URLDecoder
import java.util.regex.Pattern

/**
 * Created by TheNightman on 5/22/17.
 */


class CirkitServer(val listener: CirkitService.OnPushReceivedLisener) : NanoHTTPD(6969) {
    override fun serve(session: IHTTPSession?): Response {
        val remoteIP = session?.headers?.get("remote-addr")
        println("Request from: $remoteIP")
        val map: Map<String, String> = HashMap<String, String>()
        if (session?.method == Method.POST) {
            if (session.headers["content-type"] == "application/json") {
                try {
                    session.parseBody(map)
                } catch (ioe: IOException) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,
                            MIME_PLAINTEXT,
                            "SERVER INTERNAL ERROR: IOException: " + ioe.message)
                } catch (re: ResponseException) {
                    return newFixedLengthResponse(re.status, MIME_PLAINTEXT, re.message)
                }

                val tmp = map["postData"]

                val pushString = tmp?.singleKeyJsonExtract("msg")
                val push = Push(remoteIP!!)
                push.stringMessage = pushString

                listener.onPushReceived(push)

                return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "SERVER OK: Push received")
            } else { //if (session.headers?.get("content-type") == "multipart/form-data") {
                println("File received...")
                val files: Map<String, String> = HashMap<String, String>()
                try {
                    session.parseBody(files)
                }catch (io: IOException) {
                    io.printStackTrace()
                }catch (re: ResponseException) {
                    re.printStackTrace()
                }

                val keys: Set<String> = files.keys
                var filePath: String? = null
                keys.forEach { key ->
                    println(key)
                    val cachePath = files.get(key)
                    val tempFile: File = File(cachePath)
                    val filesDir = Environment.getExternalStorageDirectory().path
                    val permFile = File("$filesDir/Download/$key")
                    tempFile.copyTo(permFile, true)
                    filePath = permFile.path
                }

                val push: Push = Push(remoteIP!!)
                push.filePath = filePath

                listener.onPushReceived(push)
            }
        }
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "SERVER ERR: Invalid request")
    }

    fun String.singleKeyJsonExtract(key: String): String {
        //{"msg":"DATA DATA", "from":"Device name"}
        val re1 = ".*?"
        val re2 = "\"(.*?)\""
        val re3 = ".*?"
        val re4 = "\"(.*?)\""
        val patt = Pattern.compile(re1 + re2 + re3 + re4, Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matc = patt.matcher(this)

        while (matc.find()) {
            if (matc.group(1) == (key)) {
                try {
                    val toRet = URLDecoder.decode(matc.group(2).replace(Pattern.quote("+"), "%2b"), "UTF-8")
                    return toRet
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        }
        return "ERR"
    }
}