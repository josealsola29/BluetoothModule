package com.example.bluetoothmodule;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACTIVITY!!!";
    private Button btnUpdate;
    private Switch swState;
    private RecyclerView rvPairedDevices, rvAvailableDevices;
    private TextView tvDeviceName;
    private BluetoothAdapter bluetoothAdapter;

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
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindUI();

        rvAvailableDevices.setVisibility(View.GONE);
        rvPairedDevices.setVisibility(View.GONE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            swState.setChecked(false);
            Toast.makeText(this, "getDefaultAdapter Error", Toast.LENGTH_SHORT).show();
        } else
            swState.setChecked(true);

        //Si el BLUETOOTH esta habilitado
        if (bluetoothAdapter.isEnabled()) {
            //Entonces enciende el switch
            swState.setChecked(true);
            if (swState.isEnabled()) {
                showRecyclers();
                btnUpdate.setEnabled(true);
                enableDisableDiscorable();
            }
        } else {
            swState.setChecked(false);
            btnUpdate.setEnabled(false);
            Toast.makeText(this, "isEnabled Error", Toast.LENGTH_SHORT).show();
        }

        //Si presionan el SWITCH
        swState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisableBluetooth();
                enableDisableDiscorable();


                if (swState.isEnabled()) {
                    btnUpdate.setEnabled(true);
                    showRecyclers();
                } else {
                    btnUpdate.setEnabled(false);
                    rvAvailableDevices.setVisibility(View.GONE);
                    rvPairedDevices.setVisibility(View.GONE);
                }
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisableDiscorable();
            }
        });

    }

    private void enableDisableDiscorable() {

        Intent discorableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discorableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discorableIntent);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(broadcastReceiverDiscorable, intentFilter);
    }

    private void enableDisableBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Does not have bluetooth capabilities", Toast.LENGTH_SHORT).show();
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
        rvAvailableDevices = findViewById(R.id.rvAvailableDevices);
        rvPairedDevices = findViewById(R.id.rvPairedDevices);
        tvDeviceName = findViewById(R.id.tvDeviceName);
    }

    private void showRecyclers() {
        rvAvailableDevices.setVisibility(View.VISIBLE);
        rvPairedDevices.setVisibility(View.VISIBLE);
    }
}
