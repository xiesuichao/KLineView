# KLineView
股票走势图K线控件

由于时间关系，只能在空余时间做，目前只做了MA,EMA,BOLL,MACD,KDJ 5个指标。并且没有扩展功能。       

支持实时刷新的单条数据更新。          
支持滑动时的分页加载更多数据。     
支持惯性滑动。         
支持多指触控缩放。       
支持长按拖动。         
支持横屏显示         
支持布局文件自定义颜色，字体大小属性


已对性能做优化，单次添加数据量1000条，总数据量几万条，滑动都很流畅，不会影响用户体验。

//@TODO         
1、增加扩展性
后续会改进。

邮箱：xsc314@163.com       
qq：181801034    
如有需要其他修改，请联系        

![image](https://github.com/xiesuichao/KLineView/raw/master/image/KLineUI.png)
![image](https://github.com/xiesuichao/KLineView/raw/master/image/a1.png)
![image](https://github.com/xiesuichao/KLineView/raw/master/image/a2.png)
        
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
