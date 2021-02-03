package com.macode.supapp.utilities;

public class Comments {
    private String comment, commentDate, profileImageUrl, username;

    public Comments() {
    }

    public Comments(String comment, String commentDate, String profileImageUrl, String username) {
        this.comment = comment;
        this.commentDate = commentDate;
        this.profileImageUrl = profileImageUrl;
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
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
}
