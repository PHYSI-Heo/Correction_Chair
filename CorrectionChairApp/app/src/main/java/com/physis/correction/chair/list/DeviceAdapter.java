package com.physis.correction.chair.list;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.correction.chair.R;
import com.physis.correction.chair.data.DeviceInfo;

import java.util.LinkedList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceHolder> {

    public static final int REGISTER_INFO = 1;
    public static final int BLE_SCAN_INFO = 2;

    public interface OnSelectedPositionListener{
        void onSelected(int position);
    }

    private OnSelectedPositionListener listener;

    public void setOnSelectedPositionListener(OnSelectedPositionListener listener){
        this.listener = listener;
    }

    private int deviceType;
    private List<DeviceInfo> devices = new LinkedList<>();

    public DeviceAdapter(int deviceType){
        this.deviceType = deviceType;
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_device_ble, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, final int position) {
        DeviceInfo device = devices.get(position);

        if(deviceType == REGISTER_INFO)
            holder.tvDeviceName.setText(device.getTitle());
        else
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

    public void setItems(List<DeviceInfo> devices){
        this.devices = devices;
        notifyDataSetChanged();
    }
}
