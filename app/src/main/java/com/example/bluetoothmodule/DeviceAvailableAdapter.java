package com.example.bluetoothmodule;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceAvailableAdapter extends RecyclerView.Adapter<DeviceAvailableAdapter.ViewHolder> {
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private OnDeviceAvailableListener onDeviceAvailableListener;


    public DeviceAvailableAdapter(ArrayList<BluetoothDevice> bluetoothDevices, OnDeviceAvailableListener onDeviceAvailableListener) {
        this.bluetoothDevices = bluetoothDevices;
        this.onDeviceAvailableListener = onDeviceAvailableListener;
    }

    @NonNull
    @Override
    public DeviceAvailableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_paired, parent, false);
        return new ViewHolder(view,onDeviceAvailableListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAvailableAdapter.ViewHolder holder, int position) {
        if (bluetoothDevices != null) {
            holder.tvNameDevice.setText(bluetoothDevices.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvNameDevice;
        OnDeviceAvailableListener onDeviceAvailableListener;

        public ViewHolder(@NonNull View itemView, OnDeviceAvailableListener onDeviceAvailableListener) {
            super(itemView);
            tvNameDevice = itemView.findViewById(R.id.tvPairedDevice);
            this.onDeviceAvailableListener = onDeviceAvailableListener;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onDeviceAvailableListener.onDeviceAvailableClick(getAdapterPosition());
        }
    }

    public interface OnDeviceAvailableListener {
        void onDeviceAvailableClick(int position);
    }
}
