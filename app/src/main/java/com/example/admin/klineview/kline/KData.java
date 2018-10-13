package com.example.admin.klineview.kline;

/**
 * K线数据
 * Created by xiesuichao on 2018/6/29.
 */

public class KData {

    //时间戳
    private long time;
    private double openPrice;
    private double closePrice;
    private double maxPrice;
    private double minPrice;
    private double volume;
    //涨跌额
    private double upDnAmount;
    //涨跌幅
    private double upDnRate;
    private double priceMa5;
    private double priceMa10;
    private double priceMa30;
    private double ema5;
    private double ema10;
    private double ema30;
    private double ema;
    private double volumeMa5;
    private double volumeMa10;
    private double bollMb;
    private double bollUp;
    private double bollDn;
    private double macd;
    private double dea;
    private double dif;
    private double k;
    private double d;
    private double j;
    private double rs1;
    private double rs2;
    private double rs3;
    private double leftX;
    private double rightX;
    private double closeY;
    private double openY;
    private boolean initFinish;

    public KData() {
    }

    public KData(double openPrice, double closedPrice, double maxPrice, double minPrice, double volume) {
        this.openPrice = openPrice;
        this.closePrice = closedPrice;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.volume = volume;
    }

    public KData(long time, double openPrice, double closePrice, double maxPrice, double minPrice, double volume){
        this.time = time;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.volume = volume;
    }

    public double getEma() {
        return ema;
    }

    public void setEma(double ema) {
        this.ema = ema;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getLeftX() {
        return leftX;
    }

    public void setLeftX(double leftX) {
        this.leftX = leftX;
    }

    public double getRightX() {
        return rightX;
    }

    public void setRightX(double rightX) {
        this.rightX = rightX;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getPriceMa5() {
        return priceMa5;
    }

    public void setPriceMa5(double priceMa5) {
        this.priceMa5 = priceMa5;
    }

    public double getPriceMa10() {
        return priceMa10;
    }

    public void setPriceMa10(double priceMa10) {
        this.priceMa10 = priceMa10;
    }

    public double getPriceMa30() {
        return priceMa30;
    }

    public void setPriceMa30(double priceMa30) {
        this.priceMa30 = priceMa30;
    }

    public double getVolumeMa5() {
        return volumeMa5;
    }

    public void setVolumeMa5(double volumeMa5) {
        this.volumeMa5 = volumeMa5;
    }

    public double getVolumeMa10() {
        return volumeMa10;
    }

    public void setVolumeMa10(double volumeMa10) {
        this.volumeMa10 = volumeMa10;
    }

    public double getEma5() {
        return ema5;
    }

    public void setEma5(double ema5) {
        this.ema5 = ema5;
    }

    public double getEma10() {
        return ema10;
    }

    public void setEma10(double ema10) {
        this.ema10 = ema10;
    }

    public double getEma30() {
        return ema30;
    }

    public void setEma30(double ema30) {
        this.ema30 = ema30;
    }

    public double getBollMb() {
        return bollMb;
    }

    public void setBollMb(double bollMb) {
        this.bollMb = bollMb;
    }

    public double getBollUp() {
        return bollUp;
    }

    public void setBollUp(double bollUp) {
        this.bollUp = bollUp;
    }

    public double getBollDn() {
        return bollDn;
    }

    public void setBollDn(double bollDn) {
        this.bollDn = bollDn;
    }

    public double getMacd() {
        return macd;
    }

    public void setMacd(double macd) {
        this.macd = macd;
    }

    public double getDea() {
        return dea;
    }

    public void setDea(double dea) {
        this.dea = dea;
    }

    public double getDif() {
        return dif;
    }

    public void setDif(double dif) {
        this.dif = dif;
    }

    public double getK() {
        return k;
    }

    public void setK(double k) {
        this.k = k;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public double getJ() {
        return j;
    }

    public void setJ(double j) {
        this.j = j;
    }

    public double getRs1() {
        return rs1;
    }

    public void setRs1(double rs1) {
        this.rs1 = rs1;
    }

    public double getRs2() {
        return rs2;
    }

    public void setRs2(double rs2) {
        this.rs2 = rs2;
    }

    public double getRs3() {
        return rs3;
    }

    public void setRs3(double rs3) {
        this.rs3 = rs3;
    }

    public double getUpDnAmount() {
        return closePrice - openPrice;
    }

    public void setUpDnAmount(double upDnAmount) {
        this.upDnAmount = upDnAmount;
    }

    public double getUpDnRate() {
        return (closePrice - openPrice) / openPrice;
    }

    public void setUpDnRate(double upDnRate) {
        this.upDnRate = upDnRate;
    }

    public double getCloseY() {
        return closeY;
    }

    public void setCloseY(double closeY) {
        this.closeY = closeY;
    }

    public double getOpenY() {
        return openY;
    }

    public void setOpenY(double openY) {
        this.openY = openY;
    }

    public boolean isInitFinish() {
        return initFinish;
    }

    public void setInitFinish(boolean initFinish) {
        this.initFinish = initFinish;
    }

    @Override
    public String toString() {
        return "KData{" +
                "rs1=" + rs1 +
                ", rs2=" + rs2 +
                ", rs3=" + rs3 +
                '}';
    }
}
