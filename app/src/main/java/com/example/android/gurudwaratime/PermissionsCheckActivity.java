package com.example.android.gurudwaratime;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.example.android.gurudwaratime.utilities.PermissionsHelper;
import com.example.android.gurudwaratime.welcome.WelcomeActivity;


public abstract class PermissionsCheckActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (!PermissionsHelper.checkLocationAndDndPermissions(this)) {
            launchWelcomeActivity();
        }
    }

    protected void launchWelcomeActivity() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.putExtra(WelcomeActivity.EXTRA_NEED_PERMISSIONS, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
