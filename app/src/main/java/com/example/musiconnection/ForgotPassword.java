package com.example.musiconnection;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

// ForgotPassword represents the Forgot Password page in the app.
public class ForgotPassword extends AppCompatActivity implements View.OnClickListener {
    TextView forgotPasswordPage;
    EditText mail;
    Button submit, back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        //TextView
        forgotPasswordPage = (TextView)findViewById(R.id.textForgotPasswordPage);

        //Button
        submit = (Button)findViewById(R.id.submitForgotPassword);
        back = (Button)findViewById(R.id.backForgotPassword);
        //EditText
        mail = (EditText)findViewById(R.id.enterMailForgotPassword);

        submit.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == back){
            finish();
        }
        else if (view == submit){
            String response = dbInteract("searchmail users " + mail.getText().toString());
            if (!response.equals("Failed")) {
                //there is a user using that mail
                User user = toUser(response);
                if (dbInteract("sendforgotpassword " + user.getMail() + " " + user.getPassword()).equals("Sent")) {
                    Toast.makeText(this, "A mail had been sent to this user's mail containing it's password", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "An error had occured. Please try again later. ", Toast.LENGTH_LONG).show();
                }
            }
            else
                Toast.makeText(this, "No such user, please register. ", Toast.LENGTH_LONG).show();
        }
    }

    // Returns the String that the functions receives from the Server side using sockets to connect.
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

    // Genegrates a User class object from a user's String representation, and returns it.
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
}