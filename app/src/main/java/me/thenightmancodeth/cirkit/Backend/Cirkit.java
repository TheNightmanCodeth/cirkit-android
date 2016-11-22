package me.thenightmancodeth.cirkit.Backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.thenightmancodeth.cirkit.Backend.Interfaces.ServerInterface;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by andrewdiragi on 11/22/16.
 */

public class Cirkit {
    //TODO: Find server on network
    private static final String API_BASE_URL = "http://10.0.0.35:6969/";
    private Retrofit retrofit;
    private ServerInterface si;

    public Cirkit() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        si = retrofit.create(ServerInterface.class);
    }

    public ServerInterface getSi() {
        return si;
    }
}
