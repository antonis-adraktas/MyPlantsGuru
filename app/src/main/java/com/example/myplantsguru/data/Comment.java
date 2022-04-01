package com.example.myplantsguru.data;

public class Comment {
    private String user;
    private String comment;

    public Comment(String user, String comment) {
        this.user = user;
        this.comment = comment;
    }

    public Comment() {
    }

    public String getUser() {
        return user;
    }

    public String getComment() {
        return comment;
    }
}
