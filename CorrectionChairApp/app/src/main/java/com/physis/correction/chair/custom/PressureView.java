package com.physis.correction.chair.custom;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.physis.correction.chair.R;

public class PressureView extends LinearLayout {

    private TextView tvPressureValue;
    private EditText etSetupPressure;

    public PressureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(infService);
        assert inflater != null;
        View v = inflater.inflate(R.layout.view_pressure_state, this, false);
        addView(v);

        tvPressureValue = v.findViewById(R.id.tv_current_pressure);
        etSetupPressure = v.findViewById(R.id.et_setup_pressure);

        this.setBackgroundColor(Color.CYAN);
    }
}
