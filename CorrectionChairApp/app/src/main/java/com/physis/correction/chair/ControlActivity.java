package com.physis.correction.chair;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.physis.correction.chair.ble.BluetoothLEManager;
import com.physis.correction.chair.utils.DBHelper;

import java.util.Objects;

public class ControlActivity extends AppCompatActivity {

    private EditText etFrontLeftHeight, etFrontRightHeight, etBackLeftHeight, etBackRightHeight;
    private Button btnControlHeight, btnZeroSetting, btnSaveHeight;
    private LinearLayout llStateMsg;
    private TextView tvStateMsg;

    private BluetoothLEManager bleManager = null;
    private DBHelper dbHelper;

    private String deviceAddress;

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
                case R.id.btn_control_height:
                    startHeightControl();
                    break;
                case R.id.btn_save_height:
                    saveHeight();
                    break;
            }
        }
    };

    private void saveHeight(){
        String newPressures = etFrontLeftHeight.getText().toString() + ","
                + etFrontRightHeight.getText().toString() + ","
                + etBackLeftHeight.getText().toString() + ","
                + etBackRightHeight.getText().toString();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_PRESSURE, newPressures);
        boolean result = dbHelper.updateData(values, DBHelper.COL_ADDR, deviceAddress);
        Toast.makeText(getApplicationContext(), "Update Height : " + result, Toast.LENGTH_SHORT).show();
    }

    private void startHeightControl(){
        if(etFrontLeftHeight.length() == 0 || etFrontRightHeight.length() == 0 ||
                etBackLeftHeight.length() == 0 || etBackRightHeight.length() == 0 ){
            Toast.makeText(getApplicationContext(), "설정 높이를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        bleManager.writeCharacteristic("MH" + etFrontLeftHeight.getText().toString() + ","
                + etFrontRightHeight.getText().toString() + ","
                + etBackLeftHeight.getText().toString() + ","
                + etBackRightHeight.getText().toString());
    }

    private void showStateMessage(String msg){
        if(llStateMsg.getVisibility() != View.VISIBLE)
            llStateMsg.setVisibility(View.VISIBLE);
        tvStateMsg.setText(msg);
    }

    private void receiveDataHandler(String data){
        switch (data) {
            case "ZS":
                showStateMessage("초기화를 시작합니다.");
                break;
            case "MH":
                showStateMessage("교정 높이를 설정합니다.");
                break;
            case "ED":
                llStateMsg.setVisibility(View.GONE);
                break;
        }
    }


    private void init() {
        bleManager = BluetoothLEManager.getInstance(getApplicationContext());
        bleManager.setHandler(handler);
        dbHelper = new DBHelper(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("자세 교정");

        etFrontLeftHeight = findViewById(R.id.et_front_left_height);
        etFrontRightHeight = findViewById(R.id.et_front_right_height);
        etBackLeftHeight = findViewById(R.id.et_back_left_height);
        etBackRightHeight = findViewById(R.id.et_back_right_height);

        btnControlHeight = findViewById(R.id.btn_control_height);
        btnControlHeight.setOnClickListener(clickListener);
        btnZeroSetting = findViewById(R.id.btn_zero_setting);
        btnZeroSetting.setOnClickListener(clickListener);
        btnSaveHeight = findViewById(R.id.btn_save_height);
        btnSaveHeight.setOnClickListener(clickListener);

        llStateMsg = findViewById(R.id.ll_state_msg);
        llStateMsg.setVisibility(View.GONE);
        tvStateMsg = findViewById(R.id.tv_state_msg);

        deviceAddress = getIntent().getStringExtra("ADDR");
        String[] pressureList = Objects.requireNonNull(getIntent()
                .getStringExtra("PRESSUREs")).split(",");
        etFrontLeftHeight.setText(pressureList[0]);
        etFrontRightHeight.setText(pressureList[1]);
        etBackLeftHeight.setText(pressureList[2]);
        etBackRightHeight.setText(pressureList[3]);
    }
}
