package me.thenightmancodeth.cirkit.views;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.IdRes;
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
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import me.thenightmancodeth.cirkit.R;
import me.thenightmancodeth.cirkit.backend.controllers.Cirkit;
import me.thenightmancodeth.cirkit.backend.controllers.CirkitService;
import me.thenightmancodeth.cirkit.backend.controllers.RealmRecycler;
import me.thenightmancodeth.cirkit.backend.controllers.interfaces.ServerInterface;
import me.thenightmancodeth.cirkit.backend.models.NodeDevice;
import me.thenightmancodeth.cirkit.backend.models.Push;
import me.thenightmancodeth.cirkit.backend.models.RealmDevice;
import me.thenightmancodeth.cirkit.backend.models.RealmPush;
import me.thenightmancodeth.cirkit.backend.models.ServerResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/***************************************
 * Created by TheNightman on 11/21/16. *
 ***************************************/

public class MainActivity extends AppCompatActivity {
    private Cirkit cirkit;
    private String nodeIp;
    private RecyclerView recyclerView;
    private final String TAG = "MainActivity";
    private Realm realm;
    private Context ctx = this;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;
    public interface OnPushReceivedListener {
        void onPushRec(Push push);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("Cirkit", MODE_PRIVATE);
        setupRealm();
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
        final ImageButton deviceSelect = (ImageButton)findViewById(R.id.devicePicker);
        if (prefs.getString("serverIP", null) != null) {
            cirkit = new Cirkit(prefs.getString("serverIP", null));
        } else {
            showServerAlert(true);
        }
        deviceSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDevicePicker();
            }
        });
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
        initRecycler();
        //Get and display current device IP
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        this.nodeIp = String.format(Locale.US, "%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
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
                    SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_HALF_DAY,
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
        boolean firstLaunch = prefs.getBoolean("CirkitFirstLaunch", false);
        if (!firstLaunch || ovrd) {
            Log.i(TAG, "firstLaunch");
            final SharedPreferences.Editor edit = prefs.edit();
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") final View dialogView =
                    inflater.inflate(R.layout.first_launch_dialog, null);
            final EditText ipET = (EditText)dialogView.
                    findViewById(R.id.first_launch_server_et);
            final EditText nameET = (EditText)dialogView.
                    findViewById(R.id.first_launch_device_name);

            //Check if the device has already been named and given a server IP
            final RealmResults<RealmDevice> devices = realm.where(RealmDevice.class).findAll();
            if (devices.size() > 0) {
                ipET.setText(devices.get(0).getIp());
                nameET.setText(devices.get(0).getName());
            }
            for (RealmDevice d: devices) {
                Log.d("Device: ", d.getIp() +" " +d.getName());
            }
            AlertDialog.Builder dialogbuilder =
                    new AlertDialog.Builder(new ContextThemeWrapper(ctx, R.style.dialog))
                            .setTitle("Cirkit Server")
                            .setMessage("Welcome to Cirkit! Start the server on your computer, " +
                                    "and enter the IP you see here:")
                            .setView(dialogView)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (!ipET.getText().toString().isEmpty() && !nameET.getText().toString().isEmpty()) {
                                        String ip = ipET.getText().toString();
                                        String deviceName = nameET.getText().toString();

                                        cirkit = new Cirkit(ip);
                                        cirkit.setDeviceName(deviceName);
                                        cirkit.setServerIP(ip);
                                        makeSnackBar("Server IP set to: " + cirkit.getServerIP());
                                        edit.putBoolean("CirkitFirstLaunch", true);
                                        edit.putString("serverIP", ip);
                                        edit.apply();
                                        //Save device info to realm
                                        Realm realm = Realm.getDefaultInstance();
                                        realm.beginTransaction();
                                        devices.deleteAllFromRealm();
                                        RealmDevice toRealm = realm.createObject(RealmDevice.class);
                                        toRealm.setIp(ip);
                                        toRealm.setName(deviceName);
                                        realm.commitTransaction();
                                        cirkit.registerDevice(nodeIp, deviceName,
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
            if (!ovrd) {
                dialogbuilder.setCancelable(false);
            }
            dialogbuilder.create().show();
        }
    }

    public void makeSnackBar(String content) {
        Snackbar.make(findViewById(android.R.id.content), content, Snackbar.LENGTH_LONG).show();
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
            }
        });
    }

    private void setupRealm() {
        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();
    }

    private void showDevicePicker() {
        ServerInterface r = cirkit.getRetrofit();
        Call<List<NodeDevice>> call = r.getDevices();
        call.enqueue(new Callback<List<NodeDevice>>() {
            @Override
            public void onResponse(Call<List<NodeDevice>> call, Response<List<NodeDevice>> response) {
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") final View dialogView =
                        inflater.inflate(R.layout.radio_dialog, null);
                final List<NodeDevice> devices = response.body();
                // Create radio button for each device in devices
                RadioGroup rg = (RadioGroup) dialogView.findViewById(R.id.radio_group);
                for (NodeDevice device: devices) {
                    RadioButton deviceButton = new RadioButton(getApplicationContext());
                    deviceButton.setText(device.getName());
                    rg.addView(deviceButton);
                }
                // Listen for radio check change
                rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                        RadioButton selected = (RadioButton)dialogView.findViewById(checkedId);
                        String selectedName = selected.getText().toString();
                        for (NodeDevice d: devices) {
                            if (selectedName.equals(d.getName())) {
                                Log.e("Set server ip:", d.getName() +" : " +d.getIp());
                                cirkit = new Cirkit(d.getIp());
                            }
                        }
                    }
                });
                final AlertDialog.Builder radioDialog =
                    new AlertDialog.Builder(new ContextThemeWrapper(ctx, R.style.dialog))
                    .setTitle("Select recipient")
                    .setView(dialogView)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                radioDialog.create().show();
            }

            @Override
            public void onFailure(Call<List<NodeDevice>> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
