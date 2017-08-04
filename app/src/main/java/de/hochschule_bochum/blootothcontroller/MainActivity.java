package de.hochschule_bochum.blootothcontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import de.hochschule_bochum.blootothcontroller.adapter.DeviceAdapter;
import de.hochschule_bochum.blootothcontroller.adapter.ScoreAdapter;
import de.hochschule_bochum.blootothcontroller.fragments.FragmentMgr;
import de.hochschule_bochum.blootothcontroller.objects.BTDevice;
import de.hochschule_bochum.blootothcontroller.objects.GameStatus;
import de.hochschule_bochum.blootothcontroller.objects.HighscoreList;
import de.hochschule_bochum.blootothcontroller.objects.Score;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "BluetoothController";
    private GameStatus gamestatus;
    private BTDevice selectedDevice = new BTDevice("", "");
    public static BluetoothSPP spp;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private MenuItem connectBtn;
    private Debugger debugger;
    public static Vibrator vibrator;
    private TextView statusView;
    private AlertDialog gamedialog;
    private ScoreAdapter scoreAdapter;
    private HighscoreList scoreList;
    private SharedPreferences load;
    private String apiURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        load = getSharedPreferences("led-matrix-table", 0);
        apiURL = load.getString("api_url", "127.0.0.1");

        gamestatus = new GameStatus();
        gamestatus.setUsername(load.getString("username", ""));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(0);

        vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);

        debugger = new Debugger();

        spp = new BluetoothSPP(this);

        spp.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                connectBtn.setIcon(R.drawable.ic_pause_black_24dp);
                debugger.log("Connected to " + name);
            }

            public void onDeviceDisconnected() {
                connectBtn.setIcon(R.drawable.ic_play_arrow_black_24dp);
                gamestatus.setGame("");
                debugger.log("Disconnected");
                gamestatus.setStatus(GameStatus.Status.WAIING);
            }

            public void onDeviceConnectionFailed() {
                connectBtn.setIcon(R.drawable.ic_play_arrow_black_24dp);
                debugger.log("Failed to connect to device");
                gamestatus.setStatus(GameStatus.Status.WAIING);
            }
        });

        spp.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                debugger.log(message);
                try {
                    JSONObject jsonData = new JSONObject(message);
                    if (!jsonData.has("protocol")) return;
                    switch (jsonData.getInt("protocol")) {
                        case 2: // Status Protocol

                            if (statusView == null) return;
                            String gamestate = jsonData.has("status") ? jsonData.getString("status") : "unknown";
                            gamestatus.setStatus(GameStatus.Status.fromString(gamestate));
                            int score = jsonData.has("highscore") ? jsonData.getInt("highscore") : 0;
                            gamestatus.setHighscore(score);
                            int level = jsonData.has("level") ? jsonData.getInt("level") : 1;
                            gamestatus.setLevel(level);
                            setStatusView(gamestate, score, level);
                            JSONArray games = jsonData.has("games") ? jsonData.getJSONArray("games") : new JSONArray();
                            if (games.length() > 0) {
                                for (int i = 0, count = games.length(); i < count; i++) {
                                    gamestatus.addGame(games.getString(i));
                                }
                            }

                            String newAPIURL = jsonData.has("api_url") ? jsonData.getString("api_url") : "127.0.0.1";
                            if (!apiURL.equals(newAPIURL)) {
                                SharedPreferences save = getSharedPreferences("led-matrix-table", 0);
                                apiURL = newAPIURL;
                                save.edit().putString("api_url", newAPIURL).apply();
                            }
                            break;
                    }
                } catch (JSONException e) {
                    Log.d("OnReceive", null, e);
                }

            }
        });

        scoreList = new

                HighscoreList();
    }

    public void reloadDevices() {
        ListView deviceListView = (ListView) findViewById(R.id.bt_devices);
        deviceListView.setAdapter(null);
        deviceListView.deferNotifyDataSetChanged();
        ArrayList<BTDevice> deviceList = new ArrayList<>();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            BTDevice device = new BTDevice(bt.getName(), bt.getAddress());
            if (device.getMac().equals(selectedDevice.getMac())) {
                device.setSelected(true);
            }
            deviceList.add(device);
            Log.d(TAG, bt.getName() + " " + bt.getAddress());
        }

        DeviceAdapter adapter = new DeviceAdapter(getApplicationContext(), deviceList);
        deviceListView.setAdapter(adapter);
        deviceListView.deferNotifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        if (!spp.isBluetoothAvailable()) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        } else if (!spp.isBluetoothEnabled()) {
            spp.enable();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        statusView = null;
        if (id == R.id.nav_controller) {
            connectBtn.setVisible(true);
            setContent(R.layout.content_controller);
            debugger.updateViews();

            statusView = (TextView) findViewById(R.id.statusview);

            Button btnStart = (Button) findViewById(R.id.btnStart);
            Button btnSelect = (Button) findViewById(R.id.btnSelect);
            Button btnUp = (Button) findViewById(R.id.btnUp);
            Button btnDown = (Button) findViewById(R.id.btnDown);
            Button btnLeft = (Button) findViewById(R.id.btnLeft);
            Button btnRight = (Button) findViewById(R.id.btnRight);
            Button btnA = (Button) findViewById(R.id.btnA);
            Button btnB = (Button) findViewById(R.id.btnB);
            Button btnX = (Button) findViewById(R.id.btnX);
            Button btnY = (Button) findViewById(R.id.btnY);

            Button btnSelectGame = (Button) findViewById(R.id.selectGame);

            btnSelectGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (spp.getServiceState() != BluetoothState.STATE_CONNECTED) return;
                    if (gamedialog == null || !gamedialog.isShowing()) {
                        ArrayList<String> gameList = gamestatus.getGameList();
                        if (gameList.size() > 0) {
                            final String[] stringGames = new String[gameList.size()];
                            for (int i = 0; i < gameList.size(); i++) {
                                stringGames[i] = gameList.get(i);
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Choose a game");

                            builder.setItems(stringGames, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    gamestatus.setGame(stringGames[which]);
                                    JSONObject json = new JSONObject();

                                    try {
                                        json.put("protocol", 3);
                                        json.put("game", gamestatus.getSelectedGame());
                                        json.put("username", gamestatus.getUsername());
                                    } catch (JSONException e) {
                                        Log.d("sendSelectedGame", null, e);
                                    }
                                    debugger.log(json.toString());
                                    spp.send(json.toString(), true);
                                }
                            });

                            gamedialog = builder.create();
                            gamedialog.show();
                        }
                    }
                }
            });

            ControllerListener listener = new ControllerListener(btnStart, btnSelect, btnUp, btnDown, btnLeft, btnRight, btnA, btnB, btnX, btnY);

            btnStart.setOnTouchListener(listener);
            btnSelect.setOnTouchListener(listener);
            btnUp.setOnTouchListener(listener);
            btnDown.setOnTouchListener(listener);
            btnLeft.setOnTouchListener(listener);
            btnRight.setOnTouchListener(listener);
            btnA.setOnTouchListener(listener);
            btnB.setOnTouchListener(listener);
            btnX.setOnTouchListener(listener);
            btnY.setOnTouchListener(listener);
        } else if (id == R.id.nav_devices) {
            connectBtn.setVisible(false);
            setContent(R.layout.content_device);
            reloadDevices();
            debugger.updateViews();

            final ListView deviceListView = (ListView) findViewById(R.id.bt_devices);
            deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedDevice = ((BTDevice) deviceListView.getItemAtPosition(position));
                    reloadDevices();
                }
            });
        } else if (id == R.id.nav_debug) {
            connectBtn.setVisible(true);
            setContent(R.layout.content_debug);

            final ScrollView scrollBar = (ScrollView) findViewById(R.id.scrollBar);
            final TextView debugText = (TextView) findViewById(R.id.debugText);
            final EditText sendText = (EditText) findViewById(R.id.sendingText);
            final Button sendButton = (Button) findViewById(R.id.sendButton);
            debugger.updateViews(debugText, scrollBar);
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (spp.getServiceState() != BluetoothState.STATE_CONNECTED) return;
                    String text = sendText.getText().toString();
                    spp.send(text, true);
                    debugger.log(text);
                    sendText.setText("");
                }
            });
        } else if (id == R.id.nav_highscore) {
            connectBtn.setVisible(true);
            setContent(R.layout.content_highscore);

            final ListView scoreListView = (ListView) findViewById(R.id.score);
            final EditText useUsername = (EditText) findViewById(R.id.username);
            final Button usernameButton = (Button) findViewById(R.id.set_username);
            final Spinner gameList = (Spinner) findViewById(R.id.gamelist);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(apiURL + "/highscore").build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, null, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) return;
                    final String responseData = response.body().string();
                    scoreList.clear();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        System.out.println(responseData);
                        Iterator<String> temp = json.keys();
                        while (temp.hasNext()) {
                            String game = temp.next();
                            JSONArray gameList = json.getJSONArray(game);

                            for (int i = 0; i < gameList.length(); i++) {
                                JSONObject player = gameList.getJSONObject(i);
                                scoreList.add(game, new Score(player.getString("user"), player.getInt("score"), player.getInt("created")));
                            }
                        }

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                scoreAdapter = new ScoreAdapter(getApplicationContext(), scoreList);
                                scoreAdapter.selectFirstGame();

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, scoreList.getGamesArray());
                                gameList.setAdapter(adapter);

                                scoreListView.setAdapter(scoreAdapter);
                                scoreListView.deferNotifyDataSetChanged();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            gameList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (scoreAdapter != null)
                        scoreAdapter.setGame(parent.getItemAtPosition(position).toString());
                    scoreListView.setAdapter(scoreAdapter);
                    scoreListView.deferNotifyDataSetChanged();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            useUsername.setText(gamestatus.getUsername());

            usernameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences save = getSharedPreferences("led-matrix-table", 0);
                    gamestatus.setUsername(useUsername.getText().toString());
                    save.edit().putString("username", gamestatus.getUsername()).apply();
                }
            });
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BluetoothState.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "This App does not work without BT!", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        connectBtn = menu.getItem(0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect:
                if (selectedDevice.getMac().equals("")) return true;
                if (!spp.isServiceAvailable()) {
                    spp.setupService();
                    spp.startService(BluetoothState.DEVICE_OTHER);
                }
                if (spp.getServiceState() == BluetoothState.STATE_CONNECTED)
                    spp.disconnect();
                else spp.connect(selectedDevice.getMac());
                return true;
        }
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void setContent(int layout) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = FragmentMgr.newInstance(layout);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
        fragmentManager.executePendingTransactions();
        drawer.closeDrawers();
    }

    private void setStatusView(String gamestate, int score, int level) {
        if (statusView == null) return;
        String text = "Spielstatus\n";
        text += "Status: " + gamestate + "\n";
        text += "Score: " + score + "\n";
        text += "Level: " + level + "\n";

        statusView.setText(text);
    }

    public class ControllerListener implements View.OnTouchListener {

        private Button btnStart;
        private Button btnSelect;
        private Button btnUp;
        private Button btnDown;
        private Button btnLeft;
        private Button btnRight;
        private Button btnA;
        private Button btnB;
        private Button btnX;
        private Button btnY;
        private SparseIntArray states = new SparseIntArray();

        public ControllerListener(Button btnStart, Button btnSelect, Button btnUp, Button btnDown, Button btnLeft, Button btnRight, Button btnA, Button btnB, Button btnX, Button btnY) {
            this.btnStart = btnStart;
            this.btnSelect = btnSelect;
            this.btnUp = btnUp;
            this.btnDown = btnDown;
            this.btnLeft = btnLeft;
            this.btnRight = btnRight;
            this.btnA = btnA;
            this.btnB = btnB;
            this.btnX = btnX;
            this.btnY = btnY;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_DOWN))
                return false;
            if (spp.getServiceState() != BluetoothState.STATE_CONNECTED) return false;
            states.put(v.getId(), event.getAction());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                vibrator.vibrate(35);
            }

            JSONObject json = new JSONObject();

            try {
                json.put("protocol", 1);
                json.put("start", isTouched(btnStart));
                json.put("select", isTouched(btnSelect));
                json.put("up", isTouched(btnUp));
                json.put("down", isTouched(btnDown));
                json.put("left", isTouched(btnLeft));
                json.put("right", isTouched(btnRight));
                json.put("a", isTouched(btnA));
                json.put("b", isTouched(btnB));
                json.put("x", isTouched(btnX));
                json.put("y", isTouched(btnY));
            } catch (JSONException e) {
                Log.d("ControllerLis", null, e);
            }
            Log.d("ControllerLis", json.toString());
            spp.send(json.toString(), true);
            return true;
        }

        public boolean isTouched(View v) {
            return isTouched(v.getId());
        }

        public boolean isTouched(int id) {
            return states.get(id, MotionEvent.ACTION_UP) == MotionEvent.ACTION_DOWN;
        }
    }
}
