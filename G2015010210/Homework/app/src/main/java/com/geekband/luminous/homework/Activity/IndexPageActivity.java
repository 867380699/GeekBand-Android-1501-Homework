package com.geekband.luminous.homework.Activity;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.geekband.luminous.homework.R;
import com.geekband.luminous.homework.adapter.ViewPagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by hisashieki on 15/9/1.
 */
public class IndexPageActivity extends BaseActivity {
    ViewPager vpMain;
    CirclePageIndicator vpIndicator;
    int mState;
    List<View> views= new ArrayList<>();
    boolean isStart;
    @Override
    public void initView() {
        ViewPager.OnPageChangeListener vpListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.e(TAG, "onPageScrolled page:" + position + "offset:" + positionOffset + "positionOffsetPixels:" + positionOffsetPixels);
                if (position ==views.size()-1 && mState == ViewPager.SCROLL_STATE_DRAGGING && positionOffset==0 &&!isStart){
                    isStart=true;
                    Intent i = new Intent(context,HorizontalListActivity.class);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.w(TAG, "onPageScrollStateChanged: state" + state);
                mState = state;
            }
        };
        vpMain = (ViewPager) findViewById(R.id.vp_index_main);

        int[] picIds = {R.drawable.p1,R.drawable.p2,R.drawable.p3,R.drawable.p4,R.drawable.p5};
        for (int i=0;i<picIds.length;i++){
            ImageView iv = (ImageView) View.inflate(this,R.layout.item_single_picture,null);
            iv.setImageResource(picIds[i]);
            views.add(iv);
        }
        vpMain.setAdapter(new ViewPagerAdapter(views));
        vpMain.addOnPageChangeListener(vpListener);
        vpIndicator = (CirclePageIndicator) findViewById(R.id.vpi_index_main);
        //vpIndicator.setOnPageChangeListener(vpListener);
        vpIndicator.setViewPager(vpMain);

    }

    @Override
    public int getMainContentViewId() {
        return R.layout.activity_index_page;
    }
}
