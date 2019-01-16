package com.example.android.gurudwaratime;

import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class IncludeActivity extends PermissionsCheckActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_include);

        Toolbar toolbar = findViewById(R.id.toolbar_include);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
