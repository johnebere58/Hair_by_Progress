package com.hairbyprogress.adapters;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.hairbyprogress.fragments.FragmentHome;
import com.hairbyprogress.fragments.FragmentMarket;
import com.hairbyprogress.fragments.FragmentOrder;
import com.hairbyprogress.fragments.FragmentProfile;


/**
 * Created by John Ebere on 8/8/2016.
 */
public class PageAdapter extends FragmentPagerAdapter {

    int def;
    public PageAdapter(FragmentManager fm) {
        super(fm);
        this.def = def;
    }

    @Override
    public Fragment getItem(int position) {

        if(position==0)return new FragmentHome();
        if(position==1)return new FragmentMarket();
        if(position==2)return new FragmentOrder();
        if(position==3)return new FragmentProfile();

        return new Fragment();
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return null;
    }
}
