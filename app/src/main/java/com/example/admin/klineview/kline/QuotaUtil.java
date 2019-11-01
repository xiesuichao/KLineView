package com.example.admin.klineview.kline;

import android.graphics.Path;
import android.support.annotation.NonNull;

import com.example.admin.klineview.Print;

import java.util.ArrayList;
import java.util.List;

/**
 * 五项数据计算公式
 * Created by xiesuichao on 2018/8/12.
 */

public class QuotaUtil {

    private static final int QUOTA_DAY5 = 5;
    private static final int QUOTA_DAY10 = 10;
    private static final int QUOTA_DAY30 = 30;
    private static final float BEZIER_RATIO = 0.16f;
    private static List<KData> cacheList = new ArrayList<>();

    /**
     * 初始化 MA5，MA10, MA30
     *
     * @param isEndData 是否是添加到list末尾的数据
     */
    public static void initMa(List<KData> dataList, boolean isEndData) {
        long startMa = System.currentTimeMillis();
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        cacheList.clear();
        cacheList.addAll(dataList);
        for (int i = 0; i < dataList.size(); i++) {
            if (i + QUOTA_DAY5 <= dataList.size()) {
                //priceMa5
                dataList.get(i + QUOTA_DAY5 - 1).setPriceMa5(getPriceMa(cacheList.subList(i, i + QUOTA_DAY5)));
                //volumeMa5
                dataList.get(i + QUOTA_DAY5 - 1).setVolumeMa5(getVolumeMa(cacheList.subList(i, i + QUOTA_DAY5)));
            }
            if (i + QUOTA_DAY10 <= dataList.size()) {
                //priceMa10
                dataList.get(i + QUOTA_DAY10 - 1).setPriceMa10(getPriceMa(cacheList.subList(i, i + QUOTA_DAY10)));
                //volumeMa10
                dataList.get(i + QUOTA_DAY10 - 1).setVolumeMa10(getVolumeMa(cacheList.subList(i, i + QUOTA_DAY10)));
            }
            if (i + QUOTA_DAY30 <= dataList.size()) {
                //priceMa30
                if (dataList.get(i + QUOTA_DAY30 - 1).getPriceMa30() != 0 && !isEndData) {
                    break;
                } else {
                    dataList.get(i + QUOTA_DAY30 - 1).setPriceMa30(getPriceMa(cacheList.subList(i, i + QUOTA_DAY30)));
                }
            }
            dataList.get(i).setInitFinish(true);
        }
        long endMa = System.currentTimeMillis();
        Print.log("MA", endMa - startMa);
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
     */
    public static void initEma(List<KData> dataList, boolean isEndData) {
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
            if (dataList.get(i).getEma30() != 0 && !isEndData) {
                break;
            }
            double currentEma5 = 2 * (dataList.get(i).getClosePrice() - lastEma5) / (QUOTA_DAY5 + 1) + lastEma5;
            double currentEma10 = 2 * (dataList.get(i).getClosePrice() - lastEma10) / (QUOTA_DAY10 + 1) + lastEma10;
            double currentEma30 = 2 * (dataList.get(i).getClosePrice() - lastEma30) / (QUOTA_DAY30 + 1) + lastEma30;

            dataList.get(i).setEma5(currentEma5);
            dataList.get(i).setEma10(currentEma10);
            dataList.get(i).setEma30(currentEma30);

            lastEma5 = currentEma5;
            lastEma10 = currentEma10;
            lastEma30 = currentEma30;
        }
        long endEma = System.currentTimeMillis();
        Print.log("EMA", endEma - startEma);
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
     * @param dataList 数据集合
     * @param period   周期，一般为26
     * @param k        参数，可根据股票的特性来做相应的调整，一般默认为2
     */
    public static void initBOLL(List<KData> dataList, int period, int k, boolean isEndData) {
        long startBoll = System.currentTimeMillis();
        if (dataList == null || dataList.isEmpty()
                || period < 0 || period > dataList.size() - 1) {
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

        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getBollMb() != 0 && !isEndData) {
                break;
            }
            KData quotes = dataList.get(i);
            sum += quotes.getClosePrice();
            sum2 += quotes.getClosePrice();
            if (i > period - 1)
                sum -= dataList.get(i - period).getClosePrice();
            if (i > period - 2)
                sum2 -= dataList.get(i - period + 1).getClosePrice();

            //这个范围不计算，在View上的反应就是不显示这个范围的boll线
            if (i < period - 1)
                continue;

            ma = sum / period;
            ma2 = sum2 / (period - 1);
            md = 0;
            for (int j = i + 1 - period; j <= i; j++) {
                //n-1日
                md += Math.pow(dataList.get(j).getClosePrice() - ma, 2);
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
        Print.log("BOLL", endBoll - startBoll);
    }

    public static void initBoll(List<KData> dataList, boolean isEndData) {
        initBOLL(dataList, 26, 2, isEndData);
    }

    /**
     * MACD
     *
     * @param dataList
     * @param fastPeriod   日快线移动平均，标准为12，按照标准即可
     * @param slowPeriod   日慢线移动平均，标准为26，可理解为天数
     * @param signalPeriod 日移动平均，标准为9，按照标准即可
     */
    public static void initMACD(List<KData> dataList, int fastPeriod, int slowPeriod, int signalPeriod, boolean isEndData) {
        long startMacd = System.currentTimeMillis();
        if (dataList == null || dataList.isEmpty()) {
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
            if (dataList.get(i).getMacd() != 0 && !isEndData) {
                break;
            }
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
        Print.log("MACD", endMacd - startMacd);
    }

    public static void initMACD(List<KData> dataList, boolean isEndData) {
        initMACD(dataList, 12, 26, 9, isEndData);
    }

    /**
     * KDJ
     *
     * @param n1 标准值9
     * @param n2 标准值3
     * @param n3 标准值3
     */
    public static void initKDJ(List<KData> dataList, int n1, int n2, int n3, boolean isEndData) {
        long startKdj = System.currentTimeMillis();
        if (dataList == null || dataList.isEmpty()) {
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
            if (dataList.get(i).getK() != 0 && !isEndData) {
                break;
            }
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
                if (i > (n1 - 1)) {
                    //判断存储的最高价是否高于当前最高价
                    if (highValue > dataList.get(i).getMaxPrice()) {
                        //判断最高价是不是在最近n天内，若不在最近n天内，则从最近n天找出最高价并赋值
                        if (highPosition < (i - (n1 - 1))) {
                            highValue = dataList.get(i - (n1 - 1)).getMaxPrice();
                            for (int j = (i - (n1 - 2)); j <= i; j++) {
                                if (highValue <= dataList.get(j).getMaxPrice()) {
                                    highValue = dataList.get(j).getMaxPrice();
                                    highPosition = j;
                                }
                            }
                        }
                    }
                    if ((lowValue < dataList.get(i).getMinPrice())) {
                        if (lowPosition < i - (n1 - 1)) {
                            lowValue = dataList.get(i).getMinPrice();
                            for (int k = i - (n1 - 2); k <= i; k++) {
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
                mK[i] = (mK[i - 1] * (n2 - 1) + rSV) / n2;
                mD[i] = (mD[i - 1] * (n3 - 1) + mK[i]) / n3;
                jValue = 3 * mK[i] - 2 * mD[i];
            }
            dataList.get(i).setK(mK[i]);
            dataList.get(i).setD(mD[i]);
            dataList.get(i).setJ(jValue);
        }
        long endKdj = System.currentTimeMillis();
        Print.log("KDJ", endKdj - startKdj);
    }

    public static void initKDJ(List<KData> dataList, boolean isEndData) {
        initKDJ(dataList, 9, 3, 3, isEndData);
    }

    /**
     * 初始化RSI
     *
     * @param period1 标准值6
     * @param period2 标准值12
     * @param period3 标准值24
     */
    public static void initRSI(List<KData> dataList, int period1, int period2, int period3, boolean isEndData) {
        long startRSI = System.currentTimeMillis();
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        double upRateSum;
        int upRateCount;
        double dnRateSum;
        int dnRateCount;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getRs3() != 0 && !isEndData) {
                break;
            }
            upRateSum = 0;
            upRateCount = 0;
            dnRateSum = 0;
            dnRateCount = 0;
            if (i >= period1 - 1) {
                for (int x = i; x >= i + 1 - period1; x--) {
                    if (dataList.get(x).getUpDnRate() >= 0) {
                        upRateSum += dataList.get(x).getUpDnRate();
                        upRateCount++;
                    } else {
                        dnRateSum += dataList.get(x).getUpDnRate();
                        dnRateCount++;
                    }
                }
                double avgUpRate = 0;
                double avgDnRate = 0;
                if (upRateSum > 0) {
                    avgUpRate = upRateSum / upRateCount;
                }
                if (dnRateSum < 0) {
                    avgDnRate = dnRateSum / dnRateCount;
                }
                dataList.get(i).setRs1(avgUpRate / (avgUpRate + Math.abs(avgDnRate)) * 100);
            }

            upRateSum = 0;
            upRateCount = 0;
            dnRateSum = 0;
            dnRateCount = 0;
            if (i >= period2 - 1) {
                for (int x = i; x >= i + 1 - period2; x--) {
                    if (dataList.get(x).getUpDnRate() >= 0) {
                        upRateSum += dataList.get(x).getUpDnRate();
                        upRateCount++;
                    } else {
                        dnRateSum += dataList.get(x).getUpDnRate();
                        dnRateCount++;
                    }
                }
                double avgUpRate = 0;
                double avgDnRate = 0;
                if (upRateSum > 0) {
                    avgUpRate = upRateSum / upRateCount;
                }
                if (dnRateSum < 0) {
                    avgDnRate = dnRateSum / dnRateCount;
                }
                dataList.get(i).setRs2(avgUpRate / (avgUpRate + Math.abs(avgDnRate)) * 100);
            }

            upRateSum = 0;
            upRateCount = 0;
            dnRateSum = 0;
            dnRateCount = 0;
            if (i >= period3 - 1) {
                for (int x = i; x >= i + 1 - period3; x--) {
                    if (dataList.get(x).getUpDnRate() >= 0) {
                        upRateSum += dataList.get(x).getUpDnRate();
                        upRateCount++;
                    } else {
                        dnRateSum += dataList.get(x).getUpDnRate();
                        dnRateCount++;
                    }
                }
                double avgUpRate = 0;
                double avgDnRate = 0;
                if (upRateSum > 0) {
                    avgUpRate = upRateSum / upRateCount;
                }
                if (dnRateSum < 0) {
                    avgDnRate = dnRateSum / dnRateCount;
                }
                dataList.get(i).setRs3(avgUpRate / (avgUpRate + Math.abs(avgDnRate)) * 100);
            }
        }
        long endRSI = System.currentTimeMillis();
        Print.log("RSI", endRSI - startRSI);
    }

    public static void initRSI(List<KData> dataList, boolean isEndData) {
        initRSI(dataList, 6, 12, 24, isEndData);
    }

    /**
     * 三阶贝塞尔曲线控制点
     *
     * @param pointList
     * @param path
     */
    public static void setBezierPath(List<Pointer> pointList, Path path) {
        if (path == null){
            return;
        }
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

    public static void setLinePath(List<Pointer> pointerList, Path path) {
        if (path == null){
            return;
        }
        if (pointerList == null || pointerList.size() <= 1) {
            return;
        }
        path.moveTo(pointerList.get(0).getX(), pointerList.get(0).getY());
        for (int i = 1; i < pointerList.size(); i++) {
            path.lineTo(pointerList.get(i).getX(), pointerList.get(i).getY());
        }

    }


}
