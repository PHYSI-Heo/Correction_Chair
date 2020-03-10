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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.physis.correction.chair.ble.BluetoothLEManager;
import com.physis.correction.chair.custom.IconButton;
import com.physis.correction.chair.data.DeviceInfo;
import com.physis.correction.chair.list.DeviceAdapter;
import com.physis.correction.chair.utils.DBHelper;
import com.physis.correction.chair.utils.LoadingDialog;

import java.util.LinkedList;
import java.util.List;

public class ConnectActivity extends AppCompatActivity {

    public static final String HM_10_CONF = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String HM_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private RecyclerView rcvDeviceList;
    private ProgressBar pgbLoading;
    private Button btnRegister;
    private IconButton iBtnMeasure, iBtnControlHeight;
    private TextView tvStateMsg;

    private BluetoothLEManager bleManager = null;
    private DBHelper dbHelper;
    private DeviceAdapter deviceAdapter;

    private List<DeviceInfo> devices = new LinkedList<>();
    private DeviceInfo selectedDevice;
    private BluetoothDevice bleDevice;

    private boolean isConnected = false;

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
        bleManager.setHandler(handler);
        setDeviceList();
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
        public void onSelected(final int position) {
            bleManager.disconnectDevice();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectedDevice = devices.get(position);
                    bleManager.scan(true, true);
                }
            }, 500);
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_register:
                    startActivity(new Intent(ConnectActivity.this, RegisterActivity.class));
                    break;
                case R.id.btn_measure:
                    if(isConnected){
                        startActivity(new Intent(ConnectActivity.this, MeasureActivity.class)
                                .putExtra("ADDR", selectedDevice.getAddress()));
                    }else{
                        Toast.makeText(getApplicationContext(), "먼저 디바이스 연결을 진행하세요.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_control_height:
                    if(isConnected){
                        startActivity(new Intent(ConnectActivity.this, ControlActivity.class)
                                .putExtra("ADDR", selectedDevice.getAddress())
                                .putExtra("PRESSUREs", selectedDevice.getPressures()));
                    }else{
                        Toast.makeText(getApplicationContext(), "먼저 디바이스 연결을 진행하세요.", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case BluetoothLEManager.BLE_SCAN_START:
                    pgbLoading.setVisibility(View.VISIBLE);
                    tvStateMsg.setText("디바이스를 검색합니다.");
                    rcvDeviceList.setEnabled(false);
                    break;
                case BluetoothLEManager.BLE_SCAN_STOP:
                    if(bleDevice != null){
                        bleManager.connectDevice(bleDevice);
                        pgbLoading.setVisibility(View.VISIBLE);
                    }else{
                        rcvDeviceList.setEnabled(true);
                        pgbLoading.setVisibility(View.GONE);
                        tvStateMsg.setText("자세교정 디바이스의 상태를 확인하세요.");
                    }
                    break;
                case BluetoothLEManager.BLE_SCAN_DEVICE:
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    if(device.getAddress().equals(selectedDevice.getAddress())){
                        bleDevice = device;
                        bleManager.scan(false,false);
                    }
                    break;
                case BluetoothLEManager.BLE_CONNECT_DEVICE:
                    tvStateMsg.setText("디바이스가 연결되었습니다.");
                    break;
                case BluetoothLEManager.BLE_SERVICES_DISCOVERED:
                    if (!bleManager.notifyCharacteristic(HM_10_CONF, HM_RX_TX)) {
                        bleManager.disconnectDevice();
                    }
                    break;
                case BluetoothLEManager.BLE_READ_CHARACTERISTIC:
                    pgbLoading.setVisibility(View.GONE);
                    tvStateMsg.setText("자세측정 또는 제어를 선택하세요.");
                    isConnected = true;
                    break;
                case BluetoothLEManager.BLE_DISCONNECT_DEVICE:
                    tvStateMsg.setText("디바이스 연결이 종료되었습니다.");
                    rcvDeviceList.setEnabled(true);
                    isConnected = false;
//                    pgbLoading.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private void setDeviceList(){
        devices = dbHelper.getDevices();
        deviceAdapter.setItems(devices);
        if(devices.size() == 0){
            tvStateMsg.setText("등록된 디바이스가 없습니다.");
        }else{
            tvStateMsg.setText("자세교정 디바이스를 선택하세요.");
        }
    }

    private void init() {
        bleManager = BluetoothLEManager.getInstance(getApplicationContext());
        bleManager.bindService();
        bleManager.registerReceiver();

        dbHelper = new DBHelper(getApplicationContext());

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
        rcvDeviceList.setAdapter(deviceAdapter = new DeviceAdapter(DeviceAdapter.REGISTER_INFO));
        deviceAdapter.setOnSelectedPositionListener(selectedPositionListener);

        tvStateMsg = findViewById(R.id.tv_status_msg);
        pgbLoading = findViewById(R.id.pgb_loading);
        pgbLoading.setVisibility(View.GONE);

        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(clickListener);
        iBtnControlHeight = findViewById(R.id.btn_control_height);
        iBtnControlHeight.setOnClickListener(clickListener);
        iBtnMeasure = findViewById(R.id.btn_measure);
        iBtnMeasure.setOnClickListener(clickListener);
    }
}
