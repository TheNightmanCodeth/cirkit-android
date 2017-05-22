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

import fi.iki.elonen.NanoHTTPD
import java.io.IOException

/**
 * Created by TheNightman on 5/22/17.
 */

class CirkitServer() : NanoHTTPD(6969) {
    override fun serve(session: IHTTPSession?): Response {
        
        var remoteIP = session?.headers?.get("remote-addr")
        println("Request from: $remoteIP")
        if (session?.method == Method.POST) {
            try {
                session.parseBody(HashMap<String, String>())
            } catch (ioe: IOException) {
                return Response(Response.Status.INTERNAL_ERROR,
                        MIME_PLAINTEXT,
                        "SERVER INTERNAL ERROR: IOException: " + ioe.message)
            } catch (re: ResponseException) {
                return Response(re.status, MIME_PLAINTEXT, re.message)
            }
        }
        var tmp = ""
        var msg = session?.parms?.get("msg")
        println("Push received: $msg")

        return Response(Response.Status.OK, MIME_PLAINTEXT, "SERVER OK: Push received")
    }
}
