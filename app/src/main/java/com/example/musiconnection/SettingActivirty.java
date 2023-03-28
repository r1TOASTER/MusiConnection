package com.example.musiconnection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

public class SettingActivirty extends AppCompatActivity implements View.OnClickListener {
    TextView settingsText;
    Button logOut, deleteAccount, toDetails, goBack, forgotPassword;
    User currentUser;
    String strToUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_activirty);

        settingsText = findViewById(R.id.textSettingsActivity);

        SharedPreferences sh = getSharedPreferences("currentUser", MODE_PRIVATE);
        String currentUserMail = sh.getString("UserMail", "");
        currentUser = toUser(dbInteract("searchmail users " + currentUserMail));

        forgotPassword = findViewById(R.id.forgotPasswordButton);
        logOut = findViewById(R.id.logOutButton);
        deleteAccount = findViewById(R.id.deleteAccountButton);
        toDetails = findViewById(R.id.toDetailsButton);
        goBack = findViewById(R.id.goBackButton);

        logOut.setOnClickListener(this);
        deleteAccount.setOnClickListener(this);
        toDetails.setOnClickListener(this);
        goBack.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == logOut) {
            SharedPreferences settings = getSharedPreferences("currentUser", Context.MODE_PRIVATE);
            settings.edit().clear().apply();
            Intent intent = new Intent(SettingActivirty.this, MainActivity.class);
            startActivity(intent);
        }
        else if (view == deleteAccount) {
            if (currentUser != null) {
                String currentUserString = currentUser.toString();
                if (!dbInteract("remove users " + currentUserString).equals("Failed")){
                    if (!dbInteract("removebandsof bands " + currentUserString).equals("Failed")) {
                        if (!dbInteract("removeAllRequests requests " + currentUserString).equals("Failed")) {
                            SharedPreferences settings = getSharedPreferences("currentUser", Context.MODE_PRIVATE);
                            settings.edit().clear().apply();
                            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SettingActivirty.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                }
                else {
                    Toast.makeText(this, "Failed to delete account, try again later", Toast.LENGTH_LONG).show();
                }
            }

        }
        else if (view == toDetails) {
            Intent intent = new Intent(SettingActivirty.this, EditActivity.class);
            startActivity(intent);
        }
        else if (view == goBack) {
            finish();
        }
        else if (view == forgotPassword){
            Intent intent = new Intent(SettingActivirty.this, ForgotPassword.class);
            startActivity(intent);
        }
    }

    private User toUser(String ret) {
        String[] values = ret.split(","); //spliting by ","
        String name = values[0];
        String mail = values[1];
        String password = values[2];
        User returnUser = new User(name, mail, password);
        for (int i = 0; i < 4; ++i){
            returnUser.setInstrument(i, values[i + 3].equals(" true"));
        }
        return returnUser;
    }

    public String dbInteract(String message) {
        String response;
        try {
            Sockets dbLinker = new Sockets();
            response = dbLinker.execute(message).get();
        } catch (ExecutionException | InterruptedException e) {
            response = "Failed";
            e.printStackTrace();
        }
        return response;
    }
}