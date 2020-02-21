package com.app.android_shape_image_capture.utils;

import android.app.Application;
import android.os.Handler;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

public class App extends Application {

    public static final int TAKE_PHOTO_CUSTOM = 100;
    public static Handler mHandler;
    public static App sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        mHandler = new Handler();
        Fresco.initialize(this, ImagePipelineConfig.newBuilder(this).setDownsampleEnabled(true).build());
    }
}
