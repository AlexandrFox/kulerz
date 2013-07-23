package com.kulerz.app;

import android.app.Activity;
import android.os.Bundle;

public abstract class KulerzActivity extends Activity
{

    protected Kulerz kulerz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kulerz = (Kulerz)getApplication();
    }

}
