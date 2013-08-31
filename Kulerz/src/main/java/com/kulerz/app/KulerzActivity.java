package com.kulerz.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class KulerzActivity extends FragmentActivity
{

    protected Kulerz kulerz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kulerz = (Kulerz)getApplication();
    }

}
