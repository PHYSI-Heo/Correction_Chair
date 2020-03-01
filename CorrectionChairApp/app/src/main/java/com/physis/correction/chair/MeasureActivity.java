package com.physis.correction.chair;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.physis.correction.chair.ble.BluetoothLEManager;
import com.physis.correction.chair.data.DeviceInfo;

import java.util.Objects;

public class MeasureActivity extends AppCompatActivity {

    private Button btnZeroSetting, btnMeasure, btnControlHeight;
    private TextView tvFrontLeftValue, tvFrontRightValue, tvBackLeftValue, tvBackRightValue;
    private TextView tvStateMsg;
    private LinearLayout llStateMsg;

    private BluetoothLEManager bleManager;

    private String deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bleManager.setHandler(handler);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case BluetoothLEManager.BLE_DATA_AVAILABLE:
                    receiveDataHandler((String) msg.obj);
                    break;
                case BluetoothLEManager.BLE_DISCONNECT_DEVICE:
                    finish();
                    break;
            }
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_zero_setting:
                    bleManager.writeCharacteristic("ZS");
                    break;
                case R.id.btn_measure:
                    bleManager.writeCharacteristic("SM");
                    break;
                case R.id.btn_control_height:
                    startActivity(new Intent(MeasureActivity.this, ControlActivity.class)
                            .putExtra("ADDR", deviceAddress)
                            .putExtra("PRESSUREs", ""));
                    break;
            }
        }
    };

    private void receiveDataHandler(String data){
        if(data.equals("ZS")){
            showStateMessage("초기화를 시작합니다.");
        }else if(data.equals("SM")){
            showStateMessage("자세측정을 시작합니다.");
        }else if(data.equals("ED")){
            llStateMsg.setVisibility(View.GONE);
        }else{
            showPressureValues(data.substring(1));
        }
    }

    private void showStateMessage(String msg) {
        if(llStateMsg.getVisibility() != View.VISIBLE)
            llStateMsg.setVisibility(View.VISIBLE);
        tvStateMsg.setText(msg);
    }

    private void showPressureValues(String obj) {
        String[] values = obj.split(",");

        if(values.length != 4)
            return;

        tvFrontLeftValue.setText(values[0]);
        tvFrontRightValue.setText(values[1]);
        tvBackLeftValue.setText(values[2]);
        tvBackRightValue.setText(values[3]);
    }

    private void init() {
        btnZeroSetting = findViewById(R.id.btn_zero_setting);
        btnZeroSetting.setOnClickListener(clickListener);
        btnMeasure = findViewById(R.id.btn_measure);
        btnMeasure.setOnClickListener(clickListener);
        btnControlHeight = findViewById(R.id.btn_control_height);
        btnControlHeight.setOnClickListener(clickListener);

        tvFrontLeftValue = findViewById(R.id.tv_front_left_pressure);
        tvFrontRightValue = findViewById(R.id.tv_front_right_pressure);
        tvBackLeftValue = findViewById(R.id.tv_back_left_pressure);
        tvBackRightValue = findViewById(R.id.tv_back_right_pressure);

        tvStateMsg = findViewById(R.id.tv_state_msg);
        llStateMsg = findViewById(R.id.ll_state_msg);
        llStateMsg.setVisibility(View.GONE);

        bleManager = BluetoothLEManager.getInstance(getApplicationContext());
        deviceAddress = getIntent().getStringExtra("ADDR");

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("자세 측정");
    }
}
