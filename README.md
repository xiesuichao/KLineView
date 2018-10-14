# KLineView
股票走势图K线控件

主图指标：MA, EMA, BOLL      
副图指标：MACD, KDJ, RSI     
如需增加其他指标，请联系      
根目录下有个apk文件夹，内有最新的测试包，可以先安装看效果      
新增深度图控件，如下图所示，详情见demo   

支持实时刷新的单条数据更新。          
支持滑动时的分页加载更多数据。     
支持惯性滑动。         
支持多指触控缩放。       
支持长按拖动。         
支持横屏显示         
支持xml布局自定义颜色，字体大小属性

已对性能做优化，总数据量十万条以上对用户体验没有影响。   
首次加载5000条数据，页面初始化到加载完成，总共耗时400+ms，不超过0.5秒。         
分页加载5000条数据时，如果正在滑动过程中，添加数据的那一瞬间会稍微有一下卡顿，影响不大。        
经测试，800块的华为荣耀6A 每次添加4000条以下数据不会有卡顿，很流畅。         
建议每次添加数据在2000条左右。       
已对滑动事件冲突做处理，可上下滑动的父类（ScrllView、NestedScrollView等）无需再考虑滑动冲突       

//TODO         
1、性能与内存的继续优化。


邮箱：xsc314@163.com       
qq：181801034    
如有需要其他修改，请联系        

![image](https://github.com/xiesuichao/KLineView/raw/master/image/KLineUI.png)
![image](https://github.com/xiesuichao/KLineView/raw/master/image/a5.png)
![image](https://github.com/xiesuichao/KLineView/raw/master/image/a2.png)
![image](https://github.com/xiesuichao/KLineView/raw/master/image/a3.png)

1.K线控件:
      
    //初始化控件加载数据（仅作初始化用，数据重置请调用resetDataList）
    mKLineView.initKDataList(getKDataList(5));

    //设置十字线移动模式，默认为0：固定指向收盘价
    mKLineView.setCrossHairMoveMode(KLineView.CROSS_HAIR_MOVE_FREE);
                
    //分页加载时添加多条数据
    mKLineView.addDataList(getKDataList(5));
                
    //实时刷新时添加单条数据
    mKLineView.addData(getKDataList(0.1).get(0));

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_kline_reset:
                //重置数据，可用于分时加载，在做分时功能重新加载数据的时候，
                //请务必调用该方法
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

    /**
     * 当控件显示数据属于总数据量的前三分之一时，会自动调用该接口，用于预加载数据，保证控件操作过程中的流畅性，
     * 虽然做了预加载，当总数据量较小时，也会出现用户滑到左边界了，但数据还未获取到，依然会有停顿。
     * 所以数据量越大，越不会出现停顿，也就越流畅
     */
    mKLineView.setOnRequestDataListListener(new KLineView.OnRequestDataListListener() {
        @Override
        public void requestData() {
            //请求数据
        }
    });


2.深度图控件:

    //添加购买数据
    depthView.setBuyDataList(getBuyDepthList());

    //添加出售数据
    depthView.setSellDataList(getSellDepthList());

    //重置深度数据
    depthView.resetAllData(getBuyDepthList(), getSellDepthList());

    //设置横坐标中间值
    depthView.setAbscissaCenterPrice(10.265);

    //设置数据详情的价钱说明
    depthView.setDetailPriceTitle("价格(BTC)：");

    //设置数据详情的数量说明
    depthView.setDetailVolumeTitle("累积交易量：");

    //设置横坐标价钱小数位精度
    depthView.setPricePrecision(4);

    //是否显示竖线
    depthView.setShowDetailLine(true);

    //手指单击松开后，数据是否继续显示
    depthView.setShowDetailSingleClick(true);

    //手指长按松开后，数据是否继续显示
    depthView.setShowDetailLongPress(true);

