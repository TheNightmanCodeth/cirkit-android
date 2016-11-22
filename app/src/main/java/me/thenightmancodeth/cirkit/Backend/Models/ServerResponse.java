package me.thenightmancodeth.cirkit.Backend.Models;

/**
 * Created by andrewdiragi on 11/22/16.
 */

public class ServerResponse {
    String response;

    public ServerResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
