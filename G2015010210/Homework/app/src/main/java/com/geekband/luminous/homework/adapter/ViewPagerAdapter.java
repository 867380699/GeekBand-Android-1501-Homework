package com.geekband.luminous.homework.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 一个ViewPagerAdapter
 * Created by hisashieki on 15/9/1.
 */
public class ViewPagerAdapter extends PagerAdapter {
    List<View> items;
    public ViewPagerAdapter(List<View> items){
        this.items = items;
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(items.get(position), 0);
        return items.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(items.get(position));
    }

}
