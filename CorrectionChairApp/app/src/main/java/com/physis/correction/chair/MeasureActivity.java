package com.physis.correction.chair;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.physis.correction.chair.ble.BluetoothLEManager;
import com.physis.correction.chair.data.DeviceInfo;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MeasureActivity extends AppCompatActivity {

    private static final String TAG = MeasureActivity.class.getSimpleName();

    private Button btnZeroSetting, btnMeasure, btnControlHeight;
    private TextView tvFrontLeftValue, tvFrontRightValue, tvBackLeftValue, tvBackRightValue;
    private TextView tvStateMsg;
    private LinearLayout llStateMsg;

    private BluetoothLEManager bleManager;

    private String deviceAddress;
    private int flValue, frValue, blValue, brValue;
    private float flWeight, frWeight, blWeight, brWeight;
    private List<Integer> valueArray = new LinkedList<>();

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
                    String pressure = ( 2 - flWeight ) + ","  + ( 2 - frWeight )
                            + "," + ( 2 - blWeight ) + "," + ( 2 - brWeight );
                    startActivity(new Intent(MeasureActivity.this, ControlActivity.class)
                            .putExtra("ADDR", deviceAddress)
                            .putExtra("PRESSUREs", pressure));
                    break;
            }
        }
    };

    @SuppressLint("SetTextI18n")
    private void receiveDataHandler(String data){
        if(data.equals("ZS")){
            showStateMessage("초기화를 시작합니다.");
            initPressureState();
        }else if(data.equals("SM")){
            showStateMessage("자세측정을 시작합니다.");
        }else if(data.equals("ED")){
            llStateMsg.setVisibility(View.GONE);
            showPressureWeight();
        }else{
            showPressureValues(data.substring(1));
        }
    }

    private void showStateMessage(String msg) {
        if(llStateMsg.getVisibility() != View.VISIBLE)
            llStateMsg.setVisibility(View.VISIBLE);
        tvStateMsg.setText(msg);
    }

    private void initPressureState(){
        flValue = frValue = blValue = brValue = 0;
        flWeight = frWeight = blWeight = brWeight = 1;
        tvFrontLeftValue.setText("0 atm");
        tvFrontRightValue.setText("0 atm");
        tvBackLeftValue.setText("0 atm");
        tvBackRightValue.setText("0 atm");

        tvFrontLeftValue.setTextColor(Color.BLACK);
        tvFrontRightValue.setTextColor(Color.BLACK);
        tvBackLeftValue.setTextColor(Color.BLACK);
        tvBackRightValue.setTextColor(Color.BLACK);
    }

    private void showPressureWeight(){
        float maxPressure = flValue > frValue ? flValue : frValue;
        maxPressure = maxPressure > blValue ? maxPressure : blValue;
        maxPressure = maxPressure > brValue ? maxPressure : brValue;

        if(maxPressure == 0)
            return;

        flWeight = Math.round((flValue / maxPressure * 10)/10.0);
        frWeight = Math.round((frValue / maxPressure * 10)/10.0);
        blWeight = Math.round((blValue / maxPressure * 10)/10.0);
        brWeight = Math.round((brValue / maxPressure * 10)/10.0);

        tvFrontLeftValue.append(" (" + flWeight + ")");
        tvFrontRightValue.append(" (" + frWeight + ")");
        tvBackLeftValue.append(" (" + blWeight + ")");
        tvBackRightValue.append(" (" + brWeight + ")");
    }

    @SuppressLint("SetTextI18n")
    private void showPressureValues(String obj) {
        String[] values = obj.split(",");

        if(values.length != 4)
            return;

        flValue = Integer.valueOf(values[0]);
        frValue = Integer.valueOf(values[1]);
        blValue = Integer.valueOf(values[2]);
        brValue = Integer.valueOf(values[3]);

        valueArray.clear();
        valueArray.add(flValue);
        valueArray.add(frValue);
        valueArray.add(blValue);
        valueArray.add(brValue);
        Collections.sort(valueArray);

        tvFrontLeftValue.setText(values[0] + " atm");
        setEffectWeight(valueArray.indexOf(flValue), tvFrontLeftValue);
        tvFrontRightValue.setText(values[1] + " atm");
        setEffectWeight(valueArray.indexOf(frValue), tvFrontRightValue);
        tvBackLeftValue.setText(values[2] + " atm");
        setEffectWeight(valueArray.indexOf(blValue), tvBackLeftValue);
        tvBackRightValue.setText(values[3] + " atm");
        setEffectWeight(valueArray.indexOf(brValue), tvBackRightValue);
    }

    private void setEffectWeight(int index, TextView view){
        switch (index){
            case 0:
                view.setTextColor(Color.GREEN);
                break;
            case 1:
                view.setTextColor(Color.rgb(255,215,0));
                break;
            case 2:
                view.setTextColor(Color.rgb(255,140,0));
                break;
            case 3:
                view.setTextColor(Color.RED);
                break;
        }
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
