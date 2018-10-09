package com.example.admin.klineview;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.admin.klineview.depth.Depth;
import com.example.admin.klineview.depth.DepthView;
import com.example.admin.klineview.kline.KData;
import com.example.admin.klineview.kline.KLineView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Handler mHandler;
    private KLineView mKLineView;
    private Button deputyBtn;
    private Button maBtn;
    private Button emaBtn;
    private Button bollBtn;
    private Button macdBtn;
    private Button kdjBtn;
    private Runnable dataListAddRunnable;
    private Runnable singleDataAddRunnable;
    private Button depthJumpBtn;
    private Button kLineResetBtn;
    private Button rsiBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        setListener();

        //切换横屏适配测试
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dp2px(280));
            mKLineView.setLayoutParams(params);
        }else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dp2px(380));
            mKLineView.setLayoutParams(params);
        }
    }

    private void initView(){
        mKLineView = findViewById(R.id.klv_main);
        deputyBtn = findViewById(R.id.btn_deputy);
        maBtn = findViewById(R.id.btn_ma);
        emaBtn = findViewById(R.id.btn_ema);
        bollBtn = findViewById(R.id.btn_boll);
        macdBtn = findViewById(R.id.btn_macd);
        kdjBtn = findViewById(R.id.btn_kdj);
        rsiBtn = findViewById(R.id.btn_rsi);
        kLineResetBtn = findViewById(R.id.btn_kline_reset);
        depthJumpBtn = findViewById(R.id.btn_depth_activity);
    }

    private void initData(){
        //初始化控件加载数据
        mKLineView.initKDataList(getKDataList(10));

        mHandler = new Handler();
        dataListAddRunnable = new Runnable() {
            @Override
            public void run() {
                //分页加载时添加多条数据
                mKLineView.addPreDataList(getKDataList(10), true);
            }
        };

        singleDataAddRunnable = new Runnable() {
            @Override
            public void run() {
                //实时刷新时添加单条数据
                mKLineView.addSingleData(getKDataList(0.1).get(0));
                mHandler.postDelayed(this, 1000);
            }
        };
//        mHandler.postDelayed(singleDataAddRunnable, 2000);

    }

    private void setListener(){
        deputyBtn.setOnClickListener(this);
        maBtn.setOnClickListener(this);
        emaBtn.setOnClickListener(this);
        bollBtn.setOnClickListener(this);
        macdBtn.setOnClickListener(this);
        kdjBtn.setOnClickListener(this);
        rsiBtn.setOnClickListener(this);
        depthJumpBtn.setOnClickListener(this);
        kLineResetBtn.setOnClickListener(this);

        /**
         * 当控件显示数据属于总数据量的前三分之一时，会自动调用该接口，用于预加载数据，保证控件操作过程中的流畅性，
         * 虽然做了预加载，当总数据量较小时，也会出现用户滑到左边界了，但数据还未获取到，依然会有停顿。
         * 所以数据量越大，越不会出现停顿，也就越流畅
         */
        mKLineView.setOnRequestDataListListener(new KLineView.OnRequestDataListListener() {
            @Override
            public void requestData() {
                //延时3秒执行，模拟网络请求耗时
                mHandler.postDelayed(dataListAddRunnable, 3000);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_kline_reset:
                //重置数据，可用于分时加载
                mKLineView.resetDataList(getKDataList(0.1));
                break;

            case R.id.btn_deputy:
                //是否显示副图
                mKLineView.setDeputyPicShow(!mKLineView.getVicePicShow());
                break;

            case R.id.btn_ma:
                //主图展示MA
                mKLineView.setMainImgType(KLineView.MAIN_IMG_MA);
                break;

            case R.id.btn_ema:
                //主图展示EMA
                mKLineView.setMainImgType(KLineView.MAIN_IMG_EMA);
                break;

            case R.id.btn_boll:
                //主图展示BOLL
                mKLineView.setMainImgType(KLineView.MAIN_IMG_BOLL);
                break;

            case R.id.btn_macd:
                //副图展示MACD
                mKLineView.setDeputyImgType(KLineView.DEPUTY_IMG_MACD);
                break;

            case R.id.btn_kdj:
                //副图展示KDJ
                mKLineView.setDeputyImgType(KLineView.DEPUTY_IMG_KDJ);
                break;

            case R.id.btn_rsi:
                //副图展示RSI
                mKLineView.setDeputyImgType(KLineView.DEPUTY_IMG_RSI);
                break;

            case R.id.btn_depth_activity:
                //跳转到深度图页面
                startActivity(new Intent(getApplicationContext(), DepthActivity.class));
                break;
        }
    }

    //模拟K线数据
    private List<KData> getKDataList(double num) {
        long start = System.currentTimeMillis();

        Random random = new Random();
        List<KData> dataList = new ArrayList<>();
        double openPrice = 100;
        double closePrice;
        double maxPrice;
        double minPrice;
        double volume;

        for (int x = 0; x < num * 10; x++) {
            for (int i = 0; i < 12; i++) {
                start += 60 * 1000 * 5;
                closePrice = openPrice + getAddRandomDouble();
                maxPrice = closePrice + getAddRandomDouble();
                minPrice = openPrice - getSubRandomDouble();
                volume = random.nextInt(100) * 1000 + random.nextInt(10) * 10 + random.nextInt(10) + random.nextDouble();
                dataList.add(new KData(start, openPrice, closePrice, maxPrice, minPrice, volume));
                openPrice = closePrice;
            }

            for (int i = 0; i < 8; i++) {
                start += 60 * 1000 * 5;
                closePrice = openPrice - getSubRandomDouble();
                maxPrice = openPrice + getAddRandomDouble();
                minPrice = closePrice - getSubRandomDouble();
                volume = random.nextInt(100) * 1000 + random.nextInt(10) * 10 + random.nextInt(10) + random.nextDouble();
                dataList.add(new KData(start, openPrice, closePrice, maxPrice, minPrice, volume));
                openPrice = closePrice;
            }

        }
        long end = System.currentTimeMillis();
        return dataList;
    }

    private double getAddRandomDouble(){
        Random random = new Random();
        return random.nextInt(5) * 5 + random.nextDouble();
    }

    private double getSubRandomDouble(){
        Random random = new Random();
        return random.nextInt(5) * 5  - random.nextDouble();
    }

    private int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出页面时停止子线程并置空，便于回收，避免内存泄露
        mKLineView.cancelQuotaThread();
    }

}
