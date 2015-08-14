package com.geekband.luminous.homework.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

/**
 * Created by hisashieki on 15/8/14.
 */
public class MyListViewX extends AdapterView {

    /** The adapter with all the data */
    private Adapter mAdapter;

    public MyListViewX(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mAdapter=adapter;
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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
}