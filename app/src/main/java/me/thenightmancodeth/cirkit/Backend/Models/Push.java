package me.thenightmancodeth.cirkit.Backend.Models;

/**
 * Created by andrewdiragi on 11/22/16.
 */

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
