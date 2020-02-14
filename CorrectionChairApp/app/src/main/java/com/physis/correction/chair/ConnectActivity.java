package com.physis.correction.chair;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.physis.correction.chair.ble.BluetoothLEManager;
import com.physis.correction.chair.list.DeviceAdapter;
import com.physis.correction.chair.utils.LoadingDialog;

import java.util.LinkedList;
import java.util.List;

public class ConnectActivity extends AppCompatActivity {

    public static final String HM_10_CONF = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String HM_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private RecyclerView rcvDeviceList;
    private TextView tvBtnScan;
    private ProgressBar pgbScanning;

    private BluetoothLEManager bleManager = null;
    private DeviceAdapter deviceAdapter;

    private List<BluetoothDevice> devices = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_connect);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bleManager.scan(true, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleManager.disconnectDevice();
        bleManager.unregisterReceiver();
        bleManager.unBindService();
    }

    private DeviceAdapter.OnSelectedPositionListener selectedPositionListener = new DeviceAdapter.OnSelectedPositionListener() {
        @Override
        public void onSelected(int position) {
            bleManager.connectDevice(devices.get(position));
            LoadingDialog.show(ConnectActivity.this, "Connect Device..");
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.tv_btn_scan)
                bleManager.scan(true, true);
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case BluetoothLEManager.BLE_SCAN_START:
                    pgbScanning.setVisibility(View.VISIBLE);
                    rcvDeviceList.setEnabled(false);
                    devices.clear();
                    break;
                case BluetoothLEManager.BLE_SCAN_STOP:
                    pgbScanning.setVisibility(View.GONE);
                    rcvDeviceList.setEnabled(true);
                    deviceAdapter.setItems(devices);
                    break;
                case BluetoothLEManager.BLE_SCAN_DEVICE:
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    if(device.getName() == null || device.getName().equals(""))
                        return;
                    if(!devices.contains(device))
                        devices.add(device);
                    break;
                case BluetoothLEManager.BLE_CONNECT_DEVICE:
                    break;
                case BluetoothLEManager.BLE_SERVICES_DISCOVERED:
                    if (!bleManager.notifyCharacteristic(HM_10_CONF, HM_RX_TX)) {
                        bleManager.disconnectDevice();
                    }
                    break;
                case BluetoothLEManager.BLE_READ_CHARACTERISTIC:
                    startActivity(new Intent(ConnectActivity.this, SetupActivity.class));
                    LoadingDialog.dismiss();
                    break;
                case BluetoothLEManager.BLE_DISCONNECT_DEVICE:
                    LoadingDialog.dismiss();
                    break;
            }
        }
    };

    private void init() {
        bleManager = BluetoothLEManager.getInstance(getApplicationContext());
        bleManager.bindService();
        bleManager.registerReceiver();
        bleManager.setHandler(handler);

        tvBtnScan = findViewById(R.id.tv_btn_scan);
        tvBtnScan.setOnClickListener(clickListener);

        pgbScanning = findViewById(R.id.pgb_scan);
        pgbScanning.setVisibility(View.GONE);

        rcvDeviceList = findViewById(R.id.rcv_device_list);
        DividerItemDecoration decoration
                = new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(getApplicationContext().getResources().getDrawable(R.drawable.rc_item_division_line, null));
        rcvDeviceList.addItemDecoration(decoration);
        // Set Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ConnectActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setItemPrefetchEnabled(true);
        rcvDeviceList.setLayoutManager(linearLayoutManager);
        // Set Adapter
        rcvDeviceList.setAdapter(deviceAdapter = new DeviceAdapter());
        deviceAdapter.setOnSelectedPositionListener(selectedPositionListener);
    }
}
