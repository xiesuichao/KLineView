package com.example.admin.klineview;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiesuichao on 2018/8/18.
 */

public class QuotaThread extends HandlerThread implements Handler.Callback{

    public static final int MSG_QUOTA_CALCULATE = 100;
    public static final String MSG_KEY_QUOTA_BUNDLE = "MSG_KEY_QUOTA_BUNDLE";
    private Handler uiHandler;
    private Handler workHandler;
    private ArrayList receiveArrayList;


    public QuotaThread(String name) {
        super(name);
    }

    public QuotaThread(String name, int priority) {
        super(name, priority);
    }


    public void setUIHandler(Handler uiHandler){
        this.uiHandler = uiHandler;
    }

    public Handler getWorkHandler(){
        return this.workHandler;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        this.workHandler = new Handler(getLooper(), this);
    }


    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MSG_QUOTA_CALCULATE){
            receiveArrayList = msg.getData().getParcelableArrayList(MSG_KEY_QUOTA_BUNDLE);
            List<KData> dataList = (List<KData>)receiveArrayList.get(0);
            try {
                Thread.sleep(2000);
                initKDataQuota(dataList);

            }catch (Exception e){
                e.printStackTrace();
            }
            Message message = Message.obtain(null, MSG_QUOTA_CALCULATE);
            uiHandler.sendMessage(message);
        }
        return true;
    }

    public void quotaCalculate(List<KData> dataList){
        Bundle bundle = new Bundle();
        ArrayList sendArrayList = new ArrayList();
        sendArrayList.add(dataList);
        bundle.putStringArrayList(MSG_KEY_QUOTA_BUNDLE, sendArrayList);
        Message message = Message.obtain(null, MSG_QUOTA_CALCULATE);
        message.setData(bundle);
        workHandler.sendMessage(message);
    }

    private void initKDataQuota(List<KData> dataList) {
        QuotaUtil.initEma(dataList);
        QuotaUtil.initBoll(dataList);
        QuotaUtil.initMACD(dataList);
        QuotaUtil.initKDJ(dataList);
        QuotaUtil.initMa(dataList);
    }

}
