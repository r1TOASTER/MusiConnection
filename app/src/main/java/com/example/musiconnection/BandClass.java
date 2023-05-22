package com.example.musiconnection;

import androidx.annotation.NonNull;

// BandClass represents a music band with a name, owner, members, and location. It provides methods to add and remove members, check if the band is full, and clone the band object.
public class BandClass implements Cloneable {
    // SIZE_OF_MEMBERS is a constant variable that determines the maximum number of members in the band.
    static int SIZE_OF_MEMBERS = 5;

    // name is the name of the band, members is an array of User objects representing the members of the band, location is the location of the band,
// and owner is the User object representing the owner of the band.
    private String name;
    private User[] members;
    private LocationClass location;
    private static int count = 0;
    private User owner;

    // Constructor for creating a new BandClass object with the given name, location, and owner.
    public BandClass(String name, LocationClass location, User owner) {
        this.name = name;
        members = new User[SIZE_OF_MEMBERS];
        this.location = location;
        this.owner = owner;
    }

    // Getter for the name of the band.
    public String getName() {
        return name;
    }

    // Setter for the name of the band.
    public void setName(String name) {
        this.name = name;
    }

    // Getter for the members of the band.
    public User[] getMembers() {
        return members;
    }

    // Getter for the location of the band.
    public LocationClass getLocation() {
        return location;
    }

    // Setter for the location of the band.
    public void setLocation(LocationClass location) {
        this.location = location;
    }

    // Getter for the owner of the band.
    public User getOwner() {
        return owner;
    }

    // Getter for the member of the band at the given index in the members array.
    public User getMemberByIndex(int index){
        if ((0  <= index) && (index < 5)){
            return members[index];
        }
        return null;
    }

    // Setter for the owner of the band.
    public void setOwner(User owner) {
        this.owner = owner;
    }

    // Resets the members array of the band to null.
    public void resetMembers(){
        for (int i = 0; i < SIZE_OF_MEMBERS; ++i){
            members[i] = null;
        }
    }

    // Adds a new member to the band. Returns true if the member is added successfully, false otherwise.
    public boolean addMember(User user){
        for (int i = 0; i < 5; ++i){
            if (members[i] == null){
                members[i] = user;
                return true;
            }
        }
        return false;
    }

    // Converts the members array of the band to a string.
    private String membersToString(User[] members){
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < members.length; ++i){
            if (members[i] != null) ret.append(members[i].toString());
        }
        return ret.toString();
    }

    // Converts the BandClass object to a string.
    @NonNull
    @Override
    public String toString() {
        return name + ',' + location.toString() + ',' + owner.toString() + membersToString(members);
    }

    // Removes a member from the band.
    public void removeMember(User user){
        for (int i = 0; i < 5; ++i){
            if (members[i] == user){
                members[i] = null;
                return;
            }
        }
    }

    // Checks if a member is inside the band, and returns true / false accordingly.
    public boolean isMember(User member) {
        for (int i = 0; i < 5; ++i){
            if (members[i] != null && members[i].getMail().equals(member.getMail())){
                return true;
            }
        }
        return false;
    }

    // Checks if the band is full, and returns true / false accordingly.
    public boolean isFull() {
        for (User member : members) {
            if (member == null) {
                return false;
            }
        }
        return true;
    }

    // Returns the cloned band as a band object.
    public BandClass clone() throws CloneNotSupportedException {
        return (BandClass) super.clone();
    }
}
