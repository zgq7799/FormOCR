package com.formocr.model;

import java.util.Comparator;

/**
 * 按照Y坐标排序
 * <p>
 * Created by ZhangGuanQun on 2017/7/4.
 */


public class TurpleYSort implements Comparator<ThreeTurple> {
    @Override
    public int compare(ThreeTurple o1, ThreeTurple o2) {
        int preY = Integer.parseInt(o1.third.toString());
        int aftY = Integer.parseInt(o2.third.toString());
        if (preY != aftY) {
            return preY - aftY;
        } else {
            return 0;
        }
    }
}