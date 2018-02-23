package com.formocr.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动收集器
 *
 * Created by ZhangGuanQun on 2017/5/21.
 */

public class ActivityCollector {

    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity:activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}