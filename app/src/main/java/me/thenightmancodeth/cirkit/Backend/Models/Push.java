package me.thenightmancodeth.cirkit.Backend.Models;

/***************************************
 * Created by TheNightman on 11/22/16. *
 *                                     *
 * Retrofit model for holding push     *
 ***************************************/

public class Push {
    String msg;

    public Push(String p) {
        this.msg = p;
    }

    public String getPush() {
        return msg;
    }

    public void setPush(String push) {
        this.msg = push;
    }
}
