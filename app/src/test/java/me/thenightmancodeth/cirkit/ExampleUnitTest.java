package me.thenightmancodeth.cirkit;

import org.junit.Test;

import me.thenightmancodeth.cirkit.backend.controllers.NetScan;
import me.thenightmancodeth.cirkit.views.MainActivity;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void nmap() {
        MainActivity ma = new MainActivity();
        NetScan ns = new NetScan(ma.getApplicationContext());

        ns.scan();
    }
}