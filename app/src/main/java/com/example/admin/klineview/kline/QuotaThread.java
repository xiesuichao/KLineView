package com.example.admin.klineview.kline;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * 子线程计算五项数据
 * Created by xiesuichao on 2018/8/18.
 */

public class QuotaThread extends HandlerThread implements Handler.Callback {

    public static final int HANDLER_QUOTA_LIST = 100;
    public static final int HANDLER_QUOTA_SINGLE = 101;
    private Handler uiHandler;
    private Handler workHandler;

    public QuotaThread(String name, int priority) {
        super(name, priority);
    }

    public void setUIHandler(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }

    public void quotaListCalculate(List<KData> dataList) {
        if (workHandler == null) {
            return;
        }
        Message message = Message.obtain(null, HANDLER_QUOTA_LIST);
        message.obj = dataList;
        workHandler.sendMessage(message);
    }

    public void quotaSingleCalculate(List<KData> dataList){
        if (workHandler == null) {
            return;
        }
        Message message = Message.obtain(null, HANDLER_QUOTA_SINGLE);
        message.obj = dataList;
        workHandler.sendMessage(message);
    }

    private void calculateKDataQuota(List<KData> dataList, boolean isEndData) {
        QuotaUtil.initEma(dataList, isEndData);
        QuotaUtil.initBoll(dataList, isEndData);
        QuotaUtil.initMACD(dataList, isEndData);
        QuotaUtil.initKDJ(dataList, isEndData);
        QuotaUtil.initRSI(dataList, isEndData);
        QuotaUtil.initMa(dataList, isEndData);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        this.workHandler = new Handler(getLooper(), this);
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (msg.what == HANDLER_QUOTA_LIST) {
            handleData(msg, HANDLER_QUOTA_LIST, false);
        }else if (msg.what == HANDLER_QUOTA_SINGLE){
            handleData(msg, HANDLER_QUOTA_SINGLE, true);
        }
        return true;
    }

    private void handleData(Message msg, int whatId, boolean isEndData){
        if (msg == null || uiHandler == null) {
            return;
        }
        try {
            List<KData> dataList = (List<KData>) msg.obj;
            calculateKDataQuota(dataList, isEndData);
            Message message = Message.obtain(null, whatId);
            uiHandler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
