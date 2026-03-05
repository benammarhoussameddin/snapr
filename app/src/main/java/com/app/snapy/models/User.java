package com.app.snapy.models;

import java.io.Serializable;

public class User implements Serializable {

    private String uid, email, profileUrl;

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User() {
    }

    public User(String uid, String email, String profileUrl) {
        this.uid = uid;
        this.email = email;
        this.profileUrl = profileUrl;
    }
}
