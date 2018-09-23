package com.example.admin.klineview.kline;

/**
 * Created by xiesuichao on 2018/7/3.
 */

public class Pointer {
    
    private float x;
    private float y;

    public Pointer() {
    }

    public Pointer(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Pointer{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
