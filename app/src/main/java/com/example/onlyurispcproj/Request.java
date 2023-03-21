package com.example.onlyurispcproj;

public class Request {
    private BandClass bandToJoin;
    private User requester;

    public Request(BandClass bandToJoin, User requester) {
        this.bandToJoin = bandToJoin;
        this.requester = requester;
    }

    public BandClass getBandToJoin() {
        return bandToJoin;
    }

    public void setBandToJoin(BandClass bandToJoin) {
        this.bandToJoin = bandToJoin;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }
}
