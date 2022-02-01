package com.example.guardiancamera_wifi.domain.model;


/**
 * Container Class.
 * Contains the list of current user's peers.
 *
 */
public class Peers {
    private User[] protecteds = {};
    private User[] guardians = {};
    private User[] protectionRequests = {};
    private User[] protectionOffers = {};


    public void setProtecteds(User[] protecteds) {
        this.protecteds = protecteds;
    }

    public void setGuardians(User[] guardians) {
        this.guardians = guardians;
    }

    public void setProtectionRequests(User[] protectionRequests) {
        this.protectionRequests = protectionRequests;
    }

    public void setProtectionOffers(User[] protectionOffers) {
        this.protectionOffers = protectionOffers;
    }

    public User[] getProtecteds() {
        return protecteds;
    }

    public User[] getGuardians() {
        return guardians;
    }

    public User[] getProtectionRequests() {
        return protectionRequests;
    }

    public User[] getProtectionOffers() {
        return protectionOffers;
    }
}
