package com.example.android.gurudwaratime.ui.nearby;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.gurudwaratime.PermissionsCheckActivity;
import com.example.android.gurudwaratime.R;

public class NearbyActivity extends PermissionsCheckActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        Toolbar toolbar = findViewById(R.id.toolbar_nearby);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_nearby, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshList() {
        displayToast(R.string.action_refresh);
    }

    private void displayToast(int string_res_id) {
        Toast.makeText(this, string_res_id, Toast.LENGTH_SHORT).show();
    }
}
