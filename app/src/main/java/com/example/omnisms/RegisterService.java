package com.example.omnisms;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegisterService extends AppCompatActivity {

    Button acceptBttn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_service);

        acceptBttn = findViewById(R.id.acceptBttn);

        Intent intent = getIntent();
        final String package_name = intent.getStringExtra("package_name");
        final String activity_name = intent.getStringExtra("activity_name");

        acceptBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent return_intent = new Intent();
                return_intent.setComponent(new ComponentName(package_name, activity_name));
                startActivity(return_intent);
                finish();
            }
        });
    }
}
