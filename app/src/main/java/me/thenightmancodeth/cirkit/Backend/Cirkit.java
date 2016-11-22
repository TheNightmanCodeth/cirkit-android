package me.thenightmancodeth.cirkit.Backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.thenightmancodeth.cirkit.Backend.Interfaces.ServerInterface;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/***************************************
 * Created by TheNightman on 11/21/16. *
 *                                     *
 * Cirkit object for posting to server *
 ***************************************/
public class Cirkit {
    //TODO: Find server on network
    private String API_BASE_URL = "http://10.0.0.35:6969/";
    private Retrofit retrofit;
    private ServerInterface si;

    public Cirkit() {
        //Creates lenient gson client
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        //Logs all RetroFit activity
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        //Creates retrofit object for making api objects
        retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();
        //Creates ServerInterface object with retrofit
        si = retrofit.create(ServerInterface.class);
    }

    /**
     * Accepts IP address of server and sets BASE_URL
     * @param ip - Server IP address
     */
    public void setServerIP(String ip) {
        this.API_BASE_URL = "http://" +ip +":6969/";
    }

    public ServerInterface getSi() {
        return si;
    }
}
