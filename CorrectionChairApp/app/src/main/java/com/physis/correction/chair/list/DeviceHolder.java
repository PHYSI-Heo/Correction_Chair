package com.physis.correction.chair.list;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.correction.chair.R;

public class DeviceHolder extends RecyclerView.ViewHolder {

    LinearLayout llDeviceInfo;
    TextView tvDeviceName, tvDeviceAddress;

    public DeviceHolder(@NonNull View itemView) {
        super(itemView);

        llDeviceInfo = itemView.findViewById(R.id.ll_device_info);
        tvDeviceName = itemView.findViewById(R.id.tv_device_name);
        tvDeviceAddress = itemView.findViewById(R.id.tv_device_address);

    }
}
