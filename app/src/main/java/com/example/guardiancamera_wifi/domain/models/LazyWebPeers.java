package com.example.guardiancamera_wifi.domain.models;


/**
 * Container Class.
 * Contains the list of current user's peers.
 *
 */
public class LazyWebPeers {
    private LazyWebUser[] protecteds = {};
    private LazyWebUser[] guardians = {};
    private LazyWebUser[] protectionRequests = {};
    private LazyWebUser[] protectionOffers = {};


    public void setProtecteds(LazyWebUser[] protecteds) {
        this.protecteds = protecteds;
    }

    public void setGuardians(LazyWebUser[] guardians) {
        this.guardians = guardians;
    }

    public void setProtectionRequests(LazyWebUser[] protectionRequests) {
        this.protectionRequests = protectionRequests;
    }

    public void setProtectionOffers(LazyWebUser[] protectionOffers) {
        this.protectionOffers = protectionOffers;
    }

    public LazyWebUser[] getProtecteds() {
        return protecteds;
    }

    public LazyWebUser[] getGuardians() {
        return guardians;
    }

    public LazyWebUser[] getProtectionRequests() {
        return protectionRequests;
    }

    public LazyWebUser[] getProtectionOffers() {
        return protectionOffers;
    }
}
