package com.example.musiconnection;

import android.annotation.SuppressLint;
import android.content.Intent;
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

// The class is responsible for showing the Register page of the app to register the user to the app.
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    TextView welcome, registerpageText;
    EditText mail, password, username, passwordvalidate;
    Button submit, forgotPassword, toLoginPage;
    User user = new User("", "", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        //TextView
        welcome = (TextView) findViewById(R.id.welcomeTextRegisterPage);
        registerpageText = (TextView) findViewById(R.id.musiconnectionTextRegisterPage);

        //Button
        submit = (Button) findViewById(R.id.registerToMainPage);
        forgotPassword = (Button) findViewById(R.id.registerToForgotPassword);
        toLoginPage = (Button) findViewById(R.id.registerToLoginPage);

        //EditText
        mail = (EditText) findViewById(R.id.enterMailRegisterPage);
        password = (EditText) findViewById(R.id.enterPasswordRegisterPage);
        passwordvalidate = (EditText) findViewById(R.id.enterPasswordAgainRegisterPage);
        username = (EditText) findViewById(R.id.enterUsernameRegisterPage);

        submit.setOnClickListener(this);
        toLoginPage.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
    }
    
    // Checking if the email provided is valid using a regex pattern, and returning true / false accordingly.
    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    // Returns the string that recieved from the Server side using a socket.
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

    @Override
    public void onClick(View view) {
        if (view == submit) {

            String mailStr = mail.getText().toString();
            String passwordStr = password.getText().toString();
            String usernameStr = username.getText().toString();
            String passwordValitadeStr = passwordvalidate.getText().toString();

            if (!mailStr.equals("") && !passwordStr.equals("") && !usernameStr.equals("") && passwordStr.equals(passwordValitadeStr)){
                if (!isValidEmailAddress(mailStr)){
                    Toast.makeText(this, "Please Enter a Valid Mail", Toast.LENGTH_LONG).show();
                }
                else if(!isValidPassword(passwordStr)){
                    Toast.makeText(this, "Please Enter a Password With Nothing But Digits and Letters", Toast.LENGTH_LONG).show();
                }
                else {
                    if (dbInteract("searchmail users " + mailStr).equals("Failed")) { 
                        // There is no such mail in user - can register

                        user.setName(usernameStr);
                        user.setMail(mailStr);
                        user.setPassword(passwordStr);
                        //create new user and add him to the data base
                        dbInteract("add users " + user.toString());

                        // Storing data into SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("currentUser",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("UserMail", user.getMail());
                        editor.apply();

                        Intent intent = new Intent(RegisterActivity.this, MainScreenApp.class);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(this, "This mail is already in use", Toast.LENGTH_LONG).show();
                    }
                }
            }
            else if (!passwordStr.equals(passwordValitadeStr)){
                Toast.makeText(this, "Please Enter The Same Password", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "Please Enter Valid Mail, Username, and Passwords", Toast.LENGTH_LONG).show();
            }
        } else if (view == forgotPassword) {
            Intent intent = new Intent(RegisterActivity.this, ForgotPassword.class);
            startActivity(intent);
        } else if (view == toLoginPage) {
            Intent intent1 = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent1);
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_guitar:
                user.setInstrument(0, checked);
                break;
            case R.id.checkbox_piano:
                user.setInstrument(1, checked);
                break;
            case R.id.checkbox_bass:
                user.setInstrument(2, checked);
                break;
            case R.id.checkbox_drums:
                user.setInstrument(3, checked);
                break;
        }
    }

    // Checks if the password provided is valid (only chars and digits) and returns true / false accordingly
    public boolean isValidPassword(String password){
        for (int i = 0; i < password.length(); ++i){
            if (!Character.isAlphabetic(password.charAt(i)) && !Character.isDigit(password.charAt(i)))
                return false;
        }
        return true;
    }
}