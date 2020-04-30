package com.example.admin.klineview.kline;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.admin.klineview.Print;
import com.example.admin.klineview.R;

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

    //view显示的第一条数据在总数据list中的position
    private int startDataNum = 0;
    //首次加载显示的数据条数，可自行修改
    private final int VIEW_DATA_NUM_INIT = 34;
    //放大时最少显示的数据条数，可自行修改
    private final int VIEW_DATA_NUM_MIN = 18;
    //缩小时最多显示的数据条数，可自行修改
    private final int VIEW_DATA_NUM_MAX = 140;
    //view显示的最大数据条数
    private int maxViewDataNum = VIEW_DATA_NUM_INIT;
    //是否显示副图
    private boolean isShowDeputy = false;
    //是否显示详情
    private boolean isShowDetail = false;
    //是否长按
    private boolean isLongPress = false;
    //长按触发时长
    private final int LONG_PRESS_TIME_OUT = 300;
    //是否水平移动
    private boolean isHorizontalMove = false;
    //是否需要请求前期的数据
    private boolean isNeedRequestPreData = true;
    //是否双指触控
    private boolean isDoubleFinger = false;
    //是否显示分时图
    private boolean isShowInstant = false;
    //主图数据类型 0:MA, 1:EMA 2:BOLL
    public static final int MAIN_IMG_MA = 0;
    public static final int MAIN_IMG_EMA = 1;
    public static final int MAIN_IMG_BOLL = 2;
    private int mainImgType = MAIN_IMG_MA;
    //副图数据类型 0:MACD, 1:KDJ, 2:RSI
    public static final int DEPUTY_IMG_MACD = 0;
    public static final int DEPUTY_IMG_KDJ = 1;
    public static final int DEPUTY_IMG_RSI = 2;
    private int deputyImgType = DEPUTY_IMG_MACD;
    //十字线横线上下移动模式 0:固定指向收盘价，1：固定指向开盘价，2：上下自由滑动
    public static final int CROSS_HAIR_MOVE_CLOSE = 0;
    public static final int CROSS_HAIR_MOVE_OPEN = 1;
    public static final int CROSS_HAIR_MOVE_FREE = 2;
    private int crossHairMoveMode = CROSS_HAIR_MOVE_CLOSE;

    private final String STR_MA5 = "Ma5:";
    private final String STR_MA10 = "Ma10:";
    private final String STR_MA30 = "Ma30:";
    private final String STR_VOL = "VOL:";
    private final String STR_MACD_TITLE = "MACD(12,26,9)";
    private final String STR_MACD = "MACD:";
    private final String STR_DIF = "DIF:";
    private final String STR_DEA = "DEA:";
    private final String STR_KDJ_TITLE = "KDJ(9,3,3)";
    private final String STR_K = "K:";
    private final String STR_D = "D:";
    private final String STR_J = "J:";
    private final String STR_RSI_TITLE = "RSI(6,12,24)";
    private final String STR_RS1 = "RS1:";
    private final String STR_RS2 = "RS2:";
    private final String STR_RS3 = "RS3:";

    private int initTotalListSize = 0;

    private Paint strokePaint, fillPaint, instantFillPaint;
    private Path curvePath, instantPath;

    private Rect topMa5Rect = new Rect();
    private Rect topMa10Rect = new Rect();
    private Rect topMa30Rect = new Rect();
    private Rect detailTextRect = new Rect();

    private String[] detailLeftTitleArr;
    private List<KData> totalDataList = new ArrayList<>();
    private List<KData> viewDataList = new ArrayList<>();
    private List<KData> endDataList = new ArrayList<>();

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

    private KData lastKData;
    private OnRequestDataListListener requestListener;
    private QuotaThread quotaThread;
    private Runnable mDelayRunnable;
    private Runnable longPressRunnable;
    private GestureDetector gestureDetector;

    private int priceIncreaseCol, priceFallCol, priceMa5Col, priceMa10Col, priceMa30Col,
            priceMaxLabelCol, priceMinLabelCol, volumeTextCol, volumeMa5Col, volumeMa10Col, macdTextCol,
            macdPositiveCol, macdNegativeCol, difLineCol, deaLineCol, kLineCol, dLineCol,
            jLineCol, abscissaTextCol, ordinateTextCol, crossHairCol, crossHairRightLabelCol,
            crossHairBottomLabelCol, crossHairRightLabelTextCol, detailFrameCol, detailTextCol, tickMarkCol,
            detailBgCol, detailRectWidth, abscissaTextSize, volumeTextSize, crossHairBottomLabelTextCol,
            priceMaxLabelTextCol, priceMinLabelTextCol, priceMaxLabelTextSize, priceMinLabelTextSize,
            crossHairRightLabelTextSize, crossHairBottomLabelTextSize, ordinateTextSize, detailTextSize,
            topMaTextSize, detailRectHeight, moveLimitDistance;

    private float leftStart, topStart, rightEnd, bottomEnd, mulFirstDownX, mulFirstDownY, lastDiffMoveX,
            lastDiffMoveY, singleClickDownX, detailTextVerticalSpace, longPressMoveY, dispatchDownY,
            volumeImgBot, verticalSpace, flingVelocityX, priceImgBot, deputyTopY, deputyCenterY,
            singleClickDownY, mulSecondDownX, longPressDownX, longPressDownY, dispatchDownX;

    private double maxPrice, topPrice, maxPriceX, minPrice, botPrice, minPriceX, maxVolume, avgHeightPerPrice,
            avgPriceRectWidth, avgHeightPerVolume, avgHeightMacd, avgHeightDea, avgHeightDif,
            avgHeightK, avgHeightD, avgHeightJ, mMaxPriceY, avgHeightRSI,
            mMinPriceY, mMaxMacd, mMinMacd, mMaxK;


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
     * ---仅限于首次初始化赋值，不可用于更新数据，如要更新，请调用resetDataList---
     * 控件初始化时添加的数据量，建议每次添加数据在2000条左右
     * 已对性能做优化，总数据量十万条以上对用户体验没有影响
     * 首次加载5000条数据，页面初始化到加载完成，总共耗时400+ms，不超过0.5秒。
     * 分页加载5000条数据时，如果正在滑动过程中，添加数据的那一瞬间会稍微有一下卡顿，影响不大。
     * 经测试，800块的华为荣耀6A 每次添加4000条以下数据不会有卡顿，很流畅。
     */
    public void initKDataList(List<KData> dataList) {
        if (dataList == null || dataList.isEmpty() || totalDataList == null
                || totalDataList.size() > 0) {
            return;
        }
        this.totalDataList.addAll(dataList);
        startDataNum = totalDataList.size() - maxViewDataNum;
        QuotaUtil.initMa(totalDataList, false);
        resetViewData();
    }

    /**
     * 获取总数据list
     */
    public List<KData> getTotalDataList(){
        return totalDataList;
    }

    /**
     * 获取显示的数据list
     */
    public List<KData> getViewDataList(){
        return viewDataList;
    }

    /**
     * 添加最新的单条数据
     */
    public void addSingleData(KData data) {
        if (data == null || endDataList == null || totalDataList == null) {
            return;
        }
        endDataList.clear();
        int startIndex;
        if (totalDataList.size() >= 30) {
            startIndex = totalDataList.size() - 30;
        } else {
            startIndex = 0;
        }
        endDataList.addAll(totalDataList.subList(startIndex, totalDataList.size()));
        endDataList.add(data);
        if (quotaThread != null) {
            quotaThread.quotaSingleCalculate(endDataList);
        }
    }

    /**
     * 分页加载，向前期滑动时，进行分页加载添加数据，建议每次添加数据在2000条左右
     * 配合setOnRequestDataListListener接口使用实现自动分页加载
     *
     * @param isNeedReqPre 下次向前期滑动到边界时，是否需要自动调用接口请求数据
     */
    public void addPreDataList(List<KData> dataList, boolean isNeedReqPre) {
        if (dataList == null || dataList.isEmpty() || totalDataList == null) {
            return;
        }
        isNeedRequestPreData = isNeedReqPre;
        totalDataList.addAll(0, dataList);
        startDataNum += dataList.size();
        if (quotaThread != null) {
            quotaThread.quotaListCalculate(totalDataList);
        }
    }

    /**
     * 分页加载，向前期滑动时，进行分页加载添加数据，建议每次添加数据在2000条左右
     * 配合setOnRequestDataListListener接口使用实现自动分页加载
     * 首次调用该方法后会记录该次list.size，后续继续调用时会将传进来的list.size与首次
     * 的进行比较，如果比首次的size小，则判定为数据已拿完，不再自动调用接口请求数据。
     * <p>
     * ---该方法仅在能保证每次分页加载拿到的数据list.size相同的情况下调用，否则，请调用
     * 上面的方法，手动传入isNeedReqPre用来判断是否需要继续自动调用接口请求数据
     */
    public void addPreDataList(List<KData> dataList) {
        if (dataList == null || dataList.isEmpty() || totalDataList == null) {
            return;
        }
        if (initTotalListSize == 0) {
            initTotalListSize = dataList.size();
        }
        isNeedRequestPreData = dataList.size() >= initTotalListSize;
        totalDataList.addAll(0, dataList);
        startDataNum += dataList.size();
        if (quotaThread != null) {
            quotaThread.quotaListCalculate(totalDataList);
        }
    }

    /**
     * 重置所有数据，默认不作定位
     */
    public void resetDataList(List<KData> dataList) {
        if (dataList == null || dataList.isEmpty()){
            return;
        }
        resetDataList(dataList, false);
    }

    /**
     * 重置所有数据
     *
     * @param isNeedLocateCurrent 重置后的数据是否需要定位到重置前移动到的时间点，例如：
     *                            重置前已经滑动到9月20号，true则在重置后会将新数据定位到9月20号。
     *                            false则不作定位，view右边直接显示为最新的数据
     */
    public void resetDataList(List<KData> dataList, boolean isNeedLocateCurrent) {
        if (dataList == null || dataList.isEmpty() || viewDataList == null || totalDataList == null) {
            return;
        }
        long currentStartTime = 0;
        if (viewDataList.size() > 0) {
            currentStartTime = viewDataList.get(0).getTime();
        }
        isShowDetail = false;

        this.totalDataList.clear();
        this.totalDataList.addAll(dataList);
        QuotaUtil.initMa(totalDataList, false);
        switch (mainImgType) {
            case MAIN_IMG_EMA:
                QuotaUtil.initEma(totalDataList, false);
                break;

            case MAIN_IMG_BOLL:
                QuotaUtil.initBoll(totalDataList, false);
                break;

            default:
                break;
        }
        switch (deputyImgType) {
            case DEPUTY_IMG_MACD:
                QuotaUtil.initMACD(totalDataList, false);
                break;

            case DEPUTY_IMG_KDJ:
                QuotaUtil.initKDJ(totalDataList, false);
                break;

            case DEPUTY_IMG_RSI:
                QuotaUtil.initRSI(totalDataList, false);
                break;

            default:
                break;
        }

        if (isNeedLocateCurrent) {
            int halfSizeNum = totalDataList.size() / 2;
            startDataNum = -1;
            if (totalDataList.get(0).getTime() <= currentStartTime
                    && totalDataList.get(halfSizeNum).getTime() > currentStartTime) {
                for (int i = 0; i < halfSizeNum; i++) {
                    if (i + 1 < totalDataList.size() && totalDataList.get(i).getTime() <= currentStartTime
                            && totalDataList.get(i + 1).getTime() > currentStartTime) {
                        startDataNum = i;
                        break;
                    }
                }
            } else if (totalDataList.get(halfSizeNum).getTime() <= currentStartTime
                    && totalDataList.get(totalDataList.size() - 1).getTime() >= currentStartTime) {
                for (int i = halfSizeNum; i < totalDataList.size(); i++) {
                    if (i + 1 < totalDataList.size() && totalDataList.get(i).getTime() <= currentStartTime
                            && totalDataList.get(i + 1).getTime() > currentStartTime) {
                        startDataNum = i;
                        break;
                    }
                }
            }

            if (totalDataList.size() < maxViewDataNum) {
                startDataNum = 0;
            } else if (totalDataList.size() - startDataNum < maxViewDataNum || startDataNum == -1) {
                startDataNum = totalDataList.size() - maxViewDataNum;
            }

        } else {
            if (totalDataList.size() < maxViewDataNum) {
                startDataNum = 0;
            } else {
                startDataNum = totalDataList.size() - maxViewDataNum;
            }
        }

        resetViewData();
    }

    /**
     * 设置主图显示类型
     * MA: MAIN_IMG_MA
     * EMA: MAIN_IMG_EMA
     * BOLL: MAIN_IMG_BOLL
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

            default:
                break;
        }
        this.mainImgType = type;
        invalidate();
    }

    /**
     * 是否显示副图
     */
    public void setDeputyPicShow(boolean showState) {
        this.isShowDeputy = showState;
        if (isShowDeputy) {
            setDeputyImgType(deputyImgType);
        }
        invalidate();
    }

    /**
     * 设置副图显示类型
     * MACD: DEPUTY_IMG_MACD
     * KDJ: DEPUTY_IMG_KDJ
     * RSI: DEPUTY_IMG_RSI
     */
    public void setDeputyImgType(int type) {
        this.deputyImgType = type;
        switch (deputyImgType) {
            case DEPUTY_IMG_MACD:
                QuotaUtil.initMACD(totalDataList, false);
                break;

            case DEPUTY_IMG_KDJ:
                QuotaUtil.initKDJ(totalDataList, false);
                break;

            case DEPUTY_IMG_RSI:
                QuotaUtil.initRSI(totalDataList, false);
                break;

            default:
                break;
        }
        invalidate();
    }

    /**
     * 设置十字线的横线上下移动模式
     * 固定指向收盘价: CROSS_HAIR_MOVE_CLOSE
     * 固定指向开盘价: CROSS_HAIR_MOVE_OPEN
     * 上下自由滑动: CROSS_HAIR_MOVE_FREE
     */
    public void setCrossHairMoveMode(int moveMode) {
        this.crossHairMoveMode = moveMode;
    }

    /**
     * 获取副图是否显示
     */
    public boolean getVicePicShow() {
        return this.isShowDeputy;
    }

    /**
     * 退出页面时停止子线程并置空，便于回收，避免内存泄露
     */
    public void cancelQuotaThread() {
        if (quotaThread != null) {
            quotaThread.setUIHandler(null);
            quotaThread.quit();
            quotaThread = null;
        }
        removeCallbacks(mDelayRunnable);
        removeCallbacks(longPressRunnable);
    }

    /**
     * 是否显示分时图
     */
    public void setShowInstant(boolean state){
        this.isShowInstant = state;
        invalidate();
    }

    /**
     * 获取分时图是否显示
     */
    public boolean isShowInstant(){
        return this.isShowInstant;
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KLineView);
            tickMarkCol = typedArray.getColor(R.styleable.KLineView_klTickMarkLineCol, 0xffF7F7FB);
            abscissaTextCol = typedArray.getColor(R.styleable.KLineView_klAbscissaTextCol, 0xff9BACBD);
            abscissaTextSize = typedArray.getInt(R.styleable.KLineView_klAbscissaTextSize, 8);
            ordinateTextCol = typedArray.getColor(R.styleable.KLineView_klOrdinateTextCol, abscissaTextCol);
            ordinateTextSize = typedArray.getInt(R.styleable.KLineView_klOrdinateTextSize, abscissaTextSize);
            topMaTextSize = typedArray.getInt(R.styleable.KLineView_klTopMaTextSize, 10);
            priceIncreaseCol = typedArray.getColor(R.styleable.KLineView_klPriceIncreaseCol, 0xffFF5442);
            priceFallCol = typedArray.getColor(R.styleable.KLineView_klPriceFallCol, 0xff2BB8AB);
            priceMa5Col = typedArray.getColor(R.styleable.KLineView_klPriceMa5LineCol, 0xffFFA800);
            priceMa10Col = typedArray.getColor(R.styleable.KLineView_klPriceMa10LineCol, 0xff2668FF);
            priceMa30Col = typedArray.getColor(R.styleable.KLineView_klPriceMa30LineCol, 0xffFF45A1);
            priceMaxLabelCol = typedArray.getColor(R.styleable.KLineView_klPriceMaxLabelCol, 0xffC0C6C9);
            priceMaxLabelTextCol = typedArray.getColor(R.styleable.KLineView_klPriceMaxLabelTextCol, 0xffffffff);
            priceMaxLabelTextSize = typedArray.getInt(R.styleable.KLineView_klPriceMaxLabelTextSize, 10);
            priceMinLabelCol = typedArray.getColor(R.styleable.KLineView_klPriceMinLabelCol, priceMaxLabelCol);
            priceMinLabelTextCol = typedArray.getColor(R.styleable.KLineView_klPriceMinLabelTextCol, priceMaxLabelTextCol);
            priceMinLabelTextSize = typedArray.getInt(R.styleable.KLineView_klPriceMinLabelTextSize, 10);
            volumeTextCol = typedArray.getColor(R.styleable.KLineView_klVolumeTextCol, 0xff9BACBD);
            volumeTextSize = typedArray.getInt(R.styleable.KLineView_klVolumeTextSize, 10);
            volumeMa5Col = typedArray.getColor(R.styleable.KLineView_klVolumeMa5LineCol, priceMa5Col);
            volumeMa10Col = typedArray.getColor(R.styleable.KLineView_klVolumeMa10LineCol, priceMa10Col);
            macdTextCol = typedArray.getColor(R.styleable.KLineView_klMacdTextCol, volumeTextCol);
            macdPositiveCol = typedArray.getColor(R.styleable.KLineView_klMacdPositiveCol, priceIncreaseCol);
            macdNegativeCol = typedArray.getColor(R.styleable.KLineView_klMacdNegativeCol, priceFallCol);
            difLineCol = typedArray.getColor(R.styleable.KLineView_klDifLineCol, priceMa10Col);
            deaLineCol = typedArray.getColor(R.styleable.KLineView_klDeaLineCol, priceMa30Col);
            kLineCol = typedArray.getColor(R.styleable.KLineView_klKLineCol, priceMa5Col);
            dLineCol = typedArray.getColor(R.styleable.KLineView_klDLineCol, priceMa10Col);
            jLineCol = typedArray.getColor(R.styleable.KLineView_klJLineCol, priceMa30Col);
            crossHairCol = typedArray.getColor(R.styleable.KLineView_klCrossHairCol, 0xff828EA2);
            crossHairRightLabelCol = typedArray.getColor(R.styleable.KLineView_klCrossHairRightLabelCol, 0xff3193FF);
            crossHairRightLabelTextCol = typedArray.getColor(R.styleable.KLineView_klCrossHairRightLabelTextCol, 0xffffffff);
            crossHairRightLabelTextSize = typedArray.getInt(R.styleable.KLineView_klCrossHairRightLabelTextSize, 10);
            crossHairBottomLabelCol = typedArray.getColor(R.styleable.KLineView_klCrossHairBottomLabelCol, priceMaxLabelCol);
            crossHairBottomLabelTextCol = typedArray.getColor(R.styleable.KLineView_klCrossHairBottomLabelTextCol, 0xffffffff);
            crossHairBottomLabelTextSize = typedArray.getInt(R.styleable.KLineView_klCrossHairBottomLabelTextSize, 8);
            detailFrameCol = typedArray.getColor(R.styleable.KLineView_klDetailFrameCol, 0xffB5C0D0);
            detailTextCol = typedArray.getColor(R.styleable.KLineView_klDetailTextCol, 0xff808F9E);
            detailTextSize = typedArray.getInt(R.styleable.KLineView_klDetailTextSize, 10);
            detailBgCol = typedArray.getColor(R.styleable.KLineView_klDetailBgCol, 0xe6ffffff);
            typedArray.recycle();
        }
    }

    private void initData() {
        super.setOnTouchListener(this);
        super.setClickable(true);
        super.setFocusable(true);
        gestureDetector = new GestureDetector(getContext(), new CustomGestureListener());
        moveLimitDistance = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        detailRectWidth = dp2px(103);
        detailRectHeight = dp2px(120);
        detailTextVerticalSpace = (detailRectHeight - dp2px(4)) / 8;
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

        instantFillPaint = new Paint();
        instantFillPaint.setAntiAlias(true);
        instantFillPaint.setStyle(Paint.Style.FILL);

        curvePath = new Path();
        instantPath = new Path();

        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                isLongPress = true;
                isShowDetail = true;
                getClickKData(longPressDownX);
                invalidate();
            }
        };
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
            if (endDataList == null || totalDataList == null) {
                return false;
            }
            KData endLastData = endDataList.get(endDataList.size() - 1);
            int totalSize = totalDataList.size();
            KData totalLastData = totalDataList.get(totalSize - 1);
            if (endLastData.getTime() == totalLastData.getTime()) {
                totalDataList.remove(totalSize - 1);
            }
            totalDataList.add(endLastData);
            if (totalSize >= maxViewDataNum
                    && startDataNum == totalSize - maxViewDataNum - 1) {
                startDataNum++;
                resetViewData();
            } else {
                resetViewData();
            }
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        leftStart = getPaddingLeft();
        topStart = getPaddingTop();
        rightEnd = getMeasuredWidth() - getPaddingRight();
        bottomEnd = getMeasuredHeight() - getPaddingBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (totalDataList.isEmpty() || viewDataList.isEmpty()) {
            return;
        }
        resetData();

        drawTickMark(canvas);
        drawAbscissa(canvas);
        drawOrdinate(canvas);
        drawCrossHairLine(canvas);
        drawVolume(canvas);

        if (isShowInstant){
            crossHairMoveMode = CROSS_HAIR_MOVE_CLOSE;
            drawInstant(canvas);
        } else {
            drawMainDeputyRect(canvas);
            drawBezierCurve(canvas);
            drawTopPriceMAData(canvas);
            drawBotMAData(canvas);
            drawDetailData(canvas);
            drawMaxMinPriceLabel(canvas);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            longPressDownX = event.getX();
            longPressDownY = event.getY();
            dispatchDownX = event.getX();
            dispatchDownY = event.getY();
            isLongPress = false;
            postDelayed(longPressRunnable, LONG_PRESS_TIME_OUT);

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //长按控制
            float diffDispatchMoveX = Math.abs(event.getX() - longPressDownX);
            float diffDispatchMoveY = Math.abs(event.getY() - longPressDownY);
            float moveDistanceX = Math.abs(event.getX() - dispatchDownX);
            float moveDistanceY = Math.abs(event.getY() - dispatchDownY);
            longPressMoveY = event.getY();
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }

            if (isHorizontalMove || (diffDispatchMoveX > diffDispatchMoveY + dp2px(5)
                    && diffDispatchMoveX > moveLimitDistance)
                    || (isLongPress && diffDispatchMoveY > moveLimitDistance)) {
                isHorizontalMove = true;
                removeCallbacks(longPressRunnable);

                if (isLongPress && (moveDistanceX > 1 || moveDistanceY > 1)) {
                    getClickKData(event.getX());
                    if (lastKData != null) {
                        invalidate();
                    }
                }

                dispatchDownX = event.getX();
                dispatchDownY = event.getY();
                return isLongPress || super.dispatchTouchEvent(event);

            } else if (!isLongPress && !isHorizontalMove && !isDoubleFinger
                    && diffDispatchMoveY > diffDispatchMoveX + dp2px(5)
                    && diffDispatchMoveY > moveLimitDistance) {
                removeCallbacks(longPressRunnable);
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            isHorizontalMove = false;
            removeCallbacks(longPressRunnable);
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
        }

        return isLongPress || super.dispatchTouchEvent(event);

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                singleClickDownX = event.getX();
                singleClickDownY = event.getY();
                flingVelocityX = 0;
                mulFirstDownX = event.getX(0);
                mulFirstDownY = event.getY(0);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                isShowDetail = false;
                isDoubleFinger = true;
                removeCallbacks(longPressRunnable);
                mulSecondDownX = event.getX(1);
                float mulSecondDownY = event.getY(1);
                lastDiffMoveX = Math.abs(mulSecondDownX - mulFirstDownX);
                lastDiffMoveY = Math.abs(mulSecondDownY - mulFirstDownY);
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() > 1) {
                    float mulFirstMoveX = event.getX(0);
                    float mulFirstMoveY = event.getY(0);
                    float mulSecondMoveX = event.getX(1);
                    float mulSecondMoveY = event.getY(1);
                    float diffMoveX = Math.abs(mulSecondMoveX - mulFirstMoveX);
                    float diffMoveY = Math.abs(mulSecondMoveY - mulFirstMoveY);

                    //双指分开，放大显示
                    if ((diffMoveX >= diffMoveY && diffMoveX - lastDiffMoveX > 1)
                            || (diffMoveY >= diffMoveX && diffMoveY - lastDiffMoveY > 1)) {

                        if (maxViewDataNum <= VIEW_DATA_NUM_MIN) {
                            maxViewDataNum = VIEW_DATA_NUM_MIN;

                            //如果view中显示的数据量小于当前最大数据条数，则不管双指如何落点，都向左放大
                        } else if (viewDataList != null && viewDataList.size() < maxViewDataNum) {
                            maxViewDataNum -= 2;
                            startDataNum = totalDataList.size() - maxViewDataNum;

                            //如果双指起始落点都在中线左侧，则左边不动，放大右边
                        } else if (verticalXList != null && mulFirstDownX < verticalXList.get(2)
                                && mulSecondDownX <= verticalXList.get(2)) {
                            maxViewDataNum -= 2;

                            //如果双指起始落点在中线左右两侧，则左右同时放大
                        } else if ((verticalXList != null && mulFirstDownX <= verticalXList.get(2)
                                && mulSecondDownX >= verticalXList.get(2))
                                || (verticalXList != null && mulFirstDownX >= verticalXList.get(2)
                                && mulSecondDownX <= verticalXList.get(2))) {
                            maxViewDataNum -= 2;
                            startDataNum += 1;

                            //如果双指起始落点在中线右侧，则右边不动，放大左边
                        } else if (verticalXList != null && mulFirstDownX >= verticalXList.get(2)
                                && mulSecondDownX > verticalXList.get(2)) {
                            maxViewDataNum -= 2;
                            startDataNum += 2;
                        }
                        resetViewData();

                        //双指靠拢，缩小显示
                    } else if ((diffMoveX >= diffMoveY && diffMoveX - lastDiffMoveX < -1)
                            || (diffMoveY >= diffMoveX && diffMoveY - lastDiffMoveY < -1)) {

                        if (maxViewDataNum >= VIEW_DATA_NUM_MAX) {
                            maxViewDataNum = VIEW_DATA_NUM_MAX;

                            //如果view显示的数据是totalDataList最末尾的数据，则只向左边缩小
                        } else if (totalDataList != null
                                && startDataNum + maxViewDataNum >= totalDataList.size()) {
                            maxViewDataNum += 2;
                            startDataNum = totalDataList.size() - maxViewDataNum;

                            //如果view显示的数据是totalDataList最开始的数据，则只向右边缩小
                        } else if (startDataNum <= 0) {
                            startDataNum = 0;
                            maxViewDataNum += 2;

                            //如果双指起始落点都在中线左侧，则左边不动，右边缩小
                        } else if (verticalXList != null && mulFirstDownX < verticalXList.get(2)
                                && mulSecondDownX <= verticalXList.get(2)) {
                            maxViewDataNum += 2;

                            //如果双指起始落点在中线左右两侧，则左右同时缩小
                        } else if ((verticalXList != null && mulFirstDownX <= verticalXList.get(2)
                                && mulSecondDownX >= verticalXList.get(2))
                                || (verticalXList != null && mulFirstDownX >= verticalXList.get(2)
                                && mulSecondDownX <= verticalXList.get(2))) {
                            maxViewDataNum += 2;
                            startDataNum -= 1;

                            //如果双指起始落点都在中线右侧，则右边不动，左边缩小
                        } else if (verticalXList != null && mulFirstDownX >= verticalXList.get(2)
                                && mulSecondDownX > verticalXList.get(2)) {
                            maxViewDataNum += 2;
                            startDataNum -= 2;
                        }
                        resetViewData();

                    }
                    lastDiffMoveX = Math.abs(mulSecondMoveX - mulFirstMoveX);
                    lastDiffMoveY = Math.abs(mulSecondMoveY - mulFirstMoveY);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!isDoubleFinger) {
                    float diffTouchMoveX = Math.abs(event.getX() - singleClickDownX);
                    float diffTouchMoveY = Math.abs(event.getY() - singleClickDownY);
                    if (diffTouchMoveY < moveLimitDistance && diffTouchMoveX < moveLimitDistance) {
                        isShowDetail = true;
                        if (crossHairMoveMode == CROSS_HAIR_MOVE_FREE) {
                            longPressMoveY = event.getY();
                        }
                        getClickKData(singleClickDownX);
                        if (lastKData != null) {
                            invalidate();
                        }
                    }
                }
                isDoubleFinger = false;
                break;

            case MotionEvent.ACTION_CANCEL:
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
            if ((startDataNum == 0 && distanceX < 0)
                    || (totalDataList != null && startDataNum == totalDataList.size() - maxViewDataNum  && distanceX > 0)
                    || startDataNum < 0
                    || (viewDataList != null && viewDataList.size() < maxViewDataNum)) {
                if (isShowDetail) {
                    isShowDetail = false;
                    if (!viewDataList.isEmpty()) {
                        lastKData = viewDataList.get(viewDataList.size() - 1);
                    }
                    invalidate();
                }
                return true;
            } else {
                isShowDetail = false;
                if (Math.abs(distanceX) > 1) {
                    moveData(distanceX);
                    invalidate();
                }
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (totalDataList != null && startDataNum > 0
                    && startDataNum < totalDataList.size() - 1 - maxViewDataNum) {
                if (velocityX > 8000) {
                    flingVelocityX = 8000;
                } else if (velocityX < -8000) {
                    flingVelocityX = -8000;
                } else {
                    flingVelocityX = velocityX;
                }
                stopDelay();
            }
            return true;
        }
    }

    private void stopDelay() {
        post(mDelayRunnable);
    }

    private void initStopDelay() {
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
                    if (startDataNum > totalDataList.size() - maxViewDataNum) {
                        startDataNum = totalDataList.size() - maxViewDataNum;
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
                requestNewData();

                if (Math.abs(flingVelocityX) > 200) {
                    postDelayed(this, 15);
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
        if (totalDataList != null){
            int size = totalDataList.size();
            if (startDataNum > size - maxViewDataNum) {
                startDataNum = size - maxViewDataNum;
            }
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
        if (totalDataList != null && startDataNum <= totalDataList.size() / 3 && isNeedRequestPreData) {
            isNeedRequestPreData = false;
            if (requestListener != null){
                requestListener.requestData();
            }
        }
    }

    private void resetViewData() {
        if (viewDataList == null || totalDataList == null){
            return;
        }
        viewDataList.clear();
        int currentViewDataNum = Math.min(maxViewDataNum, totalDataList.size());
        if (startDataNum >= 0) {
            for (int i = 0; i < currentViewDataNum; i++) {
                if (i + startDataNum < totalDataList.size()) {
                    viewDataList.add(totalDataList.get(i + startDataNum));
                }
            }
        } else {
            for (int i = 0; i < currentViewDataNum; i++) {
                viewDataList.add(totalDataList.get(i));
            }
        }
        if (viewDataList.size() > 0 && !isShowDetail) {
            lastKData = viewDataList.get(viewDataList.size() - 1);
        } else if (viewDataList.isEmpty()) {
            lastKData = null;
        }
        invalidate();
    }

    private void resetData() {
        if (verticalXList == null || horizontalYList == null || rightEnd == 0) {
            return;
        }
        //垂直刻度线
        float horizontalSpace = (rightEnd - leftStart - (dp2px(46))) / 4;
        verticalXList.clear();
        for (int i = 0; i < 5; i++) {
            verticalXList.add(leftStart + horizontalSpace * (i) + dp2px(6));
        }
        //水平刻度线
        verticalSpace = (bottomEnd - topStart - dp2px(38)) / 5;
        horizontalYList.clear();
        for (int i = 0; i < 6; i++) {
            horizontalYList.add(topStart + verticalSpace * i + dp2px(18));
        }
        //副图顶线
        deputyTopY = horizontalYList.get(4) + dp2px(12);

        if (verticalXList == null || horizontalYList == null || viewDataList == null) {
            return;
        }
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

        int viewDataSize = viewDataList.size();
        for (int i = 0; i < viewDataSize; i++) {
            KData viewKData = viewDataList.get(i);
            double rightX = verticalXList.get(verticalXList.size() - 1)
                    - (viewDataList.size() - i - 1) * avgPriceRectWidth;
            double leftX = rightX - avgPriceRectWidth;
            double centerX = rightX - avgPriceRectWidth/2;
            viewKData.setLeftX(leftX);
            viewKData.setRightX(rightX);
            viewKData.setCenterX(centerX);

            if (viewKData.getMaxPrice() >= maxPrice) {
                maxPrice = viewKData.getMaxPrice();
                maxPriceX = viewKData.getLeftX() + avgPriceRectWidth / 2;
            }
            if (viewKData.getMinPrice() <= minPrice) {
                minPrice = viewKData.getMinPrice();
                minPriceX = viewKData.getLeftX() + avgPriceRectWidth / 2;
            }
            if (viewKData.getVolume() >= maxVolume) {
                maxVolume = viewKData.getVolume();
            }

            if (!isShowDeputy || isShowInstant){
                continue;
            }
            if (deputyImgType == DEPUTY_IMG_MACD) {
                if (viewKData.getMacd() >= mMaxMacd) {
                    mMaxMacd = viewKData.getMacd();
                }
                if (viewKData.getMacd() <= mMinMacd) {
                    mMinMacd = viewKData.getMacd();
                }
                if (viewKData.getDea() >= maxDea) {
                    maxDea = viewKData.getDea();
                }
                if (viewKData.getDea() <= minDea) {
                    minDea = viewKData.getDea();
                }
                if (viewKData.getDif() >= maxDif) {
                    maxDif = viewKData.getDif();
                }
                if (viewKData.getDif() <= minDif) {
                    minDif = viewKData.getDif();
                }

            } else if (deputyImgType == DEPUTY_IMG_KDJ) {
                if (viewKData.getK() >= mMaxK) {
                    mMaxK = viewKData.getK();
                }
                if (viewKData.getD() >= maxD) {
                    maxD = viewKData.getD();
                }
                if (viewKData.getJ() >= maxJ) {
                    maxJ = viewKData.getJ();
                }
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

        for (KData kData : viewDataList) {
            double openPrice = kData.getOpenPrice();
            double closedPrice = kData.getClosePrice();
            kData.setCloseY((float) (horizontalYList.get(0) + (topPrice - closedPrice) * avgHeightPerPrice));
            kData.setOpenY((float) (horizontalYList.get(0) + (topPrice - openPrice) * avgHeightPerPrice));
        }

        if (!isShowDeputy){
            return;
        }
        switch (deputyImgType) {
            case DEPUTY_IMG_MACD:
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
                break;

            case DEPUTY_IMG_KDJ:
                //K
                avgHeightK = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(12)) / mMaxK;
                //D
                avgHeightD = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(12)) / maxD;
                //J
                avgHeightJ = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY - dp2px(12)) / maxJ;
                break;

            case DEPUTY_IMG_RSI:
                avgHeightRSI = (horizontalYList.get(horizontalYList.size() - 1) - deputyTopY) / 100;
                break;
        }
    }

    //刻度线
    private void drawTickMark(Canvas canvas) {
        if (verticalXList == null || horizontalYList == null){
            return;
        }
        resetStrokePaint(tickMarkCol, 0);
        for (Float aFloat : verticalXList) {
            canvas.drawLine(aFloat, topStart + dp2px(18), aFloat, bottomEnd - dp2px(20), strokePaint);
        }
        float horizontalRightEnd;
        for (int i = 0; i < horizontalYList.size(); i++) {
            if (i == 0 || i == 5 || i == 4 || (isShowDeputy && i == 3)) {
                horizontalRightEnd = rightEnd;
            } else {
                horizontalRightEnd = verticalXList.get(verticalXList.size() - 1);
            }
            canvas.drawLine(leftStart + dp2px(6),
                    horizontalYList.get(i),
                    horizontalRightEnd,
                    horizontalYList.get(i),
                    strokePaint);
        }

        canvas.drawLine(leftStart + dp2px(6),
                horizontalYList.get(4) + verticalSpace / 2,
                verticalXList.get(verticalXList.size() - 1),
                horizontalYList.get(4) + verticalSpace / 2,
                strokePaint);
        //数量中线
        if (isShowDeputy) {
            canvas.drawLine(leftStart + dp2px(6),
                    horizontalYList.get(3) + verticalSpace / 2,
                    verticalXList.get(verticalXList.size() - 1),
                    horizontalYList.get(3) + verticalSpace / 2,
                    strokePaint);
        }
    }

    //主副图蜡烛图
    private void drawMainDeputyRect(Canvas canvas) {
        int viewDataSize = viewDataList.size();
        for (int i = 0; i < viewDataSize; i++) {
            //drawPriceRectAndLine
            KData viewKData = viewDataList.get(i);
            double openPrice = viewKData.getOpenPrice();
            double closedPrice = viewKData.getClosePrice();
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

            //如果开盘价==收盘价，则给1px的高度
            float upPriceCoordinate = (float) (mMaxPriceY + (maxPrice - higherPrice) * avgHeightPerPrice);
            float downPriceCoordinate = (float) (mMaxPriceY + (maxPrice - lowerPrice) * avgHeightPerPrice);
            if (upPriceCoordinate == downPriceCoordinate){
                downPriceCoordinate = upPriceCoordinate + 1;
            }

            //priceRect
            canvas.drawRect((float) viewKData.getLeftX() + dp2px(0.5f),
                    upPriceCoordinate,
                    (float) viewKData.getRightX() - dp2px(0.5f),
                    downPriceCoordinate,
                    fillPaint);

            //priceLine
            canvas.drawLine((float) (viewKData.getCenterX()),
                    (float) (mMaxPriceY + (maxPrice - viewKData.getMaxPrice()) * avgHeightPerPrice),
                    (float) (viewKData.getCenterX()),
                    (float) (mMaxPriceY + (maxPrice - viewKData.getMinPrice()) * avgHeightPerPrice),
                    strokePaint);

            //MACD
            if (isShowDeputy && deputyImgType == DEPUTY_IMG_MACD) {
                double macd = viewKData.getMacd();
                if (macd > 0) {
                    fillPaint.setColor(macdPositiveCol);
                    canvas.drawRect((float) (viewKData.getLeftX() + dp2px(0.5f)),
                            (float) (deputyCenterY - macd * avgHeightMacd),
                            (float) viewKData.getRightX() - dp2px(0.5f),
                            deputyCenterY,
                            fillPaint);

                } else {
                    fillPaint.setColor(macdNegativeCol);
                    canvas.drawRect((float) (viewKData.getLeftX() + dp2px(0.5f)),
                            deputyCenterY,
                            (float) viewKData.getRightX() - dp2px(0.5f),
                            (float) (deputyCenterY + Math.abs(macd) * avgHeightMacd),
                            fillPaint);
                }
            }
        }
    }

    private void drawVolume(Canvas canvas){
        for (KData kData : viewDataList) {
            //volumeRect
            if (!isShowInstant){
                double openPrice = kData.getOpenPrice();
                double closedPrice = kData.getClosePrice();
                if (openPrice >= closedPrice) {
                    fillPaint.setColor(priceFallCol);
                } else {
                    fillPaint.setColor(priceIncreaseCol);
                }
            } else {
                fillPaint.setColor(0xff4db7f3);
            }
            canvas.drawRect((float) (kData.getLeftX() + dp2px(0.5f)),
                    (float) (volumeImgBot - kData.getVolume() * avgHeightPerVolume),
                    (float) kData.getRightX() - dp2px(0.5f),
                    volumeImgBot,
                    fillPaint);
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
                Pointer difPoint = new Pointer();
                if (viewDataList.get(i).getDif() > 0) {
                    difPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    difPoint.setY((float) (deputyCenterY - viewDataList.get(i).getDif() * avgHeightDif));
                } else {
                    difPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    difPoint.setY((float) (deputyCenterY + Math.abs(viewDataList.get(i).getDif() * avgHeightDif)));
                }
                deputyMa10PointList.add(difPoint);

                Pointer deaPoint = new Pointer();
                if (viewDataList.get(i).getDea() > 0) {
                    deaPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    deaPoint.setY((float) (deputyCenterY - viewDataList.get(i).getDea() * avgHeightDea));
                } else {
                    deaPoint.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    deaPoint.setY((float) (deputyCenterY + Math.abs(viewDataList.get(i).getDea() * avgHeightDea)));
                }
                deputyMa30PointList.add(deaPoint);

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

            } else if (isShowDeputy && deputyImgType == DEPUTY_IMG_RSI) {
                Pointer rs1Point = new Pointer();
                if (viewDataList.get(i).getRs1() >= 0) {
                    rs1Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    rs1Point.setY((float) (horizontalYList.get(5) - viewDataList.get(i).getRs1() * avgHeightRSI));
                    deputyMa5PointList.add(rs1Point);
                }

                Pointer rs2Point = new Pointer();
                if (viewDataList.get(i).getRs2() >= 0) {
                    rs2Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    rs2Point.setY((float) (horizontalYList.get(5) - viewDataList.get(i).getRs2() * avgHeightRSI));
                    deputyMa10PointList.add(rs2Point);
                }

                Pointer rs3Point = new Pointer();
                if (viewDataList.get(i).getRs3() >= 0) {
                    rs3Point.setX((float) (viewDataList.get(i).getLeftX() + avgPriceRectWidth / 2));
                    rs3Point.setY((float) (horizontalYList.get(5) - viewDataList.get(i).getRs3() * avgHeightRSI));
                    deputyMa30PointList.add(rs3Point);
                }
            }
        }

        drawVolumeBezierCurve(canvas);
        drawMainBezierCurve(canvas);
        if (isShowDeputy) {
            drawDeputyCurve(canvas);
        }
    }

    //主图 MA曲线
    private void drawMainBezierCurve(@NonNull Canvas canvas) {
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
    private void drawVolumeBezierCurve(@NonNull Canvas canvas) {
        QuotaUtil.setBezierPath(volumeMa5PointList, curvePath);
        resetStrokePaint(priceMa5Col, 0);
        canvas.drawPath(curvePath, strokePaint);

        QuotaUtil.setBezierPath(volumeMa10PointList, curvePath);
        resetStrokePaint(priceMa10Col, 0);
        canvas.drawPath(curvePath, strokePaint);
    }

    //副图 曲线
    private void drawDeputyCurve(@NonNull Canvas canvas) {
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
    private void getClickKData(float clickX) {
        if (isShowDetail) {
            detailRightDataList.clear();
            for (int i = 0; i < viewDataList.size(); i++) {
                if (viewDataList.get(i).getLeftX() <= clickX
                        && viewDataList.get(i).getRightX() >= clickX) {
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

                } else {
                    lastKData = null;
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
        double moveY;
        switch (crossHairMoveMode) {
            case CROSS_HAIR_MOVE_OPEN:
                moveY = lastKData.getOpenY();
                break;

            case CROSS_HAIR_MOVE_FREE:
                moveY = longPressMoveY;
                break;

            case CROSS_HAIR_MOVE_CLOSE:
            default:
                moveY = lastKData.getCloseY();
                break;
        }
        if (moveY < horizontalYList.get(0)) {
            moveY = horizontalYList.get(0);
        } else if (moveY > priceImgBot) {
            moveY = priceImgBot;
        }

        resetStrokePaint(crossHairCol, 0);
        canvas.drawLine(verticalXList.get(0),
                (float) moveY,
                verticalXList.get(verticalXList.size() - 1),
                (float) moveY,
                strokePaint);

        //底部标签
        RectF grayRectF = new RectF((float) (lastKData.getLeftX() + avgPriceRectWidth / 2 - dp2px(25)),
                bottomEnd - dp2px(20),
                (float) (lastKData.getLeftX() + avgPriceRectWidth / 2 + dp2px(25)),
                bottomEnd);
        fillPaint.setColor(crossHairBottomLabelCol);
        canvas.drawRoundRect(grayRectF, 4, 4, fillPaint);

        //底部标签text
        String moveTime = formatDate(lastKData.getTime());
        resetStrokePaint(crossHairBottomLabelTextCol, crossHairBottomLabelTextSize);
        canvas.drawText(moveTime,
                (float) (lastKData.getLeftX() + avgPriceRectWidth / 2 - strokePaint.measureText(moveTime) / 2),
                bottomEnd - dp2px(7),
                strokePaint);

        //右侧标签
        RectF blueRectF = new RectF(rightEnd - dp2px(38),
                (float) moveY - dp2px(7),
                rightEnd - dp2px(1),
                (float) moveY + dp2px(7));
        fillPaint.setColor(crossHairRightLabelCol);
        canvas.drawRoundRect(blueRectF, 4, 4, fillPaint);

        curvePath.reset();
        curvePath.moveTo(verticalXList.get(verticalXList.size() - 1), (float) moveY);
        curvePath.lineTo(rightEnd - dp2px(37), (float) moveY - dp2px(3));
        curvePath.lineTo(rightEnd - dp2px(37), (float) moveY + dp2px(3));
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

        String movePrice = formatKDataNum(topPrice
                - avgPricePerHeight * ((float) moveY - horizontalYList.get(0)));
        Rect textRect = new Rect();
        resetStrokePaint(crossHairRightLabelTextCol, crossHairRightLabelTextSize);
        strokePaint.getTextBounds(movePrice, 0, movePrice.length(), textRect);
        canvas.drawText(movePrice,
                rightEnd - dp2px(38) + (blueRectF.width() - textRect.width()) / 2,
                (float) moveY + dp2px(7) - (blueRectF.height() - textRect.height()) / 2,
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
                (float) mMaxPriceY + maxPriceRect.height() / 2f,
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
                (float) mMinPriceY + minPriceRect.height() / 2f,
                strokePaint);
    }

    private void drawDetailData(Canvas canvas) {
        if (lastKData == null || !isShowDetail) {
            return;
        }
        resetStrokePaint(detailTextCol, detailTextSize);
        strokePaint.getTextBounds(detailLeftTitleArr[0], 0, detailLeftTitleArr[0].length(), detailTextRect);

        if (lastKData.getLeftX() + avgPriceRectWidth / 2 <= getMeasuredWidth() / 2f) {
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
        if (lastKData == null) {
            return;
        }
        String ma5Str = STR_MA5 + setPrecision(lastKData.getPriceMa5(), 2);
        String ma10Str = STR_MA10 + setPrecision(lastKData.getPriceMa10(), 2);
        String ma30Str = STR_MA30 + setPrecision(lastKData.getPriceMa30(), 2);

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
        if (lastKData == null) {
            return;
        }
        //VOL
        String volStr = STR_VOL + setPrecision(lastKData.getVolume(), 2);
        Rect volRect = new Rect();
        resetStrokePaint(volumeTextCol, volumeTextSize);
        strokePaint.getTextBounds(volStr, 0, volStr.length(), volRect);
        canvas.drawText(volStr,
                verticalXList.get(0),
                priceImgBot + volRect.height() + dp2px(2),
                strokePaint);

        String ma5Str = STR_MA5 + setPrecision(lastKData.getVolumeMa5(), 2);
        Rect volMa5Rect = new Rect();
        resetStrokePaint(priceMa5Col, volumeTextSize);
        strokePaint.getTextBounds(ma5Str, 0, ma5Str.length(), volMa5Rect);
        canvas.drawText(ma5Str,
                verticalXList.get(0) + volRect.width() + dp2px(10),
                priceImgBot + volRect.height() + dp2px(2),
                strokePaint);

        String ma10Str = STR_MA10 + setPrecision(lastKData.getVolumeMa10(), 2);
        resetStrokePaint(priceMa10Col, volumeTextSize);
        canvas.drawText(ma10Str,
                verticalXList.get(0) + volMa5Rect.width() + volRect.width() + dp2px(10) * 2,
                priceImgBot + volRect.height() + dp2px(2),
                strokePaint);

        String titleStr = "";
        String firstStr = "";
        String secondStr = "";
        String thirdStr = "";
        if (isShowDeputy && deputyImgType == DEPUTY_IMG_MACD) {
            titleStr = STR_MACD_TITLE;
            firstStr = STR_MACD + setPrecision(lastKData.getMacd(), 2);
            secondStr = STR_DIF + setPrecision(lastKData.getDif(), 2);
            thirdStr = STR_DEA + setPrecision(lastKData.getDea(), 2);

        } else if (isShowDeputy && deputyImgType == DEPUTY_IMG_KDJ) {
            titleStr = STR_KDJ_TITLE;
            firstStr = STR_K + setPrecision(lastKData.getK(), 2);
            secondStr = STR_D + setPrecision(lastKData.getD(), 2);
            thirdStr = STR_J + setPrecision(lastKData.getJ(), 2);

        } else if (isShowDeputy && deputyImgType == DEPUTY_IMG_RSI) {
            titleStr = STR_RSI_TITLE;
            firstStr = STR_RS1 + setPrecision(lastKData.getRs1(), 2);
            secondStr = STR_RS2 + setPrecision(lastKData.getRs2(), 2);
            thirdStr = STR_RS3 + setPrecision(lastKData.getRs3(), 2);
        }

        Rect titleRect = new Rect();
        resetStrokePaint(volumeTextCol, volumeTextSize);
        strokePaint.getTextBounds(titleStr, 0, titleStr.length(), titleRect);
        canvas.drawText(titleStr,
                verticalXList.get(0),
                horizontalYList.get(4) + titleRect.height(),
                strokePaint);

        resetStrokePaint(priceMa5Col, volumeTextSize);
        canvas.drawText(firstStr,
                verticalXList.get(0) + titleRect.width() + dp2px(10),
                horizontalYList.get(4) + titleRect.height(),
                strokePaint);
        float firstWidth = strokePaint.measureText(firstStr);

        resetStrokePaint(priceMa10Col, volumeTextSize);
        canvas.drawText(secondStr,
                verticalXList.get(0) + titleRect.width() + dp2px(20) + firstWidth,
                horizontalYList.get(4) + titleRect.height(),
                strokePaint);
        float secondWidth = strokePaint.measureText(secondStr);

        resetStrokePaint(priceMa30Col, volumeTextSize);
        canvas.drawText(thirdStr,
                verticalXList.get(0) + titleRect.width() + dp2px(30) + firstWidth + secondWidth,
                horizontalYList.get(4) + titleRect.height(),
                strokePaint);
    }

    //横坐标
    private void drawAbscissa(Canvas canvas) {
        resetStrokePaint(abscissaTextCol, abscissaTextSize);
        for (int i = 0; i < verticalXList.size(); i++) {
            if (i == 0 && viewDataList.get(0).getLeftX() <= verticalXList.get(0) + avgPriceRectWidth / 2
                    && viewDataList.get(0).getRightX() > verticalXList.get(0)) {
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
                for (KData data : viewDataList) {
                    if (data.getLeftX() <= verticalXList.get(i) && data.getRightX() >= verticalXList.get(i)) {
                        String dateStr = formatDate(data.getTime());
                        canvas.drawText(dateStr,
                                verticalXList.get(i) - strokePaint.measureText(dateStr) / 2,
                                bottomEnd - dp2px(7),
                                strokePaint);
                        break;
                    }
                }
            }
        }
    }

    //纵坐标
    private void drawOrdinate(@NonNull Canvas canvas) {
        Rect rect = new Rect();
        resetStrokePaint(ordinateTextCol, ordinateTextSize);
        //最高价
        strokePaint.getTextBounds(formatKDataNum(topPrice), 0, formatKDataNum(topPrice).length(), rect);
        canvas.drawText(formatKDataNum(topPrice),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                horizontalYList.get(0) + rect.height() + dp2px(2),
                strokePaint);

        //最低价
        strokePaint.getTextBounds(formatKDataNum(botPrice), 0, formatKDataNum(botPrice).length(), rect);
        canvas.drawText(formatKDataNum(botPrice),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                priceImgBot - dp2px(2),
                strokePaint);

        if (!isShowDeputy) {
            double avgPrice = (topPrice - botPrice) / 4;
            for (int i = 0; i < 3; i++) {
                canvas.drawText(formatKDataNum(topPrice - avgPrice * (i + 1)),
                        verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                        horizontalYList.get(i + 1) + rect.height() / 2f,
                        strokePaint);
            }

        } else {
            double avgPrice = (topPrice - botPrice) / 3;
            for (int i = 0; i < 2; i++) {
                canvas.drawText(formatKDataNum(topPrice - avgPrice * (i + 1)),
                        verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                        horizontalYList.get(i + 1) + rect.height() / 2f,
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
                topDeputy = setPrecision(mMaxK, 2);
                centerDeputy = setPrecision(mMaxK / 2, 2);
                botDeputy = "0";

            } else if (deputyImgType == DEPUTY_IMG_RSI) {
                topDeputy = "100";
                centerDeputy = "50";
                botDeputy = "0";
            }

            canvas.drawText(topDeputy,
                    verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                    horizontalYList.get(horizontalYList.size() - 2) + rect.height() + dp2px(2),
                    strokePaint);

            canvas.drawText(centerDeputy,
                    verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                    horizontalYList.get(horizontalYList.size() - 1) - verticalSpace / 2 + rect.height() / 2f,
                    strokePaint);

            canvas.drawText(botDeputy,
                    verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                    horizontalYList.get(horizontalYList.size() - 1) - dp2px(2),
                    strokePaint);
        }

        //最高量
        strokePaint.getTextBounds(formatVolNum(maxVolume), 0, formatVolNum(maxVolume).length(), rect);
        canvas.drawText(formatVolNum(maxVolume),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                priceImgBot + rect.height() + dp2px(2),
                strokePaint);

        //最高量/2
        canvas.drawText(formatVolNum(maxVolume / 2),
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                volumeImgBot - verticalSpace / 2 + rect.height() / 2f,
                strokePaint);

        //数量 0
        canvas.drawText("0",
                verticalXList.get(verticalXList.size() - 1) + dp2px(4),
                volumeImgBot - dp2px(2),
                strokePaint);

    }

    //分时图
    private void drawInstant(Canvas canvas){
        if (canvas == null || curvePath == null || viewDataList == null || strokePaint == null){
            return;
        }
        curvePath.reset();
        instantPath.reset();
        float startX = (float) viewDataList.get(0).getCenterX();
        float startY = (float) viewDataList.get(0).getCloseY();
        curvePath.moveTo(startX, startY);
        instantPath.moveTo(startX, startY);
        int viewDataSize = viewDataList.size();
        for (int i = 1; i < viewDataSize; i++) {
            KData viewData = viewDataList.get(i);
            curvePath.lineTo((float) viewData.getCenterX(), (float) viewData.getCloseY());
            instantPath.lineTo((float) viewData.getCenterX(), (float) viewData.getCloseY());
            if (i == viewDataSize - 1){
                instantPath.lineTo(verticalXList.get(verticalXList.size() - 1), (float) viewData.getCloseY());
            }
        }
        resetStrokePaint(0xff1aa3f0, 0);
        canvas.drawPath(curvePath, strokePaint);

        instantPath.lineTo(verticalXList.get(verticalXList.size() - 1), horizontalYList.get(horizontalYList.size() - 2));
        instantPath.lineTo(startX, horizontalYList.get(horizontalYList.size() - 2));
        instantPath.close();
        LinearGradient gradient = new LinearGradient(0, (int)mMaxPriceY, 0,
                horizontalYList.get(horizontalYList.size() - 2), 0x801aa3f0, 0x0d1aa3f0, Shader.TileMode.CLAMP);
        instantFillPaint.setShader(gradient);
        canvas.drawPath(instantPath, instantFillPaint);
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

    /**
     * 设置小数位精度
     *
     * @param scale 保留几位小数
     */
    private String setPrecision(Double num, int scale) {
        BigDecimal bigDecimal = new BigDecimal(num);
        return bigDecimal.setScale(scale, BigDecimal.ROUND_DOWN).toPlainString();
    }

    /**
     * 按量级格式化价格
     */
    private String formatKDataNum(double num) {
        if (num < 1) {
            return setPrecision(num, 6);
        } else if (num < 10) {
            return setPrecision(num, 4);
        } else if (num < 100) {
            return setPrecision(num, 3);
        } else if (num < 10000) {
            return setPrecision(num, 2);
        } else if (num < 100000000) {
            return setPrecision(num / 10000, 2) + "万";
        } else {
            return setPrecision(num / 100000000, 2) + "亿";
        }
    }

    /**
     * 按量级格式化数量
     */
    private String formatVolNum(double num) {
        if (num < 10000) {
            return setPrecision(num, 2);
        } else if (num < 100000000) {
            return setPrecision(num / 10000, 2) + "万";
        } else {
            return setPrecision(num / 100000000, 2) + "亿";
        }
    }

    private void resetStrokePaint(int colorId, int textSize) {
        strokePaint.setColor(colorId);
        strokePaint.setTextSize(sp2px(textSize));
    }

}
