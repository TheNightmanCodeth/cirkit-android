package me.thenightmancodeth.cirkit.views;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import me.thenightmancodeth.cirkit.R;
import me.thenightmancodeth.cirkit.backend.controllers.Cirkit;
import me.thenightmancodeth.cirkit.backend.controllers.CirkitService;
import me.thenightmancodeth.cirkit.backend.controllers.RealmRecycler;
import me.thenightmancodeth.cirkit.backend.models.Push;
import me.thenightmancodeth.cirkit.backend.models.RealmPush;
import me.thenightmancodeth.cirkit.backend.models.ServerResponse;
import retrofit2.Response;

/***************************************
 * Created by TheNightman on 11/21/16. *
 ***************************************/

public class MainActivity extends AppCompatActivity {
    private Cirkit cirkit;
    private String nodeIp;
    private RecyclerView recyclerView;
    private final String TAG = "MainActivity";
    private Realm realm;
    public interface OnPushReceivedListener {
        void onPushRec(Push push);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar cardBar = (Toolbar)findViewById(R.id.card_toolbar);
        cardBar.inflateMenu(R.menu.menu_main);
        cardBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button, so long
                // as you specify a parent activity in AndroidManifest.xml.
                int id = item.getItemId();
                //noinspection SimplifiableIfStatement
                if (id == R.id.action_server_ip) {
                    //Show server alert now
                    showServerAlert(true);
                    return true;
                }
                return false;
            }
        });
        final EditText pushET = (EditText)findViewById(R.id.pushET);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pushString = pushET.getText().toString();
                if (pushString.equals("")) {
                    makeSnackBar("Push cannot be null!");
                } else {
                    cirkit.sendPush(pushString, cirkit.getDeviceName(), new Cirkit.ServerResponseListener() {
                        @Override
                        public void onResponse(Response<ServerResponse> response) {
                            Log.d(TAG, response.body().getResponse());
                        }

                        @Override
                        public void onError(Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            }
        });
        recyclerView = (RecyclerView)findViewById(R.id.push_rec);
        setupRealm();
        initRecycler();
        //Show the server alert if it's first start
        showServerAlert(false);
        //Get and display current device IP
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formIP = String.format(Locale.US, "%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        this.nodeIp = formIP;
        startCirkitAndRegisterTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CirkitService.resetPendingPushes();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    void startCirkitAndRegisterTimer() {
        if (!CirkitService.running) {
            //Start cirkit service
            Intent cirkitService = new Intent(MainActivity.this, CirkitService.class);
            PendingIntent pendin = PendingIntent
                    .getService(MainActivity.this, 0, cirkitService, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    Calendar.getInstance().getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY,
                    pendin);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_server_ip) {
            //Show server alert now
            showServerAlert(true);
            return true;
        } else if (id == R.id.action_start_server) {
            startCirkitAndRegisterTimer();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows alert to add server IP address (Beta only)
     * @param ovrd - If true, will show dialog whether it's first launch or not
     */
    private void showServerAlert(boolean ovrd) {
        SharedPreferences prefs = getSharedPreferences("Cirkit", MODE_PRIVATE);
        boolean firstLaunch = prefs.getBoolean("CirkitFirstLaunch", false);
        if (!firstLaunch || ovrd) {
            Log.i(TAG, "firstLaunch");
            final SharedPreferences.Editor edit = prefs.edit();
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") final View dialogView =
                    inflater.inflate(R.layout.first_launch_dialog, null);
            AlertDialog.Builder dialogbuilder =
                    new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.dialog))
                            .setTitle("Cirkit Server")
                            .setMessage("Welcome to Cirkit! Start the server on your computer, " +
                                    "and enter the IP you see here:")
                            .setView(dialogView)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    EditText ipET = (EditText)dialogView.
                                            findViewById(R.id.first_launch_server_et);

                                    if (!ipET.getText().toString().isEmpty()) {
                                        String ip = ipET.getText().toString();
                                        cirkit = new Cirkit(ip);
                                        makeSnackBar("Server IP set to: " + cirkit.getServerIP());
                                        edit.putBoolean("CirkitFirstLaunch", true);
                                        edit.apply();
                                        cirkit.registerDevice(nodeIp, "GS6",
                                                new Cirkit.ServerResponseListener() {
                                                    @Override
                                                    public void onResponse(Response<ServerResponse> response) {
                                                        Log.d(TAG, response.body().getResponse());
                                                    }

                                                    @Override
                                                    public void onError(Throwable t) {
                                                        t.printStackTrace();
                                                    }
                                                });
                                    } else {
                                        makeSnackBar("IP can't be blank!");
                                    }
                                }
                            });
            dialogbuilder.create().show();
        }
    }

    private void makeSnackBar(String content) {
        Snackbar.make(findViewById(android.R.id.content), content, Snackbar.LENGTH_LONG).show();
    }

    public void itemClick(RealmPush data) {
        //TODO: show alert with options
        //Copy push to clipboard
        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.app_name), data.getMsg());
        cb.setPrimaryClip(clip);
        makeSnackBar("Copied push to clipboard!");
    }

    private void initRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final RealmRecycler adapter = new RealmRecycler(this, realm.where(RealmPush.class).findAllAsync());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
                refreshLayout.setRefreshing(false);
                RealmResults<RealmPush> pushes = realm.where(RealmPush.class).findAll();
                for (RealmPush r: pushes) {
                    Log.e(TAG, r.getMsg());
                }
            }
        });
    }

    private void setupRealm() {
        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
