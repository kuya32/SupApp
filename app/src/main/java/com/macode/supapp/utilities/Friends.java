package com.macode.supapp.utilities;

public class Friends {

    private String profileImageUrl, username, profession;

    public Friends() {
    }

    public Friends(String profileImageUrl, String username, String profession, String online) {
        this.profileImageUrl = profileImageUrl;
        this.username = username;
        this.profession = profession;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }
}
