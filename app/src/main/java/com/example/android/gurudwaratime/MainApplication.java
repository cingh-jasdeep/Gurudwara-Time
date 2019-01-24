package com.example.android.gurudwaratime;

import android.app.Application;

import com.example.android.gurudwaratime.data.Constants;
import com.facebook.stetho.Stetho;
import com.google.maps.GeoApiContext;

public class MainApplication extends Application {


    private GeoApiContext mGeoApiContext;

    @Override
    public void onCreate() {
        super.onCreate();

        //debugging with stetho
        Stetho.initializeWithDefaults(this);

        mGeoApiContext = new GeoApiContext.Builder()
                .apiKey(Constants.KEY_QUERY_DEFAULT_VALUE_PLACES_API)
                .build();

    }

    public GeoApiContext getGeoApiContext() {
        return mGeoApiContext;
    }
}

