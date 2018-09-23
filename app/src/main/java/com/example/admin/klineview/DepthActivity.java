package com.example.admin.klineview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.admin.klineview.depth.Depth;
import com.example.admin.klineview.depth.DepthView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 深度图
 * Created by xiesuichao on 2018/9/24.
 */

public class DepthActivity extends AppCompatActivity {


    private DepthView depthView;
    private Button depthResetBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_depth);

        initView();
        initData();
        setListener();

    }

    private void initView(){
        depthView = findViewById(R.id.dv_depth);
        depthResetBtn = findViewById(R.id.btn_depth_reset);
    }

    private void initData(){
        //添加购买数据
        depthView.setBuyDataList(getBuyDepthList());
        //添加出售数据
        depthView.setSellDataList(getSellDepthList());
    }

    private void setListener(){
        depthResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //重置深度数据
                depthView.resetAllData(getBuyDepthList(), getSellDepthList());
            }
        });
    }

    //模拟深度数据
    private List<Depth> getBuyDepthList(){
        List<Depth> depthList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            depthList.add(new Depth(100 - random.nextDouble() * 10,
                    random.nextInt(10) * random.nextInt(10) * random.nextInt(10) + random.nextDouble(), 0));
        }
        return depthList;
    }

    //模拟深度数据
    private List<Depth> getSellDepthList(){
        List<Depth> depthList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            depthList.add(new Depth(100 + random.nextDouble() * 10,
                    random.nextInt(10) * random.nextInt(10) * random.nextInt(10) + random.nextDouble(), 1));
        }
        return depthList;
    }

}
