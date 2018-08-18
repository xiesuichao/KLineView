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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final KLineView mainKlv = findViewById(R.id.klv_main);
        Button deputyBtn = findViewById(R.id.btn_deputy);
        Button maBtn = findViewById(R.id.btn_ma);
        Button emaBtn = findViewById(R.id.btn_ema);
        Button bollBtn = findViewById(R.id.btn_boll);
        Button macdBtn = findViewById(R.id.btn_macd);
        Button kdjBtn = findViewById(R.id.btn_kdj);

        mainKlv.initKDataList(getKDataList(5));

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
            public void requestData(final boolean isRequestBefore) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isRequestBefore){
                                    mainKlv.addDataList(getKDataList(2));

                                }else {
                                    mainKlv.addDataList(getKDataList(2));
                                }
                            }
                        });
                    }
                }, 1000);
            }
        });


    }

    private List<KData> getKDataList(double num) {
        long start = System.currentTimeMillis();
        PrintUtil.log("start", start);

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
        PrintUtil.log("mainActivity getKDataList", end - start);
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
