package com.physis.correction.chair;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.physis.correction.chair.ble.BluetoothLEManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    // region Check Permissions && Request Permissions
    private static final int REQ_APP_PERMISSION = 1500;
    private String[] appPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
    };

    private static final int INTENT_DELAY = 1500;

    private void checkPermissions(){
            // 권한 요청 목록 생성
            final List<String> reqPermissions = new ArrayList<>();
            for(String permission : appPermissions){
                if(checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
                    reqPermissions.add(permission);
                }
            }

            // 권한 요청 목록 확인
            if(reqPermissions.size() == 0){
                nextActivity();
            }else{
                requestPermissions(reqPermissions.toArray(new String[reqPermissions.size()]), REQ_APP_PERMISSION);
            }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQ_APP_PERMISSION){
            // 요청 권한 허용 상태 확인
            boolean accessStatus = true;
            for(int grantResult : grantResults){
                if(grantResult == PackageManager.PERMISSION_DENIED)
                    accessStatus = false;
            }

            if(!accessStatus){
                // 필요 권한 거부
                Toast.makeText(getApplicationContext(), "PERMISSION DENIED.\nAPP CLOSE.", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                // 필요 권한 허용
                nextActivity();
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // Block - Back Key
    }

    private void nextActivity(){
        if(!BluetoothLEManager.getInstance(getApplicationContext()).getEnable()){
            Toast.makeText(getApplicationContext(), "블루투스 활성화 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), ConnectActivity.class));
                    finish();
                }
            }, INTENT_DELAY);
        }
    }
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        checkPermissions();
    }
}
