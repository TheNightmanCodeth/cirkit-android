package me.thenightmancodeth.cirkit.backend.controllers;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.nmap4j.Nmap4j;
import org.nmap4j.data.NMapRun;

import java.util.Locale;

import static android.content.Context.WIFI_SERVICE;

/***************************************
 * Created by TheNightman on 4/16/17.  *
 *                                     *
 * Uses nmap to scan for server        *
 * on network                          *
 ***************************************/

public class NetScan {
    Context ctx;
    Nmap4j nmap;

    public NetScan(Context ctx) {
        this.ctx = ctx;

        nmap = new Nmap4j("/usr/local");
        nmap.includeHosts(getIP(true) +"1-255");
        nmap.excludeHosts(getIP(false));
        nmap.addFlags( "-T3 -oX - -O -sV" ) ;
    }

    public void scan() {
        try {
            nmap.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!nmap.hasError()) {
            NMapRun nmr = nmap.getResult();
            Log.i("NMR", "" +nmr);
        } else {
            Log.e("NMAP", nmap.getExecutionResults().getErrors());
        }
    }

    private String getIP(boolean nmap) {
        WifiManager wifiManager = (WifiManager)ctx.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        if (!nmap) {
            return String.format(Locale.US, "%d.%d.%d.%d", (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        } else {
            return String.format(Locale.US, "%d.%d.%d.", (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff));
        }
    }
}
