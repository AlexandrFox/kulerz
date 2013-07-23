package com.kulerz.app;

import android.app.Application;
import com.kulerz.app.preferences.KulerzSettings;

public class Kulerz extends Application {

    public static final String TAG = "Kulerz";
    public static final boolean DEBUG = KulerzSettings.DEBUG;

}
