package com.example.admin.klineview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler;
    private KLineView mainKlv;
    private Button deputyBtn;
    private Button maBtn;
    private Button emaBtn;
    private Button bollBtn;
    private Button macdBtn;
    private Button kdjBtn;
    private Button msgBtn;
    private Runnable getDataRunnable;
    private Runnable sendRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initViewData();

        setListener();





    }

    private void initViewData(){
        mainKlv = findViewById(R.id.klv_main);
        deputyBtn = findViewById(R.id.btn_deputy);
        maBtn = findViewById(R.id.btn_ma);
        emaBtn = findViewById(R.id.btn_ema);
        bollBtn = findViewById(R.id.btn_boll);
        macdBtn = findViewById(R.id.btn_macd);
        kdjBtn = findViewById(R.id.btn_kdj);
        msgBtn = findViewById(R.id.btn_msg);

        mainKlv.initKDataList(getKDataList(5));

        mHandler = new Handler();
        getDataRunnable = new Runnable() {
            @Override
            public void run() {
                mainKlv.addDataList(getKDataList(5));
            }
        };

        sendRunnable = new Runnable() {
            @Override
            public void run() {
                mainKlv.addData(getKDataList(0.1).get(0));
                mHandler.postDelayed(this, 1000);
            }
        };

        mHandler.postDelayed(sendRunnable, 2000);


    }

    private void setListener(){
        deputyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainKlv.setDeputyPicShow(!mainKlv.getVicePicShow());
            }
        });

        maBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainKlv.setMainImgType(KLineView.MAIN_IMG_MA);
            }
        });

        emaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainKlv.setMainImgType(KLineView.MAIN_IMG_EMA);
            }
        });

        bollBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainKlv.setMainImgType(KLineView.MAIN_IMG_BOLL);
            }
        });

        macdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainKlv.setDeputyImgType(KLineView.DEPUTY_IMG_MACD);
            }
        });

        kdjBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainKlv.setDeputyImgType(KLineView.DEPUTY_IMG_KDJ);
            }
        });

        mainKlv.setOnRequestDataListListener(new KLineView.OnRequestDataListListener() {
            @Override
            public void requestData() {
                mHandler.postDelayed(getDataRunnable, 2000);
            }
        });
    }

    private List<KData> getKDataList(double num) {
        long start = System.currentTimeMillis();

        Random random = new Random();
        List<KData> dataList = new ArrayList<>();
        double openPrice = 6000;
        double closePrice;
        double maxPrice;
        double minPrice;
        double volume;

        for (int x = 0; x < num * 10; x++) {
            for (int i = 0; i < 8; i++) {
                closePrice = openPrice + getAddRandomDouble();
                maxPrice = closePrice + getAddRandomDouble();
                minPrice = openPrice - getSubRandomDouble();
                volume = random.nextInt(10) * 100 + random.nextInt(10) * 10 + random.nextInt(10) + random.nextDouble();
                dataList.add(new KData(start + i * 60 * 1000 * 5, openPrice, closePrice, maxPrice, minPrice, volume));
                openPrice = closePrice;
            }

            for (int i = 0; i < 12; i++) {
                closePrice = openPrice - getSubRandomDouble();
                maxPrice = openPrice + getAddRandomDouble();
                minPrice = closePrice - getSubRandomDouble();
                volume = random.nextInt(10) * 100 + random.nextInt(10) * 10 + random.nextInt(10) + random.nextDouble();
                dataList.add(new KData(start + i * 60 * 1000 * 5, openPrice, closePrice, maxPrice, minPrice, volume));
                openPrice = closePrice;
            }

        }
        long end = System.currentTimeMillis();
//        PrintUtil.log("mainActivity getKDataList", end - start);
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

}
