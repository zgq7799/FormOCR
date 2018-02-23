package com.formocr.util;

import com.formocr.model.ThreeTurple;

import java.util.ArrayList;

/**
 * 划分工具类
 * <p>
 * Created by ZhangGuanQun on 2017/7/4.
 */

public class ClassifyUtil {
    /**
     * 按照X的间距500进行类别划分,其中每一个对象代表一个列
     *
     * @param lst 按照X排序后的元组
     * @return 按列划分类别后的结果
     */
    public static ArrayList<ArrayList<ThreeTurple>> classifyByX(ArrayList<ThreeTurple> lst) {
        ArrayList<ArrayList<ThreeTurple>> result = new ArrayList<>();
        ArrayList<ThreeTurple> classLst = new ArrayList<>();
        for (int i = 1; i < lst.size(); i++) {
            classLst.add(lst.get(i - 1));
            if (i == lst.size() - 1) {
                classLst.add(lst.get(i));
                result.add(classLst);
            }
            if ((Integer) lst.get(i).second - (Integer) lst.get(i - 1).second > 500) {
                result.add(classLst);
                classLst = new ArrayList<>();
            }
        }
        return result;
    }

    /**
     * 按照Y的间距200进行类别划分,其中每一个对象代表一个行
     *
     * @param lst 按照Y排序后的元组
     * @return 按行划分类别后的结果
     */
    public static ArrayList<ArrayList<ThreeTurple>> classifyByY(ArrayList<ThreeTurple> lst) {
        ArrayList<ArrayList<ThreeTurple>> result = new ArrayList<>();
        ArrayList<ThreeTurple> classLst = new ArrayList<>();
        for (int i = 1; i < lst.size(); i++) {
            classLst.add(lst.get(i - 1));
            if (i == lst.size() - 1) {
                classLst.add(lst.get(i));
                result.add(classLst);
            }
            if ((Integer) lst.get(i).third - (Integer) lst.get(i - 1).third > 60) {
                result.add(classLst);
                classLst = new ArrayList<>();
            }
        }
        return result;
    }
}
