package com.example.admin.klineview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 股票走势图 K线控件
 * Created by xiesuichao on 2018/6/29.
 */

public class KLineView extends View implements View.OnTouchListener, Handler.Callback {

    private GestureDetector gestureDetector;
    private Paint tickMarkPaint;
    private Paint datePaint;
    private Paint pricePaint;
    private Paint redPaint;
    private Paint greenPaint;
    private Paint clickGrayRectPaint;
    private Paint detailBgRectPaint;
    private Paint detailLinePaint;
    private Paint detailTextPaint;
    private Paint priceMa5Paint;
    private Paint priceMa10Paint;
    private Paint priceMa30Paint;
    private Paint volumeMa5Paint;
    private Paint volumeMa10Paint;
    private Paint volPaint;
    //十字线
    private Paint crossHairPaint;
    private Paint crossHairBluePaint;
    private Path blueTrianglePath;
    private Path maxPriceTrianglePath;
    private Path minPriceTrianglePath;
    private Paint crossHairBlueTextPaint;
    private Paint grayTimePaint;
    //边框
    private Paint framePaint;
    private Rect topMa5Rect = new Rect();
    private Rect topMa10Rect = new Rect();
    private Rect topMa30Rect = new Rect();
    private Rect detailTextRect = new Rect();
    //贝塞尔曲线
    private Path priceMa5BezierPath;
    private Path priceMa10BezierPath;
    private Path priceMa30BezierPath;
    private Path ema5BezierPath;
    private Path ema10BezierPath;
    private Path ema30BezierPath;
    private Path volumeEma5BezierPath;
    private Path volumeEma10BezierPath;
    private Path bollMbBezierPath;
    private Path bollUpBezierPath;
    private Path bollDnBezierPath;
    private Path deaBezierPath;
    private Path difBezierPath;
    private Path kLinePath;
    private Path dLinePath;
    private Path jLinePath;

    private float leftStart;
    private float topStart;
    private float rightEnd;
    private float bottomEnd;
    private int maxViewDataNum = 34;
    private int startDataNum = 0;
    private int initTotalListSize = 0;
    private float mulFirstDownX;
    private float mulSecondDownX;
    private float mulFirstDownY;
    private float mulSecondDownY;
    private float lastDiffMoveX;
    private float lastDiffMoveY;
    private float singleClickDownX;
    private float dispatchDownX;
    private float dispatchDownY;
    private float detailTextVerticalSpace;
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

    private int detailRectWidth;
    private int detailRectHeight;

    private String[] dateArr;
    private String[] detailLeftTitleArr;
    private List<KData> totalDataList = new ArrayList<>();
    private List<KData> viewDataList = new ArrayList<>();
    private List<String> detailRightDataList = new ArrayList<>();
    //水平线纵坐标
    private List<Float> horizontalYList = new ArrayList<>();
    //垂直线横坐标
    private List<Float> verticalXList = new ArrayList<>();
    private List<Pointer> priceMa5PointList = new ArrayList<>();
    private List<Pointer> priceMa10PointList = new ArrayList<>();
    private List<Pointer> priceMa30PointList = new ArrayList<>();
    private List<Pointer> volumeEma5PointList = new ArrayList<>();
    private List<Pointer> volumeEma10PointList = new ArrayList<>();
    private List<Pointer> ema5PointList = new ArrayList<>();
    private List<Pointer> ema10PointList = new ArrayList<>();
    private List<Pointer> ema30PointList = new ArrayList<>();
    private List<Pointer> bollMbPointList = new ArrayList<>();
    private List<Pointer> bollUpPointList = new ArrayList<>();
    private List<Pointer> bollDnPointList = new ArrayList<>();
    private List<Pointer> deaPointList = new ArrayList<>();
    private List<Pointer> difPointList = new ArrayList<>();
    private List<Pointer> kPointList = new ArrayList<>();
    private List<Pointer> dPointList = new ArrayList<>();
    private List<Pointer> jPointList = new ArrayList<>();

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

    private LongPressRunnable longPressRunnable = new LongPressRunnable();
    private double maxPrice = 0;
    private double maxPriceX = 0;
    private double minPrice = 0;
    private double minPriceX = 0;
    private double maxVolume;
    private float priceImgBot = 0;
    private float priceImgTop = 0;

    private float volumeTopStart;
    private double avgHeightPerPrice;
    private double avgRectWidth;
    private double avgHeightPerVolume;
    private float deputyTopY;
    private float deputyCenterY;
    private double avgHeightUpMacd;
    private double avgHeightDnMacd;
    private double avgHeightUpDea;
    private double avgHeightDnDea;
    private double avgHeightUpDif;
    private double avgHeightDnDif;
    private double avgHeightK;
    private double avgHeightD;
    private double avgHeightJ;
    private KData lastKData;
    private float volumeImgBot;
    private float verticalSpace;
    private float flingVelocityX;
    private OnRequestDataListListener requestListener;
    private Handler uiHandler;
    private QuotaThread quotaThread;
    private Handler mDelayHandler;
    private Runnable mDelayRunnable;
    private long finishStart;
    private List<KData> endDataList = new ArrayList<>();

    public KLineView(Context context) {
        this(context, null);
    }

    public KLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
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
//        totalDataList.add(data);
        endDataList.clear();
        endDataList.addAll(totalDataList.subList(totalDataList.size() - 29, totalDataList.size()));
        endDataList.add(data);
        quotaThread.quotaSingleCalculate(endDataList);
        PrintUtil.log("addData");
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
        quotaThread.quotaListCalculate(totalDataList);
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
        QuotaUtil.initMa(totalDataList);
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
                QuotaUtil.initMACD(totalDataList);
                break;

            case DEPUTY_IMG_KDJ:
                QuotaUtil.initKDJ(totalDataList);
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
                QuotaUtil.initMa(totalDataList);
                break;

            case MAIN_IMG_EMA:
                QuotaUtil.initEma(totalDataList);
                break;

            case MAIN_IMG_BOLL:
                QuotaUtil.initBoll(totalDataList);
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
                QuotaUtil.initMACD(totalDataList);
                break;

            case DEPUTY_IMG_KDJ:
                QuotaUtil.initKDJ(totalDataList);
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

    private void init() {
        super.setOnTouchListener(this);
        super.setClickable(true);
        super.setLongClickable(true);
        super.setFocusable(true);
        gestureDetector = new GestureDetector(getContext(), new CustomGestureListener());

        priceMa5Paint = new Paint();
        priceMa5Paint.setAntiAlias(true);
        priceMa5Paint.setTextSize(sp2px(10));
        priceMa5Paint.setStyle(Paint.Style.STROKE);
        priceMa5Paint.setColor(Color.parseColor("#FFA800"));

        priceMa10Paint = new Paint();
        priceMa10Paint.setAntiAlias(true);
        priceMa10Paint.setTextSize(sp2px(10));
        priceMa10Paint.setStyle(Paint.Style.STROKE);
        priceMa10Paint.setColor(Color.parseColor("#2668FF"));

        priceMa30Paint = new Paint();
        priceMa30Paint.setAntiAlias(true);
        priceMa30Paint.setTextSize(sp2px(10));
        priceMa30Paint.setStyle(Paint.Style.STROKE);
        priceMa30Paint.setColor(Color.parseColor("#FF45A1"));

        volumeMa5Paint = new Paint();
        volumeMa5Paint.setAntiAlias(true);
        volumeMa5Paint.setTextSize(sp2px(10));
        volumeMa5Paint.setStyle(Paint.Style.STROKE);
        volumeMa5Paint.setColor(Color.parseColor("#FF45A1"));

        volumeMa10Paint = new Paint();
        volumeMa10Paint.setAntiAlias(true);
        volumeMa10Paint.setTextSize(sp2px(10));
        volumeMa10Paint.setStyle(Paint.Style.STROKE);
        volumeMa10Paint.setColor(Color.parseColor("#FF45A1"));

        volPaint = new Paint();
        volPaint.setAntiAlias(true);
        volPaint.setTextSize(sp2px(10));
        volPaint.setStyle(Paint.Style.STROKE);
        volPaint.setColor(Color.parseColor("#9BACBD"));

        crossHairPaint = new Paint();
        crossHairPaint.setAntiAlias(true);
        crossHairPaint.setStyle(Paint.Style.STROKE);
        crossHairPaint.setColor(Color.parseColor("#828EA2"));

        crossHairBluePaint = new Paint();
        crossHairBluePaint.setAntiAlias(true);
        crossHairBluePaint.setStyle(Paint.Style.FILL);
        crossHairBluePaint.setColor(Color.parseColor("#3193FF"));

        blueTrianglePath = new Path();
        maxPriceTrianglePath = new Path();
        minPriceTrianglePath = new Path();

        crossHairBlueTextPaint = new Paint();
        crossHairBlueTextPaint.setAntiAlias(true);
        crossHairBlueTextPaint.setTextSize(sp2px(10));
        crossHairBlueTextPaint.setColor(Color.parseColor("#FFFFFF"));

        grayTimePaint = new Paint();
        grayTimePaint.setAntiAlias(true);
        grayTimePaint.setColor(Color.parseColor("#C0C6C9"));

        framePaint = new Paint();
        framePaint.setAntiAlias(true);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setColor(Color.parseColor("#B5C0D0"));

        datePaint = new Paint();
        datePaint.setAntiAlias(true);
        datePaint.setColor(Color.parseColor("#9BACBD"));
        datePaint.setTextSize(sp2px(8));

        tickMarkPaint = new Paint();
        tickMarkPaint.setAntiAlias(true);
        tickMarkPaint.setColor(Color.parseColor("#F7F7FB"));

        pricePaint = new Paint();
        pricePaint.setAntiAlias(true);
        pricePaint.setColor(Color.parseColor("#333333"));
        pricePaint.setTextSize(sp2px(10));

        redPaint = new Paint();
        redPaint.setAntiAlias(true);
        redPaint.setColor(Color.parseColor("#FF424A"));
        redPaint.setStrokeWidth(1);
        redPaint.setTextSize(sp2px(10));

        greenPaint = new Paint();
        greenPaint.setAntiAlias(true);
        greenPaint.setColor(Color.parseColor("#00B23E"));
        greenPaint.setStrokeWidth(1);
        greenPaint.setTextSize(sp2px(10));

        clickGrayRectPaint = new Paint();
        clickGrayRectPaint.setAntiAlias(true);
        clickGrayRectPaint.setColor(Color.parseColor("#8065707c"));

        detailBgRectPaint = new Paint();
        detailBgRectPaint.setAntiAlias(true);
        detailBgRectPaint.setColor(Color.parseColor("#cc294058"));

        detailLinePaint = new Paint();
        detailLinePaint.setAntiAlias(true);
        detailLinePaint.setColor(Color.parseColor("#e6ffffff"));

        detailTextPaint = new Paint();
        detailTextPaint.setAntiAlias(true);
        detailTextPaint.setColor(Color.parseColor("#808F9E"));
        detailTextPaint.setTextSize(sp2px(10));

        priceMa5BezierPath = new Path();
        priceMa10BezierPath = new Path();
        priceMa30BezierPath = new Path();
        ema5BezierPath = new Path();
        ema10BezierPath = new Path();
        ema30BezierPath = new Path();
        volumeEma5BezierPath = new Path();
        volumeEma10BezierPath = new Path();
        bollMbBezierPath = new Path();
        bollUpBezierPath = new Path();
        bollDnBezierPath = new Path();
        deaBezierPath = new Path();
        difBezierPath = new Path();
        kLinePath = new Path();
        dLinePath = new Path();
        jLinePath = new Path();

        detailRectWidth = dp2px(103);
        detailRectHeight = dp2px(120);
        detailTextVerticalSpace = (detailRectHeight - dp2px(4)) / 8;

        dateArr = new String[]{"06-29 10:00", "06-29 10:01", "06-29 10:02", "06-29 10:03", "06-29 10:04"};

        detailLeftTitleArr = new String[]{"时间", "开", "高", "低", "收", "涨跌额", "涨跌幅", "成交量"};

        initQuotaThread();

        initStopDelay();

    }

    private void initQuotaThread() {
        uiHandler = new Handler(this);
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
            PrintUtil.log("endDataList.end", endDataList.get(endDataList.size() - 1).toString());
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
        drawDate(canvas);
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
                mulSecondDownX = event.getX(1);
                mulSecondDownY = event.getY(1);
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
                PrintUtil.log("maxViewDataNum", maxViewDataNum);
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
            PrintUtil.log("requestBeforeData");
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
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(leftStart + horizontalSpace * (i) + dp2px(6),
                    topStart + dp2px(18),
                    leftStart + horizontalSpace * (i) + dp2px(6),
                    bottomEnd - dp2px(20),
                    tickMarkPaint);
            verticalXList.add(leftStart + horizontalSpace * (i) + dp2px(6));
        }
        //水平刻度线
        verticalSpace = (bottomEnd - topStart - dp2px(38)) / 5;
        for (int i = 0; i < 6; i++) {
            canvas.drawLine(leftStart + dp2px(6),
                    topStart + verticalSpace * i + dp2px(18),
                    rightEnd,
                    topStart + verticalSpace * i + dp2px(18),
                    tickMarkPaint);
            horizontalYList.add(topStart + verticalSpace * i + dp2px(18));
        }
        //副图中线
        deputyTopY = horizontalYList.get(4) + dp2px(17);
        deputyCenterY = deputyTopY + (verticalSpace - dp2px(15)) / 2;
        canvas.drawLine(leftStart + dp2px(6),
                deputyCenterY,
                rightEnd,
                deputyCenterY,
                tickMarkPaint);
        //数量中线
        if (isShowDeputy) {
            canvas.drawLine(leftStart + dp2px(6),
                    horizontalYList.get(3) + verticalSpace / 2,
                    rightEnd,
                    horizontalYList.get(3) + verticalSpace / 2,
                    tickMarkPaint);
        }
    }

    //主副图蜡烛图
    private void drawMainDeputyRect(Canvas canvas) {
        avgRectWidth = (verticalXList.get(verticalXList.size() - 1)
                - verticalXList.get(0)) / maxViewDataNum;
        maxPrice = viewDataList.get(0).getMaxPrice();
        minPrice = viewDataList.get(0).getMinPrice();
        maxVolume = viewDataList.get(0).getVolume();
        double maxMacd = viewDataList.get(0).getMacd();
        double minMacd = viewDataList.get(0).getMacd();
        double maxDea = viewDataList.get(0).getDea();
        double minDea = viewDataList.get(0).getDea();
        double maxDif = viewDataList.get(0).getDif();
        double minDif = viewDataList.get(0).getDif();
        double maxK = viewDataList.get(0).getK();
        double maxD = viewDataList.get(0).getD();
        double maxJ = viewDataList.get(0).getJ();

        for (int i = 0; i < viewDataList.size(); i++) {
            viewDataList.get(i).setLeftX(verticalXList.get(0) + avgRectWidth * i);
            viewDataList.get(i).setRightX(verticalXList.get(0) + avgRectWidth * (i + 1));
            if (viewDataList.get(i).getMaxPrice() >= maxPrice) {
                maxPrice = viewDataList.get(i).getMaxPrice();
                maxPriceX = viewDataList.get(i).getLeftX() + avgRectWidth / 2;
            }
            if (viewDataList.get(i).getMinPrice() <= minPrice) {
                minPrice = viewDataList.get(i).getMinPrice();
                minPriceX = viewDataList.get(i).getLeftX() + avgRectWidth / 2;
            }
            if (viewDataList.get(i).getVolume() >= maxVolume) {
                maxVolume = viewDataList.get(i).getVolume();
            }
            if (viewDataList.get(i).getMacd() >= maxMacd) {
                maxMacd = viewDataList.get(i).getMacd();
            }
            if (viewDataList.get(i).getMacd() <= minMacd) {
                minMacd = viewDataList.get(i).getMacd();
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
            if (viewDataList.get(i).getK() >= maxK) {
                maxK = viewDataList.get(i).getK();
            }
            if (viewDataList.get(i).getD() >= maxD) {
                maxD = viewDataList.get(i).getD();
            }
            if (viewDataList.get(i).getJ() >= maxJ) {
                maxJ = viewDataList.get(i).getJ();
            }
        }
        if (!isShowDeputy) {
            priceImgBot = horizontalYList.get(4);
            volumeImgBot = horizontalYList.get(5);
        } else {
            priceImgBot = horizontalYList.get(3);
            volumeImgBot = horizontalYList.get(4);
        }
        //priceData
        avgHeightPerPrice = ((priceImgBot - horizontalYList.get(0)) / (maxPrice - minPrice));
        //volumeData
        volumeTopStart = volumeImgBot - verticalSpace / 2;
        avgHeightPerVolume = verticalSpace / 2 / maxVolume;
        //MACD
        avgHeightUpMacd = 0;
        avgHeightDnMacd = 0;
        if (maxMacd > 0) {
            avgHeightUpMacd = (deputyCenterY - deputyTopY) / maxMacd;
        }
        if (minMacd < 0) {
            avgHeightDnMacd = (deputyCenterY - deputyTopY) / minMacd;
        }
        //DEA
        avgHeightUpDea = 0;
        avgHeightDnDea = 0;
        if (maxDea > 0) {
            avgHeightUpDea = (deputyCenterY - deputyTopY) / maxDea;
        } else if (minDea < 0) {
            avgHeightDnDea = (deputyCenterY - deputyTopY) / minDea;
        }
        //DIF
        avgHeightUpDif = 0;
        avgHeightDnDif = 0;
        if (maxDif > 0) {
            avgHeightUpDif = (deputyCenterY - deputyTopY) / maxDif;
        } else if (minDif < 0) {
            avgHeightDnDif = (deputyCenterY - deputyTopY) / minDif;
        }
        //K
        avgHeightK = (horizontalYList.get(5) - deputyTopY - dp2px(10)) / maxK;
        //D
        avgHeightD = (horizontalYList.get(5) - deputyTopY - dp2px(10)) / maxD;
        //J
        avgHeightJ = (horizontalYList.get(5) - deputyTopY - dp2px(10)) / maxJ;

        for (int i = 0; i < viewDataList.size(); i++) {
            Paint rectPaint;
            //drawPriceRectAndLine
            double openPrice = viewDataList.get(i).getOpenPrice();
            double closedPrice = viewDataList.get(i).getClosePrice();
            double higherPrice;
            double lowerPrice;
            if (openPrice >= closedPrice) {
                higherPrice = openPrice;
                lowerPrice = closedPrice;
                rectPaint = greenPaint;
            } else {
                higherPrice = closedPrice;
                lowerPrice = openPrice;
                rectPaint = redPaint;
            }
            viewDataList.get(i).setCloseY((float) (horizontalYList.get(0) + (maxPrice - closedPrice) * avgHeightPerPrice));
            canvas.drawRect((float) (verticalXList.get(0) + avgRectWidth * i + dp2px(0.5f)),
                    (float) (horizontalYList.get(0) + (maxPrice - higherPrice) * avgHeightPerPrice),
                    (float) (verticalXList.get(0) + avgRectWidth * (i + 1) - dp2px(0.5f)),
                    (float) (horizontalYList.get(0) + (maxPrice - lowerPrice) * avgHeightPerPrice),
                    rectPaint);
            canvas.drawLine((float) (verticalXList.get(0) + avgRectWidth * (i * 2 + 1) / 2),
                    (float) (horizontalYList.get(0)
                            + (maxPrice - viewDataList.get(i).getMaxPrice()) * avgHeightPerPrice),
                    (float) (verticalXList.get(0) + avgRectWidth * (i * 2 + 1) / 2),
                    (float) (horizontalYList.get(0)
                            + (maxPrice - viewDataList.get(i).getMinPrice()) * avgHeightPerPrice),
                    rectPaint);
            //drawVolumeRect
            canvas.drawRect((float) (verticalXList.get(0) + avgRectWidth * i + dp2px(0.5f)),
                    (float) (volumeImgBot - viewDataList.get(i).getVolume() * avgHeightPerVolume),
                    (float) (verticalXList.get(0) + avgRectWidth * (i + 1) - dp2px(0.5f)),
                    volumeImgBot,
                    rectPaint);
            //MACD
            if (isShowDeputy && deputyImgType == DEPUTY_IMG_MACD) {
                double macd = viewDataList.get(i).getMacd();
                if (macd > 0) {
                    rectPaint = redPaint;
                    canvas.drawRect((float) (verticalXList.get(0) + avgRectWidth * i + dp2px(0.5f)),
                            (float) (deputyCenterY - macd * avgHeightUpMacd),
                            (float) (verticalXList.get(0) + avgRectWidth * (i + 1) - dp2px(0.5f)),
                            deputyCenterY,
                            rectPaint);

                } else {
                    rectPaint = greenPaint;
                    canvas.drawRect((float) (verticalXList.get(0) + avgRectWidth * i + dp2px(0.5f)),
                            deputyCenterY,
                            (float) (verticalXList.get(0) + avgRectWidth * (i + 1) - dp2px(0.5f)),
                            (float) (deputyCenterY + Math.abs(macd * avgHeightDnMacd)),
                            rectPaint);
                }
            }
        }
    }

    //贝塞尔曲线
    private void drawBezierCurve(Canvas canvas) {
        priceMa5PointList.clear();
        priceMa10PointList.clear();
        priceMa30PointList.clear();
        ema5PointList.clear();
        ema10PointList.clear();
        ema30PointList.clear();
        volumeEma5PointList.clear();
        volumeEma10PointList.clear();
        bollMbPointList.clear();
        bollUpPointList.clear();
        bollDnPointList.clear();
        deaPointList.clear();
        difPointList.clear();
        kPointList.clear();
        dPointList.clear();
        jPointList.clear();

        for (int i = 0; i < viewDataList.size(); i++) {
            if (!viewDataList.get(i).isInitFinish()) {
                break;
            }
            Pointer volumeMa5Point = new Pointer();
            if (viewDataList.get(i).getVolumeMa5() > 0) {
                volumeMa5Point.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                volumeMa5Point.setY((float) (volumeImgBot
                        - viewDataList.get(i).getVolumeMa5() * avgHeightPerVolume));
                volumeEma5PointList.add(volumeMa5Point);
            }
            Pointer volumeMa10Point = new Pointer();
            if (viewDataList.get(i).getVolumeMa10() > 0) {
                volumeMa10Point.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                volumeMa10Point.setY((float) (volumeImgBot
                        - viewDataList.get(i).getVolumeMa10() * avgHeightPerVolume));
                volumeEma10PointList.add(volumeMa10Point);
            }
            switch (mainImgType) {
                case MAIN_IMG_MA:
                    Pointer priceMa5Point = new Pointer();
                    if (viewDataList.get(i).getPriceMa5() > 0) {
                        priceMa5Point.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        priceMa5Point.setY((float) (horizontalYList.get(0)
                                + (maxPrice - viewDataList.get(i).getPriceMa5()) * avgHeightPerPrice));
                        priceMa5PointList.add(priceMa5Point);
                    }
                    Pointer priceMa10Point = new Pointer();
                    if (viewDataList.get(i).getPriceMa10() > 0) {
                        priceMa10Point.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        priceMa10Point.setY((float) (horizontalYList.get(0)
                                + (maxPrice - viewDataList.get(i).getPriceMa10()) * avgHeightPerPrice));
                        priceMa10PointList.add(priceMa10Point);
                    }
                    Pointer priceMa30Point = new Pointer();
                    if (viewDataList.get(i).getPriceMa30() > 0) {
                        priceMa30Point.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        priceMa30Point.setY((float) (horizontalYList.get(0)
                                + (maxPrice - viewDataList.get(i).getPriceMa30()) * avgHeightPerPrice));
                        priceMa30PointList.add(priceMa30Point);
                    }
                    break;

                case MAIN_IMG_EMA:
                    Pointer ema5Point = new Pointer();
                    if (viewDataList.get(i).getEma5() > 0) {
                        ema5Point.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        ema5Point.setY((float) (horizontalYList.get(0)
                                + (maxPrice - viewDataList.get(i).getEma5()) * avgHeightPerPrice));
                        ema5PointList.add(ema5Point);
                    }
                    Pointer ema10Point = new Pointer();
                    if (viewDataList.get(i).getEma10() > 0) {
                        ema10Point.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        ema10Point.setY((float) (horizontalYList.get(0)
                                + (maxPrice - viewDataList.get(i).getEma10()) * avgHeightPerPrice));
                        ema10PointList.add(ema10Point);
                    }
                    Pointer ema30Point = new Pointer();
                    if (viewDataList.get(i).getEma30() > 0) {
                        ema30Point.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        ema30Point.setY((float) (horizontalYList.get(0)
                                + (maxPrice - viewDataList.get(i).getEma30()) * avgHeightPerPrice));
                        ema30PointList.add(ema30Point);
                    }
                    break;

                case MAIN_IMG_BOLL:
                    Pointer bollMbPoint = new Pointer();
                    if (viewDataList.get(i).getBollMb() > 0) {
                        bollMbPoint.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        bollMbPoint.setY((float) (horizontalYList.get(0)
                                + (maxPrice - viewDataList.get(i).getBollMb()) * avgHeightPerPrice));
                        bollMbPointList.add(bollMbPoint);
                    }
                    Pointer bollUpPoint = new Pointer();
                    if (viewDataList.get(i).getBollUp() > 0) {
                        bollUpPoint.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        bollUpPoint.setY((float) (horizontalYList.get(0)
                                + (maxPrice - viewDataList.get(i).getBollUp()) * avgHeightPerPrice));
                        bollUpPointList.add(bollUpPoint);
                    }
                    Pointer bollDnPoint = new Pointer();
                    if (viewDataList.get(i).getBollDn() > 0) {
                        bollDnPoint.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        bollDnPoint.setY((float) (horizontalYList.get(0)
                                + (maxPrice - viewDataList.get(i).getBollDn()) * avgHeightPerPrice));
                        bollDnPointList.add(bollDnPoint);
                    }
                    break;
            }

            if (isShowDeputy && deputyImgType == DEPUTY_IMG_MACD) {
                Pointer deaPoint = new Pointer();
                if (viewDataList.get(i).getDea() > 0) {
                    deaPoint.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    deaPoint.setY((float) (deputyCenterY - viewDataList.get(i).getDea() * avgHeightUpDea));
                } else {
                    deaPoint.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    deaPoint.setY((float) (deputyCenterY + Math.abs(viewDataList.get(i).getDea() * avgHeightDnDea)));
                }
                deaPointList.add(deaPoint);

                Pointer difPoint = new Pointer();
                if (viewDataList.get(i).getDif() > 0) {
                    difPoint.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    difPoint.setY((float) (deputyCenterY - viewDataList.get(i).getDif() * avgHeightUpDif));
                } else {
                    difPoint.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    difPoint.setY((float) (deputyCenterY + Math.abs(viewDataList.get(i).getDif() * avgHeightDnDif)));
                }
                difPointList.add(difPoint);

            } else if (isShowDeputy && deputyImgType == DEPUTY_IMG_KDJ) {
                Pointer kPoint = new Pointer();
                if (viewDataList.get(i).getK() > 0) {
                    kPoint.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    kPoint.setY((float) (horizontalYList.get(5) - viewDataList.get(i).getK() * avgHeightK));
                    kPointList.add(kPoint);
                }

                Pointer dPoint = new Pointer();
                if (viewDataList.get(i).getD() > 0) {
                    dPoint.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    dPoint.setY((float) (horizontalYList.get(5) - viewDataList.get(i).getD() * avgHeightD));
                    dPointList.add(dPoint);
                }

                Pointer jPoint = new Pointer();
                if (viewDataList.get(i).getJ() > 0) {
                    jPoint.setX((float) (viewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    jPoint.setY((float) (horizontalYList.get(5) - viewDataList.get(i).getJ() * avgHeightJ));
                    jPointList.add(jPoint);
                }
            }

        }

        drawVolumeMaBezierCurve(canvas);

        switch (mainImgType) {
            case MAIN_IMG_MA:
                drawPriceMaBezierCurve(canvas);
                break;

            case MAIN_IMG_EMA:
                drawEmaBezierCurve(canvas);
                break;

            case MAIN_IMG_BOLL:
                drawBollBezierCurve(canvas);
                break;
        }

        if (isShowDeputy && deputyImgType == DEPUTY_IMG_MACD) {
            drawDeaDifBezier(canvas);
        } else if (isShowDeputy && deputyImgType == DEPUTY_IMG_KDJ) {
            drawKdjLine(canvas);
        }

    }

    //price MA曲线
    private void drawPriceMaBezierCurve(Canvas canvas) {
        QuotaUtil.setBezierPath(priceMa5PointList, priceMa5BezierPath);
        canvas.drawPath(priceMa5BezierPath, priceMa5Paint);

        QuotaUtil.setBezierPath(priceMa10PointList, priceMa10BezierPath);
        canvas.drawPath(priceMa10BezierPath, priceMa10Paint);

        QuotaUtil.setBezierPath(priceMa30PointList, priceMa30BezierPath);
        canvas.drawPath(priceMa30BezierPath, priceMa30Paint);
    }

    //volume MA曲线
    private void drawVolumeMaBezierCurve(Canvas canvas) {
        QuotaUtil.setBezierPath(volumeEma5PointList, volumeEma5BezierPath);
        canvas.drawPath(volumeEma5BezierPath, priceMa5Paint);

        QuotaUtil.setBezierPath(volumeEma10PointList, volumeEma10BezierPath);
        canvas.drawPath(volumeEma10BezierPath, priceMa10Paint);
    }

    //EMA曲线
    private void drawEmaBezierCurve(Canvas canvas) {
        QuotaUtil.setBezierPath(ema5PointList, ema5BezierPath);
        canvas.drawPath(ema5BezierPath, priceMa5Paint);

        QuotaUtil.setBezierPath(ema10PointList, ema10BezierPath);
        canvas.drawPath(ema10BezierPath, priceMa10Paint);

        QuotaUtil.setBezierPath(ema30PointList, ema30BezierPath);
        canvas.drawPath(ema30BezierPath, priceMa30Paint);
    }

    //BOLL曲线
    private void drawBollBezierCurve(Canvas canvas) {
        QuotaUtil.setBezierPath(bollMbPointList, bollMbBezierPath);
        canvas.drawPath(bollMbBezierPath, priceMa5Paint);

        QuotaUtil.setBezierPath(bollUpPointList, bollUpBezierPath);
        canvas.drawPath(bollUpBezierPath, priceMa10Paint);

        QuotaUtil.setBezierPath(bollDnPointList, bollDnBezierPath);
        canvas.drawPath(bollDnBezierPath, priceMa30Paint);
    }

    //DEA DIF曲线
    private void drawDeaDifBezier(Canvas canvas) {
        QuotaUtil.setBezierPath(deaPointList, deaBezierPath);
        canvas.drawPath(deaBezierPath, priceMa10Paint);

        QuotaUtil.setBezierPath(difPointList, difBezierPath);
        canvas.drawPath(difBezierPath, priceMa30Paint);
    }

    //KDJ
    private void drawKdjLine(Canvas canvas) {
        QuotaUtil.setLinePath(kPointList, kLinePath);
        canvas.drawPath(kLinePath, priceMa5Paint);

        QuotaUtil.setLinePath(dPointList, dLinePath);
        canvas.drawPath(dLinePath, priceMa10Paint);

        QuotaUtil.setLinePath(jPointList, jLinePath);
        canvas.drawPath(jLinePath, priceMa30Paint);
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
                    detailRightDataList.add(ArithUtil.setPrecision(lastKData.getOpenPrice(), 2));
                    detailRightDataList.add(ArithUtil.setPrecision(lastKData.getMaxPrice(), 2));
                    detailRightDataList.add(ArithUtil.setPrecision(lastKData.getMinPrice(), 2));
                    detailRightDataList.add(ArithUtil.setPrecision(lastKData.getClosePrice(), 2));
                    double upDnAmount = lastKData.getUpDnAmount();
                    if (upDnAmount > 0) {
                        detailRightDataList.add("+" + ArithUtil.setPrecision(upDnAmount, 2));
                        detailRightDataList.add("+" + ArithUtil.setPrecision(lastKData.getUpDnRate() * 100, 2) + "%");
                    } else {
                        detailRightDataList.add(ArithUtil.setPrecision(upDnAmount, 2));
                        detailRightDataList.add(ArithUtil.setPrecision(lastKData.getUpDnRate() * 100, 2) + "%");
                    }
                    detailRightDataList.add(ArithUtil.setPrecision(lastKData.getVolume(), 2));
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
        canvas.drawLine((float) (lastKData.getLeftX() + avgRectWidth / 2),
                topStart,
                (float) (lastKData.getLeftX() + avgRectWidth / 2),
                bottomEnd,
                crossHairPaint);

        //水平
        canvas.drawLine(verticalXList.get(0),
                (float) lastKData.getCloseY(),
                verticalXList.get(verticalXList.size() - 1),
                (float) lastKData.getCloseY(),
                crossHairPaint);

        //时间指示器
        RectF grayRectF = new RectF(singleClickDownX - dp2px(25),
                bottomEnd - dp2px(20),
                singleClickDownX + dp2px(25),
                bottomEnd);
        canvas.drawRoundRect(grayRectF, 4, 4, grayTimePaint);

        //时间text
        String moveTime = formatDate(lastKData.getTime());
        datePaint.setColor(Color.parseColor("#FFFFFF"));
        canvas.drawText(moveTime,
                singleClickDownX - datePaint.measureText(moveTime) / 2,
                bottomEnd - dp2px(7),
                datePaint);

        //蓝色指示器
        RectF blueRectF = new RectF(rightEnd - dp2px(38),
                (float) lastKData.getCloseY() - dp2px(7),
                rightEnd - dp2px(1),
                (float) lastKData.getCloseY() + dp2px(7));
        canvas.drawRoundRect(blueRectF, 4, 4, crossHairBluePaint);

        blueTrianglePath.reset();
        blueTrianglePath.moveTo(verticalXList.get(verticalXList.size() - 1), (float) lastKData.getCloseY());
        blueTrianglePath.lineTo(rightEnd - dp2px(37), (float) lastKData.getCloseY() - dp2px(3));
        blueTrianglePath.lineTo(rightEnd - dp2px(37), (float) lastKData.getCloseY() + dp2px(3));
        blueTrianglePath.close();
        canvas.drawPath(blueTrianglePath, crossHairBluePaint);

        if (!isShowDeputy) {
            //price
            double avgPricePerHeight = (maxPrice - minPrice)
                    / (horizontalYList.get(4) - horizontalYList.get(0));
            String movePrice = ArithUtil.setPrecision(maxPrice
                    - avgPricePerHeight * ((float) lastKData.getCloseY() - horizontalYList.get(0)), 2);
            Rect textRect = new Rect();
            crossHairBlueTextPaint.getTextBounds(movePrice, 0, movePrice.length(), textRect);
            canvas.drawText(movePrice,
                    rightEnd - dp2px(38) + (blueRectF.width() - textRect.width()) / 2,
                    (float) lastKData.getCloseY() + dp2px(7) - (blueRectF.height() - textRect.height()) / 2,
                    crossHairBlueTextPaint);
        } else {
            double avgPricePerHeight = (maxPrice - minPrice)
                    / (horizontalYList.get(3) - horizontalYList.get(0));
            String movePrice = ArithUtil.setPrecision(maxPrice
                    - avgPricePerHeight * ((float) lastKData.getCloseY() - horizontalYList.get(0)), 2);
            Rect textRect = new Rect();
            crossHairBlueTextPaint.getTextBounds(movePrice, 0, movePrice.length(), textRect);
            canvas.drawText(movePrice,
                    rightEnd - dp2px(38) + (blueRectF.width() - textRect.width()) / 2,
                    (float) lastKData.getCloseY() + dp2px(7) - (blueRectF.height() - textRect.height()) / 2,
                    crossHairBlueTextPaint);
        }
    }

    //最高价、最低价标签
    private void drawMaxMinPriceLabel(Canvas canvas) {
        //maxPrice
        Rect maxPriceRect = new Rect();
        String maxPriceStr = ArithUtil.setPrecision(maxPrice, 2);
        crossHairBlueTextPaint.getTextBounds(maxPriceStr, 0, maxPriceStr.length(), maxPriceRect);

        RectF maxRectF = new RectF((float) (maxPriceX + dp2px(3)),
                horizontalYList.get(0) - dp2px(7),
                (float) (maxPriceX + maxPriceRect.width() + dp2px(8)),
                horizontalYList.get(0) + dp2px(7));
        canvas.drawRoundRect(maxRectF, 4, 4, grayTimePaint);

        maxPriceTrianglePath.reset();
        maxPriceTrianglePath.moveTo((float) maxPriceX, horizontalYList.get(0));
        maxPriceTrianglePath.lineTo((float) (maxPriceX + dp2px(4)), horizontalYList.get(0) - dp2px(3));
        maxPriceTrianglePath.lineTo((float) (maxPriceX + dp2px(4)), horizontalYList.get(0) + dp2px(3));
        maxPriceTrianglePath.close();
        canvas.drawPath(maxPriceTrianglePath, grayTimePaint);

        canvas.drawText(maxPriceStr,
                (float) (maxPriceX + dp2px(5)),
                horizontalYList.get(0) + maxPriceRect.height() / 2,
                crossHairBlueTextPaint);

        //minPrice
        Rect minPriceRect = new Rect();
        String minPriceStr = ArithUtil.setPrecision(minPrice, 2);
        crossHairBlueTextPaint.getTextBounds(minPriceStr, 0, minPriceStr.length(), minPriceRect);

        RectF minRectF = new RectF((float) (minPriceX + dp2px(2)),
                priceImgBot - dp2px(7),
                (float) (minPriceX + minPriceRect.width() + dp2px(8)),
                priceImgBot + dp2px(7));
        canvas.drawRoundRect(minRectF, 4, 4, grayTimePaint);

        minPriceTrianglePath.reset();
        minPriceTrianglePath.moveTo((float) minPriceX, priceImgBot);
        minPriceTrianglePath.lineTo((float) (minPriceX + dp2px(4)), priceImgBot - dp2px(3));
        minPriceTrianglePath.lineTo((float) (minPriceX + dp2px(4)), priceImgBot + dp2px(3));
        minPriceTrianglePath.close();
        canvas.drawPath(minPriceTrianglePath, grayTimePaint);

        canvas.drawText(minPriceStr,
                (float) (minPriceX + dp2px(4)),
                priceImgBot + minPriceRect.height() / 2,
                crossHairBlueTextPaint);

    }

    private void drawDetailData(Canvas canvas) {
        if (lastKData == null || !isShowDetail) {
            return;
        }
        detailTextPaint.getTextBounds(detailLeftTitleArr[0], 0, detailLeftTitleArr[0].length(), detailTextRect);

        if (singleClickDownX <= getMeasuredWidth() / 2) {
            //边框(右侧)
            canvas.drawRect(verticalXList.get(verticalXList.size() - 1) - detailRectWidth,
                    horizontalYList.get(0),
                    verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(0) + detailRectHeight,
                    detailLinePaint);

            canvas.drawLine(verticalXList.get(verticalXList.size() - 1) - detailRectWidth,
                    horizontalYList.get(0),
                    verticalXList.get(verticalXList.size() - 1) - detailRectWidth,
                    horizontalYList.get(0) + detailRectHeight,
                    framePaint);

            canvas.drawLine(verticalXList.get(verticalXList.size() - 1) - detailRectWidth,
                    horizontalYList.get(0),
                    verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(0),
                    framePaint);

            canvas.drawLine(verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(0),
                    verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(0) + detailRectHeight,
                    framePaint);

            canvas.drawLine(verticalXList.get(verticalXList.size() - 1) - detailRectWidth,
                    horizontalYList.get(0) + detailRectHeight,
                    verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(0) + detailRectHeight,
                    framePaint);

            //详情字段
            for (int i = 0; i < detailLeftTitleArr.length; i++) {
                canvas.drawText(detailLeftTitleArr[i],
                        verticalXList.get(verticalXList.size() - 1) - detailRectWidth + dp2px(4),
                        horizontalYList.get(0) + detailTextVerticalSpace * i
                                + detailTextRect.height() + (detailTextVerticalSpace - detailTextRect.height()) / 2,
                        detailTextPaint);
            }

            //详情数据
            Paint detailPaint;
            for (int i = 0; i < detailRightDataList.size(); i++) {
                if (i == 5 || i == 6) {
                    if (lastKData.getUpDnAmount() > 0) {
                        detailPaint = redPaint;
                    } else {
                        detailPaint = greenPaint;
                    }
                } else {
                    detailPaint = detailTextPaint;
                }
                canvas.drawText(detailRightDataList.get(i),
                        verticalXList.get(verticalXList.size() - 1) - dp2px(4)
                                - detailTextPaint.measureText(detailRightDataList.get(i)),
                        horizontalYList.get(0) + detailTextVerticalSpace * i
                                + detailTextRect.height() + (detailTextVerticalSpace - detailTextRect.height()) / 2,
                        detailPaint);
            }

        } else {
            //边框(左侧)
            canvas.drawRect(verticalXList.get(0),
                    horizontalYList.get(0),
                    verticalXList.get(0) + detailRectWidth,
                    horizontalYList.get(0) + detailRectHeight,
                    detailLinePaint);

            canvas.drawLine(verticalXList.get(0),
                    horizontalYList.get(0),
                    verticalXList.get(0),
                    horizontalYList.get(0) + detailRectHeight,
                    framePaint);

            canvas.drawLine(verticalXList.get(0),
                    horizontalYList.get(0),
                    verticalXList.get(0) + detailRectWidth,
                    horizontalYList.get(0),
                    framePaint);

            canvas.drawLine(verticalXList.get(0) + detailRectWidth,
                    horizontalYList.get(0),
                    verticalXList.get(0) + detailRectWidth,
                    horizontalYList.get(0) + detailRectHeight,
                    framePaint);

            canvas.drawLine(verticalXList.get(0),
                    horizontalYList.get(0) + detailRectHeight,
                    verticalXList.get(0) + detailRectWidth,
                    horizontalYList.get(0) + detailRectHeight,
                    framePaint);

            //文字详情
            for (int i = 0; i < detailLeftTitleArr.length; i++) {
                canvas.drawText(detailLeftTitleArr[i],
                        verticalXList.get(0) + dp2px(4),
                        horizontalYList.get(0) + detailTextVerticalSpace * i
                                + detailTextRect.height() + (detailTextVerticalSpace - detailTextRect.height()) / 2,
                        detailTextPaint);
            }

            //详情数据
            Paint detailPaint;
            for (int i = 0; i < detailRightDataList.size(); i++) {
                if (i == 5 || i == 6) {
                    if (lastKData.getUpDnAmount() > 0) {
                        detailPaint = redPaint;
                    } else {
                        detailPaint = greenPaint;
                    }
                } else {
                    detailPaint = detailTextPaint;
                }
                canvas.drawText(detailRightDataList.get(i),
                        verticalXList.get(0) + detailRectWidth - dp2px(4)
                                - detailTextPaint.measureText(detailRightDataList.get(i)),
                        horizontalYList.get(0) + detailTextVerticalSpace * i
                                + detailTextRect.height() + (detailTextVerticalSpace - detailTextRect.height()) / 2,
                        detailPaint);
            }
        }
    }

    //顶部价格MA
    private void drawTopPriceMAData(Canvas canvas) {
        String ma5Str = mMa5 + ArithUtil.setPrecision(lastKData.getPriceMa5(), 2);
        String ma10Str = mMa10 + ArithUtil.setPrecision(lastKData.getPriceMa10(), 2);
        String ma30Str = mMa30 + ArithUtil.setPrecision(lastKData.getPriceMa30(), 2);

        priceMa5Paint.getTextBounds(ma5Str, 0, ma5Str.length(), topMa5Rect);
        canvas.drawText(ma5Str,
                leftStart + dp2px(6),
                topStart + topMa5Rect.height() + dp2px(6),
                priceMa5Paint);

        priceMa10Paint.getTextBounds(ma10Str, 0, ma10Str.length(), topMa10Rect);
        canvas.drawText(ma10Str,
                leftStart + dp2px(6) + topMa5Rect.width() + dp2px(10),
                topStart + topMa5Rect.height() + dp2px(6),
                priceMa10Paint);

        priceMa30Paint.getTextBounds(ma30Str, 0, ma30Str.length(), topMa30Rect);
        canvas.drawText(ma30Str,
                leftStart + dp2px(6) + topMa5Rect.width() + topMa10Rect.width() + dp2px(10) * 2,
                topStart + topMa5Rect.height() + dp2px(6),
                priceMa30Paint);
    }

    //底部MA
    private void drawBotMAData(Canvas canvas) {
        //VOL
        String volStr = mVol + ArithUtil.setPrecision(lastKData.getVolume(), 2);
        Rect volRect = new Rect();
        volPaint.getTextBounds(volStr, 0, volStr.length(), volRect);
        canvas.drawText(volStr,
                verticalXList.get(0),
                volumeTopStart - verticalSpace / 2 + volRect.height() + dp2px(2),
                volPaint);

        String ma5Str = mMa5 + ArithUtil.setPrecision(lastKData.getVolumeMa5(), 2);
        Rect volMa5Rect = new Rect();
        priceMa5Paint.getTextBounds(ma5Str, 0, ma5Str.length(), volMa5Rect);
        canvas.drawText(ma5Str,
                verticalXList.get(0) + volRect.width() + dp2px(10),
                volumeTopStart - verticalSpace / 2 + volRect.height() + dp2px(2),
                priceMa5Paint);

        String ma10Str = mMa10 + ArithUtil.setPrecision(lastKData.getVolumeMa10(), 2);
        canvas.drawText(ma10Str,
                verticalXList.get(0) + volMa5Rect.width() + volRect.width() + dp2px(10) * 2,
                volumeTopStart - verticalSpace / 2 + volRect.height() + dp2px(2),
                priceMa10Paint);

        if (isShowDeputy && deputyImgType == DEPUTY_IMG_MACD) {
            //MACD
            Rect titleRect = new Rect();
            volPaint.getTextBounds(mMacdTitle, 0, mMacdTitle.length(), titleRect);
            canvas.drawText(mMacdTitle,
                    verticalXList.get(0),
                    horizontalYList.get(4) + titleRect.height(),
                    volPaint);

            String macdStr = mMacd + ArithUtil.setPrecision(lastKData.getMacd(), 2);
            canvas.drawText(macdStr,
                    verticalXList.get(0) + titleRect.width() + dp2px(10),
                    horizontalYList.get(4) + titleRect.height(),
                    priceMa5Paint);
            float macdWidth = priceMa5Paint.measureText(macdStr);

            String difStr = mDif + ArithUtil.setPrecision(lastKData.getDif(), 2);
            canvas.drawText(difStr,
                    verticalXList.get(0) + titleRect.width() + dp2px(20) + macdWidth,
                    horizontalYList.get(4) + titleRect.height(),
                    priceMa10Paint);
            float difWidth = priceMa10Paint.measureText(difStr);

            canvas.drawText(mDea + ArithUtil.setPrecision(lastKData.getDea(), 2),
                    verticalXList.get(0) + titleRect.width() + dp2px(30) + macdWidth + difWidth,
                    horizontalYList.get(4) + titleRect.height(),
                    priceMa30Paint);

        } else if (isShowDeputy && deputyImgType == DEPUTY_IMG_KDJ) {
            //KDJ
            Rect titleRect = new Rect();
            volPaint.getTextBounds(mKdjTitle, 0, mKdjTitle.length(), titleRect);
            canvas.drawText(mKdjTitle,
                    verticalXList.get(0),
                    horizontalYList.get(4) + titleRect.height(),
                    volPaint);

            String kStr = mK + ArithUtil.setPrecision(lastKData.getK(), 2);
            canvas.drawText(kStr,
                    verticalXList.get(0) + titleRect.width() + dp2px(10),
                    horizontalYList.get(4) + titleRect.height(),
                    priceMa5Paint);
            float kWidth = priceMa5Paint.measureText(kStr);

            String dStr = mD + ArithUtil.setPrecision(lastKData.getD(), 2);
            canvas.drawText(dStr,
                    verticalXList.get(0) + titleRect.width() + dp2px(20) + kWidth,
                    horizontalYList.get(4) + titleRect.height(),
                    priceMa10Paint);
            float dWidth = priceMa10Paint.measureText(dStr);

            canvas.drawText(mJ + ArithUtil.setPrecision(lastKData.getJ(), 2),
                    verticalXList.get(0) + titleRect.width() + dp2px(30) + kWidth + dWidth,
                    horizontalYList.get(4) + titleRect.height(),
                    priceMa30Paint);
        }
    }

    //日期
    private void drawDate(Canvas canvas) {
        datePaint.setColor(Color.parseColor("#9BACBD"));
        for (int i = 0; i < 5; i++) {
            if (i == 0) {
                canvas.drawText(dateArr[i],
                        leftStart + dp2px(6),
                        bottomEnd - dp2px(7),
                        datePaint);

            } else if (i == dateArr.length - 1) {
                canvas.drawText(dateArr[i],
                        rightEnd - dp2px(41) - datePaint.measureText(dateArr[i]),
                        bottomEnd - dp2px(7),
                        datePaint);

            } else {
                canvas.drawText(dateArr[i],
                        leftStart + dp2px(6)
                                + (rightEnd - leftStart - dp2px(47)) / 4 * i - datePaint.measureText(dateArr[i]) / 2,
                        bottomEnd - dp2px(7),
                        datePaint);
            }
        }
    }

    //纵坐标
    private void drawOrdinate(Canvas canvas) {
        Rect rect = new Rect();
        //最高价
        datePaint.getTextBounds(maxPrice + "", 0, (maxPrice + "").length(), rect);
        canvas.drawText(ArithUtil.setPrecision(maxPrice, 2),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                horizontalYList.get(0) + rect.height(),
                datePaint);

        //最低价
        datePaint.getTextBounds(minPrice + "", 0, (minPrice + "").length(), rect);
        canvas.drawText(ArithUtil.setPrecision(minPrice, 2),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                volumeTopStart - verticalSpace / 2 - rect.height() + dp2px(1),
                datePaint);

        if (!isShowDeputy) {
            double avgPrice = (maxPrice - minPrice) / 4;
            for (int i = 0; i < 3; i++) {
                canvas.drawText(ArithUtil.setPrecision(maxPrice - avgPrice * (i + 1), 2),
                        verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                        horizontalYList.get(i + 1) + rect.height() / 2,
                        datePaint);
            }
        } else {
            double avgPrice = (maxPrice - minPrice) / 3;
            for (int i = 0; i < 2; i++) {
                canvas.drawText(ArithUtil.setPrecision(maxPrice - avgPrice * (i + 1), 2),
                        verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                        horizontalYList.get(i + 1) + rect.height() / 2,
                        datePaint);
            }
        }

        //最高量
        datePaint.getTextBounds(maxVolume + "", 0, (maxVolume + "").length(), rect);
        canvas.drawText(ArithUtil.setPrecision(maxVolume, 2),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                volumeTopStart - verticalSpace / 2 + rect.height() + dp2px(3),
                datePaint);

        //最高量/2
        canvas.drawText(ArithUtil.setPrecision(maxVolume / 2, 2),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                volumeTopStart + rect.height() / 2,
                datePaint);

        //数量 0
        canvas.drawText("0",
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                volumeImgBot,
                datePaint);

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


}
