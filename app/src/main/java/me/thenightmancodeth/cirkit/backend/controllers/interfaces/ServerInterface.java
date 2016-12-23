package me.thenightmancodeth.cirkit.backend.controllers.interfaces;

import java.util.List;

import me.thenightmancodeth.cirkit.backend.models.NodeDevice;
import me.thenightmancodeth.cirkit.backend.models.Push;
import me.thenightmancodeth.cirkit.backend.models.RealmDevice;
import me.thenightmancodeth.cirkit.backend.models.ServerResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/***************************************
 * Created by TheNightman on 11/22/16. *
 *                                     *
 * Retrofit interface for talking to   *
 * server node.                        *
 ***************************************/

public interface ServerInterface {
    @POST("cirkit")
    Call<ServerResponse> sendPush(@Body Push push);

    @POST("register")
    Call<ServerResponse> registerDevice(@Body NodeDevice device);

    @GET("devices")
    Call<List<NodeDevice>> getDevices();
}
