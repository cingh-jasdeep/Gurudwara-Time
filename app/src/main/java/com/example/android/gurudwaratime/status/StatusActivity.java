package com.example.android.gurudwaratime.status;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gurudwaratime.ExcludeActivity;
import com.example.android.gurudwaratime.IncludeActivity;
import com.example.android.gurudwaratime.NearbyActivity;
import com.example.android.gurudwaratime.PermissionsCheckActivity;
import com.example.android.gurudwaratime.R;

public class StatusActivity extends PermissionsCheckActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private StatusViewModel mViewModel;
    private SwitchCompat mAutoSilentSwitch;
    private TextView mSilentModeStatusText;
    private Group mAtLocationUiGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        setupToolbar();

        mAutoSilentSwitch = findViewById(R.id.switch_silent_mode);
        mSilentModeStatusText = findViewById(R.id.text_silent_mode_status);
        mAtLocationUiGroup = findViewById(R.id.group_at_location_ui);

        connectViewModel();

        //register shared preference listener
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(this);

        mViewModel.getAutoSilentStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean autoSilentStatus) {
                if (autoSilentStatus != null) {
                    handleAutoSilentStatusChanged(autoSilentStatus);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open_include_list:
                launchIncludeList();
                break;
            case R.id.action_open_exclude_list:
                launchExcludeList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case StatusViewModel.KEY_AUTO_SILENT_STATUS:
                if (mViewModel == null) connectViewModel();
                mViewModel.onAutoSilentStatusChanged();
        }
    }

    private void connectViewModel() {
        mViewModel = ViewModelProviders.of(this).get(StatusViewModel.class);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_status);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayShowTitleEnabled(false);
    }

    public void onAutoSilentSwitchClick(View view) {
        if (view instanceof SwitchCompat) {
            boolean requestedAutoSilentStatus = ((SwitchCompat) view).isChecked();
            if (mViewModel == null) connectViewModel();
            mViewModel.updateAutoSilentStatus(requestedAutoSilentStatus);
        }

    }

    private void handleAutoSilentStatusChanged(@NonNull Boolean autoSilentStatus) {
        //update switch ui
        if (mAutoSilentSwitch.isChecked() != autoSilentStatus) {
            mAutoSilentSwitch.setChecked(autoSilentStatus);
        }

        if (autoSilentStatus) {
            //if auto silent turned on
            updateStatusText(true);
            //start location updates
            StatusViewModel.startLocationUpdates(getApplicationContext());

        } else {
            //if auto silent turned off
            updateStatusText(false);
            //hide at location ui
            mAtLocationUiGroup.setVisibility(View.GONE);//todo could animate
            //stop location updates
            StatusViewModel.stopLocationUpdates(getApplicationContext());
            //todo remove geofences
        }
    }

    private void updateStatusText(Boolean autoSilentStatus) {
        int stringRes = R.string.msg_auto_silent_off;
        if (autoSilentStatus) {
            stringRes = R.string.msg_auto_silent_init;
        }
        mSilentModeStatusText.setText(stringRes);
    }

    public void onNearbyButtonClick(View view) {
        Intent openNearbyIntent = new Intent(this, NearbyActivity.class);
        startActivity(openNearbyIntent);
    }

    private void launchIncludeList() {
        Intent intent = new Intent(this, IncludeActivity.class);
        startActivity(intent);
    }

    private void launchExcludeList() {
        Intent intent = new Intent(this, ExcludeActivity.class);
        startActivity(intent);
    }


    private void displayToast(int string_res_id) {
        Toast.makeText(this, string_res_id, Toast.LENGTH_SHORT).show();
    }
}
