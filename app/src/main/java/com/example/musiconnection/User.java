package com.example.musiconnection;

import androidx.annotation.NonNull;

import java.io.Serializable;

// The class that represents a user as an object, containing his username, password, mail and instruments.
public class User implements Serializable {
    private String name;
    private String mail;
    private String password;
    private boolean[] instruments;

    // Constructor for creating a new User instance using the given name, email and password
    public User(String name, String mail, String password) {
        this.name = name;
        this.mail = mail;
        this.password = password;
        instruments = new boolean[4];
    }

    // Returns the instruments of the user
    public boolean[] getInstruments() {return instruments;}

    // Resets the instruments of the user to none all (false)
    public void resetInstruments() { for (int i = 0; i < 4; i++) instruments[i] = false; }

    // Returns the username of the user
    public String getName() {
        return name;
    }

    // Sets the user's username
    public void setName(String name) {
        this.name = name;
    }

    // Returns the email of the user
    public String getMail() {
        return mail;
    }
    
    // Sets the user's email
    public void setMail(String mail) {
        this.mail = mail;
    }

    // Returns the password of the user
    public String getPassword() {
        return password;
    }

    // Sets the user's password
    public void setPassword(String password) {
        this.password = password;
    }

    // Sets the user's instrument in the specified index to the specified value
    public void setInstrument(int index, boolean check) {if ((index >= 0) && (index < 4)) instruments[index] = check; }

    // Converting the instruments into a string representation of them, and returning it
    private String instrumentsToString(boolean[] instruments){
        StringBuilder ret = new StringBuilder(",");
        for (int i = 0; i < instruments.length; ++i){
            if (!instruments[i]){
                ret.append("false,");
            }
            else {
                ret.append("true,");
            }
        }
        return ret.toString();
    }

    // Converting the user into a string representation of it, and returning it
    @NonNull
    @Override
    public String toString() {
        return name + ',' + mail + ',' + password +
               instrumentsToString(instruments);
    }
}
