package com.example.android.gurudwaratime.ui.welcome;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.android.gurudwaratime.BuildConfig;
import com.example.android.gurudwaratime.R;
import com.example.android.gurudwaratime.ui.status.StatusActivity;

public class WelcomeActivity extends AppCompatActivity {

    public static final String EXTRA_NEED_PERMISSIONS = BuildConfig.APPLICATION_ID +
            ".EXTRA_NEED_PERMISSIONS";
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    Button mPermissionsButton;
    CheckBox mLocationCheckBox, mDNDCheckbox;
    PermissionsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PermissionsViewModel.checkLocationAndDndPermissions(this)) {
            launchStatusScreen(true);
        }

        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_NEED_PERMISSIONS)) {
            if (intent.getBooleanExtra(EXTRA_NEED_PERMISSIONS, false)) {
                Toast.makeText(this, R.string.msg_permissions_needed,
                        Toast.LENGTH_LONG).show();
            }
        }

        setContentView(R.layout.activity_welcome);
        mPermissionsButton = findViewById(R.id.button_welcome_permissions);
        mLocationCheckBox = findViewById(R.id.checkbox_welcome_location_permission);
        mDNDCheckbox = findViewById(R.id.checkbox_welcome_dnd_permission);

        mViewModel = ViewModelProviders.of(this).get(PermissionsViewModel.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            mViewModel.getDNDGrantedStatus().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean granted) {
                    if (granted != null) {
                        if (granted) {
                            mDNDCheckbox.setChecked(true);
                            mDNDCheckbox.setEnabled(false);
                            if (mLocationCheckBox.isChecked()) {
                                launchStatusScreen(false);
                            }
                        }
                    }
                }
            });
        } else {
            mDNDCheckbox.setChecked(true);
            mDNDCheckbox.setEnabled(false);
            mDNDCheckbox.setVisibility(View.INVISIBLE);
        }

        mViewModel.getLocationGrantedStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean granted) {
                if (granted != null) {
                    if (granted) {
                        //location permissions granted
                        mLocationCheckBox.setChecked(true);
                        mLocationCheckBox.setEnabled(false);

                        if (mDNDCheckbox.isChecked()) { // all permissions granted
                            launchStatusScreen(false);
                        } else {
                            if (mDNDCheckbox.getVisibility() == View.VISIBLE) {
                                mDNDCheckbox.setEnabled(true);
                                mPermissionsButton.setText(R.string.action_welcome_button_text_give_dnd_access);
                            }
                        }
                    } else {
                        //location permissions permissions needed
                        mLocationCheckBox.setChecked(false);
                        mLocationCheckBox.setEnabled(true);
                        //disable dnd box, first enable location
                        if (mDNDCheckbox.getVisibility() == View.VISIBLE) {
                            mDNDCheckbox.setEnabled(false);
                        }
                    }
                }
            }
        });

    }

    /**
     * launches status screen
     * usually called after permissions are granted
     */
    private void launchStatusScreen(boolean initialLaunch) {
        if (!initialLaunch) {
            Toast.makeText(this, R.string.msg_permissions_granted,
                    Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(this, StatusActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //in onResume check for permission change
        mViewModel.checkLocationPermission();
        mViewModel.checkDNDPermission();
    }

    /**
     * Called when permission button or permission checkbox is clicked.
     * Request appropriate permissions from viewmodel (first location, then dnd(if needed)
     */
    public void onPermissionsButtonClick(View view) {
        if (view instanceof CheckBox) {
            //reset check on click
            ((CheckBox) view).setChecked(false);
        }
        mViewModel.requestPermissions(this);
    }

    /**
     * Callback received when a permissions request has been completed.
     * for our case this is true only for location permissions
     * source: https://github.com/googlecodelabs/background-location-updates-android-o
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == PermissionsHelper.REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mViewModel.setLocationPermissionGranted(true);
            } else {
                // Permission denied.
                mViewModel.setLocationPermissionGranted(false);

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.activity_welcome),
                        R.string.msg_permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_app_settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

}
