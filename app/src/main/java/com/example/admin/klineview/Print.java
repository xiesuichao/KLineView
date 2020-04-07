package com.example.admin.klineview;


import android.util.Log;

/**
 * Created by xiesuichao on 2016/10/18.
 */
public class Print {

    private static boolean printAvailable = false;
    private static final String TAG_LOG = "KLine";

    public static void log(Object obj) {
        if (printAvailable){
            Log.w(TAG_LOG, "--x--" + getCurrentClassName() + "--:" + obj);
        }
    }

    public static void log(String title, Object obj) {
        if (printAvailable){
            Log.w(TAG_LOG, "--x--" + getCurrentClassName() + "--:" + title + ":" + obj);
        }
    }

    private static String getCurrentClassName() {
        int level = 2;
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        String className = stacks[level].getClassName();
        return className.substring(className.lastIndexOf(".") + 1);
    }

}
