package com.example.bluetoothmodule;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DevicePairedAdapter extends RecyclerView.Adapter<DevicePairedAdapter.ViewHolder> {
    private ArrayList<BluetoothDevice> bluetoothDevices;

    public DevicePairedAdapter(ArrayList<BluetoothDevice> bluetoothDevices) {
        this.bluetoothDevices = bluetoothDevices;
    }

    @NonNull
    @Override
    public DevicePairedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_paired, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DevicePairedAdapter.ViewHolder holder, int position) {
        if (bluetoothDevices != null) {
            holder.tvNameDevice.setText(bluetoothDevices.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNameDevice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameDevice = itemView.findViewById(R.id.tvDeviceName);
        }
    }
}
