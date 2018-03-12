package com.xshengh.cmax;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

/**
 * Created by xshengh on 18/3/3.
 */

public class CmaxApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
