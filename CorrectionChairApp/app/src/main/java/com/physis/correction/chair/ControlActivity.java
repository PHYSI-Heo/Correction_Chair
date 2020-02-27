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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.physis.correction.chair.ble.BluetoothLEManager;

import java.util.Objects;

public class ControlActivity extends AppCompatActivity {

    private TextView tvFrontLeftPressure, tvFrontRightPressure, tvBackLeftPressure, tvBackRightPressure;
    private EditText etFrontLeftHeight, etFrontRightHeight, etBackLeftHeight, etBackRightHeight;
    private Button btnStartMeasure, btnStartSetup, btnZeroSetting;

    private ProgressBar pgbSetting;
    private TextView tvSetState;

    private BluetoothLEManager bleManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleManager.disconnectDevice();
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
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_zero_setting:
                    bleManager.writeCharacteristic("ZS");
                    break;
                case R.id.btn_start_measure_pressure:
                    bleManager.writeCharacteristic("SM");
                    break;
                case R.id.btn_start_manual_control:
                    if(etFrontLeftHeight.length() == 0 || etFrontRightHeight.length() == 0 ||
                            etBackLeftHeight.length() == 0 || etBackRightHeight.length() == 0 ){
                        Toast.makeText(getApplicationContext(), "설정 높이를 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(etFrontLeftHeight.getText().toString().equals("0")|| etFrontRightHeight.getText().toString().equals("0")||
                            etBackLeftHeight.getText().toString().equals("0") || etBackRightHeight.getText().toString().equals("0")){
                        Toast.makeText(getApplicationContext(), "1 cm 이상의 높이를 설정하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    bleManager.writeCharacteristic("MH" + etFrontLeftHeight.getText().toString() + ","
                            + etFrontRightHeight.getText().toString() + ","
                            + etBackLeftHeight.getText().toString() + ","
                            + etBackRightHeight.getText().toString());
                    break;
            }
        }
    };

    private void showStateMessage(String msg){
        tvSetState.setText(msg);
        if(tvSetState.getVisibility() != View.VISIBLE)
            tvSetState.setVisibility(View.VISIBLE);
        if(pgbSetting.getVisibility() != View.VISIBLE)
            pgbSetting.setVisibility(View.VISIBLE);
    }

    private void receiveDataHandler(String data){
        if(data.equals("ZS")){
            showStateMessage("초기화를 시작합니다.");
        }else if(data.equals("SM")){
            showStateMessage("자세측정을 시작합니다.");
        }else if(data.equals("MH")){
            showStateMessage("교정 높이를 설정합니다.");
        }else if(data.equals("ED")){
            tvSetState.setVisibility(View.GONE);
            pgbSetting.setVisibility(View.GONE);
        }else{
            showPressureValues(data.substring(1));
        }
    }

    private void showPressureValues(String obj) {
        String[] values = obj.split(",");

        if(values.length != 4)
            return;

        tvFrontLeftPressure.setText(values[0]);
        tvFrontRightPressure.setText(values[1]);
        tvBackLeftPressure.setText(values[2]);
        tvBackRightPressure.setText(values[3]);
    }


    private void init() {
        bleManager = BluetoothLEManager.getInstance(getApplicationContext());
        bleManager.setHandler(handler);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayHomeAsUpEnabled(true);

        tvFrontLeftPressure = findViewById(R.id.tv_front_left_pressure);
        tvFrontRightPressure = findViewById(R.id.tv_front_right_pressure);
        tvBackLeftPressure = findViewById(R.id.tv_back_left_pressure);
        tvBackRightPressure = findViewById(R.id.tv_back_right_pressure);

        etFrontLeftHeight = findViewById(R.id.et_front_left_height);
        etFrontRightHeight = findViewById(R.id.et_front_right_height);
        etBackLeftHeight = findViewById(R.id.et_back_left_height);
        etBackRightHeight = findViewById(R.id.et_back_right_height);

        btnStartMeasure = findViewById(R.id.btn_start_measure_pressure);
        btnStartMeasure.setOnClickListener(clickListener);
        btnStartSetup = findViewById(R.id.btn_start_manual_control);
        btnStartSetup.setOnClickListener(clickListener);
        btnZeroSetting = findViewById(R.id.btn_zero_setting);
        btnZeroSetting.setOnClickListener(clickListener);

        pgbSetting = findViewById(R.id.pgb_setting);
        pgbSetting.setVisibility(View.GONE);
        tvSetState = findViewById(R.id.tv_setup_state);
        tvSetState.setVisibility(View.GONE);
    }
}
