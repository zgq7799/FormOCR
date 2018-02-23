package com.formocr.model;

/**
 * 三个元素的元组,用于在一个方法里返回三种类型的值
 * <p>
 * Created by ZhangGuanQun on 2017/7/4.
 */


public class ThreeTurple<A, B, C> {
    public final A first;
    public final B second;
    public final C third;

    public ThreeTurple(A a, B b, C c) {
        this.first = a;
        this.second = b;
        this.third = c;
    }

    @Override
    public String toString() {
        return first + "," + second + "," + third;
    }
}





