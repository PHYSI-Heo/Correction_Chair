package com.physis.correction.chair.list;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.correction.chair.R;

import java.util.LinkedList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceHolder> {

    public interface OnSelectedPositionListener{
        void onSelected(int position);
    }

    private OnSelectedPositionListener listener;

    public void setOnSelectedPositionListener(OnSelectedPositionListener listener){
        this.listener = listener;
    }

    private List<BluetoothDevice> devices = new LinkedList<>();

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_device_ble, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, final int position) {
        BluetoothDevice device = devices.get(position);

        holder.tvDeviceName.setText(device.getName());
        holder.tvDeviceAddress.setText(device.getAddress());

        holder.llDeviceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onSelected(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void setItems(List<BluetoothDevice> devices){
        this.devices = devices;
        notifyDataSetChanged();
    }
}
