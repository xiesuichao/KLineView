package com.example.admin.klineview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 股票走势图 K线控件
 * Created by xiesuichao on 2018/6/29.
 */

public class KLineView extends View implements View.OnTouchListener, Handler.Callback {

    //是否显示副图
    private boolean isShowDeputy = false;
    //是否显示详情
    private boolean isShowDetail = false;
    //是否长按
    private boolean isLongPress = false;
    //是否需要请求前面的数据
    private boolean isNeedRequestBeforeData = true;
    //是否双指触控
    private boolean isDoubleFinger = false;
    //主图数据类型 0:MA, 1:EMA 2:BOLL
    private int mainImgType = 0;
    public static final int MAIN_IMG_MA = 0;
    public static final int MAIN_IMG_EMA = 1;
    public static final int MAIN_IMG_BOLL = 2;
    //副图数据类型 0:MACD, 1:KDJ
    private int deputyImgType = 0;
    public static final int DEPUTY_IMG_MACD = 0;
    public static final int DEPUTY_IMG_KDJ = 1;

    private final String mMa5 = "Ma5:";
    private final String mMa10 = "Ma10:";
    private final String mMa30 = "Ma30:";
    private final String mVol = "VOL:";
    private final String mMacdTitle = "MACD(12,26,9)";
    private final String mMacd = "MACD:";
    private final String mDif = "DIF:";
    private final String mDea = "DEA:";
    private final String mKdjTitle = "KDJ(9,3,3)";
    private final String mK = "K:";
    private final String mD = "D:";
    private final String mJ = "J:";

    private int maxViewDataNum = 34;
    private int startDataNum = 0;
    private int initTotalListSize = 0;

    private int priceIncreaseCol, priceFallCol, priceMa5Col, priceMa10Col, priceMa30Col,
            priceMaxLabelCol, priceMinLabelCol, volumeTextCol, volumeMa5Col, volumeMa10Col, macdTextCol,
            macdPositiveCol, macdNegativeCol, difLineCol, deaLineCol, kLineCol, dLineCol,
            jLineCol, abscissaTextCol, ordinateTextCol, crossHairCol, crossHairRightLabelCol,
            crossHairBottomLabelCol, crossHairRightLabelTextCol, detailFrameCol, detailTextCol, tickMarkCol,
            detailBgCol, detailRectWidth, abscissaTextSize, volumeTextSize, crossHairBottomLabelTextCol,
            priceMaxLabelTextCol, priceMinLabelTextCol, priceMaxLabelTextSize, priceMinLabelTextSize,
            crossHairRightLabelTextSize, crossHairBottomLabelTextSize, ordinateTextSize, detailTextSize,
            topMaTextSize, detailRectHeight;

    private float leftStart, topStart, rightEnd, bottomEnd, mulFirstDownX, mulFirstDownY, lastDiffMoveX,
            lastDiffMoveY, singleClickDownX, dispatchDownX, dispatchDownY, detailTextVerticalSpace,
            volumeImgBot, verticalSpace, flingVelocityX, priceImgBot, deputyTopY, deputyCenterY;

    private double maxPrice, topPrice, maxPriceX, minPrice, botPrice, minPriceX, maxVolume, avgHeightPerPrice,
            avgPriceRectWidth, avgHeightPerVolume, avgHeightMacd, avgHeightDea, avgHeightDif,
            avgHeightK, avgHeightD, avgHeightJ, mMaxPriceY,
            mMinPriceY, mMaxMacd, mMinMacd, mMaxK;

    private Paint strokePaint, fillPaint;
    private Path curvePath;

    private Rect topMa5Rect = new Rect();
    private Rect topMa10Rect = new Rect();
    private Rect topMa30Rect = new Rect();
    private Rect detailTextRect = new Rect();

    private String[] dateArr;
    private String[] detailLeftTitleArr;
    private List<KData> totalDataList = new ArrayList<>();
    private List<KData> viewDataList = new ArrayList<>();
    private List<String> detailRightDataList = new ArrayList<>();

    //水平线纵坐标
    private List<Float> horizontalYList = new ArrayList<>();
    //垂直线横坐标
    private List<Float> verticalXList = new ArrayList<>();

    private List<Pointer> mainMa5PointList = new ArrayList<>();
    private List<Pointer> mainMa10PointList = new ArrayList<>();
    private List<Pointer> mainMa30PointList = new ArrayList<>();

    private List<Pointer> deputyMa5PointList = new ArrayList<>();
    private List<Pointer> deputyMa10PointList = new ArrayList<>();
    private List<Pointer> deputyMa30PointList = new ArrayList<>();

    private List<Pointer> volumeMa5PointList = new ArrayList<>();
    private List<Pointer> volumeMa10PointList = new ArrayList<>();

    private List<KData> endDataList = new ArrayList<>();

    private KData lastKData;
    private LongPressRunnable longPressRunnable;
    private OnRequestDataListListener requestListener;
    private QuotaThread quotaThread;
    private Handler mDelayHandler;
    private Runnable mDelayRunnable;
    private GestureDetector gestureDetector;


    public KLineView(Context context) {
        this(context, null);
    }

    public KLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initData();
    }

    public interface OnRequestDataListListener {
        void requestData();
    }

    public void setOnRequestDataListListener(OnRequestDataListListener requestListener) {
        this.requestListener = requestListener;
    }

    /**
     * 添加最新单条数据
     *
     * @param data
     */
    public void addData(KData data) {
        endDataList.clear();
        endDataList.addAll(totalDataList.subList(totalDataList.size() - 30, totalDataList.size()));
        endDataList.add(data);
        if (quotaThread != null) {
            quotaThread.quotaSingleCalculate(endDataList);
        }
    }

    /**
     * 分页加载，向前期滑动时新增的数据，目前限制单次添加数据量不超过1100条
     *
     * @param dataList
     */
    public void addDataList(List<KData> dataList) {
        if (dataList.size() > 1100) {
            return;
        }
        if (initTotalListSize == 0) {
            initTotalListSize = dataList.size();
        }
        isNeedRequestBeforeData = dataList.size() >= initTotalListSize;
        totalDataList.addAll(0, dataList);
        startDataNum += dataList.size();
        if (quotaThread != null) {
            quotaThread.quotaListCalculate(totalDataList);
        }
    }

    /**
     * 控件初始化时添加的数据量，目前限制单次添加数据量不超过1100条
     *
     * @param dataList
     */
    public void initKDataList(List<KData> dataList) {
        if (dataList.size() > 1100) {
            return;
        }
        this.totalDataList.clear();
        this.totalDataList.addAll(dataList);
        startDataNum = totalDataList.size() - 1 - maxViewDataNum;
        QuotaUtil.initMa(totalDataList, false);
        resetViewData();
    }

    /**
     * 是否显示副图
     *
     * @param showState
     */
    public void setDeputyPicShow(boolean showState) {
        switch (deputyImgType) {
            case DEPUTY_IMG_MACD:
                QuotaUtil.initMACD(totalDataList, false);
                break;

            case DEPUTY_IMG_KDJ:
                QuotaUtil.initKDJ(totalDataList, false);
                break;
        }
        this.isShowDeputy = showState;
        invalidate();
    }

    /**
     * 设置主图显示类型，0：MA, 1:EMA, 2:BOLL
     *
     * @param type
     */
    public void setMainImgType(int type) {
        switch (type) {
            case MAIN_IMG_MA:
                QuotaUtil.initMa(totalDataList, false);
                break;

            case MAIN_IMG_EMA:
                QuotaUtil.initEma(totalDataList, false);
                break;

            case MAIN_IMG_BOLL:
                QuotaUtil.initBoll(totalDataList, false);
                break;
        }
        this.mainImgType = type;
        invalidate();
    }

    /**
     * 设置副图显示类型，0：MACD, 1:KDJ
     *
     * @param type
     */
    public void setDeputyImgType(int type) {
        switch (type) {
            case DEPUTY_IMG_MACD:
                QuotaUtil.initMACD(totalDataList, false);
                break;

            case DEPUTY_IMG_KDJ:
                QuotaUtil.initKDJ(totalDataList, false);
                break;
        }
        this.deputyImgType = type;
        invalidate();
    }

    /**
     * 获取副图是否显示
     *
     * @return
     */
    public boolean getVicePicShow() {
        return this.isShowDeputy;
    }

    /**
     * 退出页面时停止子线程并置空，便于回收，避免内存泄露
     */
    public void cancelQuotaThread() {
        quotaThread.setUIHandler(null);
        quotaThread.quit();
        quotaThread = null;
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KLineView);
            tickMarkCol = typedArray.getColor(R.styleable.KLineView_klTickMarkLineCol, -1);
            abscissaTextCol = typedArray.getColor(R.styleable.KLineView_klAbscissaTextCol, -1);
            abscissaTextSize = typedArray.getInt(R.styleable.KLineView_klAbscissaTextSize, -1);
            ordinateTextCol = typedArray.getColor(R.styleable.KLineView_klOrdinateTextCol, -1);
            ordinateTextSize = typedArray.getInt(R.styleable.KLineView_klOrdinateTextSize, -1);
            topMaTextSize = typedArray.getInt(R.styleable.KLineView_klTopMaTextSize, -1);
            priceIncreaseCol = typedArray.getColor(R.styleable.KLineView_klPriceIncreaseCol, -1);
            priceFallCol = typedArray.getColor(R.styleable.KLineView_klPriceFallCol, -1);
            priceMa5Col = typedArray.getColor(R.styleable.KLineView_klPriceMa5LineCol, -1);
            priceMa10Col = typedArray.getColor(R.styleable.KLineView_klPriceMa10LineCol, -1);
            priceMa30Col = typedArray.getColor(R.styleable.KLineView_klPriceMa30LineCol, -1);
            priceMaxLabelCol = typedArray.getColor(R.styleable.KLineView_klPriceMaxLabelCol, -1);
            priceMaxLabelTextCol = typedArray.getColor(R.styleable.KLineView_klPriceMaxLabelTextCol, -1);
            priceMaxLabelTextSize = typedArray.getInt(R.styleable.KLineView_klPriceMaxLabelTextSize, -1);
            priceMinLabelCol = typedArray.getColor(R.styleable.KLineView_klPriceMinLabelCol, -1);
            priceMinLabelTextCol = typedArray.getColor(R.styleable.KLineView_klPriceMinLabelTextCol, -1);
            priceMinLabelTextSize = typedArray.getInt(R.styleable.KLineView_klPriceMinLabelTextSize, -1);
            volumeTextCol = typedArray.getColor(R.styleable.KLineView_klVolumeTextCol, -1);
            volumeTextSize = typedArray.getInt(R.styleable.KLineView_klVolumeTextSize, -1);
            volumeMa5Col = typedArray.getColor(R.styleable.KLineView_klVolumeMa5LineCol, -1);
            volumeMa10Col = typedArray.getColor(R.styleable.KLineView_klVolumeMa10LineCol, -1);
            macdTextCol = typedArray.getColor(R.styleable.KLineView_klMacdTextCol, -1);
            macdPositiveCol = typedArray.getColor(R.styleable.KLineView_klMacdPositiveCol, -1);
            macdNegativeCol = typedArray.getColor(R.styleable.KLineView_klMacdNegativeCol, -1);
            difLineCol = typedArray.getColor(R.styleable.KLineView_klDifLineCol, -1);
            deaLineCol = typedArray.getColor(R.styleable.KLineView_klDeaLineCol, -1);
            kLineCol = typedArray.getColor(R.styleable.KLineView_klKLineCol, -1);
            dLineCol = typedArray.getColor(R.styleable.KLineView_klDLineCol, -1);
            jLineCol = typedArray.getColor(R.styleable.KLineView_klJLineCol, -1);
            crossHairCol = typedArray.getColor(R.styleable.KLineView_klCrossHairCol, -1);
            crossHairRightLabelCol = typedArray.getColor(R.styleable.KLineView_klCrossHairRightLabelCol, -1);
            crossHairRightLabelTextCol = typedArray.getColor(R.styleable.KLineView_klCrossHairRightLabelTextCol, -1);
            crossHairRightLabelTextSize = typedArray.getInt(R.styleable.KLineView_klCrossHairRightLabelTextSize, -1);
            crossHairBottomLabelCol = typedArray.getColor(R.styleable.KLineView_klCrossHairBottomLabelCol, -1);
            crossHairBottomLabelTextCol = typedArray.getColor(R.styleable.KLineView_klCrossHairBottomLabelTextCol, -1);
            crossHairBottomLabelTextSize = typedArray.getInt(R.styleable.KLineView_klCrossHairBottomLabelTextSize, -1);
            detailFrameCol = typedArray.getColor(R.styleable.KLineView_klDetailFrameCol, -1);
            detailTextCol = typedArray.getColor(R.styleable.KLineView_klDetailTextCol, -1);
            detailTextSize = typedArray.getInt(R.styleable.KLineView_klDetailTextSize, -1);
            detailBgCol = typedArray.getColor(R.styleable.KLineView_klDetailBgCol, -1);
            typedArray.recycle();
        }

        if (tickMarkCol == -1) {
            tickMarkCol = 0xffF7F7FB;
        }

        if (abscissaTextCol == -1) {
            abscissaTextCol = 0xff9BACBD;
        }

        if (abscissaTextSize == -1) {
            abscissaTextSize = 8;
        }

        if (ordinateTextCol == -1) {
            ordinateTextCol = abscissaTextCol;
        }

        if (ordinateTextSize == -1) {
            ordinateTextSize = abscissaTextSize;
        }

        if (topMaTextSize == -1) {
            topMaTextSize = 10;
        }

        if (priceIncreaseCol == -1) {
            priceIncreaseCol = 0xffFF5442;
        }

        if (priceFallCol == -1) {
            priceFallCol = 0xff2BB8AB;
        }

        if (priceMa5Col == -1) {
            priceMa5Col = 0xffFFA800;
        }

        if (priceMa10Col == -1) {
            priceMa10Col = 0xff2668FF;
        }

        if (priceMa30Col == -1) {
            priceMa30Col = 0xffFF45A1;
        }

        if (priceMaxLabelCol == -1) {
            priceMaxLabelCol = 0xffC0C6C9;
        }

        if (priceMaxLabelTextCol == -1) {
            priceMaxLabelTextCol = 0xffffffff;
        }

        if (priceMaxLabelTextSize == -1) {
            priceMaxLabelTextSize = 10;
        }

        if (priceMinLabelCol == -1) {
            priceMinLabelCol = priceMaxLabelCol;
        }

        if (priceMinLabelTextCol == -1) {
            priceMinLabelTextCol = priceMaxLabelTextCol;
        }

        if (priceMinLabelTextSize == -1) {
            priceMinLabelTextSize = 10;
        }

        if (volumeTextCol == -1) {
            volumeTextCol = 0xff9BACBD;
        }

        if (volumeMa5Col == -1) {
            volumeMa5Col = priceMa5Col;
        }

        if (volumeMa10Col == -1) {
            volumeMa10Col = priceMa10Col;
        }

        if (macdTextCol == -1) {
            macdTextCol = volumeTextCol;
        }

        if (macdPositiveCol == -1) {
            macdPositiveCol = priceIncreaseCol;
        }

        if (macdNegativeCol == -1) {
            macdNegativeCol = priceFallCol;
        }

        if (difLineCol == -1) {
            difLineCol = priceMa10Col;
        }

        if (deaLineCol == -1) {
            deaLineCol = priceMa30Col;
        }

        if (kLineCol == -1) {
            kLineCol = priceMa5Col;
        }

        if (dLineCol == -1) {
            dLineCol = priceMa10Col;
        }

        if (jLineCol == -1) {
            jLineCol = priceMa30Col;
        }

        if (crossHairCol == -1) {
            crossHairCol = 0xff828EA2;
        }

        if (crossHairBottomLabelCol == -1) {
            crossHairBottomLabelCol = priceMaxLabelCol;
        }

        if (crossHairBottomLabelTextCol == -1) {
            crossHairBottomLabelTextCol = 0xffffffff;
        }

        if (crossHairBottomLabelTextSize == -1) {
            crossHairBottomLabelTextSize = 8;
        }

        if (crossHairRightLabelCol == -1) {
            crossHairRightLabelCol = 0xff3193FF;
        }

        if (crossHairRightLabelTextCol == -1) {
            crossHairRightLabelTextCol = 0xffffffff;
        }

        if (crossHairRightLabelTextSize == -1) {
            crossHairRightLabelTextSize = 10;
        }

        if (detailFrameCol == -1) {
            detailFrameCol = 0xffB5C0D0;
        }

        if (detailTextCol == -1) {
            detailTextCol = 0xff808F9E;
        }

        if (detailTextSize == -1) {
            detailTextSize = 10;
        }

        if (detailBgCol == -1) {
            detailBgCol = 0xe6ffffff;
        }
    }

    private void initData() {
        super.setOnTouchListener(this);
        super.setClickable(true);
        super.setLongClickable(true);
        super.setFocusable(true);
        gestureDetector = new GestureDetector(getContext(), new CustomGestureListener());
        longPressRunnable = new LongPressRunnable();
        detailRectWidth = dp2px(103);
        detailRectHeight = dp2px(120);
        detailTextVerticalSpace = (detailRectHeight - dp2px(4)) / 8;
        dateArr = new String[3];
        detailLeftTitleArr = new String[]{"时间", "开", "高", "低", "收", "涨跌额", "涨跌幅", "成交量"};
        initQuotaThread();
        initStopDelay();

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setTextSize(sp2px(abscissaTextSize));
        strokePaint.setStyle(Paint.Style.STROKE);

        fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.FILL);

        curvePath = new Path();

    }

    private void initQuotaThread() {
        Handler uiHandler = new Handler(this);
        quotaThread = new QuotaThread("quotaThread", Process.THREAD_PRIORITY_BACKGROUND);
        quotaThread.setUIHandler(uiHandler);
        quotaThread.start();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == QuotaThread.HANDLER_QUOTA_LIST) {
            invalidate();
        } else if (msg.what == QuotaThread.HANDLER_QUOTA_SINGLE) {
            totalDataList.add(endDataList.get(endDataList.size() - 1));
            if (startDataNum == totalDataList.size() - maxViewDataNum - 2) {
                startDataNum++;
                resetViewData();
                invalidate();
            }
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        leftStart = getPaddingLeft() + 1;
        topStart = getPaddingTop() + 1;
        rightEnd = getMeasuredWidth() - getPaddingRight() - 1;
        bottomEnd = getMeasuredHeight() - getPaddingBottom() - 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (totalDataList.isEmpty() || viewDataList.isEmpty()) {
            return;
        }
        drawTickMark(canvas);
        drawMainDeputyRect(canvas);
        drawBezierCurve(canvas);

        getClickKData();

        drawTopPriceMAData(canvas);
        drawBotMAData(canvas);
        drawAbscissa(canvas);
        drawOrdinate(canvas);
        drawMaxMinPriceLabel(canvas);

        drawCrossHairLine(canvas);
        drawDetailData(canvas);
    }

    private class LongPressRunnable implements Runnable {
        private float longPressX;

        void setPressLocation(float x) {
            this.longPressX = x;
        }

        @Override
        public void run() {
            isLongPress = true;
            isShowDetail = true;
            singleClickDownX = longPressX;
            invalidate();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dispatchDownX = event.getX();
                dispatchDownY = event.getY();
                longPressRunnable.setPressLocation(event.getX());
                postDelayed(longPressRunnable, 500);
                isLongPress = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - dispatchDownX) > 5
                        || Math.abs(event.getY() - dispatchDownY) > 5) {
                    removeCallbacks(longPressRunnable);
                    if (isLongPress) {
                        singleClickDownX = event.getX();
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                removeCallbacks(longPressRunnable);
                break;
        }
        return isLongPress || super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                singleClickDownX = event.getX();
                flingVelocityX = 0;
                break;

            case MotionEvent.ACTION_UP:
                float upX = event.getX();
                if (Math.abs(upX - singleClickDownX) < 5) {
                    isShowDetail = true;
                    invalidate();
                }
                break;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mulFirstDownX = event.getX(0);
                mulFirstDownY = event.getY(0);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                isDoubleFinger = true;
                removeCallbacks(longPressRunnable);
                float mulSecondDownX = event.getX(1);
                float mulSecondDownY = event.getY(1);
                lastDiffMoveX = Math.abs(mulSecondDownX - mulFirstDownX);
                lastDiffMoveY = Math.abs(mulSecondDownY - mulFirstDownY);
                break;

            case MotionEvent.ACTION_MOVE:
                isShowDetail = false;
                if (event.getPointerCount() > 1) {
                    float mulFirstMoveX = event.getX(0);
                    float mulFirstMoveY = event.getY(0);
                    float mulSecondMoveX = event.getX(1);
                    float mulSecondMoveY = event.getY(1);

                    if (Math.abs(mulSecondMoveX - mulFirstMoveX) - lastDiffMoveX > 1.5
                            || Math.abs(mulSecondMoveY - mulFirstMoveY) - lastDiffMoveY > 1.5) {
                        maxViewDataNum -= 3;
                        if (maxViewDataNum < 18) {
                            maxViewDataNum = 18;
                        }

                    } else if (Math.abs(mulSecondMoveX - mulFirstMoveX) - lastDiffMoveX < -1.5
                            || Math.abs(mulSecondMoveY - mulFirstMoveY) - lastDiffMoveY < -1.5) {
                        maxViewDataNum += 3;
                        if (totalDataList.size() - startDataNum >= 140 && maxViewDataNum > 140) {
                            maxViewDataNum = 140;
                        } else if (totalDataList.size() - startDataNum < 140 && maxViewDataNum > 140) {
                            maxViewDataNum = totalDataList.size() - startDataNum;
                        }
                    }
                    lastDiffMoveX = Math.abs(mulSecondMoveX - mulFirstMoveX);
                    lastDiffMoveY = Math.abs(mulSecondMoveY - mulFirstMoveY);

                    resetViewData();
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                isDoubleFinger = false;
                break;

        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return !isDoubleFinger && gestureDetector.onTouchEvent(event);
    }

    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            moveData(distanceX);
            invalidate();
            isShowDetail = false;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityX > 8000) {
                flingVelocityX = 8000;
            } else if (velocityX < -8000) {
                flingVelocityX = -8000;
            } else {
                flingVelocityX = velocityX;
            }
            stopDelay();
            return true;
        }
    }

    private void stopDelay() {
        mDelayHandler.postDelayed(mDelayRunnable, 0);
    }

    private void initStopDelay() {
        mDelayHandler = new Handler();
        mDelayRunnable = new Runnable() {
            @Override
            public void run() {
                if (flingVelocityX < -200) {
                    if (flingVelocityX < -6000) {
                        startDataNum += 6;
                    } else if (flingVelocityX < -4000) {
                        startDataNum += 5;
                    } else if (flingVelocityX < -2500) {
                        startDataNum += 4;
                    } else if (flingVelocityX < -1000) {
                        startDataNum += 3;
                    } else {
                        startDataNum++;
                    }
                    flingVelocityX += 200;
                    if (startDataNum > totalDataList.size() - maxViewDataNum - 1) {
                        startDataNum = totalDataList.size() - maxViewDataNum - 1;
                    }
                } else if (flingVelocityX > 200) {
                    if (flingVelocityX > 6000) {
                        startDataNum -= 6;
                    } else if (flingVelocityX > 4000) {
                        startDataNum -= 5;
                    } else if (flingVelocityX > 2500) {
                        startDataNum -= 4;
                    } else if (flingVelocityX > 1000) {
                        startDataNum -= 3;
                    } else {
                        startDataNum--;
                    }
                    flingVelocityX -= 200;
                    if (startDataNum < 0) {
                        startDataNum = 0;
                    }
                }
                resetViewData();
                invalidate();
                requestNewData();

                if (Math.abs(flingVelocityX) > 200) {
                    mDelayHandler.postDelayed(this, 30);
                }
            }
        };
    }

    private void moveData(float distanceX) {
        if (maxViewDataNum < 60) {
            setSpeed(distanceX, 10);
        } else {
            setSpeed(distanceX, 3.5);
        }
        if (startDataNum < 0) {
            startDataNum = 0;
        }
        if (startDataNum > totalDataList.size() - maxViewDataNum - 1) {
            startDataNum = totalDataList.size() - maxViewDataNum - 1;
        }
        requestNewData();
        resetViewData();
    }

    private void setSpeed(float distanceX, double num) {
        if (Math.abs(distanceX) > 1 && Math.abs(distanceX) < 2) {
            startDataNum += (int) (distanceX * 10) % 2;
        } else if (Math.abs(distanceX) < 10) {
            startDataNum += (int) distanceX % 2;
        } else {
            startDataNum += (int) distanceX / num;
        }
    }

    private void requestNewData() {
        if (startDataNum <= totalDataList.size() / 3 && isNeedRequestBeforeData) {
            requestListener.requestData();
            isNeedRequestBeforeData = false;
        }
    }

    private void resetViewData() {
        viewDataList.clear();
        for (int i = 0; i < maxViewDataNum; i++) {
            if (i + startDataNum < totalDataList.size()) {
                viewDataList.add(totalDataList.get(i + startDataNum));
            }
        }
        if (viewDataList.size() > 0) {
            lastKData = viewDataList.get(viewDataList.size() - 1);
        }
    }

    //刻度线
    private void drawTickMark(Canvas canvas) {
        //垂直刻度线
        float horizontalSpace = (rightEnd - leftStart - (dp2px(46))) / 4;
        verticalXList.clear();
        resetStrokePaint(tickMarkCol, 0);
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(leftStart + horizontalSpace * (i) + dp2px(6),
                    topStart + dp2px(18),
                    leftStart + horizontalSpace * (i) + dp2px(6),
                    bottomEnd - dp2px(20),
                    strokePaint);
            verticalXList.add(leftStart + horizontalSpace * (i) + dp2px(6));
        }
        //水平刻度线
        verticalSpace = (bottomEnd - topStart - dp2px(38)) / 5;
        horizontalYList.clear();
        for (int i = 0; i < 6; i++) {
            canvas.drawLine(leftStart + dp2px(6),
                    topStart + verticalSpace * i + dp2px(18),
                    rightEnd,
                    topStart + verticalSpace * i + dp2px(18),
                    strokePaint);
            horizontalYList.add(topStart + verticalSpace * i + dp2px(18));
        }
        //副图顶线
        deputyTopY = horizontalYList.get(4) + dp2px(12);
        canvas.drawLine(leftStart + dp2px(6),
                horizontalYList.get(4) + verticalSpace / 2,
                rightEnd,
                horizontalYList.get(4) + verticalSpace / 2,
                strokePaint);
        //数量中线
        if (isShowDeputy) {
            canvas.drawLine(leftStart + dp2px(6),
                    horizontalYList.get(3) + verticalSpace / 2,
                    rightEnd,
                    horizontalYList.get(3) + verticalSpace / 2,
                    strokePaint);
        }
    }

    //主副图蜡烛图
    private void drawMainDeputyRect(Canvas canvas) {
        avgPriceRectWidth = (verticalXList.get(verticalXList.size() - 1)
                - verticalXList.get(0)) / maxViewDataNum;
        maxPrice = viewDataList.get(0).getMaxPrice();
        minPrice = viewDataList.get(0).getMinPrice();
        maxVolume = viewDataList.get(0).getVolume();
        mMaxMacd = viewDataList.get(0).getMacd();
        mMinMacd = viewDataList.get(0).getMacd();
        double maxDea = viewDataList.get(0).getDea();
        double minDea = viewDataList.get(0).getDea();
        double maxDif = viewDataList.get(0).getDif();
        double minDif = viewDataList.get(0).getDif();
        mMaxK = viewDataList.get(0).getK();
        double maxD = viewDataList.get(0).getD();
        double maxJ = viewDataList.get(0).getJ();

        for (int i = 0; i < viewDataList.size(); i++) {
            viewDataList.get(i).setLeftX(verticalXList.get(0) + avgPriceRectWidth * i);
            viewDataList.get(i).setRightX(verticalXList.get(0) + avgPriceRectWidth * (i + 1));
            if (viewDataList.get(i).getMaxPrice() >= maxPrice) {
                maxPrice = viewDataList.get(i).getMaxPrice();
                maxPriceX = viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2;
            }
            if (viewDataList.get(i).getMinPrice() <= minPrice) {
                minPrice = viewDataList.get(i).getMinPrice();
                minPriceX = viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2;
            }
            if (viewDataList.get(i).getVolume() >= maxVolume) {
                maxVolume = viewDataList.get(i).getVolume();
            }
            if (viewDataList.get(i).getMacd() >= mMaxMacd) {
                mMaxMacd = viewDataList.get(i).getMacd();
            }
            if (viewDataList.get(i).getMacd() <= mMinMacd) {
                mMinMacd = viewDataList.get(i).getMacd();
            }
            if (viewDataList.get(i).getDea() >= maxDea) {
                maxDea = viewDataList.get(i).getDea();
            }
            if (viewDataList.get(i).getDea() <= minDea) {
                minDea = viewDataList.get(i).getDea();
            }
            if (viewDataList.get(i).getDif() >= maxDif) {
                maxDif = viewDataList.get(i).getDif();
            }
            if (viewDataList.get(i).getDif() <= minDif) {
                minDif = viewDataList.get(i).getDif();
            }
            if (viewDataList.get(i).getK() >= mMaxK) {
                mMaxK = viewDataList.get(i).getK();
            }
            if (viewDataList.get(i).getD() >= maxD) {
                maxD = viewDataList.get(i).getD();
            }
            if (viewDataList.get(i).getJ() >= maxJ) {
                maxJ = viewDataList.get(i).getJ();
            }
        }

        topPrice = maxPrice + (maxPrice - minPrice) * 0.1;
        botPrice = minPrice - (maxPrice - minPrice) * 0.1;

        if (!isShowDeputy) {
            priceImgBot = horizontalYList.get(4);
            volumeImgBot = horizontalYList.get(5);
        } else {
            priceImgBot = horizontalYList.get(3);
            volumeImgBot = horizontalYList.get(4);
        }
        //priceData
        avgHeightPerPrice = (priceImgBot - horizontalYList.get(0)) / (topPrice - botPrice);
        mMaxPriceY = (horizontalYList.get(0) + (topPrice - maxPrice) * avgHeightPerPrice);
        mMinPriceY = (horizontalYList.get(0) + (topPrice - minPrice) * avgHeightPerPrice);

        //volumeData
        avgHeightPerVolume = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY) / maxVolume;

        //MACD
        if (mMaxMacd > 0 && mMinMacd < 0) {
            avgHeightMacd = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY) / Math.abs(mMaxMacd - mMinMacd);
            deputyCenterY = (float) (deputyTopY + mMaxMacd * avgHeightMacd);
        } else if (mMaxMacd <= 0) {
            avgHeightMacd = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY) / Math.abs(mMinMacd);
            deputyCenterY = deputyTopY;
        } else if (mMinMacd >= 0) {
            avgHeightMacd = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY) / Math.abs(mMaxMacd);
            deputyCenterY = horizontalYList.get(horizontalYList.size() - 1);
        }
        //DEA
        if (maxDea > 0 && minDea < 0) {
            avgHeightDea = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(24)) / (maxDea - minDea);
        } else if (maxDea <= 0) {
            avgHeightDea = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(24)) / Math.abs(minDea);
        } else if (minDea >= 0) {
            avgHeightDea = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(24)) / Math.abs(maxDea);
        }
        //DIF
        if (maxDif > 0 && minDif < 0) {
            avgHeightDif = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(24)) / (maxDif - minDif);
        } else if (maxDif <= 0) {
            avgHeightDif = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(24)) / Math.abs(minDif);
        } else if (minDif >= 0) {
            avgHeightDif = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(24)) / Math.abs(maxDif);
        }
        //K
        avgHeightK = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(12)) / mMaxK;
        //D
        avgHeightD = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(12)) / maxD;
        //J
        avgHeightJ = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(12)) / maxJ;

        for (int i = 0; i < viewDataList.size(); i++) {
            //drawPriceRectAndLine
            double openPrice = viewDataList.get(i).getOpenPrice();
            double closedPrice = viewDataList.get(i).getClosePrice();
            double higherPrice;
            double lowerPrice;
            if (openPrice >= closedPrice) {
                higherPrice = openPrice;
                lowerPrice = closedPrice;
                fillPaint.setColor(priceFallCol);
                resetStrokePaint(priceFallCol, 0);
            } else {
                higherPrice = closedPrice;
                lowerPrice = openPrice;
                fillPaint.setColor(priceIncreaseCol);
                resetStrokePaint(priceIncreaseCol, 0);
            }
            viewDataList.get(i).setCloseY((float) (horizontalYList.get(0) + (topPrice - closedPrice) * avgHeightPerPrice));
            //priceRect
            canvas.drawRect((float) (verticalXList.get(0) + avgPriceRectWidth * i + dp2px(0.5f)),
                    (float) (mMaxPriceY + (maxPrice - higherPrice) * avgHeightPerPrice),
                    (float) (verticalXList.get(0) + avgPriceRectWidth * (i + 1) - dp2px(0.5f)),
                    (float) (mMaxPriceY + (maxPrice - lowerPrice) * avgHeightPerPrice),
                    fillPaint);
            //priceLine
            canvas.drawLine((float) (verticalXList.get(0) + avgPriceRectWidth * (i * 2 + 1) / 2),
                    (float) (mMaxPriceY + (maxPrice - viewDataList.get(i).getMaxPrice()) * avgHeightPerPrice),
                    (float) (verticalXList.get(0) + avgPriceRectWidth * (i * 2 + 1) / 2),
                    (float) (mMaxPriceY + (maxPrice - viewDataList.get(i).getMinPrice()) * avgHeightPerPrice),
                    strokePaint);
            //VolumeRect
            canvas.drawRect((float) (verticalXList.get(0) + avgPriceRectWidth * i + dp2px(0.5f)),
                    (float) (volumeImgBot - viewDataList.get(i).getVolume() * avgHeightPerVolume),
                    (float) (verticalXList.get(0) + avgPriceRectWidth * (i + 1) - dp2px(0.5f)),
                    volumeImgBot,
                    fillPaint);
            //MACD
            if (isShowDeputy && deputyImgType == DEPUTY_IMG_MACD) {
                double macd = viewDataList.get(i).getMacd();
                if (macd > 0) {
                    fillPaint.setColor(macdPositiveCol);
                    canvas.drawRect((float) (verticalXList.get(0) + avgPriceRectWidth * i + dp2px(0.5f)),
                            (float) (deputyCenterY - macd * avgHeightMacd),
                            (float) (verticalXList.get(0) + avgPriceRectWidth * (i + 1) - dp2px(0.5f)),
                            deputyCenterY,
                            fillPaint);

                } else {
                    fillPaint.setColor(macdNegativeCol);
                    canvas.drawRect((float) (verticalXList.get(0) + avgPriceRectWidth * i + dp2px(0.5f)),
                            deputyCenterY,
                            (float) (verticalXList.get(0) + avgPriceRectWidth * (i + 1) - dp2px(0.5f)),
                            (float) (deputyCenterY + Math.abs(macd) * avgHeightMacd),
                            fillPaint);
                }
            }
        }
    }

    //贝塞尔曲线
    private void drawBezierCurve(Canvas canvas) {
        mainMa5PointList.clear();
        mainMa10PointList.clear();
        mainMa30PointList.clear();

        volumeMa5PointList.clear();
        volumeMa10PointList.clear();

        deputyMa5PointList.clear();
        deputyMa10PointList.clear();
        deputyMa30PointList.clear();

        for (int i = 0; i < viewDataList.size(); i++) {
            if (!viewDataList.get(i).isInitFinish()) {
                break;
            }
            //volumeMA
            Pointer volumeMa5Point = new Pointer();
            if (viewDataList.get(i).getVolumeMa5() > 0) {
                volumeMa5Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                volumeMa5Point.setY((float) (volumeImgBot
                        - viewDataList.get(i).getVolumeMa5() * avgHeightPerVolume));
                volumeMa5PointList.add(volumeMa5Point);
            }
            Pointer volumeMa10Point = new Pointer();
            if (viewDataList.get(i).getVolumeMa10() > 0) {
                volumeMa10Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                volumeMa10Point.setY((float) (volumeImgBot
                        - viewDataList.get(i).getVolumeMa10() * avgHeightPerVolume));
                volumeMa10PointList.add(volumeMa10Point);
            }

            switch (mainImgType) {
                //priceMA
                case MAIN_IMG_MA:
                    Pointer priceMa5Point = new Pointer();
                    if (viewDataList.get(i).getPriceMa5() > 0) {
                        priceMa5Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                        priceMa5Point.setY((float) (mMaxPriceY
                                + (maxPrice - viewDataList.get(i).getPriceMa5()) * avgHeightPerPrice));
                        mainMa5PointList.add(priceMa5Point);
                    }
                    Pointer priceMa10Point = new Pointer();
                    if (viewDataList.get(i).getPriceMa10() > 0) {
                        priceMa10Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                        priceMa10Point.setY((float) (mMaxPriceY
                                + (maxPrice - viewDataList.get(i).getPriceMa10()) * avgHeightPerPrice));
                        mainMa10PointList.add(priceMa10Point);
                    }
                    Pointer priceMa30Point = new Pointer();
                    if (viewDataList.get(i).getPriceMa30() > 0) {
                        priceMa30Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                        priceMa30Point.setY((float) (mMaxPriceY
                                + (maxPrice - viewDataList.get(i).getPriceMa30()) * avgHeightPerPrice));
                        mainMa30PointList.add(priceMa30Point);
                    }
                    break;

                //priceEMA
                case MAIN_IMG_EMA:
                    Pointer ema5Point = new Pointer();
                    if (viewDataList.get(i).getEma5() > 0) {
                        ema5Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                        ema5Point.setY((float) (mMaxPriceY
                                + (maxPrice - viewDataList.get(i).getEma5()) * avgHeightPerPrice));
                        mainMa5PointList.add(ema5Point);
                    }
                    Pointer ema10Point = new Pointer();
                    if (viewDataList.get(i).getEma10() > 0) {
                        ema10Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                        ema10Point.setY((float) (mMaxPriceY
                                + (maxPrice - viewDataList.get(i).getEma10()) * avgHeightPerPrice));
                        mainMa10PointList.add(ema10Point);
                    }
                    Pointer ema30Point = new Pointer();
                    if (viewDataList.get(i).getEma30() > 0) {
                        ema30Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                        ema30Point.setY((float) (mMaxPriceY
                                + (maxPrice - viewDataList.get(i).getEma30()) * avgHeightPerPrice));
                        mainMa30PointList.add(ema30Point);
                    }
                    break;

                //priceBOLL
                case MAIN_IMG_BOLL:
                    Pointer bollMbPoint = new Pointer();
                    if (viewDataList.get(i).getBollMb() > 0) {
                        bollMbPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                        bollMbPoint.setY((float) (mMaxPriceY
                                + (maxPrice - viewDataList.get(i).getBollMb()) * avgHeightPerPrice));
                        mainMa5PointList.add(bollMbPoint);
                    }
                    Pointer bollUpPoint = new Pointer();
                    if (viewDataList.get(i).getBollUp() > 0) {
                        bollUpPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                        bollUpPoint.setY((float) (mMaxPriceY
                                + (maxPrice - viewDataList.get(i).getBollUp()) * avgHeightPerPrice));
                        mainMa10PointList.add(bollUpPoint);
                    }
                    Pointer bollDnPoint = new Pointer();
                    if (viewDataList.get(i).getBollDn() > 0) {
                        bollDnPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                        bollDnPoint.setY((float) (mMaxPriceY
                                + (maxPrice - viewDataList.get(i).getBollDn()) * avgHeightPerPrice));
                        mainMa30PointList.add(bollDnPoint);
                    }
                    break;
            }

            if (isShowDeputy && deputyImgType == DEPUTY_IMG_MACD) {
                Pointer deaPoint = new Pointer();
                if (viewDataList.get(i).getDea() > 0) {
                    deaPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    deaPoint.setY((float) (deputyCenterY - viewDataList.get(i).getDea() * avgHeightDea));
                } else {
                    deaPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    deaPoint.setY((float) (deputyCenterY + Math.abs(viewDataList.get(i).getDea() * avgHeightDea)));
                }
                deputyMa5PointList.add(deaPoint);

                Pointer difPoint = new Pointer();
                if (viewDataList.get(i).getDif() > 0) {
                    difPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    difPoint.setY((float) (deputyCenterY - viewDataList.get(i).getDif() * avgHeightDif));
                } else {
                    difPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    difPoint.setY((float) (deputyCenterY + Math.abs(viewDataList.get(i).getDif() * avgHeightDif)));
                }
                deputyMa10PointList.add(difPoint);

            } else if (isShowDeputy && deputyImgType == DEPUTY_IMG_KDJ) {
                Pointer kPoint = new Pointer();
                if (viewDataList.get(i).getK() > 0) {
                    kPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    kPoint.setY((float) (horizontalYList.get(5) - viewDataList.get(i).getK() * avgHeightK));
                    deputyMa5PointList.add(kPoint);
                }

                Pointer dPoint = new Pointer();
                if (viewDataList.get(i).getD() > 0) {
                    dPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    dPoint.setY((float) (horizontalYList.get(5) - viewDataList.get(i).getD() * avgHeightD));
                    deputyMa10PointList.add(dPoint);
                }

                Pointer jPoint = new Pointer();
                if (viewDataList.get(i).getJ() > 0) {
                    jPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    jPoint.setY((float) (horizontalYList.get(5) - viewDataList.get(i).getJ() * avgHeightJ));
                    deputyMa30PointList.add(jPoint);
                }
            }
        }

        drawVolumeBezierCurve(canvas);
        drawMainBezierCurve(canvas);
        if (isShowDeputy){
            drawDeputyCurve(canvas);
        }
    }

    //主图 MA曲线
    private void drawMainBezierCurve(Canvas canvas) {
        QuotaUtil.setBezierPath(mainMa5PointList, curvePath);
        resetStrokePaint(priceMa5Col, 0);
        canvas.drawPath(curvePath, strokePaint);

        QuotaUtil.setBezierPath(mainMa10PointList, curvePath);
        resetStrokePaint(priceMa10Col, 0);
        canvas.drawPath(curvePath, strokePaint);

        QuotaUtil.setBezierPath(mainMa30PointList, curvePath);
        resetStrokePaint(priceMa30Col, 0);
        canvas.drawPath(curvePath, strokePaint);
    }

    //volume MA曲线
    private void drawVolumeBezierCurve(Canvas canvas) {
        QuotaUtil.setBezierPath(volumeMa5PointList, curvePath);
        resetStrokePaint(priceMa5Col, 0);
        canvas.drawPath(curvePath, strokePaint);

        QuotaUtil.setBezierPath(volumeMa10PointList, curvePath);
        resetStrokePaint(priceMa10Col, 0);
        canvas.drawPath(curvePath, strokePaint);
    }

    //副图 曲线
    private void drawDeputyCurve(Canvas canvas) {
        QuotaUtil.setLinePath(deputyMa5PointList, curvePath);
        resetStrokePaint(priceMa5Col, 0);
        canvas.drawPath(curvePath, strokePaint);

        QuotaUtil.setLinePath(deputyMa10PointList, curvePath);
        resetStrokePaint(priceMa10Col, 0);
        canvas.drawPath(curvePath, strokePaint);

        QuotaUtil.setLinePath(deputyMa30PointList, curvePath);
        resetStrokePaint(priceMa30Col, 0);
        canvas.drawPath(curvePath, strokePaint);
    }

    //获取单击位置的数据
    private void getClickKData() {
        if (isShowDetail) {
            detailRightDataList.clear();
            for (int i = 0; i < viewDataList.size(); i++) {
                if (viewDataList.get(i).getLeftX() <= singleClickDownX
                        && viewDataList.get(i).getRightX() >= singleClickDownX) {
                    lastKData = viewDataList.get(i);
                    detailRightDataList.add(formatDate(lastKData.getTime()));
                    detailRightDataList.add(setPrecision(lastKData.getOpenPrice(), 2));
                    detailRightDataList.add(setPrecision(lastKData.getMaxPrice(), 2));
                    detailRightDataList.add(setPrecision(lastKData.getMinPrice(), 2));
                    detailRightDataList.add(setPrecision(lastKData.getClosePrice(), 2));
                    double upDnAmount = lastKData.getUpDnAmount();
                    if (upDnAmount > 0) {
                        detailRightDataList.add("+" + setPrecision(upDnAmount, 2));
                        detailRightDataList.add("+" + setPrecision(lastKData.getUpDnRate() * 100, 2) + "%");
                    } else {
                        detailRightDataList.add(setPrecision(upDnAmount, 2));
                        detailRightDataList.add(setPrecision(lastKData.getUpDnRate() * 100, 2) + "%");
                    }
                    detailRightDataList.add(setPrecision(lastKData.getVolume(), 2));
                    break;
                }
            }
        } else {
            lastKData = viewDataList.get(viewDataList.size() - 1);
        }
    }

    //十字线
    private void drawCrossHairLine(Canvas canvas) {
        if (lastKData == null || !isShowDetail) {
            return;
        }
        //垂直
        resetStrokePaint(crossHairCol, 0);
        canvas.drawLine((float) (lastKData.getLeftX() + avgPriceRectWidth / 2),
                horizontalYList.get(0),
                (float) (lastKData.getLeftX() + avgPriceRectWidth / 2),
                horizontalYList.get(horizontalYList.size() - 1),
                strokePaint);

        //水平
        resetStrokePaint(crossHairCol, 0);
        canvas.drawLine(verticalXList.get(0),
                (float) lastKData.getCloseY(),
                verticalXList.get(verticalXList.size() - 1),
                (float) lastKData.getCloseY(),
                strokePaint);

        //底部标签
        RectF grayRectF = new RectF(singleClickDownX - dp2px(25),
                bottomEnd - dp2px(20),
                singleClickDownX + dp2px(25),
                bottomEnd);
        fillPaint.setColor(crossHairBottomLabelCol);
        canvas.drawRoundRect(grayRectF, 4, 4, fillPaint);

        //底部标签text
        String moveTime = formatDate(lastKData.getTime());
        resetStrokePaint(crossHairBottomLabelTextCol, crossHairBottomLabelTextSize);
        canvas.drawText(moveTime,
                singleClickDownX - strokePaint.measureText(moveTime) / 2,
                bottomEnd - dp2px(7),
                strokePaint);

        //右侧标签
        RectF blueRectF = new RectF(rightEnd - dp2px(38),
                (float) lastKData.getCloseY() - dp2px(7),
                rightEnd - dp2px(1),
                (float) lastKData.getCloseY() + dp2px(7));
        fillPaint.setColor(crossHairRightLabelCol);
        canvas.drawRoundRect(blueRectF, 4, 4, fillPaint);

        curvePath.reset();
        curvePath.moveTo(verticalXList.get(verticalXList.size() - 1), (float) lastKData.getCloseY());
        curvePath.lineTo(rightEnd - dp2px(37), (float) lastKData.getCloseY() - dp2px(3));
        curvePath.lineTo(rightEnd - dp2px(37), (float) lastKData.getCloseY() + dp2px(3));
        curvePath.close();
        canvas.drawPath(curvePath, fillPaint);

        double avgPricePerHeight;
        if (!isShowDeputy) {
            avgPricePerHeight = (topPrice - botPrice)
                    / (horizontalYList.get(4) - horizontalYList.get(0));
        } else {
            avgPricePerHeight = (topPrice - botPrice)
                    / (horizontalYList.get(3) - horizontalYList.get(0));
        }

        String movePrice = setPrecision(topPrice
                - avgPricePerHeight * ((float) lastKData.getCloseY() - horizontalYList.get(0)), 2);
        Rect textRect = new Rect();
        resetStrokePaint(crossHairRightLabelTextCol, crossHairRightLabelTextSize);
        strokePaint.getTextBounds(movePrice, 0, movePrice.length(), textRect);
        canvas.drawText(movePrice,
                rightEnd - dp2px(38) + (blueRectF.width() - textRect.width()) / 2,
                (float) lastKData.getCloseY() + dp2px(7) - (blueRectF.height() - textRect.height()) / 2,
                strokePaint);
    }

    //最高价、最低价标签
    private void drawMaxMinPriceLabel(Canvas canvas) {
        //maxPrice
        Rect maxPriceRect = new Rect();
        String maxPriceStr = setPrecision(maxPrice, 2);
        resetStrokePaint(priceMaxLabelTextCol, priceMaxLabelTextSize);
        strokePaint.getTextBounds(maxPriceStr, 0, maxPriceStr.length(), maxPriceRect);

        RectF maxRectF;
        float maxPriceTextX;
        if (maxPriceX + maxPriceRect.width() + dp2px(8) < verticalXList.get(verticalXList.size() - 1)) {
            maxRectF = new RectF((float) (maxPriceX + dp2px(3)),
                    (float) mMaxPriceY - dp2px(7),
                    (float) (maxPriceX + maxPriceRect.width() + dp2px(8)),
                    (float) mMaxPriceY + dp2px(7));

            curvePath.reset();
            curvePath.moveTo((float) maxPriceX, (float) mMaxPriceY);
            curvePath.lineTo((float) (maxPriceX + dp2px(4)), (float) mMaxPriceY - dp2px(3));
            curvePath.lineTo((float) (maxPriceX + dp2px(4)), (float) mMaxPriceY + dp2px(3));
            curvePath.close();

            maxPriceTextX = (float) (maxPriceX + dp2px(5));

        } else {
            maxRectF = new RectF((float) (maxPriceX - dp2px(3)),
                    (float) mMaxPriceY - dp2px(7),
                    (float) (maxPriceX - maxPriceRect.width() - dp2px(8)),
                    (float) mMaxPriceY + dp2px(7));

            curvePath.reset();
            curvePath.moveTo((float) maxPriceX, (float) mMaxPriceY);
            curvePath.lineTo((float) (maxPriceX - dp2px(4)), (float) mMaxPriceY - dp2px(3));
            curvePath.lineTo((float) (maxPriceX - dp2px(4)), (float) mMaxPriceY + dp2px(3));
            curvePath.close();

            maxPriceTextX = (float) (maxPriceX - dp2px(5)) - maxPriceRect.width();
        }

        fillPaint.setColor(priceMaxLabelCol);
        canvas.drawRoundRect(maxRectF, 4, 4, fillPaint);
        canvas.drawPath(curvePath, fillPaint);

        resetStrokePaint(priceMaxLabelTextCol, priceMaxLabelTextSize);
        canvas.drawText(maxPriceStr,
                maxPriceTextX,
                (float) mMaxPriceY + maxPriceRect.height() / 2,
                strokePaint);

        //minPrice
        Rect minPriceRect = new Rect();
        String minPriceStr = setPrecision(minPrice, 2);
        resetStrokePaint(priceMinLabelTextCol, priceMinLabelTextSize);
        strokePaint.getTextBounds(minPriceStr, 0, minPriceStr.length(), minPriceRect);

        RectF minRectF;
        float minPriceTextX;
        if (minPriceX + minPriceRect.width() + dp2px(8) < verticalXList.get(verticalXList.size() - 1)) {
            minRectF = new RectF((float) (minPriceX + dp2px(3)),
                    (float) mMinPriceY - dp2px(7),
                    (float) (minPriceX + minPriceRect.width() + dp2px(8)),
                    (float) mMinPriceY + dp2px(7));

            curvePath.reset();
            curvePath.moveTo((float) minPriceX, (float) mMinPriceY);
            curvePath.lineTo((float) (minPriceX + dp2px(4)), (float) mMinPriceY - dp2px(3));
            curvePath.lineTo((float) (minPriceX + dp2px(4)), (float) mMinPriceY + dp2px(3));
            curvePath.close();

            minPriceTextX = (float) (minPriceX + dp2px(5));

        } else {
            minRectF = new RectF((float) (minPriceX - dp2px(3)),
                    (float) mMinPriceY - dp2px(7),
                    (float) (minPriceX - minPriceRect.width() - dp2px(8)),
                    (float) mMinPriceY + dp2px(7));

            curvePath.reset();
            curvePath.moveTo((float) minPriceX, (float) mMinPriceY);
            curvePath.lineTo((float) (minPriceX - dp2px(4)), (float) mMinPriceY - dp2px(3));
            curvePath.lineTo((float) (minPriceX - dp2px(4)), (float) mMinPriceY + dp2px(3));
            curvePath.close();

            minPriceTextX = (float) (minPriceX - dp2px(5)) - minPriceRect.width();
        }

        fillPaint.setColor(priceMinLabelCol);
        canvas.drawRoundRect(minRectF, 4, 4, fillPaint);
        canvas.drawPath(curvePath, fillPaint);

        resetStrokePaint(priceMinLabelTextCol, priceMinLabelTextSize);
        canvas.drawText(minPriceStr,
                minPriceTextX,
                (float) mMinPriceY + minPriceRect.height() / 2,
                strokePaint);
    }

    private void drawDetailData(Canvas canvas) {
        if (lastKData == null || !isShowDetail) {
            return;
        }
        resetStrokePaint(detailTextCol, detailTextSize);
        strokePaint.getTextBounds(detailLeftTitleArr[0], 0, detailLeftTitleArr[0].length(), detailTextRect);

        if (singleClickDownX <= getMeasuredWidth() / 2) {
            //边框(右侧)
            fillPaint.setColor(detailBgCol);
            canvas.drawRect(verticalXList.get(verticalXList.size() - 1) - detailRectWidth,
                    horizontalYList.get(0),
                    verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(0) + detailRectHeight,
                    fillPaint);

            resetStrokePaint(detailFrameCol, 0);
            canvas.drawLine(verticalXList.get(verticalXList.size() - 1) - detailRectWidth,
                    horizontalYList.get(0),
                    verticalXList.get(verticalXList.size() - 1) - detailRectWidth,
                    horizontalYList.get(0) + detailRectHeight,
                    strokePaint);

            canvas.drawLine(verticalXList.get(verticalXList.size() - 1) - detailRectWidth,
                    horizontalYList.get(0),
                    verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(0),
                    strokePaint);

            canvas.drawLine(verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(0),
                    verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(0) + detailRectHeight,
                    strokePaint);

            canvas.drawLine(verticalXList.get(verticalXList.size() - 1) - detailRectWidth,
                    horizontalYList.get(0) + detailRectHeight,
                    verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(0) + detailRectHeight,
                    strokePaint);

            //详情字段
            resetStrokePaint(detailTextCol, detailTextSize);
            for (int i = 0; i < detailLeftTitleArr.length; i++) {
                canvas.drawText(detailLeftTitleArr[i],
                        verticalXList.get(verticalXList.size() - 1) - detailRectWidth + dp2px(4),
                        horizontalYList.get(0) + detailTextVerticalSpace * i
                                + detailTextRect.height() + (detailTextVerticalSpace - detailTextRect.height()) / 2,
                        strokePaint);
            }

            //详情数据
            for (int i = 0; i < detailRightDataList.size(); i++) {
                if (i == 5 || i == 6) {
                    if (lastKData.getUpDnAmount() > 0) {
                        resetStrokePaint(priceIncreaseCol, detailTextSize);
                    } else {
                        resetStrokePaint(priceFallCol, detailTextSize);
                    }
                } else {
                    resetStrokePaint(detailTextCol, detailTextSize);
                }
                canvas.drawText(detailRightDataList.get(i),
                        verticalXList.get(verticalXList.size() - 1) - dp2px(4)
                                - strokePaint.measureText(detailRightDataList.get(i)),
                        horizontalYList.get(0) + detailTextVerticalSpace * i
                                + detailTextRect.height() + (detailTextVerticalSpace - detailTextRect.height()) / 2,
                        strokePaint);
            }

        } else {
            //边框(左侧)
            fillPaint.setColor(detailBgCol);
            canvas.drawRect(verticalXList.get(0),
                    horizontalYList.get(0),
                    verticalXList.get(0) + detailRectWidth,
                    horizontalYList.get(0) + detailRectHeight,
                    fillPaint);

            resetStrokePaint(detailFrameCol, 0);
            canvas.drawLine(verticalXList.get(0),
                    horizontalYList.get(0),
                    verticalXList.get(0),
                    horizontalYList.get(0) + detailRectHeight,
                    strokePaint);

            canvas.drawLine(verticalXList.get(0),
                    horizontalYList.get(0),
                    verticalXList.get(0) + detailRectWidth,
                    horizontalYList.get(0),
                    strokePaint);

            canvas.drawLine(verticalXList.get(0) + detailRectWidth,
                    horizontalYList.get(0),
                    verticalXList.get(0) + detailRectWidth,
                    horizontalYList.get(0) + detailRectHeight,
                    strokePaint);

            canvas.drawLine(verticalXList.get(0),
                    horizontalYList.get(0) + detailRectHeight,
                    verticalXList.get(0) + detailRectWidth,
                    horizontalYList.get(0) + detailRectHeight,
                    strokePaint);

            //文字详情
            resetStrokePaint(detailTextCol, detailTextSize);
            for (int i = 0; i < detailLeftTitleArr.length; i++) {
                canvas.drawText(detailLeftTitleArr[i],
                        verticalXList.get(0) + dp2px(4),
                        horizontalYList.get(0) + detailTextVerticalSpace * i
                                + detailTextRect.height() + (detailTextVerticalSpace - detailTextRect.height()) / 2,
                        strokePaint);
            }

            //详情数据
            for (int i = 0; i < detailRightDataList.size(); i++) {
                if (i == 5 || i == 6) {
                    if (lastKData.getUpDnAmount() > 0) {
                        resetStrokePaint(priceIncreaseCol, detailTextSize);
                    } else {
                        resetStrokePaint(priceFallCol, detailTextSize);
                    }
                } else {
                    resetStrokePaint(detailTextCol, detailTextSize);
                }
                canvas.drawText(detailRightDataList.get(i),
                        verticalXList.get(0) + detailRectWidth - dp2px(4)
                                - strokePaint.measureText(detailRightDataList.get(i)),
                        horizontalYList.get(0) + detailTextVerticalSpace * i
                                + detailTextRect.height() + (detailTextVerticalSpace - detailTextRect.height()) / 2,
                        strokePaint);
            }
        }
    }

    //顶部价格MA
    private void drawTopPriceMAData(Canvas canvas) {
        String ma5Str = mMa5 + setPrecision(lastKData.getPriceMa5(), 2);
        String ma10Str = mMa10 + setPrecision(lastKData.getPriceMa10(), 2);
        String ma30Str = mMa30 + setPrecision(lastKData.getPriceMa30(), 2);

        resetStrokePaint(priceMa5Col, topMaTextSize);
        strokePaint.getTextBounds(ma5Str, 0, ma5Str.length(), topMa5Rect);
        canvas.drawText(ma5Str,
                leftStart + dp2px(6),
                topStart + topMa5Rect.height() + dp2px(6),
                strokePaint);

        resetStrokePaint(priceMa10Col, topMaTextSize);
        strokePaint.getTextBounds(ma10Str, 0, ma10Str.length(), topMa10Rect);
        canvas.drawText(ma10Str,
                leftStart + dp2px(6) + topMa5Rect.width() + dp2px(10),
                topStart + topMa5Rect.height() + dp2px(6),
                strokePaint);

        resetStrokePaint(priceMa30Col, topMaTextSize);
        strokePaint.getTextBounds(ma30Str, 0, ma30Str.length(), topMa30Rect);
        canvas.drawText(ma30Str,
                leftStart + dp2px(6) + topMa5Rect.width() + topMa10Rect.width() + dp2px(10) * 2,
                topStart + topMa5Rect.height() + dp2px(6),
                strokePaint);
    }

    //数量MA
    private void drawBotMAData(Canvas canvas) {
        //VOL
        String volStr = mVol + setPrecision(lastKData.getVolume(), 2);
        Rect volRect = new Rect();
        resetStrokePaint(volumeTextCol, volumeTextSize);
        strokePaint.getTextBounds(volStr, 0, volStr.length(), volRect);
        canvas.drawText(volStr,
                verticalXList.get(0),
                priceImgBot + volRect.height() + dp2px(2),
                strokePaint);

        String ma5Str = mMa5 + setPrecision(lastKData.getVolumeMa5(), 2);
        Rect volMa5Rect = new Rect();
        resetStrokePaint(priceMa5Col, volumeTextSize);
        strokePaint.getTextBounds(ma5Str, 0, ma5Str.length(), volMa5Rect);
        canvas.drawText(ma5Str,
                verticalXList.get(0) + volRect.width() + dp2px(10),
                priceImgBot + volRect.height() + dp2px(2),
                strokePaint);

        String ma10Str = mMa10 + setPrecision(lastKData.getVolumeMa10(), 2);
        resetStrokePaint(priceMa10Col, volumeTextSize);
        canvas.drawText(ma10Str,
                verticalXList.get(0) + volMa5Rect.width() + volRect.width() + dp2px(10) * 2,
                priceImgBot + volRect.height() + dp2px(2),
                strokePaint);

        if (isShowDeputy && deputyImgType == DEPUTY_IMG_MACD) {
            //MACD
            Rect titleRect = new Rect();
            resetStrokePaint(volumeTextCol, volumeTextSize);
            strokePaint.getTextBounds(mMacdTitle, 0, mMacdTitle.length(), titleRect);
            canvas.drawText(mMacdTitle,
                    verticalXList.get(0),
                    horizontalYList.get(4) + titleRect.height(),
                    strokePaint);

            String macdStr = mMacd + setPrecision(lastKData.getMacd(), 2);
            resetStrokePaint(priceMa5Col, volumeTextSize);
            canvas.drawText(macdStr,
                    verticalXList.get(0) + titleRect.width() + dp2px(10),
                    horizontalYList.get(4) + titleRect.height(),
                    strokePaint);
            float macdWidth = strokePaint.measureText(macdStr);

            String difStr = mDif + setPrecision(lastKData.getDif(), 2);
            resetStrokePaint(priceMa10Col, volumeTextSize);
            canvas.drawText(difStr,
                    verticalXList.get(0) + titleRect.width() + dp2px(20) + macdWidth,
                    horizontalYList.get(4) + titleRect.height(),
                    strokePaint);
            float difWidth = strokePaint.measureText(difStr);

            resetStrokePaint(priceMa30Col, volumeTextSize);
            canvas.drawText(mDea + setPrecision(lastKData.getDea(), 2),
                    verticalXList.get(0) + titleRect.width() + dp2px(30) + macdWidth + difWidth,
                    horizontalYList.get(4) + titleRect.height(),
                    strokePaint);

        } else if (isShowDeputy && deputyImgType == DEPUTY_IMG_KDJ) {
            //KDJ
            Rect titleRect = new Rect();
            resetStrokePaint(volumeTextCol, volumeTextSize);
            strokePaint.getTextBounds(mKdjTitle, 0, mKdjTitle.length(), titleRect);
            canvas.drawText(mKdjTitle,
                    verticalXList.get(0),
                    horizontalYList.get(4) + titleRect.height(),
                    strokePaint);

            String kStr = mK + setPrecision(lastKData.getK(), 2);
            resetStrokePaint(priceMa5Col, volumeTextSize);
            canvas.drawText(kStr,
                    verticalXList.get(0) + titleRect.width() + dp2px(10),
                    horizontalYList.get(4) + titleRect.height(),
                    strokePaint);
            float kWidth = strokePaint.measureText(kStr);

            String dStr = mD + setPrecision(lastKData.getD(), 2);
            resetStrokePaint(priceMa10Col, volumeTextSize);
            canvas.drawText(dStr,
                    verticalXList.get(0) + titleRect.width() + dp2px(20) + kWidth,
                    horizontalYList.get(4) + titleRect.height(),
                    strokePaint);
            float dWidth = strokePaint.measureText(dStr);

            resetStrokePaint(priceMa30Col, volumeTextSize);
            canvas.drawText(mJ + setPrecision(lastKData.getJ(), 2),
                    verticalXList.get(0) + titleRect.width() + dp2px(30) + kWidth + dWidth,
                    horizontalYList.get(4) + titleRect.height(),
                    strokePaint);
        }
    }

    //横坐标
    private void drawAbscissa(Canvas canvas) {
        for (int i = 0; i < 3; i++) {
            for (KData data : viewDataList) {
                if (data.getLeftX() <= verticalXList.get(i + 1) && data.getRightX() >= verticalXList.get(i)) {
                    dateArr[i] = formatDate(data.getTime());
                    break;
                }
            }
        }

        resetStrokePaint(abscissaTextCol, abscissaTextSize);
        for (int i = 0; i < verticalXList.size(); i++) {
            if (i == 0) {
                canvas.drawText(formatDate(viewDataList.get(0).getTime()),
                        leftStart + dp2px(6),
                        bottomEnd - dp2px(7),
                        strokePaint);

            } else if (i == verticalXList.size() - 1) {
                String dateStr = formatDate(viewDataList.get(viewDataList.size() - 1).getTime());
                canvas.drawText(dateStr,
                        rightEnd - dp2px(41) - strokePaint.measureText(dateStr),
                        bottomEnd - dp2px(7),
                        strokePaint);

            } else {
                canvas.drawText(dateArr[i - 1],
                        leftStart + dp2px(6) + (rightEnd - leftStart - dp2px(47)) / 4 * i
                                - strokePaint.measureText(dateArr[i - 1]) / 2,
                        bottomEnd - dp2px(7),
                        strokePaint);
            }
        }
    }

    //纵坐标
    private void drawOrdinate(Canvas canvas) {
        Rect rect = new Rect();
        resetStrokePaint(ordinateTextCol, ordinateTextSize);
        //最高价
        strokePaint.getTextBounds(topPrice + "", 0, (topPrice + "").length(), rect);
        canvas.drawText(setPrecision(topPrice, 2),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                horizontalYList.get(0) + rect.height(),
                strokePaint);

        //最低价
        strokePaint.getTextBounds(botPrice + "", 0, (botPrice + "").length(), rect);
        canvas.drawText(setPrecision(botPrice, 2),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                priceImgBot - dp2px(3),
                strokePaint);

        if (!isShowDeputy) {
            double avgPrice = (topPrice - botPrice) / 4;
            for (int i = 0; i < 3; i++) {
                canvas.drawText(setPrecision(topPrice - avgPrice * (i + 1), 2),
                        verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                        horizontalYList.get(i + 1) + rect.height() / 2,
                        strokePaint);
            }
        } else {
            double avgPrice = (topPrice - botPrice) / 3;
            for (int i = 0; i < 2; i++) {
                canvas.drawText(setPrecision(topPrice - avgPrice * (i + 1), 2),
                        verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                        horizontalYList.get(i + 1) + rect.height() / 2,
                        strokePaint);
            }

            String topDeputy = "";
            String botDeputy = "";
            String centerDeputy = "";
            if (deputyImgType == DEPUTY_IMG_MACD) {
                if (mMaxMacd > 0 && mMinMacd < 0) {
                    topDeputy = setPrecision(mMaxMacd, 2);
                    botDeputy = setPrecision(mMinMacd, 2);
                    centerDeputy = setPrecision((mMaxMacd - mMinMacd) / 2, 2);
                } else if (mMaxMacd <= 0) {
                    topDeputy = "0";
                    botDeputy = setPrecision(mMinMacd, 2);
                    centerDeputy = setPrecision((mMinMacd - mMaxMacd) / 2, 2);
                } else if (mMinMacd >= 0) {
                    topDeputy = setPrecision(mMaxMacd, 2);
                    botDeputy = "0";
                    centerDeputy = setPrecision((mMaxMacd - mMinMacd) / 2, 2);
                }
            } else if (deputyImgType == DEPUTY_IMG_KDJ) {
                topDeputy = mMaxK + "";
                centerDeputy = mMaxK / 2 + "";
                botDeputy = "0";
            }

            canvas.drawText(topDeputy,
                    verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                    horizontalYList.get(horizontalYList.size() - 2) + rect.height(),
                    strokePaint);

            canvas.drawText(centerDeputy,
                    verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                    horizontalYList.get(horizontalYList.size() - 1) - verticalSpace / 2,
                    strokePaint);

            canvas.drawText(botDeputy,
                    verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                    horizontalYList.get(horizontalYList.size() - 1),
                    strokePaint);
        }

        //最高量
        strokePaint.getTextBounds(maxVolume + "", 0, (maxVolume + "").length(), rect);
        canvas.drawText(setPrecision(maxVolume, 2),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                priceImgBot + rect.height() + dp2px(3),
                strokePaint);

        //最高量/2
        canvas.drawText(setPrecision(maxVolume / 2, 2),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                volumeImgBot - verticalSpace / 2,
                strokePaint);

        //数量 0
        canvas.drawText("0",
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                volumeImgBot - dp2px(2),
                strokePaint);

    }

    private int dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private String formatDate(long timeStamp) {
        if (timeStamp <= 0) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        return format.format(new Date(timeStamp));
    }

    private String setPrecision(Double num, int scale) {
        BigDecimal bigDecimal = new BigDecimal(num);
        return bigDecimal.setScale(scale, BigDecimal.ROUND_DOWN).toPlainString();
    }

    private void resetStrokePaint(int colorId, int textSize) {
        strokePaint.setColor(colorId);
        strokePaint.setTextSize(sp2px(textSize));
    }

}
