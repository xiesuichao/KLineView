package com.example.admin.klineview;


/**
 * Created by xiesuichao on 2016/10/18.
 */
public class Print {

    private static boolean printAvailable = true;

    public static void log(Object obj) {
        if (printAvailable)
            System.out.println("---x---" + getCurrentClassName() + obj);
    }

    public static void log(String title, Object obj) {
        if (printAvailable)
            System.out.println("---x---" + getCurrentClassName() + title + ":" + obj);
    }

    private static String getCurrentClassName() {
        int level = 2;
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        String className = stacks[level].getClassName();
        return className.substring(className.lastIndexOf(".") + 1);
    }

}
