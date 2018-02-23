package com.formocr.util;

import com.formocr.model.Youtu;

/**
 * 优图配置类
 *
 * Created by ZhangGuanQun on 2017/7/2.
 */

public class Config {
    private static final String APP_ID = "10086527";
    private static final String SECRET_ID = "AKIDZ1HLDvF7i7LNNIHb5OVP7dw40CNumUXN";
    private static final String SECRET_KEY = "nOo12qSs55wgGgx51kHdmjT1Qi6klhU6";
    private static final String USER_ID = "1170182824";

    public static Youtu getYoutuInstance() {
        return new Youtu(APP_ID, SECRET_ID, SECRET_KEY, Youtu.API_YOUTU_END_POINT);
    }
}
