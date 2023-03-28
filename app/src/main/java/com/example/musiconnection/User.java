package com.example.musiconnection;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String mail;
    private String password;
    private boolean[] instruments;

    public User(String name, String mail, String password) {
        this.name = name;
        this.mail = mail;
        this.password = password;
        instruments = new boolean[4];
    }


    public boolean[] getInstruments() {return instruments;}

    public void resetInstruments() { for (int i = 0; i < 4; i++) instruments[i] = false; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setInstrument(int index, boolean check) {if ((index >= 0) && (index < 4)) instruments[index] = check; }

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

    @NonNull
    @Override
    public String toString() {
        return name + ',' + mail + ',' + password +
               instrumentsToString(instruments);
    }
}
