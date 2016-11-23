package me.thenightmancodeth.cirkit.Backend.Interfaces;

import me.thenightmancodeth.cirkit.Backend.Models.NodeDevice;
import me.thenightmancodeth.cirkit.Backend.Models.Push;
import me.thenightmancodeth.cirkit.Backend.Models.ServerResponse;
import retrofit2.Call;
import retrofit2.http.Body;
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
}
