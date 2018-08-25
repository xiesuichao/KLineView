# KLineView
股票走势图K线控件
![image](https://github.com/xiesuichao/KLineView/raw/master/image/KLineUI.png)
![image](https://github.com/xiesuichao/KLineView/raw/master/image/a1.png)


//初始化控件加载数据
        mKLineView.initKDataList(getKDataList(5));

        deputyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //是否显示副图
                mKLineView.setDeputyPicShow(!mKLineView.getVicePicShow());
            }
        });

        maBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //主图展示MA
                mKLineView.setMainImgType(KLineView.MAIN_IMG_MA);
            }
        });

        emaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //主图展示EMA
                mKLineView.setMainImgType(KLineView.MAIN_IMG_EMA);
            }
        });

        bollBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //主图展示BOLL
                mKLineView.setMainImgType(KLineView.MAIN_IMG_BOLL);
            }
        });

        macdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //副图展示MACD
                mKLineView.setDeputyImgType(KLineView.DEPUTY_IMG_MACD);
            }
        });

        kdjBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //副图展示KDJ
                mKLineView.setDeputyImgType(KLineView.DEPUTY_IMG_KDJ);
            }
        });

        /**
         * 当控件显示数据属于总数据量的前三分之一时，会自动调用该接口，用于预加载数据，保证控件操作过程中的流畅性，
         * 虽然做了预加载，当总数据量较小时，也会出现用户滑到左边界了，但数据还未获取到，依然会有停顿。
         * 所以数据量越大，越不会出现停顿，也就越流畅
         */
        mKLineView.setOnRequestDataListListener(new KLineView.OnRequestDataListListener() {
            @Override
            public void requestData() {
                mHandler.postDelayed(getDataRunnable, 2000);
            }
        });
