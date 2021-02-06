package com.macode.supapp.utilities;

public class Chats {

    private String message, status, userId;

    public Chats() {
    }

    public Chats(String message, String status, String userId) {
        this.message = message;
        this.status = status;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
