package com.kulerz.app.adapters;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.kulerz.app.KulerzActivity;
import java.util.ArrayList;

public class KulerzFragmentPagerAdapter extends FragmentPagerAdapter
    implements ViewPager.OnPageChangeListener, ActionBar.TabListener
{

    private KulerzActivity activity;
    private ViewPager pager;
    private final ArrayList<TabInfo> tabs = new ArrayList<TabInfo>();
    private ActionBar actionBar;

    private static class TabInfo {
        private final Class<?> clss;
        private final Bundle args;

        TabInfo(Class<?> clss, Bundle args) {
            this.clss = clss;
            this.args = args;
        }
    }

    public KulerzFragmentPagerAdapter(KulerzActivity activity, ViewPager pager) {
        super(activity.getSupportFragmentManager());
        this.activity = activity;
        this.pager = pager;
        this.pager.setOnPageChangeListener(this);
        this.pager.setAdapter(this);
        this.actionBar = activity.getActionBar();
    }

    public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
        TabInfo tabInfo = new TabInfo(clss, args);
        tab.setTag(tabInfo);
        tab.setTabListener(this);
        tabs.add(tabInfo);
        actionBar.addTab(tab);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int i) {
        TabInfo info = tabs.get(i);
        return Fragment.instantiate(activity, info.clss.getName(), info.args);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        actionBar.setSelectedNavigationItem(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Object tag = tab.getTag();
        for(int i = 0; i < tabs.size(); i++) {
            if(tabs.get(i).equals(tag)) {
                pager.setCurrentItem(i);
            }
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public Fragment findFragmentByPosition(int position) {
        String tag = "android:switcher:" + pager.getId() + ":" + position;
        return activity.getSupportFragmentManager().findFragmentByTag(tag);
    }

}
