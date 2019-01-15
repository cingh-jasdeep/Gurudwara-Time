package com.example.android.gurudwaratime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class StatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        Toolbar toolbar = findViewById(R.id.toolbar_status);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayShowTitleEnabled(false);


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
