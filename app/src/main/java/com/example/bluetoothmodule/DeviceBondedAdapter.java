package com.example.bluetoothmodule;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class DeviceBondedAdapter  extends RecyclerView.Adapter<DeviceBondedAdapter.ViewHolder> {
    private ArrayList<BluetoothDevice> pairedDevices;

    public DeviceBondedAdapter(ArrayList<BluetoothDevice> bluetoothDevices) {
        this.pairedDevices = bluetoothDevices;
    }

    @NonNull
    @Override
    public DeviceBondedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_bonded, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceBondedAdapter.ViewHolder holder, int position) {
        if (pairedDevices != null) {
            holder.tvNameDeviceBonded.setText(pairedDevices.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        return pairedDevices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNameDeviceBonded;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameDeviceBonded = itemView.findViewById(R.id.tvNameDeviceBonded);
        }
    }
}
