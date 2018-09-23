package com.example.admin.klineview.depth;

import android.support.annotation.NonNull;

/**
 * 深度数据
 * Created by xiesuichao on 2018/9/23.
 */

public class Depth implements Comparable<Depth>{

    private double price;
    private double volume;
    //buy:0, sell:1
    private int tradeType;
    private float x;
    private float y;

    public Depth() {
    }

    public Depth(double price, double volume, int tradeType) {
        this.price = price;
        this.volume = volume;
        this.tradeType = tradeType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public int getTradeType() {
        return tradeType;
    }

    public void setTradeType(int tradeType) {
        this.tradeType = tradeType;
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
    public int compareTo(@NonNull Depth o) {
        double diff = this.getPrice() - o.getPrice();
        if (diff > 0){
            return 1;
        }else if (diff < 0){
            return -1;
        }else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Depth{" +
                "price=" + price +
                ", volume=" + volume +
                ", tradeType=" + tradeType +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
