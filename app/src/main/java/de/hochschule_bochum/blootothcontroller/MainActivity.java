package de.hochschule_bochum.blootothcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import de.hochschule_bochum.blootothcontroller.fragments.FragmentMgr;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "BluetoothController";
    private BTDevice selectedDevice = new BTDevice("", "");
    public static BluetoothSPP spp;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private MenuItem connectBtn;
    private Debugger debugger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        debugger = new Debugger();

        spp = new BluetoothSPP(this);

        spp.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                connectBtn.setIcon(R.drawable.ic_pause_black_24dp);
                debugger.log("Connected to " + name);
            }

            public void onDeviceDisconnected() {
                connectBtn.setIcon(R.drawable.ic_play_arrow_black_24dp);
                debugger.log("Disconnected");
            }

            public void onDeviceConnectionFailed() {
                connectBtn.setIcon(R.drawable.ic_play_arrow_black_24dp);
                debugger.log("Failed to connect to device");
            }
        });

        spp.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                debugger.log(message);
            }
        });

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
        if (id == R.id.nav_controller) {
            Toast.makeText(this, "Controller", Toast.LENGTH_SHORT).show();
            connectBtn.setVisible(true);
            setContent(R.layout.content_controller);
            debugger.updateViews();

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
            Toast.makeText(this, "Devices", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Debugging", Toast.LENGTH_SHORT).show();
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
                if (spp.getServiceState() == BluetoothState.STATE_CONNECTED) spp.disconnect();
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
                Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(35);
            }

            JSONObject json = new JSONObject();

            try {
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
