package com.example.truyenchu.model;

import com.google.firebase.firestore.DocumentId;

public class User {
//    @DocumentId
    private String uid;

    private String email;
    private String displayName;
    private String phoneNumber;
    private String profileImage;;
    private String userType;

    private Long  timestamp;

    public User() {
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getUserType() {
        return userType;
    }

    public Long  getTimestamp() {
        return timestamp;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}