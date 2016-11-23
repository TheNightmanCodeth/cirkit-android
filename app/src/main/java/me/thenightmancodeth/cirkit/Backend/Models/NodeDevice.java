package me.thenightmancodeth.cirkit.Backend.Models;

/**
 * Created by andrewdiragi on 11/23/16.
 */

public class NodeDevice {
    private String ip;
    private String name;

    public NodeDevice(String ip, String name) {
        this.ip = ip;
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
