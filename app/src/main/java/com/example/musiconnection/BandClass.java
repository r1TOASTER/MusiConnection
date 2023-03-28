package com.example.musiconnection;

import androidx.annotation.NonNull;

public class BandClass implements Cloneable {
    static int SIZE_OF_MEMBERS = 5;
    private String name;
    private User[] members;
    private LocationClass location;
    private static int count = 0;
    private User owner;

    public BandClass(String name, LocationClass location, User owner) {
        this.name = name;
        members = new User[SIZE_OF_MEMBERS];
        this.location = location;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User[] getMembers() {
        return members;
    }

    public LocationClass getLocation() {
        return location;
    }

    public void setLocation(LocationClass location) {
        this.location = location;
    }

    public User getOwner() {
        return owner;
    }

    public User getMemberByIndex(int index){
        if ((0  <= index) && (index < 5)){
            return members[index];
        }
        return null;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void resetMembers(){
        for (int i = 0; i < SIZE_OF_MEMBERS; ++i){
            members[i] = null;
        }
    }

    public boolean addMember(User user){
        for (int i = 0; i < 5; ++i){
            if (members[i] == null){
                members[i] = user;
                return true;
            }
        }
        return false;
    }

    private String membersToString(User[] members){
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < members.length; ++i){
            if (members[i] != null) ret.append(members[i].toString());
        }
        return ret.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return name + ',' + location.toString() + ',' + owner.toString() + membersToString(members);
    }

    public void removeMember(User user){
        for (int i = 0; i < 5; ++i){
            if (members[i] == user){
                members[i] = null;
                return;
            }
        }
    }

    public boolean isMember(User member) {
        for (int i = 0; i < 5; ++i){
            if (members[i] != null && members[i].getMail().equals(member.getMail())){
                return true;
            }
        }
        return false;
    }

    public boolean isFull() {
        for (User member : members) {
            if (member == null) {
                return false;
            }
        }
        return true;
    }

    public BandClass clone() throws CloneNotSupportedException {
        return (BandClass) super.clone();
    }
}
