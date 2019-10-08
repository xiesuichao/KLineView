package com.example.admin.klineview.depth;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.admin.klineview.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 深度控件
 * Created by xiesuichao on 2018/9/23.
 */

public class DepthView extends View {

    //是否显示详情
    private boolean isShowDetail = false;
    //是否长按
    private boolean isLongPress = false;
    //是否显示竖线
    private boolean isShowDetailLine = true;
    //手指单击松开后，数据是否继续显示
    private boolean isShowDetailSingleClick = true;
    //单击松开，数据延时消失，单位毫秒
    private final int DISAPPEAR_TIME = 500;
    //手指长按松开后，数据是否继续显示
    private boolean isShowDetailLongPress = true;
    //长按触发时长，单位毫秒
    private final int LONG_PRESS_TIME_OUT = 300;
    //横坐标中间值
    private double abscissaCenterPrice = -1;
    private boolean isHorizontalMove;
    private Depth clickDepth;
    private String detailPriceTitle;
    private String detailVolumeTitle;
    private Paint strokePaint, fillPaint;
    private Rect textRect;
    private Path linePath;
    private List<Depth> buyDataList, sellDataList;
    private double maxVolume, avgVolumeSpace, avgOrdinateSpace, depthImgHeight;
    private float leftStart, topStart, rightEnd, bottomEnd, longPressDownX, longPressDownY,
            singleClickDownX, singleClickDownY, detailLineWidth, dispatchDownX;
    private int buyLineCol, buyBgCol, sellLineCol, sellBgCol, ordinateTextCol, ordinateTextSize,
            abscissaTextCol, abscissaTextSize, detailBgCol, detailTextCol, detailTextSize, ordinateNum,
            buyLineStrokeWidth, sellLineStrokeWidth, detailLineCol, detailPointRadius, pricePrecision,
            moveLimitDistance;
    private Runnable longPressRunnable;
    private Runnable singleClickDisappearRunnable;
    private String leftPriceStr;
    private String rightPriceStr;

    public DepthView(Context context) {
        this(context, null);
    }

    public DepthView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DepthView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 设置购买数据
     */
    public void setBuyDataList(List<Depth> depthList) {
        buyDataList.clear();
        buyDataList.addAll(depthList);
        //如果数据是无序的，则按价格进行排序。如果是有序的，则注释掉
        Collections.sort(buyDataList);
        //计算累积交易量
        for (int i = buyDataList.size() - 1; i >= 0; i--) {
            if (i < buyDataList.size() - 1) {
                buyDataList.get(i).setVolume(buyDataList.get(i).getVolume() + buyDataList.get(i + 1).getVolume());
            }
        }
        requestLayout();
        invalidate();
    }

    /**
     * 设置出售数据
     */
    public void setSellDataList(List<Depth> depthList) {
        sellDataList.clear();
        sellDataList.addAll(depthList);
        //如果数据是无序的，则按价格进行排序。如果是有序的，则注释掉
        Collections.sort(sellDataList);
        //计算累积交易量
        for (int i = 0; i < sellDataList.size(); i++) {
            if (i > 0) {
                sellDataList.get(i).setVolume(sellDataList.get(i).getVolume() + sellDataList.get(i - 1).getVolume());
            }
        }
        requestLayout();
        invalidate();
    }

    /**
     * 重置深度数据
     */
    public void resetAllData(List<Depth> buyDataList, List<Depth> sellDataList) {
        setBuyDataList(buyDataList);
        setSellDataList(sellDataList);
        isShowDetail = false;
        isLongPress = false;
        requestLayout();
    }

    /**
     * 设置横坐标中间值
     */
    public void setAbscissaCenterPrice(double centerPrice) {
        this.abscissaCenterPrice = centerPrice;
    }

    /**
     * 是否显示竖线
     */
    public void setShowDetailLine(boolean isShowLine) {
        this.isShowDetailLine = isShowLine;
    }

    /**
     * 手指单击松开后，数据是否继续显示
     */
    public void setShowDetailSingleClick(boolean isShowDetailSingleClick) {
        this.isShowDetailSingleClick = isShowDetailSingleClick;
    }

    /**
     * 手指长按松开后，数据是否继续显示
     */
    public void setShowDetailLongPress(boolean isShowDetailLongPress) {
        this.isShowDetailLongPress = isShowDetailLongPress;
    }

    /**
     * 设置横坐标价钱小数位精度
     */
    public void setPricePrecision(int pricePrecision) {
        this.pricePrecision = pricePrecision;
    }

    /**
     * 设置数据详情的价钱说明
     */
    public void setDetailPriceTitle(String priceTitle) {
        this.detailPriceTitle = priceTitle;
    }

    /**
     * 设置数据详情的数量说明
     */
    public void setDetailVolumeTitle(String volumeTitle) {
        this.detailVolumeTitle = volumeTitle;
    }

    /**
     * 移除runnable
     */
    public void cancelCallback() {
        removeCallbacks(longPressRunnable);
        removeCallbacks(singleClickDisappearRunnable);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DepthView);
            buyLineCol = typedArray.getColor(R.styleable.DepthView_dvBuyLineCol, 0xff2BB8AB);
            buyLineStrokeWidth = typedArray.getInt(R.styleable.DepthView_dvBuyLineStrokeWidth, 1);
            buyBgCol = typedArray.getColor(R.styleable.DepthView_dvBuyBgCol, 0x662BB8AB);
            sellLineCol = typedArray.getColor(R.styleable.DepthView_dvSellLineCol, 0xffFF5442);
            sellLineStrokeWidth = typedArray.getInt(R.styleable.DepthView_dvSellLineStrokeWidth, 1);
            sellBgCol = typedArray.getColor(R.styleable.DepthView_dvSellBgCol, 0x66FF5442);
            ordinateTextCol = typedArray.getColor(R.styleable.DepthView_dvOrdinateCol, 0xff808F9E);
            ordinateTextSize = typedArray.getInt(R.styleable.DepthView_dvOrdinateTextSize, 8);
            ordinateNum = typedArray.getInt(R.styleable.DepthView_dvOrdinateNum, 5);
            abscissaTextCol = typedArray.getColor(R.styleable.DepthView_dvAbscissaCol, ordinateTextCol);
            abscissaTextSize = typedArray.getInt(R.styleable.DepthView_dvAbscissaTextSize, ordinateTextSize);
            detailBgCol = typedArray.getColor(R.styleable.DepthView_dvDetailBgCol, 0x99F3F4F6);
            detailTextCol = typedArray.getColor(R.styleable.DepthView_dvDetailTextCol, 0xff294058);
            detailTextSize = typedArray.getInt(R.styleable.DepthView_dvDetailTextSize, 10);
            detailLineCol = typedArray.getColor(R.styleable.DepthView_dvDetailLineCol, 0xff828EA2);
            detailLineWidth = typedArray.getFloat(R.styleable.DepthView_dvDetailLineWidth, 0);
            detailPointRadius = typedArray.getInt(R.styleable.DepthView_dvDetailPointRadius, 3);
            detailPriceTitle = typedArray.getString(R.styleable.DepthView_dvDetailPriceTitle);
            detailVolumeTitle = typedArray.getString(R.styleable.DepthView_dvDetailVolumeTitle);
            typedArray.recycle();
        }

        buyDataList = new ArrayList<>();
        sellDataList = new ArrayList<>();

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);

        fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.FILL);

        textRect = new Rect();
        linePath = new Path();

        pricePrecision = 8;

        if (TextUtils.isEmpty(detailPriceTitle)) {
            detailPriceTitle = "价格(BTC)：";
        }
        if (TextUtils.isEmpty(detailVolumeTitle)) {
            detailVolumeTitle = "累积交易量：";
        }

        moveLimitDistance = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                isLongPress = true;
                isShowDetail = true;
                getClickDepth(longPressDownX);
                invalidate();
            }
        };
        singleClickDisappearRunnable = new Runnable() {
            @Override
            public void run() {
                isShowDetail = false;
                invalidate();
            }
        };

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        leftStart = getPaddingLeft() + 1;
        topStart = getPaddingTop() + 1;
        rightEnd = getMeasuredWidth() - getPaddingRight() - 1;
        bottomEnd = getMeasuredHeight() - getPaddingBottom() - 1;

        double maxBuyVolume;
        double minBuyVolume;
        double maxSellVolume;
        double minSellVolume;

        if (!buyDataList.isEmpty()) {
            maxBuyVolume = buyDataList.get(0).getVolume();
            minBuyVolume = buyDataList.get(buyDataList.size() - 1).getVolume();
        } else {
            maxBuyVolume = minBuyVolume = 0;
        }

        if (!sellDataList.isEmpty()) {
            maxSellVolume = sellDataList.get(sellDataList.size() - 1).getVolume();
            minSellVolume = sellDataList.get(0).getVolume();
        } else {
            maxSellVolume = minSellVolume = 0;
        }

        maxVolume = Math.max(maxBuyVolume, maxSellVolume);
        double minVolume = Math.min(minBuyVolume, minSellVolume);

        resetStrokePaint(abscissaTextCol, abscissaTextSize, 0);

        if (!buyDataList.isEmpty()) {
            leftPriceStr = setPrecision(buyDataList.get(0).getPrice(), pricePrecision);
        } else if (!sellDataList.isEmpty()) {
            leftPriceStr = setPrecision(sellDataList.get(0).getPrice(), pricePrecision);
        } else {
            leftPriceStr = "0";
        }

        if (!sellDataList.isEmpty()) {
            rightPriceStr = setPrecision(sellDataList.get(sellDataList.size() - 1).getPrice(), pricePrecision);
        } else if (!buyDataList.isEmpty()) {
            rightPriceStr = setPrecision(buyDataList.get(buyDataList.size() - 1).getPrice(), pricePrecision);
        } else {
            rightPriceStr = "0";
        }

        strokePaint.getTextBounds(leftPriceStr, 0, leftPriceStr.length(), textRect);
        depthImgHeight = bottomEnd - topStart - textRect.height() - dp2px(4);
        double avgHeightPerVolume = depthImgHeight / (maxVolume - minVolume);
        double avgWidthPerSize = (rightEnd - leftStart) / (buyDataList.size() + sellDataList.size());
        avgVolumeSpace = maxVolume / ordinateNum;
        avgOrdinateSpace = depthImgHeight / ordinateNum;

        for (int i = 0; i < buyDataList.size(); i++) {
            buyDataList.get(i).setX(leftStart + (float) avgWidthPerSize * i);
            buyDataList.get(i).setY(topStart + (float) ((maxVolume - buyDataList.get(i).getVolume()) * avgHeightPerVolume));
        }

        for (int i = sellDataList.size() - 1; i >= 0; i--) {
            sellDataList.get(i).setX(rightEnd - (float) (avgWidthPerSize * (sellDataList.size() - 1 - i)));
            sellDataList.get(i).setY(topStart + (float) ((maxVolume - sellDataList.get(i).getVolume()) * avgHeightPerVolume));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (buyDataList.isEmpty() && sellDataList.isEmpty()) {
            return;
        }
        drawLineAndBg(canvas);
        drawCoordinateValue(canvas);
        drawDetailData(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            longPressDownX = event.getX();
            longPressDownY = event.getY();
            dispatchDownX = event.getX();
            isLongPress = false;
            postDelayed(longPressRunnable, LONG_PRESS_TIME_OUT);

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //长按控制
            float dispatchMoveX = event.getX();
            float dispatchMoveY = event.getY();
            float diffDispatchMoveX = Math.abs(dispatchMoveX - longPressDownX);
            float diffDispatchMoveY = Math.abs(dispatchMoveY - longPressDownY);
            float moveDistanceX = Math.abs(event.getX() - dispatchDownX);

            getParent().requestDisallowInterceptTouchEvent(true);

            if (isHorizontalMove || (diffDispatchMoveX > diffDispatchMoveY + dp2px(5)
                    && diffDispatchMoveX > moveLimitDistance)) {
                isHorizontalMove = true;
                removeCallbacks(longPressRunnable);

                if (isLongPress && moveDistanceX > 2) {
                    getClickDepth(event.getX());
                    if (clickDepth != null) {
                        invalidate();
                    }
                }
                dispatchDownX = event.getX();
                return isLongPress || super.dispatchTouchEvent(event);

            } else if (!isHorizontalMove && diffDispatchMoveY > diffDispatchMoveX + dp2px(5)
                    && diffDispatchMoveY > moveLimitDistance) {
                removeCallbacks(longPressRunnable);
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            isHorizontalMove = false;
            removeCallbacks(longPressRunnable);
            if (!isShowDetailLongPress) {
                isShowDetail = false;
                invalidate();
            }
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        return isLongPress || super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                singleClickDownX = event.getX();
                singleClickDownY = event.getY();
                break;

            case MotionEvent.ACTION_UP:
                float diffTouchMoveX = event.getX() - singleClickDownX;
                float diffTouchMoveY = event.getY() - singleClickDownY;
                if (diffTouchMoveY < moveLimitDistance && diffTouchMoveX < moveLimitDistance) {
                    isShowDetail = true;
                    getClickDepth(singleClickDownX);
                    if (clickDepth != null) {
                        invalidate();
                    }
                }
                if (!isShowDetailSingleClick) {
                    postDelayed(singleClickDisappearRunnable, DISAPPEAR_TIME);
                }
                break;
        }
        return true;
    }

    //获取单击位置数据
    private void getClickDepth(float clickX) {
        clickDepth = null;
        if (sellDataList.isEmpty()) {
            for (int i = 0; i < buyDataList.size(); i++) {
                if (i + 1 < buyDataList.size() && clickX >= buyDataList.get(i).getX()
                        && clickX < buyDataList.get(i + 1).getX()) {
                    clickDepth = buyDataList.get(i);
                    break;
                } else if (i == buyDataList.size() - 1 && clickX >= buyDataList.get(i).getX()) {
                    clickDepth = buyDataList.get(i);
                    break;
                }
            }
        } else if (clickX < sellDataList.get(0).getX()) {
            for (int i = 0; i < buyDataList.size(); i++) {
                if (i + 1 < buyDataList.size() && clickX >= buyDataList.get(i).getX()
                        && clickX < buyDataList.get(i + 1).getX()) {
                    clickDepth = buyDataList.get(i);
                    break;
                } else if (i == buyDataList.size() - 1 && clickX >= buyDataList.get(i).getX()
                        && clickX < sellDataList.get(0).getX()) {
                    clickDepth = buyDataList.get(i);
                    break;
                }
            }
        } else {
            for (int i = 0; i < sellDataList.size(); i++) {
                if (i + 1 < sellDataList.size() && clickX >= sellDataList.get(i).getX()
                        && clickX < sellDataList.get(i + 1).getX()) {
                    clickDepth = sellDataList.get(i);
                    break;
                } else if (i == sellDataList.size() - 1 && clickX >= sellDataList.get(i).getX()) {
                    clickDepth = sellDataList.get(i);
                    break;
                }
            }
        }
    }

    //坐标轴
    private void drawCoordinateValue(Canvas canvas) {
        //横轴
        resetStrokePaint(abscissaTextCol, abscissaTextSize, 0);

        strokePaint.getTextBounds(rightPriceStr, 0, rightPriceStr.length(), textRect);
        //左边价格
        canvas.drawText(leftPriceStr,
                leftStart,
                bottomEnd - dp2px(2),
                strokePaint);
        //右边价格
        canvas.drawText(rightPriceStr,
                rightEnd - textRect.width(),
                bottomEnd - dp2px(2),
                strokePaint);
        //中间价格
        if (abscissaCenterPrice != -1) {
            canvas.drawText(setPrecision(abscissaCenterPrice, pricePrecision),
                    getWidth() / 2 - strokePaint.measureText(setPrecision(abscissaCenterPrice, pricePrecision)) / 2,
                    bottomEnd - dp2px(2),
                    strokePaint);
        }

        //纵轴
        resetStrokePaint(ordinateTextCol, ordinateTextSize, 0);
        strokePaint.getTextBounds(maxVolume + "", 0, (maxVolume + "").length(), textRect);
        for (int i = 0; i < ordinateNum; i++) {
            String ordinateStr = formatNum(maxVolume - i * avgVolumeSpace);
            canvas.drawText(ordinateStr,
                    rightEnd - strokePaint.measureText(ordinateStr),
                    (float) (topStart + textRect.height() + i * avgOrdinateSpace),
                    strokePaint);
        }
    }

    private void drawLineAndBg(Canvas canvas) {
        //买方背景
        if (!buyDataList.isEmpty()) {
            linePath.reset();
            for (int i = 0; i < buyDataList.size(); i++) {
                if (i == 0) {
                    linePath.moveTo(buyDataList.get(i).getX(), buyDataList.get(i).getY());
                } else {
                    linePath.lineTo(buyDataList.get(i).getX(), buyDataList.get(i).getY());
                }
            }
            if (!buyDataList.isEmpty() && buyDataList.get(buyDataList.size() - 1).getY() < topStart + depthImgHeight) {
                linePath.lineTo(buyDataList.get(buyDataList.size() - 1).getX(), (float) (topStart + depthImgHeight));
            }
            linePath.lineTo(leftStart, (float) (topStart + depthImgHeight));
            linePath.close();
            fillPaint.setColor(buyBgCol);
            canvas.drawPath(linePath, fillPaint);

            //买方线条
            linePath.reset();
            for (int i = 0; i < buyDataList.size(); i++) {
                if (i == 0) {
                    linePath.moveTo(buyDataList.get(i).getX(), buyDataList.get(i).getY());
                } else {
                    linePath.lineTo(buyDataList.get(i).getX(), buyDataList.get(i).getY());
                }
            }
            resetStrokePaint(buyLineCol, 0, buyLineStrokeWidth);
            canvas.drawPath(linePath, strokePaint);
        }

        //卖方背景
        if (!sellDataList.isEmpty()) {
            linePath.reset();
            for (int i = sellDataList.size() - 1; i >= 0; i--) {
                if (i == sellDataList.size() - 1) {
                    linePath.moveTo(sellDataList.get(i).getX(), sellDataList.get(i).getY());
                } else {
                    linePath.lineTo(sellDataList.get(i).getX(), sellDataList.get(i).getY());
                }
            }
            if (!sellDataList.isEmpty() && sellDataList.get(0).getY() < (float) (topStart + depthImgHeight)) {
                linePath.lineTo(sellDataList.get(0).getX(), (float) (topStart + depthImgHeight));
            }
            linePath.lineTo(rightEnd, (float) (topStart + depthImgHeight));
            linePath.close();
            fillPaint.setColor(sellBgCol);
            canvas.drawPath(linePath, fillPaint);

            //卖方线条
            linePath.reset();
            for (int i = 0; i < sellDataList.size(); i++) {
                if (i == 0) {
                    linePath.moveTo(sellDataList.get(i).getX(), sellDataList.get(i).getY());
                } else {
                    linePath.lineTo(sellDataList.get(i).getX(), sellDataList.get(i).getY());
                }
            }
            resetStrokePaint(sellLineCol, 0, sellLineStrokeWidth);
            canvas.drawPath(linePath, strokePaint);
        }

    }

    private void drawDetailData(Canvas canvas) {
        if (!isShowDetail || clickDepth == null) {
            return;
        }
        //游标线
        if (isShowDetailLine) {
            resetStrokePaint(detailLineCol, 0, detailLineWidth);
            canvas.drawLine(clickDepth.getX(), topStart, clickDepth.getX(), topStart + (float) depthImgHeight,
                    strokePaint);
        }

        if (sellDataList.isEmpty() || clickDepth.getX() < sellDataList.get(0).getX()) {
            fillPaint.setColor(buyLineCol);
        } else if (buyDataList.isEmpty() || clickDepth.getX() >= sellDataList.get(0).getX()) {
            fillPaint.setColor(sellLineCol);
        }
        canvas.drawCircle(clickDepth.getX(), clickDepth.getY(), dp2px(detailPointRadius), fillPaint);

        resetStrokePaint(detailTextCol, detailTextSize, 0);
        fillPaint.setColor(detailBgCol);
        String clickPriceStr = detailPriceTitle + formatNum(clickDepth.getPrice());
        String clickVolumeStr = detailVolumeTitle + formatNum(clickDepth.getVolume());
        strokePaint.getTextBounds(clickPriceStr, 0, clickPriceStr.length(), textRect);
        int priceStrWidth = textRect.width();
        int priceStrHeight = textRect.height();
        strokePaint.getTextBounds(clickVolumeStr, 0, clickVolumeStr.length(), textRect);
        int volumeStrWidth = textRect.width();
        int maxWidth = Math.max(priceStrWidth, volumeStrWidth);

        float bgLeft, bgTop, bgRight, bgBottom, priceStrX, priceStrY, volumeStrY;

        if (clickDepth.getX() <= maxWidth + dp2px(15)) {
            bgLeft = clickDepth.getX() + dp2px(5);
            bgRight = clickDepth.getX() + dp2px(15) + maxWidth;
            priceStrX = clickDepth.getX() + dp2px(10);

        } else {
            bgLeft = clickDepth.getX() - dp2px(15) - maxWidth;
            bgRight = clickDepth.getX() - dp2px(5);
            priceStrX = clickDepth.getX() - dp2px(10) - maxWidth;
        }

        if (clickDepth.getY() < topStart + dp2px(7) + priceStrHeight) {
            bgTop = topStart;
            bgBottom = topStart + dp2px(14) + priceStrHeight * 2;
            priceStrY = topStart + dp2px(3) + priceStrHeight;
            volumeStrY = topStart + dp2px(7) + priceStrHeight * 2;

        } else if (clickDepth.getY() > topStart + depthImgHeight - dp2px(7) - priceStrHeight) {
            bgTop = topStart + (float) depthImgHeight - dp2px(14) - priceStrHeight * 2;
            bgBottom = topStart + (float) depthImgHeight;
            priceStrY = topStart + (float) depthImgHeight - dp2px(9) - priceStrHeight;
            volumeStrY = topStart + (float) depthImgHeight - dp2px(5);

        } else {
            bgTop = clickDepth.getY() - dp2px(10) - priceStrHeight;
            bgBottom = clickDepth.getY() + dp2px(10) + priceStrHeight;
            priceStrY = clickDepth.getY() - dp2px(2);
            volumeStrY = clickDepth.getY() + priceStrHeight;
        }

        RectF rectF = new RectF(bgLeft, bgTop, bgRight, bgBottom);
        canvas.drawRoundRect(rectF, 6, 6, fillPaint);
        canvas.drawText(clickPriceStr, priceStrX, priceStrY, strokePaint);
        canvas.drawText(clickVolumeStr, priceStrX, volumeStrY, strokePaint);

    }

    /**
     * 设置小数位精度
     *
     * @param num
     * @param scale 保留几位小数
     */
    private String setPrecision(Double num, int scale) {
        BigDecimal bigDecimal = new BigDecimal(num);
        return bigDecimal.setScale(scale, BigDecimal.ROUND_DOWN).toPlainString();
    }

    /**
     * 按量级格式化数量
     */
    private String formatNum(double num) {
        if (num < 1) {
            return setPrecision(num, 6);
        } else if (num < 10) {
            return setPrecision(num, 4);
        } else if (num < 100) {
            return setPrecision(num, 3);
        } else if (num < 10000) {
            return setPrecision(num / 1000, 1) + "K";
        } else {
            return setPrecision(num / 10000, 2) + "万";
        }
    }

    private void resetStrokePaint(int colorId, int textSize, float strokeWidth) {
        strokePaint.setColor(colorId);
        strokePaint.setTextSize(sp2px(textSize));
        strokePaint.setStrokeWidth(dp2px(strokeWidth));
    }

    private int dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


}
