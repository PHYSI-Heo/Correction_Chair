package com.physis.correction.chair;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.physis.correction.chair.ble.BluetoothLEManager;
import com.physis.correction.chair.data.DeviceInfo;
import com.physis.correction.chair.list.DeviceAdapter;
import com.physis.correction.chair.utils.DBHelper;

import java.util.LinkedList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName;
    private ProgressBar pgbScanning;
    private RecyclerView rcvDeviceList;
    private Button btnScan;

    private BluetoothLEManager bleManager;
    private DeviceAdapter deviceAdapter;
    private DBHelper dbHelper;

    private List<DeviceInfo> deviceInfos = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bleManager.setHandler(handler);
        bleManager.scan(true,true);
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case BluetoothLEManager.BLE_SCAN_START:
                    btnScan.setEnabled(false);
                    deviceInfos.clear();
                    pgbScanning.setVisibility(View.VISIBLE);
                    rcvDeviceList.setVisibility(View.GONE);
                    break;
                case BluetoothLEManager.BLE_SCAN_STOP:
                    btnScan.setEnabled(true);
                    deviceAdapter.setItems(deviceInfos);
                    pgbScanning.setVisibility(View.GONE);
                    rcvDeviceList.setVisibility(View.VISIBLE);
                    break;
                case BluetoothLEManager.BLE_SCAN_DEVICE:
                    setDeviceInfo((BluetoothDevice)msg.obj);
                    break;
            }
        }
    };

    private void setDeviceInfo(BluetoothDevice device){
        if(device.getName() == null || device.getName().equals(""))
            return;
        boolean isExist = false;
        for(DeviceInfo info : deviceInfos){
            if(info.getAddress().equals(device.getAddress())){
                isExist = true;
                break;
            }
        }

        if(!isExist)
            deviceInfos.add(new DeviceInfo(device.getName(), device.getAddress()));
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            bleManager.scan(true,true);
        }
    };

    private DeviceAdapter.OnSelectedPositionListener selectedPositionListener = new DeviceAdapter.OnSelectedPositionListener() {
        @Override
        public void onSelected(int position) {
            deviceRegister(deviceInfos.get(position));
        }
    };

    private void deviceRegister(DeviceInfo info){
        if(etName.length() == 0){
            Toast.makeText(getApplicationContext(), "디바이스 명칭을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_NAME, etName.getText().toString());
        values.put(DBHelper.COL_ADDR, info.getAddress());
        boolean result = dbHelper.insertData(values);
        Toast.makeText(getApplicationContext(), "Device Register Result : " + result, Toast.LENGTH_SHORT).show();
        if(result)
            finish();
    }

    private void init() {
        etName = findViewById(R.id.et_device_name);
        pgbScanning = findViewById(R.id.pgb_scanning);
        pgbScanning.setVisibility(View.GONE);

        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(clickListener);

        rcvDeviceList = findViewById(R.id.rcv_device_list);
        DividerItemDecoration decoration
                = new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(getApplicationContext().getResources().getDrawable(R.drawable.rc_item_division_line, null));
        rcvDeviceList.addItemDecoration(decoration);
        // Set Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RegisterActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setItemPrefetchEnabled(true);
        rcvDeviceList.setLayoutManager(linearLayoutManager);
        // Set Adapter
        rcvDeviceList.setAdapter(deviceAdapter = new DeviceAdapter(DeviceAdapter.BLE_SCAN_INFO));
        deviceAdapter.setOnSelectedPositionListener(selectedPositionListener);

        bleManager = BluetoothLEManager.getInstance(getApplicationContext());
        dbHelper = new DBHelper(getApplicationContext());
    }
}
