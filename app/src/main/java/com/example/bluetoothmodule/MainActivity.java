package com.example.bluetoothmodule;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements DeviceBondedAdapter.OnDeviceBondedListener, DeviceAvailableAdapter.OnDeviceAvailableListener {
    private static final String TAG = "MAIN_ACTIVITY!!!";
    private Button btnUpdate;
    private Switch swState;
    private RecyclerView rvPairedDevices, rvBondedDevices;
    private TextView tvDeviceName;
    private BluetoothAdapter bluetoothAdapter;
    private DeviceAvailableAdapter deviceAvailableAdapter;
    private DeviceBondedAdapter deviceBondedAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private ArrayList<BluetoothDevice> bondedDevices = new ArrayList<>();
    private static final int REQUEST_DISCOVER_AVAILABLE = 1;

    // Create a BroadcastReceiver for ACTION_STATE_CHANGED.
    private final BroadcastReceiver broadcastReceiverState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.requireNonNull(action).equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "STATE_OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "STATE_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "STATE_TURNING_ON");
                        break;
                }
            }
        }
    };

    // Create a BroadcastReceiver for Discorable.
    private final BroadcastReceiver broadcastReceiverDiscorable = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.requireNonNull(action).equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (mode) {
                    //Dispositivo Visible
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "Discoberality Enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "Habilitado para recibir conexion");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "No habilitado para recibir conexión");
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTING:
                        Log.d(TAG, "STATE_DISCONNECTING");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "STATE_CONNECTED");
                        break;
                }
            }
        }
    };
    // Create a BroadcastReceiver for Discorable.
    private final BroadcastReceiver broadcastReceiverDiscover = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Objects.requireNonNull(action).equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!bluetoothDevices.contains(device)){
                    bluetoothDevices.add(device);
                    deviceAvailableAdapter.notifyDataSetChanged();
                }

            }
        }
    };

    // Create a BroadcastReceiver for Discorable.
    private final BroadcastReceiver broadcastReceiverBond = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Objects.requireNonNull(action).equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (Objects.requireNonNull(bluetoothDevice).getBondState() == BluetoothDevice.BOND_BONDED)
                    Toast.makeText(context, "Se ha vinculado exitosamente el dispositivo. "
                            + bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING)
                    Toast.makeText(context, "Se esta vinculando.... "
                            + bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE)
                    Toast.makeText(context, "No se ha podido vinvular "
                            + bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverState);
        unregisterReceiver(broadcastReceiverBond);
        unregisterReceiver(broadcastReceiverDiscorable);
        unregisterReceiver(broadcastReceiverDiscover);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindUI();

        rvBondedDevices.setVisibility(View.GONE);
        rvPairedDevices.setVisibility(View.GONE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        tvDeviceName.setText(bluetoothAdapter.getName());

        if (bluetoothAdapter == null) {
            swState.setChecked(false);
            swState.setVisibility(View.GONE);
            Toast.makeText(this, "getDefaultAdapter Error", Toast.LENGTH_SHORT).show();
        } else {
            swState.setChecked(true);
        }

        //Si el BLUETOOTH esta habilitado
        if (bluetoothAdapter.isEnabled()) {
            //Entonces enciende el switch
            swState.setChecked(true);
            if (swState.isEnabled()) {
                showRecyclers();
                showPairedDevices();
                btnUpdate.setEnabled(true);
                enableDiscoverable();
                discoverDevices();
            }
        } else {
            swState.setChecked(false);
            btnUpdate.setEnabled(false);
            Toast.makeText(this, "isEnabled Error", Toast.LENGTH_SHORT).show();
        }

        //Si cambia el estado del bluetooth
        swState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enableDisableBluetooth();
                    enableDiscoverable();
                    btnUpdate.setEnabled(true);
                    showRecyclers();
                    showPairedDevices();
                    discoverDevices();
                } else {
                    enableDisableBluetooth();
                    btnUpdate.setEnabled(false);
                    rvBondedDevices.setVisibility(View.GONE);
                    rvPairedDevices.setVisibility(View.GONE);
                }
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverDevices();
            }
        });
    }

    private void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            bondedDevices.addAll(pairedDevices);
            deviceBondedAdapter = new DeviceBondedAdapter(bondedDevices, this);
            rvBondedDevices.setLayoutManager(new LinearLayoutManager(this));
            rvBondedDevices.setAdapter(deviceBondedAdapter);
        }
    }

    private void enableDiscoverable() {
        Intent discorableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discorableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discorableIntent);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(broadcastReceiverDiscorable, intentFilter);
    }

    private void discoverDevices() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            checkBTPermission();

            bluetoothAdapter.startDiscovery();
            IntentFilter intentFilterDiscover = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiverDiscover, intentFilterDiscover);
            rvPairedDevices.setLayoutManager(new LinearLayoutManager(this));
            deviceAvailableAdapter = new DeviceAvailableAdapter(bluetoothDevices, this);
            rvPairedDevices.setAdapter(deviceAvailableAdapter);
        }
        if (!bluetoothAdapter.isDiscovering()) {
            checkBTPermission();
            bluetoothAdapter.startDiscovery();
            IntentFilter intentFilterDiscover = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiverDiscover, intentFilterDiscover);
            rvPairedDevices.setLayoutManager(new LinearLayoutManager(this));
            deviceAvailableAdapter = new DeviceAvailableAdapter(bluetoothDevices, this);
            rvPairedDevices.setAdapter(deviceAvailableAdapter);
        }
    }

    private void checkBTPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1001
                );
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1002);
            }
        }
    }

    private void enableDisableBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Does not have bluetooth capabilities", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);

            IntentFilter intentFilterEnable = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiverState, intentFilterEnable);
        }

        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            IntentFilter intentFilterDisable = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiverState, intentFilterDisable);
        }
    }

    private void bindUI() {
        btnUpdate = findViewById(R.id.btnUpdate);
        swState = findViewById(R.id.swState);
        rvBondedDevices = findViewById(R.id.rvBondedDevices);
        rvPairedDevices = findViewById(R.id.rvPairedDevices);
        tvDeviceName = findViewById(R.id.tvDeviceName);
    }

    private void showRecyclers() {
        rvBondedDevices.setVisibility(View.VISIBLE);
        rvPairedDevices.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDeviceAvailableClick(int position) {
        bluetoothAdapter.cancelDiscovery();
        bondedDevices.get(position).createBond();
    }

    @Override
    public void onDeviceBondedClick(int position) {

    }
}
