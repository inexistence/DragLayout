package com.jb.draglayout;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;

public class DraggableWebView extends WebView {
    boolean allowDragTop = true; // 如果是true，则允许拖动至底部的下一页
    float downY = 0;
    boolean needConsumeTouch = true; // 是否需要承包touch事件，needConsumeTouch一旦被定性，则不会更改

    private int minTouchDy = 30;

    private GestureDetectorCompat gestureDetector;


    public DraggableWebView(Context arg0) {
        this(arg0, null);
    }

    public DraggableWebView(Context arg0, AttributeSet arg1) {
        this(arg0, arg1, 0);
    }

    public DraggableWebView(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        gestureDetector = new GestureDetectorCompat(arg0,
                new YScrollDetector());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean scrollY = gestureDetector.onTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downY = ev.getRawY();
            needConsumeTouch = true; // 默认情况下，listView内部的滚动优先，默认情况下由该listView去消费touch事件
            allowDragTop = isAtTop();
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (!needConsumeTouch) {
                // 在最顶端且向上拉了，则这个touch事件交给父类去处理
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            } else if (allowDragTop) {
                // needConsumeTouch尚未被定性，此处给其定性
                // 允许拖动到底部的下一页，而且又向上拖动了，就将touch事件交给父view
                if (ev.getRawY() - downY > minTouchDy && scrollY) {
                    // flag设置，由父类去消费
                    needConsumeTouch = false;
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
            }
        }

        // 通知父view是否要处理touch事件
        getParent().requestDisallowInterceptTouchEvent(needConsumeTouch);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断listView是否在顶部
     *
     * @return 是否在顶部
     */
    private boolean isAtTop() {
        return getScrollY() == 0;
    }

    class YScrollDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx,
                                float dy) {
            // 垂直滑动时dy>dx，才被认定是上下拖动
            return Math.abs(dy) > Math.abs(dx);
        }
    }
}
