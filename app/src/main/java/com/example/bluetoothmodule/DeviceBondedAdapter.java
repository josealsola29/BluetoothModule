package com.example.bluetoothmodule;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class DeviceBondedAdapter extends RecyclerView.Adapter<DeviceBondedAdapter.ViewHolder> {
    private ArrayList<BluetoothDevice> pairedDevices;
    private OnDeviceBondedListener onDeviceBondedListener;

    public DeviceBondedAdapter(ArrayList<BluetoothDevice> bluetoothDevices, OnDeviceBondedListener onDeviceBondedListener) {
        this.pairedDevices = bluetoothDevices;
        this.onDeviceBondedListener = onDeviceBondedListener;
    }

    @NonNull
    @Override
    public DeviceBondedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_bonded, parent, false);
        return new ViewHolder(view, onDeviceBondedListener);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvNameDeviceBonded;
        OnDeviceBondedListener onDeviceBondedListener;

        public ViewHolder(@NonNull View itemView, OnDeviceBondedListener onDeviceBondedListener) {
            super(itemView);
            tvNameDeviceBonded = itemView.findViewById(R.id.tvNameDeviceBonded);
            this.onDeviceBondedListener = onDeviceBondedListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onDeviceBondedListener.onDeviceBondedClick(getAdapterPosition());
        }
    }

    public interface OnDeviceBondedListener {
        void onDeviceBondedClick(int position);
    }
}
