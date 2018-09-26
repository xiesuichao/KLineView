# KLineView
股票走势图K线控件

由于时间关系，目前只做了MA,EMA,BOLL,MACD,KDJ 5个指标。       
根目录下有个apk文件夹，内有最新的测试包，可以先安装看效果
新增深度图控件，如下图所示，详情见demo

支持实时刷新的单条数据更新。          
支持滑动时的分页加载更多数据。     
支持惯性滑动。         
支持多指触控缩放。       
支持长按拖动。         
支持横屏显示         
支持布局文件自定义颜色，字体大小属性

已对性能做优化，总数据量十万条以上对用户体验没有影响。
首次加载5000条数据，页面初始化到加载完成，总共耗时400+ms，不超过0.5秒。
分页加载5000条数据时，如果正在滑动过程中，添加数据的那一瞬间会稍微有一下卡顿，影响不大。
经测试，800块的华为荣耀6A 每次添加4000条以下数据不会有卡顿，很流畅。
建议每次添加数据在2000条左右。

//TODO         
1、性能与内存的继续优化。


邮箱：xsc314@163.com       
qq：181801034    
如有需要其他修改，请联系        

![image](https://github.com/xiesuichao/KLineView/raw/master/image/KLineUI.png)
![image](https://github.com/xiesuichao/KLineView/raw/master/image/a1.png)
![image](https://github.com/xiesuichao/KLineView/raw/master/image/a2.png)
![image](https://github.com/xiesuichao/KLineView/raw/master/image/a3.png)

1.K线控件:
      
    //初始化控件加载数据
    mKLineView.initKDataList(getKDataList(5));
                
    //分页加载时添加多条数据
    mKLineView.addDataList(getKDataList(5));
                
    //实时刷新时添加单条数据
    mKLineView.addData(getKDataList(0.1).get(0));

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


    /**
    * 如果外层需要嵌套上下滑动的view，包括ScrollView，ListView，RecyclerView等，
    * 复写onInterceptTouchEvent进行点击事件拦截处理
    */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //如果是双指触控，不拦截
        if (ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN || ev.getPointerCount() > 1){
            return false;
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downX = ev.getX();
            downY = ev.getY();

        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float diffMoveX = Math.abs(ev.getX() - downX);
            float diffMoveY = Math.abs(ev.getY() - downY);

            //如果竖直滑动间距大于水平滑动间距 + 5，进行拦截
            if ((isVerticalMove || diffMoveY > diffMoveX + 5 ) && !isHorizontalMove) {
                isVerticalMove = true;
                return true;

            //如果水平间距大于竖直滑动间距 + 5，不拦截
            } else if ((isHorizontalMove || diffMoveX > diffMoveY + 5 ) && !isVerticalMove) {
                isHorizontalMove = true;
                return false;
            }

        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            isVerticalMove = false;
            isHorizontalMove = false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP){
            isVerticalMove = false;
            isHorizontalMove = false;
        }
        return super.onTouchEvent(ev);
    }

2.深度图控件:

    //添加购买数据
    depthView.setBuyDataList(getBuyDepthList());
    //添加出售数据
    depthView.setSellDataList(getSellDepthList());
    //重置深度数据
    depthView.resetAllData(getBuyDepthList(), getSellDepthList());


