package com.example.admin.klineview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
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
 * Created by xiesuichao on 2018/6/29.
 */

public class KLineView extends View implements View.OnTouchListener {

    private GestureDetector mGestureDetector;
    private Paint mTickMarkPaint;
    private Paint mDatePaint;
    private Paint mPricePaint;
    private Paint mRedPaint;
    private Paint mGreenPaint;
    private Paint mClickGrayRectPaint;
    private Paint mDetailBgRectPaint;
    private Paint mDetailLinePaint;
    private Paint mDetailTextPaint;
    private Paint mPriceMa5Paint;
    private Paint mPriceMa10Paint;
    private Paint mPriceMa30Paint;
    private Paint mVolumeMa5Paint;
    private Paint mVolumeMa10Paint;
    private Paint mVolPaint;
    //十字线
    private Paint crossHairPaint;
    private Paint crossHairBluePaint;
    private Path blueTrianglePath;
    private Path mMaxPriceTrianglePath;
    private Path mMinPriceTrianglePath;
    private Paint crossHairBlueTextPaint;
    private Paint grayTimePaint;
    //边框
    private Paint framePaint;
    private Rect mTopMa5Rect = new Rect();
    private Rect mTopMa10Rect = new Rect();
    private Rect mTopMa30Rect = new Rect();
    private Rect mDetailTextRect = new Rect();
    //贝塞尔曲线
    private Path mPriceMa5BezierPath;
    private Path mPriceMa10BezierPath;
    private Path mPriceMa30BezierPath;
    private Path mEma5BezierPath;
    private Path mEma10BezierPath;
    private Path mEma30BezierPath;
    private Path mVolumeMa5BezierPath;
    private Path mVolumeMa10BezierPath;
    private Path mBollMbBezierPath;
    private Path mBollUpBezierPath;
    private Path mBollDnBezierPath;
    private Path mDeaBezierPath;
    private Path mDifBezierPath;
    private Path mKLinePath;
    private Path mDLinePath;
    private Path mJLinePath;

    private float mLeftStart;
    private float mTopStart;
    private float mRightEnd;
    private float mBottomEnd;
    private int mMaxViewDataNum = 34;
    private int mStartDataNum = 0;
    private int mMaxTotalSize = 3000;
    private int initTotalListSize = 0;
    private float mMulFirstDownX;
    private float mMulSecondDownX;
    private float mMulFirstDownY;
    private float mMulSecondDownY;
    private float mLastDiffMoveX;
    private float mLastDiffMoveY;
    private float mSingleClickDownX;
    private float mDispatchDownX;
    private float mDispatchDownY;
    private float mDetailTextVerticalSpace;
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
    private List<KData> mTotalDataList = new ArrayList<>();
    private List<KData> mViewDataList = new ArrayList<>();
    private List<String> detailRightDataList = new ArrayList<>();
    //水平线纵坐标
    private List<Float> mHorizontalYList = new ArrayList<>();
    //垂直线横坐标
    private List<Float> mVerticalXList = new ArrayList<>();
    private List<Pointer> mPriceMa5PointList = new ArrayList<>();
    private List<Pointer> mPriceMa10PointList = new ArrayList<>();
    private List<Pointer> mPriceMa30PointList = new ArrayList<>();
    private List<Pointer> mVolumeMa5PointList = new ArrayList<>();
    private List<Pointer> mVolumeMa10PointList = new ArrayList<>();
    private List<Pointer> mEma5PointList = new ArrayList<>();
    private List<Pointer> mEma10PointList = new ArrayList<>();
    private List<Pointer> mEma30PointList = new ArrayList<>();
    private List<Pointer> mBollMbPointList = new ArrayList<>();
    private List<Pointer> mBollUpPointList = new ArrayList<>();
    private List<Pointer> mBollDnPointList = new ArrayList<>();
    private List<Pointer> mDeaPointList = new ArrayList<>();
    private List<Pointer> mDifPointList = new ArrayList<>();
    private List<Pointer> mKPointList = new ArrayList<>();
    private List<Pointer> mDPointList = new ArrayList<>();
    private List<Pointer> mJPointList = new ArrayList<>();

    //是否显示副图
    private boolean isShowDeputy = false;
    //是否显示详情
    private boolean isShowDetail = false;
    //dispatchTouchEvent
    private boolean isLongPress = false;
    //是否需要请求前面的数据
    private boolean isNeedRequestBeforeData = true;
    //是否需要请求后面的数据
    private boolean isNeedRequestAfterData = false;
    //双指触控
    private boolean isDoubleFinger = false;
    //主图数据类型 0:MA, 1:EMA 2:BOLL
    private int mMainImgType = 0;
    public static final int MAIN_IMG_MA = 0;
    public static final int MAIN_IMG_EMA = 1;
    public static final int MAIN_IMG_BOLL = 2;
    //副图数据类型 0:MACD, 1:KDJ
    private int mDeputyImgType = 0;
    public static final int DEPUTY_IMG_MACD = 0;
    public static final int DEPUTY_IMG_KDJ = 1;

    private LongPressRunnable longPressRunnable = new LongPressRunnable();
    private double maxPrice = 0;
    private double maxPriceX = 0;
    private double minPrice = 0;
    private double minPriceX = 0;
    private double maxVolume;
    private float mPriceImgBot = 0;
    private float mPriceImgTop = 0;

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
    private KData mLastKData;
    private float mVolumeImgBot;
    private float mVerticalSpace;
    private float mVelocityX;
    private OnRequestDataListListener mRequestListener;

    public KLineView(Context context) {
        super(context);
        init();
    }

    public KLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public interface OnRequestDataListListener {
        void requestData(boolean isRequestBefore);
    }

    public void setOnRequestDataListListener(OnRequestDataListListener requestListener) {
        this.mRequestListener = requestListener;
    }

    public void addDataList(List<KData> dataList) {
        if (dataList.size() > 2000){
            return;
        }
        if (mStartDataNum <= mTotalDataList.size() / 4) {
            mTotalDataList.addAll(0, dataList);
            isNeedRequestBeforeData = dataList.size() >= initTotalListSize;
            isNeedRequestAfterData = true;
            if (initTotalListSize == 0){
                initTotalListSize = dataList.size();
            }
            if (mTotalDataList.size() > mMaxTotalSize) {
                int diffSize = mTotalDataList.size() - mMaxTotalSize;
                mTotalDataList.removeAll(mTotalDataList.subList(mTotalDataList.size() - diffSize, mTotalDataList.size()));
                mStartDataNum += diffSize;
            }else {
                mStartDataNum += dataList.size();
            }
        } else if (mStartDataNum >= mTotalDataList.size() * 3 / 4) {
            mTotalDataList.addAll(dataList);
            isNeedRequestAfterData = dataList.size() >= initTotalListSize;
            isNeedRequestBeforeData = true;
            if (mTotalDataList.size() > mMaxTotalSize) {
                int diffSize = mTotalDataList.size() - mMaxTotalSize;
                mTotalDataList.removeAll(mTotalDataList.subList(0, diffSize));
                mStartDataNum -= diffSize;
            }else {
                mStartDataNum -= dataList.size();
            }
        }
        initKDataQuota();
        PrintUtil.log("mStartDataNum", mStartDataNum);
        PrintUtil.log("mTotalDataList.size", mTotalDataList.size());

        /*if (mStartDataNum == mTotalDataList.size() - 1 - mMaxViewDataNum) {
            resetAllData();
            invalidate();
        }*/
    }

    public void initKDataList(List<KData> dataList) {
        if (dataList.size() > 2000){
            return;
        }
        this.mTotalDataList.clear();
        this.mTotalDataList.addAll(dataList);
        mStartDataNum = mTotalDataList.size() - 1 - mMaxViewDataNum;
        if (mMaxTotalSize >= mTotalDataList.size() * 2) {
            mMaxTotalSize = mTotalDataList.size() * 2;
        } else {
            mMaxTotalSize = (int) (mTotalDataList.size() * 1.5);
        }
        PrintUtil.log("mMaxTotalSize", mMaxTotalSize);
        initKDataQuota();
        resetAllData();
    }

    private void initKDataQuota() {
        KDataUtil.initMa(mTotalDataList);
        KDataUtil.initEma(mTotalDataList);
        KDataUtil.initBoll(mTotalDataList);
        KDataUtil.initMACD(mTotalDataList);
        KDataUtil.initKDJ(mTotalDataList);
    }

    public void setDeputyPicShow(boolean showState) {
        this.isShowDeputy = showState;
        invalidate();
    }

    public void setMainImgType(int type) {
        this.mMainImgType = type;
        invalidate();
    }

    public void setDeputyImgType(int type) {
        this.mDeputyImgType = type;
        invalidate();
    }

    public boolean getVicePicShow() {
        return this.isShowDeputy;
    }

    private void init() {
        PrintUtil.log("init");
        super.setOnTouchListener(this);
        super.setClickable(true);
        super.setLongClickable(true);
        super.setFocusable(true);
        mGestureDetector = new GestureDetector(getContext(), new CustomGestureListener());

        mPriceMa5Paint = new Paint();
        mPriceMa5Paint.setAntiAlias(true);
        mPriceMa5Paint.setTextSize(sp2px(10));
        mPriceMa5Paint.setStyle(Paint.Style.STROKE);
        mPriceMa5Paint.setColor(Color.parseColor("#FFA800"));

        mPriceMa10Paint = new Paint();
        mPriceMa10Paint.setAntiAlias(true);
        mPriceMa10Paint.setTextSize(sp2px(10));
        mPriceMa10Paint.setStyle(Paint.Style.STROKE);
        mPriceMa10Paint.setColor(Color.parseColor("#2668FF"));

        mPriceMa30Paint = new Paint();
        mPriceMa30Paint.setAntiAlias(true);
        mPriceMa30Paint.setTextSize(sp2px(10));
        mPriceMa30Paint.setStyle(Paint.Style.STROKE);
        mPriceMa30Paint.setColor(Color.parseColor("#FF45A1"));

        mVolumeMa5Paint = new Paint();
        mVolumeMa5Paint.setAntiAlias(true);
        mVolumeMa5Paint.setTextSize(sp2px(10));
        mVolumeMa5Paint.setStyle(Paint.Style.STROKE);
        mVolumeMa5Paint.setColor(Color.parseColor("#FF45A1"));

        mVolumeMa10Paint = new Paint();
        mVolumeMa10Paint.setAntiAlias(true);
        mVolumeMa10Paint.setTextSize(sp2px(10));
        mVolumeMa10Paint.setStyle(Paint.Style.STROKE);
        mVolumeMa10Paint.setColor(Color.parseColor("#FF45A1"));

        mVolPaint = new Paint();
        mVolPaint.setAntiAlias(true);
        mVolPaint.setTextSize(sp2px(10));
        mVolPaint.setStyle(Paint.Style.STROKE);
        mVolPaint.setColor(Color.parseColor("#9BACBD"));

        crossHairPaint = new Paint();
        crossHairPaint.setAntiAlias(true);
        crossHairPaint.setStyle(Paint.Style.STROKE);
        crossHairPaint.setColor(Color.parseColor("#828EA2"));

        crossHairBluePaint = new Paint();
        crossHairBluePaint.setAntiAlias(true);
        crossHairBluePaint.setStyle(Paint.Style.FILL);
        crossHairBluePaint.setColor(Color.parseColor("#3193FF"));

        blueTrianglePath = new Path();
        mMaxPriceTrianglePath = new Path();
        mMinPriceTrianglePath = new Path();

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

        mDatePaint = new Paint();
        mDatePaint.setAntiAlias(true);
        mDatePaint.setColor(Color.parseColor("#9BACBD"));
        mDatePaint.setTextSize(sp2px(8));

        mTickMarkPaint = new Paint();
        mTickMarkPaint.setAntiAlias(true);
        mTickMarkPaint.setColor(Color.parseColor("#F7F7FB"));

        mPricePaint = new Paint();
        mPricePaint.setAntiAlias(true);
        mPricePaint.setColor(Color.parseColor("#333333"));
        mPricePaint.setTextSize(sp2px(10));

        mRedPaint = new Paint();
        mRedPaint.setAntiAlias(true);
        mRedPaint.setColor(Color.parseColor("#FF424A"));
        mRedPaint.setStrokeWidth(1);
        mRedPaint.setTextSize(sp2px(10));

        mGreenPaint = new Paint();
        mGreenPaint.setAntiAlias(true);
        mGreenPaint.setColor(Color.parseColor("#00B23E"));
        mGreenPaint.setStrokeWidth(1);
        mGreenPaint.setTextSize(sp2px(10));

        mClickGrayRectPaint = new Paint();
        mClickGrayRectPaint.setAntiAlias(true);
        mClickGrayRectPaint.setColor(Color.parseColor("#8065707c"));

        mDetailBgRectPaint = new Paint();
        mDetailBgRectPaint.setAntiAlias(true);
        mDetailBgRectPaint.setColor(Color.parseColor("#cc294058"));

        mDetailLinePaint = new Paint();
        mDetailLinePaint.setAntiAlias(true);
        mDetailLinePaint.setColor(Color.parseColor("#e6ffffff"));

        mDetailTextPaint = new Paint();
        mDetailTextPaint.setAntiAlias(true);
        mDetailTextPaint.setColor(Color.parseColor("#808F9E"));
        mDetailTextPaint.setTextSize(sp2px(10));

        mPriceMa5BezierPath = new Path();
        mPriceMa10BezierPath = new Path();
        mPriceMa30BezierPath = new Path();
        mEma5BezierPath = new Path();
        mEma10BezierPath = new Path();
        mEma30BezierPath = new Path();
        mVolumeMa5BezierPath = new Path();
        mVolumeMa10BezierPath = new Path();
        mBollMbBezierPath = new Path();
        mBollUpBezierPath = new Path();
        mBollDnBezierPath = new Path();
        mDeaBezierPath = new Path();
        mDifBezierPath = new Path();
        mKLinePath = new Path();
        mDLinePath = new Path();
        mJLinePath = new Path();

        detailRectWidth = dp2px(103);
        detailRectHeight = dp2px(120);
        mDetailTextVerticalSpace = (detailRectHeight - dp2px(4)) / 8;

        dateArr = new String[]{"06-29 10:00", "06-29 10:01", "06-29 10:02", "06-29 10:03", "06-29 10:04"};

        detailLeftTitleArr = new String[]{"时间", "开", "高", "低", "收", "涨跌额", "涨跌幅", "成交量"};

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mLeftStart = getPaddingLeft() + 1;
        mTopStart = getPaddingTop() + 1;
        mRightEnd = getMeasuredWidth() - getPaddingRight() - 1;
        mBottomEnd = getMeasuredHeight() - getPaddingBottom() - 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mTotalDataList.isEmpty() || mViewDataList.isEmpty()) {
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
            mSingleClickDownX = longPressX;
            invalidate();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDispatchDownX = event.getX();
                mDispatchDownY = event.getY();
                longPressRunnable.setPressLocation(event.getX());
                postDelayed(longPressRunnable, 500);
                isLongPress = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - mDispatchDownX) > 5
                        || Math.abs(event.getY() - mDispatchDownY) > 5) {
                    removeCallbacks(longPressRunnable);
                    if (isLongPress) {
                        mSingleClickDownX = event.getX();
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
                mSingleClickDownX = event.getX();
                break;

            case MotionEvent.ACTION_UP:
                float upX = event.getX();
                if (Math.abs(upX - mSingleClickDownX) < 5) {
                    isShowDetail = true;
                    invalidate();
                }
                break;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mMulFirstDownX = event.getX(0);
                mMulFirstDownY = event.getY(0);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                isDoubleFinger = true;
                removeCallbacks(longPressRunnable);
                mMulSecondDownX = event.getX(1);
                mMulSecondDownY = event.getY(1);
                mLastDiffMoveX = Math.abs(mMulSecondDownX - mMulFirstDownX);
                mLastDiffMoveY = Math.abs(mMulSecondDownY - mMulFirstDownY);
                break;

            case MotionEvent.ACTION_MOVE:
                isShowDetail = false;
                if (event.getPointerCount() > 1) {
                    float mulFirstMoveX = event.getX(0);
                    float mulFirstMoveY = event.getY(0);
                    float mulSecondMoveX = event.getX(1);
                    float mulSecondMoveY = event.getY(1);

                    if (Math.abs(mulSecondMoveX - mulFirstMoveX) - mLastDiffMoveX > 1.5
                            || Math.abs(mulSecondMoveY - mulFirstMoveY) - mLastDiffMoveY > 1.5) {
                        mMaxViewDataNum -= 3;
                        if (mMaxViewDataNum < 18) {
                            mMaxViewDataNum = 18;
                        }

                    } else if (Math.abs(mulSecondMoveX - mulFirstMoveX) - mLastDiffMoveX < -1.5
                            || Math.abs(mulSecondMoveY - mulFirstMoveY) - mLastDiffMoveY < -1.5) {
                        mMaxViewDataNum += 3;
                        if (mTotalDataList.size() - mStartDataNum >= 140 && mMaxViewDataNum > 140) {
                            mMaxViewDataNum = 140;
                        } else if (mTotalDataList.size() - mStartDataNum < 140 && mMaxViewDataNum > 140) {
                            mMaxViewDataNum = mTotalDataList.size() - mStartDataNum;
                        }
                    }
                    mLastDiffMoveX = Math.abs(mulSecondMoveX - mulFirstMoveX);
                    mLastDiffMoveY = Math.abs(mulSecondMoveY - mulFirstMoveY);

                    resetAllData();
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                isDoubleFinger = false;
                PrintUtil.log("mMaxViewDataNum", mMaxViewDataNum);
                break;

        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return !isDoubleFinger && mGestureDetector.onTouchEvent(event);
    }

    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            moveData(distanceX);
            PrintUtil.log("onScroll startDataNum", mStartDataNum);
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityX > 8000) {
                mVelocityX = 8000;
            } else if (velocityX < -8000) {
                mVelocityX = -8000;
            } else {
                mVelocityX = velocityX;
            }
            stopDelay();
            return true;
        }
    }

    private void stopDelay() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mVelocityX < -200) {
                    if (mVelocityX < -6000) {
                        mStartDataNum += 6;
                    } else if (mVelocityX < -4000) {
                        mStartDataNum += 5;
                    } else if (mVelocityX < -2500) {
                        mStartDataNum += 4;
                    } else if (mVelocityX < -1000) {
                        mStartDataNum += 3;
                    } else {
                        mStartDataNum++;
                    }
                    mVelocityX += 200;
                    if (mStartDataNum > mTotalDataList.size() - mMaxViewDataNum - 1) {
                        mStartDataNum = mTotalDataList.size() - mMaxViewDataNum - 1;
                    }
                } else if (mVelocityX > 200) {
                    if (mVelocityX > 6000) {
                        mStartDataNum -= 6;
                    } else if (mVelocityX > 4000) {
                        mStartDataNum -= 5;
                    } else if (mVelocityX > 2500) {
                        mStartDataNum -= 4;
                    } else if (mVelocityX > 1000) {
                        mStartDataNum -= 3;
                    } else {
                        mStartDataNum--;
                    }
                    mVelocityX -= 200;
                    if (mStartDataNum < 0) {
                        mStartDataNum = 0;
                    }
                }
                PrintUtil.log("onScroll startDataNum", mStartDataNum);
                resetAllData();
                invalidate();
                requestNewData();
                if (Math.abs(mVelocityX) > 200) {
                    handler.postDelayed(this, 30);
                }

            }
        };
        handler.postDelayed(runnable, 0);
    }

    private void moveData(float distanceX) {
        if (mMaxViewDataNum < 20) {
            if (Math.abs(distanceX) < 2) {
                mStartDataNum += (int) distanceX;
            } else if (Math.abs(distanceX) < 10) {
                mStartDataNum += (int) distanceX / 5;
            } else {
                mStartDataNum += (int) distanceX / 10;
            }
        } else if (mMaxViewDataNum < 40) {
            mStartDataNum += (int) distanceX / 4;
        } else if (mMaxViewDataNum < 60) {
            mStartDataNum += (int) distanceX / 4;
        } else if (mMaxViewDataNum < 80) {
            mStartDataNum += (int) distanceX / 3;
        } else if (mMaxViewDataNum < 100) {
            mStartDataNum += (int) distanceX / 2;
        } else {
            mStartDataNum += (int) distanceX / 2;
        }

        if (mStartDataNum < 0) {
            mStartDataNum = 0;
        }
        if (mStartDataNum > mTotalDataList.size() - mMaxViewDataNum - 1) {
            mStartDataNum = mTotalDataList.size() - mMaxViewDataNum - 1;
        }

        requestNewData();
        resetAllData();

    }

    private void requestNewData() {
        if (mStartDataNum <= mTotalDataList.size() / 4 && isNeedRequestBeforeData) {
            mRequestListener.requestData(true);
            PrintUtil.log("requestBeforeData");
            isNeedRequestBeforeData = false;
        } else if (mStartDataNum >= mTotalDataList.size() * 3 / 4 && isNeedRequestAfterData) {
            mRequestListener.requestData(false);
            PrintUtil.log("requestAfterData");
            isNeedRequestAfterData = false;
        }
    }

    private void resetAllData() {
        mViewDataList.clear();
        for (int i = mStartDataNum; i < mStartDataNum + mMaxViewDataNum; i++) {
            if (i < mTotalDataList.size()) {
                mViewDataList.add(mTotalDataList.get(i));
            }
        }
        mLastKData = mViewDataList.get(mViewDataList.size() - 1);
    }

    //刻度线
    private void drawTickMark(Canvas canvas) {
        //垂直刻度线
        float horizontalSpace = (mRightEnd - mLeftStart - (dp2px(46))) / 4;
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(mLeftStart + horizontalSpace * (i) + dp2px(6),
                    mTopStart + dp2px(18),
                    mLeftStart + horizontalSpace * (i) + dp2px(6),
                    mBottomEnd - dp2px(20),
                    mTickMarkPaint);
            mVerticalXList.add(mLeftStart + horizontalSpace * (i) + dp2px(6));
        }

        //水平刻度线
        mVerticalSpace = (mBottomEnd - mTopStart - dp2px(38)) / 5;
        for (int i = 0; i < 6; i++) {
            canvas.drawLine(mLeftStart + dp2px(6),
                    mTopStart + mVerticalSpace * i + dp2px(18),
                    mRightEnd,
                    mTopStart + mVerticalSpace * i + dp2px(18),
                    mTickMarkPaint);
            mHorizontalYList.add(mTopStart + mVerticalSpace * i + dp2px(18));
        }

        //副图中线
        deputyTopY = mHorizontalYList.get(4) + dp2px(17);
        deputyCenterY = deputyTopY + (mVerticalSpace - dp2px(15)) / 2;
        canvas.drawLine(mLeftStart + dp2px(6),
                deputyCenterY,
                mRightEnd,
                deputyCenterY,
                mTickMarkPaint);

        //数量中线
        if (isShowDeputy) {
            canvas.drawLine(mLeftStart + dp2px(6),
                    mHorizontalYList.get(3) + mVerticalSpace / 2,
                    mRightEnd,
                    mHorizontalYList.get(3) + mVerticalSpace / 2,
                    mTickMarkPaint);
        }
    }

    //主副图蜡烛图
    private void drawMainDeputyRect(Canvas canvas) {
        avgRectWidth = (mVerticalXList.get(mVerticalXList.size() - 1)
                - mVerticalXList.get(0)) / mMaxViewDataNum;
        maxPrice = mViewDataList.get(0).getMaxPrice();
        minPrice = mViewDataList.get(0).getMinPrice();
        maxVolume = mViewDataList.get(0).getVolume();
        double maxMacd = mViewDataList.get(0).getMacd();
        double minMacd = mViewDataList.get(0).getMacd();
        double maxDea = mViewDataList.get(0).getDea();
        double minDea = mViewDataList.get(0).getDea();
        double maxDif = mViewDataList.get(0).getDif();
        double minDif = mViewDataList.get(0).getDif();
        double maxK = mViewDataList.get(0).getK();
        double maxD = mViewDataList.get(0).getD();
        double maxJ = mViewDataList.get(0).getJ();

        for (int i = 0; i < mViewDataList.size(); i++) {
            mViewDataList.get(i).setLeftX(mVerticalXList.get(0) + avgRectWidth * i);
            mViewDataList.get(i).setRightX(mVerticalXList.get(0) + avgRectWidth * (i + 1));
            if (mViewDataList.get(i).getMaxPrice() >= maxPrice) {
                maxPrice = mViewDataList.get(i).getMaxPrice();
                maxPriceX = mViewDataList.get(i).getLeftX() + avgRectWidth / 2;
            }
            if (mViewDataList.get(i).getMinPrice() <= minPrice) {
                minPrice = mViewDataList.get(i).getMinPrice();
                minPriceX = mViewDataList.get(i).getLeftX() + avgRectWidth / 2;
            }
            if (mViewDataList.get(i).getVolume() >= maxVolume) {
                maxVolume = mViewDataList.get(i).getVolume();
            }
            if (mViewDataList.get(i).getMacd() >= maxMacd) {
                maxMacd = mViewDataList.get(i).getMacd();
            }
            if (mViewDataList.get(i).getMacd() <= minMacd) {
                minMacd = mViewDataList.get(i).getMacd();
            }
            if (mViewDataList.get(i).getDea() >= maxDea) {
                maxDea = mViewDataList.get(i).getDea();
            }
            if (mViewDataList.get(i).getDea() <= minDea) {
                minDea = mViewDataList.get(i).getDea();
            }
            if (mViewDataList.get(i).getDif() >= maxDif) {
                maxDif = mViewDataList.get(i).getDif();
            }
            if (mViewDataList.get(i).getDif() <= minDif) {
                minDif = mViewDataList.get(i).getDif();
            }
            if (mViewDataList.get(i).getK() >= maxK) {
                maxK = mViewDataList.get(i).getK();
            }
            if (mViewDataList.get(i).getD() >= maxD) {
                maxD = mViewDataList.get(i).getD();
            }
            if (mViewDataList.get(i).getJ() >= maxJ) {
                maxJ = mViewDataList.get(i).getJ();
            }
        }

        if (!isShowDeputy) {
            mPriceImgBot = mHorizontalYList.get(4);
            mVolumeImgBot = mHorizontalYList.get(5);
        } else {
            mPriceImgBot = mHorizontalYList.get(3);
            mVolumeImgBot = mHorizontalYList.get(4);
        }

        //priceData
        avgHeightPerPrice = ((mPriceImgBot - mHorizontalYList.get(0)) / (maxPrice - minPrice));

        //volumeData
        volumeTopStart = mVolumeImgBot - mVerticalSpace / 2;
        avgHeightPerVolume = mVerticalSpace / 2 / maxVolume;

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
        avgHeightK = (mHorizontalYList.get(5) - deputyTopY - dp2px(10)) / maxK;
        //D
        avgHeightD = (mHorizontalYList.get(5) - deputyTopY - dp2px(10)) / maxD;
        //J
        avgHeightJ = (mHorizontalYList.get(5) - deputyTopY - dp2px(10)) / maxJ;

        for (int i = 0; i < mViewDataList.size(); i++) {
            Paint rectPaint;
            //drawPriceRectAndLine
            double openPrice = mViewDataList.get(i).getOpenPrice();
            double closedPrice = mViewDataList.get(i).getClosePrice();
            double higherPrice;
            double lowerPrice;
            if (openPrice >= closedPrice) {
                higherPrice = openPrice;
                lowerPrice = closedPrice;
                rectPaint = mGreenPaint;
            } else {
                higherPrice = closedPrice;
                lowerPrice = openPrice;
                rectPaint = mRedPaint;
            }

            mViewDataList.get(i).setCloseY((float) (mHorizontalYList.get(0) + (maxPrice - closedPrice) * avgHeightPerPrice));

            canvas.drawRect((float) (mVerticalXList.get(0) + avgRectWidth * i + dp2px(0.5f)),
                    (float) (mHorizontalYList.get(0) + (maxPrice - higherPrice) * avgHeightPerPrice),
                    (float) (mVerticalXList.get(0) + avgRectWidth * (i + 1) - dp2px(0.5f)),
                    (float) (mHorizontalYList.get(0) + (maxPrice - lowerPrice) * avgHeightPerPrice),
                    rectPaint);

            canvas.drawLine((float) (mVerticalXList.get(0) + avgRectWidth * (i * 2 + 1) / 2),
                    (float) (mHorizontalYList.get(0)
                            + (maxPrice - mViewDataList.get(i).getMaxPrice()) * avgHeightPerPrice),
                    (float) (mVerticalXList.get(0) + avgRectWidth * (i * 2 + 1) / 2),
                    (float) (mHorizontalYList.get(0)
                            + (maxPrice - mViewDataList.get(i).getMinPrice()) * avgHeightPerPrice),
                    rectPaint);

            //drawVolumeRect
            canvas.drawRect((float) (mVerticalXList.get(0) + avgRectWidth * i + dp2px(0.5f)),
                    (float) (mVolumeImgBot - mViewDataList.get(i).getVolume() * avgHeightPerVolume),
                    (float) (mVerticalXList.get(0) + avgRectWidth * (i + 1) - dp2px(0.5f)),
                    mVolumeImgBot,
                    rectPaint);

            //MACD
            if (isShowDeputy && mDeputyImgType == DEPUTY_IMG_MACD) {
                double macd = mViewDataList.get(i).getMacd();
                if (macd > 0) {
                    rectPaint = mRedPaint;
                    canvas.drawRect((float) (mVerticalXList.get(0) + avgRectWidth * i + dp2px(0.5f)),
                            (float) (deputyCenterY - macd * avgHeightUpMacd),
                            (float) (mVerticalXList.get(0) + avgRectWidth * (i + 1) - dp2px(0.5f)),
                            deputyCenterY,
                            rectPaint);

                } else {
                    rectPaint = mGreenPaint;
                    canvas.drawRect((float) (mVerticalXList.get(0) + avgRectWidth * i + dp2px(0.5f)),
                            deputyCenterY,
                            (float) (mVerticalXList.get(0) + avgRectWidth * (i + 1) - dp2px(0.5f)),
                            (float) (deputyCenterY + Math.abs(macd * avgHeightDnMacd)),
                            rectPaint);
                }
            }
        }
    }

    //贝塞尔曲线
    private void drawBezierCurve(Canvas canvas) {
        mPriceMa5PointList.clear();
        mPriceMa10PointList.clear();
        mPriceMa30PointList.clear();
        mEma5PointList.clear();
        mEma10PointList.clear();
        mEma30PointList.clear();
        mVolumeMa5PointList.clear();
        mVolumeMa10PointList.clear();
        mBollMbPointList.clear();
        mBollUpPointList.clear();
        mBollDnPointList.clear();
        mDeaPointList.clear();
        mDifPointList.clear();
        mKPointList.clear();
        mDPointList.clear();
        mJPointList.clear();

        for (int i = 0; i < mViewDataList.size(); i++) {
            Pointer volumeMa5Point = new Pointer();
            if (mViewDataList.get(i).getVolumeMa5() > 0) {
                volumeMa5Point.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                volumeMa5Point.setY((float) (mVolumeImgBot
                        - mViewDataList.get(i).getVolumeMa5() * avgHeightPerVolume));
                mVolumeMa5PointList.add(volumeMa5Point);
            }

            Pointer volumeMa10Point = new Pointer();
            if (mViewDataList.get(i).getVolumeMa10() > 0) {
                volumeMa10Point.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                volumeMa10Point.setY((float) (mVolumeImgBot
                        - mViewDataList.get(i).getVolumeMa10() * avgHeightPerVolume));
                mVolumeMa10PointList.add(volumeMa10Point);
            }

            switch (mMainImgType) {
                case MAIN_IMG_MA:
                    Pointer priceMa5Point = new Pointer();
                    if (mViewDataList.get(i).getPriceMa5() > 0) {
                        priceMa5Point.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        priceMa5Point.setY((float) (mHorizontalYList.get(0)
                                + (maxPrice - mViewDataList.get(i).getPriceMa5()) * avgHeightPerPrice));
                        mPriceMa5PointList.add(priceMa5Point);
                    }

                    Pointer priceMa10Point = new Pointer();
                    if (mViewDataList.get(i).getPriceMa10() > 0) {
                        priceMa10Point.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        priceMa10Point.setY((float) (mHorizontalYList.get(0)
                                + (maxPrice - mViewDataList.get(i).getPriceMa10()) * avgHeightPerPrice));
                        mPriceMa10PointList.add(priceMa10Point);
                    }

                    Pointer priceMa30Point = new Pointer();
                    if (mViewDataList.get(i).getPriceMa30() > 0) {
                        priceMa30Point.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        priceMa30Point.setY((float) (mHorizontalYList.get(0)
                                + (maxPrice - mViewDataList.get(i).getPriceMa30()) * avgHeightPerPrice));
                        mPriceMa30PointList.add(priceMa30Point);
                    }
                    break;

                case MAIN_IMG_EMA:
                    Pointer ema5Point = new Pointer();
                    if (mViewDataList.get(i).getEma5() > 0) {
                        ema5Point.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        ema5Point.setY((float) (mHorizontalYList.get(0)
                                + (maxPrice - mViewDataList.get(i).getEma5()) * avgHeightPerPrice));
                        mEma5PointList.add(ema5Point);
                    }

                    Pointer ema10Point = new Pointer();
                    if (mViewDataList.get(i).getEma10() > 0) {
                        ema10Point.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        ema10Point.setY((float) (mHorizontalYList.get(0)
                                + (maxPrice - mViewDataList.get(i).getEma10()) * avgHeightPerPrice));
                        mEma10PointList.add(ema10Point);
                    }

                    Pointer ema30Point = new Pointer();
                    if (mViewDataList.get(i).getEma30() > 0) {
                        ema30Point.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        ema30Point.setY((float) (mHorizontalYList.get(0)
                                + (maxPrice - mViewDataList.get(i).getEma30()) * avgHeightPerPrice));
                        mEma30PointList.add(ema30Point);
                    }
                    break;

                case MAIN_IMG_BOLL:
                    Pointer bollMbPoint = new Pointer();
                    if (mViewDataList.get(i).getBollMb() > 0) {
                        bollMbPoint.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        bollMbPoint.setY((float) (mHorizontalYList.get(0)
                                + (maxPrice - mViewDataList.get(i).getBollMb()) * avgHeightPerPrice));
                        mBollMbPointList.add(bollMbPoint);
                    }

                    Pointer bollUpPoint = new Pointer();
                    if (mViewDataList.get(i).getBollUp() > 0) {
                        bollUpPoint.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        bollUpPoint.setY((float) (mHorizontalYList.get(0)
                                + (maxPrice - mViewDataList.get(i).getBollUp()) * avgHeightPerPrice));
                        mBollUpPointList.add(bollUpPoint);
                    }

                    Pointer bollDnPoint = new Pointer();
                    if (mViewDataList.get(i).getBollDn() > 0) {
                        bollDnPoint.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                        bollDnPoint.setY((float) (mHorizontalYList.get(0)
                                + (maxPrice - mViewDataList.get(i).getBollDn()) * avgHeightPerPrice));
                        mBollDnPointList.add(bollDnPoint);
                    }
                    break;
            }

            if (isShowDeputy && mDeputyImgType == DEPUTY_IMG_MACD) {
                Pointer deaPoint = new Pointer();
                if (mViewDataList.get(i).getDea() > 0) {
                    deaPoint.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    deaPoint.setY((float) (deputyCenterY - mViewDataList.get(i).getDea() * avgHeightUpDea));
                } else {
                    deaPoint.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    deaPoint.setY((float) (deputyCenterY + Math.abs(mViewDataList.get(i).getDea() * avgHeightDnDea)));
                }
                mDeaPointList.add(deaPoint);

                Pointer difPoint = new Pointer();
                if (mViewDataList.get(i).getDif() > 0) {
                    difPoint.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    difPoint.setY((float) (deputyCenterY - mViewDataList.get(i).getDif() * avgHeightUpDif));
                } else {
                    difPoint.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    difPoint.setY((float) (deputyCenterY + Math.abs(mViewDataList.get(i).getDif() * avgHeightDnDif)));
                }
                mDifPointList.add(difPoint);

            } else if (isShowDeputy && mDeputyImgType == DEPUTY_IMG_KDJ) {
                Pointer kPoint = new Pointer();
                if (mViewDataList.get(i).getK() > 0) {
                    kPoint.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    kPoint.setY((float) (mHorizontalYList.get(5) - mViewDataList.get(i).getK() * avgHeightK));
                    mKPointList.add(kPoint);
                }

                Pointer dPoint = new Pointer();
                if (mViewDataList.get(i).getD() > 0) {
                    dPoint.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    dPoint.setY((float) (mHorizontalYList.get(5) - mViewDataList.get(i).getD() * avgHeightD));
                    mDPointList.add(dPoint);
                }

                Pointer jPoint = new Pointer();
                if (mViewDataList.get(i).getJ() > 0) {
                    jPoint.setX((float) (mViewDataList.get(i).getLeftX() + avgRectWidth / 2));
                    jPoint.setY((float) (mHorizontalYList.get(5) - mViewDataList.get(i).getJ() * avgHeightJ));
                    mJPointList.add(jPoint);
                }
            }

        }

        drawVolumeMaBezierCurve(canvas);

        switch (mMainImgType) {
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

        if (isShowDeputy && mDeputyImgType == DEPUTY_IMG_MACD) {
            drawDeaDifBezier(canvas);
        } else if (isShowDeputy && mDeputyImgType == DEPUTY_IMG_KDJ) {
            drawKdjLine(canvas);
        }

    }

    //price MA曲线
    private void drawPriceMaBezierCurve(Canvas canvas) {
        KDataUtil.setBezierPath(mPriceMa5PointList, mPriceMa5BezierPath);
        canvas.drawPath(mPriceMa5BezierPath, mPriceMa5Paint);

        KDataUtil.setBezierPath(mPriceMa10PointList, mPriceMa10BezierPath);
        canvas.drawPath(mPriceMa10BezierPath, mPriceMa10Paint);

        KDataUtil.setBezierPath(mPriceMa30PointList, mPriceMa30BezierPath);
        canvas.drawPath(mPriceMa30BezierPath, mPriceMa30Paint);
    }

    //volume MA曲线
    private void drawVolumeMaBezierCurve(Canvas canvas) {
        KDataUtil.setBezierPath(mVolumeMa5PointList, mVolumeMa5BezierPath);
        canvas.drawPath(mVolumeMa5BezierPath, mPriceMa5Paint);

        KDataUtil.setBezierPath(mVolumeMa10PointList, mVolumeMa10BezierPath);
        canvas.drawPath(mVolumeMa10BezierPath, mPriceMa10Paint);
    }

    //EMA曲线
    private void drawEmaBezierCurve(Canvas canvas) {
        KDataUtil.setBezierPath(mEma5PointList, mEma5BezierPath);
        canvas.drawPath(mEma5BezierPath, mPriceMa5Paint);

        KDataUtil.setBezierPath(mEma10PointList, mEma10BezierPath);
        canvas.drawPath(mEma10BezierPath, mPriceMa10Paint);

        KDataUtil.setBezierPath(mEma30PointList, mEma30BezierPath);
        canvas.drawPath(mEma30BezierPath, mPriceMa30Paint);
    }

    //BOLL曲线
    private void drawBollBezierCurve(Canvas canvas) {
        KDataUtil.setBezierPath(mBollMbPointList, mBollMbBezierPath);
        canvas.drawPath(mBollMbBezierPath, mPriceMa5Paint);

        KDataUtil.setBezierPath(mBollUpPointList, mBollUpBezierPath);
        canvas.drawPath(mBollUpBezierPath, mPriceMa10Paint);

        KDataUtil.setBezierPath(mBollDnPointList, mBollDnBezierPath);
        canvas.drawPath(mBollDnBezierPath, mPriceMa30Paint);
    }

    //DEA DIF曲线
    private void drawDeaDifBezier(Canvas canvas) {
        KDataUtil.setBezierPath(mDeaPointList, mDeaBezierPath);
        canvas.drawPath(mDeaBezierPath, mPriceMa10Paint);

        KDataUtil.setBezierPath(mDifPointList, mDifBezierPath);
        canvas.drawPath(mDifBezierPath, mPriceMa30Paint);
    }

    //KDJ
    private void drawKdjLine(Canvas canvas) {
        KDataUtil.setLinePath(mKPointList, mKLinePath);
        canvas.drawPath(mKLinePath, mPriceMa5Paint);

        KDataUtil.setLinePath(mDPointList, mDLinePath);
        canvas.drawPath(mDLinePath, mPriceMa10Paint);

        KDataUtil.setLinePath(mJPointList, mJLinePath);
        canvas.drawPath(mJLinePath, mPriceMa30Paint);
    }

    //获取单击位置的数据
    private void getClickKData() {
        if (isShowDetail) {
            detailRightDataList.clear();
            for (int i = 0; i < mViewDataList.size(); i++) {
                if (mViewDataList.get(i).getLeftX() <= mSingleClickDownX
                        && mViewDataList.get(i).getRightX() >= mSingleClickDownX) {
                    mLastKData = mViewDataList.get(i);
                    detailRightDataList.add(formatDate(mLastKData.getTime()));
                    detailRightDataList.add(ArithUtil.setPrecision(mLastKData.getOpenPrice(), 2));
                    detailRightDataList.add(ArithUtil.setPrecision(mLastKData.getMaxPrice(), 2));
                    detailRightDataList.add(ArithUtil.setPrecision(mLastKData.getMinPrice(), 2));
                    detailRightDataList.add(ArithUtil.setPrecision(mLastKData.getClosePrice(), 2));
                    double upDnAmount = mLastKData.getUpDnAmount();
                    if (upDnAmount > 0) {
                        detailRightDataList.add("+" + ArithUtil.setPrecision(upDnAmount, 2));
                        detailRightDataList.add("+" + ArithUtil.setPrecision(mLastKData.getUpDnRate() * 100, 2) + "%");
                    } else {
                        detailRightDataList.add(ArithUtil.setPrecision(upDnAmount, 2));
                        detailRightDataList.add(ArithUtil.setPrecision(mLastKData.getUpDnRate() * 100, 2) + "%");
                    }
                    detailRightDataList.add(ArithUtil.setPrecision(mLastKData.getVolume(), 2));
                    break;
                }
            }
        } else {
            mLastKData = mViewDataList.get(mViewDataList.size() - 1);
        }
    }

    //十字线
    private void drawCrossHairLine(Canvas canvas) {
        if (mLastKData == null || !isShowDetail) {
            return;
        }
        //垂直
        canvas.drawLine((float) (mLastKData.getLeftX() + avgRectWidth / 2),
                mTopStart,
                (float) (mLastKData.getLeftX() + avgRectWidth / 2),
                mBottomEnd,
                crossHairPaint);

        //水平
        canvas.drawLine(mVerticalXList.get(0),
                (float) mLastKData.getCloseY(),
                mVerticalXList.get(mVerticalXList.size() - 1),
                (float) mLastKData.getCloseY(),
                crossHairPaint);

        //灰色指示器
        RectF grayRectF = new RectF(mSingleClickDownX - dp2px(25),
                mBottomEnd - dp2px(20),
                mSingleClickDownX + dp2px(25),
                mBottomEnd);
        canvas.drawRoundRect(grayRectF, 4, 4, grayTimePaint);

        //时间
        String moveTime = formatDate(mLastKData.getTime());
        mDatePaint.setColor(Color.parseColor("#FFFFFF"));
        canvas.drawText(moveTime,
                mSingleClickDownX - mDatePaint.measureText(moveTime) / 2,
                mBottomEnd - dp2px(7),
                mDatePaint);

        //蓝色指示器
        RectF blueRectF = new RectF(mRightEnd - dp2px(38),
                (float) mLastKData.getCloseY() - dp2px(7),
                mRightEnd - dp2px(1),
                (float) mLastKData.getCloseY() + dp2px(7));
        canvas.drawRoundRect(blueRectF, 4, 4, crossHairBluePaint);

        blueTrianglePath.reset();
        blueTrianglePath.moveTo(mVerticalXList.get(mVerticalXList.size() - 1), (float) mLastKData.getCloseY());
        blueTrianglePath.lineTo(mRightEnd - dp2px(37), (float) mLastKData.getCloseY() - dp2px(3));
        blueTrianglePath.lineTo(mRightEnd - dp2px(37), (float) mLastKData.getCloseY() + dp2px(3));
        blueTrianglePath.close();
        canvas.drawPath(blueTrianglePath, crossHairBluePaint);

        if (!isShowDeputy) {
            //price
            double avgPricePerHeight = (maxPrice - minPrice)
                    / (mHorizontalYList.get(4) - mHorizontalYList.get(0));
            String movePrice = ArithUtil.setPrecision(maxPrice
                    - avgPricePerHeight * ((float) mLastKData.getCloseY() - mHorizontalYList.get(0)), 2);
            Rect textRect = new Rect();
            crossHairBlueTextPaint.getTextBounds(movePrice, 0, movePrice.length(), textRect);
            canvas.drawText(movePrice,
                    mRightEnd - dp2px(38) + (blueRectF.width() - textRect.width()) / 2,
                    (float) mLastKData.getCloseY() + dp2px(7) - (blueRectF.height() - textRect.height()) / 2,
                    crossHairBlueTextPaint);
        } else {
            double avgPricePerHeight = (maxPrice - minPrice)
                    / (mHorizontalYList.get(3) - mHorizontalYList.get(0));
            String movePrice = ArithUtil.setPrecision(maxPrice
                    - avgPricePerHeight * ((float) mLastKData.getCloseY() - mHorizontalYList.get(0)), 2);
            Rect textRect = new Rect();
            crossHairBlueTextPaint.getTextBounds(movePrice, 0, movePrice.length(), textRect);
            canvas.drawText(movePrice,
                    mRightEnd - dp2px(38) + (blueRectF.width() - textRect.width()) / 2,
                    (float) mLastKData.getCloseY() + dp2px(7) - (blueRectF.height() - textRect.height()) / 2,
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
                mHorizontalYList.get(0) - dp2px(7),
                (float) (maxPriceX + maxPriceRect.width() + dp2px(8)),
                mHorizontalYList.get(0) + dp2px(7));
        canvas.drawRoundRect(maxRectF, 4, 4, grayTimePaint);

        mMaxPriceTrianglePath.reset();
        mMaxPriceTrianglePath.moveTo((float) maxPriceX, mHorizontalYList.get(0));
        mMaxPriceTrianglePath.lineTo((float) (maxPriceX + dp2px(4)), mHorizontalYList.get(0) - dp2px(3));
        mMaxPriceTrianglePath.lineTo((float) (maxPriceX + dp2px(4)), mHorizontalYList.get(0) + dp2px(3));
        mMaxPriceTrianglePath.close();
        canvas.drawPath(mMaxPriceTrianglePath, grayTimePaint);

        canvas.drawText(maxPriceStr,
                (float) (maxPriceX + dp2px(5)),
                mHorizontalYList.get(0) + maxPriceRect.height() / 2,
                crossHairBlueTextPaint);

        //minPrice
        Rect minPriceRect = new Rect();
        String minPriceStr = ArithUtil.setPrecision(minPrice, 2);
        crossHairBlueTextPaint.getTextBounds(minPriceStr, 0, minPriceStr.length(), minPriceRect);

        RectF minRectF = new RectF((float) (minPriceX + dp2px(2)),
                mPriceImgBot - dp2px(7),
                (float) (minPriceX + minPriceRect.width() + dp2px(8)),
                mPriceImgBot + dp2px(7));
        canvas.drawRoundRect(minRectF, 4, 4, grayTimePaint);

        mMinPriceTrianglePath.reset();
        mMinPriceTrianglePath.moveTo((float) minPriceX, mPriceImgBot);
        mMinPriceTrianglePath.lineTo((float) (minPriceX + dp2px(4)), mPriceImgBot - dp2px(3));
        mMinPriceTrianglePath.lineTo((float) (minPriceX + dp2px(4)), mPriceImgBot + dp2px(3));
        mMinPriceTrianglePath.close();
        canvas.drawPath(mMinPriceTrianglePath, grayTimePaint);

        canvas.drawText(minPriceStr,
                (float) (minPriceX + dp2px(4)),
                mPriceImgBot + minPriceRect.height() / 2,
                crossHairBlueTextPaint);

    }

    private void drawDetailData(Canvas canvas) {
        if (mLastKData == null || !isShowDetail) {
            return;
        }
        mDetailTextPaint.getTextBounds(detailLeftTitleArr[0], 0, detailLeftTitleArr[0].length(), mDetailTextRect);

        if (mSingleClickDownX <= getMeasuredWidth() / 2) {
            //边框(右侧)
            canvas.drawRect(mVerticalXList.get(mVerticalXList.size() - 1) - detailRectWidth,
                    mHorizontalYList.get(0),
                    mVerticalXList.get(mVerticalXList.size() - 1),
                    mHorizontalYList.get(0) + detailRectHeight,
                    mDetailLinePaint);

            canvas.drawLine(mVerticalXList.get(mVerticalXList.size() - 1) - detailRectWidth,
                    mHorizontalYList.get(0),
                    mVerticalXList.get(mVerticalXList.size() - 1) - detailRectWidth,
                    mHorizontalYList.get(0) + detailRectHeight,
                    framePaint);

            canvas.drawLine(mVerticalXList.get(mVerticalXList.size() - 1) - detailRectWidth,
                    mHorizontalYList.get(0),
                    mVerticalXList.get(mVerticalXList.size() - 1),
                    mHorizontalYList.get(0),
                    framePaint);

            canvas.drawLine(mVerticalXList.get(mVerticalXList.size() - 1),
                    mHorizontalYList.get(0),
                    mVerticalXList.get(mVerticalXList.size() - 1),
                    mHorizontalYList.get(0) + detailRectHeight,
                    framePaint);

            canvas.drawLine(mVerticalXList.get(mVerticalXList.size() - 1) - detailRectWidth,
                    mHorizontalYList.get(0) + detailRectHeight,
                    mVerticalXList.get(mVerticalXList.size() - 1),
                    mHorizontalYList.get(0) + detailRectHeight,
                    framePaint);

            //详情字段
            for (int i = 0; i < detailLeftTitleArr.length; i++) {
                canvas.drawText(detailLeftTitleArr[i],
                        mVerticalXList.get(mVerticalXList.size() - 1) - detailRectWidth + dp2px(4),
                        mHorizontalYList.get(0) + mDetailTextVerticalSpace * i
                                + mDetailTextRect.height() + (mDetailTextVerticalSpace - mDetailTextRect.height()) / 2,
                        mDetailTextPaint);
            }

            //详情数据
            Paint detailPaint;
            for (int i = 0; i < detailRightDataList.size(); i++) {
                if (i == 5 || i == 6) {
                    if (mLastKData.getUpDnAmount() > 0) {
                        detailPaint = mRedPaint;
                    } else {
                        detailPaint = mGreenPaint;
                    }
                } else {
                    detailPaint = mDetailTextPaint;
                }
                canvas.drawText(detailRightDataList.get(i),
                        mVerticalXList.get(mVerticalXList.size() - 1) - dp2px(4)
                                - mDetailTextPaint.measureText(detailRightDataList.get(i)),
                        mHorizontalYList.get(0) + mDetailTextVerticalSpace * i
                                + mDetailTextRect.height() + (mDetailTextVerticalSpace - mDetailTextRect.height()) / 2,
                        detailPaint);
            }

        } else {
            //边框(左侧)
            canvas.drawRect(mVerticalXList.get(0),
                    mHorizontalYList.get(0),
                    mVerticalXList.get(0) + detailRectWidth,
                    mHorizontalYList.get(0) + detailRectHeight,
                    mDetailLinePaint);

            canvas.drawLine(mVerticalXList.get(0),
                    mHorizontalYList.get(0),
                    mVerticalXList.get(0),
                    mHorizontalYList.get(0) + detailRectHeight,
                    framePaint);

            canvas.drawLine(mVerticalXList.get(0),
                    mHorizontalYList.get(0),
                    mVerticalXList.get(0) + detailRectWidth,
                    mHorizontalYList.get(0),
                    framePaint);

            canvas.drawLine(mVerticalXList.get(0) + detailRectWidth,
                    mHorizontalYList.get(0),
                    mVerticalXList.get(0) + detailRectWidth,
                    mHorizontalYList.get(0) + detailRectHeight,
                    framePaint);

            canvas.drawLine(mVerticalXList.get(0),
                    mHorizontalYList.get(0) + detailRectHeight,
                    mVerticalXList.get(0) + detailRectWidth,
                    mHorizontalYList.get(0) + detailRectHeight,
                    framePaint);

            //文字详情
            for (int i = 0; i < detailLeftTitleArr.length; i++) {
                canvas.drawText(detailLeftTitleArr[i],
                        mVerticalXList.get(0) + dp2px(4),
                        mHorizontalYList.get(0) + mDetailTextVerticalSpace * i
                                + mDetailTextRect.height() + (mDetailTextVerticalSpace - mDetailTextRect.height()) / 2,
                        mDetailTextPaint);
            }

            //详情数据
            Paint detailPaint;
            for (int i = 0; i < detailRightDataList.size(); i++) {
                if (i == 5 || i == 6) {
                    if (mLastKData.getUpDnAmount() > 0) {
                        detailPaint = mRedPaint;
                    } else {
                        detailPaint = mGreenPaint;
                    }
                } else {
                    detailPaint = mDetailTextPaint;
                }
                canvas.drawText(detailRightDataList.get(i),
                        mVerticalXList.get(0) + detailRectWidth - dp2px(4)
                                - mDetailTextPaint.measureText(detailRightDataList.get(i)),
                        mHorizontalYList.get(0) + mDetailTextVerticalSpace * i
                                + mDetailTextRect.height() + (mDetailTextVerticalSpace - mDetailTextRect.height()) / 2,
                        detailPaint);
            }
        }
    }

    //顶部价格MA
    private void drawTopPriceMAData(Canvas canvas) {
        String ma5Str = mMa5 + ArithUtil.setPrecision(mLastKData.getPriceMa5(), 2);
        String ma10Str = mMa10 + ArithUtil.setPrecision(mLastKData.getPriceMa10(), 2);
        String ma30Str = mMa30 + ArithUtil.setPrecision(mLastKData.getPriceMa30(), 2);

        mPriceMa5Paint.getTextBounds(ma5Str, 0, ma5Str.length(), mTopMa5Rect);
        canvas.drawText(ma5Str,
                mLeftStart + dp2px(6),
                mTopStart + mTopMa5Rect.height() + dp2px(6),
                mPriceMa5Paint);

        mPriceMa10Paint.getTextBounds(ma10Str, 0, ma10Str.length(), mTopMa10Rect);
        canvas.drawText(ma10Str,
                mLeftStart + dp2px(6) + mTopMa5Rect.width() + dp2px(10),
                mTopStart + mTopMa5Rect.height() + dp2px(6),
                mPriceMa10Paint);

        mPriceMa30Paint.getTextBounds(ma30Str, 0, ma30Str.length(), mTopMa30Rect);
        canvas.drawText(ma30Str,
                mLeftStart + dp2px(6) + mTopMa5Rect.width() + mTopMa10Rect.width() + dp2px(10) * 2,
                mTopStart + mTopMa5Rect.height() + dp2px(6),
                mPriceMa30Paint);
    }

    //底部MA
    private void drawBotMAData(Canvas canvas) {
        //VOL
        String volStr = mVol + ArithUtil.setPrecision(mLastKData.getVolume(), 2);
        Rect volRect = new Rect();
        mVolPaint.getTextBounds(volStr, 0, volStr.length(), volRect);
        canvas.drawText(volStr,
                mVerticalXList.get(0),
                volumeTopStart - mVerticalSpace / 2 + volRect.height() + dp2px(2),
                mVolPaint);

        String ma5Str = mMa5 + ArithUtil.setPrecision(mLastKData.getVolumeMa5(), 2);
        Rect volMa5Rect = new Rect();
        mPriceMa5Paint.getTextBounds(ma5Str, 0, ma5Str.length(), volMa5Rect);
        canvas.drawText(ma5Str,
                mVerticalXList.get(0) + volRect.width() + dp2px(10),
                volumeTopStart - mVerticalSpace / 2 + volRect.height() + dp2px(2),
                mPriceMa5Paint);

        String ma10Str = mMa10 + ArithUtil.setPrecision(mLastKData.getVolumeMa10(), 2);
        canvas.drawText(ma10Str,
                mVerticalXList.get(0) + volMa5Rect.width() + volRect.width() + dp2px(10) * 2,
                volumeTopStart - mVerticalSpace / 2 + volRect.height() + dp2px(2),
                mPriceMa10Paint);

        if (isShowDeputy && mDeputyImgType == DEPUTY_IMG_MACD) {
            //MACD
            Rect titleRect = new Rect();
            mVolPaint.getTextBounds(mMacdTitle, 0, mMacdTitle.length(), titleRect);
            canvas.drawText(mMacdTitle,
                    mVerticalXList.get(0),
                    mHorizontalYList.get(4) + titleRect.height(),
                    mVolPaint);

            String macdStr = mMacd + ArithUtil.setPrecision(mLastKData.getMacd(), 2);
            canvas.drawText(macdStr,
                    mVerticalXList.get(0) + titleRect.width() + dp2px(10),
                    mHorizontalYList.get(4) + titleRect.height(),
                    mPriceMa5Paint);
            float macdWidth = mPriceMa5Paint.measureText(macdStr);

            String difStr = mDif + ArithUtil.setPrecision(mLastKData.getDif(), 2);
            canvas.drawText(difStr,
                    mVerticalXList.get(0) + titleRect.width() + dp2px(20) + macdWidth,
                    mHorizontalYList.get(4) + titleRect.height(),
                    mPriceMa10Paint);
            float difWidth = mPriceMa10Paint.measureText(difStr);

            canvas.drawText(mDea + ArithUtil.setPrecision(mLastKData.getDea(), 2),
                    mVerticalXList.get(0) + titleRect.width() + dp2px(30) + macdWidth + difWidth,
                    mHorizontalYList.get(4) + titleRect.height(),
                    mPriceMa30Paint);

        } else if (isShowDeputy && mDeputyImgType == DEPUTY_IMG_KDJ) {
            //KDJ
            Rect titleRect = new Rect();
            mVolPaint.getTextBounds(mKdjTitle, 0, mKdjTitle.length(), titleRect);
            canvas.drawText(mKdjTitle,
                    mVerticalXList.get(0),
                    mHorizontalYList.get(4) + titleRect.height(),
                    mVolPaint);

            String kStr = mK + ArithUtil.setPrecision(mLastKData.getK(), 2);
            canvas.drawText(kStr,
                    mVerticalXList.get(0) + titleRect.width() + dp2px(10),
                    mHorizontalYList.get(4) + titleRect.height(),
                    mPriceMa5Paint);
            float kWidth = mPriceMa5Paint.measureText(kStr);

            String dStr = mD + ArithUtil.setPrecision(mLastKData.getD(), 2);
            canvas.drawText(dStr,
                    mVerticalXList.get(0) + titleRect.width() + dp2px(20) + kWidth,
                    mHorizontalYList.get(4) + titleRect.height(),
                    mPriceMa10Paint);
            float dWidth = mPriceMa10Paint.measureText(dStr);

            canvas.drawText(mJ + ArithUtil.setPrecision(mLastKData.getJ(), 2),
                    mVerticalXList.get(0) + titleRect.width() + dp2px(30) + kWidth + dWidth,
                    mHorizontalYList.get(4) + titleRect.height(),
                    mPriceMa30Paint);
        }
    }

    //日期
    private void drawDate(Canvas canvas) {
        mDatePaint.setColor(Color.parseColor("#9BACBD"));
        for (int i = 0; i < 5; i++) {
            if (i == 0) {
                canvas.drawText(dateArr[i],
                        mLeftStart + dp2px(6),
                        mBottomEnd - dp2px(7),
                        mDatePaint);

            } else if (i == dateArr.length - 1) {
                canvas.drawText(dateArr[i],
                        mRightEnd - dp2px(41) - mDatePaint.measureText(dateArr[i]),
                        mBottomEnd - dp2px(7),
                        mDatePaint);

            } else {
                canvas.drawText(dateArr[i],
                        mLeftStart + dp2px(6)
                                + (mRightEnd - mLeftStart - dp2px(47)) / 4 * i - mDatePaint.measureText(dateArr[i]) / 2,
                        mBottomEnd - dp2px(7),
                        mDatePaint);
            }
        }
    }

    //纵坐标
    private void drawOrdinate(Canvas canvas) {
        Rect rect = new Rect();
        //最高价
        mDatePaint.getTextBounds(maxPrice + "", 0, (maxPrice + "").length(), rect);
        canvas.drawText(ArithUtil.setPrecision(maxPrice, 2),
                mVerticalXList.get(mVerticalXList.size() - 1) + dp2px(4),
                mHorizontalYList.get(0) + rect.height(),
                mDatePaint);

        //最低价
        mDatePaint.getTextBounds(minPrice + "", 0, (minPrice + "").length(), rect);
        canvas.drawText(ArithUtil.setPrecision(minPrice, 2),
                mVerticalXList.get(mVerticalXList.size() - 1) + dp2px(4),
                volumeTopStart - mVerticalSpace / 2 - rect.height() + dp2px(1),
                mDatePaint);

        if (!isShowDeputy) {
            double avgPrice = (maxPrice - minPrice) / 4;
            for (int i = 0; i < 3; i++) {
                canvas.drawText(ArithUtil.setPrecision(maxPrice - avgPrice * (i + 1), 2),
                        mVerticalXList.get(mVerticalXList.size() - 1) + dp2px(4),
                        mHorizontalYList.get(i + 1) + rect.height() / 2,
                        mDatePaint);
            }
        } else {
            double avgPrice = (maxPrice - minPrice) / 3;
            for (int i = 0; i < 2; i++) {
                canvas.drawText(ArithUtil.setPrecision(maxPrice - avgPrice * (i + 1), 2),
                        mVerticalXList.get(mVerticalXList.size() - 1) + dp2px(4),
                        mHorizontalYList.get(i + 1) + rect.height() / 2,
                        mDatePaint);
            }
        }

        //最高量
        mDatePaint.getTextBounds(maxVolume + "", 0, (maxVolume + "").length(), rect);
        canvas.drawText(ArithUtil.setPrecision(maxVolume, 2),
                mVerticalXList.get(mVerticalXList.size() - 1) + dp2px(4),
                volumeTopStart - mVerticalSpace / 2 + rect.height() + dp2px(3),
                mDatePaint);

        //最高量/2
        canvas.drawText(ArithUtil.setPrecision(maxVolume / 2, 2),
                mVerticalXList.get(mVerticalXList.size() - 1) + dp2px(4),
                volumeTopStart + rect.height() / 2,
                mDatePaint);

        //数量 0
        canvas.drawText("0",
                mVerticalXList.get(mVerticalXList.size() - 1) + dp2px(4),
                mVolumeImgBot,
                mDatePaint);

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
