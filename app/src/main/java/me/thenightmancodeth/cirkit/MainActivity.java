package me.thenightmancodeth.cirkit;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RunnableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {
    private CirkitServer server;
    private Handler handler = new Handler();
    public interface OnPushReceivedListener {
        void onPushRec(String push);
    }
    public TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        tv = (TextView) findViewById(R.id.text);
    }

    @Override
    protected void onResume() {
        super.onResume();

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formIP = String.format(Locale.US, "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        Toast.makeText(getApplicationContext(), "http://" +formIP +":6969", Toast.LENGTH_LONG).show();

        try {
            server = new CirkitServer(new OnPushReceivedListener() {
                @Override
                public void onPushRec(String push) {
                    tv.setText(push);
                }
            });
            server.start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (server != null) {
            server.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class CirkitServer extends NanoHTTPD {
        private OnPushReceivedListener listener;
        public CirkitServer(OnPushReceivedListener l) throws IOException {
            super(6969);
            this.listener = l;
        }

        @Override
        public Response serve(IHTTPSession session) {
            Map<String, String> jsonBody = new HashMap<String, String>();
            Method method = session.getMethod();
            if (method.POST.equals(method)) {
                try {
                    session.parseBody(jsonBody);
                } catch (IOException ioe) {
                    return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                } catch (ResponseException re) {
                    return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
                }
            }
            String tem = "";
            for (Map.Entry<String, String> k: session.getParms().entrySet()) {
                tem = k.getKey();
                Log.e("REGEX", k.getKey());
            }
            final String dataRaw = tem;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String push = extractVal(dataRaw);
                    Toast.makeText(getApplicationContext(), "Push received: " +push, Toast.LENGTH_LONG).show();
                }
            });
            return new Response(Response.Status.OK, MIME_PLAINTEXT, "Push:  received successfully");
        }
    }

    public String extractVal(String dataRaw) {
        String test = ".+:\"(.+)\"";
        Pattern patt = Pattern.compile(test,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matc = patt.matcher(dataRaw);
        if (matc.find()) {
            return matc.group(1);
        }
        return null;
    }
}
