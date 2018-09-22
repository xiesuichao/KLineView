package com.example.admin.klineview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by xiesuichao on 2018/9/20.
 */

public class CustomNestedScrollView extends NestedScrollView {

    private float downX;
    private float downY;
    private boolean isHorizontalMove = false;
    private boolean isVerticalMove = false;

    public CustomNestedScrollView(@NonNull Context context) {
        super(context);
    }

    public CustomNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN || ev.getPointerCount() > 1){
            return false;
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downX = ev.getX();
            downY = ev.getY();

        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float diffMoveX = Math.abs(ev.getX() - downX);
            float diffMoveY = Math.abs(ev.getY() - downY);

            if ((isVerticalMove || diffMoveY > diffMoveX + 5 ) && !isHorizontalMove) {
                isVerticalMove = true;
                return true;

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

}
