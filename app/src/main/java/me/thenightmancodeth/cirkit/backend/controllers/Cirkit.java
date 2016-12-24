package me.thenightmancodeth.cirkit.backend.controllers;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.thenightmancodeth.cirkit.backend.controllers.interfaces.ServerInterface;
import me.thenightmancodeth.cirkit.backend.models.NodeDevice;
import me.thenightmancodeth.cirkit.backend.models.Push;
import me.thenightmancodeth.cirkit.backend.models.ServerResponse;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/***************************************
 * Created by TheNightman on 11/21/16. *
 *                                     *
 * Cirkit object for posting to server *
 ***************************************/
public class Cirkit {
    //TODO: Find server on network
    private String API_BASE_URL = "";
    private String DEVICE_NAME = "";
    private ServerInterface si;
    private String IP = "10.0.0.35";
    Retrofit retrofit;
    public interface ServerResponseListener {
        void onResponse(Response<ServerResponse> response);
        void onError(Throwable t);
    }

    public Cirkit(String ip) {
        setServerIP(ip);
        //Creates retrofit object for making api objects
        retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(/*enableLoggingGson*/))
                .build();
        //Creates ServerInterface object with retrofit
        si = retrofit.create(ServerInterface.class);
    }

    public ServerInterface getRetrofit() {
        return si;
    }

    /**
     * Accepts IP address of server and sets BASE_URL
     * @param ip - Server IP address
     */
    public void setServerIP(String ip) {
        this.API_BASE_URL = "http://" +ip +":6969/";
        this.IP = ip;
    }

    public String getServerIP() {
        return this.IP;
    }

    public void setDeviceName(String name) {
        this.DEVICE_NAME = name;
    }

    public String getDeviceName() {
        return DEVICE_NAME;
    }

    /**
     * Sends message to device
     * @param push - The message to send
     * @param name - This device
     * @param ip - The device to send the push to. If null, push is sent to server
     * @param listener - The listener, run on server response
     */
    public void sendPush(String push, String name, @Nullable String ip, final ServerResponseListener listener) {
        //Create a new server interface to push with
        ServerInterface toPushWith = si;
        //If we're given an ip to send to, create a new serverinterface with a new retrofit instance
        if (ip != null) {
            //Create a new retrofit isntance with the ip provided
            Retrofit r = new Retrofit.Builder()
                    .baseUrl("http://" +ip +":6969/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            //Assign new value to the serverinterface used to push with
            toPushWith = r.create(ServerInterface.class);
        }
        Call<ServerResponse> call = toPushWith.sendPush(new Push(push, name));
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                listener.onResponse(response);
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                listener.onError(t);
            }
        });
    }

    public void registerDevice(String ip, String name, final ServerResponseListener listener) {
        Call<ServerResponse> call = si.registerDevice(new NodeDevice(ip, name));
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                listener.onResponse(response);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                listener.onError(t);
            }
        });
    }
}
