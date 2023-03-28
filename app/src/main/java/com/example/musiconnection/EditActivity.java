package com.example.musiconnection;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {
    TextView textEditAccount;
    EditText newMail, newUsername, newPassword, newRadius;
    Button saveNew, goBack;
    User currentUser, saveTo = null;
    CheckBox guitar, piano, bass, drums;

    int radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edt_account);

        textEditAccount = findViewById(R.id.textEditAccount);

        SharedPreferences sh = getSharedPreferences("currentUser", MODE_PRIVATE);
        String currentUserMail = sh.getString("UserMail", "");
        radius = sh.getInt("radius", -1);

        if (radius == -1){
            SharedPreferences.Editor editor = sh.edit();
            editor.putInt("radius", 10);
            editor.apply();
            radius = 10;
        }

        currentUser = toUser(dbInteract("searchmail users " + currentUserMail));

        saveNew = findViewById(R.id.saveEditAccount);
        goBack = findViewById(R.id.goBackButton);

        newMail = findViewById(R.id.newMailEdit);
        newMail.setHint(currentUser.getMail());

        newUsername = findViewById(R.id.newUsernameEdit);
        newUsername.setHint(currentUser.getName());

        newPassword = findViewById(R.id.newPasswordEdit);
        newPassword.setHint(currentUser.getPassword());

        newRadius = findViewById(R.id.newRadiusEdit);
        newRadius.setHint(String.valueOf(radius));

        guitar = findViewById(R.id.checkbox_guitarEdit);
        guitar.setChecked(currentUser.getInstruments()[0]);

        piano = findViewById(R.id.checkbox_pianoEdit);
        piano.setChecked(currentUser.getInstruments()[1]);

        bass = findViewById(R.id.checkbox_bassEdit);
        bass.setChecked(currentUser.getInstruments()[2]);

        drums = findViewById(R.id.checkbox_drumsEdit);
        drums.setChecked(currentUser.getInstruments()[3]);


        saveTo = new User(currentUser.getName(), currentUser.getMail(), currentUser.getPassword());
        for (int i = 0; i < 4; ++i){
            saveTo.setInstrument(i, currentUser.getInstruments()[i]);
        }

        saveNew.setOnClickListener(this);
        goBack.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_guitarEdit:
                saveTo.setInstrument(0, checked);
                break;
            case R.id.checkbox_pianoEdit:
                saveTo.setInstrument(1, checked);
                break;
            case R.id.checkbox_bassEdit:
                saveTo.setInstrument(2, checked);
                break;
            case R.id.checkbox_drumsEdit:
                saveTo.setInstrument(3, checked);
                break;
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

    @Override
    public void onClick(View view) {
        if (view == saveNew) {
            if ((!isValidEmailAddress(newMail.getText().toString()) && !newMail.getText().toString().equals(""))
                    || (!isValidPassword(newPassword.getText().toString()) && (!newPassword.getText().toString().equals("")))) {
                Toast.makeText(this, "Please enter valid mail / password", Toast.LENGTH_LONG).show();
            }
            else {
                if (!newMail.getText().toString().equals("")) {
                    if (dbInteract("searchmail users " + newMail.getText().toString()).equals("Failed")) {
                        saveTo.setMail(newMail.getText().toString());

                        if (!newPassword.getText().toString().equals(""))
                            saveTo.setPassword(newPassword.getText().toString());

                        if (!newUsername.getText().toString().equals(""))
                            saveTo.setName(newUsername.getText().toString());

                        if (!dbInteract("update users " + currentUser.toString() + "," + saveTo.toString()).equals("Failed")){

                            SharedPreferences sharedPreferences = getSharedPreferences("currentUser",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            if (newRadius.getText().toString().equals("")) {
                                editor.putInt("radius", 10);
                                editor.apply();
                            }
                            try {
                                editor.putInt("radius", Integer.parseInt(newRadius.getText().toString()));
                                editor.apply();
                            } catch (Exception e) {
                                Toast.makeText(EditActivity.this, "Not a valid number. ", Toast.LENGTH_LONG).show();
                            }

                            Toast.makeText(this, "User details updated successfully", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else {
                            Toast.makeText(this, "Failed to update account, try again later", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(this, "This mail is already in use", Toast.LENGTH_LONG).show();
                    }
                }
            else {
                    saveTo.setMail(currentUser.getMail());

                    if (!newPassword.getText().toString().equals(""))
                        saveTo.setPassword(newPassword.getText().toString());

                    if (!newUsername.getText().toString().equals(""))
                        saveTo.setName(newUsername.getText().toString());

                    SharedPreferences sharedPreferences = getSharedPreferences("currentUser",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    if (newRadius.getText().toString().equals("")) {
                        editor.putInt("radius", 10);
                        editor.apply();
                    }
                    else {
                        try {
                            editor.putInt("radius", Integer.parseInt(newRadius.getText().toString()));
                            editor.apply();
                        } catch (Exception e) {
                            Toast.makeText(EditActivity.this, "Not a valid number. ", Toast.LENGTH_LONG).show();
                        }
                    }

                    if (!dbInteract("update users " + currentUser.toString() + "," + saveTo.toString()).equals("Failed")){
                        Toast.makeText(this, "User details updated successfully", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else {
                        Toast.makeText(this, "Failed to update account, try again later", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        else if (view == goBack) {
            finish();
        }
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

    public boolean isValidPassword(String password){
        for (int i = 0; i < password.length(); ++i){
            if (!Character.isAlphabetic(password.charAt(i)) && !Character.isDigit(password.charAt(i)))
                return false;
        }
        return true;
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}