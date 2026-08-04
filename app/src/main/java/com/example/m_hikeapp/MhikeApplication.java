package com.example.m_hikeapp;

import android.app.Application;
import com.mapbox.common.MapboxOptions;

public class MhikeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Manually setting the access token to ensure Mapbox SDK is initialized correctly.
        // Using the token from strings.xml (which now contains the secret key).
        MapboxOptions.setAccessToken(getString(R.string.mapbox_access_token));
    }
}
