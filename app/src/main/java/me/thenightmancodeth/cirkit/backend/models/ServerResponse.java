package me.thenightmancodeth.cirkit.backend.models;

/***************************************
 * Created by TheNightman on 11/22/16. *
 *                                     *
 * RF model for parsing server res     *
 ***************************************/

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
