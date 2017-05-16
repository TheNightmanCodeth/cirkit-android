package me.thenightmancodeth.cirkit2.network

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost

/**
 * Created by joe on 5/16/17.
 */
class Cirkit {
    init {
        FuelManager.instance.baseHeaders = mapOf("Content-Type" to "application/json")
    }

    fun sendStringPush(msg: String) {
        Fuel.post("http://10.0.0.245:5500/msg").body("{ \"msg\" : \"$msg\" }").response { request, response, result ->
            println(response)
            val (bytes, error) = result
            if (bytes != null) {
                println(bytes)
            }
        }
    }
}