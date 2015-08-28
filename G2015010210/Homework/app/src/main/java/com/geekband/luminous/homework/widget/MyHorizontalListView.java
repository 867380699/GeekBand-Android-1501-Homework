package com.geekband.luminous.homework.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;

import java.util.LinkedList;

/**
 * custom twice
 * Created by luminous on 15/8/15.
 */
public class MyHorizontalListView extends AdapterView {
    public static final String TAG = "MHV";
    /** 滚动的状态 */
    public static final int TOUCH_STATE_SCROLLING = 2;
    /** 没有点击的状态 */
    private static final int TOUCH_STATE_RESTING = 0;
    /** 点击的状态 */
    private static final int TOUCH_STATE_CLICK = 1;
    private static final int VELOCITY_TOLERANCE = 10;
    /** 用于判定是否为scroll的threshold */
    static int TOUCH_THRESHOLD = 10;
    Adapter mAdapter;
    /** 列表的最左边 */
    int mListLeft;
    /** 手指点下去的X */
    int mTouchDownX;
    /** 手指点下去的Y */
    int mTouchDownY;
    /** 测量时的List的右边缘,超出屏幕则不再添加View */
    int rightEdge;
    /** 测量时得List得左边缘,超出屏幕则不在添加View */
    int leftEdge;
    /** 最后一个加入的View的position */
    int myLastAddPosition = -1;
    /** layout中的第一个View的position */
    int myFirstViewPosition;
    LinkedList<View> cacheViews = new LinkedList<>();
    /** 按键状态 */
    private int mTouchState = TOUCH_STATE_RESTING;
    private Runnable longClickHandler;
    VelocityTracker velocityTracker;
    private Runnable scrollingHandler;
    private Dynamics mDynamics = new SimpleDynamics(0.9f,0.5f);
    public MyHorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setClickable(true);
        TOUCH_THRESHOLD = ViewConfiguration.get(context).getScaledPagingTouchSlop();
    }

    @Override
    public Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        this.mAdapter = adapter;
    }

    @Override
    public View getSelectedView() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setSelection(int position) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        Bitmap bitMap = child.getDrawingCache();
        if (bitMap == null) {
            return super.drawChild(canvas, child, drawingTime);
        }


        int left = child.getLeft();
        int top = child.getTop();

        // get offset to center
        int centerX = child.getWidth() / 2;
        int centerY = child.getHeight() / 2;

        // get absolute center of child
        float pivotX = left + centerX;
        float pivotY = top + centerY;

        // calculate distance from center
        float centerScreen = getWidth() / 2;
        float distFromCenter = (pivotX - centerScreen) / centerScreen;

        // calculate scale and rotation
        float scale = (float) (1 - 0.5 * (1 - Math.cos(distFromCenter)));
        float rotation = 30 * distFromCenter;

        canvas.save();
        //canvas.rotate(rotation, pivotX, pivotY);
        canvas.scale(scale, scale, pivotX, pivotY);
        canvas.translate(0, (1 - scale) * child.getHeight());
        super.drawChild(canvas, child, drawingTime);
        canvas.restore();
        return false;
    }

    //TODO:reconstruct the method
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mAdapter == null || mAdapter.getCount() <= 0) {
            return;
        }
        //新添加的View没有被layout,所以left,right的值都是错误的,不能在add之后调用remove
        //remove left
        while (getChildCount() > 0 && getChildAt(0).getRight() < 0) {
            View firstView = getChildAt(0);
            cacheViews.addLast(firstView);
            mListLeft += firstView.getWidth();
            leftEdge += firstView.getMeasuredWidth();
            removeViewInLayout(firstView);
            myFirstViewPosition++;
            //Log.w(TAG, "onLayout after remove left views count:" + getChildCount());
        }
        //remove right
        while (getChildCount() > 0 && getChildAt(getChildCount() - 1).getLeft() > getWidth()) {
            View lastView = getChildAt(getChildCount() - 1);
            cacheViews.addLast(lastView);
            rightEdge -= lastView.getMeasuredWidth();
            removeViewInLayout(lastView);
            myLastAddPosition--;
            //Log.w(TAG, "onLayout after remove right views count:" + getChildCount());
        }
        //add in right
        while (rightEdge < getWidth() && myLastAddPosition + 1 < mAdapter.getCount()) {
            View v = mAdapter.getView(myLastAddPosition + 1, getCachedView(), this);
            v.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.EXACTLY | getHeight());
            rightEdge += v.getMeasuredWidth();
            LayoutParams myLayoutParams = v.getLayoutParams();
            if (myLayoutParams == null) {
                myLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            }
            addViewInLayout(v, -1, myLayoutParams);
            myLastAddPosition++;
        }
        //add in left
        while (leftEdge >= 0 && myFirstViewPosition > 0) {
            View v = mAdapter.getView(myFirstViewPosition - 1, getCachedView(), this);
            //TODO:当View是回收来的时候,View是否需要measure
            v.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.EXACTLY | getHeight());
            leftEdge -= v.getMeasuredWidth();
            mListLeft -= v.getMeasuredWidth();
            LayoutParams myLayoutParams = v.getLayoutParams();
            if (myLayoutParams == null) {
                myLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            }
            addViewInLayout(v, 0, myLayoutParams);
            myFirstViewPosition--;
            //Log.d(TAG, "onLayout add to Left");
        }

        //Layout
        int mLeft = 0;
        //Log.e(TAG, "onLayout last added position:" + myLastAddPosition);
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            int cRight = childView.getMeasuredWidth();
            childView.layout(mLeft + mListLeft, 0, mLeft + cRight + mListLeft, getHeight());
            childView.setDrawingCacheEnabled(true);
            mLeft += cRight;
            //Log.e(TAG, "onLayout mLeft" + mLeft);
            VelocityTracker v = VelocityTracker.obtain();
        }
    }

    /**
     * 如果在这里不返回true的话,所有的事件都会被Item的listener拦截,导致无法移动List
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = (int) event.getX();
                mTouchDownY = (int) event.getY();
                mTouchState = TOUCH_STATE_CLICK;
                startLongClickCheck(mTouchDownX, mTouchDownY);
                velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchState == TOUCH_STATE_CLICK) {
                    if (Math.abs(event.getX() - mTouchDownX) > TOUCH_THRESHOLD || Math.abs(event.getY() - mTouchDownY) > TOUCH_THRESHOLD) {
                        mTouchDownX = (int) event.getX();
                        mTouchDownY = (int) event.getY();
                        scrollingList(event);
                        removeCallbacks(longClickHandler);
                    }
                } else if (mTouchState == TOUCH_STATE_SCROLLING) {
                    velocityTracker.addMovement(event);
                    scrollingList(event);
                }
                break;
            case MotionEvent.ACTION_UP:

                if (mTouchState == TOUCH_STATE_CLICK) {
                    clickChild((int) event.getX(), (int) event.getY());
                    endTouch();
                    break;
                } else if (mTouchState == TOUCH_STATE_SCROLLING) {
                    velocityTracker.computeCurrentVelocity(1000);
                    final float velocity = velocityTracker.getXVelocity();
                    Log.e(TAG, "onTouchEvent " + velocity);
                    //TODO
                    mDynamics.setState(event.getX(),velocity,AnimationUtils.currentAnimationTimeMillis());
                    if (scrollingHandler == null) {

                        scrollingHandler = new Runnable() {
                            @Override
                            public void run() {
                                mDynamics.update(AnimationUtils.currentAnimationTimeMillis());
                                scrollingList((int)mDynamics.getPosition(), (int) event.getY());
                                if (!mDynamics.isAtRest(VELOCITY_TOLERANCE, 10)) {
                                    // the list is not at rest, so schedule a new frame
                                    postDelayed(this, 16);
                                }
                            }
                        };
                    }
                    postDelayed(scrollingHandler,16);
                }

                endTouch();
                break;
            default:
                endTouch();
                break;
        }
        return super.onTouchEvent(event);

    }

    private void endTouch() {
        removeCallbacks(longClickHandler);
        mTouchState = TOUCH_STATE_RESTING;
        velocityTracker.recycle();
        velocityTracker = null;
    }

    private void scrollingList(MotionEvent event) {
        mListLeft += (int) (event.getX() - mTouchDownX);
        rightEdge += (int) (event.getX() - mTouchDownX);
        leftEdge += (int) (event.getX() - mTouchDownX);
//                if (mListLeft > 0) {
//                    rightEdge -= mListLeft;
//                    mListLeft = 0;
//                }
        mTouchDownX = (int) event.getX();
        mTouchDownY = (int) event.getY();
        mTouchState = TOUCH_STATE_SCROLLING;
        requestLayout();
    }
    private void scrollingList(int x,int y){
        mListLeft += x - mTouchDownX;
        rightEdge += x - mTouchDownX;
        leftEdge += x - mTouchDownX;
        mTouchDownX = x;
        mTouchDownY = y;
        mTouchState = TOUCH_STATE_SCROLLING;
        requestLayout();
    }
    private void clickChild(int x, int y) {
        Rect rect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.getHitRect(rect);
            if (rect.contains(x, y)) {
                performItemClick(v, myFirstViewPosition + i, mAdapter.getItemId(myFirstViewPosition + i));
            }
        }
    }

    public void startLongClickCheck(final int x, final int y) {
        longClickHandler = new Runnable() {
            @Override
            public void run() {
                longClickChild(x, y);
            }
        };
        postDelayed(longClickHandler, ViewConfiguration.getLongPressTimeout());
    }

    private void longClickChild(int x, int y) {
        Rect rect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.getHitRect(rect);
            if (rect.contains(x, y)) {
                OnItemLongClickListener listener = getOnItemLongClickListener();
                if (listener != null) {
                    listener.onItemLongClick(this, v, myFirstViewPosition + i, mAdapter.getItemId(myFirstViewPosition + i));
                    mTouchState = TOUCH_STATE_RESTING;
                }
            }
        }
    }

    /**
     * 获得缓存的View,作为ConvertView
     *
     * @return
     */
    private View getCachedView() {
        if (cacheViews.size() > 0) {
            return cacheViews.removeFirst();
        }
        return null;
    }
}
