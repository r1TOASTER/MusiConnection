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

// Showing the Settings screen of the app for the user.
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

        String search_mail = dbInteract("searchmail users " + currentUserMail);
        if (search_mail.equals("ServerFailed")) {
            Toast.makeText(this, "Server failed to connect. Please try again later", Toast.LENGTH_LONG).show();
            finish();
        }
        else if (search_mail.equals("Failed")) { // no such user
            Toast.makeText(this, "Error when trying to get the user. Please try again later", Toast.LENGTH_LONG).show();
            finish();
        }
        currentUser = toUser(search_mail);

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
            // if the user clicked on the log out button
            SharedPreferences settings = getSharedPreferences("currentUser", Context.MODE_PRIVATE);
            settings.edit().clear().apply();
            Intent intent = new Intent(SettingActivirty.this, MainActivity.class);
            startActivity(intent);
        }
        else if (view == deleteAccount) {
            // if the user clicked on the delete account button
            if (currentUser != null) {
                String currentUserString = currentUser.toString();
                String remove_user = dbInteract("remove users " + currentUserString);
                if (remove_user.equals("ServerFailed")) {
                    Toast.makeText(this, "Server failed to connect. Please try again later", Toast.LENGTH_LONG).show();
                }
                else if (!remove_user.equals("Failed")){
                    String remove_bands_of = dbInteract("removebandsof bands " + currentUserString);
                    if (remove_bands_of.equals("ServerFailed")) {
                        Toast.makeText(this, "Server failed to connect. Please try again later", Toast.LENGTH_LONG).show();
                    }
                    else if (!remove_bands_of.equals("Failed")) {
                        String remove_all_requests = dbInteract("removeAllRequests requests " + currentUserString);
                        if (remove_all_requests.equals("ServerFailed")) {
                            Toast.makeText(this, "Server failed to connect. Please try again later", Toast.LENGTH_LONG).show();
                        }
                        else if (!remove_all_requests.equals("Failed")) {
                            SharedPreferences settings = getSharedPreferences("currentUser", Context.MODE_PRIVATE);
                            settings.edit().clear().apply();
                            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SettingActivirty.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(this, "Failed to remove the user's requests. Please try again later", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(this, "Failed to remove the user's bands / the user as a member of bands. Please try again later", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(this, "Failed to delete account, try again later", Toast.LENGTH_LONG).show();
                }
            }

        }
        else if (view == toDetails) {
            // if the user wants to go into the account details page
            Intent intent = new Intent(SettingActivirty.this, EditActivity.class);
            startActivity(intent);
        }
        else if (view == goBack) {
            // if the user wants to return to the previous page
            finish();
        }
        else if (view == forgotPassword){
            // if the user wants to go into the forgot password page
            Intent intent = new Intent(SettingActivirty.this, ForgotPassword.class);
            startActivity(intent);
        }
    }

    // Converts a user's representation as a string into a User class object and returns it
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

    // Returns the string retrieved from the Server side using a socket.
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