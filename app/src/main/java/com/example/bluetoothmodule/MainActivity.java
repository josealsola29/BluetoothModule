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
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
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
            }
        } else {
            swState.setChecked(false);
            btnUpdate.setEnabled(true);
            Toast.makeText(this, "isEnabled Error", Toast.LENGTH_SHORT).show();
        }

        //Si presionan el SWITCH
        swState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisableBluetooth();

                if (swState.isEnabled())
                    showRecyclers();
                else {
                    rvAvailableDevices.setVisibility(View.GONE);
                    rvPairedDevices.setVisibility(View.GONE);
                }
            }
        });

    }

    private void enableDisableBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Does not have bluetooth capabilities", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);

            IntentFilter intentFilterEnable = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiver, intentFilterEnable);
        }

        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();

            IntentFilter intentFilterDisable = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiver, intentFilterDisable);
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
