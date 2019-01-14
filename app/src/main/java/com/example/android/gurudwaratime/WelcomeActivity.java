package com.example.android.gurudwaratime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    Button mPermissionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mPermissionsButton = findViewById(R.id.button_welcome_permissions);

    }

    public void onPermissionsButtonClick(View view) {
        Intent intent = new Intent(this, StatusActivity.class);
        startActivity(intent);
    }
}
