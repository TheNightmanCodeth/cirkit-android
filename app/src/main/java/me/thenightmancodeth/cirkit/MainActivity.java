package me.thenightmancodeth.cirkit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import me.thenightmancodeth.cirkit.Backend.Cirkit;
import me.thenightmancodeth.cirkit.Backend.CirkitService;
import me.thenightmancodeth.cirkit.Backend.Models.Push;
import me.thenightmancodeth.cirkit.Backend.Models.ServerResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/***************************************
 * Created by TheNightman on 11/21/16. *
 ***************************************/

public class MainActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    private Cirkit cirkit = new Cirkit();
    private final String TAG = "MainActivity";
    private final Context ctx = MainActivity.this;
    public interface OnPushReceivedListener {
        void onPushRec(String push);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ifFirstLaunchShowServerAlert();

        final EditText pushET = (EditText)findViewById(R.id.pushET);
        final TextView addrTV = (TextView)findViewById(R.id.serverAddressTV);

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formIP = String.format(Locale.US, "%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        addrTV.setText("Cirkit is listening for pushes at: \nhttp://" +formIP +":6969/");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pushString = pushET.getText().toString();
                if (pushString.equals("")) {
                    makeSnackBar("Push cannot be null!");
                } else {
                    Call<Push> call = cirkit.getSi().sendPush(new Push(pushString));
                    call.enqueue(new Callback<Push>() {
                        @Override
                        public void onResponse(Call<Push> call, Response<Push> response) {
                            makeSnackBar("Push successful!");
                            Log.i(TAG, "Response was: " + response);
                        }

                        @Override
                        public void onFailure(Call<Push> call, Throwable t) {
                            Log.d(TAG, "ERROR: " + t);

                        }
                    });
                }
            }
        });
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
            PendingIntent pendin = PendingIntent.getService(MainActivity.this, 0, cirkitService, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    Calendar.getInstance().getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, pendin);
            makeSnackBar("Cirkit is running!");
        }
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

    private void ifFirstLaunchShowServerAlert() {
        SharedPreferences prefs = getSharedPreferences("Cirkit", MODE_PRIVATE);
        boolean firstLaunch = prefs.getBoolean("CirkitFirstLaunch", false);
        if (!firstLaunch) {
            Log.i(TAG, "firstLaunch");
            final SharedPreferences.Editor edit = prefs.edit();
            LayoutInflater inflater = getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.first_launch_dialog, null);
            AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.dialog))
                    .setTitle("Cirkit Server")
                    .setMessage("Welcome to Cirkit! Start the server on your computer, and enter the IP you see here:")
                    .setView(dialogView)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditText ipET = (EditText)dialogView.findViewById(R.id.first_launch_server_et);
                            String ip = ipET.getText().toString();
                            cirkit.setServerIP(ip);
                            makeSnackBar("Server IP set to: " +cirkit.getServerIP());
                            edit.putBoolean("CirkitFirstLaunch", true);
                            edit.apply();
                        }
                    });
            dialogbuilder.create().show();
        }
    }

    private void makeSnackBar(String content) {
        Snackbar.make(findViewById(android.R.id.content), content, Snackbar.LENGTH_LONG).show();
    }
}
