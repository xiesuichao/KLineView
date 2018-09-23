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

import com.example.admin.klineview.PrintUtil;
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
    //能否显示长按
    private boolean canShowLongPress = true;
    private long dispatchDownTime;
    private Depth clickDepth;
    private String detailPriceTitle;
    private String detailVolumeTitle;
    private Paint strokePaint, fillPaint;
    private Rect textRect;
    private Path linePath;
    private List<Depth> buyDataList, sellDataList;
    private float leftStart, topStart, rightEnd, bottomEnd, dispatchDownX, dispatchDownY,
            singleClickDownX, touchDownY, detailLineWidth;
    private double maxVolume, avgVolumeSpace, avgOrdinateSpace, depthImgHeight;
    private int buyLineCol, buyBgCol, sellLineCol, sellBgCol, ordinateTextCol, ordinateTextSize,
            abscissaTextCol, abscissaTextSize, detailBgCol, detailTextCol, detailTextSize, ordinateNum,
            buyLineStrokeWidth, sellLineStrokeWidth, detailLineCol, detailPointRadius, pricePrecision,
            volumePrecision;

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
    }

    /**
     * 重置深度数据
     */
    public void resetAllData(List<Depth> buyDataList, List<Depth> sellDataList) {
        setBuyDataList(buyDataList);
        setSellDataList(sellDataList);
        isShowDetail = false;
        isLongPress = false;
        canShowLongPress = true;
        requestLayout();
    }

    public void setPricePrecision(int pricePrecision) {
        this.pricePrecision = pricePrecision;
    }

    public void setVolumePrecision(int volumePrecision) {
        this.volumePrecision = volumePrecision;
    }

    public void setDetailPriceTitle(String priceTitle) {
        this.detailPriceTitle = priceTitle;
    }

    public void setDetailVolumeTitle(String volumeTitle) {
        this.detailVolumeTitle = volumeTitle;
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

        pricePrecision = 4;
        volumePrecision = 4;
        if (TextUtils.isEmpty(detailPriceTitle)) {
            detailPriceTitle = "价格(BTC)：";
        }
        if (TextUtils.isEmpty(detailVolumeTitle)) {
            detailVolumeTitle = "累积交易量：";
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        leftStart = getPaddingLeft() + 1;
        topStart = getPaddingTop() + 1;
        rightEnd = getMeasuredWidth() - getPaddingRight() - 1;
        bottomEnd = getMeasuredHeight() - getPaddingBottom() - 1;

        maxVolume = Math.max(buyDataList.get(0).getVolume(), sellDataList.get(sellDataList.size() - 1).getVolume());
        double minVolume = Math.min(buyDataList.get(buyDataList.size() - 1).getVolume(), sellDataList.get(0).getVolume());

        resetStrokePaint(abscissaTextCol, abscissaTextSize, 0);
        String abscissaStr = buyDataList.get(0).getPrice() + "";
        strokePaint.getTextBounds(abscissaStr, 0, abscissaStr.length(), textRect);
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
        drawLineAndBg(canvas);
        drawCoordinateValue(canvas);
        drawDetailData(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            dispatchDownX = event.getX();
            dispatchDownY = event.getY();
            isLongPress = false;
            canShowLongPress = true;
            dispatchDownTime = event.getDownTime();

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //长按控制
            float dispatchMoveX = event.getX();
            float dispatchMoveY = event.getY();
            float diffDispatchMoveX = Math.abs(dispatchMoveX - dispatchDownX);
            float diffDispatchMoveY = Math.abs(dispatchMoveY - dispatchDownY);
            if (diffDispatchMoveX > 5 || diffDispatchMoveY > 5) {
                canShowLongPress = false;
            }
            if (canShowLongPress && !isLongPress
                    && event.getEventTime() - dispatchDownTime > 400
                    && diffDispatchMoveX < 1 && diffDispatchMoveY < 1) {
                isLongPress = true;
                isShowDetail = true;
                singleClickDownX = event.getX();
                getClickDepth(event.getX());
                invalidate();
            }

            if (diffDispatchMoveX > 1) {
                if (isLongPress) {
                    singleClickDownX = event.getX();
                    getClickDepth(event.getX());
                    invalidate();
                }
            }
        }
        return isLongPress || super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                singleClickDownX = event.getX();
                touchDownY = event.getY();
                break;

            case MotionEvent.ACTION_UP:
                float diffTouchMoveX = event.getX() - singleClickDownX;
                float diffTouchMoveY = event.getY() - touchDownY;
                if (diffTouchMoveY <= diffTouchMoveX && diffTouchMoveX < 5) {
                    getClickDepth(event.getX());
                    isShowDetail = true;
                    invalidate();
                }
                break;
        }
        return true;
    }

    private void getClickDepth(float clickX) {
        if (clickX < sellDataList.get(0).getX()) {
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

    private void drawCoordinateValue(Canvas canvas) {
        //横轴
        resetStrokePaint(abscissaTextCol, abscissaTextSize, 0);
        String endPriceStr = formatNum(sellDataList.get(sellDataList.size() - 1).getPrice());
        strokePaint.getTextBounds(endPriceStr, 0, endPriceStr.length(), textRect);
        canvas.drawText(formatNum(buyDataList.get(0).getPrice()),
                leftStart,
                bottomEnd - dp2px(2),
                strokePaint);
        canvas.drawText(endPriceStr,
                rightEnd - textRect.width(),
                bottomEnd - dp2px(2),
                strokePaint);

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
        linePath.reset();
        for (int i = 0; i < buyDataList.size(); i++) {
            if (i == 0) {
                linePath.moveTo(buyDataList.get(i).getX(), buyDataList.get(i).getY());
            } else {
                linePath.lineTo(buyDataList.get(i).getX(), buyDataList.get(i).getY());
            }
        }
        if (buyDataList.get(buyDataList.size() - 1).getY() < topStart + depthImgHeight) {
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

        //卖方背景
        linePath.reset();
        for (int i = sellDataList.size() - 1; i >= 0; i--) {
            if (i == sellDataList.size() - 1) {
                linePath.moveTo(sellDataList.get(i).getX(), sellDataList.get(i).getY());
            } else {
                linePath.lineTo(sellDataList.get(i).getX(), sellDataList.get(i).getY());
            }
        }
        if (sellDataList.get(0).getY() < (float) (topStart + depthImgHeight)) {
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

    private void drawDetailData(Canvas canvas) {
        if (!isShowDetail || clickDepth == null) {
            return;
        }
        //游标线
        resetStrokePaint(detailLineCol, 0, detailLineWidth);
        canvas.drawLine(clickDepth.getX(), topStart, clickDepth.getX(), topStart + (float) depthImgHeight,
                strokePaint);

        if (clickDepth.getX() < sellDataList.get(0).getX()) {
            fillPaint.setColor(buyLineCol);
        } else {
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

    private String setPrecision(Double num, int scale) {
        BigDecimal bigDecimal = new BigDecimal(num);
        return bigDecimal.setScale(scale, BigDecimal.ROUND_DOWN).toPlainString();
    }

    private String formatNum(double num) {
        if (num < 1) {
            return setPrecision(num, 6);
        } else if (num < 10) {
            return setPrecision(num, 4);
        } else if (num < 100) {
            return setPrecision(num, 3);
        } else if (num < 10000) {
            return setPrecision(num, 2);
        } else {
            return setPrecision(num / 10000, 2) + "万";
        }
    }

}
