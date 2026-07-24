package com.example.m_hikeapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Splash/redirect Activity kept to satisfy the original manifest entry.
 * Immediately redirects to HikeListActivity which is the real launcher.
 *
 * Note: The AndroidManifest now sets HikeListActivity as the MAIN launcher,
 * so this class is only retained in case the old manifest entry is still cached.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Redirect immediately to the real entry point
        startActivity(new Intent(this, HikeListActivity.class));
        finish();
    }
}