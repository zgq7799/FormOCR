package com.formocr.model;

import java.util.Comparator;

/**
 * 按照X坐标排序
 * <p>
 * Created by ZhangGuanQun on 2017/7/4.
 */


public class TurpleXSort implements Comparator<ThreeTurple> {
    @Override
    public int compare(ThreeTurple o1, ThreeTurple o2) {
        int preX = Integer.parseInt(o1.second.toString());
        int aftX = Integer.parseInt(o2.second.toString());
        if (preX != aftX) {
            return preX - aftX;
        } else {
            return 0;
        }
    }
}
