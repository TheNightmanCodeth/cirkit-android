package me.thenightmancodeth.cirkit2.network

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost

/**
 * Created by joe on 5/16/17.
 */
class Cirkit {
    init {
        FuelManager.instance.basePath = "http://10.0.0.245:5500/"
    }

    fun sendStringPush(msg: String) {
        "/msg".httpPost().body("{ \"msg\": \"$msg\"}").responseString {request, response, result ->
            println(response)
            val (bytes, error) = result
            if (bytes != null) {
                println(bytes)
            }
        }
    }
}