package com.example.android.gurudwaratime;

import android.app.Application;

import com.example.android.gurudwaratime.data.Constants;
import com.facebook.stetho.Stetho;
import com.google.maps.GeoApiContext;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //debugging with stetho
        Stetho.initializeWithDefaults(this);

    }
}

