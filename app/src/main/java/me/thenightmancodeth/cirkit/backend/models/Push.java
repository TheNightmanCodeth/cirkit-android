package me.thenightmancodeth.cirkit.backend.models;

/***************************************
 * Created by TheNightman on 11/22/16. *
 *                                     *
 * Retrofit model for holding push     *
 ***************************************/

public class Push {
    String push;
    String device;

    public Push(String p, String d) {
        this.push = p;
        this.device = d;
    }

    public String getPush() {
        return push;
    }

    public void setPush(String push) {
        this.push = push;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
