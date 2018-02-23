package com.formocr.activity;

import android.app.Activity;
import android.os.Bundle;

import com.formocr.R;
import com.formocr.util.ActivityCollector;

/**
 * 基Acitivty
 *
 * Created by ZhangGuanQun on 2017/5/21.
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
