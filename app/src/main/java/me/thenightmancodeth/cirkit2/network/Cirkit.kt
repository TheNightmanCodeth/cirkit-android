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
        FuelManager.instance.baseHeaders = mapOf("Content-Type" to "application/json")
    }

    fun sendStringPush(msg: String) {
        Fuel.post("/msg").body("{ \"msg\" : \"$msg\" }").response { request, response, result ->
            println(response)
            val (bytes, error) = result
            if (bytes != null) {
                println(bytes)
            }
        }
    }

    fun sendImagePush(file: File) {
        Fuel.upload("/img").source { request, url ->
            File.createTempFile("temp1", ".png")
        }.responseString {request, response, result ->

        }
    }

    fun sendFilePush(file: File) {
        Fuel.upload("/file").source { request, url ->
            file
        }.responseString {request, response, result ->

        }
    }
}