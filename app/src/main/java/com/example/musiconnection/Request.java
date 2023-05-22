package com.example.musiconnection;
// Request class is responsible for holding the information about a request object – the requester who wants to join a band, and the requested band to join.
public class Request {
    private BandClass bandToJoin;
    private User requester;
    // Constructor for creating a request using the requester and the band requested to join as provided.
    public Request(BandClass bandToJoin, User requester) {
        this.bandToJoin = bandToJoin;
        this.requester = requester;
    }
    // Returns the band to join
    public BandClass getBandToJoin() {
        return bandToJoin;
    }
    // Sets the band to join using the band provided
    public void setBandToJoin(BandClass bandToJoin) {
        this.bandToJoin = bandToJoin;
    }
    // Returns the requester user
    public User getRequester() {
        return requester;
    }
    // Sets the requester user using the user provided
    public void setRequester(User requester) {
        this.requester = requester;
    }
}
