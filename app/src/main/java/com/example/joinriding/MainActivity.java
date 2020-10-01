package com.example.joinriding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    // Declare View
    private Button buttonRegister, buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init View
        buttonRegister = findViewById(R.id.btnRegisterUser);
        buttonLogin = findViewById(R.id.btnLoginUser);

        // Handle Button Register Click
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Register Activity
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        // Handle Button Login Click
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Login Activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}
