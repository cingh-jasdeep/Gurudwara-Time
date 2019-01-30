package com.example.android.gurudwaratime.ui.status;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gurudwaratime.ExcludeActivity;
import com.example.android.gurudwaratime.IncludeActivity;
import com.example.android.gurudwaratime.PermissionsCheckActivity;
import com.example.android.gurudwaratime.R;

import static com.example.android.gurudwaratime.data.Constants.KEYWORD_QUERY_NEARBY_MAP;
import static com.example.android.gurudwaratime.data.Constants.KEYWORD_QUERY_NEARBY_URL;

public class StatusActivity extends PermissionsCheckActivity {

    private static final String TAG = StatusActivity.class.getSimpleName();
    private StatusViewModel mViewModel;
    private SwitchCompat mAutoSilentSwitch;
    private TextView mSilentModeStatusText;
    private Group mAtLocationUiGroup;
    private Intent mNearbyIntent;
    private Button mNearbyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        setupToolbar();

        mAutoSilentSwitch = findViewById(R.id.switch_silent_mode);
        mSilentModeStatusText = findViewById(R.id.text_silent_mode_status);
        mAtLocationUiGroup = findViewById(R.id.group_at_location_ui);

        mNearbyButton = findViewById(R.id.button_show_nearby);

        connectViewModel();

        mViewModel.getAutoSilentStatus().observe(this,
                new Observer<AutoSilentStatusStates>() {
                    @Override
                    public void onChanged(@Nullable AutoSilentStatusStates autoSilentStatusStates) {
                        if (autoSilentStatusStates != null) {
                            Log.i(TAG, "onChanged: " + autoSilentStatusStates);
                            handleAutoSilentStatusChanged(autoSilentStatusStates);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupNearbyButtonIntent();
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
            SwitchCompat switchView = ((SwitchCompat) view);
            boolean requestedAutoSilentStatus = switchView.isChecked();

            //reset checkbox
            switchView.setChecked(!switchView.isChecked());

            if (mViewModel == null) connectViewModel();
            mViewModel.updateAutoSilentRequestedStatus(requestedAutoSilentStatus);
        }

    }

    /**
     * sets up google maps intent with nearby gurudwaras
     * https://developers.google.com/maps/documentation/urls/android-intents
     */
    private void setupNearbyButtonIntent() {

        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + KEYWORD_QUERY_NEARBY_MAP);
        mNearbyIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mNearbyIntent.setPackage("com.google.android.apps.maps");
        if (mNearbyIntent.resolveActivity(getPackageManager()) == null) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("www.google.com")
                    .appendPath("search")
                    .appendQueryParameter("q", KEYWORD_QUERY_NEARBY_URL);
            mNearbyIntent = new Intent(Intent.ACTION_VIEW, builder.build());
        }
    }

    private void handleAutoSilentStatusChanged(@NonNull AutoSilentStatusStates autoSilentStatus) {
        int stringRes;

        switch (autoSilentStatus) {
            case INIT:
                mAutoSilentSwitch.setChecked(true);
                mAtLocationUiGroup.setVisibility(View.GONE);
                mSilentModeStatusText.setText(R.string.msg_auto_silent_init);

                break;
            case NO_LOCATION:
                mAtLocationUiGroup.setVisibility(View.GONE);
                mSilentModeStatusText.setText(R.string.msg_no_location_detected);

                break;
            case AT_LOCATION:
                mAtLocationUiGroup.setVisibility(View.VISIBLE);
                mSilentModeStatusText.setText(R.string.msg_at_location);
                //todo get current location data

                break;
            case TURNED_OFF:
                mAutoSilentSwitch.setChecked(false);
                mAtLocationUiGroup.setVisibility(View.GONE);
                mSilentModeStatusText.setText(R.string.msg_auto_silent_off);

                break;
        }
    }

    private void updateStatusText(Boolean autoSilentStatus) {
        int stringRes = R.string.msg_auto_silent_off;
        if (autoSilentStatus) {
            stringRes = R.string.msg_auto_silent_init;
        }
        mSilentModeStatusText.setText(stringRes);
    }

    /**
     * opens google maps with nearby gurudwaras
     * https://developers.google.com/maps/documentation/urls/android-intents
     *
     * @param view button view
     */
    public void onNearbyButtonClick(View view) {
        startActivity(mNearbyIntent);
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
