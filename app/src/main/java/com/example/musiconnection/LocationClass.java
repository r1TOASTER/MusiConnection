package com.example.musiconnection;

import androidx.annotation.NonNull;

// LocationClass represents a location with a latitude, longitude, and an address.
public class LocationClass {
    private double latitude;
    private double longitude;
    private String address;

    // Constructor that gets latitude, longitude and an address and returns the location object from that.
    public LocationClass(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    // Returns the latitude of the location.
    public double getLatitude() {
        return latitude;
    }

    // Sets the latitude of the location using a given one.
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    // Returns the longitude of the location.
    public double getLongitude() {
        return longitude;
    }

    // Sets the longitude of the location using a given one.
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Returns the address of the location.
    public String getAddress() {
        return address;
    }

    // Sets the address of the location using a given one.
    public void setAddress(String address) {
        this.address = address;
    }

    // Returns the string representation of the location class.
    @NonNull
    @Override
    public String toString(){
        return latitude + "," + longitude + "," + address;
    }
}