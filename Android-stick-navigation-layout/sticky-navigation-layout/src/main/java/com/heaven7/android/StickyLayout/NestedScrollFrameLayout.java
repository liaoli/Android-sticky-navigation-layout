package com.heaven7.android.StickyLayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.heaven7.android.scroll.IScrollHelper;
import com.heaven7.android.scroll.NestedScrollFactory;
import com.heaven7.android.scroll.NestedScrollHelper;

/**
 * this is a a child of FrameLayout, but can nested as {@link NestedScrollingChild} or {@link NestedScrollingParent}.
 * it can scroll in vertical.
 * Created by heaven7 on 2016/11/14.
 */
public class NestedScrollFrameLayout extends FrameLayout implements NestedScrollingChild, NestedScrollingParent {

    private static final String TAG = NestedScrollFrameLayout.class.getSimpleName();

    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private NestedScrollHelper mNestedHelper;

    private int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    private float mMaxYPercent = 1f;

    public NestedScrollFrameLayout(Context context) {
        this(context, null);
    }

    public NestedScrollFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NestedScrollFrameLayout);
        mMaxYPercent = a.getFloat(R.styleable.NestedScrollFrameLayout_nsfl_max_y_percent, 1f);
        a.recycle();

        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mNestedHelper = NestedScrollFactory.create(this, new NestedScrollHelper.NestedScrollCallback() {
            @Override
            public boolean canScrollHorizontally(View target) {
                return false;
            }
            @Override
            public boolean canScrollVertically(View target) {
                return true;
            }
            @Override
            public int getMaximumYScrollDistance(View target) {
                return (int) (target.getHeight() * mMaxYPercent);
            }
        });
        setNestedScrollingEnabled(true);
    }

    /**
     * set the max y percent, default is 1. it is also can assign in xml config.
     * @param maxYPercent the max y percent
     */
    public void setMaximumYPercent(float maxYPercent){
         this.mMaxYPercent = maxYPercent;
    }

    /**
     * get the max y percent , it is used in scroll .
     * @return  the max y percent
     */
    public float getMaximumYPercent(){
         return this.mMaxYPercent ;
    }

    /**
     * Return the current scrolling state of the RecyclerView.
     *
     * @return {@link com.heaven7.android.scroll.IScrollHelper#SCROLL_STATE_IDLE}, {@link com.heaven7.android.scroll.IScrollHelper#SCROLL_STATE_DRAGGING} or
     * {@link com.heaven7.android.scroll.IScrollHelper#SCROLL_STATE_SETTLING}
     */
    public int getScrollState() {
        return mNestedHelper.getScrollState();
    }

    /**
     * add a scroll change listener.
     * @param l the OnScrollChangeListener
     */
    public void addOnScrollChangeListener(IScrollHelper.OnScrollChangeListener l){
        mNestedHelper.addOnScrollChangeListener(l);
    }

    /**
     * remove a scroll change listener.
     * @param l the OnScrollChangeListener
     */
    public void removeOnScrollChangeListener(IScrollHelper.OnScrollChangeListener l){
        mNestedHelper.removeOnScrollChangeListener(l);
    }

    /**
     * judge if has the target OnScrollChangeListener.
     * @param l the OnScrollChangeListener
     * @return true if has the target OnScrollChangeListener
     */
    public boolean hasOnScrollChangeListener(IScrollHelper.OnScrollChangeListener l){
        return mNestedHelper.hasOnScrollChangeListener(l);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!isNestedScrollingEnabled()){
            return super.onInterceptTouchEvent(ev);
        }
        return mNestedHelper.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isNestedScrollingEnabled()){
            return super.onTouchEvent(event);
        }
        return mNestedHelper.onTouchEvent(event);
    }
    @Override
    public void computeScroll() {
        mNestedHelper.computeScroll();
    }

    // =========================== nested parent =======================================
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        // Dispatch up to the nested parent
        startNestedScroll(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        mNestedHelper.nestedScroll(dx, dy, consumed, true);

        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {

        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);

        // Dispatch up our nested parent
        stopNestedScroll();
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }
    //========================  NestedScrollingParent end ========================

    //========================  NestedScrollingChild begin ========================
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
                                        int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
    //======================== end NestedScrollingChild =====================
}
