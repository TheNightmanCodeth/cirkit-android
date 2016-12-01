package me.thenightmancodeth.cirkit.backend.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by joe on 11/30/16.
 */

public class RealmPush extends RealmObject {
    @PrimaryKey
    private int id;
    private String msg;
    private String sender;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
