package me.thenightmancodeth.cirkit.backend.models;

import io.realm.RealmObject;

/**
 * Created by joe on 12/4/16.
 */

public class RealmDevice extends RealmObject {
    private String serverIp;
    private String name;

    public String getIp() {
        return serverIp;
    }

    public void setIp(String ip) {
        this.serverIp = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
