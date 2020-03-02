package com.physis.correction.chair.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.physis.correction.chair.R;

public class IconButton extends LinearLayout {

    private int iconResID, backColorResID;
    private String btnText;

    private ImageView ivBtnIcon;
    private TextView tvBtnText;
    private LinearLayout llButton;

    @SuppressLint("Recycle")
    public IconButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        iconResID = context.obtainStyledAttributes(attrs, R.styleable.IconButton)
                .getResourceId(R.styleable.IconButton_button_icon, R.drawable.ic_air_pressure);
        btnText = context.obtainStyledAttributes(attrs, R.styleable.IconButton)
                .getString(R.styleable.IconButton_button_text);
        backColorResID = context.obtainStyledAttributes(attrs, R.styleable.IconButton)
                .getResourceId(R.styleable.IconButton_button_backColor, R.color.colorAccent);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view = inflater.inflate(R.layout.view_icon_button, this, false);
        addView(view);

        ivBtnIcon = view.findViewById(R.id.iv_btn_icon);
        ivBtnIcon.setImageResource(iconResID);
        tvBtnText = view.findViewById(R.id.tv_btn_text);
        tvBtnText.setText(btnText);

        llButton = view.findViewById(R.id.ll_button);
        llButton.setBackgroundResource(backColorResID);
    }
}
