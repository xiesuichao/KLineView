package com.example.admin.klineview;

import android.graphics.Path;

import java.util.List;

/**
 * Created by xiesuichao on 2018/8/12.
 */

public class QuotaUtil {

    private static final int START_SMA5 = 5;
    private static final int START_SMA10 = 10;
    private static final int START_SMA30 = 30;
    private static final int[] MA_PERIOD_ARR = {START_SMA5, START_SMA10, START_SMA30};
    private static final float BEZIER_RATIO = 0.16f;


    /**
     * 初始化 MA5，MA10, MA30
     *
     * @param dataList
     */
    public static void initMa(List<KData> dataList){
        long startMa = System.currentTimeMillis();
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        for (int i = 0; i < dataList.size(); i++) {
            if (i >= 4 && i + 4 <= dataList.size()){
                //priceMa5
                dataList.get(i).setPriceMa5(getPriceMa(dataList.subList(i, i + 4)));
                //volumeMa5
                dataList.get(i).setVolumeMa5(getVolumeMa(dataList.subList(i, i + 4)));
            }
            if (i >= 9 && i + 9 <= dataList.size()){
                //priceMa10
                dataList.get(i).setPriceMa10(getPriceMa(dataList.subList(i, i + 9)));
                //volumeMa10
                dataList.get(i).setVolumeMa10(getVolumeMa(dataList.subList(i, i + 9)));
            }
            if (i >= 29 && i + 29 <= dataList.size()){
                //priceMa30
                dataList.get(i).setPriceMa30(getPriceMa(dataList.subList(i, i + 29)));
            }
            dataList.get(i).setInitFinish(true);
        }
        long endMa = System.currentTimeMillis();
        PrintUtil.log("MA", endMa - startMa);

    }

    public static void getMa(List<KData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        for (int i = 0; i < dataList.size(); i++) {
            for (int j : MA_PERIOD_ARR) {
                if (i + j <= dataList.size()) {
                    if (j == START_SMA5) {
                        //priceMa5
                        dataList.get(i + j - 1).setPriceMa5(getPriceMa(dataList.subList(i, i + j)));
                        //volumeMa5
                        dataList.get(i + j - 1).setVolumeMa5(getVolumeMa(dataList.subList(i, i + j)));
                    } else if (j == START_SMA10) {
                        //priceMa10
                        dataList.get(i + j - 1).setPriceMa10(getPriceMa(dataList.subList(i, i + j)));
                        //volumeMa10
                        dataList.get(i + j - 1).setVolumeMa10(getVolumeMa(dataList.subList(i, i + j)));
                    } else if (j == START_SMA30) {
                        //priceMa30
                        dataList.get(i + j - 1).setPriceMa30(getPriceMa(dataList.subList(i, i + j)));
                    }
                }
            }
        }
    }

    private static double getVolumeMa(List<KData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return -1;
        }
        double sum = 0;
        for (KData data : dataList) {
            sum += data.getVolume();
        }
        return sum / dataList.size();
    }

    private static double getPriceMa(List<KData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return -1;
        }
        double sum = 0;
        for (KData data : dataList) {
            sum += data.getClosePrice();
        }
        return sum / dataList.size();
    }

    /**
     * 初始化 EMA5, EMA10, EMA30
     *
     * @param dataList
     */
    public static void initEma(List<KData> dataList) {
        long startEma = System.currentTimeMillis();
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        double lastEma5 = dataList.get(0).getClosePrice();
        double lastEma10 = dataList.get(0).getClosePrice();
        double lastEma30 = dataList.get(0).getClosePrice();
        dataList.get(0).setEma5(lastEma5);
        dataList.get(0).setEma10(lastEma10);
        dataList.get(0).setEma30(lastEma30);

        for (int i = 1; i < dataList.size(); i++) {
            double currentEma5 = 2 * (dataList.get(i).getClosePrice() - lastEma5) / (5 + 1) + lastEma5;
            double currentEma10 = 2 * (dataList.get(i).getClosePrice() - lastEma10) / (10 + 1) + lastEma10;
            double currentEma30 = 2 * (dataList.get(i).getClosePrice() - lastEma30) / (30 + 1) + lastEma30;

            dataList.get(i).setEma5(currentEma5);
            dataList.get(i).setEma10(currentEma10);
            dataList.get(i).setEma30(currentEma30);

            lastEma5 = currentEma5;
            lastEma10 = currentEma10;
            lastEma30 = currentEma30;
        }
        long endEma = System.currentTimeMillis();
        PrintUtil.log("EMA", endEma - startEma);
    }

    /**
     * BOLL(n)计算公式：
     * MA=n日内的收盘价之和÷n。
     * MD=（n-1）日的平方根（C－MA）的两次方之和除以n
     * MB=（n－1）日的MA
     * UP=MB+k×MD
     * DN=MB－k×MD
     * K为参数，可根据股票的特性来做相应的调整，一般默认为2
     *
     * @param quotesList 数据集合
     * @param period     周期，一般为26
     * @param k          参数，可根据股票的特性来做相应的调整，一般默认为2
     */
    public static void initBOLL(List<KData> quotesList, int period, int k) {
        long startBoll = System.currentTimeMillis();
        if (quotesList == null || quotesList.isEmpty()
                || period < 0 || period > quotesList.size() - 1) {
            return;
        }
        double mb;//上轨线
        double up;//中轨线
        double dn;//下轨线
        //n日
        double sum = 0;
        //n-1日
        double sum2 = 0;
        //n日MA
        double ma;
        //n-1日MA
        double ma2;
        double md;

        for (int i = 0; i < quotesList.size(); i++) {
            KData quotes = quotesList.get(i);
            sum += quotes.getClosePrice();
            sum2 += quotes.getClosePrice();
            if (i > period - 1)
                sum -= quotesList.get(i - period).getClosePrice();
            if (i > period - 2)
                sum2 -= quotesList.get(i - period + 1).getClosePrice();

            //这个范围不计算，在View上的反应就是不显示这个范围的boll线
            if (i < period - 1)
                continue;

            ma = sum / period;
            ma2 = sum2 / (period - 1);
            md = 0;
            for (int j = i + 1 - period; j <= i; j++) {
                //n-1日
                md += Math.pow(quotesList.get(j).getClosePrice() - ma, 2);
            }
            md = Math.sqrt(md / period);
            //(n－1）日的MA
            mb = ma2;
            up = mb + k * md;
            dn = mb - k * md;

            quotes.setBollMb(mb);
            quotes.setBollUp(up);
            quotes.setBollDn(dn);
        }
        long endBoll = System.currentTimeMillis();
        PrintUtil.log("BOLL", endBoll - startBoll);
    }

    public static void initBoll(List<KData> dataList) {
        initBOLL(dataList, 26, 2);
    }

    /**
     * MACD
     *
     * @param dataList
     * @param fastPeriod   日快线移动平均，标准为12，按照标准即可
     * @param slowPeriod   日慢线移动平均，标准为26，可理解为天数
     * @param signalPeriod 日移动平均，标准为9，按照标准即可
     */
    public static void initMACD(List<KData> dataList, int fastPeriod, int slowPeriod, int signalPeriod) {
        long startMacd = System.currentTimeMillis();
        if (dataList == null || dataList.isEmpty()){
            return;
        }
        double preEma_12 = 0;
        double preEma_26 = 0;
        double preDEA = 0;

        double ema_12 = 0;
        double ema_26 = 0;

        double dea = 0;
        double dif = 0;
        double macd = 0;
        for (int i = 0; i < dataList.size(); i++) {
            ema_12 = preEma_12 * (fastPeriod - 1) / (fastPeriod + 1) + dataList.get(i).getClosePrice() * 2 / (fastPeriod + 1);
            ema_26 = preEma_26 * (slowPeriod - 1) / (slowPeriod + 1) + dataList.get(i).getClosePrice() * 2 / (slowPeriod + 1);
            dif = ema_12 - ema_26;
            dea = preDEA * (signalPeriod - 1) / (signalPeriod + 1) + dif * 2 / (signalPeriod + 1);
            macd = 2 * (dif - dea);

            preEma_12 = ema_12;
            preEma_26 = ema_26;
            preDEA = dea;

            dataList.get(i).setMacd(macd);
            dataList.get(i).setDea(dea);
            dataList.get(i).setDif(dif);
        }
        long endMacd = System.currentTimeMillis();
        PrintUtil.log("MACD", endMacd - startMacd);
    }

    public static void initMACD(List<KData> dataList) {
        initMACD(dataList, 12, 26, 9);
    }

    /**
     * KDJ
     *
     * @param dataList
     * @param n        标准值9
     * @param m1       标准值3
     * @param m2       标准值3
     */
    public static void initKDJ(List<KData> dataList, int n, int m1, int m2) {
        long startKdj = System.currentTimeMillis();
        if (dataList == null || dataList.isEmpty()){
            return;
        }
        //K值
        double[] mK = new double[dataList.size()];
        //D值
        double[] mD = new double[dataList.size()];
        //J值
        double jValue;
        double highValue = dataList.get(0).getMaxPrice();
        double lowValue = dataList.get(0).getMinPrice();
        //记录最高价位置
        int highPosition = 0;
        //记录最低价位置
        int lowPosition = 0;
        double rSV = 0.0;
        for (int i = 0; i < dataList.size(); i++) {
            if (i == 0) {
//                mK[0] = 33.33;
//                mD[0] = 11.11;
//                jValue = 77.78;
                mK[0] = 50;
                mD[0] = 50;
                jValue = 50;
            } else {
                //对最高价和最低价赋值
                if (highValue <= dataList.get(i).getMaxPrice()) {
                    highValue = dataList.get(i).getMaxPrice();
                    highPosition = i;
                }
                if (lowValue >= dataList.get(i).getMinPrice()) {
                    lowValue = dataList.get(i).getMinPrice();
                    lowPosition = i;
                }
                if (i > (n - 1)) {
                    //判断存储的最高价是否高于当前最高价
                    if (highValue > dataList.get(i).getMaxPrice()) {
                        //判断最高价是不是在最近n天内，若不在最近n天内，则从最近n天找出最高价并赋值
                        if (highPosition < (i - (n - 1))) {
                            highValue = dataList.get(i - (n - 1)).getMaxPrice();
                            for (int j = (i - (n - 2)); j <= i; j++) {
                                if (highValue <= dataList.get(j).getMaxPrice()) {
                                    highValue = dataList.get(j).getMaxPrice();
                                    highPosition = j;
                                }
                            }
                        }
                    }
                    if ((lowValue < dataList.get(i).getMinPrice())) {
                        if (lowPosition < i - (n - 1)) {
                            lowValue = dataList.get(i).getMinPrice();
                            for (int k = i - (n - 2); k <= i; k++) {
                                if (lowValue >= dataList.get(k).getMinPrice()) {
                                    lowValue = dataList.get(k).getMinPrice();
                                    lowPosition = k;
                                }
                            }
                        }
                    }
                }
                if (highValue != lowValue) {
                    rSV = (dataList.get(i).getClosePrice() - lowValue) / (highValue - lowValue) * 100;
                }
                mK[i] = (mK[i - 1] * (m1 - 1) + rSV) / m1;
                mD[i] = (mD[i - 1] * (m2 - 1) + mK[i]) / m2;
                jValue = 3 * mK[i] - 2 * mD[i];
            }
            dataList.get(i).setK(mK[i]);
            dataList.get(i).setD(mD[i]);
            dataList.get(i).setJ(jValue);
        }
        long endKdj = System.currentTimeMillis();
        PrintUtil.log("KDJ", endKdj - startKdj);
    }

    public static void initKDJ(List<KData> dataList) {
        initKDJ(dataList, 9, 3, 3);
    }

    //三阶贝塞尔曲线控制点
    public static void setBezierPath(List<Pointer> pointList, Path path) {
        path.reset();
        if (pointList == null || pointList.isEmpty()) {
            return;
        }
        path.moveTo(pointList.get(0).getX(), pointList.get(0).getY());
        Pointer leftControlPointer = new Pointer();
        Pointer rightControlPointer = new Pointer();

        for (int i = 0; i < pointList.size(); i++) {
            if (i == 0 && pointList.size() > 2) {
                leftControlPointer.setX(pointList.get(i).getX() + BEZIER_RATIO * (pointList.get(i + 1).getX()
                        - pointList.get(0).getX()));
                leftControlPointer.setY(pointList.get(i).getY() + BEZIER_RATIO * (pointList.get(i + 1).getY()
                        - pointList.get(0).getY()));
                rightControlPointer.setX(pointList.get(i + 1).getX() - BEZIER_RATIO * (pointList.get(i + 2).getX()
                        - pointList.get(i).getX()));
                rightControlPointer.setY(pointList.get(i + 1).getY() - BEZIER_RATIO * (pointList.get(i + 2).getY()
                        - pointList.get(i).getY()));

            } else if (i == pointList.size() - 2 && i > 1) {
                leftControlPointer.setX(pointList.get(i).getX() + BEZIER_RATIO * (pointList.get(i + 1).getX()
                        - pointList.get(i - 1).getX()));
                leftControlPointer.setY(pointList.get(i).getY() + BEZIER_RATIO * (pointList.get(i + 1).getY()
                        - pointList.get(i - 1).getY()));
                rightControlPointer.setX(pointList.get(i + 1).getX() - BEZIER_RATIO * (pointList.get(i + 1).getX()
                        - pointList.get(i).getX()));
                rightControlPointer.setY(pointList.get(i + 1).getY() - BEZIER_RATIO * (pointList.get(i + 1).getY()
                        - pointList.get(i).getY()));

            } else if (i > 0 && i < pointList.size() - 2) {
                leftControlPointer.setX(pointList.get(i).getX() + BEZIER_RATIO * (pointList.get(i + 1).getX()
                        - pointList.get(i - 1).getX()));
                leftControlPointer.setY(pointList.get(i).getY() + BEZIER_RATIO * (pointList.get(i + 1).getY()
                        - pointList.get(i - 1).getY()));
                rightControlPointer.setX(pointList.get(i + 1).getX() - BEZIER_RATIO * (pointList.get(i + 2).getX()
                        - pointList.get(i).getX()));
                rightControlPointer.setY(pointList.get(i + 1).getY() - BEZIER_RATIO * (pointList.get(i + 2).getY()
                        - pointList.get(i).getY()));
            }

            if (i < pointList.size() - 1) {
                path.cubicTo(leftControlPointer.getX(), leftControlPointer.getY(),
                        rightControlPointer.getX(), rightControlPointer.getY(),
                        pointList.get(i + 1).getX(), pointList.get(i + 1).getY());
            }
        }
    }

    public static void setLinePath(List<Pointer> pointerList, Path path){
        if (pointerList == null || pointerList.size() <= 1){
            return;
        }
        path.reset();
        path.moveTo(pointerList.get(0).getX(), pointerList.get(0).getY());
        for (int i = 1; i < pointerList.size(); i++) {
            path.lineTo(pointerList.get(i).getX(), pointerList.get(i).getY());
        }

    }


}
