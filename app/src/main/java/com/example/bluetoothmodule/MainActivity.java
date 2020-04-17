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
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements DeviceBondedAdapter.OnDeviceBondedListener, DeviceAvailableAdapter.OnDeviceAvailableListener {
    private static final String TAG = "MAIN_ACTIVITY!!!";
    private Button btnUpdate;
    private Switch swState;
    private RecyclerView rvPairedDevices, rvBondedDevices;
    private TextView tvDeviceName;

    private DeviceAvailableAdapter deviceAvailableAdapter;
    private DeviceBondedAdapter deviceBondedAdapter;

    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private ArrayList<BluetoothDevice> bondedDevices = new ArrayList<>();

    private static final int REQUEST_DISCOVER_AVAILABLE = 1;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothConnectionService bluetoothConnectionService;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");


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

    // Create a BroadcastReceiver for Discorable. indica que el modo de escaneo Bluetooth del adaptador local ha cambiado.
    private final BroadcastReceiver broadcastReceiverDiscorable = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            showPairedDevices();
            String action = intent.getAction();
            if (Objects.requireNonNull(action).equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (mode) {
                    //Dispositivo Visible
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "broadcastReceiverDiscorable: Discoberality Enabled");
                        Toast.makeText(context, "Dispositivo es reconocible y conectable desde " +
                                "dispositivos Bluetooth remotos.", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "broadcastReceiverDiscorable: Habilitado para recibir conexion");
                        Toast.makeText(context, "este dispositivo no se puede detectar desde dispositivos" +
                                " Bluetooth remotos, pero se puede conectar desde dispositivos remotos que han" +
                                " descubierto previamente este dispositivo.", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "broadcastReceiverDiscorable: No habilitado para recibir conexión");
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTING:
                        Log.d(TAG, "broadcastReceiverDiscorable: STATE_DISCONNECTING");
                        Toast.makeText(context, "Indica que tanto el escaneo de consulta como" +
                                " el escaneo de página están deshabilitados en el adaptador Bluetooth " +
                                "local. Por lo tanto, este dispositivo no es reconocible ni conectable " +
                                "desde dispositivos Bluetooth remotos.\n", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "broadcastReceiverDiscorable: STATE_CONNECTED");
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
                if (!bluetoothDevices.contains(device)) {
                    bluetoothDevices.add(device);
                    deviceAvailableAdapter.notifyDataSetChanged();
                }
                addDevicesDiscovered();
            }
        }
    };

    // Create a BroadcastReceiver for Discorable.
    private final BroadcastReceiver broadcastReceiverBondChange = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Objects.requireNonNull(action).equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice bluetoothDeviceBond = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (Objects.requireNonNull(bluetoothDeviceBond).getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    Toast.makeText(context, "El dispositivo está vinculado. "
                            + bluetoothDeviceBond.getName(), Toast.LENGTH_SHORT).show();
                    bluetoothDevice = bluetoothDeviceBond;
                }

                if (bluetoothDeviceBond.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                    Toast.makeText(context, "Se esta vinculando con el dispositivo.... "
                            + bluetoothDeviceBond.getName(), Toast.LENGTH_SHORT).show();
                }

                if (bluetoothDeviceBond.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE");
                    Toast.makeText(context, "El dispositivo remoto no está unido (emparejado). ",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverState);
        unregisterReceiver(broadcastReceiverBondChange);
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

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiverBondChange, filter);


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
                btnUpdate.setEnabled(true);
                showPairedDevices();
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
        bondedDevices.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            bondedDevices.addAll(pairedDevices);
            deviceBondedAdapter = new DeviceBondedAdapter(bondedDevices, this);
            rvBondedDevices.setLayoutManager(new LinearLayoutManager(this));
            rvBondedDevices.setAdapter(deviceBondedAdapter);
        }
    }

    //Habilita las funciones que permite que el dispositivo se meustre reconocible
    private void enableDiscoverable() {

        //Muestra una actividad del sistema que solicita el modo reconocible. Esta actividad también
        // solicitará al usuario que active Bluetooth si no está habilitado actualmente.
        Intent discorableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discorableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discorableIntent);

        //indica que el modo de escaneo Bluetooth del adaptador local ha cambiado.
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

    private void addDevicesDiscovered() {
        rvPairedDevices.setLayoutManager(new LinearLayoutManager(this));
        deviceAvailableAdapter = new DeviceAvailableAdapter(bluetoothDevices, this);
        rvPairedDevices.setAdapter(deviceAvailableAdapter);
    }

    private void startBluetoothConnection(BluetoothDevice bluetoothDevice, UUID uuid) {
        Log.d(TAG, "startBluetoothConnection: Initializing RFCOM Bluetooth Connection");
        bluetoothConnectionService.startClient(bluetoothDevice, uuid);

    }

    @Override
    public void onDeviceAvailableClick(int position) {
        bluetoothAdapter.cancelDiscovery();
        bluetoothDevices.get(position).createBond();
        showPairedDevices();
    }

    @Override
    public void onDeviceBondedClick(int position) {
        bluetoothDevice = bluetoothDevices.get(position);
        bluetoothConnectionService = new BluetoothConnectionService(MainActivity.this);
//        startBluetoothConnection
    }
}


