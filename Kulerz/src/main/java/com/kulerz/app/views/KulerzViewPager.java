package com.kulerz.app.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class KulerzViewPager extends ViewPager
{

    private boolean isSwipeEnabled = true;

    public KulerzViewPager(Context context) {
        super(context);
    }

    public KulerzViewPager(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isSwipeEnabled && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isSwipeEnabled && super.onInterceptTouchEvent(ev);
    }

    public void setSwipeEnabled(boolean enabled) {
        isSwipeEnabled = enabled;
    }

    public boolean isSwipeEnabled() {
        return isSwipeEnabled;
    }

}
