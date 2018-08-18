package com.example.admin.klineview;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by xiesuichao on 2016/10/18.
 */
public class PrintUtil {

    private static boolean printAvailable = true;

    public static void log(Object obj) {
        if (printAvailable)
            System.out.println("---x---" + getCurrentClassName() + obj);
    }

    public static void log(String title, Object obj) {
        if (printAvailable)
            System.out.println("---x---" + getCurrentClassName() + title + ":" + obj);
    }

    public static void logCode(String method, int code) {
        if (printAvailable)
            System.out.println("---x---" + getCurrentClassName() + "." + method + "() onFailed, code:" + code);
    }

    public static void logStatus(String method, String status) {
        if (printAvailable)
            System.out.println("---x---" + getCurrentClassName() + "." + method + "() onFailed, status:" + status);
    }

    public static void toastShort(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    public static void toastShort(Context context, int resourceId) {
        Toast.makeText(context, resourceId, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }

    public static void toastLong(Context context, int resourceId) {
        Toast.makeText(context, resourceId, Toast.LENGTH_LONG).show();
    }

    public static void toastCode(Context context, String content, int code) {
        Toast.makeText(context, content + ", code:" + code, Toast.LENGTH_SHORT).show();
    }

    public static void toastCode(Context context, int resourceId, int code) {
        Toast.makeText(context, context.getString(resourceId) + ", code:" + code, Toast.LENGTH_SHORT).show();
    }

    public static void toastStatus(Context context, String content, String status) {
        Toast.makeText(context, content + ", status:" + status, Toast.LENGTH_SHORT).show();
    }

    public static void toastStatus(Context context, int resourceId, String status) {
        Toast.makeText(context, context.getResources().getString(resourceId) + ", status:" + status, Toast.LENGTH_SHORT).show();
    }

    private static String getCurrentMethodName() {
        int level = 13;
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[level].getMethodName();
    }

    private static String getCurrentClassName() {
        int level = 2;
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        String className = stacks[level].getClassName();
        return className.substring(27, className.length()) + "---:";
    }

}
