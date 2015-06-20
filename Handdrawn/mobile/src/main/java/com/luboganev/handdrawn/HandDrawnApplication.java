package com.luboganev.handdrawn;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by luboganev on 20/06/15.
 */
public class HandDrawnApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
