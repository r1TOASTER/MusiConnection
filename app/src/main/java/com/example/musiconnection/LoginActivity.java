package com.example.musiconnection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    TextView welcome, loginpageText;
    EditText mail, password;
    Button submit, registerPage, forgotPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        //TextView
        welcome = (TextView)findViewById(R.id.welcomeTextLoginPage);
        loginpageText = (TextView)findViewById(R.id.musiconnectionTextLoginPage);

        //Button
        submit = (Button)findViewById(R.id.loginToMainPage);
        registerPage = (Button)findViewById(R.id.loginToRegisterPage);
        forgotPassword = (Button)findViewById(R.id.forgotPasswordLoginPage);

        //EditText
        mail = (EditText)findViewById(R.id.enterMailLoginPage);
        password = (EditText)findViewById(R.id.enterPasswordLoginPage);

        submit.setOnClickListener(this);
        registerPage.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
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

    @Override
    public void onClick(View view) {
        if (view == submit){
            String mailStr = mail.getText().toString();
            String passwordStr = password.getText().toString();
            if (!mailStr.equals("") && !passwordStr.equals("")){
                if (!isValidEmailAddress(mailStr)){
                    Toast.makeText(this, "Please Enter a Valid Mail", Toast.LENGTH_LONG).show();
                }
                else {
                    String isUser = dbInteract("searchmail users " + mailStr);


                    if (isUser.equals("Failed")){
                        Toast.makeText(this, "No such user, please register", Toast.LENGTH_LONG).show();
                    }
                    else {
                        String isPassword = dbInteract("searchpassword users " + passwordStr);

                        if (isPassword.equals("Failed")){
                            Toast.makeText(this, "Please enter the correct password, or press on the forgot password button", Toast.LENGTH_LONG).show();
                        }
                        else {
                            //sleep between getting user from server after logging / registering
                            try {
                                Thread.sleep(2000);
                            }
                            catch (InterruptedException e){
                                e.printStackTrace();
                            }

                            SharedPreferences sharedPreferences = getSharedPreferences("currentUser",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("UserMail", mailStr);
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, MainScreenApp.class);

                            startActivity(intent);
                        }
                    }
                }
            }
            else {
                Toast.makeText(this, "Please Enter Valid Mail and Password", Toast.LENGTH_LONG).show();
            }
        }
        else if (view == registerPage){
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
        else if (view == forgotPassword){
            Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
            startActivity(intent);
        }
    }

}