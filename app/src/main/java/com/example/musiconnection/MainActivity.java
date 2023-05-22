package com.example.musiconnection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// MainActivity is responsible for the Opening page of the app.
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView welcome, musiconnection;
    Button login, register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // if there is a user already on this device (saved)
        SharedPreferences sh = getSharedPreferences("currentUser", MODE_PRIVATE);
        String currentUserMail = sh.getString("UserMail", "");

        if (!currentUserMail.equals("")){
            Intent intent = new Intent(MainActivity.this, MainScreenApp.class);
            startActivity(intent);
        }

        //TextView
        welcome = (TextView)findViewById(R.id.welcomeTextFirstPage);
        musiconnection = (TextView)findViewById(R.id.musiconnectionTextFirstPage);

        //Button
        login = (Button)findViewById(R.id.firstToLoginPage);
        register = (Button)findViewById(R.id.firstToRegisterPage);

        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == login){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        else if (view == register){
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    }

}