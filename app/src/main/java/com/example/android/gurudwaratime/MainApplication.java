package com.example.android.gurudwaratime;

import android.support.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;

public class MainApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        //debugging with stetho
        Stetho.initializeWithDefaults(this);

    }
}

